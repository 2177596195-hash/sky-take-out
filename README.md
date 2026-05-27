

# 外卖订单管理系统 (Sky Take-Out)

这是一个基于 Spring Boot 构建的外卖订单管理后端服务系统，包含了完整的员工管理、订单处理、菜品管理、购物车、微信支付等功能模块。

## 项目简介

本项目是一个餐饮外卖平台的 RESTful API 后端服务，采用微服务架构设计，支持多端接入（用户端、管理端）。系统集成了 JWT 认证、阿里云OSS文件存储、微信小程序登录、微信支付等第三方服务。

## 技术栈

- **框架**: Spring Boot 2.x
- **持久层**: MyBatis + MySQL
- **认证**: JWT Token
- **文件存储**: 阿里云 OSS
- **支付**: 微信支付
- **API文档**: Swagger (Knife4j)

## 模块说明

```
sky-take-out
├── sky-common          # 公共模块
│   ├── constant      # 常量定义
│   ├── context       # 上下文
│   ├── enumeration  # 枚举类型
│   ├── exception     # 自定义异常
│   ├── json          # JSON处理
│   ├── properties   # 配置属性类
│   ├── result       # 统一响应结果
│   └── utils        # 工具类
├── sky-pojo           # 数据对象
│   ├── dto           # 数据传输对象
│   ├── entity        # 实体类
│   └── vo             # 视图对象
└── sky-server        # 服务模块
    ├── config        # 配置类
    ├── controller    # 控制器
    ├── handler       # 异常处理
    ├── interceptor  # 拦截器
    ├── mapper        # 数据访问层
    └── service       # 业务逻辑
```

## 核心功能

### 员工管理
- 员工登录/退出
- 新增员工
- 密码修改

### 用户管理
- 微信小程序授权登录
- 用户信息管理

### 菜品分类管理
- 分类增删改查
- 分类启用/禁用

### 菜品管理
- 菜品增删改查
-菜品上下架
- 口味配置

### 套餐管理
- 套餐增删改查
- 套餐菜品关联
- 套餐上下架

### 购物车
- 添加购物车
- 查看购物车
- 清空购物车

### 订单管理
- 订单提交
- 订单支付
- 订单查询
- 订单取消/拒单
- 订单配送状态更新

### 地址簿管理
- 收货地址管理
- 默认地址设置

### 数据统计
- 营业额概览
- 订单完成率
- 销售排行
- 用户统计

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.0+
- MySQL 5.7+

### 配置说明

在 `application.yml` 中配置以下内容：

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

### 运行项目

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080/doc.html` 查看API文档。

## 接口示例

### 员工登录

```http
POST /admin/employee/login
Content-Type: application/json

{
    "username": "admin",
    "password": "123456"
}
```

### 获取菜品列表

```http
GET /admin/dish/page?page=1&pageSize=10
Authorization: Bearer {{token}}
```

## 项目结构亮点

- **统一响应**: 所有接口返回统一的 `Result` 对象
- **自动填充**: 创建时间、更新时间、创建人等字段自动填充
- **异常处理**: 全局统一异常处理机制
- **JWT鉴权**: 管理员和用户使用不同的 JWT 密钥
- **DTO转换**: DO/VO/DTO 分离，职责明确

## 许可证

本项目仅供学习交流使用。