import api from './axiosConfig';

export const authApi = {
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  loginWithGoogle: async (idToken) => {
    const response = await api.post('/auth/google', { idToken });
    return response.data;
  }
};
