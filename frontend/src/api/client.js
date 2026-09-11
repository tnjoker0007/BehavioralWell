const API_BASE = '/api/v1';

export const DEMO_USER_ID = 'usr_demo12345';

export async function fetchJson(endpoint, options = {}) {
  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });
    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    return await res.json();
  } catch (err) {
    console.warn(`API call ${endpoint} failed, using local simulated fallback`, err);
    return null;
  }
}

export const api = {
  getDashboard: () => fetchJson('/dashboard'),
  getLatestRisk: (userId = DEMO_USER_ID) => fetchJson(`/risk/${userId}/latest`),
  getUserBaseline: (userId = DEMO_USER_ID) => fetchJson(`/telemetry/${userId}/baseline`),
  getConsent: (userId = DEMO_USER_ID) => fetchJson(`/consent/${userId}`),
  updateConsent: (userId = DEMO_USER_ID, consentData) => fetchJson(`/consent/${userId}`, { method: 'PUT', body: JSON.stringify(consentData) }),
  injectSimulatorPreset: (userId = DEMO_USER_ID, presetName) => fetchJson(`/simulator/inject/${userId}`, { method: 'POST', body: JSON.stringify({ preset_name: presetName }) }),
  getInterventions: (userId = DEMO_USER_ID) => fetchJson(`/interventions/${userId}/recommendations`),
  submitSelfCheck: (userId = DEMO_USER_ID, checkData) => fetchJson(`/interventions/${userId}/self-check`, { method: 'POST', body: JSON.stringify(checkData) }),
  startIntervention: (userId = DEMO_USER_ID, activityType) => fetchJson(`/interventions/${userId}/start`, { method: 'POST', body: JSON.stringify({ activity_type: activityType }) }),
  completeIntervention: (sessionId, resultData) => fetchJson(`/interventions/sessions/${sessionId}/complete`, { method: 'POST', body: JSON.stringify(resultData) }),
  deleteUserData: (userId = DEMO_USER_ID) => fetchJson(`/auth/users/${userId}/data`, { method: 'DELETE' }),
  getConsultantPatients: () => fetchJson('/consultant/patients'),
  getResearchAnalytics: () => fetchJson('/analytics/overview'),
};

