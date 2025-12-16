import apiClient from './client';

export const learnerAPI = {
  // Get my profile
  getProfile: async () => {
    const response = await apiClient.get('/learners/profile');
    return response.data;
  },

  // Update profile
  updateProfile: async (data) => {
    const response = await apiClient.patch('/learners/profile', data);
    return response.data;
  },
};