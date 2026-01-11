import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';

import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  OrderResponse,
  OrderStatus,
  ORDER_STATUS_LABELS,
  ORDER_STATUS_COLORS
} from '../../../core/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatBadgeModule,
    MatDividerModule
  ],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss'
})
export class OrderListComponent implements OnInit {
  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  orders = signal<OrderResponse[]>([]);
  filteredOrders = signal<OrderResponse[]>([]);
  isLoading = signal(false);
  selectedStatus = signal<OrderStatus | null>(null);

  readonly OrderStatus = OrderStatus;
  readonly statusLabels = ORDER_STATUS_LABELS;
  readonly statusColors = ORDER_STATUS_COLORS;

  statusOptions = [
    { value: null, label: 'All Orders' },
    { value: OrderStatus.PENDING, label: 'Pending' },
    { value: OrderStatus.CONFIRMED, label: 'Confirmed' },
    { value: OrderStatus.PROCESSING, label: 'Processing' },
    { value: OrderStatus.SHIPPED, label: 'Shipped' },
    { value: OrderStatus.DELIVERED, label: 'Delivered' },
    { value: OrderStatus.CANCELLED, label: 'Cancelled' },
    { value: OrderStatus.FAILED, label: 'Failed' }
  ];

  ngOnInit(): void {
    this.loadOrders();
  }

  private loadOrders(): void {
    const user = this.authService.currentUserValue;
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading.set(true);

    const request = this.selectedStatus()
      ? this.orderService.getUserOrdersByStatus(user.id, this.selectedStatus()!)
      : this.orderService.getUserOrders(user.id);

    request.subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.filteredOrders.set(orders);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.snackBar.open('Failed to load orders', 'Close', { duration: 3000 });
        this.isLoading.set(false);
      }
    });
  }

  onStatusFilterChange(): void {
    this.loadOrders();
  }

  viewOrderDetails(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  createNewOrder(): void {
    this.router.navigate(['/orders/create']);
  }

  cancelOrder(order: OrderResponse, event: Event): void {
    event.stopPropagation();

    const confirmCancel = confirm(
      `Are you sure you want to cancel order ${order.orderNumber}?`
    );

    if (confirmCancel) {
      this.orderService.cancelOrder(order.id, 'Cancelled by user').subscribe({
        next: (updatedOrder) => {
          this.snackBar.open('Order cancelled successfully', 'Close', { duration: 3000 });
          this.loadOrders();
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.snackBar.open(
            error.error?.message || 'Failed to cancel order',
            'Close',
            { duration: 3000 }
          );
        }
      });
    }
  }

  canCancelOrder(order: OrderResponse): boolean {
    return order.status === OrderStatus.PENDING ||
           order.status === OrderStatus.CONFIRMED;
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
}
