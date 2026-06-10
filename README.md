# 苍穹外卖 (Sky Take-Out)

一个完整的餐饮外卖平台后端系统，采用 Spring Boot 构建，包含管理端（PC后台）和用户端（微信小程序）两套接口。本项目为学习实战项目，从零开始逐步迭代开发。

## 技术栈

| 组件 | 版本 | 作用 |
|------|------|------|
| Spring Boot | 2.7.3 | 项目骨架，自动配置、依赖注入、Web服务 |
| MyBatis | 2.2.0 | ORM框架，SQL映射，支持动态SQL |
| MySQL | 8.0+ | 关系型数据库 |
| Druid | 1.2.1 | 数据库连接池，内置监控 |
| Redis | - | 缓存（菜品/套餐/店铺状态） |
| Spring Cache | - | 基于注解的缓存抽象层 |
| Knife4j (Swagger) | 3.0.2 | API接口文档，自动生成、在线调试 |
| PageHelper | 1.3.0 | MyBatis分页插件，自动拼接LIMIT |
| JJWT | 0.9.1 | JWT令牌的生成与解析 |
| 阿里云 OSS | - | 对象存储，用于图片上传 |
| WeChat Pay | - | 微信支付（已集成工具类） |
| Lombok | - | 简化实体类代码（@Data, @Builder等） |
| AspectJ | - | AOP切面编程，用于公共字段自动填充 |
| Apache POI | - | Excel报表导出（已引入依赖） |
| WebSocket | - | 长连接（已引入依赖） |

## 项目结构

采用**三模块分层架构**，职责清晰：

```
sky-take-out/
├── sky-common          # 公共模块
│   ├── constant        #   常量类（消息、状态码、JWT Claims键名等）
│   ├── context         #   线程上下文（ThreadLocal存储当前用户ID）
│   ├── enumeration     #   枚举类（操作类型：INSERT/UPDATE）
│   ├── exception       #   自定义业务异常（12个，覆盖各种业务场景）
│   ├── json            #   Jackson序列化配置（日期格式、未知属性忽略）
│   ├── properties      #   配置属性类（JWT、阿里云OSS、微信）
│   ├── result          #   统一响应封装（Result、PageResult）
│   └── utils           #   工具类（JWT、阿里云OSS、HTTP客户端、微信支付）
│
├── sky-pojo            # 数据对象模块
│   ├── dto             #   数据传输对象（21个，用于接收前端请求参数）
│   ├── entity          #   数据库实体类（11个，对应数据库表）
│   └── vo              #   视图对象（19个，用于返回前端响应数据）
│
└── sky-server          # 服务模块（Spring Boot主程序）
    ├── annotation      #   自定义注解（@AutoFill 公共字段自动填充）
    ├── aspect          #   AOP切面（拦截@AutoFill注解，反射调用setter）
    ├── config          #   配置类（MVC、Redis、OSS、Knife4j）
    ├── controller      #   控制器
    │   ├── admin       #     管理端接口（6个Controller，25个接口）
    │   └── user        #     用户端接口（7个Controller，15个接口）
    ├── handler         #   全局异常处理器
    ├── interceptor     #   JWT拦截器（管理端+用户端，两套独立鉴权）
    ├── mapper          #   MyBatis Mapper接口（9个）+ XML映射文件（9个）
    └── service         #   业务逻辑层（7个Service接口+实现类）
```

### 为什么分三个模块？

```
sky-common ──────────────┐
                         ├──→ sky-server（Spring Boot应用）
sky-pojo ────────────────┘

sky-common: 不依赖任何其他模块，放最基础的东西（工具、常量、异常）
sky-pojo:   依赖 sky-common，放数据对象（实体、DTO、VO）
sky-server: 依赖前两者，放业务代码（Controller、Service、Mapper）
```

这样的好处是：如果以后要加一个新的微服务模块，可以直接复用 sky-common 和 sky-pojo，不需要重复拷贝代码。

## 数据库设计

