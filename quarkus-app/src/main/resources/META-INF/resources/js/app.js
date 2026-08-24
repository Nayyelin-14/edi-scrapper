/* ── EDI Spec Scraper – React frontend (CDN, no Node) ─── */
const { useState, useEffect, useRef, createElement: ce } = React;
const html = htm.bind(ce);

const API_URL = '';

// ── Data ──────────────────────────────────────────────────
const standards = [
  { label: 'EDIFACT', value: 'EDIFACT' },
  { label: 'X12', value: 'X12' },
];

// ── Helpers ───────────────────────────────────────────────
function statusClass(status) {
  switch ((status || '').toLowerCase()) {
    case 'mandatory': return 'status-mandatory';
    case 'conditional': return 'status-conditional';
    case 'optional': return 'status-optional';
    default: return 'status-default';
  }
}

function groupLabel(item) {
  if (item.Segment && item.Segment.startsWith('Loop_'))
    return (item.Name || item.Segment.replace('Loop_', '')) + ' Loop';
  return (item.Name || item.Segment) + ' Loop';
}

function downloadJson(data, filename) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = filename;
  document.body.appendChild(a); a.click();
  document.body.removeChild(a); URL.revokeObjectURL(url);
}

// ── DetailRow component ───────────────────────────────────
function DetailRow({ item }) {
  const [open, setOpen] = useState(false);
  const children = item.Elements;
  const hasChildren = Array.isArray(children) && children.length > 0;

  return html`
    <div>
      <div class="detail-row ${hasChildren ? 'clickable' : ''}"
           onClick=${hasChildren ? () => setOpen(!open) : undefined}>
        <span class="tree-pos">${item.Position}</span>
        <span class="detail-code">${item.Element}</span>
        <div>
          <span class="tree-name">${item.Name}
            ${hasChildren && html`<span class="tree-expand">${open ? '▾' : '▸'}</span>`}
          </span>
          ${item.Type && html`<div class="detail-type">${item.Type} (${item.Min}–${item.Max})</div>`}
        </div>
        <span class="status-badge ${statusClass(item.Requirement)}">${item.Requirement}</span>
        <span class="tree-max">${item.Max ? 'Max ' + item.Max : ''}</span>
      </div>
      ${hasChildren && open && html`
        <div class="detail-children">
          ${children.map((sub, i) => html`<${DetailRow} key=${i} item=${sub} />`)}
        </div>
      `}
    </div>
  `;
}

// ── ItemRow component ─────────────────────────────────────
function ItemRow({ item }) {
  const [expanded, setExpanded] = useState(false);
  const details = item.Elements;
  const hasDetails = Array.isArray(details) && details.length > 0;

  return html`
    <div style=${{ borderBottom: '1px solid var(--border)' }}>
      <div class="tree-row ${hasDetails ? 'clickable' : ''}"
           onClick=${hasDetails ? () => setExpanded(!expanded) : undefined}>
        <span class="tree-pos">${item.Position}</span>
        <span>
          <span class="tree-code">${item.Segment}</span>
          ${hasDetails && html`<span class="tree-expand">${expanded ? '▾' : '▸'}</span>`}
        </span>
        <span class="tree-name">${item.Name}</span>
        <span class="status-badge ${statusClass(item.Requirement)}">${item.Requirement}</span>
        <span class="tree-max">${item.Max ? 'Max ' + item.Max : ''}</span>
      </div>
      ${item.Description && html`
        <div style=${{ fontSize: '0.75rem', color: 'var(--text-secondary)', padding: '0 12px 8px', paddingLeft: '148px' }}>
          ${item.Description}
        </div>
      `}
      ${hasDetails && expanded && html`
        <div class="detail-container fade-in">
          <div class="detail-header">
            <span>Pos</span><span>Element</span><span>Name</span><span>Requirement</span><span style=${{ textAlign: 'right' }}>Max</span>
          </div>
          ${details.map((el, i) => html`<${DetailRow} key=${i} item=${el} />`)}
        </div>
      `}
    </div>
  `;
}

