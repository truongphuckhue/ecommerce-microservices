// Request DTOs
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

// Response DTOs
export interface UserInfo {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  emailVerified: boolean;
  enabled: boolean;
  roles: string[];
  createdAt: string;
  lastLoginAt?: string;
}

// Generic API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}
