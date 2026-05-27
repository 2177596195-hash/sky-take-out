# Take-Out Order Management System (Sky Take-Out)

This is a backend service system for take-out order management built on Spring Boot, featuring complete modules for employee management, order processing, dish management, shopping cart, WeChat Pay, and more.

## Project Overview

This project is a RESTful API backend service for a food delivery platform, designed with a microservices architecture to support multi-platform access (user side and management side). The system integrates third-party services including JWT authentication, Alibaba Cloud OSS file storage, WeChat Mini Program login, and WeChat Pay.

## Technology Stack

- **Framework**: Spring Boot 2.x
- **Persistence Layer**: MyBatis + MySQL
- **Authentication**: JWT Token
- **File Storage**: Alibaba Cloud OSS
- **Payment**: WeChat Pay
- **API Documentation**: Swagger (Knife4j)

## Module Structure

```
sky-take-out
├── sky-common          # Common Module
│   ├── constant      # Constant Definitions
│   ├── context       # Context
│   ├── enumeration   # Enumerations
│   ├── exception     # Custom Exceptions
│   ├── json          # JSON Processing
│   ├── properties    # Configuration Property Classes
│   ├── result        # Unified Response Results
│   └── utils         # Utility Classes
├── sky-pojo           # Data Objects
│   ├── dto           # Data Transfer Objects
│   ├── entity        # Entity Classes
│   └── vo            # View Objects
└── sky-server        # Service Module
    ├── config        # Configuration Classes
    ├── controller    # Controllers
    ├── handler       # Exception Handling
    ├── interceptor   # Interceptors
    ├── mapper        # Data Access Layer
    └── service       # Business Logic
```

## Core Features

### Employee Management
- Employee login/logout
- Add new employees
- Password modification

### User Management
- WeChat Mini Program authorization login
- User information management

### Dish Category Management
- Create, read, update, delete categories
- Enable/disable categories

### Dish Management
- Create, read, update, delete dishes
- Publish/unpublish dishes
- Flavor configuration

### Set Meal Management
- Create, read, update, delete set meals
- Associate dishes with set meals
- Publish/unpublish set meals

### Shopping Cart
- Add items to cart
- View cart contents
- Clear cart

### Order Management
- Submit orders
- Process payments
- Query orders
- Cancel or reject orders
- Update delivery status

### Address Book Management
- Manage delivery addresses
- Set default address

### Data Analytics
- Sales overview
- Order completion rate
- Sales ranking
- User statistics

## Quick Start

### Environment Requirements

- JDK 1.8+
- Maven 3.0+
- MySQL 5.7+

### Configuration

Configure the following in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: your_password

sky:
  alioss:
    endpoint: your_endpoint
    accessKeyId: your_access_key
    accessKeySecret: your_secret
    bucketName: your_bucket
  
  jwt:
    adminSecretKey: your_admin_key
    adminTtl: 3600000
    adminTokenName: token
    
wechat:
  appid: your_appid
  secret: your_secret
```

### Run the Project

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```

After the service starts, visit `http://localhost:8080/doc.html` to view the API documentation.

## API Examples

### Employee Login

```http
POST /admin/employee/login
Content-Type: application/json

{
    "username": "admin",
    "password": "123456"
}
```

### Retrieve Dish List

```http
GET /admin/dish/page?page=1&pageSize=10
Authorization: Bearer {{token}}
```

## Project Structure Highlights

- **Unified Response**: All APIs return a standardized `Result` object
- **Automatic Field Filling**: Automatically populate fields such as creation time, update time, and creator
- **Global Exception Handling**: Centralized exception handling mechanism
- **JWT Authentication**: Separate JWT secrets for administrators and users
- **DTO Separation**: Clear separation of DO/VO/DTO with well-defined responsibilities

## License

This project is for learning and communication purposes only.