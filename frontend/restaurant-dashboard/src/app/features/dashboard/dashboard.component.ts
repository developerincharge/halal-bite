// src/app/features/dashboard/dashboard.component.ts
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RestaurantService, OrderService } from '../../core/services/api.service';
import { Restaurant, OrderSummary } from '../../shared/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Dashboard</h1>
          <p class="page-sub">Welcome back! Here's what's happening today.</p>
        </div>
        @if (!restaurant()) {
          <a routerLink="/menu" class="btn-primary">+ Setup Restaurant</a>
        }
      </div>

      @if (restaurant()) {
        <!-- Restaurant status banner -->
        <div class="status-banner" [class]="'status-' + restaurant()!.status.toLowerCase()">
          <span class="status-icon">{{ statusIcon(restaurant()!.status) }}</span>
          <div>
            <strong>{{ restaurant()!.name }}</strong>
            <span class="status-label">{{ restaurant()!.status }}</span>
          </div>
          @if (restaurant()!.status === 'PENDING') {
            <span class="status-hint">Awaiting admin approval</span>
          }
        </div>

        <!-- Stats cards -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">🧾</div>
            <div class="stat-value">{{ activeOrders().length }}</div>
            <div class="stat-label">Active Orders</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">⭐</div>
            <div class="stat-value">{{ restaurant()!.averageRating || '—' }}</div>
            <div class="stat-label">Average Rating</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">💬</div>
            <div class="stat-value">{{ restaurant()!.totalReviews }}</div>
            <div class="stat-label">Total Reviews</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🏷️</div>
            <div class="stat-value">{{ restaurant()!.affiliateRevenuePercentage * 100 | number:'1.0-0' }}%</div>
            <div class="stat-label">Platform Fee</div>
          </div>
        </div>

        <!-- Active orders -->
        <div class="section">
          <div class="section-header">
            <h2>Active Orders</h2>
            <a routerLink="/orders" class="view-all">View all →</a>
          </div>

          @if (loading()) {
            <div class="loading">Loading orders...</div>
          } @else if (activeOrders().length === 0) {
            <div class="empty-state">
              <div class="empty-icon">🧾</div>
              <p>No active orders right now.</p>
              <span>New orders will appear here automatically.</span>
            </div>
          } @else {
            <div class="orders-list">
              @for (order of activeOrders(); track order.id) {
                <a [routerLink]="['/orders', order.id]" class="order-card">
                  <div class="order-id">#{{ shortId(order.id) }}</div>
                  <div class="order-meta">
                    <span>{{ order.itemCount }} item(s)</span>
                    <span class="order-amount">\${{ order.totalAmount | number:'1.2-2' }}</span>
                  </div>
                  <div class="order-status" [class]="'status-' + order.status.toLowerCase()">
                    {{ order.status }}
                  </div>
                  <div class="order-time">{{ order.createdAt | date:'h:mm a' }}</div>
                </a>
              }
            </div>
          }
        </div>
      } @else {
        <!-- No restaurant yet -->
        <div class="empty-state large">
          <div class="empty-icon">🏪</div>
          <h2>No restaurant set up yet</h2>
          <p>Go to the Menu section to create your restaurant profile and start receiving orders.</p>
          <a routerLink="/menu" class="btn-primary">Set Up Restaurant</a>
        </div>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 900px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
    .page-title { font-size: 26px; font-weight: 700; color: #1a1a2e; margin: 0 0 4px; }
    .page-sub { color: #666; margin: 0; font-size: 14px; }
    .btn-primary {
      padding: 10px 20px; background: #f4a261; color: white;
      border: none; border-radius: 8px; font-size: 14px;
      font-weight: 600; cursor: pointer; text-decoration: none;
    }

    .status-banner {
      display: flex; align-items: center; gap: 12px;
      padding: 14px 18px; border-radius: 10px; margin-bottom: 24px;
    }
    .status-active { background: #d4edda; color: #155724; }
    .status-pending { background: #fff3cd; color: #856404; }
    .status-suspended { background: #f8d7da; color: #721c24; }
    .status-icon { font-size: 22px; }
    .status-label {
      margin-left: 8px; font-size: 12px;
      padding: 2px 8px; border-radius: 20px;
      background: rgba(0,0,0,0.1); font-weight: 600;
    }
    .status-hint { margin-left: auto; font-size: 12px; opacity: 0.7; }

    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
    .stat-card {
      background: white; border-radius: 12px; padding: 20px;
      text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    }
    .stat-icon { font-size: 28px; margin-bottom: 8px; }
    .stat-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }
    .stat-label { font-size: 12px; color: #888; margin-top: 4px; }

    .section { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .section-header h2 { font-size: 16px; font-weight: 700; margin: 0; color: #1a1a2e; }
    .view-all { font-size: 13px; color: #f4a261; text-decoration: none; }

    .loading { color: #888; text-align: center; padding: 32px; }

    .empty-state { text-align: center; padding: 40px 20px; color: #888; }
    .empty-state.large { background: white; border-radius: 12px; padding: 60px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .empty-icon { font-size: 40px; margin-bottom: 12px; }
    .empty-state p { margin: 0 0 8px; font-size: 15px; color: #444; }
    .empty-state span { font-size: 13px; }

    .orders-list { display: flex; flex-direction: column; gap: 8px; }
    .order-card {
      display: flex; align-items: center; gap: 16px;
      padding: 14px 16px; border-radius: 8px;
      border: 1px solid #eee; text-decoration: none; color: inherit;
      transition: background 0.15s;
    }
    .order-card:hover { background: #fafafa; }
    .order-id { font-weight: 700; font-size: 13px; color: #1a1a2e; width: 80px; }
    .order-meta { flex: 1; display: flex; gap: 16px; font-size: 13px; color: #666; }
    .order-amount { font-weight: 600; color: #1a1a2e; }
    .order-status {
      padding: 3px 10px; border-radius: 20px;
      font-size: 11px; font-weight: 700; text-transform: uppercase;
    }
    .status-pending { background: #fff3cd; color: #856404; }
    .status-confirmed { background: #cce5ff; color: #004085; }
    .status-preparing { background: #d4edda; color: #155724; }
    .status-ready { background: #d1ecf1; color: #0c5460; }
    .order-time { font-size: 12px; color: #aaa; }
  `]
})
export class DashboardComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private orderService = inject(OrderService);

  restaurant = signal<Restaurant | null>(null);
  activeOrders = signal<OrderSummary[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.restaurantService.getMyRestaurant().subscribe({
      next: (restaurants) => {
        if (restaurants.length > 0) {
          this.restaurant.set(restaurants[0]);
          this.loadActiveOrders(restaurants[0].id);
        } else {
          this.loading.set(false);
        }
      },
      error: () => this.loading.set(false)
    });
  }

  private loadActiveOrders(restaurantId: string) {
    this.orderService.getActiveOrders(restaurantId).subscribe({
      next: (orders) => { this.activeOrders.set(orders); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  statusIcon(status: string) {
    return { ACTIVE: '✅', PENDING: '⏳', SUSPENDED: '🚫', CLOSED: '🔒' }[status] ?? '❓';
  }

  shortId(id: string) { return id.slice(-8).toUpperCase(); }
}
