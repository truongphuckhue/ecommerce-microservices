import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { 
  ProductResponse, 
  PageResponse, 
  ProductSearchCriteria 
} from '../models/product.model';
import { ApiResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly API_URL = 'http://localhost:8082/products'; // Product service port
  private http = inject(HttpClient);

  // Get all products with pagination
  getAllProducts(
    page: number = 0, 
    size: number = 20, 
    sortBy: string = 'createdAt', 
    sortDirection: string = 'desc'
  ): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(this.API_URL, { params })
      .pipe(map(response => response.data));
  }

  // Get product by ID
  getProductById(id: number): Observable<ProductResponse> {
    return this.http.get<ApiResponse<ProductResponse>>(`${this.API_URL}/${id}`)
      .pipe(map(response => response.data));
  }

  // Get product by SKU
  getProductBySku(sku: string): Observable<ProductResponse> {
    return this.http.get<ApiResponse<ProductResponse>>(`${this.API_URL}/sku/${sku}`)
      .pipe(map(response => response.data));
  }

  // Search products
  searchProducts(criteria: ProductSearchCriteria): Observable<PageResponse<ProductResponse>> {
    return this.http.post<ApiResponse<PageResponse<ProductResponse>>>(
      `${this.API_URL}/search`, 
      criteria
    ).pipe(map(response => response.data));
  }

  // Get products by category
  getProductsByCategory(
    categoryId: number, 
    page: number = 0, 
    size: number = 20
  ): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(
      `${this.API_URL}/category/${categoryId}`, 
      { params }
    ).pipe(map(response => response.data));
  }

  // Get featured products
  getFeaturedProducts(limit: number = 10): Observable<ProductResponse[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<ProductResponse[]>>(`${this.API_URL}/featured`, { params })
      .pipe(map(response => response.data));
  }

  // Get best selling products
  getBestSellingProducts(limit: number = 10): Observable<ProductResponse[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<ProductResponse[]>>(`${this.API_URL}/best-selling`, { params })
      .pipe(map(response => response.data));
  }

  // Get new arrivals
  getNewArrivals(limit: number = 10): Observable<ProductResponse[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<ProductResponse[]>>(`${this.API_URL}/new-arrivals`, { params })
      .pipe(map(response => response.data));
  }

  // Get on-sale products
  getOnSaleProducts(page: number = 0, size: number = 20): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(
      `${this.API_URL}/on-sale`, 
      { params }
    ).pipe(map(response => response.data));
  }

  // Get all brands
  getAllBrands(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.API_URL}/brands`)
      .pipe(map(response => response.data));
  }

  // Increment view count
  incrementViewCount(id: number): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/${id}/view`, {})
      .pipe(map(() => undefined));
  }
}
