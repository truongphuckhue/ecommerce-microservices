import { ThemePalette } from '@angular/material/core';

// Order Item DTO
export interface OrderItem {
  productId: number;
  productName?: string;
  productSku?: string;
  quantity: number;
  unitPrice: number;
  totalPrice?: number;
}

// Order Request DTO
export interface OrderRequest {
  userId: number;
  items: OrderItem[];
  shippingAddress?: string;
  billingAddress?: string;
  paymentMethod?: string;
  notes?: string;
}

// Order Response DTO
export interface OrderResponse {
  id: number;
  orderNumber: string;
  userId: number;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  sagaStatus?: SagaStatus;
  shippingAddress?: string;
  billingAddress?: string;
  paymentMethod?: string;
  notes?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
}

// Order Statuses
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  FAILED = 'FAILED'
}

export enum SagaStatus {
  STARTED = 'STARTED',
  INVENTORY_RESERVED = 'INVENTORY_RESERVED',
  PAYMENT_PROCESSED = 'PAYMENT_PROCESSED',
  COMPLETED = 'COMPLETED',
  COMPENSATING = 'COMPENSATING',
  FAILED = 'FAILED'
}

// Order status display mapping
export const ORDER_STATUS_LABELS: Record<OrderStatus, String> = {
  [OrderStatus.PENDING]: 'Pending',
  [OrderStatus.CONFIRMED]: 'Confirmed',
  [OrderStatus.PROCESSING]: 'Processing',
  [OrderStatus.SHIPPED]: 'Shipped',
  [OrderStatus.DELIVERED]: 'Delivered',
  [OrderStatus.CANCELLED]: 'Cancelled',
  [OrderStatus.FAILED]: 'Failed'
};

// Order status colors for UI
export const ORDER_STATUS_COLORS: Record<OrderStatus, ThemePalette> = {
  [OrderStatus.PENDING]: 'warn',
  [OrderStatus.CONFIRMED]: 'primary',
  [OrderStatus.PROCESSING]: 'accent',
  [OrderStatus.SHIPPED]: 'primary',
  [OrderStatus.DELIVERED]: 'primary',
  [OrderStatus.CANCELLED]: 'warn',
  [OrderStatus.FAILED]: 'warn'
};

// Saga status display mapping
export const SAGA_STATUS_LABELS: Record<SagaStatus, string> = {
  [SagaStatus.STARTED]: 'Started',
  [SagaStatus.INVENTORY_RESERVED]: 'Inventory Reserved',
  [SagaStatus.PAYMENT_PROCESSED]: 'Payment Processed',
  [SagaStatus.COMPLETED]: 'Completed',
  [SagaStatus.COMPENSATING]: 'Compensating',
  [SagaStatus.FAILED]: 'Failed'
};
