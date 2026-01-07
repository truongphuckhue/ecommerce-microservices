# E-Commerce Platform - Angular Frontend

Professional Angular 18 frontend application for microservices-based e-commerce platform.

## 🚀 Quick Start

```bash
# Extract package
tar -xzf ecommerce-frontend-complete.tar.gz
cd ecommerce-frontend

# Install dependencies
npm install

# Start development server
npm start
```

**Access at:** http://localhost:4200

---

## ✨ Features

### 🔐 Authentication System
- Login with JWT tokens
- Register with email verification  
- Password validation (strong password requirements)
- Auto token refresh on expiry
- Protected routes with guards
- Persistent sessions via localStorage

### 🛍️ Product Catalog
- Product listing with grid layout
- Pagination (12/24/36/48 items per page)
- Search by keyword
- Filter by brand
- Sort options (price, name, date)
- Featured products section
- Product detail pages with full info
- Image gallery with thumbnails
- Stock indicators

### 🎨 UI/UX
- Material Design components
- Responsive layout (mobile, tablet, desktop)
- Loading states for all async operations
- Error handling with user-friendly messages
- Smooth animations and transitions
- Professional color scheme

---

## 📦 Tech Stack

- **Angular 18** - Latest framework
- **Angular Material 18** - UI components
- **RxJS 7.8** - Reactive programming
- **TypeScript 5.9** - Type safety
- **SCSS** - Advanced styling

---

## 📂 Project Structure

```
src/app/
├── core/                      # Core functionality
│   ├── models/               # TypeScript interfaces
│   ├── services/             # Business logic services
│   ├── guards/               # Route guards
│   └── interceptors/         # HTTP interceptors
├── features/                 # Feature modules
│   ├── auth/                # Authentication
│   ├── products/            # Product catalog
│   └── dashboard/           # User dashboard
└── shared/                  # Shared components
    └── components/
```

---

## 🔧 Configuration

### Backend API URLs

**Auth Service** (`src/app/core/services/auth.service.ts`):
```typescript
private readonly API_URL = 'http://localhost:8081/auth';
```

**Product Service** (`src/app/core/services/product.service.ts`):
```typescript
private readonly API_URL = 'http://localhost:8082/products';
```

### CORS Required

Backend services must allow `http://localhost:4200`

---

## 🎯 Routes

| Route | Auth Required | Description |
|-------|--------------|-------------|
| `/` | No | Home (products) |
| `/auth/login` | No | Login page |
| `/auth/register` | No | Register page |
| `/products` | No | Product listing |
| `/products/:id` | No | Product detail |
| `/dashboard` | Yes | User dashboard |

---

## 🛠️ Development Commands

```bash
# Development server
npm start

# Production build  
npm run build

# Watch mode
npm run watch

# Run tests
npm test
```

---

## 🧪 Testing Flow

1. **Register:** `/auth/register`
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `Test@123456`

2. **Login:** `/auth/login`
   - Use credentials above
   
3. **Browse Products:** `/products`
   - Search, filter, sort
   - Click product for details

---

## 🐛 Troubleshooting

**Backend connection errors:**
- Verify services running on ports 8081, 8082
- Check CORS configuration
- Review browser console

**Build fails:**
```bash
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

---

## 🚀 Production Build

```bash
npm run build
```

Output: `dist/ecommerce-frontend/`

Bundle sizes:
- Initial: ~360 KB (~82 KB gzipped)
- Lazy routes: 3-93 KB each

---

## 📈 Roadmap

- [ ] Shopping Cart
- [ ] Checkout Process
- [ ] User Profile
- [ ] Order History
- [ ] Product Reviews

---

**Built with ❤️ using Angular 18**
