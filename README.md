# Bakery Shop E-commerce Website

A full-featured e-commerce website for a bakery shop built with Java Spring Boot.

## Features

### Customer Features
- Browse products by category
- View product details
- Search products
- Shopping cart functionality
- User registration and authentication
- Order management
- VNPay payment integration
- Email notifications
- Password reset functionality
- View order history
- Blog section

### Admin Features
- Product management
- Category management
- Order management
- User management
- Blog management
- Sales statistics and reports

## Technologies Used

- Java 11
- Spring Boot 2.7.0
- Spring Security with JWT
- Spring Data JPA
- SQL Server
- Thymeleaf
- Bootstrap
- VNPay Payment Integration

## Prerequisites

- JDK 11
- Maven
- SQL Server
- SMTP Server (for email notifications)
- VNPay Merchant Account (for payment integration)

## Configuration

1. Database Configuration (application.properties):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=bakeryshop
spring.datasource.username=your_username
spring.datasource.password=your_password
```

2. Email Configuration:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

3. VNPay Configuration:
```properties
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_HASH_SECRET
```

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/bakery-shop.git
```

2. Navigate to the project directory:
```bash
cd bakery-shop
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080/bakery-shop`

## API Documentation

### Authentication Endpoints
- POST `/api/auth/signin` - User login
- POST `/api/auth/signup` - User registration
- GET `/api/auth/verify` - Email verification
- POST `/api/auth/forgot-password` - Password reset request
- POST `/api/auth/reset-password` - Password reset

### Product Endpoints
- GET `/api/products` - Get all products
- GET `/api/products/{id}` - Get product by ID
- GET `/api/products/category/{categoryId}` - Get products by category
- GET `/api/products/search` - Search products

### Order Endpoints
- POST `/api/orders` - Create order
- GET `/api/orders/{id}` - Get order by ID
- GET `/api/orders/user` - Get user's orders

### Payment Endpoints
- POST `/vnpay/create-payment/{orderId}` - Create payment URL
- GET `/vnpay/payment-callback` - Payment callback handler

## Security

The application uses JWT (JSON Web Token) for authentication. Protected endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License. 


http://localhost:8081/bakery-shop
http://localhost:8081/bakery-shop/admin