共设计了 **11 张业务表**：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `employee` | 员工表 | username, password(MD5), name, phone, sex, status |
| `user` | 微信用户表 | openid(微信唯一标识), name, phone, avatar |
| `category` | 分类表 | name, type(1菜品/2套餐), sort, status |
| `dish` | 菜品表 | name, category_id, price, image, description, status |
| `dish_flavor` | 菜品口味表 | dish_id, name(如"辣度"), value(如"微辣/中辣/特辣") |
| `setmeal` | 套餐表 | name, category_id, price, image, description, status |
| `setmeal_dish` | 套餐菜品关联表 | setmeal_id, dish_id, name, price, copies(份数) |
| `shopping_cart` | 购物车表 | user_id, dish_id/setmeal_id, dish_flavor, number, amount |
| `address_book` | 地址簿表 | user_id, consignee, phone, detail, is_default |
| `orders` | 订单表 | number, user_id, address_book_id, status(1-6), amount, pay_method |
| `order_detail` | 订单明细表 | order_id, dish_id/setmeal_id, number, amount |

### 表之间的关系

```
员工(employee)                    用户(user)
                                    │
分类(category)                      │
  ├── 菜品(dish)                    │
  │     └── 口味(dish_flavor)       │
  └── 套餐(setmeal)                 │
        └── 套餐菜品(setmeal_dish)   │
                                    │
                              购物车(shopping_cart)
                              地址簿(address_book)
                                    │
                              订单(orders)
                                └── 订单明细(order_detail)
```

## 已实现功能

### 管理端接口（6个Controller，25个接口）

管理端供店铺管理员在PC端使用，通过 JWT 鉴权保护。

#### 员工管理 (`/admin/employee`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/employee/login` | 员工登录，验证账号密码，返回JWT Token |
| POST | `/admin/employee/logout` | 员工登出 |
| POST | `/admin/employee` | 新增员工（默认密码123456） |
| GET | `/admin/employee/page` | 分页查询员工（支持按姓名模糊搜索） |
| POST | `/admin/employee/status/{status}` | 启用/禁用员工账号 |
| GET | `/admin/employee/{id}` | 根据ID查询员工信息 |
| PUT | `/admin/employee` | 修改员工信息 |

#### 分类管理 (`/admin/category`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/category` | 新增分类（菜品分类或套餐分类） |
| GET | `/admin/category/page` | 分页查询分类 |
| DELETE | `/admin/category` | 根据ID删除分类 |
| PUT | `/admin/category` | 修改分类信息 |
| POST | `/admin/category/status/{status}` | 启用/停用分类 |
| GET | `/admin/category/list` | 按类型查询分类列表（用于下拉选择） |

#### 菜品管理 (`/admin/dish`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/dish` | 新增菜品（含口味数据，事务处理） |
| GET | `/admin/dish/page` | 分页查询菜品（关联分类名称） |
| DELETE | `/admin/dish` | 批量删除菜品（校验：起售中不可删、关联套餐不可删） |
| GET | `/admin/dish/{id}` | 根据ID查询菜品详情（含口味列表） |
| PUT | `/admin/dish` | 修改菜品（先删旧口味再插新口味） |
| POST | `/admin/dish/status/{status}` | 菜品起售/停售（停售时级联停售关联套餐） |
| GET | `/admin/dish/list` | 按分类ID查询菜品列表 |

#### 套餐管理 (`/admin/setmeal`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/setmeal` | 新增套餐（含关联菜品，事务处理） |
| GET | `/admin/setmeal/page` | 分页查询套餐（关联分类名称） |
| DELETE | `/admin/setmeal` | 批量删除套餐（校验：起售中不可删） |
| GET | `/admin/setmeal/{id}` | 根据ID查询套餐详情（含关联菜品列表） |
| PUT | `/admin/setmeal` | 修改套餐 |
| POST | `/admin/setmeal/status/{status}` | 套餐起售/停售（校验：含停售菜品的套餐不可起售） |

#### 文件上传 (`/admin/common`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/common/upload` | 上传文件到阿里云OSS，返回图片URL |

#### 店铺管理 (`/admin/shop`)

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/admin/shop/{status}` | 设置店铺营业状态（Redis存储） |
| GET | `/admin/shop/status` | 查询店铺当前营业状态 |

### 用户端接口（7个Controller，15个接口）

用户端供微信小程序使用，通过 JWT 鉴权保护（与管理端使用不同的密钥和Token名称）。

#### 微信登录 (`/user/user`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/user/login` | 微信登录：接收code，调用微信接口获取openid，自动注册新用户 |

#### 分类浏览 (`/user/category`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/category/list` | 查询分类列表（用于小程序顶部Tab） |

