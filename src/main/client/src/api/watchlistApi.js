import api from './axiosConfig';

export const watchlistApi = {
  getUserWatchlist: async (userId) => {
    const response = await api.get(`/watchlist/${userId}`);
    return response.data;
  },

  addToWatchlist: async (userId, movieId) => {
    const response = await api.post('/watchlist/add', 
      { movieId }, 
      { headers: { UserId: userId } }
    );
    return response.data;
  },

  removeFromWatchlist: async (userId, movieId) => {
    const response = await api.delete('/watchlist/remove', {
      data: { movieId },
      headers: { UserId: userId }
    });
    return response.data;
  },

  checkInWatchlist: async (userId, movieId) => {
    const response = await api.get(`/watchlist/check/${userId}/${movieId}`);
    return response.data;
  }
};
