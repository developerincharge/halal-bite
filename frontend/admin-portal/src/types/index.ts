

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
  state?: string;
  postalCode: string;
  phoneNumber: string;
  email?: string;
  status: RestaurantStatus;
  ownerUserId: string;
  averageRating: number;
  totalReviews: number;
  estimatedDeliveryMinutes: number;
  minimumOrderAmount: number;
  affiliateRevenuePercentage: number;
  isHalalCertified: boolean;
  createdAt: string;
}

export type RestaurantStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';

export interface Order {
  id: string;
  customerId: string;
  restaurantId: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
  lineItems: OrderLineItem[];
}

export interface OrderLineItem {
  id: string;
  itemName: string;
  quantity: number;
  lineTotal: number;
}

export type OrderStatus =
  | 'PENDING' | 'CONFIRMED' | 'PREPARING'
  | 'READY' | 'DELIVERED' | 'CANCELLED';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
