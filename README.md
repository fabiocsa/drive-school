# 驾校报名与学员管理系统

## 项目概述

基于 B/S 架构的驾校报名系统，支持学员在线报名、材料自动生成、教练分配、学习进度管理、考试管理、基础信息维护和统计分析。

**技术栈**: Spring Boot + MyBatis-Plus + Spring Security + JWT + Vue 3 + Element Plus + ECharts + MySQL

## 环境要求

- JDK 11+
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+

## 快速启动

### 1. 数据库配置

```sql
-- 执行 schema.sql 和 data.sql 初始化数据库
-- 数据库连接信息在 application.yml 中配置
-- 默认数据库名: drive_school
-- 默认用户名/密码: root/root
```

修改 `src/main/resources/application.yml` 中的数据库连接信息。

### 2. 后端启动

```bash
mvn clean package -DskipTests
mvn spring-boot:run
# 服务运行在 http://localhost:8080
```

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

## 默认账号

| 角色   | 用户名   | 密码   |
| ------ | -------- | ------ |
| 管理员 | admin    | 123456 |
| 教练   | coach1   | 123456 |
| 教练   | coach2   | 123456 |
| 教练   | coach3   | 123456 |
| 学员   | student1 | 123456 |
| 学员   | student2 | 123456 |

## 项目结构

```
├── pom.xml                          # Maven 配置
├── src/main/java/com/driveschool/
│   ├── DriveSchoolApplication.java  # 启动类
│   ├── config/                      # 配置类 (Security, CORS, MyBatis-Plus, FileUpload)
│   ├── entity/                      # 实体类 (User, StudentInfo, Coach, VehicleType...)
│   ├── mapper/                      # MyBatis Mapper 接口
│   ├── service/                     # 服务接口与实现
│   ├── controller/                  # REST API 控制器
│   ├── security/                    # JWT 认证 (TokenProvider, Filter)
│   └── util/                        # 工具类 (Result, GlobalExceptionHandler, PdfGenerator)
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── schema.sql                   # 数据库建表脚本
│   └── data.sql                     # 初始数据
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                  # Vue 入口
│       ├── App.vue                  # 根组件
│       ├── api/index.js             # API 封装
│       ├── router/index.js          # 路由配置
│       ├── stores/user.js           # Pinia 状态管理
│       └── views/                   # 页面组件
│           ├── Login.vue / Register.vue
│           ├── layouts/             # 布局组件
│           ├── student/             # 学员端页面
│           ├── coach/               # 教练端页面
│           └── admin/               # 管理员端页面
```

## 功能模块

### 学员端
- 在线注册/登录
- 报名信息填写与附件上传
- 查看审核状态与教练信息
- 学时记录查看
- 约课/取消约课
- 考试报名与成绩查询
- PDF（报名表、体检表、准考证）下载打印

### 教练端
- 查看名下学员
- 录入学时记录
- 确认/取消约课

### 管理员端
- 学员审核（体检审核、报名审核）
- 教练推荐与分配
- 基础信息管理（车型、科目、考场、费用标准）
- 考试审核与成绩录入
- 发证管理
- 统计分析图表（报名人数折线图、通过率柱状图、教练工作量图）

## 业务流程

1. **报名**: 注册 → 填写信息 → 上传附件 → 自动初审 → 管理员复审 → 通过 → 生成PDF → 待分配
2. **分配**: 系统推荐 → 管理员确认 → 学员-教练绑定
3. **学习**: 教练录学时 → 学员约课 → 练车记录 → 阶段完成
4. **考试**: 报名（满足学时）→ 管理员审核 → 录入成绩 → 不合格补考 → 全部合格 → 发证
5. **统计**: 按条件生成图表

## API 接口

所有接口遵循 RESTful 风格，统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 认证接口
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册

### 学员接口 (需 ROLE_STUDENT)
- `POST /api/student/register` - 提交报名
- `POST /api/student/upload` - 上传文件
- `GET /api/student/myinfo` - 获取个人信息
- `GET/POST /api/student/trainings` - 学时记录
- `GET/POST/PUT /api/student/appointments` - 约课管理
- `POST/GET /api/student/exams` - 考试管理
- `GET /api/student/pdf/**` - PDF下载

### 教练接口 (需 ROLE_COACH)
- `GET /api/coach/myinfo` - 个人信息
- `POST /api/coach/training/record` - 录入学时
- `GET/PUT /api/coach/appointments` - 约课管理

### 管理员接口 (需 ROLE_ADMIN)
- `GET/PUT /api/admin/students/**` - 学员管理
- `GET/POST/PUT/DELETE /api/admin/coaches/**` - 教练管理
- `GET/POST/PUT/DELETE /api/admin/vehicle-types/**` - 车型管理
- `GET/POST/PUT/DELETE /api/admin/subjects/**` - 科目管理
- `GET/POST/PUT/DELETE /api/admin/exam-locations/**` - 考场管理
- `GET/POST/PUT/DELETE /api/admin/fee-standards/**` - 费用标准
- `GET/PUT /api/admin/exams/**` - 考试管理
- `GET/PUT /api/admin/waiting-cert/**` - 发证管理
- `GET /api/admin/statistics/**` - 统计分析

## 注意事项

- PDF 生成使用 iText 7，需在 `src/main/resources/fonts/` 目录放置中文字体（如 simsun.ttc）
- 文件上传限制 10MB，支持 jpg/png/pdf 格式
- 密码使用 BCrypt 加密
- JWT 默认过期时间 24 小时
