# EDI Spec Scrapper

Scrapes EDI message specifications (**UN/EDIFACT** and **ANSI X12**) from public
reference sites and converts them into machine-readable artifacts for integration
projects:

| Artifact | Description |
|----------|-------------|
| `<messageType>.json` | Canonical spec model (segments, elements, composites) |
| `<messageType>.schema.json` | JSON Schema generated from the canonical model |
| `<messageType>.beanio.xml` | [BeanIO](https://beanio.github.io/) mapping XML for flat-file parsing |

- **EDIFACT** definitions are scraped from [edifactory.de](https://www.edifactory.de/edifact/directory).
- **X12** transaction set definitions are scraped from [Stedi](https://www.stedi.com/edi).

Results can optionally be published to an Azure DevOps Git repository
(`hubsabai-integration-schema`) using a PAT/OAuth token stored in
`~/.datasabai`.

## Project structure

Multi-module Maven project (Java 25, Quarkus 3.x):

```
core/          Scrapper core library (scrapers, converters, model, publisher)
quarkus-app/   REST API + web UI built on Quarkus (RESTEasy Reactive)
samples/       End-to-end API test script and HTTP request collection
output/        Scraped artifacts (created at runtime, relative to working dir)
screenshots/   UI and API screenshots
```

### Core library (`scrapper-core`)

Key classes under `com.datasabai.services.scrapper.core`:

- `ScrapperService` – main orchestrator; supports `scrape`, `result`, and
  `publish` actions.
- `ScrapperRequest` / `ScrapperResult` – SDK-style request/response objects
  with builders.
- `scraper.EdifactScraper` / `scraper.X12Scraper` – revision/message-type
  catalogs and segment-level scraping, with optional `ScrapeProgressListener`
  callbacks.
- `converter.JsonSchemaConverter` / `converter.BeanioXmlConverter` – artifact
  generation from the canonical model.
- `publisher.AzureDevOpsPublisher` – commits artifacts to Azure DevOps.

## REST API

The Quarkus app serves the UI at `http://localhost:9010/` and exposes:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/scrapper/execute` | Run scrape/result/publish actions |
| GET  | `/api/scrapper/health` | Service health |
| GET  | `/api/scrapper/config-schema` | Request configuration schema |
| GET  | `/api/scrape?standard=..&revision=..&messageType=..` | Live scrape with SSE progress events |
| GET  | `/api/edifact/revisions` | EDIFACT directory revisions |
| GET  | `/api/edifact/message-types/{revision}` | Message types for a revision |
| GET  | `/api/x12/revisions` | X12 versions |
| GET  | `/api/x12/transaction-sets/{revision}` | Transaction sets for a version |
| GET  | `/api/result/{revision}/{messageType}?format=canonical\|jsonschema\|beanio` | Retrieve artifacts |
| POST | `/api/publish` | Publish artifacts to Azure DevOps |

## Getting started

Requirements: **Java 25** and Maven.

```bash
# Build and run tests
mvn clean install

# Start the app in dev mode (port 9010)
mvn quarkus:dev -pl quarkus-app
```

Then open http://localhost:9010/ for the web UI, or exercise the API:

```bash
bash samples/test-api.sh            # full suite incl. live scraping
QUICK=1 bash samples/test-api.sh    # skip live-scrape steps
BASE_URL=http://localhost:9000 bash samples/test-api.sh
```

Example scrape via the unified endpoint:

```bash
curl -X POST http://localhost:9010/api/scrapper/execute \
  -H 'Content-Type: application/json' \
  -d '{"standard":"EDIFACT","revision":"D18A","messageType":"APERAK"}'
```

The request also accepts custom delimiters (`delimiter`,
`recordTerminator`, `componentDelimiter`) and BeanIO options
(`basePackage`, `rootClass`).

Artifacts are written to `./output/<revision>/` relative to the app's
working directory; EDIFACT envelope caches go to `output/.cache/edifact/envelope`.

> **Note:** steps that perform live scraping hit public EDI reference sites and
> may take several minutes per message type.

## Testing

Unit tests live in `core/src/test` and cover the model, converters, composite
type handlers, and revision scraping:

```bash
mvn test -pl core
```
