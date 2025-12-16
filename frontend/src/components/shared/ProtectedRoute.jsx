import { Navigate } from 'react-router-dom';
import useAuthStore from '@/lib/stores/authStore';
import { USER_TYPES } from '@/utils/constants';

export default function ProtectedRoute({ children, requiredUserType }) {
  const { isAuthenticated, userType } = useAuthStore();

  // Not authenticated - redirect to login
  if (!isAuthenticated) {
    const loginPath = requiredUserType === USER_TYPES.PROVIDER
      ? '/provider/login'
      : '/learner/login';
    return <Navigate to={loginPath} replace />;
  }

  // Wrong user type - redirect to correct dashboard
  if (requiredUserType && userType !== requiredUserType) {
    const redirectPath = userType === USER_TYPES.PROVIDER
      ? '/provider/dashboard'
      : '/learner/dashboard';
    return <Navigate to={redirectPath} replace />;
  }

  return children;
}