import axios from 'axios';

// Use environment variable or fallback to relative path for Docker
// Backend has context-path=/api/v1, so we need to include it in baseURL
const baseURL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: baseURL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
    timeout: 10000, // 10 seconds timeout
});

// Request interceptor - add auth token if available
api.interceptors.request.use(
    (config) => {
        const user = localStorage.getItem('user');
        if (user) {
            try {
                const userData = JSON.parse(user);
                if (userData.token) {
                    config.headers.Authorization = `Bearer ${userData.token}`;
                }
                if (userData.userId) {
                    config.headers.UserId = userData.userId;
                }
            } catch (error) {
                console.error('Error parsing user data:', error);
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - handle common errors
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response?.status === 403) {
            console.error('403 Forbidden - Check CORS and permissions');
            // Optionally redirect to login or show error message
        }
        if (error.response?.status === 401) {
            console.error('401 Unauthorized - User may need to login');
            localStorage.removeItem('user');
            // Optionally redirect to login
        }
        return Promise.reject(error);
    }
);

export default api;