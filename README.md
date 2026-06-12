# 驾校报名与学员管理系统

基于 B/S 架构的驾校综合管理平台，覆盖学员报名、材料生成、教练分配、学时管理、考试管理和统计分析全流程。

## 技术栈

| 层次 | 技术 | 版本 |
| --- | --- | --- |
| 后端框架 | Spring Boot + Spring Security + JWT | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| PDF 生成 | iText 7（kernel + layout + font-asian） | 7.2.5 |
| 前端框架 | Vue 3 + Vue Router + Pinia + Element Plus | 3.4 / 4.3 / 2.1 / 2.5 |
| 图表 | ECharts | 5.5 |
| 构建工具 | Maven / Vite | 3.6+ / 5.x |

## 环境要求

| 工具 | 最低版本 | 说明 |
| --- | --- | --- |
| JDK | **17** | 必须 ≥ 17，项目 `java.version` 设为 17 |
| MySQL | 8.0 | 使用 `caching_sha2_password` 认证 |
| Node.js | 16+ | 前端运行环境 |
| Maven | 3.6+ | 后端构建 |

> ⚠️ `jjwt 0.9.1` 依赖 `javax.xml.bind`（Java 11 起已移除），`pom.xml` 中已显式添加 `jaxb-api:2.3.1`。同时 MySQL 8.0 默认使用 `caching_sha2_password`，JDBC URL 中必须携带 `allowPublicKeyRetrieval=true`。

## 快速启动

### 1. 修改数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/drive_school?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
```

### 2. 首次启动（自动初始化数据库）

配置好数据库连接后，使用 `dev` profile 启动，Spring Boot 自动执行 SQL 脚本：
```bash
# Linux / macOS / Git Bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Windows PowerShell（需要用引号包裹 -D 参数）
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# 或使用环境变量方式（所有平台通用）
export SPRING_PROFILES_ACTIVE=dev   # Linux/macOS/Git Bash
$env:SPRING_PROFILES_ACTIVE="dev"   # Windows PowerShell
mvn spring-boot:run
```

自动完成：建库 → 建表 → 插入基础数据 → 创建默认用户。

| SQL 脚本 | 作用 | 幂等性 |
| --- | --- | --- |
| `schema.sql` | 建库 + 建 11 张表 | `CREATE TABLE IF NOT EXISTS`（重复启动不丢数据） |
| `data.sql` | 插入车型/科目/考场/费用标准 | `INSERT IGNORE`（可重复执行） |
| `DataInitializer` | 创建 admin/coach/student 默认用户（BCrypt） | 存在则跳过 |

> 生产环境请勿使用 `dev` profile。默认 `sql.init.mode=never`，不会执行 SQL 脚本。

### 3. 日常启动

数据库已就绪后，使用默认 profile：

```bash
cd 项目根目录
mvn spring-boot:run
```

看到 `Started DriveSchoolApplication in X seconds` 即启动成功，端口 **8080**。启动时 `DataInitializer` 仍会检查并补建缺失的默认用户。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

看到 `Local: http://localhost:3000/` 即就绪。Vite 已配置代理，所有 `/api` 和 `/uploads` 请求自动转发到后端 8080。

## 默认账号

所有账号密码统一为 **`123456`**，密码通过 BCrypt 动态编码，由 `DataInitializer` 启动时初始化。

| 角色 | 用户名 | 说明 |
| --- | --- | --- |
| 管理员 | `admin` | 全部功能权限 |
| 教练 | `coach1` | 执教 C1，评分 5，档期充足 |
| 教练 | `coach2` | 执教 C1，评分 4 |
| 教练 | `coach3` | 执教 C2，评分 4 |
| 学员 | `student1` | 已注册，未报名 |
| 学员 | `student2` | 已注册，未报名 |

通过前端注册页创建的新账号默认角色为 `ROLE_STUDENT`。

## 角色与权限矩阵

