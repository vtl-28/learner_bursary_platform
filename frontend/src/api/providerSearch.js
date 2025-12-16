import apiClient from './client';

export const providerSearchAPI = {
  // Search learners
  searchLearners: async (params) => {
    const response = await apiClient.get('/providers/learners/search', { params });
    return response.data;
  },

  // Get learner profile
  getLearnerProfile: async (learnerId) => {
    const response = await apiClient.get(`/providers/learners/${learnerId}/profile`);
    return response.data;
  },
};