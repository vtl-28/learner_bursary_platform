import apiClient from './client';

export const notificationsAPI = {
  // Get all notifications
  getAll: async () => {
    const response = await apiClient.get('/notifications');
    return response.data;
  },

  // Get unread notifications
  getUnread: async () => {
    const response = await apiClient.get('/notifications/unread');
    return response.data;
  },

  // Get unread count
  getUnreadCount: async () => {
    const response = await apiClient.get('/notifications/unread/count');
    return response.data;
  },

  // Mark as read
  markAsRead: async (id) => {
    const response = await apiClient.patch(`/notifications/${id}/read`);
    return response.data;
  },
};