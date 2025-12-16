import apiClient from './client';

export const followAPI = {
  // Provider endpoints
  followLearner: async (learnerId, notes = null) => {
    const data = notes ? { notes } : {};
    const response = await apiClient.post(`/providers/follow/${learnerId}`, data);
    return response.data;
  },

  unfollowLearner: async (learnerId) => {
    const response = await apiClient.delete(`/providers/follow/${learnerId}`);
    return response.data;
  },

  getFollowing: async () => {
    const response = await apiClient.get('/providers/follow/following');
    return response.data;
  },

  checkIfFollowing: async (learnerId) => {
    const response = await apiClient.get(`/providers/follow/check/${learnerId}`);
    return response.data;
  },

  // Learner endpoints
  getMyFollowers: async () => {
    const response = await apiClient.get('/learners/followers');
    return response.data;
  },

  getFollowerCount: async () => {
    const response = await apiClient.get('/learners/followers/count');
    return response.data;
  },
};