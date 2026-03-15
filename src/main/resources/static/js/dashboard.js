/**
 * Pason Alert Engine — Dashboard JavaScript
 *
 * Fetches data from the REST API and updates the dashboard UI.
 * Uses vanilla JS with fetch() and safe DOM manipulation — no innerHTML
 * to prevent XSS vulnerabilities. No framework needed since this
 * is a supplementary dashboard for a Java backend project.
 */

const API_BASE = '/api';
let pollInterval = null;

// ────────────────────────────────────────────────
// Initialization
// ────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  refreshAll();
  startPolling();
});

function startPolling() {
  if (pollInterval) return;
  pollInterval = setInterval(refreshAll, 3000);
}

// ────────────────────────────────────────────────
// API Calls
// ────────────────────────────────────────────────

async function fetchJson(path) {
  const response = await fetch(API_BASE + path);
  if (!response.ok) {
    throw new Error('API error: ' + response.status);
  }
  return response.json();
}

async function postJson(path) {
  const response = await fetch(API_BASE + path, { method: 'POST' });
  if (!response.ok) {
    throw new Error('API error: ' + response.status);
  }
  return response.json();
}

// ────────────────────────────────────────────────
// Safe DOM Helpers
// ────────────────────────────────────────────────

function clearElement(el) {
  while (el.firstChild) {
    el.removeChild(el.firstChild);
  }
}

function createElement(tag, className, textContent) {
  const el = document.createElement(tag);
  if (className) el.className = className;
  if (textContent) el.textContent = textContent;
  return el;
}

// ────────────────────────────────────────────────
// Refresh Functions
// ────────────────────────────────────────────────

async function refreshAll() {
  try {
    await Promise.all([
      refreshStatus(),
      refreshAlerts(),
      refreshReadings(),
      refreshRules()
    ]);
  } catch (err) {
    console.error('Refresh failed:', err);
  }
}

async function refreshStatus() {
  try {
    const status = await fetchJson('/engine/status');
    const statusEl = document.getElementById('engine-status');
    const readingsEl = document.getElementById('readings-count');
    const alertsEl = document.getElementById('alerts-count');

    if (status.simulatorRunning) {
      statusEl.textContent = 'LIVE';
      statusEl.className = 'status-badge status-online';
      document.getElementById('btn-start').disabled = true;
      document.getElementById('btn-stop').disabled = false;
    } else {
      statusEl.textContent = 'OFFLINE';
      statusEl.className = 'status-badge status-offline';
      document.getElementById('btn-start').disabled = false;
      document.getElementById('btn-stop').disabled = true;
    }

    readingsEl.textContent = 'Readings: ' + status.readingsProcessed.toLocaleString();
    alertsEl.textContent = 'Alerts: ' + status.alertsTriggered.toLocaleString();

    // Format uptime from seconds to human-readable
    var uptimeEl = document.getElementById('uptime');
    var secs = status.uptimeSeconds;
    var h = Math.floor(secs / 3600);
    var m = Math.floor((secs % 3600) / 60);
    var s = secs % 60;
    uptimeEl.textContent = 'Uptime: '
        + (h > 0 ? h + 'h ' : '') + (m > 0 ? m + 'm ' : '') + s + 's';
  } catch (err) {
    // Status endpoint may not be ready yet
  }
}

async function refreshAlerts() {
  try {
    const alerts = await fetchJson('/alerts?limit=30');
    const feed = document.getElementById('alerts-feed');
    const badge = document.getElementById('alert-badge');

    badge.textContent = alerts.length;
    clearElement(feed);

    if (alerts.length === 0) {
      feed.appendChild(createElement('p', 'empty-state',
        'No alerts triggered yet. Start the simulator to begin.'));
      return;
    }

    alerts.forEach(function(alert) {
      const card = createElement('div', 'alert-card severity-' + alert.severity);

      const header = createElement('div', 'alert-header');
      header.appendChild(createElement('span', 'alert-rule', alert.ruleName));
      header.appendChild(createElement('span', 'alert-severity ' + alert.severity, alert.severity));
      card.appendChild(header);

      card.appendChild(createElement('div', 'alert-message', alert.message));
      card.appendChild(createElement('div', 'alert-meta',
        alert.sensorId + ' \u00B7 ' + formatTime(alert.timestamp)));

      feed.appendChild(card);
    });
  } catch (err) {
    // Alerts may not be available yet
  }
}

