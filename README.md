# 外卖订单管理系统 (Sky Take-Out)

基于 Spring Boot 构建的餐饮外卖平台后端服务，当前处于开发阶段。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 2.7.3 |
| MyBatis | 2.2.0 |
| MySQL | 8.0+ |
| Druid | 1.2.1 |
| Knife4j (Swagger) | 3.0.2 |
| PageHelper | 1.3.0 |
| JJWT | 0.9.1 |

## 项目结构

```
sky-take-out
├── sky-common               # 公共模块：常量、枚举、异常、工具类、统一响应
├── sky-pojo                 # 数据对象：11 个实体、21 个 DTO、17 个 VO
└── sky-server               # 服务模块：Spring Boot 应用
    ├── config               # WebMvc 配置、Knife4j、消息转换器
    ├── controller           # 控制器（目前仅 employee）
    ├── handler              # 全局异常处理
    ├── interceptor          # JWT 拦截器
    ├── mapper               # MyBatis Mapper（目前仅 employee）
    └── service              # 业务逻辑
```

## 当前已实现

### 员工管理 (`/admin/employee`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/employee/login` | 员工登录，返回 JWT Token |
| POST | `/admin/employee/logout` | 员工登出 |
| POST | `/admin/employee` | 新增员工 |
| GET | `/admin/employee/page` | 分页查询员工 |

- JWT 鉴权：`/admin/**` 路径自动拦截（登录接口除外）
- 全局异常处理：`BaseException` 及唯一约束冲突
- PageHelper 分页

### 基础设施（已就绪，待接入）

| 组件 | 状态 |
|------|------|
| 阿里云 OSS 工具类 | 已实现，未接入 Controller |
| 微信小程序登录属性 | 已配置，未接入 |
| 微信支付工具类 | 已实现，未接入 |
| Redis 依赖 | 已引入，未配置 |
| WebSocket 依赖 | 已引入，未配置 |
| Apache POI (Excel) | 已引入，未使用 |

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.0+
- MySQL 8.0+

### 配置

修改 `sky-server/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: your_password
```

### 运行

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```

启动后访问 `http://localhost:8080/doc.html` 查看 API 文档。

## 数据模型

系统设计了以下业务实体（POJO 层已完整，服务层逐步实现中）：

| 实体 | 说明 |
|------|------|
| Employee | 员工 |
| User | 微信用户 |
| Category | 分类（菜品/套餐） |
| Dish / DishFlavor | 菜品 / 口味 |
| Setmeal / SetmealDish | 套餐 / 套餐菜品 |
| ShoppingCart | 购物车 |
| AddressBook | 地址簿 |
| Orders / OrderDetail | 订单 / 订单明细 |

## 许可证

本项目仅供学习交流使用。
