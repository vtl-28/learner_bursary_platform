import apiClient from './client';

export const providerAuthAPI = {
  // Login
  login: async (data) => {
    const response = await apiClient.post('/providers/login', data);
    return response.data;
  },

  // Logout
  logout: async () => {
    const response = await apiClient.post('/providers/logout');
    return response.data;
  },
};