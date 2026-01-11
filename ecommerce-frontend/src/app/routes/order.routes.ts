import { Routes } from '@angular/router';
import { authGuard } from '../core/guards/auth.guard';

// Import order components
import { OrderListComponent } from '../features/orders/order-list/order-list.component';
import { OrderDetailComponent } from '../features/orders/order-detail/order-detail.component';
import { CreateOrderComponent } from '../features/orders/create-order/create-order.component';
import { OrderAdminComponent } from '../features/orders/order-admin/order-admin.component';

/**
 * Order Routes
 * These routes should be added to your main app.routes.ts file
 *
 * Example integration in app.routes.ts:
 *
 * import { orderRoutes } from './routes/order.routes';
 *
 * export const routes: Routes = [
 *   // ... other routes
 *   ...orderRoutes,
 *   // ... more routes
 * ];
 */
export const orderRoutes: Routes = [
  {
    path: 'orders',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: OrderListComponent,
        title: 'My Orders'
      },
      {
        path: 'create',
        component: CreateOrderComponent,
        title: 'Create Order'
      },
      {
        path: 'admin',
        component: OrderAdminComponent,
        title: 'Order Administration'
        // Optional: Add role-based guard for admin access
        // canActivate: [adminGuard]
      },
      {
        path: ':id',
        component: OrderDetailComponent,
        title: 'Order Details'
      }
    ]
  }
];

/**
 * Alternative: Lazy-loaded module approach
 *
 * If you prefer lazy loading, you can use this in app.routes.ts:
 *
 * {
 *   path: 'orders',
 *   canActivate: [authGuard],
 *   loadChildren: () => import('./routes/order.routes').then(m => m.orderRoutes)
 * }
 */
