import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import useAuthStore from '@/lib/stores/authStore';
import { USER_TYPES, ROUTES } from '@/utils/constants';
import { Toaster } from '@/components/ui/toaster';

// Layouts
import LearnerLayout from '@/components/layouts/LearnerLayout.jsx';
import ProviderLayout from '@/components/layouts/ProviderLayout.jsx';
import ProtectedRoute from '@/components/shared/ProtectedRoute.jsx';

// Pages
import LandingPage from '@/pages/LandingPage.jsx';
import LearnerDashboard from '@/pages/learner/Dashboard.jsx';
import ProviderDashboard from '@/pages/provider/Dashboard.jsx';
import BursariesPage from '@/pages/learner/BursariesPage.jsx';
import Applications from '@/pages/learner/Applications.jsx';
import Profile from '@/pages/learner/Profile.jsx';
import Notifications from '@/pages/learner/Notifications.jsx';
import ProviderApplications from '@/pages/provider/Applications.jsx';
import SearchLearners from './pages/provider/SearchLearners.jsx';
import Following from './pages/provider/Following.jsx';

// Auth Pages
import LearnerLogin from '@/pages/auth/LearnerLogin.jsx';
import LearnerSignup from '@/pages/auth/LearnerSignup.jsx';
import ProviderLogin from '@/pages/auth/ProviderLogin.jsx';

function App() {
  const { initAuth } = useAuthStore();

  useEffect(() => {
    initAuth();
  }, [initAuth]);

  return (
    <BrowserRouter>
      <Routes>
        {/* Landing Page */}
        <Route path="/" element={<LandingPage />} />

        {/* Auth Routes */}
        <Route path="/learner/login" element={<LearnerLogin />} />
        <Route path="/learner/signup" element={<LearnerSignup />} />
        <Route path="/provider/login" element={<ProviderLogin />} />

        {/* Learner Routes */}
        <Route
          path="/learner"
          element={
            <ProtectedRoute requiredUserType={USER_TYPES.LEARNER}>
              <LearnerLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<LearnerDashboard />} />
          <Route path="bursaries" element={<BursariesPage />} />
          <Route path="applications" element={<Applications />} />
         <Route path="notifications" element={<Notifications />} />
          <Route path="profile" element={<Profile />} />
        </Route>

        {/* Provider Routes */}
        <Route
          path="/provider"
          element={
            <ProtectedRoute requiredUserType={USER_TYPES.PROVIDER}>
              <ProviderLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<ProviderDashboard />} />
          <Route path="applications" element={<ProviderApplications />} />
          <Route path="search" element={<SearchLearners />} />
          <Route path="following" element={<Following />} />
          <Route path="notifications" element={<div>Notifications Page (Coming Soon)</div>} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toaster />
    </BrowserRouter>
  );
}

export default App;