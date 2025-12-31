import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map } from 'rxjs';
import { Router } from '@angular/router';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UserResponse,
  ApiResponse,
  UserInfo,
  RefreshTokenRequest
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8081/auth'; // User service port
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  private http = inject(HttpClient);
  private router = inject(Router);

  private currentUserSubject = new BehaviorSubject<UserInfo | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {}

  // Register new user
  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.API_URL}/register`, request)
      .pipe(
        map(response => response.data)
      );
  }

  // Login
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/login`, request)
      .pipe(
        map(response => response.data),
        tap(authResponse => this.handleAuthSuccess(authResponse))
      );
  }

  // Logout
  logout(): Observable<void> {
    const token = this.getAccessToken();
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/logout`, {}, {
      headers: { 'Authorization': `Bearer ${token}` }
    }).pipe(
      map(() => undefined),
      tap(() => this.handleLogout())
    );
  }

  // Refresh token
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const request: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/refresh`, request)
      .pipe(
        map(response => response.data),
        tap(authResponse => this.handleAuthSuccess(authResponse))
      );
  }

  // Get current user
  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.API_URL}/me`)
      .pipe(
        map(response => response.data)
      );
  }

  // Verify email
  verifyEmail(token: string): Observable<void> {
    return this.http.get<ApiResponse<void>>(`${this.API_URL}/verify-email`, {
      params: { token }
    }).pipe(
      map(() => undefined)
    );
  }

  // Resend verification email
  resendVerification(email: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.API_URL}/resend-verification`, null, {
      params: { email }
    }).pipe(
      map(() => undefined)
    );
  }

  // Handle successful authentication
  private handleAuthSuccess(authResponse: AuthResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, authResponse.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse.user));
    this.currentUserSubject.next(authResponse.user);
  }

  // Handle logout
  private handleLogout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  // Get access token
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  // Get refresh token
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  // Get user from storage
  private getUserFromStorage(): UserInfo | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  // Get current user value
  get currentUserValue(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  // Force logout (for errors)
  forceLogout(): void {
    this.handleLogout();
  }
}
