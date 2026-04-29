// src/app/features/orders/orders.component.ts
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RestaurantService, OrderService } from '../../core/services/api.service';
import { OrderSummary } from '../../shared/models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <h1 class="page-title">Orders</h1>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button class="tab" [class.active]="activeTab() === 'active'" (click)="setTab('active')">
          Active Orders
          @if (activeOrders().length > 0) {
            <span class="tab-badge">{{ activeOrders().length }}</span>
          }
        </button>
        <button class="tab" [class.active]="activeTab() === 'history'" (click)="setTab('history')">
          Order History
        </button>
      </div>

      @if (loading()) {
        <div class="loading">Loading orders...</div>
      } @else if (activeTab() === 'active') {

        @if (activeOrders().length === 0) {
          <div class="empty-state">
            <div class="empty-icon">🎉</div>
            <p>No active orders right now</p>
            <span>New orders will appear here in real time</span>
          </div>
        } @else {
          <div class="orders-grid">
            @for (order of activeOrders(); track order.id) {
              <a [routerLink]="['/orders', order.id]" class="order-card">
                <div class="order-top">
                  <span class="order-id">#{{ shortId(order.id) }}</span>
                  <span class="order-status" [class]="'status-' + order.status.toLowerCase()">
                    {{ order.status }}
                  </span>
                </div>
                <div class="order-items">{{ order.itemCount }} item(s)</div>
                <div class="order-bottom">
                  <span class="order-amount">\${{ order.totalAmount | number:'1.2-2' }}</span>
                  <span class="order-time">{{ order.createdAt | date:'h:mm a' }}</span>
                </div>
              </a>
            }
          </div>
        }

      } @else {

        @if (historyOrders().length === 0) {
          <div class="empty-state">
            <div class="empty-icon">📋</div>
            <p>No order history yet</p>
          </div>
        } @else {
          <div class="history-table">
            <div class="table-header">
              <span>Order ID</span>
              <span>Items</span>
              <span>Total</span>
              <span>Status</span>
              <span>Date</span>
            </div>
            @for (order of historyOrders(); track order.id) {
              <a [routerLink]="['/orders', order.id]" class="table-row">
                <span class="order-id-sm">#{{ shortId(order.id) }}</span>
                <span>{{ order.itemCount }} item(s)</span>
                <span class="amount">\${{ order.totalAmount | number:'1.2-2' }}</span>
                <span class="order-status" [class]="'status-' + order.status.toLowerCase()">
                  {{ order.status }}
                </span>
                <span class="date">{{ order.createdAt | date:'MMM d, h:mm a' }}</span>
              </a>
            }
          </div>
        }

      }
    </div>
  `,
  styles: [`
    .page { max-width: 900px; margin: 0 auto; }
    .page-header { margin-bottom: 24px; }
    .page-title { font-size: 26px; font-weight: 700; color: #1a1a2e; margin: 0; }

    .tabs { display: flex; gap: 4px; margin-bottom: 24px; border-bottom: 2px solid #eee; }
    .tab {
      padding: 10px 20px; background: none; border: none;
      cursor: pointer; font-size: 14px; color: #888;
      border-bottom: 2px solid transparent; margin-bottom: -2px;
      display: flex; align-items: center; gap: 8px;
    }
    .tab.active { color: #f4a261; border-bottom-color: #f4a261; font-weight: 600; }
    .tab-badge {
      background: #f4a261; color: white;
      font-size: 11px; padding: 1px 6px; border-radius: 10px; font-weight: 700;
    }

    .loading { color: #888; text-align: center; padding: 60px; }

    .empty-state { text-align: center; padding: 60px 20px; color: #888; }
    .empty-icon { font-size: 40px; margin-bottom: 12px; }
    .empty-state p { font-size: 16px; color: #444; margin: 0 0 8px; }
    .empty-state span { font-size: 13px; }

    .orders-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
    .order-card {
      background: white; border-radius: 12px; padding: 20px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      text-decoration: none; color: inherit;
      transition: box-shadow 0.15s; display: block;
    }
    .order-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
    .order-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .order-id { font-weight: 700; font-size: 14px; color: #1a1a2e; }
    .order-items { font-size: 13px; color: #666; margin-bottom: 16px; }
    .order-bottom { display: flex; justify-content: space-between; align-items: center; }
    .order-amount { font-size: 18px; font-weight: 700; color: #1a1a2e; }
    .order-time { font-size: 12px; color: #aaa; }

    .order-status {
      padding: 3px 10px; border-radius: 20px;
      font-size: 11px; font-weight: 700; text-transform: uppercase;
    }
    .status-pending { background: #fff3cd; color: #856404; }
    .status-confirmed { background: #cce5ff; color: #004085; }
    .status-preparing { background: #d4edda; color: #155724; }
    .status-ready { background: #d1ecf1; color: #0c5460; }
    .status-delivered { background: #e2e3e5; color: #383d41; }
    .status-cancelled { background: #f8d7da; color: #721c24; }

    .history-table { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .table-header {
      display: grid; grid-template-columns: 100px 1fr 100px 120px 160px;
      padding: 12px 20px; background: #f8f9fa;
      font-size: 12px; font-weight: 600; color: #888; text-transform: uppercase;
    }
    .table-row {
      display: grid; grid-template-columns: 100px 1fr 100px 120px 160px;
      padding: 14px 20px; border-top: 1px solid #eee;
      font-size: 13px; text-decoration: none; color: inherit;
      transition: background 0.15s; align-items: center;
    }
    .table-row:hover { background: #fafafa; }
    .order-id-sm { font-weight: 700; color: #1a1a2e; }
    .amount { font-weight: 600; }
    .date { color: #888; }
  `]
})
export class OrdersComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private orderService = inject(OrderService);

  activeOrders = signal<OrderSummary[]>([]);
  historyOrders = signal<OrderSummary[]>([]);
  activeTab = signal<'active' | 'history'>('active');
  loading = signal(true);
  restaurantId = signal('');

  ngOnInit() {
    this.restaurantService.getMyRestaurant().subscribe({
      next: (restaurants) => {
        if (restaurants.length > 0) {
          this.restaurantId.set(restaurants[0].id);
          this.loadOrders(restaurants[0].id);
        } else {
          this.loading.set(false);
        }
      }
    });
  }

  setTab(tab: 'active' | 'history') {
    this.activeTab.set(tab);
    if (tab === 'history' && this.restaurantId()) {
      this.loadHistory(this.restaurantId());
    }
  }

private loadOrders(restaurantId: string) {
  console.log('Loading orders for restaurantId:', restaurantId);  // ← add this
  this.orderService.getActiveOrders(restaurantId).subscribe({
    next: (orders) => {
      console.log('Orders received:', orders);  // ← add this
      this.activeOrders.set(orders);
      this.loading.set(false);
    },
    error: (err) => {
      console.error('Error loading orders:', err);  // ← add this
      this.loading.set(false);
    }
  });
}

  private loadHistory(restaurantId: string) {
    this.orderService.getOrderHistory(restaurantId).subscribe({
      next: (page) => this.historyOrders.set(page.content)
    });
  }

  shortId(id: string) { return id.slice(-8).toUpperCase(); }
}
