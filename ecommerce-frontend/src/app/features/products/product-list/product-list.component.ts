import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { ProductService } from '../../../core/services/product.service';
import { ProductResponse, PageResponse, ProductSearchCriteria } from '../../../core/models/product.model';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    ProductCardComponent,
    NavbarComponent
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);

  products: ProductResponse[] = [];
  featuredProducts: ProductResponse[] = [];
  isLoading = false;
  
  // Pagination
  totalElements = 0;
  pageSize = 12;
  pageIndex = 0;
  
  // Filters
  searchKeyword = '';
  selectedBrand: string | null = null;
  selectedSort = 'createdAt_desc';
  brands: string[] = [];
  
  // Filter options
  sortOptions = [
    { value: 'createdAt_desc', label: 'Newest First' },
    { value: 'createdAt_asc', label: 'Oldest First' },
    { value: 'price_asc', label: 'Price: Low to High' },
    { value: 'price_desc', label: 'Price: High to Low' },
    { value: 'name_asc', label: 'Name: A-Z' },
    { value: 'name_desc', label: 'Name: Z-A' }
  ];

  ngOnInit(): void {
    this.loadFeaturedProducts();
    this.loadBrands();
    this.loadProducts();
  }

  loadFeaturedProducts(): void {
    this.productService.getFeaturedProducts(8).subscribe({
      next: (products) => {
        this.featuredProducts = products;
      },
      error: (error) => {
        console.error('Error loading featured products:', error);
      }
    });
  }

  loadBrands(): void {
    this.productService.getAllBrands().subscribe({
      next: (brands) => {
        this.brands = brands;
      },
      error: (error) => {
        console.error('Error loading brands:', error);
      }
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    
    const criteria: ProductSearchCriteria = {
      keyword: this.searchKeyword || undefined,
      brand: this.selectedBrand || undefined,
      page: this.pageIndex,
      size: this.pageSize,
      sortBy: this.getSortField(),
      sortDirection: this.getSortDirection()
    };

    this.productService.searchProducts(criteria).subscribe({
      next: (response: PageResponse<ProductResponse>) => {
        this.products = response.content;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.loadProducts();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.loadProducts();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedBrand = null;
    this.selectedSort = 'createdAt_desc';
    this.pageIndex = 0;
    this.loadProducts();
  }

  private getSortField(): string {
    return this.selectedSort.split('_')[0];
  }

  private getSortDirection(): string {
    return this.selectedSort.split('_')[1];
  }

  get hasActiveFilters(): boolean {
    return !!this.searchKeyword || !!this.selectedBrand;
  }
}
