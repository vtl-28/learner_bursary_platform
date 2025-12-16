import apiClient from './client';

export const learnerAuthAPI = {
  // Signup
  signup: async (data) => {
    const response = await apiClient.post('/learners/signup', data);
    return response.data;
  },

  // Login
  login: async (data) => {
    const response = await apiClient.post('/learners/login', data);
    return response.data;
  },

  // Logout
  logout: async () => {
    const response = await apiClient.post('/learners/logout');
    return response.data;
  },

  // Check email availability
  checkEmail: async (email) => {
    const response = await apiClient.get(`/learners/check-email?email=${email}`);
    return response.data;
  },
};