#### 菜品浏览 (`/user/dish`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/dish/list` | 按分类查询菜品列表（含口味，使用Redis缓存） |

#### 套餐浏览 (`/user/setmeal`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/setmeal/list` | 按分类查询套餐列表（使用Spring Cache缓存） |
| GET | `/user/setmeal/dish/{id}` | 查询套餐内包含的菜品列表 |

#### 购物车 (`/user/shoppingCart`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/shoppingCart/add` | 添加商品到购物车（重复添加则数量+1） |
| GET | `/user/shoppingCart/list` | 查看购物车列表 |
| DELETE | `/user/shoppingCart/clean` | 清空购物车 |
| POST | `/user/shoppingCart/sub` | 减少商品数量（数量为0时自动删除） |

#### 地址簿 (`/user/addressBook`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/addressBook/list` | 查询当前用户所有地址 |
| POST | `/user/addressBook` | 新增地址 |
| GET | `/user/addressBook/{id}` | 根据ID查询地址 |
| PUT | `/user/addressBook` | 修改地址 |
| DELETE | `/user/addressBook` | 删除地址 |
| PUT | `/user/addressBook/default` | 设置默认地址（事务保证唯一性） |
| GET | `/user/addressBook/default` | 查询当前用户的默认地址 |

#### 店铺状态 (`/user/shop`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/shop/status` | 查询店铺营业状态（用户端只读） |

## 核心技术实现

### 1. JWT 双端鉴权

管理端和用户端使用**独立的密钥和Token名称**，互不干扰：

```yaml
# application.yml
sky:
  jwt:
    admin-secret-key: itcast        # 管理端密钥
    admin-token-name: token          # 管理端请求头名称
    user-secret-key: itheima         # 用户端密钥
    user-token-name: authorization   # 用户端请求头名称
```

拦截器注册顺序：先管理端后用户端，分别拦截 `/admin/**` 和 `/user/**`。

### 2. AOP 公共字段自动填充

每次插入或更新数据时，`createTime`、`updateTime`、`createUser`、`updateUser` 这四个字段需要手动设置，容易遗漏。通过自定义注解 + AOP切面实现自动填充：

```java
// Mapper接口方法上标注注解
@AutoFill(OperationType.INSERT)
void insert(Dish dish);

@AutoFill(OperationType.UPDATE)
void update(Dish dish);
```

AOP切面拦截这些方法，通过反射自动调用对应的setter方法填充字段，无需在Service层手动编写。

### 3. Redis 缓存策略

| 缓存内容 | Key格式 | 更新策略 |
|----------|---------|----------|
| 店铺营业状态 | `SHOP_STATUS` | 管理端设置时直接更新 |
| 菜品列表 | `dish_{categoryId}` | 管理端增删改时清除对应分类缓存 |
| 套餐列表 | Spring Cache自动管理 | 管理端增删改时清除全部套餐缓存 |

### 4. 动态SQL（MyBatis）

大量使用MyBatis动态SQL来实现灵活的条件查询：

- `<where>` + `<if>`：根据参数是否为null动态拼接WHERE条件
- `<set>` + `<if>`：UPDATE时只更新非null字段（局部更新）
- `<foreach>`：批量删除、批量插入（如菜品口味、套餐菜品）
- `useGeneratedKeys="true"`：插入后自动回填自增ID

### 5. 统一响应格式

所有接口返回统一的JSON格式：

```json
{
    "code": 1,
    "msg": "success",
    "data": { ... }
}
```

分页查询额外返回：

```json
{
    "code": 1,
    "data": {
        "total": 100,
        "records": [ ... ]
    }
}
```

### 6. 微信小程序登录流程

```
小程序端                         后端                           微信服务器
   │                              │                              │
   │  1. uni.login() 获取code     │                              │
   │ ────────────────────────────→│                              │
   │                              │  2. code + appid + secret    │
   │                              │ ────────────────────────────→│
   │                              │  3. 返回 openid              │
   │                              │ ←────────────────────────────│
   │                              │                              │
   │                              │  4. 查询openid是否已注册      │
   │                              │     是→查询用户信息            │
   │                              │     否→自动注册新用户          │
   │                              │                              │
   │                              │  5. 生成JWT Token             │
   │  6. 返回token + 用户信息      │                              │
   │ ←────────────────────────────│                              │
```

## 快速开始

