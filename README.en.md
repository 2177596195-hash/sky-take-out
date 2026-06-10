# Sky Take-Out

A complete food delivery platform backend built with Spring Boot, providing two sets of RESTful APIs: admin-side (PC management) and user-side (WeChat Mini Program). This is a learning project developed incrementally from scratch.

## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 2.7.3 | Application framework, DI, embedded web server |
| MyBatis | 2.2.0 | ORM framework with dynamic SQL support |
| MySQL | 8.0+ | Relational database |
| Druid | 1.2.1 | Database connection pool with built-in monitoring |
| Redis | - | Caching (dishes, setmeals, shop status) |
| Spring Cache | - | Annotation-based cache abstraction |
| Knife4j (Swagger) | 3.0.2 | Auto-generated API documentation with live testing |
| PageHelper | 1.3.0 | MyBatis pagination plugin |
| JJWT | 0.9.1 | JWT token generation and parsing |
| Aliyun OSS | - | Object storage for image uploads |
| WeChat Pay | - | WeChat payment integration (utility class ready) |
| Lombok | - | Boilerplate code reduction (@Data, @Builder, etc.) |
| AspectJ | - | AOP for auto-filling audit fields |
| Apache POI | - | Excel report export (dependency declared) |
| WebSocket | - | Real-time communication (dependency declared) |

## Project Structure

A **three-module layered architecture** with clear separation of concerns:

```
sky-take-out/
├── sky-common          # Shared module
│   ├── constant        #   Constants (messages, status codes, JWT claim keys)
│   ├── context         #   ThreadLocal context (stores current user/employee ID)
│   ├── enumeration     #   Enums (OperationType: INSERT/UPDATE)
│   ├── exception       #   Custom business exceptions (12 classes)
│   ├── json            #   Jackson configuration (date formats, unknown property handling)
│   ├── properties      #   Configuration property classes (JWT, Aliyun OSS, WeChat)
│   ├── result          #   Unified API response (Result, PageResult)
│   └── utils           #   Utilities (JWT, Aliyun OSS, HTTP client, WeChat Pay)
│
├── sky-pojo            # Data objects module
│   ├── dto             #   Data Transfer Objects (21 classes, for request parameters)
│   ├── entity          #   Database entities (11 classes, mapping to tables)
│   └── vo              #   View Objects (19 classes, for API responses)
│
└── sky-server          # Service module (Spring Boot application)
    ├── annotation      #   Custom annotations (@AutoFill for audit field auto-fill)
    ├── aspect          #   AOP aspects (intercepts @AutoFill, uses reflection)
    ├── config          #   Configuration (MVC, Redis, OSS, Knife4j)
    ├── controller      #   Controllers
    │   ├── admin       #     Admin-side (6 controllers, 25 endpoints)
    │   └── user        #     User-side (7 controllers, 15 endpoints)
    ├── handler         #   Global exception handler
    ├── interceptor     #   JWT interceptors (separate for admin and user)
    ├── mapper          #   MyBatis Mapper interfaces (9) + XML mappings (9)
    └── service         #   Business logic layer (7 service interfaces + implementations)
```

### Why Three Modules?

```
sky-common ──────────────┐
                         ├──→ sky-server (Spring Boot app)
sky-pojo ────────────────┘

sky-common: No dependencies. Contains the most fundamental code (utilities, constants, exceptions).
sky-pojo:   Depends on sky-common. Contains data objects (entities, DTOs, VOs).
sky-server: Depends on both. Contains all business code (controllers, services, mappers).
```

This modular design allows code reuse. If you add a new microservice module in the future, you can directly depend on sky-common and sky-pojo without duplicating code.

## Database Design

**11 business tables** in total:

| Table | Description | Key Fields |
|-------|-------------|------------|
| `employee` | Employee accounts | username, password (MD5), name, phone, sex, status |
| `user` | WeChat users | openid (WeChat unique ID), name, phone, avatar |
| `category` | Categories | name, type (1=dish, 2=setmeal), sort, status |
| `dish` | Dishes | name, category_id, price, image, description, status |
| `dish_flavor` | Dish flavor options | dish_id, name (e.g. "spiciness"), value (e.g. "mild/medium/hot") |
| `setmeal` | Set meals (combos) | name, category_id, price, image, description, status |
| `setmeal_dish` | Setmeal-dish mapping | setmeal_id, dish_id, name, price, copies |
| `shopping_cart` | Shopping cart | user_id, dish_id/setmeal_id, dish_flavor, number, amount |
| `address_book` | Delivery addresses | user_id, consignee, phone, detail, is_default |
| `orders` | Orders | number, user_id, address_book_id, status (1-6), amount, pay_method |
| `order_detail` | Order line items | order_id, dish_id/setmeal_id, number, amount |

### Table Relationships

```
Employee                          User
                                    │
Category                            │
  ├── Dish                          │
  │     └── DishFlavor              │
  └── Setmeal                       │
        └── SetmealDish             │
                                    │
                              ShoppingCart
                              AddressBook
                                    │
                                Orders
                                  └── OrderDetail
```