async function refreshReadings() {
  try {
    const readings = await fetchJson('/readings?limit=25');
    const feed = document.getElementById('sensor-feed');
    clearElement(feed);

    if (readings.length === 0) {
      feed.appendChild(createElement('p', 'empty-state', 'Waiting for sensor data...'));
      return;
    }

    readings.forEach(function(reading) {
      const valueClass = getValueClass(reading.sensorType, reading.value);
      const card = createElement('div', 'reading-card');

      const left = createElement('div');
      left.appendChild(createElement('div', 'reading-sensor', reading.sensorId));
      left.appendChild(createElement('div', 'reading-time', formatTime(reading.timestamp)));
      card.appendChild(left);

      card.appendChild(createElement('div', 'reading-value ' + valueClass,
        reading.value.toFixed(1) + ' ' + reading.unit));

      feed.appendChild(card);
    });
  } catch (err) {
    // Readings may not be available yet
  }
}

async function refreshRules() {
  try {
    const rules = await fetchJson('/rules');
    const container = document.getElementById('rules-list');
    clearElement(container);

    if (rules.length === 0) {
      container.appendChild(createElement('p', 'empty-state', 'No alert rules configured.'));
      return;
    }

    rules.forEach(function(rule) {
      const card = createElement('div', 'rule-card');

      const header = createElement('div', 'rule-header');
      header.appendChild(createElement('span', 'rule-name', rule.name));
      header.appendChild(createElement('span',
        'rule-enabled ' + (rule.enabled ? 'active' : 'disabled'),
        rule.enabled ? 'ACTIVE' : 'DISABLED'));
      card.appendChild(header);

      card.appendChild(createElement('div', 'rule-detail', 'Sensor: ' + rule.sensorType));
      card.appendChild(createElement('div', 'rule-detail', 'Condition: ' + rule.conditionDescription));
      card.appendChild(createElement('div', 'rule-detail', 'Severity: ' + rule.severity));

      const actions = createElement('div', 'rule-actions');
      const toggleBtn = createElement('button', 'btn-toggle', rule.enabled ? 'Disable' : 'Enable');
      toggleBtn.addEventListener('click', function() { toggleRule(rule.id); });
      actions.appendChild(toggleBtn);
      card.appendChild(actions);

      container.appendChild(card);
    });
  } catch (err) {
    // Rules may not be loaded yet
  }
}

// ────────────────────────────────────────────────
// Actions
// ────────────────────────────────────────────────

async function startSimulator() {
  try {
    await postJson('/simulator/start');
    refreshAll();
  } catch (err) {
    console.error('Failed to start simulator:', err);
  }
}

async function stopSimulator() {
  try {
    await postJson('/simulator/stop');
    refreshAll();
  } catch (err) {
    console.error('Failed to stop simulator:', err);
  }
}

async function toggleRule(id) {
  try {
    await fetch(API_BASE + '/rules/' + id + '/toggle', { method: 'PATCH' });
    refreshRules();
  } catch (err) {
    console.error('Failed to toggle rule:', err);
  }
}

// ────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────

/**
 * Returns a CSS class for the reading value based on whether
 * it is in a normal, warning, or critical range for its sensor type.
 */
function getValueClass(sensorType, value) {
  switch (sensorType) {
    case 'PRESSURE':
      if (value > 3500) return 'critical';
      if (value > 3000) return 'warning';
      return '';
    case 'TEMPERATURE':
      if (value > 275) return 'critical';
      if (value > 250) return 'warning';
      return '';
    case 'FLOW_RATE':
      if (value < 200 || value > 700) return 'critical';
      if (value < 250 || value > 650) return 'warning';
      return '';
    case 'GAS_LEVEL':
      if (value > 20) return 'critical';
      if (value > 15) return 'warning';
      return '';
    case 'ROTARY_SPEED':
      if (value < 30 || value > 180) return 'critical';
      if (value < 40 || value > 160) return 'warning';
      return '';
    default:
      return '';
  }
}

function formatTime(timestamp) {
  var date = new Date(timestamp);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
}