| 功能 | 管理员 | 教练 | 学员 | 游客 |
| --- | :---: | :---: | :---: | :---: |
| 登录/注册 | ✓ | ✓ | ✓ | ✓ |
| 提交报名信息 | | | ✓ | |
| 上传身份证/体检表 | | | ✓ | |
| 查看审核状态与教练 | | | ✓ | |
| 查看学时记录 | | ✓ | ✓ | |
| 约课/取消约课 | | ✓ | ✓ | |
| 录入学时 | | ✓ | | |
| 确认约课 | | ✓ | | |
| 考试报名 | | | ✓ | |
| 考试审核与成绩录入 | ✓ | | | |
| 学员审核与教练分配 | ✓ | | | |
| 批量审核 | ✓ | | | |
| 审核日志查看 | ✓ | | | |
| 教练 CRUD | ✓ | | | |
| 基础信息管理 | ✓ | | | |
| 发证管理 | ✓ | | | |
| 统计分析 | ✓ | | | |
| 查询车型/科目/考场(公共) | ✓ | ✓ | ✓ | |

## 业务流程

### 报名流程
```
注册 → 填写信息+上传附件 → 自动初审(年龄/车型)
  ├─ 不通过 → REJECTED（写明原因，流程结束）
  └─ 通过 → 保持 PENDING（等待管理员）
       ├─ 管理员标记体检合格 + 审核通过 → APPROVED
       │    → 创建学习阶段 → 生成 PDF(报名表/体检表/准考证) → 待分配教练
       └─ 管理员驳回 → REJECTED → 填写驳回原因
```

### 教练分配
```
系统推荐(按车型匹配、档期可用、评分降序取前 3) → 管理员确认或手动选择 → 绑定
```

### 学习进度
```
教练录入学时 → 系统累加并检查各阶段是否达标 → 学员约课 → 练车记录
  → 阶段完成 → 可报名下一科目考试
```

### 考试流程
```
报名(需前一科通过且本科学时达标) → 管理员审核 → 录入成绩
  → 不合格可补考(≤5 次) → 四科全过 → 待发证 → 管理员发证
```

## 项目结构

```
├── pom.xml                                  # Maven 配置 (JDK 17, Spring Boot 2.7.18)
├── src/main/java/com/driveschool/
│   ├── DriveSchoolApplication.java          # 启动入口
│   ├── config/
│   │   ├── CorsConfig.java                  # CORS + 文件路径配置 + ResourceHandler
│   │   ├── DataInitializer.java             # 启动时初始化账号和教练
│   │   ├── MyBatisPlusConfig.java           # 分页插件
│   │   ├── MyMetaObjectHandler.java         # 自动填充 createdTime
│   │   └── SecurityConfig.java              # Spring Security + JWT 过滤器链
│   ├── entity/                              # 11 个实体 (User/StudentInfo/Coach/...)
│   ├── mapper/                              # MyBatis-Plus Mapper 接口
│   ├── service/                             # 业务接口 + impl (7 组)
│   ├── controller/
│   │   ├── AuthController.java              # 登录/注册 (开放)
│   │   ├── CommonController.java            # 公共数据 (车型/科目/考场/教练学员列表)
│   │   ├── StudentController.java           # 学员端 (报名/学时/约课/考试/PDF)
│   │   ├── CoachController.java             # 教练端 (录学时/确认约课)
│   │   ├── AdminController.java             # 管理员端 (全量 CRUD + 统计)
│   │   └── FileController.java              # 文件上传/下载/预览
│   ├── security/                            # JwtTokenProvider + JwtAuthenticationFilter + SecurityUtils
│   └── util/                                # Result / GlobalExceptionHandler / PdfGenerator
├── src/main/resources/
│   ├── application.yml                      # 默认配置 (sql.init.mode=never)
│   ├── application-dev.yml                  # 开发环境 (sql.init.mode=always + continue-on-error)
│   ├── schema.sql                           # 建表 11 张 (CREATE TABLE IF NOT EXISTS，幂等)
│   └── data.sql                             # 基础数据 (INSERT IGNORE，幂等)
└── frontend/
    ├── package.json                         # Vue 3.4 / Vite 5 / Element Plus 2.5 / ECharts 5.5
    ├── vite.config.js                       # 端口 3000, 代理 8080
    └── src/
        ├── main.js / App.vue
        ├── api/index.js                     # Axios + auth/common/student/coach/admin 五组 API
        ├── router/index.js                  # 路由 + token 守卫
        ├── stores/user.js                   # Pinia 状态 (token/role/userId)
        └── views/
            ├── Login.vue / Register.vue
            ├── layouts/                     # StudentLayout / CoachLayout / AdminLayout
            ├── student/                     # Dashboard, Registration, Status, Trainings, Appointments, Exams, PdfDownload
            ├── coach/                       # Dashboard, Training, Appointments
            └── admin/                       # Dashboard, Students, Coaches, BasicInfo, Exams, CertIssue, Statistics
```

