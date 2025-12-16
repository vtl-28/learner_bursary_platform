import apiClient from './client';

export const applicationsAPI = {
  // Learner endpoints
  apply: async (bursaryId) => {
    const response = await apiClient.post('/applications', { bursaryId });
    return response.data;
  },

  getMyApplications: async () => {
    const response = await apiClient.get('/applications/my-applications');
    return response.data;
  },

  getApplicationById: async (id) => {
    const response = await apiClient.get(`/applications/${id}`);
    return response.data;
  },

  checkIfApplied: async (bursaryId) => {
    const response = await apiClient.get(`/applications/check/${bursaryId}`);
    return response.data;
  },

  withdraw: async (id) => {
    const response = await apiClient.delete(`/applications/${id}`);
    return response.data;
  },

  // Provider endpoints
  getReceivedApplications: async (status = null) => {
    const params = status ? { status } : {};
    const response = await apiClient.get('/applications/provider/received', { params });
    return response.data;
  },

  getBursaryApplications: async (bursaryId) => {
    const response = await apiClient.get(`/applications/provider/bursary/${bursaryId}`);
    return response.data;
  },

  updateStatus: async (id, data) => {
    const response = await apiClient.patch(`/applications/provider/${id}/status`, data);
    return response.data;
  },

  getStatistics: async () => {
    const response = await apiClient.get('/applications/provider/statistics');
    return response.data;
  },
};