## Implemented Features

### Admin-Side APIs (6 Controllers, 25 Endpoints)

For shop administrators on PC, protected by JWT authentication.

#### Employee Management (`/admin/employee`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/employee/login` | Login with username/password, returns JWT token |
| POST | `/admin/employee/logout` | Logout |
| POST | `/admin/employee` | Create new employee (default password: 123456) |
| GET | `/admin/employee/page` | Paginated query with name search |
| POST | `/admin/employee/status/{status}` | Enable/disable employee account |
| GET | `/admin/employee/{id}` | Get employee by ID |
| PUT | `/admin/employee` | Update employee info |

#### Category Management (`/admin/category`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/category` | Create category (dish or setmeal type) |
| GET | `/admin/category/page` | Paginated category query |
| DELETE | `/admin/category` | Delete category by ID |
| PUT | `/admin/category` | Update category |
| POST | `/admin/category/status/{status}` | Enable/disable category |
| GET | `/admin/category/list` | List categories by type |

#### Dish Management (`/admin/dish`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/dish` | Create dish with flavors (transactional) |
| GET | `/admin/dish/page` | Paginated query (joined with category name) |
| DELETE | `/admin/dish` | Batch delete (validates: not on sale, not in setmeals) |
| GET | `/admin/dish/{id}` | Get dish detail with flavors |
| PUT | `/admin/dish` | Update dish (deletes old flavors, inserts new ones) |
| POST | `/admin/dish/status/{status}` | Start/stop selling (cascades to related setmeals) |
| GET | `/admin/dish/list` | List dishes by category ID |

#### Setmeal Management (`/admin/setmeal`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/setmeal` | Create setmeal with dishes (transactional) |
| GET | `/admin/setmeal/page` | Paginated query (joined with category name) |
| DELETE | `/admin/setmeal` | Batch delete (validates: not on sale) |
| GET | `/admin/setmeal/{id}` | Get setmeal detail with dish list |
| PUT | `/admin/setmeal` | Update setmeal |
| POST | `/admin/setmeal/status/{status}` | Start/stop selling (validates: all dishes must be enabled) |

#### File Upload (`/admin/common`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/common/upload` | Upload file to Aliyun OSS, returns URL |

#### Shop Management (`/admin/shop`)

| Method | Path | Description |
|--------|------|-------------|
| PUT | `/admin/shop/{status}` | Set shop status (stored in Redis) |
| GET | `/admin/shop/status` | Get current shop status |

### User-Side APIs (7 Controllers, 15 Endpoints)

For WeChat Mini Program users, protected by JWT with separate secret keys.

#### WeChat Login (`/user/user`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/user/user/login` | WeChat login: receives code, gets openid, auto-registers new users |

#### Category Browsing (`/user/category`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/category/list` | List categories (for mini program tab bar) |

#### Dish Browsing (`/user/dish`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/dish/list` | List dishes by category (with flavors, Redis cached) |

#### Setmeal Browsing (`/user/setmeal`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/setmeal/list` | List setmeals by category (Spring Cache) |
| GET | `/user/setmeal/dish/{id}` | Get dishes in a setmeal |

#### Shopping Cart (`/user/shoppingCart`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/user/shoppingCart/add` | Add item to cart (increments quantity if already exists) |
| GET | `/user/shoppingCart/list` | List cart items |
| DELETE | `/user/shoppingCart/clean` | Clear entire cart |
| POST | `/user/shoppingCart/sub` | Decrease item quantity (auto-removes when reaching 0) |

#### Address Book (`/user/addressBook`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/addressBook/list` | List all addresses for current user |
| POST | `/user/addressBook` | Create new address |
| GET | `/user/addressBook/{id}` | Get address by ID |
| PUT | `/user/addressBook` | Update address |
| DELETE | `/user/addressBook` | Delete address |
| PUT | `/user/addressBook/default` | Set default address (transactional, ensures uniqueness) |
| GET | `/user/addressBook/default` | Get default address |

#### Shop Status (`/user/shop`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/shop/status` | Get shop status (read-only for users) |

## Core Technical Implementations

### 1. Dual JWT Authentication

Admin and user sides use **independent secret keys and token names**:

```yaml
sky:
  jwt:
    admin-secret-key: itcast        # Admin secret
    admin-token-name: token          # Admin header name
    user-secret-key: itheima         # User secret
    user-token-name: authorization   # User header name
```

Interceptors are registered for `/admin/**` and `/user/**` respectively.

### 2. AOP Auto-Fill for Audit Fields

Four fields (`createTime`, `updateTime`, `createUser`, `updateUser`) are automatically populated via a custom annotation + AOP aspect:

```java
@AutoFill(OperationType.INSERT)
void insert(Dish dish);

@AutoFill(OperationType.UPDATE)
void update(Dish dish);
```

The aspect intercepts these methods and uses reflection to call the appropriate setters, eliminating manual field-setting in the service layer.

