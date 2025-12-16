import apiClient from './client';

export const bursariesAPI = {
  // Get all active bursaries
  getAll: async () => {
    const response = await apiClient.get('/bursaries');
    return response.data;
  },

  // Get available bursaries (deadline not passed)
  getAvailable: async () => {
    const response = await apiClient.get('/bursaries/available');
    return response.data;
  },

  // Search bursaries
  search: async (params) => {
    const response = await apiClient.get('/bursaries/search', { params });
    return response.data;
  },

  // Get bursary by ID
  getById: async (id) => {
    const response = await apiClient.get(`/bursaries/${id}`);
    return response.data;
  },

  // Get bursaries count
  getCount: async () => {
    const response = await apiClient.get('/bursaries/count');
    return response.data;
  },
};