# 大学教务管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 项目简介

大学教务管理系统是一个基于Spring Boot + Vue 3 + MySQL的现代化教务管理平台，采用前后端集成的单体架构。系统支持管理员、教师、学生三种角色，提供完整的教务管理功能。

### 主要功能

- 🔐 **用户认证与授权**：基于JWT的安全认证，支持角色权限控制
- 👥 **用户管理**：学生管理、教师管理、用户信息维护
- 📚 **课程管理**：课程信息管理、先修课程设置、开课计划
- 📝 **选课系统**：在线选课、退课、时间冲突检测、人数限制
- 📊 **成绩管理**：成绩录入、成绩查询、成绩单生成、统计分析
- 📅 **课表查询**：个人课表、课程安排、导出功能
- 📝 **考试管理**：考试计划、考场安排、监考管理、自动分配学生
- 📢 **通知公告**：系统通知、课程通知、考试通知、消息推送
- 📈 **数据统计**：学生统计、课程统计、成绩分析、教师工作量

## 🏗️ 技术架构

### 后端技术栈

- **核心框架**：Spring Boot 3.2.0
- **安全框架**：Spring Security + JWT
- **持久层**：Spring Data JPA + Hibernate
- **数据库**：MySQL 8.0
- **缓存**：Caffeine Cache
- **构建工具**：Maven 3.6+
- **文档生成**：Apache POI (Excel)、iText (PDF)

### 前端技术栈

- **框架**：Vue 3 (Composition API)
- **路由**：Vue Router 4
- **状态管理**：Pinia
- **UI组件**：Element Plus
- **HTTP客户端**：Axios
- **构建工具**：Vite

### 性能优化

#### 后端优化
- ✅ **缓存策略**：Spring Cache + Caffeine多级缓存，默认30分钟过期
- ✅ **批量操作**：考试学生分配采用批量保存，减少数据库往返
- ✅ **乐观锁**：考场容量控制使用@Version乐观锁，防止并发冲突
- ✅ **查询优化**：JOIN FETCH避免N+1查询，批量删除使用@Modifying
- ✅ **连接池**：HikariCP高性能数据库连接池
- ✅ **索引优化**：关键字段添加数据库索引，提升查询速度

#### 前端优化
- ✅ **路由懒加载**：Vue Router按需加载组件
- ✅ **状态缓存**：Keep-alive缓存页面状态
- ✅ **防抖节流**：搜索输入防抖优化
- ✅ **虚拟滚动**：大数据列表渲染优化（备选）

#### 性能提升
- 📈 1000+学生自动分配性能提升 **>90%**
- 📈 缓存命中查询性能提升 **>95%**  
- 📈 批量删除操作性能提升 **>80%**

## 📁 项目结构

```
university-academic-system/
├── src/
│   ├── main/
│   │   ├── java/com/university/academic/
│   │   │   ├── config/          # 配置类（Security、Cache等）
│   │   │   ├── controller/      # REST API控制器
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── entity/          # JPA实体类
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── vo/              # 视图对象
│   │   │   ├── security/        # 安全相关（JWT、Filter）
│   │   │   ├── exception/       # 异常处理
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── application.yml       # 主配置文件
│   │       ├── application-dev.yml   # 开发环境配置
│   │       ├── application-prod.yml  # 生产环境配置
│   │       └── db/migration/         # 数据库脚本
│   └── frontend/
│       ├── src/
│       │   ├── views/           # 页面组件
│       │   ├── components/      # 公共组件
│       │   ├── router/          # 路由配置
│       │   ├── stores/          # Pinia状态管理
│       │   ├── api/             # API接口封装
│       │   └── utils/           # 工具函数
│       ├── package.json
│       └── vite.config.js
└── pom.xml                      # Maven配置
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ & npm 8+

### 1. 克隆项目

```bash
git clone https://github.com/tangqingfeng7/university-academic-system
cd university-academic-system
```

### 2. 配置数据库

创建数据库：
```sql
CREATE DATABASE academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入初始化脚本：
```bash
mysql -u root -p academic_system < src/main/resources/db/migration/schema.sql
mysql -u root -p academic_system < src/main/resources/db/migration/test_data.sql
```

### 3. 配置应用

编辑 `src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academic_system
    username: root
    password: your_password
```

### 4. 启动后端

```bash
# 使用Maven
mvn spring-boot:run

# 或者先打包再运行
mvn clean package
java -jar target/academic-system.jar
```

后端服务将在 http://localhost:8080 启动

### 5. 启动前端（开发模式）

```bash
cd src/frontend
npm install
npm run dev
```

前端开发服务器将在 http://localhost:5173 启动

### 6. 访问系统

打开浏览器访问：http://localhost:5173

默认账户：
- **管理员**：admin / admin123
- **教师**：T001 / 123456
- **学生**：S000001 / 123456

