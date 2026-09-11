import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import UserDashboardView from './views/UserDashboardView';
import InterventionsView from './views/InterventionsView';
import PrivacyConsentView from './views/PrivacyConsentView';
import AccountView from './views/AccountView';
import ConsultantDashboardView from './views/ConsultantDashboardView';
import ResearchAnalyticsView from './views/ResearchAnalyticsView';
import AndroidSyncSimulator from './components/AndroidSyncSimulator';
import SelfCheckModal from './components/SelfCheckModal';
import { api, DEMO_USER_ID } from './api/client';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [userProfile, setUserProfile] = useState(null);
  const [riskData, setRiskData] = useState(null);
  const [baselineData, setBaselineData] = useState(null);
  const [consentData, setConsentData] = useState(null);
  const [isSelfCheckOpen, setIsSelfCheckOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  // Initial Data Fetch
  const loadUserData = async () => {
    setLoading(true);
    const dashboard = await api.getDashboard();

    if (dashboard) {
      if (dashboard.user) setUserProfile(dashboard.user);
      if (dashboard.current_risk) setRiskData(dashboard.current_risk);
      if (dashboard.consent) setConsentData(dashboard.consent);
    } else {
      const [risk, baseline, consent] = await Promise.all([
        api.getLatestRisk(DEMO_USER_ID),
        api.getUserBaseline(DEMO_USER_ID),
        api.getConsent(DEMO_USER_ID)
      ]);
      if (risk) setRiskData(risk);
      if (baseline) setBaselineData(baseline);
      if (consent) setConsentData(consent);
    }
    setLoading(false);
  };


  useEffect(() => {
    loadUserData();
  }, []);

  const handleInjectPreset = async (presetName) => {
    const updatedRisk = await api.injectSimulatorPreset(DEMO_USER_ID, presetName);
    if (updatedRisk) {
      setRiskData(updatedRisk);
    }
  };

  const handleUpdateConsent = async (nextConsent) => {
    setConsentData(nextConsent);
    await api.updateConsent(DEMO_USER_ID, nextConsent);
  };

  const handleDeleteData = async () => {
    await api.deleteUserData(DEMO_USER_ID);
    await loadUserData();
  };

  const handleSelfCheckSubmit = async (reportData) => {
    await api.submitSelfCheck(DEMO_USER_ID, reportData);
  };

  const handleLogActivity = async (activityType, resultData) => {
    const session = await api.startIntervention(DEMO_USER_ID, activityType);
    if (session && session.session_id) {
      await api.completeIntervention(session.session_id, { feedback_score: 5, result_metrics: resultData });
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenSelfCheck={() => setIsSelfCheckOpen(true)}
      />

      <main style={{ maxWidth: '1400px', width: '100%', margin: '0 auto', padding: '24px' }}>
        {activeTab === 'dashboard' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <AndroidSyncSimulator onSyncCompleted={loadUserData} />
            <UserDashboardView
              riskData={riskData}
              baselineData={baselineData}
              onInjectPreset={handleInjectPreset}
              onNavigateTab={setActiveTab}
              onOpenSelfCheck={() => setIsSelfCheckOpen(true)}
            />
          </div>
        )}

        {activeTab === 'interventions' && (
          <InterventionsView onLogActivity={handleLogActivity} />
        )}

        {activeTab === 'privacy' && (
          <PrivacyConsentView
            consentData={consentData}
            onUpdateConsent={handleUpdateConsent}
            onDeleteData={handleDeleteData}
          />
        )}

        {activeTab === 'account' && (
          <AccountView
            user={userProfile}
            consent={consentData}
            onDeleteData={handleDeleteData}
            onLogout={loadUserData}
          />
        )}

        {activeTab === 'consultant' && (
          <ConsultantDashboardView />
        )}

        {activeTab === 'analytics' && (
          <ResearchAnalyticsView />
        )}
      </main>

      <SelfCheckModal
        isOpen={isSelfCheckOpen}
        onClose={() => setIsSelfCheckOpen(false)}
        onSubmit={handleSelfCheckSubmit}
      />
    </div>
  );
}