// ── GroupBlock component ───────────────────────────────────
function GroupBlock({ item }) {
  const [open, setOpen] = useState(true);
  const children = item.Segments || [];

  return html`
    <div class="group-block">
      <div class="group-header" onClick=${() => setOpen(!open)}>
        <div class="group-header-left">
          <span class="tree-expand">${open ? '▾' : '▸'}</span>
          <span class="group-label">${groupLabel(item)}</span>
          <span class="status-badge ${statusClass(item.Requirement)}">${item.Requirement}</span>
        </div>
        <span class="group-max">${item.Max ? 'Repeat ' + item.Max : ''}</span>
      </div>
      ${open && html`
        <div>
          ${children.map((child, i) =>
            Array.isArray(child.Segments)
              ? html`<${GroupBlock} key=${i} item=${child} />`
              : html`<${ItemRow} key=${i} item=${child} />`
          )}
        </div>
      `}
    </div>
  `;
}

// ── TreeDocView component ─────────────────────────────────
function TreeDocView({ data }) {
  const items = data && data.Segments;
  if (!Array.isArray(items) || items.length === 0) return null;

  return html`
    <div>
      <div class="tree-title">${data.Standard} ${data.Revision} — ${data.Document}</div>
      <div class="tree-columns">
        <span>Position</span><span>Segment</span><span>Name</span><span></span><span style=${{ textAlign: 'right' }}>Max use</span>
      </div>
      <div>
        ${items.map((item, i) =>
          Array.isArray(item.Segments)
            ? html`<${GroupBlock} key=${i} item=${item} />`
            : html`<${ItemRow} key=${i} item=${item} />`
        )}
      </div>
    </div>
  `;
}