### 3. Redis Caching Strategy

| Cache Target | Key Pattern | Invalidation Strategy |
|--------------|-------------|----------------------|
| Shop status | `SHOP_STATUS` | Direct update from admin |
| Dish list | `dish_{categoryId}` | Evict category cache on admin changes |
| Setmeal list | Spring Cache auto-managed | Evict all setmeal cache on admin changes |

### 4. Dynamic SQL (MyBatis)

Extensively uses MyBatis dynamic SQL for flexible queries:

- `<where>` + `<if>`: Dynamic WHERE clause based on non-null parameters
- `<set>` + `<if>`: Partial UPDATE (only updates non-null fields)
- `<foreach>`: Batch delete and batch insert (flavors, setmeal dishes)
- `useGeneratedKeys="true"`: Auto-backfill generated IDs after insert

### 5. Unified Response Format

All endpoints return a consistent JSON structure:

```json
{
    "code": 1,
    "msg": "success",
    "data": { ... }
}
```

Paginated queries include additional fields:

```json
{
    "code": 1,
    "data": {
        "total": 100,
        "records": [ ... ]
    }
}
```

### 6. WeChat Mini Program Login Flow

```
Mini Program                    Backend                       WeChat Server
   │                              │                              │
   │  1. uni.login() get code     │                              │
   │ ────────────────────────────→│                              │
   │                              │  2. code + appid + secret    │
   │                              │ ────────────────────────────→│
   │                              │  3. Return openid            │
   │                              │ ←────────────────────────────│
   │                              │                              │
   │                              │  4. Check if openid exists   │
   │                              │     yes → query user          │
   │                              │     no  → auto-register       │
   │                              │                              │
   │                              │  5. Generate JWT token        │
   │  6. Return token + user info │                              │
   │ ←────────────────────────────│                              │
```

## Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| JDK | 1.8+ |
| Maven | 3.0+ |
| MySQL | 8.0+ |
| Redis | Any version |

### 1. Create Database

```sql
CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

Then import the project's SQL scripts (table creation + initial data).

### 2. Edit Configuration

Edit `sky-server/src/main/resources/application-dev.yml`:

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: your_mysql_password   # ← change this

  redis:
    host: localhost
    port: 6379
    password:                       # set if Redis requires auth
    database: 10

  wechat:
    appid: your_mini_program_appid   # ← change this
    secret: your_mini_program_secret # ← change this
```

### 3. Start Redis

Make sure Redis is running. On Windows, run `redis-server.exe` directly.

### 4. Build and Run

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```

### 5. Access API Documentation

After startup, open in browser:

```
http://localhost:8080/doc.html
```

Knife4j provides two groups:
- **Admin APIs**: All `/admin/**` endpoints
- **User APIs**: All `/user/**` endpoints

You can test APIs directly from the documentation page.

### 6. WeChat Mini Program Integration

The mini program source code is in `day06/微信小程序代码/mp-weixin/mp-weixin/`. After opening with WeChat Developer Tools:

1. Ensure the backend is running (localhost:8080)
2. Check "Do not verify valid domain names" in developer tools (backend uses HTTP)
3. Ensure `SHOP_STATUS` in Redis is set to `1` (shop open), otherwise the mini program shows "Closed"

## Project Highlights

1. **Standard Layered Architecture**: Controller → Service → Mapper with clear responsibilities
2. **Consistent Conventions**: Unified response format, exception handling, and parameter validation
3. **Annotation-Driven**: `@AutoFill` for audit fields, `@Transactional` for transactions, `@Cacheable` for caching
4. **Frontend-Backend Separation**: Backend provides RESTful APIs only, frontend calls via HTTP
5. **Dual JWT Authentication**: Admin and user sides use independent JWT credentials
6. **Redis Caching**: Hot data cached to reduce database load
7. **Dynamic SQL**: MyBatis dynamic SQL for flexible queries and partial updates
8. **AOP Programming**: Aspect-based auto-fill eliminates repetitive code

## Development Progress

| Module | Status | Description |
|--------|--------|-------------|
| Employee Management | Done | Login/logout, CRUD, enable/disable |
| Category Management | Done | CRUD, enable/disable, list by type |
| Dish Management | Done | CRUD, batch delete, start/stop selling, flavor management |
| Setmeal Management | Done | CRUD, batch delete, start/stop selling, dish association |
| File Upload | Done | Aliyun OSS image upload |
| Shop Management | Done | Redis-based shop status |
| WeChat Login | Done | code2session + auto-registration + JWT |
| Dish Browsing | Done | User-side category dish query + Redis cache |
| Setmeal Browsing | Done | User-side category setmeal query + Spring Cache |
| Shopping Cart | Done | Add, view, decrease, clear |
| Address Book | Done | CRUD, set default address |
| Order Management | TODO | Submit order, payment, order status workflow |
| Data Statistics | TODO | Turnover, order count, sales ranking reports |
| New Order Alert | TODO | WebSocket real-time push for new orders |

## License

This project is for learning and communication purposes only.
