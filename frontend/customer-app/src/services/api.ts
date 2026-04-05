import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// =====================================================
// API Configuration
// =====================================================
// For Android emulator: use 10.0.2.2 instead of localhost
// For iOS simulator:    use localhost
// For physical device:  use your machine's local IP e.g. 192.168.1.x
// =====================================================
const BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request automatically
api.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('halalbite_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 — clear token and let app redirect to login
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      await AsyncStorage.removeItem('halalbite_token');
      await AsyncStorage.removeItem('halalbite_user');
    }
    return Promise.reject(error);
  }
);

export default api;

// =====================================================
// Types
// =====================================================

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  role: string;
}

export interface Restaurant {
  id: string;
  name: string;
  cuisineType: string;
  description?: string;
  streetAddress: string;
  city: string;
  averageRating: number;
  totalReviews: number;
  estimatedDeliveryMinutes: number;
  minimumOrderAmount: number;
  status: string;
  logoUrl?: string;
}

export interface MenuCategory {
  id: string;
  name: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
  items: MenuItem[];
}

export interface MenuItem {
  id: string;
  categoryId: string;
  restaurantId: string;
  name: string;
  description?: string;
  price: number;
  discountedPrice?: number;
  isSpicy: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  calories?: number;
  preparationTimeMinutes: number;
  isAvailable: boolean;
  imageUrl?: string;
}

export interface CartItem {
  menuItem: MenuItem;
  quantity: number;
  specialRequests?: string;
}

export interface Order {
  id: string;
  restaurantId: string;
  lineItems: OrderLineItem[];
  subtotal: number;
  deliveryFee: number;
  totalAmount: number;
  status: OrderStatus;
  deliveryStreetAddress: string;
  deliveryCity: string;
  specialInstructions?: string;
  estimatedReadyAt: string;
  createdAt: string;
}

export interface OrderSummary {
  id: string;
  restaurantId: string;
  totalAmount: number;
  itemCount: number;
  status: OrderStatus;
  createdAt: string;
}

export interface OrderLineItem {
  id: string;
  itemName: string;
  itemUnitPrice: number;
  quantity: number;
  lineTotal: number;
  specialRequests?: string;
}

export type OrderStatus =
  | 'PENDING' | 'CONFIRMED' | 'PREPARING'
  | 'READY' | 'DELIVERED' | 'CANCELLED';

// =====================================================
// Auth API
// =====================================================

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }),

  register: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/register', {
      email, password, role: 'CUSTOMER'
    }),
};

// =====================================================
// Restaurant API
// =====================================================

export const restaurantApi = {
  getAll: (page = 0, size = 20) =>
    api.get<{ content: Restaurant[]; totalPages: number }>(
      `/restaurants?page=${page}&size=${size}`
    ),

  search: (query: string) =>
    api.get<{ content: Restaurant[] }>(
      `/restaurants/search?query=${query}`
    ),

  getById: (id: string) =>
    api.get<Restaurant>(`/restaurants/${id}`),
};

// =====================================================
// Menu API
// =====================================================

export const menuApi = {
  getCategories: (restaurantId: string) =>
    api.get<MenuCategory[]>(
      `/menus/restaurants/${restaurantId}/categories`
    ),
};

// =====================================================
// Order API
// =====================================================

export const orderApi = {
  placeOrder: (data: {
    restaurantId: string;
    items: { menuItemId: string; quantity: number; specialRequests?: string }[];
    deliveryStreetAddress: string;
    deliveryCity: string;
    deliveryPostalCode: string;
    deliveryState?: string;
    specialInstructions?: string;
  }) => api.post<Order>('/orders', data),

getMyOrders: (page = 0) =>
  api.get<{ content: OrderSummary[]; totalPages: number }>(
    `/orders?page=${page}&size=20`
  ),

  getOrderById: (id: string) =>
    api.get<Order>(`/orders/${id}`),
};