// ── Main App component ────────────────────────────────────
function App() {
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const [standard, setStandard] = useState('');
  const [revision, setRevision] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [scrapeTime, setScrapeTime] = useState(null);
  const [progress, setProgress] = useState(null);
  // Dynamic revision/message data (loaded from API)
  const [edifactRevisions, setEdifactRevisions] = useState([]);
  const [edifactMessageTypes, setEdifactMessageTypes] = useState([]);
  const [x12Revisions, setX12Revisions] = useState([]);
  const [x12TransactionSets, setX12TransactionSets] = useState([]);
  const [publishing, setPublishing] = useState(false);
  const [publishDialog, setPublishDialog] = useState(false);
  const [snackbar, setSnackbar] = useState(null);
  const startTimeRef = useRef(null);
  const esRef = useRef(null);

  // Apply theme
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  // Fetch EDIFACT revisions dynamically
  useEffect(() => {
    fetch(API_URL + '/api/edifact/revisions')
      .then(r => r.json())
      .then(data => {
        if (Array.isArray(data))
          setEdifactRevisions(data.map(v => typeof v === 'string' ? { label: v, value: v } : { label: v.label || v.value, value: v.value || v.label }));
      })
      .catch(() => {});
  }, []);

  // Fetch X12 revisions dynamically
  useEffect(() => {
    fetch(API_URL + '/api/x12/revisions')
      .then(r => r.json())
      .then(data => {
        if (Array.isArray(data))
          setX12Revisions(data.map(v => typeof v === 'string' ? { label: v, value: v } : { label: v.label || v.value, value: v.value || v.label }));
      })
      .catch(() => {});
  }, []);

  // Fetch EDIFACT message types when revision changes
  useEffect(() => {
    if (standard === 'EDIFACT' && revision) {
      setEdifactMessageTypes([]);
      fetch(API_URL + '/api/edifact/message-types/' + encodeURIComponent(revision))
        .then(r => r.json())
        .then(data => {
          if (Array.isArray(data))
            setEdifactMessageTypes(data.map(mt => ({ label: mt.code + ' - ' + mt.name, value: mt.code })));
        })
        .catch(() => {});
    }
  }, [standard, revision]);

  // Fetch X12 transaction sets when revision changes
  useEffect(() => {
    if (standard === 'X12' && revision) {
      setX12TransactionSets([]);
      fetch(API_URL + '/api/x12/transaction-sets/' + encodeURIComponent(revision))
        .then(r => r.json())
        .then(data => {
          if (Array.isArray(data))
            setX12TransactionSets(data.map(ts => ({ label: ts.code + ' - ' + ts.name, value: ts.code })));
        })
        .catch(() => {});
    }
  }, [standard, revision]);

  const [beanioXml, setBeanioXml] = useState(null);
  const [beanioLoading, setBeanioLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('tree'); // 'tree' or 'beanio'

  const revisionOptions = standard === 'EDIFACT' ? edifactRevisions : standard === 'X12' ? x12Revisions : [];
  const messageOptions = standard === 'EDIFACT' ? edifactMessageTypes : standard === 'X12' ? x12TransactionSets : [];
  const segmentCount = result && result.Segments ? result.Segments.length : 0;

  function handleSubmit(e) {
    e.preventDefault();
    if (!standard || !revision || !messageType) { setError('All fields are required'); return; }
    if (esRef.current) esRef.current.close();

    setLoading(true); setError(null); setResult(null); setProgress(null); setBeanioXml(null); setActiveTab('tree');
    startTimeRef.current = Date.now();

    const params = new URLSearchParams({ standard, revision, messageType });
    const es = new EventSource(API_URL + '/api/scrape?' + params);
    esRef.current = es;

    es.addEventListener('progress', e => setProgress(JSON.parse(e.data)));

    es.addEventListener('done', async e => {
      es.close();
      const d = JSON.parse(e.data);
      const url = API_URL + '/api/result/' + encodeURIComponent(d.revision) + '/' + encodeURIComponent(d.messageType);
      for (let attempt = 1; attempt <= 3; attempt++) {
        try {
          if (attempt > 1) await new Promise(r => setTimeout(r, attempt * 1000));
          const res = await fetch(url);
          if (!res.ok) throw new Error('Failed (' + res.status + ')');
          const data = await res.json();
          setScrapeTime(((Date.now() - startTimeRef.current) / 1000).toFixed(1));
          setResult(data); setProgress(null); setLoading(false);
          return;
        } catch (err) {
          if (attempt === 3) { setError(err.message); setProgress(null); setLoading(false); }
        }
      }
    });

    es.addEventListener('error', e => {
      if (e.data) { const d = JSON.parse(e.data); setError(d.message || 'Scraping error'); }
      else setError('Connection lost — server may be down');
      setProgress(null); setLoading(false); es.close();
    });
  }

  async function handleDownloadSchema() {
    try {
      const res = await fetch(API_URL + '/api/result/' + encodeURIComponent(revision) + '/' + encodeURIComponent(messageType) + '?format=jsonschema');
      if (!res.ok) throw new Error('Failed (' + res.status + ')');
      const schema = await res.json();
      downloadJson(schema, standard + '_' + revision + '_' + messageType + '.schema.json');
    } catch (err) {
      setSnackbar({ message: 'Download failed: ' + err.message, type: 'error' });
    }
  }

  async function handleDownloadBeanio() {
    try {
      const res = await fetch(API_URL + '/api/result/' + encodeURIComponent(revision) + '/' + encodeURIComponent(messageType) + '?format=beanio');
      if (!res.ok) throw new Error('Failed (' + res.status + ')');
      const xml = await res.text();
      const blob = new Blob([xml], { type: 'application/xml' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = standard + '_' + revision + '_' + messageType + '.beanio.xml';
      document.body.appendChild(a); a.click();
      document.body.removeChild(a); URL.revokeObjectURL(url);
    } catch (err) {
      setSnackbar({ message: 'Download failed: ' + err.message, type: 'error' });
    }
  }

  async function fetchBeanioPreview() {
    if (beanioXml) { setActiveTab('beanio'); return; }
    setBeanioLoading(true);
    try {
      const res = await fetch(API_URL + '/api/result/' + encodeURIComponent(revision) + '/' + encodeURIComponent(messageType) + '?format=beanio');
      if (!res.ok) throw new Error('Failed (' + res.status + ')');
      const xml = await res.text();
      setBeanioXml(xml);
      setActiveTab('beanio');
    } catch (err) {
      setSnackbar({ message: 'Failed to load BeanIO XML: ' + err.message, type: 'error' });
    } finally { setBeanioLoading(false); }
  }

  async function handlePublish() {
    setPublishDialog(false); setPublishing(true);
    try {
      const res = await fetch(API_URL + '/api/publish', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ standard, revision, messageType }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Publish failed');
      setSnackbar({ message: 'Published to ' + data.path, type: 'success' });
    } catch (err) {
      setSnackbar({ message: 'Publish failed: ' + err.message, type: 'error' });
    } finally { setPublishing(false); }
  }

  // Auto-hide snackbar
  useEffect(() => {
    if (snackbar) { const t = setTimeout(() => setSnackbar(null), 5000); return () => clearTimeout(t); }
  }, [snackbar]);

  return html`
    <div>
      <!-- App Bar -->
      <div class="app-bar">
        <h1>EDI Spec Scraper</h1>
        <button class="theme-toggle" onClick=${() => setTheme(theme === 'light' ? 'dark' : 'light')}>
          ${theme === 'light' ? '🌙' : '☀️'}
        </button>
      </div>

      <div class="container">
        <!-- Form Card -->
        <div class="card">
          <h2 style=${{ fontSize: '1.15rem', marginBottom: '16px' }}>Scrape EDI Specification</h2>
          <form onSubmit=${handleSubmit}>
            <div class="form-row">
              <div class="form-group">
                <label>Standard</label>
                <select value=${standard} onChange=${e => { setStandard(e.target.value); setRevision(''); setMessageType(''); setResult(null); }}>
                  <option value="">Select...</option>
                  ${standards.map(s => html`<option key=${s.value} value=${s.value}>${s.label}</option>`)}
                </select>
              </div>
              <div class="form-group">
                <label>Revision</label>
                <select value=${revision} onChange=${e => { setRevision(e.target.value); setMessageType(''); }}
                        disabled=${!standard}>
                  <option value="">Select...</option>
                  ${revisionOptions.map(r => html`<option key=${r.value} value=${r.value}>${r.label}</option>`)}
                </select>
              </div>
              <div class="form-group">
                <label>Message Type</label>
                ${messageOptions.length > 0
                  ? html`<select value=${messageType} onChange=${e => setMessageType(e.target.value)} disabled=${!standard}>
                      <option value="">Select...</option>
                      ${messageOptions.map(m => html`<option key=${m.value} value=${m.value}>${m.label}</option>`)}
                    </select>`
                  : html`<input type="text" value=${messageType} onChange=${e => setMessageType(e.target.value)}
                          placeholder="Enter message type..." disabled=${!standard} />`
                }
              </div>
            </div>
            <button type="submit" class="btn btn-primary" disabled=${loading}>
              ${loading ? html`<span class="spinner"></span> Scraping...` : '🔍 Scrape Specification'}
            </button>
          </form>
        </div>

        <!-- Progress -->
        ${loading && progress && html`
          <div class="card fade-in">
            <div class="progress-info">
              <span>
                ${progress.phase === 'segments'
                  ? html`Fetching segment <strong>${progress.segment}</strong>`
                  : progress.message}
              </span>
              ${progress.phase === 'segments' && html`<strong>${progress.current} / ${progress.total}</strong>`}
            </div>
            <div class="progress-bar-track">
              ${progress.phase === 'segments'
                ? html`<div class="progress-bar-fill" style=${{ width: (progress.current / progress.total * 100) + '%' }}></div>`
                : html`<div class="progress-bar-indeterminate"></div>`
              }
            </div>
          </div>
        `}

        <!-- Error -->
        ${error && html`
          <div class="alert alert-error fade-in">
            <span>${error}</span>
            <button class="btn btn-sm" onClick=${() => { setError(null); handleSubmit({ preventDefault: () => {} }); }}>
              ↻ Retry
            </button>
          </div>
        `}

        <!-- Empty state -->
        ${!result && !error && !loading && html`
          <div class="card card-dashed">
            <div style=${{ fontSize: '2.5rem', marginBottom: '12px' }}>📄</div>
            <h3 style=${{ marginBottom: '8px', color: 'var(--text-secondary)' }}>No results yet</h3>
            <p>Select a standard, revision, and message type to scrape an EDI specification.</p>
          </div>
        `}

        <!-- Results -->
        ${result && html`
          <div class="card fade-in" style=${{ padding: 0, overflow: 'hidden' }}>
            <div class="results-header">
              <div class="results-header-left">
                <strong>Specification</strong>
                ${segmentCount > 0 && html`<span class="chip chip-primary">${segmentCount} segment${segmentCount !== 1 ? 's' : ''}</span>`}
                ${scrapeTime && html`<span class="chip">${scrapeTime}s</span>`}
              </div>
              <div class="results-header-right">
                <button class="btn-icon" title="Download JSON"
                        onClick=${() => downloadJson(result, standard + '_' + revision + '_' + messageType + '.json')}>
                  ⬇ JSON
                </button>
                <button class="btn-icon" title="Download JSON Schema" onClick=${handleDownloadSchema}>
                  🌲 Schema
                </button>
                <button class="btn-icon" title="Download BeanIO XML" onClick=${handleDownloadBeanio}>
                  ⬇ BeanIO
                </button>
                <button class="btn btn-primary btn-sm" onClick=${() => setPublishDialog(true)} disabled=${publishing}>
                  ${publishing ? html`<span class="spinner"></span>` : '☁'} Publish
                </button>
              </div>
            </div>
            <div class="tab-bar">
              <button class="tab ${activeTab === 'tree' ? 'tab-active' : ''}" onClick=${() => setActiveTab('tree')}>
                Tree View
              </button>
              <button class="tab ${activeTab === 'beanio' ? 'tab-active' : ''}" onClick=${fetchBeanioPreview}
                      disabled=${beanioLoading}>
                ${beanioLoading ? html`<span class="spinner-dark"></span>` : ''} BeanIO XML
              </button>
            </div>
            ${activeTab === 'tree' && html`<${TreeDocView} data=${result} />`}
            ${activeTab === 'beanio' && beanioXml && html`
              <div class="beanio-preview">
                <div class="beanio-toolbar">
                  <button class="btn-icon" onClick=${() => { navigator.clipboard.writeText(beanioXml); setSnackbar({ message: 'Copied to clipboard', type: 'success' }); }}>
                    Copy
                  </button>
                </div>
                <pre class="beanio-code"><code>${beanioXml}</code></pre>
              </div>
            `}
          </div>
        `}
      </div>

      <!-- Publish dialog -->
      ${publishDialog && html`
        <div class="modal-overlay" onClick=${() => setPublishDialog(false)}>
          <div class="modal" onClick=${e => e.stopPropagation()}>
            <h3>Publish to Azure DevOps</h3>
            <p>
              This will publish <strong>${standard} ${revision} ${messageType}</strong> specs
              (JSON + JSON Schema) to the integration schema repository at:
              <br/><code>${(standard || '').toLowerCase()}/${revision}/${messageType}/</code>
            </p>
            <div class="modal-actions">
              <button class="btn btn-sm" style=${{ background: 'var(--bg)', border: '1px solid var(--border)', color: 'var(--text)' }}
                      onClick=${() => setPublishDialog(false)}>Cancel</button>
              <button class="btn btn-primary btn-sm" onClick=${handlePublish}>☁ Publish</button>
            </div>
          </div>
        </div>
      `}

      <!-- Snackbar -->
      ${snackbar && html`
        <div class="snackbar alert-${snackbar.type === 'error' ? 'error' : 'success'}"
             style=${{ border: 'none' }}>
          ${snackbar.message}
        </div>
      `}
    </div>
  `;
}

// ── Mount ─────────────────────────────────────────────────
ReactDOM.createRoot(document.getElementById('root')).render(html`<${App} />`);
