import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/services/auth.service';
import { OrderService } from '../../core/services/order.service';
import { UserInfo } from '../../core/models/auth.model';
import { OrderResponse, OrderStatus, ORDER_STATUS_LABELS, ORDER_STATUS_COLORS } from '../../core/models/order.model';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NavbarComponent,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  template: `
    <app-navbar></app-navbar>

    <div class="dashboard-container">
      <!-- Welcome Banner -->
      <div class="welcome-banner">
        <div class="welcome-content">
          <h1>Welcome back, {{ currentUser?.username }}! 👋</h1>
          <p>Manage your orders and explore our products</p>
        </div>
      </div>

      <!-- Quick Actions Grid -->
      <div class="quick-actions-section">
        <h2 class="section-title">
          <mat-icon>flash_on</mat-icon>
          Quick Actions
        </h2>

        <div class="actions-grid">
          <!-- Create Order -->
          <mat-card class="action-card primary" routerLink="/orders/create">
            <div class="card-icon">
              <mat-icon>add_shopping_cart</mat-icon>
            </div>
            <h3>Create Order</h3>
            <p>Place a new order for products</p>
            <div class="card-arrow">
              <mat-icon>arrow_forward</mat-icon>
            </div>
          </mat-card>

          <!-- My Orders -->
          <mat-card class="action-card accent" routerLink="/orders">
            <div class="card-icon">
              <mat-icon>receipt_long</mat-icon>
            </div>
            <h3>My Orders</h3>
            <p>View your order history</p>
            @if (totalOrders() > 0) {
              <div class="card-badge">{{ totalOrders() }}</div>
            }
            <div class="card-arrow">
              <mat-icon>arrow_forward</mat-icon>
            </div>
          </mat-card>

          <!-- Browse Products -->
          <mat-card class="action-card success" routerLink="/products">
            <div class="card-icon">
              <mat-icon>store</mat-icon>
            </div>
            <h3>Browse Products</h3>
            <p>Discover new items to order</p>
            <div class="card-arrow">
              <mat-icon>arrow_forward</mat-icon>
            </div>
          </mat-card>

          <!-- Admin Panel (if admin) -->
          @if (isAdmin()) {
            <mat-card class="action-card warn" routerLink="/orders/admin">
              <div class="card-icon">
                <mat-icon>admin_panel_settings</mat-icon>
              </div>
              <h3>Order Admin</h3>
              <p>Manage all orders (Admin)</p>
              <div class="card-arrow">
                <mat-icon>arrow_forward</mat-icon>
              </div>
            </mat-card>
          }
        </div>
      </div>

      <!-- Order Statistics -->
      <div class="statistics-section">
        <h2 class="section-title">
          <mat-icon>analytics</mat-icon>
          Order Statistics
        </h2>

        @if (isLoadingStats()) {
          <div class="loading-stats">
            <mat-spinner diameter="40"></mat-spinner>
            <p>Loading statistics...</p>
          </div>
        } @else {
          <div class="stats-grid">
            <mat-card class="stat-card">
              <div class="stat-icon total">
                <mat-icon>shopping_bag</mat-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ totalOrders() }}</div>
                <div class="stat-label">Total Orders</div>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <div class="stat-icon pending">
                <mat-icon>schedule</mat-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ pendingOrders() }}</div>
                <div class="stat-label">Pending</div>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <div class="stat-icon processing">
                <mat-icon>hourglass_empty</mat-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ processingOrders() }}</div>
                <div class="stat-label">Processing</div>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <div class="stat-icon delivered">
                <mat-icon>done_all</mat-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ deliveredOrders() }}</div>
                <div class="stat-label">Delivered</div>
              </div>
            </mat-card>
          </div>
        }
      </div>

      <!-- Recent Orders -->
      <div class="recent-orders-section">
        <div class="section-header">
          <h2 class="section-title">
            <mat-icon>history</mat-icon>
            Recent Orders
          </h2>
          <button mat-button routerLink="/orders" class="view-all-btn">
            View All
            <mat-icon>arrow_forward</mat-icon>
          </button>
        </div>

        @if (isLoadingOrders()) {
          <div class="loading-orders">
            <mat-spinner diameter="40"></mat-spinner>
            <p>Loading orders...</p>
          </div>
        } @else if (recentOrders().length > 0) {
          <div class="orders-list">
            @for (order of recentOrders(); track order.id) {
              <mat-card class="order-item" (click)="viewOrder(order.id)">
                <div class="order-header">
                  <div class="order-number">
                    <mat-icon>confirmation_number</mat-icon>
                    <span>{{ order.orderNumber }}</span>
                  </div>
                  <mat-chip
                    [color]="getStatusColor(order.status)"
                    highlighted
                  >
                    {{ getStatusLabel(order.status) }}
                  </mat-chip>
                </div>

                <div class="order-details">
                  <div class="detail-item">
                    <mat-icon>calendar_today</mat-icon>
                    <span>{{ formatDate(order.createdAt) }}</span>
                  </div>
                  <div class="detail-item">
                    <mat-icon>shopping_cart</mat-icon>
                    <span>{{ order.items.length }} item(s)</span>
                  </div>
                  <div class="detail-item total">
                    <mat-icon>payments</mat-icon>
                    <span>\${{ order.totalAmount.toFixed(2) }}</span>
                  </div>
                </div>

                <div class="order-actions">
                  <button
                    mat-icon-button
                    (click)="viewOrder(order.id); $event.stopPropagation()"
                  >
                    <mat-icon>arrow_forward</mat-icon>
                  </button>
                </div>
              </mat-card>
            }
          </div>
        } @else {
          <mat-card class="no-orders">
            <mat-icon>inbox</mat-icon>
            <h3>No orders yet</h3>
            <p>Start shopping and create your first order!</p>
            <button mat-raised-button color="primary" routerLink="/products">
              <mat-icon>store</mat-icon>
              Browse Products
            </button>
          </mat-card>
        }
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 2rem 1rem;
      background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 50%);
      min-height: calc(100vh - 64px);
    }

    .welcome-banner {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 16px;
      padding: 2.5rem;
      margin-bottom: 2rem;
      color: white;
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);

      .welcome-content {
        h1 {
          margin: 0 0 0.5rem;
          font-size: 2rem;
          font-weight: 600;
        }

        p {
          margin: 0;
          font-size: 1.1rem;
          opacity: 0.9;
        }
      }
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.5rem;
      font-weight: 600;
      color: #333;
      margin-bottom: 1.5rem;

      mat-icon {
        color: #667eea;
      }
    }

    /* Quick Actions */
    .quick-actions-section {
      margin-bottom: 3rem;

      .actions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 1.5rem;

        .action-card {
          position: relative;
          padding: 2rem;
          cursor: pointer;
          transition: all 0.3s ease;
          overflow: hidden;
          border-left: 4px solid transparent;

          &:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 24px rgba(0,0,0,0.15);
          }

          &.primary {
            border-left-color: #667eea;

            .card-icon {
              background: linear-gradient(135deg, #667eea, #764ba2);
            }
          }

          &.accent {
            border-left-color: #f093fb;

            .card-icon {
              background: linear-gradient(135deg, #f093fb, #f5576c);
            }
          }

          &.success {
            border-left-color: #4facfe;

            .card-icon {
              background: linear-gradient(135deg, #4facfe, #00f2fe);
            }
          }

          &.warn {
            border-left-color: #fa709a;

            .card-icon {
              background: linear-gradient(135deg, #fa709a, #fee140);
            }
          }

          .card-icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 1rem;

            mat-icon {
              font-size: 32px;
              width: 32px;
              height: 32px;
              color: white;
            }
          }

          h3 {
            margin: 0 0 0.5rem;
            font-size: 1.2rem;
            font-weight: 600;
            color: #333;
          }

          p {
            margin: 0;
            color: #666;
            font-size: 0.9rem;
          }

          .card-badge {
            position: absolute;
            top: 1rem;
            right: 1rem;
            background: #f5576c;
            color: white;
            border-radius: 12px;
            padding: 0.25rem 0.75rem;
            font-weight: 600;
            font-size: 0.85rem;
          }

          .card-arrow {
            position: absolute;
            bottom: 1rem;
            right: 1rem;
            opacity: 0;
            transition: all 0.3s ease;

            mat-icon {
              color: #667eea;
            }
          }

          &:hover .card-arrow {
            opacity: 1;
            transform: translateX(4px);
          }
        }
      }
    }

    /* Statistics */
    .statistics-section {
      margin-bottom: 3rem;

      .loading-stats {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 3rem;
        gap: 1rem;

        p {
          color: #666;
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1.5rem;

        .stat-card {
          padding: 1.5rem;
          display: flex;
          gap: 1rem;
          align-items: center;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
          }

          .stat-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;

            mat-icon {
              font-size: 24px;
              width: 24px;
              height: 24px;
              color: white;
            }

            &.total { background: linear-gradient(135deg, #667eea, #764ba2); }
            &.pending { background: linear-gradient(135deg, #fbc2eb, #a6c1ee); }
            &.processing { background: linear-gradient(135deg, #fa709a, #fee140); }
            &.delivered { background: linear-gradient(135deg, #30cfd0, #330867); }
          }

          .stat-content {
            flex: 1;

            .stat-value {
              font-size: 2rem;
              font-weight: 700;
              color: #333;
              line-height: 1;
              margin-bottom: 0.25rem;
            }

            .stat-label {
              font-size: 0.875rem;
              color: #666;
            }
          }
        }
      }
    }

    /* Recent Orders */
    .recent-orders-section {
      margin-bottom: 2rem;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;

        .view-all-btn {
          display: flex;
          align-items: center;
          gap: 0.25rem;
          color: #667eea;
        }
      }

      .loading-orders {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 3rem;
        gap: 1rem;

        p {
          color: #666;
        }
      }

      .orders-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;

        .order-item {
          padding: 1.5rem;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            transform: translateX(4px);
          }

          .order-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;

            .order-number {
              display: flex;
              align-items: center;
              gap: 0.5rem;
              font-weight: 600;
              color: #667eea;

              mat-icon {
                font-size: 1.2rem;
                width: 1.2rem;
                height: 1.2rem;
              }
            }
          }

          .order-details {
            display: flex;
            gap: 2rem;
            flex-wrap: wrap;

            .detail-item {
              display: flex;
              align-items: center;
              gap: 0.5rem;
              color: #666;
              font-size: 0.9rem;

              mat-icon {
                font-size: 1rem;
                width: 1rem;
                height: 1rem;
              }

              &.total {
                color: #2e7d32;
                font-weight: 600;
                font-size: 1rem;
              }
            }
          }

          .order-actions {
            display: flex;
            justify-content: flex-end;
            margin-top: 0.5rem;
          }
        }
      }

      .no-orders {
        padding: 4rem 2rem;
        text-align: center;

        mat-icon {
          font-size: 4rem;
          width: 4rem;
          height: 4rem;
          color: #bdbdbd;
          margin-bottom: 1rem;
        }

        h3 {
          margin: 0 0 0.5rem;
          color: #333;
        }

        p {
          margin: 0 0 1.5rem;
          color: #666;
        }

        button mat-icon {
          margin-right: 0.5rem;
          font-size: 1.2rem;
          width: 1.2rem;
          height: 1.2rem;
        }
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 1rem 0.5rem;
      }

      .welcome-banner {
        padding: 1.5rem;

        .welcome-content h1 {
          font-size: 1.5rem;
        }
      }

      .actions-grid,
      .stats-grid {
        grid-template-columns: 1fr !important;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private orderService = inject(OrderService);
  private router = inject(Router);

  currentUser: UserInfo | null = null;
  recentOrders = signal<OrderResponse[]>([]);
  allOrders = signal<OrderResponse[]>([]);

  isLoadingOrders = signal(false);
  isLoadingStats = signal(false);

  readonly OrderStatus = OrderStatus;
  readonly statusLabels = ORDER_STATUS_LABELS;
  readonly statusColors = ORDER_STATUS_COLORS;

  // Computed statistics
  totalOrders = computed(() => this.allOrders().length);
  pendingOrders = computed(() =>
    this.allOrders().filter(o => o.status === OrderStatus.PENDING || o.status === OrderStatus.CONFIRMED).length
  );
  processingOrders = computed(() =>
    this.allOrders().filter(o => o.status === OrderStatus.PROCESSING || o.status === OrderStatus.SHIPPED).length
  );
  deliveredOrders = computed(() =>
    this.allOrders().filter(o => o.status === OrderStatus.DELIVERED).length
  );

  isAdmin = computed(() =>
    this.currentUser?.roles?.includes('ADMIN') || this.currentUser?.roles?.includes('ROLE_ADMIN')
  );

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadDashboardData();
      }
    });
  }

  private loadDashboardData(): void {
    if (!this.currentUser) return;

    this.isLoadingOrders.set(true);
    this.isLoadingStats.set(true);

    this.orderService.getUserOrders(this.currentUser.id).subscribe({
      next: (orders) => {
        this.allOrders.set(orders);
        this.recentOrders.set(orders.slice(0, 5));
        this.isLoadingOrders.set(false);
        this.isLoadingStats.set(false);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.isLoadingOrders.set(false);
        this.isLoadingStats.set(false);
      }
    });
  }

  viewOrder(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.authService.forceLogout();
      }
    });
  }

  getStatusLabel(status: OrderStatus) {
    return this.statusLabels[status];
  }

  getStatusColor(status: OrderStatus) {
    return this.statusColors[status];
  }
}
