import apiClient from './client';

export const academicAPI = {
  // Create academic year
  createYear: async (data) => {
    const response = await apiClient.post('/academic/years', data);
    return response.data;
  },

  // Add term results
  addTerm: async (academicYearId, data) => {
    const response = await apiClient.post(`/academic/years/${academicYearId}/terms`, data);
    return response.data;
  },

  // Get all my results
  getMyResults: async () => {
    const response = await apiClient.get('/academic/my-results');
    return response.data;
  },

  // Get specific academic year
  getYearById: async (id) => {
    const response = await apiClient.get(`/academic/years/${id}`);
    return response.data;
  },

  // Update term results
  updateTerm: async (termId, data) => {
    const response = await apiClient.put(`/academic/terms/${termId}`, data);
    return response.data;
  },

  // Delete academic year
  deleteYear: async (id) => {
    const response = await apiClient.delete(`/academic/years/${id}`);
    return response.data;
  },
};