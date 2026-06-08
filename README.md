# 🌿 Sarvasva Naturals — Full Stack E-Commerce Platform

Premium Ayurvedic Spices E-Commerce built with **Java Spring Boot + Thymeleaf**.

---

## 🚀 Tech Stack

| Layer        | Technology                              |
|-------------|------------------------------------------|
| Backend     | Java 17, Spring Boot 3.2                |
| Security    | Spring Security 6, BCrypt, JWT          |
| ORM         | Spring Data JPA, Hibernate              |
| Database    | H2 (dev) / MySQL 8 (prod)              |
| Templates   | Thymeleaf 3 + Thymeleaf Security Extras|
| Payments    | Razorpay (India), Stripe (International), COD |
| Email       | Spring Mail (Gmail SMTP)                |
| Images      | Cloudinary CDN                          |
| Build       | Maven 3.9                               |

---

## 📁 Project Structure

```
sarvasvanaturals/
├── src/main/java/com/sarvasvanaturals/
│   ├── SarvasvanaturalsApplication.java   ← Entry point
│   ├── config/
│   │   ├── SecurityConfig.java            ← Spring Security setup
│   │   └── DataInitializer.java           ← Seeds DB on startup
│   ├── controller/
│   │   ├── HomeController.java            ← Home, search, pages
│   │   ├── ShopController.java            ← Shop, product, cart API
│   │   ├── CheckoutController.java        ← Checkout + payments
│   │   ├── AuthController.java            ← Login, register, reset
│   │   └── AdminController.java           ← Admin panel
│   ├── model/                             ← JPA entities
│   ├── repository/                        ← Spring Data interfaces
│   └── service/
│       ├── ProductService.java
│       ├── CartService.java
│       ├── OrderService.java
│       ├── UserService.java
│       ├── PaymentService.java            ← Razorpay + Stripe
│       └── EmailService.java
├── src/main/resources/
│   ├── application.properties             ← All config
│   ├── templates/                         ← Thymeleaf HTML pages
│   └── static/
│       ├── css/main.css                   ← Full design system
│       ├── css/components.css             ← UI components
│       └── js/main.js                     ← Cart, payments JS
└── pom.xml
```

---

## ⚡ Quick Start (Development)

### Prerequisites
- Java 17+
- Maven 3.9+

### 1. Clone & Run
```bash
git clone <your-repo-url>
cd sarvasvanaturals
mvn spring-boot:run
```

App starts at **http://localhost:8080**

**Default Admin:** `admin@sarvasvanaturals.com` / `Admin@123`
**H2 Console:** http://localhost:8080/h2-console

---

## 🗄️ Switch to MySQL (Production)

In `application.properties`, comment H2 and uncomment MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sarvasvadb?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

Create the database:
```sql
CREATE DATABASE sarvasvadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 💳 Payment Gateway Setup

### Razorpay (Primary — India)
1. Create account at [razorpay.com](https://razorpay.com)
2. Get Test Key ID and Secret from Dashboard
3. Update in `application.properties`:
```properties
razorpay.key.id=rzp_test_YOUR_KEY_ID
razorpay.key.secret=YOUR_SECRET
```

### Stripe (International)
1. Create account at [stripe.com](https://stripe.com)
2. Get publishable + secret keys
3. Update in `application.properties`:
```properties
stripe.secret.key=sk_test_YOUR_SECRET
stripe.publishable.key=pk_test_YOUR_PUBLISHABLE
```

### COD (Cash on Delivery)
No setup needed — works out of the box.

---

## 📧 Email Setup (Gmail)

1. Enable 2FA on your Gmail account
2. Generate App Password: Google Account → Security → App Passwords
3. Update:
```properties
spring.mail.username=your@gmail.com
spring.mail.password=your_16_char_app_password
```

---

## 🌐 Environment Variables (Production)

For production, use environment variables instead of hardcoding:

```bash
export RAZORPAY_KEY_ID=rzp_live_xxx
export RAZORPAY_KEY_SECRET=xxx
export STRIPE_SECRET_KEY=sk_live_xxx
export STRIPE_PUBLISHABLE_KEY=pk_live_xxx
export MAIL_USERNAME=your@gmail.com
export MAIL_PASSWORD=your_app_password
export ADMIN_EMAIL=admin@sarvasvanaturals.com
export ADMIN_PASSWORD=StrongPassword@123
export JWT_SECRET=your_very_long_random_secret_key
```

---

## 🏗️ Build for Production

```bash
mvn clean package -DskipTests
java -jar target/sarvasvanaturals-1.0.0.jar
```

### Docker (optional)
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/sarvasvanaturals-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t sarvasvanaturals .
docker run -p 8080:8080 --env-file .env sarvasvanaturals
```

---

## 🎯 Features

### Customer
- ✅ Browse, search & filter products
- ✅ Persistent cart (guest + logged in)
- ✅ Full checkout with address management
- ✅ Razorpay (UPI/Cards/Wallets), Stripe, COD payments
- ✅ Order tracking & history
- ✅ Email confirmations + shipping updates
- ✅ Batch purity verification
- ✅ Password reset via email
- ✅ Mobile-responsive design

### Admin
- ✅ Dashboard with revenue/orders stats
- ✅ Product CRUD with image support
- ✅ Category management
- ✅ Order management + status updates + tracking
- ✅ Coupon code creation (% or fixed)
- ✅ Review moderation

---

## 📱 Responsive Design

Fully responsive across:
- 📱 Mobile (320px+)
- 📱 Tablet (768px+)
- 💻 Laptop (1024px+)
- 🖥️ Desktop (1400px+)

---

## 🔒 Security Features

- BCrypt password hashing (strength 12)
- CSRF protection on all forms
- Role-based access (CUSTOMER / ADMIN)
- Session management
- Razorpay webhook signature verification
- Stripe payment intent confirmation

---

## 📄 License

© 2024 Sarvasva Naturals. All rights reserved.
