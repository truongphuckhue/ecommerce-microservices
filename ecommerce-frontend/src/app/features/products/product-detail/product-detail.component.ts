import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
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
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  product: ProductResponse | null = null;
  selectedImage = '';
  isLoading = false;
  hasError = false;
  errorMessage = '';
  showDebug = true; // ✅ Always show debug panel initially

  ngOnInit(): void {
    console.log('🚀 ProductDetailComponent ngOnInit');
    
    const productId = this.route.snapshot.paramMap.get('id');
    console.log('📦 Product ID from route:', productId);

    if (!productId) {
      console.error('❌ Missing product ID in route');
      this.hasError = true;
      this.errorMessage = 'Product ID is missing from the URL';
      this.cdr.detectChanges();
      return;
    }

    const id = Number(productId);
    if (isNaN(id)) {
      console.error('❌ Invalid product ID:', productId);
      this.hasError = true;
      this.errorMessage = `Invalid product ID: "${productId}"`;
      this.cdr.detectChanges();
      return;
    }

    console.log('✅ Valid product ID:', id);
    this.loadProduct(id);
  }

  loadProduct(id: number): void {
    console.log('🔄 Loading product with ID:', id);
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        console.log('✅ Product loaded successfully:', product);
        this.product = product;
        this.selectedImage = product.images?.[0] || 'https://placehold.co/600x600?text=No+Image';
        this.isLoading = false;
        
        console.log('🔍 State after loading:', {
          isLoading: this.isLoading,
          hasError: this.hasError,
          productExists: !!this.product,
          productName: this.product?.name
        });
        
        this.cdr.detectChanges();

        // Increment view count
        this.productService.incrementViewCount(id).subscribe({
          next: () => console.log('👁️ View count incremented'),
          error: (err) => console.warn('⚠️ Failed to increment view count:', err)
        });
      },
      error: (error) => {
        console.error('❌ Error loading product:', error);
        this.isLoading = false;
        this.hasError = true;
        
        if (error.status === 404) {
          this.errorMessage = 'Product not found. It may have been removed or the ID is incorrect.';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to the server. Please check your internet connection.';
        } else if (error.status >= 500) {
          this.errorMessage = 'Server error. Please try again later.';
        } else {
          this.errorMessage = error.error?.message || error.message || 'An unexpected error occurred.';
        }
        
        this.cdr.detectChanges();
      }
    });
  }

  retryLoad(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      const id = Number(productId);
      if (!isNaN(id)) {
        this.loadProduct(id);
      }
    }
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://placehold.co/600x600?text=No+Image';
  }

  formatPrice(price: number | undefined | null): string {
    if (price == null || isNaN(price)) {
      return '0.00';
    }
    return price.toFixed(2);
  }

  formatRating(rating: number | undefined | null): string {
    if (rating == null || isNaN(rating)) {
      return '0.0';
    }
    return rating.toFixed(1);
  }

  calculateDiscount(): number {
    if (this.product?.discountPrice && this.product?.price) {
      return Math.round(((this.product.price - this.product.discountPrice) / this.product.price) * 100);
    }
    return 0;
  }

  addToCart(): void {
    this.snackBar.open('Added to cart!', 'Close', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top'
    });
  }
}