### 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| Maven | 3.0+ |
| MySQL | 8.0+ |
| Redis | 任意版本 |

### 1. 创建数据库

```sql
CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后导入项目提供的 SQL 脚本（建表 + 初始数据）。

### 2. 修改配置

编辑 `sky-server/src/main/resources/application-dev.yml`：

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: 你的MySQL密码      # ← 改这里

  redis:
    host: localhost
    port: 6379
    password:                    # 如果Redis有密码填这里
    database: 10

  wechat:
    appid: 你的小程序appid       # ← 改这里
    secret: 你的小程序secret     # ← 改这里
```

### 3. 启动 Redis

确保 Redis 服务正在运行。Windows 下可以直接运行 `redis-server.exe`。

### 4. 编译运行

```bash
# 在项目根目录执行
mvn clean install

# 启动服务
cd sky-server
mvn spring-boot:run
```

### 5. 访问接口文档

启动成功后，浏览器访问：

```
http://localhost:8080/doc.html
```

Knife4j 提供了两个分组：
- **管理端接口**：包含所有 `/admin/**` 接口
- **用户端接口**：包含所有 `/user/**` 接口

可以在页面上直接调试接口，非常方便。

### 6. 微信小程序联调

小程序源码在项目的 `day06/微信小程序代码/mp-weixin/mp-weixin/` 目录下。用微信开发者工具打开后，需要：

1. 确保后端服务已启动（localhost:8080）
2. 在开发者工具中勾选「不校验合法域名」（因为后端是HTTP）
3. 确保 Redis 中 `SHOP_STATUS` 值为 `1`（店铺营业），否则小程序显示"休息中"

## 配置说明

### 环境隔离

项目使用 Spring Profile 实现配置隔离：

```
application.yml          # 公共配置（端口、MyBatis、JWT等）
application-dev.yml      # 开发环境配置（数据库、Redis、OSS等）
```

通过 `spring.profiles.active: dev` 激活开发环境配置。部署生产环境时，创建 `application-prod.yml` 并修改为生产环境的配置即可。

### 密钥安全提醒

`application-dev.yml` 中包含了数据库密码、OSS密钥、微信密钥等敏感信息。正式项目中应该：
- 不要把密钥文件提交到 Git 仓库
- 使用环境变量或配置中心管理密钥
- 本项目是学习项目，为了方便学习直接写在配置文件中

## 项目特点

1. **标准分层架构**：Controller → Service → Mapper，职责清晰
2. **统一规范**：统一响应格式、统一异常处理、统一参数校验
3. **注解驱动**：`@AutoFill` 自动填充、`@Transactional` 事务管理、`@Cacheable` 缓存
4. **前后端分离**：后端只提供RESTful API，前端通过HTTP请求调用
5. **双端鉴权**：管理端和用户端独立的JWT认证，安全隔离
6. **Redis缓存**：热门数据缓存，减少数据库查询压力
7. **动态SQL**：MyBatis动态SQL实现灵活的条件查询和局部更新
8. **AOP编程**：切面自动处理公共字段填充，减少重复代码

## 开发进度

| 模块 | 状态 | 说明 |
|------|------|------|
| 员工管理 | 已完成 | 登录登出、CRUD、启禁用 |
| 分类管理 | 已完成 | CRUD、启停用、按类型查询 |
| 菜品管理 | 已完成 | CRUD、批量删除、起售停售、口味管理 |
| 套餐管理 | 已完成 | CRUD、批量删除、起售停售、关联菜品 |
| 文件上传 | 已完成 | 阿里云OSS图片上传 |
| 店铺管理 | 已完成 | Redis存储营业状态 |
| 微信登录 | 已完成 | code2session + 自动注册 + JWT |
| 菜品浏览 | 已完成 | 用户端分类菜品查询 + Redis缓存 |
| 套餐浏览 | 已完成 | 用户端分类套餐查询 + Spring Cache |
| 购物车 | 已完成 | 添加、查看、减少、清空 |
| 地址簿 | 已完成 | CRUD、设置默认地址 |
| 订单管理 | 待开发 | 提交订单、支付、订单状态流转 |
| 数据统计 | 待开发 | 营业额、订单量、销量排行等报表 |
| 来单提醒 | 待开发 | WebSocket实时推送新订单 |

## 许可证

本项目仅供学习交流使用。
