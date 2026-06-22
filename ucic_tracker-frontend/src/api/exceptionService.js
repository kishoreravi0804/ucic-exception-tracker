import axiosClient from './axiosClient';

export const exceptionService = {
  // Dashboard summary
  getDashboard: () => axiosClient.get('/dashboard'),

  // Get all exception groups, optionally filtered by status
  getAllGroups: (status) => {
    const params = status ? { status } : {};
    return axiosClient.get('/exceptions', { params });
  },

  // Get single group with members
  getGroupById: (id) => axiosClient.get(`/exceptions/${id}`),

  // Send group to vendor (generates token)
  sendToVendor: (id) => axiosClient.put(`/exceptions/${id}/send-vendor`),

  // Manually resolve a group
  resolveManually: (id, data) => axiosClient.put(`/exceptions/${id}/resolve`, data),

  // Reject a group
  rejectGroup: (id, note) => axiosClient.put(`/exceptions/${id}/reject`, null, { params: { note } }),

  // Get audit log for a group
  getAuditLog: (id) => axiosClient.get(`/exceptions/${id}/audit`),

  // Trigger Union-Find analysis
  runAnalysis: () => axiosClient.post('/analysis/run'),
};

export const vendorService = {
  // Vendor views their assigned group by token
  getByToken: (token) => axiosClient.get('/vendor/review', { params: { token } }),

  // Vendor submits resolution
  resolveByToken: (token, data) => axiosClient.post('/vendor/resolve', data, { params: { token } }),
};
