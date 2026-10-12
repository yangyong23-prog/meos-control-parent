# CLAUDE.md

> 云边架构能源监控系统，Java 8 + Spring Boot。

---

## 项目概述

**meos-control-parent** 是能源监控系统，采用云边架构，支持云端和边端部署。

---

## 技术栈

| 分类 | 技术 |
|------|------|
| 框架 | Spring Boot + Nacos |
| 数据库 | MyBatis Plus + Druid |
| 缓存 | Redis + Caffeine |
| 消息 | RabbitMQ、Kafka |
| 定时 | XXL-JOB |

---

## 模块结构

```
meos-control-parent/
├── common/basic/              # 共享基础模块（云+边共用）
├── saas/                      # 云端部署
│   └── meos-control-server/   # 云端入口
└── local/                     # 边端部署
    └── meos-control-server-edge/  # 边端入口
```

**应用入口**

- 云端: `com.persagy.apccontrol.ApcControlServerApplication`
- 边端: `com.persagy.apccontrol.ApcControlServerEdgeApplication`

---

## 开发规范

> **实现任何代码前必须阅读规范文件** — 规范位于 `.trellis/spec/`

### 规范索引

| 文件 | 说明 |
|------|------|
| [Backend Index](./.trellis/spec/backend/index.md) | 核心规范索引、MUST FOLLOW |
| [Directory Structure](./.trellis/spec/backend/directory-structure.md) | 模块布局、包结构 |
| [Database](./.trellis/spec/backend/database-guidelines.md) | MyBatis、SQL 查询 |
| [Error Handling](./.trellis/spec/backend/error-handling.md) | 异常类型、全局处理器 |
| [Logging](./.trellis/spec/backend/logging-guidelines.md) | LogComponent、LogExecutor |
| [Quality](./.trellis/spec/backend/quality-guidelines.md) | 代码模式、空值注解、Javadoc |
| [Guides Index](./.trellis/spec/guides/index.md) | 思考指南索引 |

---

## MUST FOLLOW（最常违规的规则）

```java
// ❌ new Date() / LocalDateTime.now() — FORBIDDEN
DateUtils.now() // ✅ CORRECT
```

```java
// ❌ @Builder on DTO — FORBIDDEN
@Data @NoArgsConstructor @AllArgsConstructor // ✅ CORRECT
```

```java
// ❌ @Slf4j in business class — FORBIDDEN
public class UserService implements LogComponent { // ✅ CORRECT
    public String logTag() { return "UserService"; }
}
```

```java
// ❌ Empty catch block — FORBIDDEN
catch(Exception e) { throw new BusinessException(...); } // ✅ CORRECT
```

```java
// ❌ Loop queries (N+1) — FORBIDDEN
mapper.selectBatchIds(ids); // ✅ CORRECT — batch query
```

---

## 分层架构

**所有类只能在规定的包中创建，禁止新增包。**

| 层 | 包名 | 说明 |
|---|------|------|
| Controller | `controller/{business}/` | REST 接口 |
| Service | `service/{business}/impl/` | 业务逻辑实现 |
| Mapper | `mapper/{business}/` | MyBatis 接口 |
| Mapper XML | `resources/mapper/` | |
| Entity | `model/entity/` | DO 类 |
| DTO | `model/dto/` | 数据传输对象 |
| VO | `model/vo/` | 视图对象 |
| Param | `model/param/{business}/` | 查询参数 |
| Convert | `convert/` | MapStruct 转换 |

**禁止新增的包**: `handler/`、`po/`、`page/`

---

## 数据库

- 所有 Mapper 继承 MyBatis Plus 基础 Mapper
- XML mapper 放在 `src/main/resources/mapper/{business}/`
- 基础 DO 类位于 `basic/model/base/`（`BaseDO`、`BaseAssignIdDO`）
- 使用 Flyway 进行数据库迁移

---

## 缓存机制

- **严格控制缓存使用**，仅当产品需求明确要求时才能添加
- `CacheKey` 常量定义缓存名称
- 两个缓存管理器: `REDIS_CACHE_MANAGER`（主）、`CAFFEINE_CACHE_MANAGER`

---

## 配置

- 应用通过 `bootstrap.yml` 从 Nacos 读取配置
- 共享配置: `x-common.yml`
- 本地 profile: `application-local.yml`

---

## 构建命令

```bash
# 全量构建
mvn clean package

# 构建指定模块
mvn clean package -pl saas/meos-control-server -am

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=类名 -pl 模块名
```

---

## 本地运行

使用 `.run/` 目录下的 IntelliJ 运行配置（Server-Dev、Edge-Dev 等），设置了连接 Nacos 所需的 VM 参数。
