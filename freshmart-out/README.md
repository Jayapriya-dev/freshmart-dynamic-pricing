# 🌿 FreshMart — Smart Grocery Store

**Tech Stack (exactly as specified):**
- Backend: Spring Boot + Spring Security + JWT
- Database: MySQL + JPA/Hibernate
- Frontend: Pure HTML + CSS + Vanilla JS (no frameworks, no npm)
- API Testing: Postman

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

---
## 📸 Screenshots

### Landing Page
![Landing Page](screenshots/Home.png)

### Login Page
![Login Page](screenshots/Login.png)

### Register Page
![Register Page](screenshots/register.png)

### Cart Page
![Shop Page](screenshots/Cart.png)

### Admin Dashboard
![Admin Dashboard](screenshots/Admin.png)

---
## 🗄️ Step 1 — Create Database

```sql
mysql -u root -p
CREATE DATABASE smart_grocery_db;
EXIT;
```

---

## ⚙️ Step 2 — Configure Database Password

Open `src/main/resources/application.properties`:

```properties
spring.datasource.password=your_actual_mysql_password
```

---

## 🚀 Step 3 — Run the Application

```bash
mvn spring-boot:run
```

Wait for:
```
Started SmartGroceryStoreApplication in X seconds
```

---

## 🌐 Step 4 — Open in Browser

```
http://localhost:8080
```

That's it. Spring Boot serves both the API and the HTML frontend.

---

## 📄 Pages

| URL | Page |
|-----|------|
| `http://localhost:8080/` | Landing page |
| `http://localhost:8080/pages/login.html` | Sign In |
| `http://localhost:8080/pages/register.html` | Register |
| `http://localhost:8080/pages/shop.html` | Customer Shop |
| `http://localhost:8080/pages/orders.html` | My Orders |
| `http://localhost:8080/pages/admin.html` | Admin Dashboard |

---

## 🔑 First Time Setup — Create Accounts

Go to `http://localhost:8080/pages/register.html` and create:

1. An **Admin** account (select "Admin" role)
2. A **Customer** account (select "Customer" role)

Or use the Postman collection below.

---

## 📡 API Endpoints (for Postman)

**Base URL:** `http://localhost:8080/api`

### Auth (no token needed)
```
POST /api/auth/register   Body: { "fullName":"...", "email":"...", "password":"...", "role":"CUSTOMER" }
POST /api/auth/login      Body: { "email":"...", "password":"..." }
```
→ Login returns a **JWT token** — copy it for other requests.

### Products (Customer)
```
GET /api/products
GET /api/products/{id}
GET /api/products/category/{category}
GET /api/products/search?keyword=rice
```

### Admin Products (add header: Authorization: Bearer {token})
```
GET    /api/admin/products
POST   /api/admin/products        Body: { name, category, basePrice, stockQuantity, discountPercentage, description, available }
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
```

### Cart (Customer token)
```
GET    /api/cart
POST   /api/cart              Body: { "productId": 1, "quantity": 2 }
PUT    /api/cart/{itemId}?quantity=3
DELETE /api/cart/{itemId}
DELETE /api/cart
```

### Orders
```
POST /api/orders              Body: { "shippingAddress": "..." }
GET  /api/orders              (customer: own orders)
GET  /api/admin/orders        (admin: all orders)
PATCH /api/admin/orders/{id}/status?status=CONFIRMED
```

### Postman Authorization Header
```
Key:   Authorization
Value: Bearer eyJhbGci...  (your JWT token from login)
```

---

## 💰 Dynamic Pricing Rules

| Rule | Condition | Effect |
|------|-----------|--------|
| Admin Discount | discountPercentage > 0 | −N% |
| Low Stock | stockQuantity < 10 | +10% |
| High Stock | stockQuantity > 100 | −5% |
| Weekend | Saturday or Sunday | −5% |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/grocery/store/
│   │   ├── controller/     AuthController, ProductController, CartController, OrderController, AdminController
│   │   ├── service/        Business logic + PricingService
│   │   ├── entity/         User, Product, Order, OrderItem, CartItem
│   │   ├── repository/     JPA repositories
│   │   ├── security/       JwtUtils, JwtAuthenticationFilter
│   │   ├── config/         SecurityConfig
│   │   └── dto/            Request/Response objects
│   └── resources/
│       ├── application.properties
│       └── static/              ← Frontend (served by Spring Boot)
│           ├── index.html
│           ├── css/style.css
│           ├── js/api.js         ← API client with JWT
│           ├── js/ui.js          ← Shared UI components
│           └── pages/
│               ├── login.html
│               ├── register.html
│               ├── shop.html
│               ├── orders.html
│               └── admin.html
```
