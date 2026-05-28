# Take-Out Order Management System (Sky Take-Out)

A food delivery platform backend built with Spring Boot. Currently in active development.

## Tech Stack

| Component | Version |
|-----------|---------|
| Spring Boot | 2.7.3 |
| MyBatis | 2.2.0 |
| MySQL | 8.0+ |
| Druid | 1.2.1 |
| Knife4j (Swagger) | 3.0.2 |
| PageHelper | 1.3.0 |
| JJWT | 0.9.1 |

## Project Structure

```
sky-take-out
├── sky-common               # Shared module: constants, enums, exceptions, utils, unified response
├── sky-pojo                 # Data objects: 11 entities, 21 DTOs, 17 VOs
└── sky-server               # Service module: Spring Boot application
    ├── config               # WebMvc config, Knife4j, message converters
    ├── controller           # Controllers (currently employee only)
    ├── handler              # Global exception handling
    ├── interceptor          # JWT interceptor
    ├── mapper               # MyBatis mappers (currently employee only)
    └── service              # Business logic
```

## Implemented Features

### Employee Management (`/admin/employee`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/employee/login` | Employee login, returns JWT token |
| POST | `/admin/employee/logout` | Employee logout |
| POST | `/admin/employee` | Create new employee |
| GET | `/admin/employee/page` | Paginated employee query |

- JWT authentication on `/admin/**` (login endpoint excluded)
- Global exception handling for `BaseException` and unique constraint violations
- PageHelper pagination

### Infrastructure (ready, not yet wired)

| Component | Status |
|-----------|--------|
| Aliyun OSS utility | Implemented, no controller endpoint |
| WeChat Mini Program login properties | Configured, not integrated |
| WeChat Pay utility | Implemented, not integrated |
| Redis dependency | Declared, not configured |
| WebSocket dependency | Declared, not configured |
| Apache POI (Excel) | Declared, not used |

## Quick Start

### Prerequisites

- JDK 1.8+
- Maven 3.0+
- MySQL 8.0+

### Configuration

Edit `sky-server/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: your_password
```

### Run

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```

After startup, visit `http://localhost:8080/doc.html` for API documentation.

## Data Model

The following business entities are defined (POJO layer is complete, service layer being built incrementally):

| Entity | Description |
|--------|-------------|
| Employee | Employee |
| User | WeChat user |
| Category | Category (dish / setmeal) |
| Dish / DishFlavor | Dish / flavor options |
| Setmeal / SetmealDish | Setmeal / setmeal-dish association |
| ShoppingCart | Shopping cart |
| AddressBook | Delivery address |
| Orders / OrderDetail | Order / order line items |

## License

This project is for learning and communication purposes only.
