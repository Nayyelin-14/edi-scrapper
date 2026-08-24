#!/usr/bin/env bash
# EDI Spec Scrapper Service - end-to-end API test suite.
# Usage:  bash samples/test-api.sh          (app must be on :9010)
#         BASE_URL=http://localhost:9000 bash samples/test-api.sh
#
# NOTE: steps 4-5 perform LIVE scraping of public EDIFACT spec sites
#       and take a few minutes. Set QUICK=1 to skip them.

set -u
BASE_URL="${BASE_URL:-http://localhost:9010}"
API="$BASE_URL/api"
QUICK="${QUICK:-0}"
DIR="$(cd "$(dirname "$0")" && pwd)"
PASS=0; FAIL=0

check() {
  if [ "$2" -eq 0 ]; then echo "PASS: $1"; PASS=$((PASS+1)); else echo "FAIL: $1"; FAIL=$((FAIL+1)); fi
}

echo "== 1. Health =="
curl -sf "$API/scrapper/health" | grep -q '"status":"UP"' ; check "scrapper health UP" $?

echo "== 2. Config schema =="
curl -sf "$API/scrapper/config-schema" | grep -q '"standard"' ; check "config-schema lists options" $?

echo "== 3. Catalog endpoints =="
REV=$(curl -sf "$API/edifact/revisions" | python3 -c 'import json,sys; print(json.load(sys.stdin)[-1])')
[ -n "$REV" ] ; check "edifact revisions (last=$REV)" $?
MT=$(curl -sf "$API/edifact/message-types/$REV" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d[0].get("value") or d[0].get("messageType") or list(d[0].values())[0])')
[ -n "$MT" ] ; check "message types for $REV (first=$MT)" $?
XREV=$(curl -sf "$API/x12/revisions" | python3 -c 'import json,sys; print(json.load(sys.stdin)[0])')
[ -n "$XREV" ] ; check "x12 revisions (first=$XREV)" $?

if [ "$QUICK" != "1" ]; then
  echo "== 4. SSE scrape of $REV/$MT (live, watch progress events) =="
  curl -sN --max-time 600 "$API/scrape?standard=EDIFACT&revision=$(python3 -c "import urllib.parse,sys;print(urllib.parse.quote('$REV'))")&messageType=$(python3 -c "import urllib.parse,sys;print(urllib.parse.quote('$MT'))")" > /tmp/opencode/sse.log
  grep -q 'event:progress' /tmp/opencode/sse.log ; check "SSE emitted progress events" $?
  grep -q 'event:done'     /tmp/opencode/sse.log ; check "SSE emitted done event" $?

  echo "== 5. POST /execute scrape (canonical + schema + beanio) =="
  curl -sf -X POST "$API/scrapper/execute" -H 'Content-Type: application/json' \
    -d "{\"standard\":\"EDIFACT\",\"revision\":\"$REV\",\"messageType\":\"$MT\"}" > /tmp/opencode/scrape_result.json
  check "execute returned result" $?
  grep -q '"success":true' /tmp/opencode/scrape_result.json ; check "result success:true" $?

  echo "== 6. Output artifacts on disk (app writes to ./output relative to its working dir) =="
  APPDIR="${APP_WORKDIR:-$(cd "$DIR/.." && pwd)}"
  ls "$APPDIR/output/$REV/$MT.json"        > /dev/null 2>&1 ; check "$MT.json written" $?
  ls "$APPDIR/output/$REV/$MT.schema.json" > /dev/null 2>&1 ; check "$MT.schema.json written" $?
  ls "$APPDIR/output/$REV/$MT.beanio.xml"  > /dev/null 2>&1 ; check "$MT.beanio.xml written" $?

  echo "== 7. Result endpoints for scraped spec =="
  curl -sf "$API/result/$REV/$MT?format=jsonschema" | head -c 200 | grep -qi 'schema\|type' ; check "result format=jsonschema" $?
  curl -sf "$API/result/$REV/$MT?format=beanio" | head -c 200 | grep -qi '<beanio\|<?xml' ; check "result format=beanio" $?
fi

echo
echo "Results: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
