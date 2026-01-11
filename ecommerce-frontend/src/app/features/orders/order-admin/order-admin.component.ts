import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatBadgeModule } from '@angular/material/badge';
import { ThemePalette } from '@angular/material/core';

import { OrderService } from '../../../core/services/order.service';
import {
  OrderResponse,
  OrderStatus,
  SagaStatus,
  ORDER_STATUS_LABELS,
  ORDER_STATUS_COLORS,
  SAGA_STATUS_LABELS
} from '../../../core/models/order.model';

@Component({
  selector: 'app-order-admin',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTabsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatBadgeModule
  ],
  templateUrl: './order-admin.component.html',
  styleUrl: './order-admin.component.scss'
})
export class OrderAdminComponent implements OnInit {
  private orderService = inject(OrderService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  allOrders = signal<OrderResponse[]>([]);
  failedOrders = signal<OrderResponse[]>([]);
  displayedOrders = signal<OrderResponse[]>([]);

  isLoadingAll = signal(false);
  isLoadingFailed = signal(false);

  pageSize = 10;
  pageIndex = 0;
  totalOrders = 0;

  readonly OrderStatus = OrderStatus;
  readonly statusLabels = ORDER_STATUS_LABELS;
  readonly statusColors = ORDER_STATUS_COLORS;
  readonly sagaStatusLabels = SAGA_STATUS_LABELS;

  displayedColumns: string[] = [
    'orderNumber',
    'userId',
    'items',
    'totalAmount',
    'status',
    'sagaStatus',
    'createdAt',
    'actions'
  ];

  ngOnInit(): void {
    this.loadAllOrders();
    this.loadFailedOrders();
  }

  loadAllOrders(): void {
    this.isLoadingAll.set(true);

    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.allOrders.set(orders);
        this.totalOrders = orders.length;
        this.updateDisplayedOrders();
        this.isLoadingAll.set(false);
      },
      error: (error) => {
        console.error('Error loading all orders:', error);
        this.snackBar.open('Failed to load orders', 'Close', { duration: 3000 });
        this.isLoadingAll.set(false);
      }
    });
  }

  loadFailedOrders(): void {
    this.isLoadingFailed.set(true);

    this.orderService.getFailedOrders().subscribe({
      next: (orders) => {
        this.failedOrders.set(orders);
        this.isLoadingFailed.set(false);
      },
      error: (error) => {
        console.error('Error loading failed orders:', error);
        this.snackBar.open('Failed to load failed orders', 'Close', { duration: 3000 });
        this.isLoadingFailed.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updateDisplayedOrders();
  }

  private updateDisplayedOrders(): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedOrders.set(
      this.allOrders().slice(startIndex, endIndex)
    );
  }

  viewOrderDetails(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  refreshAll(): void {
    this.loadAllOrders();
  }

  refreshFailed(): void {
    this.loadFailedOrders();
  }

  getOrderStatusIcon(status: OrderStatus): string {
    const iconMap: Record<OrderStatus, string> = {
      [OrderStatus.PENDING]: 'schedule',
      [OrderStatus.CONFIRMED]: 'check_circle',
      [OrderStatus.PROCESSING]: 'hourglass_empty',
      [OrderStatus.SHIPPED]: 'local_shipping',
      [OrderStatus.DELIVERED]: 'done_all',
      [OrderStatus.CANCELLED]: 'cancel',
      [OrderStatus.FAILED]: 'error'
    };
    return iconMap[status] || 'help';
  }

  getSagaStatusIcon(status: SagaStatus): string {
    const iconMap: Record<SagaStatus, string> = {
      [SagaStatus.STARTED]: 'play_arrow',
      [SagaStatus.INVENTORY_RESERVED]: 'inventory',
      [SagaStatus.PAYMENT_PROCESSED]: 'payment',
      [SagaStatus.COMPLETED]: 'check_circle',
      [SagaStatus.COMPENSATING]: 'refresh',
      [SagaStatus.FAILED]: 'error'
    };
    return iconMap[status] || 'help';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSagaStatusColor(status: SagaStatus): ThemePalette {
    if (status === SagaStatus.COMPLETED) return 'primary';
    if (status === SagaStatus.FAILED) return 'warn';
    if (status === SagaStatus.COMPENSATING) return 'warn';
    return 'accent';
  }

  getStatusColor(status: OrderStatus): ThemePalette {
    return this.statusColors[status];
  }

  getStatusLabel(status: OrderStatus): String {
    return this.statusLabels[status];
  }

  getSagaStatusLabel(sagaStatus: SagaStatus): String {
    return this.sagaStatusLabels[sagaStatus];
  }
}
