// Product Response DTO
export interface CategorySummary {
  id: number;
  name: string;
  slug: string;
}

export interface ProductResponse {
  id: number;
  name: string;
  sku: string;
  description: string;
  price: number;
  discountPrice?: number;
  effectivePrice: number;
  category: CategorySummary;
  active: boolean;
  stockQuantity: number;
  images: string[];
  brand?: string;
  weight?: number;
  dimensions?: string;
  viewCount: number;
  soldCount: number;
  averageRating?: number;
  reviewCount: number;
  featured: boolean;
  onSale: boolean;
  createdAt: string;
  updatedAt: string;
}

// Pagination wrapper
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}

// Search Criteria
export interface ProductSearchCriteria {
  keyword?: string;
  categoryId?: number;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  featured?: boolean;
  onSale?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}
