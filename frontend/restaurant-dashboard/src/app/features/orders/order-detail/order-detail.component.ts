// src/app/features/orders/order-detail/order-detail.component.ts
import { Component, inject, OnInit, signal, Input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../core/services/api.service';
import { Order, OrderStatus } from '../../../shared/models';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-header">
        <a routerLink="/orders" class="back-link">← Back to Orders</a>
      </div>

      @if (loading()) {
        <div class="loading">Loading order...</div>
      } @else if (order()) {
        <div class="detail-layout">

          <!-- Order info -->
          <div class="card">
            <div class="card-header">
              <div>
                <h2>Order #{{ shortId(order()!.id) }}</h2>
                <div class="order-time">{{ order()!.createdAt | date:'MMM d, yyyy h:mm a' }}</div>
              </div>
              <span class="status-badge" [class]="'status-' + order()!.status.toLowerCase()">
                {{ order()!.status }}
              </span>
            </div>

            <!-- Line items -->
            <div class="items-section">
              <h3>Items Ordered</h3>
              @for (item of order()!.lineItems; track item.id) {
                <div class="line-item">
                  <div class="line-item-info">
                    <span class="qty">{{ item.quantity }}×</span>
                    <span class="item-name">{{ item.itemName }}</span>
                    @if (item.specialRequests) {
                      <span class="special-req">{{ item.specialRequests }}</span>
                    }
                  </div>
                  <span class="line-total">\${{ item.lineTotal | number:'1.2-2' }}</span>
                </div>
              }
            </div>

            <!-- Totals -->
            <div class="totals">
              <div class="total-row">
                <span>Subtotal</span>
                <span>\${{ order()!.subtotal | number:'1.2-2' }}</span>
              </div>
              <div class="total-row">
                <span>Delivery Fee</span>
                <span>\${{ order()!.deliveryFee | number:'1.2-2' }}</span>
              </div>
              <div class="total-row grand">
                <span>Total</span>
                <span>\${{ order()!.totalAmount | number:'1.2-2' }}</span>
              </div>
            </div>

            @if (order()!.specialInstructions) {
              <div class="special-instructions">
                <strong>Special Instructions:</strong>
                {{ order()!.specialInstructions }}
              </div>
            }
          </div>

          <!-- Status + delivery -->
          <div class="side-panel">

            <!-- Status update -->
            <div class="card">
              <h3>Update Order Status</h3>

              <div class="status-flow">
                @for (step of statusFlow; track step.status) {
                  <div class="status-step"
                    [class.completed]="isCompleted(step.status)"
                    [class.current]="order()!.status === step.status">
                    <div class="step-dot">{{ step.icon }}</div>
                    <div class="step-info">
                      <div class="step-label">{{ step.label }}</div>
                    </div>
                  </div>
                }
              </div>

              @if (nextStatus()) {
                <button class="btn-advance" (click)="advanceStatus()" [disabled]="updating()">
                  {{ updating() ? 'Updating...' : 'Mark as ' + nextStatus() }}
                </button>
              }

              @if (order()!.status === 'PENDING' || order()!.status === 'CONFIRMED') {
                <button class="btn-cancel" (click)="cancelOrder()" [disabled]="updating()">
                  Cancel Order
                </button>
              }
            </div>

            <!-- Delivery address -->
            <div class="card">
              <h3>Delivery Address</h3>
              <div class="address">
                <div>{{ order()!.deliveryStreetAddress }}</div>
                <div>{{ order()!.deliveryCity }}{{ order()!.deliveryState ? ', ' + order()!.deliveryState : '' }}</div>
                <div>{{ order()!.deliveryPostalCode }}</div>
              </div>
            </div>

          </div>
        </div>
      } @else {
        <div class="empty-state">Order not found.</div>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 900px; margin: 0 auto; }
    .page-header { margin-bottom: 20px; }
    .back-link { color: #f4a261; text-decoration: none; font-size: 14px; font-weight: 600; }
    .loading { text-align: center; padding: 60px; color: #888; }

    .detail-layout { display: grid; grid-template-columns: 1fr 300px; gap: 20px; align-items: start; }

    .card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); margin-bottom: 16px; }
    .card h3 { font-size: 14px; font-weight: 700; color: #1a1a2e; margin: 0 0 16px; }

    .card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .card-header h2 { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0 0 4px; }
    .order-time { font-size: 13px; color: #888; }

    .status-badge {
      padding: 5px 14px; border-radius: 20px;
      font-size: 12px; font-weight: 700; text-transform: uppercase;
    }
    .status-pending { background: #fff3cd; color: #856404; }
    .status-confirmed { background: #cce5ff; color: #004085; }
    .status-preparing { background: #d4edda; color: #155724; }
    .status-ready { background: #d1ecf1; color: #0c5460; }
    .status-delivered { background: #e2e3e5; color: #383d41; }
    .status-cancelled { background: #f8d7da; color: #721c24; }

    .items-section { margin-bottom: 20px; }
    .items-section h3 { font-size: 13px; font-weight: 700; color: #888; text-transform: uppercase; letter-spacing: 0.5px; margin: 0 0 12px; }

    .line-item { display: flex; justify-content: space-between; align-items: flex-start; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
    .line-item:last-child { border-bottom: none; }
    .line-item-info { display: flex; flex-direction: column; gap: 2px; }
    .qty { font-size: 13px; color: #888; }
    .item-name { font-size: 14px; font-weight: 600; color: #1a1a2e; }
    .special-req { font-size: 11px; color: #f4a261; font-style: italic; }
    .line-total { font-size: 14px; font-weight: 600; color: #1a1a2e; }

    .totals { border-top: 1px solid #eee; padding-top: 16px; }
    .total-row { display: flex; justify-content: space-between; font-size: 14px; color: #666; margin-bottom: 8px; }
    .total-row.grand { font-size: 16px; font-weight: 700; color: #1a1a2e; padding-top: 8px; border-top: 1px solid #eee; }

    .special-instructions { margin-top: 16px; padding: 12px; background: #fffbf0; border-radius: 8px; font-size: 13px; color: #666; }

    .status-flow { display: flex; flex-direction: column; gap: 0; margin-bottom: 20px; }
    .status-step { display: flex; align-items: center; gap: 12px; padding: 10px 0; position: relative; }
    .status-step:not(:last-child)::after {
      content: ''; position: absolute; left: 15px; top: 36px;
      width: 2px; height: 12px; background: #e0e0e0;
    }
    .status-step.completed::after { background: #28a745; }
    .step-dot {
      width: 32px; height: 32px; border-radius: 50%;
      background: #f0f0f0; display: flex; align-items: center;
      justify-content: center; font-size: 14px; flex-shrink: 0;
    }
    .status-step.completed .step-dot { background: #d4edda; }
    .status-step.current .step-dot { background: #f4a261; font-size: 16px; }
    .step-label { font-size: 13px; color: #666; }
    .status-step.current .step-label { font-weight: 700; color: #1a1a2e; }
    .status-step.completed .step-label { color: #28a745; }

    .btn-advance {
      width: 100%; padding: 12px; background: #f4a261; color: white;
      border: none; border-radius: 8px; font-size: 14px;
      font-weight: 600; cursor: pointer; margin-bottom: 8px;
    }
    .btn-advance:disabled { opacity: 0.6; cursor: not-allowed; }
    .btn-cancel {
      width: 100%; padding: 10px; background: white; color: #dc3545;
      border: 1px solid #dc3545; border-radius: 8px; font-size: 13px;
      font-weight: 600; cursor: pointer;
    }

    .address { font-size: 14px; color: #444; line-height: 1.8; }
    .empty-state { text-align: center; padding: 60px; color: #888; }
  `]
})
export class OrderDetailComponent implements OnInit {
  @Input() id!: string;

  private orderService = inject(OrderService);
  private router = inject(Router);

  order = signal<Order | null>(null);
  loading = signal(true);
  updating = signal(false);

  statusFlow = [
    { status: 'PENDING',   label: 'Order Received',    icon: '📥' },
    { status: 'CONFIRMED', label: 'Payment Confirmed',  icon: '✅' },
    { status: 'PREPARING', label: 'Preparing Food',     icon: '👨‍🍳' },
    { status: 'READY',     label: 'Ready for Delivery', icon: '📦' },
    { status: 'DELIVERED', label: 'Delivered',          icon: '🎉' }
  ];

  ngOnInit() {
    this.orderService.getOrderDetail(this.id).subscribe({
      next: (order) => { this.order.set(order); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  isCompleted(status: string): boolean {
    const order = this.order();
    if (!order) return false;
    const currentIdx = this.statusFlow.findIndex(s => s.status === order.status);
    const checkIdx = this.statusFlow.findIndex(s => s.status === status);
    return checkIdx < currentIdx;
  }

  nextStatus(): OrderStatus | null {
    const order = this.order();
    if (!order) return null;
    const transitions: Record<string, OrderStatus> = {
      CONFIRMED: 'PREPARING',
      PREPARING: 'READY',
      READY: 'DELIVERED'
    };
    return transitions[order.status] ?? null;
  }

  advanceStatus() {
    const next = this.nextStatus();
    if (!next || !this.order()) return;
    this.updating.set(true);
    this.orderService.updateOrderStatus(this.order()!.id, { status: next }).subscribe({
      next: (updated) => { this.order.set(updated); this.updating.set(false); },
      error: () => this.updating.set(false)
    });
  }

  cancelOrder() {
    if (!this.order()) return;
    this.updating.set(true);
    this.orderService.updateOrderStatus(this.order()!.id, { status: 'CANCELLED' }).subscribe({
      next: () => this.router.navigate(['/orders']),
      error: () => this.updating.set(false)
    });
  }

  shortId(id: string) { return id.slice(-8).toUpperCase(); }
}