## 📦 部署

### 生产环境打包

```bash
# 后端打包（包含前端构建）
mvn clean package -Pprod

# 生成的jar文件
target/academic-system.jar
```

### 运行生产环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=academic_system
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export JWT_SECRET=your-secret-key

# 启动应用
java -jar target/academic-system.jar
```

详细部署文档请参考：[DEPLOY.md](DEPLOY.md)

## 📚 API文档

启动应用后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 🔑 核心功能说明

### 考试管理模块（新增）

#### 功能特性
- **考试计划管理**
  - 创建考试（期中/期末/补考/重修）
  - 考试时间安排和时长设置
  - 考试状态自动管理（草稿→已发布→进行中→已结束）
  
- **考场管理**
  - 手动创建考场
  - 批量自动创建考场
  - 考场容量控制
  - 考场时间冲突检测
  
- **学生分配**
  - 自动分配学生到考场（按学号排序）
  - 自动生成座位号（A01、A02...）
  - 学生考试冲突检测
  - 支持重新分配
  
- **监考管理**
  - 添加主监考/副监考教师
  - 教师时间冲突检测
  - 监考任务统计
  - 监考安排导出

#### 智能化特性
- ⚡ **自动化分配**：一键完成1000+学生的考场和座位分配
- 🔍 **冲突检测**：智能检测学生、考场、教师的时间冲突
- 📊 **状态管理**：定时任务自动更新考试状态
- 🔔 **通知推送**：考试发布自动通知相关师生

### 管理端功能
- 学生信息管理（CRUD、导入导出）
- 教师信息管理
- 课程管理与先修课程设置
- 开课计划管理（排课、冲突检测）
- 学期管理
- 院系专业管理
- 考试管理（考试计划、考场安排、监考分配、冲突检测）
- 自动化功能（自动创建考场、自动分配学生）
- 系统配置
- 操作日志查询
- 数据统计与报表

### 教师端功能
- 我的课程管理
- 成绩录入与管理
- 成绩批量导入
- 监考任务查询
- 课程考试查看
- 课程通知发布
- 我的课表查询

### 学生端功能
- 在线选课退课
- 我的课表查询
- 我的考试安排
- 考场座位查询
- 成绩查询
- 成绩单导出
- 通知公告查看

## 🎯 系统特色

1. **安全性**
   - JWT无状态认证
   - BCrypt密码加密
   - RBAC权限控制
   - 操作日志记录

2. **性能优化**
   - 多级缓存机制（Caffeine Cache）
   - 批量操作优化（减少数据库往返>90%）
   - 乐观锁并发控制（@Version防冲突）
   - 数据库索引优化
   - 前端路由懒加载
   - 防抖节流优化

3. **智能化功能**
   - 自动考场分配算法
   - 智能冲突检测（时间、考场、教师）
   - 自动学生座位分配
   - 考试状态自动更新

4. **用户体验**
   - 响应式设计
   - 实时数据更新
   - 友好的错误提示
   - 快捷操作支持
   - 一键批量操作

5. **可维护性**
   - 清晰的分层架构
   - 完善的异常处理
   - 详细的代码注释
   - 标准化的API设计
   - 数据库迁移脚本

## 🛠️ 开发指南

### 代码规范

- 遵循阿里巴巴Java开发规范
- 使用ESLint + Prettier格式化前端代码
- 所有API接口返回统一格式
- Service层方法添加事务注解
- 重要操作添加操作日志

### 数据库规范

- 所有表必须有主键和索引
- 使用软删除而非物理删除
- 添加created_at和updated_at字段
- 外键关联使用@ManyToOne/@OneToMany

### 测试

```bash
# 运行后端测试
mvn test

# 运行前端测试
cd src/frontend
npm run test
```

## 📝 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请联系：
- Email: traceck1@gmail.com
- Issue: https://github.com/tangqingfeng7/university-academic-system/issues

## 🙏 致谢

感谢所有开源项目的贡献者！

---

## 📋 版本历史

### v1.1.0 (2025-10-09)
**新增功能**
- ✨ 考试管理模块（考试计划、考场安排、监考管理）
- ✨ 自动考场创建和学生分配功能
- ✨ 考试时间冲突智能检测
- ✨ 考试通知系统集成

**性能优化**
- ⚡ 批量操作优化，1000+学生分配性能提升>90%
- ⚡ Caffeine缓存集成，查询性能提升>95%
- ⚡ 乐观锁并发控制，保证数据一致性
- ⚡ 数据库查询优化，批量删除性能提升>80%

### v1.0.0 (2025-10-08)
- 🎉 初始版本发布
- ✅ 用户管理、课程管理、选课系统
- ✅ 成绩管理、课表查询、通知系统

---

**当前版本**: v1.1.0  
**最后更新**: 2025-10-09
