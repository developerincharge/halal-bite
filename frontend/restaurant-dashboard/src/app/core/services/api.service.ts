import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Restaurant, CreateRestaurantRequest,
  MenuCategory, MenuItem, CreateCategoryRequest, CreateMenuItemRequest,
  Order, OrderSummary, UpdateStatusRequest, Page, NotificationLog
} from '../../shared/models';
 
const API = environment.apiUrl;
 
// =====================================================
// RestaurantService — CRUD for the owner's restaurant
// =====================================================
@Injectable({ providedIn: 'root' })
export class RestaurantService {
  constructor(private http: HttpClient) {}
 
  // Get the restaurants owned by the current user (returns array)
  getMyRestaurant(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${API}/restaurants/owner`);
  }
 
  createRestaurant(data: CreateRestaurantRequest): Observable<Restaurant> {
    return this.http.post<Restaurant>(`${API}/restaurants`, data);
  }
 
  updateRestaurant(id: string, data: Partial<CreateRestaurantRequest>): Observable<Restaurant> {
    return this.http.patch<Restaurant>(`${API}/restaurants/${id}`, data);
  }
}
 
// =====================================================
// MenuService — category and item management
// =====================================================
@Injectable({ providedIn: 'root' })
export class MenuService {
  constructor(private http: HttpClient) {}
 
  // Categories
  getCategories(restaurantId: string): Observable<MenuCategory[]> {
    return this.http.get<MenuCategory[]>(
      `${API}/menus/restaurants/${restaurantId}/categories`
    );
  }
 
  createCategory(restaurantId: string, data: CreateCategoryRequest): Observable<MenuCategory> {
    return this.http.post<MenuCategory>(
      `${API}/menus/restaurants/${restaurantId}/categories`, data
    );
  }
 
  updateCategory(restaurantId: string, categoryId: string, data: Partial<CreateCategoryRequest>): Observable<MenuCategory> {
    return this.http.patch<MenuCategory>(
      `${API}/menus/restaurants/${restaurantId}/categories/${categoryId}`, data
    );
  }
 
  deleteCategory(restaurantId: string, categoryId: string): Observable<void> {
    return this.http.delete<void>(
      `${API}/menus/restaurants/${restaurantId}/categories/${categoryId}`
    );
  }
 
  // Items
  createItem(restaurantId: string, data: CreateMenuItemRequest): Observable<MenuItem> {
    return this.http.post<MenuItem>(
      `${API}/menus/restaurants/${restaurantId}/items`, data
    );
  }
 
  updateItem(restaurantId: string, itemId: string, data: Partial<CreateMenuItemRequest>): Observable<MenuItem> {
    return this.http.patch<MenuItem>(
      `${API}/menus/restaurants/${restaurantId}/items/${itemId}`, data
    );
  }
 
  toggleAvailability(restaurantId: string, itemId: string): Observable<MenuItem> {
    return this.http.patch<MenuItem>(
      `${API}/menus/restaurants/${restaurantId}/items/${itemId}/toggle-availability`, {}
    );
  }
 
  deleteItem(restaurantId: string, itemId: string): Observable<void> {
    return this.http.delete<void>(
      `${API}/menus/restaurants/${restaurantId}/items/${itemId}`
    );
  }
}
 
// =====================================================
// OrderService — view and manage incoming orders
// =====================================================
@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}
 
  getActiveOrders(restaurantId: string): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(
      `${API}/orders/restaurant/${restaurantId}/active`
    );
  }
 
  getOrderHistory(restaurantId: string, page = 0, size = 20): Observable<Page<OrderSummary>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<OrderSummary>>(
      `${API}/orders/restaurant/${restaurantId}`, { params }
    );
  }
 
  // Fixed: uses restaurant-view endpoint so owner can see order without being the customer
  getOrderDetail(orderId: string): Observable<Order> {
    return this.http.get<Order>(`${API}/orders/restaurant-view/${orderId}`);
  }
 
  updateOrderStatus(orderId: string, data: UpdateStatusRequest): Observable<Order> {
    return this.http.patch<Order>(`${API}/orders/${orderId}/status`, data);
  }
}
 
// =====================================================
// NotificationService — view notification history
// =====================================================
@Injectable({ providedIn: 'root' })
export class NotificationService {
  constructor(private http: HttpClient) {}
 
  getOrderNotifications(orderId: string): Observable<NotificationLog[]> {
    return this.http.get<NotificationLog[]>(`${API}/notifications/order/${orderId}`);
  }
}