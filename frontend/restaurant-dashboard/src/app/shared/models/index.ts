// src/app/shared/models/index.ts
// These interfaces mirror the backend DTOs exactly.
// If you change a backend DTO, update the matching interface here.

// =====================================================
// AUTH
// =====================================================
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: UserRole;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
  role: UserRole;
}

export type UserRole = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'ADMIN';

// =====================================================
// RESTAURANT
// =====================================================
export interface Restaurant {
  id: string;
  name: string;
  cuisineType: string;
  description?: string;
  streetAddress: string;
  city: string;
  state?: string;
  postalCode: string;
  country?: string;
  phoneNumber: string;
  email?: string;
  logoUrl?: string;
  status: RestaurantStatus;
  isHalalCertified: boolean;
  halalCertificationNumber?: string;
  averageRating: number;
  totalReviews: number;
  estimatedDeliveryMinutes: number;
  minimumOrderAmount: number;
  affiliateRevenuePercentage: number;
  ownerId: string;
  createdAt: string;
}

export type RestaurantStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';

export interface CreateRestaurantRequest {
  name: string;
  cuisineType: string;
  description?: string;
  streetAddress: string;
  city: string;
  state?: string;
  postalCode: string;
  phoneNumber: string;
  email?: string;
}

// =====================================================
// MENU
// =====================================================
export interface MenuCategory {
  id: string;
  restaurantId: string;
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
  imageUrl?: string;
  price: number;
  discountedPrice?: number;
  isVegan: boolean;
  isVegetarian: boolean;
  isGlutenFree: boolean;
  isSpicy: boolean;
  calories?: number;
  preparationTimeMinutes: number;
  isAvailable: boolean;
  displayOrder: number;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  displayOrder?: number;
}

export interface CreateMenuItemRequest {
  categoryId: string;
  name: string;
  description?: string;
  price: number;
  discountedPrice?: number;
  isVegan?: boolean;
  isVegetarian?: boolean;
  isGlutenFree?: boolean;
  isSpicy?: boolean;
  calories?: number;
  preparationTimeMinutes?: number;
  displayOrder?: number;
}

// =====================================================
// ORDERS
// =====================================================
export interface Order {
  id: string;
  customerId: string;
  restaurantId: string;
  lineItems: OrderLineItem[];
  subtotal: number;
  deliveryFee: number;
  platformFeeAmount: number;
  totalAmount: number;
  deliveryStreetAddress: string;
  deliveryCity: string;
  deliveryState?: string;
  deliveryPostalCode: string;
  specialInstructions?: string;
  status: OrderStatus;
  estimatedReadyAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderLineItem {
  id: string;
  menuItemId: string;
  itemName: string;
  itemUnitPrice: number;
  quantity: number;
  lineTotal: number;
  specialRequests?: string;
}

export interface OrderSummary {
  id: string;
  restaurantId: string;
  totalAmount: number;
  itemCount: number;
  status: OrderStatus;
  createdAt: string;
}

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'DELIVERED'
  | 'CANCELLED';

export interface UpdateStatusRequest {
  status: OrderStatus;
  reason?: string;
}

// =====================================================
// NOTIFICATIONS
// =====================================================
export interface NotificationLog {
  id: string;
  recipientId: string;
  recipientEmail: string;
  notificationType: string;
  subject: string;
  referenceId: string;
  status: 'SENT' | 'FAILED' | 'SKIPPED';
  createdAt: string;
}

// =====================================================
// PAGINATION
// =====================================================
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
