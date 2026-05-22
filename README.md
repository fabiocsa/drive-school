# 驾校报名与学员管理系统

基于 B/S 架构的驾校综合管理平台，覆盖学员报名、材料生成、教练分配、学时管理、考试管理和统计分析全流程。

## 技术栈

| 层次     | 技术                                          |
| -------- | --------------------------------------------- |
| 后端框架 | Spring Boot 2.7 + Spring Security + JWT        |
| ORM      | MyBatis-Plus 3.5                              |
| 数据库   | MySQL 8.0                                     |
| PDF 生成 | iText 7                                       |
| 前端框架 | Vue 3 + Vue Router + Pinia + Element Plus      |
| 图表     | ECharts 5                                     |
| 构建工具 | Maven / Vite                                  |

## 环境要求

- **JDK 17**（必须 ≥ 17）
- **MySQL 8.0+**
- **Node.js 16+**
- **Maven 3.6+**

> ⚠️ 本项目中 `jjwt 0.9.1` 依赖 `javax.xml.bind`，JDK 11+ 已移除该模块，`pom.xml` 中已补充 `jaxb-api` 依赖。若使用 JDK 8/9/10 运行需自行调整。

## 快速启动

### 1. 创建数据库

用 MySQL 客户端连接后依次执行：

```sql
CREATE DATABASE IF NOT EXISTS drive_school DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后导入初始化脚本：

```sql
USE drive_school;
SOURCE src/main/resources/schema.sql;
SOURCE src/main/resources/data.sql;
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/drive_school?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

启动成功后会看到 `Started DriveSchoolApplication`，服务运行在 **http://localhost:8080**。

首次启动时 `DataInitializer` 会自动创建默认用户和教练数据。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 **http://localhost:3000**，已配置代理转发到后端 8080 端口。

## 默认账号

| 角色     | 用户名     | 密码     |
| -------- | ---------- | -------- |
| 管理员   | `admin`    | `123456` |
| 教练     | `coach1`   | `123456` |
| 教练     | `coach2`   | `123456` |
| 教练     | `coach3`   | `123456` |
| 学员     | `student1` | `123456` |
| 学员     | `student2` | `123456` |

注册的新账号默认为学员角色。

## 角色与权限

| 角色   | 权限                                                         |
| ------ | ------------------------------------------------------------ |
| 管理员 | 学员审核、教练管理、教练分配、基础信息维护、考试审核、成绩录入、发证、统计分析 |
| 教练   | 查看名下学员、录入学时、确认/取消约课                          |
| 学员   | 报名信息提交、查看审核状态、查看教练、约课、考试报名、成绩查询、PDF 下载 |

## 业务流程

### 报名流程
```
注册 → 填写信息 + 上传附件 → 自动初审（年龄/体检） → 管理员复审 → 通过 → 生成 PDF → 待分配教练
```

### 教练分配
```
系统自动推荐（按车型匹配、档期可用、评分前 3） → 管理员确认分配 → 绑定学员-教练
```

### 学习进度
```
教练录入学时 → 学员约课 → 练车记录 → 阶段完成（达到最低学时） → 可报名下一科目考试
```

### 考试流程
```
报名考试（满足学时前提） → 管理员审核 → 录入成绩 → 不合格可补考（≤5 次） → 四科全过 → 等待发证
```

## 项目结构

```
├── pom.xml
├── src/main/java/com/driveschool/
│   ├── DriveSchoolApplication.java      # 启动入口
│   ├── config/                          # DataInitializer / Security / CORS / MyBatis-Plus / MetaObjectHandler
│   ├── entity/                          # 11 个 JPA 实体
│   ├── mapper/                          # MyBatis-Plus Mapper
│   ├── service/                         # 业务接口 + impl
│   ├── controller/                      # REST 控制器
│   ├── security/                        # JWT TokenProvider + Filter
│   └── util/                            # Result / ExceptionHandler / PdfGenerator
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql                       # 建表
│   └── data.sql                         # 基础数据（车型/科目/考场/费用）
└── frontend/
    ├── package.json / vite.config.js
    └── src/
        ├── main.js / App.vue
        ├── api/index.js                 # Axios 封装 + 拦截器
        ├── router/index.js              # 路由 + 守卫
        ├── stores/user.js               # Pinia 用户状态
        └── views/
            ├── Login.vue / Register.vue
            ├── layouts/                 # 3 套布局
            ├── student/                 # 7 个页面
            ├── coach/                   # 3 个页面
            └── admin/                   # 7 个页面
```

## API 概览

统一返回格式：

```json
{ "code": 200, "message": "success", "data": {} }
```

### 开放接口

| 方法   | 路径                   | 说明     |
| ------ | ---------------------- | -------- |
| POST   | `/api/auth/login`      | 登录     |
| POST   | `/api/auth/register`   | 注册     |
| GET    | `/api/files/preview/*` | 文件预览 |
| GET    | `/api/files/download/*` | 文件下载 |

### 管理员接口 (ROLE_ADMIN)

| 资源              | 方法                  | 说明             |
| ----------------- | --------------------- | ---------------- |
| `/api/admin/students` | GET / PUT          | 学员列表、审核、分配教练 |
| `/api/admin/coaches`  | CRUD               | 教练管理         |
| `/api/admin/vehicle-types` | CRUD          | 车型管理         |
| `/api/admin/subjects` | CRUD               | 科目管理         |
| `/api/admin/exam-locations` | CRUD          | 考场管理         |
| `/api/admin/fee-standards` | CRUD           | 费用标准管理     |
| `/api/admin/exams` | GET / PUT             | 考试审核、录入成绩 |
| `/api/admin/waiting-cert` | GET / PUT        | 待发证列表、发证 |
| `/api/admin/statistics` | GET                | 统计分析         |

### 学员 / 教练接口

接口路径分别为 `/api/student/**`（ROLE_STUDENT）和 `/api/coach/**`（ROLE_COACH），覆盖个人信息、学时管理、约课、考试报名、PDF 下载等。

## 配置说明

| 配置项                   | 默认值   | 说明                   |
| ------------------------ | -------- | ---------------------- |
| `server.port`            | 8080     | 后端端口               |
| `jwt.secret`             | —        | JWT 签名密钥           |
| `jwt.expiration`         | 86400000 | Token 过期时间（24h）  |
| `file.upload.path`       | ./uploads/ | 附件上传目录         |
| `file.upload.allowed-extensions` | jpg,jpeg,png,pdf | 允许类型 |
| `pdf.output.path`        | ./pdfs/  | PDF 输出目录           |
| `spring.servlet.multipart.max-file-size` | 10MB | 单文件大小上限 |

## 注意事项

- **PDF 中文支持**：若下载的 PDF 中文乱码，请在 `src/main/resources/fonts/` 下放入中文字体文件（如 `simsun.ttc`），程序会自动检测并回退到内置字体
- **文件上传**限制 10MB，仅支持 `jpg / png / pdf`
- 所有密码使用 **BCrypt** 加密存储
- JWT Token 默认 **24 小时**过期
- 首次启动后**不要再次执行 `schema.sql`**（会 DROP TABLE 清空数据），如需重置数据手动执行或删库重建
