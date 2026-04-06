import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT from localStorage to every request
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('admin_token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401
api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;

// =====================================================
// Auth
// =====================================================
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),
};

// =====================================================
// Restaurants
// =====================================================
export const restaurantApi = {
  getAll: (page = 0, size = 20) =>
    api.get(`/restaurants?page=${page}&size=${size}`),

  getPending: () =>
    api.get('/restaurants?page=0&size=100'),

  getById: (id: string) =>
    api.get(`/restaurants/${id}`),

  updateStatus: (id: string, status: string) =>
    api.patch(`/restaurants/${id}/status`, { status }),
};

// =====================================================
// Orders
// =====================================================
export const orderApi = {
  getByRestaurant: (restaurantId: string, page = 0) =>
    api.get(`/orders/restaurant/${restaurantId}?page=${page}&size=20`),
};
