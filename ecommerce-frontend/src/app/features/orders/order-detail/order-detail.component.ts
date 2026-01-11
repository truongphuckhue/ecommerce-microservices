import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule } from '@angular/material/table';
import { MatBadgeModule } from '@angular/material/badge';

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
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDividerModule,
    MatTableModule,
    MatBadgeModule
  ],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private snackBar = inject(MatSnackBar);

  order = signal<OrderResponse | null>(null);
  isLoading = signal(false);
  
  readonly OrderStatus = OrderStatus;
  readonly statusLabels = ORDER_STATUS_LABELS;
  readonly statusColors = ORDER_STATUS_COLORS;
  readonly sagaStatusLabels = SAGA_STATUS_LABELS;

  displayedColumns: string[] = ['product', 'quantity', 'unitPrice', 'total'];

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.loadOrder(+orderId);
    }
  }

  private loadOrder(orderId: number): void {
    this.isLoading.set(true);
    
    this.orderService.getOrderById(orderId).subscribe({
      next: (order) => {
        this.order.set(order);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.snackBar.open('Failed to load order details', 'Close', { duration: 3000 });
        this.isLoading.set(false);
        this.router.navigate(['/orders']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }

  cancelOrder(): void {
    const order = this.order();
    if (!order) return;

    const reason = prompt('Please provide a reason for cancellation (optional):');
    if (reason === null) return; // User cancelled the prompt

    this.orderService.cancelOrder(order.id, reason || 'Cancelled by user').subscribe({
      next: (updatedOrder) => {
        this.order.set(updatedOrder);
        this.snackBar.open('Order cancelled successfully', 'Close', { duration: 3000 });
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

  canCancelOrder(): boolean {
    const order = this.order();
    if (!order) return false;
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
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getItemTotal(unitPrice: number, quantity: number, totalPrice?: number): number {
    return totalPrice || (unitPrice * quantity);
  }

  refreshOrder(): void {
    const order = this.order();
    if (order) {
      this.loadOrder(order.id);
    }
  }
}
