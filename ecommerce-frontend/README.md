# E-Commerce Platform - Angular Frontend

Modern Angular 18 frontend application for the E-Commerce microservices platform.

## Features

- ✅ **Login/Register** with JWT authentication
- ✅ **Material Design** UI with Angular Material
- ✅ **Reactive Forms** with validation
- ✅ **HTTP Interceptor** for automatic token refresh
- ✅ **Route Guards** for protected routes
- ✅ **Standalone Components** (modern Angular approach)

## Tech Stack

- **Angular 18** - Latest version with standalone components
- **Angular Material** - Professional UI components
- **RxJS** - Reactive programming
- **TypeScript** - Type-safe development
- **SCSS** - Advanced styling

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 18
- Backend services running (User Service on port 8081)

## Installation

```bash
# Install dependencies
npm install

# Start development server
npm start

# Or run with custom port
ng serve --port 4200
```

The application will be available at: **http://localhost:4200**

## Project Structure

```
src/app/
├── core/                      # Core functionality
│   ├── models/               # TypeScript interfaces/models
│   │   └── auth.model.ts    # Auth request/response models
│   ├── services/            # Business logic services
│   │   └── auth.service.ts  # Authentication service
│   ├── guards/              # Route guards
│   │   └── auth.guard.ts    # Protect authenticated routes
│   └── interceptors/        # HTTP interceptors
│       └── jwt.interceptor.ts # Auto-attach JWT tokens
├── features/                # Feature modules
│   ├── auth/
│   │   ├── login/          # Login component
│   │   └── register/       # Register component
│   └── dashboard/          # Dashboard (protected)
└── shared/                 # Shared components/utilities
```

## API Endpoints

The frontend connects to these backend endpoints:

- **POST** `/auth/register` - User registration
- **POST** `/auth/login` - User login
- **POST** `/auth/logout` - User logout
- **POST** `/auth/refresh` - Refresh access token
- **GET** `/auth/me` - Get current user
- **GET** `/auth/verify-email?token=xxx` - Verify email
- **POST** `/auth/resend-verification?email=xxx` - Resend verification

## Configuration

Backend API URL is configured in `auth.service.ts`:

```typescript
private readonly API_URL = 'http://localhost:8081/auth';
```

Change this if your User Service runs on a different port.

## Features Details

### Login Page
- Username/email + password authentication
- Password visibility toggle
- Form validation with error messages
- Redirect to dashboard on success

### Register Page
- Username (3-50 chars, alphanumeric + underscore/hyphen)
- Email validation
- Password strength requirements:
  - Min 8 characters
  - At least 1 uppercase letter
  - At least 1 lowercase letter
  - At least 1 number
  - At least 1 special character (@#$%^&+=)
- Password confirmation
- First name, last name (optional)
- Phone number (optional)
- Email verification sent after registration

### Security Features
- JWT token storage in localStorage
- Automatic token refresh on 401 errors
- HTTP interceptor for authorization headers
- Auth guard for protected routes
- Secure password validation

## Build for Production

```bash
# Production build
npm run build

# Output will be in dist/ecommerce-frontend/browser/
```

## CORS Configuration

Make sure your backend User Service allows CORS for `http://localhost:4200`:

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

## Troubleshooting

**Issue: CORS errors**
- Enable CORS in backend services
- Check API_URL configuration

**Issue: 401 Unauthorized**
- Check if backend is running
- Verify token is being sent in headers

**Issue: Styles not loading**
- Run `npm install`
- Check Material is installed

---

**Happy Coding! 🚀**
