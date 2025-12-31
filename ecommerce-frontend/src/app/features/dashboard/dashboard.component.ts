import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '../../core/services/auth.service';
import { UserInfo } from '../../core/models/auth.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule
  ],
  template: `
    <div class="dashboard-container">
      <mat-toolbar color="primary">
        <span>E-Commerce Platform</span>
        <span class="spacer"></span>
        @if (currentUser) {
          <span class="user-info">
            <mat-icon>person</mat-icon>
            {{ currentUser.username }}
          </span>
        }
        <button mat-button (click)="logout()">
          <mat-icon>logout</mat-icon>
          Logout
        </button>
      </mat-toolbar>

      <div class="content">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Welcome to Dashboard!</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @if (currentUser) {
              <div class="user-details">
                <h3>User Information</h3>
                <p><strong>Username:</strong> {{ currentUser.username }}</p>
                <p><strong>Email:</strong> {{ currentUser.email }}</p>
                @if (currentUser.firstName || currentUser.lastName) {
                  <p><strong>Name:</strong> {{ currentUser.firstName }} {{ currentUser.lastName }}</p>
                }
                <p><strong>Roles:</strong> {{ currentUser.roles.join(', ') }}</p>
              </div>
            }
            <p class="coming-soon">More features coming soon...</p>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      height: 100vh;
      display: flex;
      flex-direction: column;
    }

    mat-toolbar {
      .spacer {
        flex: 1 1 auto;
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-right: 20px;
      }
    }

    .content {
      flex: 1;
      padding: 20px;
      overflow: auto;

      mat-card {
        max-width: 800px;
        margin: 0 auto;

        .user-details {
          margin: 20px 0;

          h3 {
            margin-bottom: 15px;
            color: #667eea;
          }

          p {
            margin: 8px 0;
            font-size: 14px;

            strong {
              color: #333;
            }
          }
        }

        .coming-soon {
          margin-top: 30px;
          padding: 20px;
          background: #f5f5f5;
          border-radius: 8px;
          text-align: center;
          color: #666;
        }
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser: UserInfo | null = null;

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
        // Force logout even on error
        this.authService.forceLogout();
      }
    });
  }
}
