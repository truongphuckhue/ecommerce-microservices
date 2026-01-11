import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { UserInfo } from '../../../core/models/auth.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary" class="navbar">
      <div class="navbar-content">
        <!-- Logo -->
        <div class="logo" routerLink="/">
          <mat-icon>shopping_bag</mat-icon>
          <span>E-Shop</span>
        </div>

        <!-- Navigation Links -->
        <nav class="nav-links">
          <a mat-button routerLink="/products" routerLinkActive="active">
            <mat-icon>store</mat-icon>
            Products
          </a>
          <a mat-button routerLink="/products/featured" routerLinkActive="active">
            <mat-icon>star</mat-icon>
            Featured
          </a>
          <a mat-button routerLink="/products/sale" routerLinkActive="active">
            <mat-icon>local_offer</mat-icon>
            Sale
          </a>
        </nav>

        <span class="spacer"></span>

        <!-- User Actions -->
        <div class="user-actions">
          <!-- Cart -->
          <button mat-icon-button routerLink="/cart" [matBadge]="cartItemCount" matBadgeColor="warn">
            <mat-icon>shopping_cart</mat-icon>
          </button>

          @if (currentUser) {
            <!-- User Menu -->
            <button mat-button [matMenuTriggerFor]="userMenu" class="user-button">
              <mat-icon>person</mat-icon>
              <span class="username">{{ currentUser.username }}</span>
              <mat-icon>arrow_drop_down</mat-icon>
            </button>
            <mat-menu #userMenu="matMenu">
              <button mat-menu-item routerLink="/profile">
                <mat-icon>account_circle</mat-icon>
                <span>My Profile</span>
              </button>
              <button mat-menu-item routerLink="/orders">
                <mat-icon>receipt_long</mat-icon>
                <span>My Orders</span>
              </button>
              <mat-divider></mat-divider>

              <button mat-menu-item routerLink="/orders/create">
                <mat-icon>add_shopping_cart</mat-icon>
                <span>Create Order</span>
              </button>

              <button mat-menu-item routerLink="/orders">
                <mat-icon>receipt_long</mat-icon>
                <span>My Orders</span>
              </button>

              <button mat-menu-item (click)="logout()">
                <mat-icon>logout</mat-icon>
                <span>Logout</span>
              </button>
            </mat-menu>
          } @else {
            <!-- Login Button -->
            <button mat-raised-button color="accent" routerLink="/auth/login">
              <mat-icon>login</mat-icon>
              Login
            </button>
          }
        </div>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .navbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);

      .navbar-content {
        display: flex;
        align-items: center;
        width: 100%;
        max-width: 1400px;
        margin: 0 auto;

        .logo {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          font-size: 20px;
          font-weight: 600;

          mat-icon {
            font-size: 28px;
            width: 28px;
            height: 28px;
          }
        }

        .nav-links {
          display: flex;
          gap: 8px;
          margin-left: 40px;

          a {
            display: flex;
            align-items: center;
            gap: 6px;

            &.active {
              background: rgba(255, 255, 255, 0.1);
            }

            mat-icon {
              font-size: 20px;
              width: 20px;
              height: 20px;
            }
          }
        }

        .spacer {
          flex: 1 1 auto;
        }

        .user-actions {
          display: flex;
          align-items: center;
          gap: 12px;

          .user-button {
            display: flex;
            align-items: center;
            gap: 4px;

            .username {
              max-width: 120px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
      }
    }

    @media (max-width: 768px) {
      .navbar-content {
        .nav-links {
          display: none;
        }

        .user-actions {
          .user-button .username {
            display: none;
          }
        }
      }
    }
  `]
})
export class NavbarComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser: UserInfo | null = null;
  cartItemCount = 0; // TODO: Get from cart service

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.authService.forceLogout();
      }
    });
  }
}
