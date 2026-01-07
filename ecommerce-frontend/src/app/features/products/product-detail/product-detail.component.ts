import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { ProductResponse } from '../../../core/models/product.model';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    NavbarComponent
  ],
  template: `
    <app-navbar></app-navbar>

    <div class="product-detail-container">
      @if (isLoading) {
        <div class="loading">
          <mat-spinner diameter="60"></mat-spinner>
        </div>
      }

      @if (!isLoading && product) {
        <div class="breadcrumb">
          <a routerLink="/products">Products</a>
          <mat-icon>chevron_right</mat-icon>
          @if (product.category) {
            <span>{{ product.category.name }}</span>
            <mat-icon>chevron_right</mat-icon>
          }
          <span class="current">{{ product.name }}</span>
        </div>

        <div class="product-content">
          <!-- Image Gallery -->
          <div class="image-section">
            <div class="main-image">
              <img [src]="selectedImage" [alt]="product.name" (error)="onImageError($event)">
              @if (product.onSale) {
                <mat-chip class="sale-badge">SALE</mat-chip>
              }
              @if (product.featured) {
                <mat-chip class="featured-badge">FEATURED</mat-chip>
              }
            </div>
            @if (product.images && product.images.length > 1) {
              <div class="thumbnail-list">
                @for (image of product.images; track image) {
                  <img 
                    [src]="image" 
                    [alt]="product.name"
                    [class.active]="image === selectedImage"
                    (click)="selectedImage = image"
                    (error)="onImageError($event)"
                  >
                }
              </div>
            }
          </div>

          <!-- Product Info -->
          <div class="info-section">
            <h1 class="product-name">{{ product.name }}</h1>
            
            <div class="meta-info">
              <span class="sku">SKU: {{ product.sku }}</span>
              @if (product.brand) {
                <span class="brand">Brand: {{ product.brand }}</span>
              }
            </div>

            @if (product.averageRating && product.reviewCount > 0) {
              <div class="rating">
                <div class="stars">
                  @for (star of [1,2,3,4,5]; track star) {
                    <mat-icon [class.filled]="star <= (product.averageRating || 0)">star</mat-icon>
                  }
                </div>
                <span class="rating-value">{{ product.averageRating?.toFixed(1) }}</span>
                <span class="reviews">({{ product.reviewCount }} reviews)</span>
              </div>
            }

            <mat-divider></mat-divider>

            <div class="price-section">
              @if (product.discountPrice && product.discountPrice < product.price) {
                <div class="price-with-discount">
                  <span class="original-price">\${{ product.price.toFixed(2) }}</span>
                  <span class="discount-price">\${{ product.discountPrice.toFixed(2) }}</span>
                  <mat-chip class="discount-badge">
                    Save {{ calculateDiscount() }}%
                  </mat-chip>
                </div>
              } @else {
                <div class="regular-price">\${{ product.price.toFixed(2) }}</div>
              }
            </div>

            <div class="stock-section">
              @if (product.stockQuantity > 0) {
                <mat-icon class="in-stock">check_circle</mat-icon>
                <span class="stock-text">In Stock ({{ product.stockQuantity }} available)</span>
              } @else {
                <mat-icon class="out-of-stock">cancel</mat-icon>
                <span class="stock-text out">Out of Stock</span>
              }
            </div>

            <div class="description">
              <h3>Description</h3>
              <p>{{ product.description }}</p>
            </div>

            @if (product.weight || product.dimensions) {
              <div class="specifications">
                <h3>Specifications</h3>
                @if (product.weight) {
                  <div class="spec-item">
                    <span class="label">Weight:</span>
                    <span class="value">{{ product.weight }} kg</span>
                  </div>
                }
                @if (product.dimensions) {
                  <div class="spec-item">
                    <span class="label">Dimensions:</span>
                    <span class="value">{{ product.dimensions }}</span>
                  </div>
                }
              </div>
            }

            <div class="actions">
              <button 
                mat-raised-button 
                color="primary" 
                class="add-to-cart"
                [disabled]="product.stockQuantity === 0"
                (click)="addToCart()"
              >
                <mat-icon>shopping_cart</mat-icon>
                Add to Cart
              </button>
              <button mat-stroked-button class="wishlist">
                <mat-icon>favorite_border</mat-icon>
                Add to Wishlist
              </button>
            </div>

            <div class="stats">
              <div class="stat-item">
                <mat-icon>visibility</mat-icon>
                <span>{{ product.viewCount }} views</span>
              </div>
              <div class="stat-item">
                <mat-icon>shopping_bag</mat-icon>
                <span>{{ product.soldCount }} sold</span>
              </div>
            </div>
          </div>
        </div>
      }

      @if (!isLoading && !product) {
        <div class="not-found">
          <mat-icon>search_off</mat-icon>
          <h2>Product Not Found</h2>
          <p>The product you're looking for doesn't exist.</p>
          <button mat-raised-button color="primary" routerLink="/products">
            Back to Products
          </button>
        </div>
      }
    </div>
  `,
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);
  private snackBar = inject(MatSnackBar);

  product: ProductResponse | null = null;
  selectedImage = '';
  isLoading = false;

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(+productId);
    }
  }

  loadProduct(id: number): void {
    this.isLoading = true;
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.selectedImage = product.images?.[0] || 'https://via.placeholder.com/600x600?text=No+Image';
        this.isLoading = false;
        
        // Increment view count
        this.productService.incrementViewCount(id).subscribe();
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.isLoading = false;
      }
    });
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://via.placeholder.com/600x600?text=No+Image';
  }

  calculateDiscount(): number {
    if (this.product?.discountPrice && this.product?.price) {
      return Math.round(((this.product.price - this.product.discountPrice) / this.product.price) * 100);
    }
    return 0;
  }

  addToCart(): void {
    // TODO: Implement add to cart
    this.snackBar.open('Added to cart!', 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }
}
