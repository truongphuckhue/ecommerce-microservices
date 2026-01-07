import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { ProductResponse } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatBadgeModule
  ],
  template: `
    <mat-card class="product-card" [routerLink]="['/products', product.id]">
      <!-- Product Image -->
      <div class="image-container">
        @if (product.onSale) {
          <mat-chip class="sale-badge">SALE</mat-chip>
        }
        @if (product.featured) {
          <mat-chip class="featured-badge">Featured</mat-chip>
        }
        <img 
          [src]="productImage" 
          [alt]="product.name"
          (error)="onImageError($event)"
        >
      </div>

      <mat-card-content>
        <!-- Category & Brand -->
        <div class="meta">
          @if (product.category) {
            <span class="category">{{ product.category.name }}</span>
          }
          @if (product.brand) {
            <span class="brand">{{ product.brand }}</span>
          }
        </div>

        <!-- Product Name -->
        <h3 class="product-name">{{ product.name }}</h3>

        <!-- Rating -->
        @if (product.averageRating && product.reviewCount > 0) {
          <div class="rating">
            <mat-icon class="star">star</mat-icon>
            <span class="rating-value">{{ product.averageRating.toFixed(1) }}</span>
            <span class="review-count">({{ product.reviewCount }})</span>
          </div>
        }

        <!-- Price -->
        <div class="price-container">
          @if (product.discountPrice && product.discountPrice < product.price) {
            <div class="price-with-discount">
              <span class="original-price">\${{ product.price.toFixed(2) }}</span>
              <span class="discount-price">\${{ product.discountPrice.toFixed(2) }}</span>
              <span class="discount-percent">
                -{{ calculateDiscount() }}%
              </span>
            </div>
          } @else {
            <span class="regular-price">\${{ product.price.toFixed(2) }}</span>
          }
        </div>

        <!-- Stock Status -->
        <div class="stock-info">
          @if (product.stockQuantity > 0) {
            <mat-icon class="in-stock">check_circle</mat-icon>
            <span class="stock-text">In Stock ({{ product.stockQuantity }})</span>
          } @else {
            <mat-icon class="out-of-stock">cancel</mat-icon>
            <span class="stock-text out">Out of Stock</span>
          }
        </div>
      </mat-card-content>

      <mat-card-actions>
        <button 
          mat-raised-button 
          color="primary" 
          class="add-to-cart-btn"
          [disabled]="product.stockQuantity === 0"
          (click)="onAddToCart($event)"
        >
          <mat-icon>shopping_cart</mat-icon>
          Add to Cart
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .product-card {
      cursor: pointer;
      transition: transform 0.3s, box-shadow 0.3s;
      height: 100%;
      display: flex;
      flex-direction: column;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 16px rgba(0,0,0,0.2);
      }

      .image-container {
        position: relative;
        height: 250px;
        overflow: hidden;
        background: #f5f5f5;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
        }

        &:hover img {
          transform: scale(1.05);
        }

        .sale-badge {
          position: absolute;
          top: 10px;
          right: 10px;
          background: #f44336;
          color: white;
          font-weight: bold;
        }

        .featured-badge {
          position: absolute;
          top: 10px;
          left: 10px;
          background: #ff9800;
          color: white;
          font-weight: bold;
        }
      }

      mat-card-content {
        flex: 1;
        padding: 16px;

        .meta {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 12px;
          color: #666;

          .category {
            color: #667eea;
            font-weight: 500;
          }

          .brand {
            font-style: italic;
          }
        }

        .product-name {
          font-size: 16px;
          font-weight: 500;
          margin: 8px 0;
          color: #333;
          line-height: 1.4;
          height: 44px;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .rating {
          display: flex;
          align-items: center;
          gap: 4px;
          margin: 8px 0;

          .star {
            color: #ffc107;
            font-size: 18px;
            width: 18px;
            height: 18px;
          }

          .rating-value {
            font-weight: 500;
            color: #333;
          }

          .review-count {
            font-size: 12px;
            color: #999;
          }
        }

        .price-container {
          margin: 12px 0;

          .regular-price {
            font-size: 24px;
            font-weight: 600;
            color: #333;
          }

          .price-with-discount {
            display: flex;
            align-items: center;
            gap: 8px;

            .original-price {
              font-size: 16px;
              color: #999;
              text-decoration: line-through;
            }

            .discount-price {
              font-size: 24px;
              font-weight: 600;
              color: #f44336;
            }

            .discount-percent {
              background: #f44336;
              color: white;
              padding: 2px 6px;
              border-radius: 4px;
              font-size: 12px;
              font-weight: bold;
            }
          }
        }

        .stock-info {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          margin-top: 8px;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;

            &.in-stock {
              color: #4caf50;
            }

            &.out-of-stock {
              color: #f44336;
            }
          }

          .stock-text {
            color: #4caf50;

            &.out {
              color: #f44336;
            }
          }
        }
      }

      mat-card-actions {
        padding: 8px 16px 16px;

        .add-to-cart-btn {
          width: 100%;
          height: 42px;
          font-weight: 500;

          mat-icon {
            margin-right: 8px;
          }
        }
      }
    }
  `]
})
export class ProductCardComponent {
  @Input({ required: true }) product!: ProductResponse;

  get productImage(): string {
    if (this.product.images && this.product.images.length > 0) {
      return this.product.images[0];
    }
    return 'https://via.placeholder.com/300x300?text=No+Image';
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://via.placeholder.com/300x300?text=No+Image';
  }

  calculateDiscount(): number {
    if (this.product.discountPrice && this.product.price) {
      const discount = ((this.product.price - this.product.discountPrice) / this.product.price) * 100;
      return Math.round(discount);
    }
    return 0;
  }

  onAddToCart(event: Event): void {
    event.stopPropagation();
    // TODO: Implement add to cart
    console.log('Add to cart:', this.product);
  }
}
