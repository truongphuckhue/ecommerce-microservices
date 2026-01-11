import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

import { OrderService } from '../../../core/services/order.service';
import { ProductService } from '../../../core/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderRequest, OrderItem } from '../../../core/models/order.model';
import { ProductResponse } from '../../../core/models/product.model';

@Component({
  selector: 'app-create-order',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSelectModule,
    MatStepperModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  templateUrl: './create-order.component.html',
  styleUrl: './create-order.component.scss'
})
export class CreateOrderComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  orderForm!: FormGroup;
  isLoading = signal(false);
  products = signal<ProductResponse[]>([]);

  paymentMethods = [
    { value: 'CREDIT_CARD', label: 'Credit Card' },
    { value: 'DEBIT_CARD', label: 'Debit Card' },
    { value: 'PAYPAL', label: 'PayPal' },
    { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
    { value: 'CASH_ON_DELIVERY', label: 'Cash on Delivery' }
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadProducts();
    this.handlePreselectedProduct();
  }

  /**
   * Handle preselected product from "Buy Now" navigation
   */
  private handlePreselectedProduct(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras?.state || history.state;
    const preselected = state?.['preselectedProduct'];

    if (preselected) {
      console.log('✅ Preselected product:', preselected);

      // Auto-fill first item
      const firstItem = this.items.at(0) as FormGroup;
      firstItem.patchValue({
        productId: preselected.id,
        quantity: preselected.quantity || 1,
        unitPrice: preselected.price
      });

      // Show success message
      setTimeout(() => {
        this.snackBar.open(
          `${preselected.name} added to your order`,
          'Close',
          { duration: 3000 }
        );
      }, 500);
    }
  }

  private initForm(): void {
    const currentUser = this.authService.currentUserValue;

    this.orderForm = this.fb.group({
      userId: [currentUser?.id || null, Validators.required],
      items: this.fb.array([this.createOrderItem()], Validators.required),
      shippingAddress: ['', Validators.required],
      billingAddress: [''],
      paymentMethod: ['', Validators.required],
      notes: ['']
    });
  }

  private createOrderItem(): FormGroup {
    return this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0]
    });
  }

  get items(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  addItem(): void {
    this.items.push(this.createOrderItem());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  private loadProducts(): void {
    this.productService.getAllProducts(0, 100).subscribe({
      next: (response) => {
        this.products.set(response.content);
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.snackBar.open('Failed to load products', 'Close', { duration: 3000 });
      }
    });
  }

  onProductChange(index: number, productId: number): void {
    const product = this.products().find(p => p.id === productId);
    if (product) {
      const itemGroup = this.items.at(index) as FormGroup;
      itemGroup.patchValue({
        unitPrice: product.effectivePrice
      });
    }
  }

  getItemTotal(index: number): number {
    const item = this.items.at(index).value;
    return (item.quantity || 0) * (item.unitPrice || 0);
  }

  getOrderTotal(): number {
    return this.items.controls.reduce((total, control) => {
      const item = control.value;
      return total + ((item.quantity || 0) * (item.unitPrice || 0));
    }, 0);
  }

  copyShippingToBilling(): void {
    const shippingAddress = this.orderForm.get('shippingAddress')?.value;
    this.orderForm.patchValue({
      billingAddress: shippingAddress
    });
  }

  onSubmit(): void {
    if (this.orderForm.invalid) {
      this.snackBar.open('Please fill in all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    const orderRequest: OrderRequest = this.orderForm.value;

    this.orderService.createOrder(orderRequest).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.snackBar.open('Order created successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/orders', response.id]);
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Error creating order:', error);
        this.snackBar.open(
          error.error?.message || 'Failed to create order. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  getProductName(productId: number): string {
    const product = this.products().find(p => p.id === productId);
    return product ? product.name : '';
  }

  getPaymentMethodLabel(): string {
    const value = this.orderForm.get('paymentMethod')?.value;
    const method = this.paymentMethods.find(m => m.value === value);
    return method ? method.label : '';
  }
}
