import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { OrderRequest, OrderResponse, OrderStatus } from '../models/order.model';
import { ApiResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = 'http://localhost:8084/api/orders'; // Order service port
  private http = inject(HttpClient);

  /**
   * Create new order
   * POST /api/orders
   */
  createOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<ApiResponse<OrderResponse>>(this.API_URL, request)
      .pipe(map(response => response.data));
  }

  /**
   * Get order by ID
   * GET /api/orders/{id}
   */
  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<ApiResponse<OrderResponse>>(`${this.API_URL}/${id}`)
      .pipe(map(response => response.data));
  }

  /**
   * Get order by order number
   * GET /api/orders/number/{orderNumber}
   */
  getOrderByOrderNumber(orderNumber: string): Observable<OrderResponse> {
    return this.http.get<ApiResponse<OrderResponse>>(`${this.API_URL}/number/${orderNumber}`)
      .pipe(map(response => response.data));
  }

  /**
   * Get all orders for a user
   * GET /api/orders/user/{userId}
   */
  getUserOrders(userId: number): Observable<OrderResponse[]> {
    return this.http.get<ApiResponse<OrderResponse[]>>(`${this.API_URL}/user/${userId}`)
      .pipe(map(response => response.data));
  }

  /**
   * Get orders by user and status
   * GET /api/orders/user/{userId}/status/{status}
   */
  getUserOrdersByStatus(userId: number, status: OrderStatus): Observable<OrderResponse[]> {
    return this.http.get<ApiResponse<OrderResponse[]>>(
      `${this.API_URL}/user/${userId}/status/${status}`
    ).pipe(map(response => response.data));
  }

  /**
   * Cancel order
   * PUT /api/orders/{id}/cancel
   */
  cancelOrder(id: number, reason?: string): Observable<OrderResponse> {
    let params = new HttpParams();
    if (reason) {
      params = params.set('reason', reason);
    }
    
    return this.http.put<ApiResponse<OrderResponse>>(
      `${this.API_URL}/${id}/cancel`,
      null,
      { params }
    ).pipe(map(response => response.data));
  }

  /**
   * Get order status
   * GET /api/orders/{id}/status
   */
  getOrderStatus(id: number): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.API_URL}/${id}/status`)
      .pipe(map(response => response.data));
  }

  /**
   * Get saga status (for debugging)
   * GET /api/orders/{id}/saga-status
   */
  getSagaStatus(id: number): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.API_URL}/${id}/saga-status`)
      .pipe(map(response => response.data));
  }

  /**
   * Get all orders (admin)
   * GET /api/orders
   */
  getAllOrders(): Observable<OrderResponse[]> {
    return this.http.get<ApiResponse<OrderResponse[]>>(this.API_URL)
      .pipe(map(response => response.data));
  }

  /**
   * Get failed orders (monitoring)
   * GET /api/orders/failed
   */
  getFailedOrders(): Observable<OrderResponse[]> {
    return this.http.get<ApiResponse<OrderResponse[]>>(`${this.API_URL}/failed`)
      .pipe(map(response => response.data));
  }
}