## API 概览

统一返回格式：`{ "code": 200, "message": "success", "data": {} }`

### 开放接口（无需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 JWT + 角色 |
| POST | `/api/auth/register` | 注册（默认 ROLE_STUDENT） |
| GET | `/api/files/preview/{filename}` | 文件预览 |
| GET | `/api/files/download/{filename}` | 文件下载 |

### 公共接口（任意已登录角色）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/common/vehicle-types` | 车型列表（仅启用的） |
| GET | `/api/common/subjects` | 科目列表 |
| GET | `/api/common/exam-locations` | 考场列表 |
| GET | `/api/common/coaches/students` | 教练自动查看名下学员（JWT 识别身份） |

### 管理员接口 (`ROLE_ADMIN`)

| 资源 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin/students` | GET | 学员列表（含姓名、电话、审核人、审核时间） |
| | PUT `/{id}/audit` | 审核（含体检状态，自动记录操作人+时间） |
| | PUT `/batch-audit` | 批量审核（一次审核多个学员） |
| | GET `/{id}/recommend-coaches` | 推荐教练 |
| | PUT `/{id}/assign-coach` | 分配教练 |
| `/api/admin/coaches` | CRUD | 教练管理 |
| `/api/admin/vehicle-types` | CRUD | 车型管理 |
| `/api/admin/subjects` | CRUD | 科目管理 |
| `/api/admin/exam-locations` | CRUD | 考场管理 |
| `/api/admin/fee-standards` | CRUD | 费用标准 |
| `/api/admin/exams` | GET | 考试列表 |
| | PUT `/{id}/audit` | 审核考试报名 |
| | PUT `/{id}/score` | 录入成绩 |
| `/api/admin/waiting-cert` | GET | 待发证学员 |
| `/api/admin/issue-cert/{id}` | PUT | 发证 |
| `/api/admin/statistics` | GET | 全部统计 (报名/通过率/工作量) |

### 学员 / 教练接口

- `/api/student/**` — 需 `ROLE_STUDENT`：个人信息、报名提交、文件上传、学时查询、约课/取消、考试报名/查询、PDF 下载
  - 所有接口从 JWT Token 中获取 userId，**不接受客户端传入 userId 参数**，防止越权访问
- `/api/coach/**` — 需 `ROLE_COACH`：个人信息、录入学时、约课确认/查询
  - 同样从 JWT 获取身份，学时录入和学员查询时会校验学员是否属于当前教练

## 配置说明

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 后端端口 |
| `spring.datasource.url` | — | **必须含 `allowPublicKeyRetrieval=true`** |
| `jwt.secret` | — | JWT HS512 签名密钥 |
| `jwt.expiration` | 86400000（24h） | Token 过期，毫秒 |
| `spring.servlet.multipart.max-file-size` | 10MB | 单文件上传上限 |
| `file.upload.allowed-extensions` | jpg,jpeg,png,pdf | 允许上传的文件类型 |
| `spring.sql.init.mode` | `never`（默认） | 生产环境不执行 SQL 脚本 |
| | `always`（dev profile） | 开发环境自动执行 schema.sql + data.sql |

### Profile 说明

| Profile | sql.init.mode | 适用场景 |
| --- | --- | --- |
| 默认（无 profile） | `never` | 生产环境 / 日常运行 |
| `dev` | `always` + `continue-on-error: true` | 首次部署 / 重置数据库 |

```bash
# 开发环境（自动建库建表）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境（不执行 SQL）
mvn spring-boot:run
```

## 更新日志

### v1.1.0 (2026-06-12)

**安全修复（权限控制缺陷）**

| 编号 | 问题 | 影响 | 修复 |
| --- | --- | --- | --- |
| S01 | IDOR 越权：StudentController 所有接口从客户端接受 userId/studentId 参数 | 学员可查看/操作他人数据（个人信息、学时、约课、考试、PDF） | 所有接口改为从 JWT Token 中获取 userId，不再接受客户端传入 |
| S02 | IDOR 越权：CoachController 接口从客户端接受 userId 参数 | 教练可查看/操作其他教练的数据 | 统一从 JWT 获取身份，学时记录/查询增加学员归属校验 |
| S03 | 越权：`/api/common/coaches/students` 任意已登录用户可查询任意教练的学员 | 学员可查看教练名下所有学员信息 | 改为无参调用，JWT 自动识别教练身份，非教练返回空 |
| S04 | 前端路由仅检查 token 不检查角色 | 学生可手动输入 `/admin` 看到管理员界面和菜单 | 路由 `beforeEach` 新增角色校验，layout 组件 `onMounted` 二次校验 |

**Bug 修复 — 自动审核逻辑**

| 编号 | 问题 | 影响 | 修复 |
| --- | --- | --- | --- |
| B16 | `autoAudit()` 检查 `medicalStatus != PASSED` 永远不通过 | 所有报名自动被标为 REJECTED，流程完全阻塞 | 移除体检检查；通过后保持 PENDING（不再自动 APPROVE）；PDF 和学习阶段移到管理员手动通过时创建 |

**新增功能**

| 编号 | 功能 | 说明 |
| --- | --- | --- |
| F01 | 批量审核 | `PUT /api/admin/students/batch-audit`，勾选多个学员一次审核 |
| F02 | 审核日志 | 新增 `audited_by` + `audited_time` 字段，记录审核人和时间 |
| F03 | 前端审核增强 | 两步审核对话框（体检确认 → 审核决定），体检未合格时"通过"按钮置灰；体检/审核状态筛选；批量审核按钮 |
| F04 | 开发环境 profile | `application-dev.yml`：首次部署 `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 自动建库建表 |

**架构改进 — 数据库初始化**

| 编号 | 改进 | 说明 |
| --- | --- | --- |
| I01 | schema.sql 幂等化 | `DROP TABLE IF EXISTS` 改为 `CREATE TABLE IF NOT EXISTS`，重复启动不丢数据 |
| I02 | 初始化分工明确 | `schema.sql`（DDL）、`data.sql`（静态基础数据）、`DataInitializer`（BCrypt 用户）职责分离 |
| I03 | Profile 策略 | 默认 `never`（生产安全），dev profile `always`（开发便利） |

### v1.0.0 (2026-06-04)

**Bug 修复**

| 编号 | 问题 | 影响 | 修复 |
| --- | --- | --- | --- |
| B01 | MySQL `Public Key Retrieval is not allowed` | 数据库连接失败，后端无法启动 | JDBC URL 增加 `allowPublicKeyRetrieval=true` |
| B02 | CORS 配置类型不兼容 + Filter 双重注册 | 登录/注册返回 403 无权限 | CorsConfig 改用 `CorsConfigurationSource`；JwtAuthenticationFilter 移除 `@Component` 改为 SecurityConfig 显式 `@Bean` |
| B03 | `Column 'created_time' cannot be null` | 新注册用户写入失败 | 新增 `MyMetaObjectHandler`，自动填充 `LocalDateTime.now()` |
| B04 | `javax.xml.bind.DatatypeConverter` ClassNotFoundException | Java 17 下 JWT 解析报错 | pom.xml 添加 `jaxb-api:2.3.1` |
| B05 | iText 7 依赖 `<type>pom</type>` 无法下载 | 项目编译失败 | 拆分为 `kernel` + `layout` + `font-asian` 三个独立依赖 |
| B06 | 新注册学员登录后所有页面 403 | 学员端调用 `/api/admin/subjects` 等被角色拦截 | 新增 `CommonController` (`/api/common/**`)，对所有已认证角色开放 |
| B07 | 教练端无学员列表，只能手动输入 ID | 教练录学时操作不便 | `/api/common/coaches/students` 返回教练名下学员，前端改为下拉选择 |
| B08 | 管理员学员列表不显示姓名 | 界面只展示 userId 不友好 | `AdminController.listStudents` 关联 `user` 表返回 `realName`/`phone` |
| B09 | 约课取消原因始终为空 | 取消约课后无法查看原因 | 前端解构 `ElMessageBox.prompt` 返回值 `{ value: reason }` |
| B10 | 考试报名日期格式异常 | 前后端日期字段不匹配 | `el-date-picker` 添加 `value-format="YYYY-MM-DD"` |
| B11 | 管理员审核 `adminAudit` 逻辑混乱 | status 参数同时承载体检和审核状态 | Controller 层先设 `medicalStatus`，Service 层仅处理 `auditStatus` |
| B12 | `data.sql` 密码为手写占位符 | admin/123456 可能登录失败 | 新增 `DataInitializer`，启动时用 `BCryptPasswordEncoder` 动态编码 |
| B13 | `sql.init.mode: always` 每次重启清数据 | 开发过程中数据丢失 | 改为 `never` |
| B14 | `StudentLayout.vue` 导入路径 `../stores/user` 不存在 | 学员端页面白屏 | 修正为 `../../stores/user` |
| B15 | `StudentController` 有永远返回 null 的死代码 | 路径 `/info` 无法使用 | 删除死代码，保留 `/myinfo` |

**新增**

- `CommonController` — 提供车型、科目、考场、教练学员列表等公共只读接口
- `DataInitializer` — 启动时自动检查并创建 6 个默认用户 + 3 个教练记录
- `MyMetaObjectHandler` — MyBatis-Plus 自动填充 `createdTime`/`registrationTime`
- `commonApi()` — 前端 API 封装，统一管理公共接口调用

**优化**

- 教练端学时录入改为学员下拉选择，替换手动输入 ID
- 管理员学员列表展示 `realName` + `phone`
- 管理员审核对话框增加体检状态修改
- 前端 API 封装从 `adminApi` 拆分为 `adminApi` + `commonApi`
- `java.version` 从 11 更新为 17（与实际 JDK 一致）
- 密码全部改用 `DataInitializer` 的 BCrypt 动态编码，不再依赖手写哈希
- `.gitignore` 精简为 14 行，覆盖 Maven/Node/IDE/OS/上传/输出目录

## 注意事项

1. **数据库初始化**：首次部署使用 `dev` profile 自动建库建表（`mvn spring-boot:run -Dspring-boot.run.profiles=dev`）。后续启动使用默认 profile（`sql.init.mode=never`，不执行 SQL 脚本）。`schema.sql` 已改为 `CREATE TABLE IF NOT EXISTS`（幂等），`data.sql` 使用 `INSERT IGNORE`，`DataInitializer` 存在则跳过——三重保障重复启动安全
2. **MySQL 8.0 连接**：`application.yml` 中 JDBC URL 已包含 `allowPublicKeyRetrieval=true`，修改密码时请勿删除此参数
3. **PDF 中文**：若 PDF 中文显示为方块，在 `src/main/resources/fonts/` 下放入中文字体（如 `simsun.ttc`）
4. **文件上传**：限制 10MB，仅支持 `jpg/png/pdf`。上传目录 `uploads/` 和 PDF 输出目录 `pdfs/` 启动时自动创建
5. **端口冲突**：如果后端偶尔启动失败，先执行 `taskkill /F /FI "IMAGENAME eq java.exe"` 清理残留进程
6. **密码安全**：所有密码通过 BCrypt 加密存储，注册/登录流程均走加密比对，无明文传输
7. **Token 过期**：JWT 默认 24 小时过期，过期后需重新登录。修改 `application.yml` 中 `jwt.expiration` 可调整
8. **跨域**：CORS 配置为 `allowedOriginPattern("*")`，生产环境请替换为具体域名
9. **代理转发**：前端开发模式下 Vite 自动代理 `/api` 和 `/uploads` 到 `localhost:8080`，部署时改为 Nginx 反向代理


大小姐来也