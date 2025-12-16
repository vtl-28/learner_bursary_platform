import axios from 'axios';
import { API_BASE_URL, STORAGE_KEYS, USER_TYPES } from '@/utils/constants';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  (config) => {
    // Determine user type from URL
    const isProviderEndpoint = config.url.includes('/providers/');
    const tokenKey = isProviderEndpoint ? STORAGE_KEYS.PROVIDER_TOKEN : STORAGE_KEYS.LEARNER_TOKEN;

    const token = localStorage.getItem(tokenKey);

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // Server responded with error
      const { status, data } = error.response;

      // Handle 401 Unauthorized
      if (status === 401) {
        // Clear auth data
        localStorage.removeItem(STORAGE_KEYS.LEARNER_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.LEARNER_USER);
        localStorage.removeItem(STORAGE_KEYS.PROVIDER_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.PROVIDER_USER);

        // Redirect to login
        const isProviderRoute = window.location.pathname.includes('/provider');
        window.location.href = isProviderRoute ? '/provider/login' : '/learner/login';
      }

      // Return formatted error
      return Promise.reject({
        status,
        message: data.message || 'An error occurred',
        errors: data.validationErrors || null,
      });
    } else if (error.request) {
      // Request made but no response
      return Promise.reject({
        status: 0,
        message: 'No response from server. Please check your connection.',
      });
    } else {
      // Something else happened
      return Promise.reject({
        status: 0,
        message: error.message || 'An unexpected error occurred',
      });
    }
  }
);

export default apiClient;