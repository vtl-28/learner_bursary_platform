import { create } from 'zustand';
import { STORAGE_KEYS, USER_TYPES } from '@/utils/constants';

const useAuthStore = create((set) => ({
  // State
  user: null,
  token: null,
  userType: null,
  isAuthenticated: false,

  // Actions
  setAuth: (user, token, userType) => {
  console.log('Setting auth:', { user, token: token?.substring(0, 20) + '...', userType });
    // Save to localStorage
    if (userType === USER_TYPES.LEARNER) {
      localStorage.setItem(STORAGE_KEYS.LEARNER_TOKEN, token);
      localStorage.setItem(STORAGE_KEYS.LEARNER_USER, JSON.stringify(user));
    } else {
      localStorage.setItem(STORAGE_KEYS.PROVIDER_TOKEN, token);
      localStorage.setItem(STORAGE_KEYS.PROVIDER_USER, JSON.stringify(user));
    }

console.log('Auth saved to localStorage');

    // Update state
    set({
      user,
      token,
      userType,
      isAuthenticated: true,
    });
  },

  clearAuth: () => {
    // Clear localStorage
    localStorage.removeItem(STORAGE_KEYS.LEARNER_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.LEARNER_USER);
    localStorage.removeItem(STORAGE_KEYS.PROVIDER_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.PROVIDER_USER);

    // Clear state
    set({
      user: null,
      token: null,
      userType: null,
      isAuthenticated: false,
    });
  },

  initAuth: () => {
    try {
      // Try to load learner auth
      const learnerToken = localStorage.getItem(STORAGE_KEYS.LEARNER_TOKEN);
      const learnerUser = localStorage.getItem(STORAGE_KEYS.LEARNER_USER);

      if (learnerToken && learnerUser && learnerUser !== 'undefined') {
        try {
          const parsedUser = JSON.parse(learnerUser);
          set({
            user: parsedUser,
            token: learnerToken,
            userType: USER_TYPES.LEARNER,
            isAuthenticated: true,
          });
          return;
        } catch (e) {
          // Invalid JSON, clear learner data
          localStorage.removeItem(STORAGE_KEYS.LEARNER_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.LEARNER_USER);
        }
      }

      // Try to load provider auth
      const providerToken = localStorage.getItem(STORAGE_KEYS.PROVIDER_TOKEN);
      const providerUser = localStorage.getItem(STORAGE_KEYS.PROVIDER_USER);

      if (providerToken && providerUser && providerUser !== 'undefined') {
        try {
          const parsedUser = JSON.parse(providerUser);
          set({
            user: parsedUser,
            token: providerToken,
            userType: USER_TYPES.PROVIDER,
            isAuthenticated: true,
          });
        } catch (e) {
          // Invalid JSON, clear provider data
          localStorage.removeItem(STORAGE_KEYS.PROVIDER_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.PROVIDER_USER);
        }
      }
    } catch (error) {
      console.error('Error initializing auth:', error);
      // Clear everything on error
      localStorage.removeItem(STORAGE_KEYS.LEARNER_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.LEARNER_USER);
      localStorage.removeItem(STORAGE_KEYS.PROVIDER_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.PROVIDER_USER);
    }
  },
}));

export default useAuthStore;