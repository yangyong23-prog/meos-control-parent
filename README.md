# meos-control-parent

能源监控后端父工程，基于 Java 8、Spring Boot 和 Maven 多模块组织。项目同时维护云端 SaaS 与本地边缘端两套模块，公共能力沉淀在 `common`，业务接口服务由各自的 `server` 模块启动。

## 项目结构

```text
meos-control-parent
|-- common                         # 云端/边缘端共享公共能力
|-- saas                           # 云端 SaaS 模块组
|   |-- meos-control-common        # 云端公共服务能力
|   |-- meos-control-compute       # 云端计算能力依赖模块
|   `-- meos-control-server        # 云端接口服务，Spring Boot 启动模块
|-- local                          # 本地边缘端模块组
|   |-- meos-control-common-edge   # 边缘端公共服务能力
|   |-- meos-control-compute-edge  # 边缘端计算能力依赖模块
|   `-- meos-control-server-edge   # 边缘端接口服务，Spring Boot 启动模块
`-- docs                           # 项目设计文档目录
```

## 技术栈

| 类别 | 组件 |
| --- | --- |
| 基础框架 | Java 8, Spring Boot, Spring Cloud, Maven |
| 配置与注册 | Nacos |
| 数据访问 | MySQL, Druid, MyBatis Plus, Flyway |
| 缓存与消息 | Redis, Caffeine, RabbitMQ, Kafka |
| 任务调度 | XXL-JOB, Spring Scheduling |
| 接口文档 | Knife4j, springdoc-openapi |
| 质量工具 | JUnit, Mockito, P3C PMD |

## 环境要求

| 名称 | 版本/说明 |
| --- | --- |
| JDK | 1.8 |
| Maven | 使用项目父 POM 兼容版本 |
| MySQL | 8.x |
| Redis | 项目环境版本 |
| Nacos | 按运行环境配置 |
| RabbitMQ | 3.8.x |
| Erlang | 23.x |
| XXL-JOB | 2.3.x |

项目依赖公司私有 Maven 仓库：

```text
https://persagy2021-maven.pkg.coding.net/repository/yushu/public/
```

首次构建前请确认 Maven `settings.xml` 已配置私服访问权限。

## 构建

在仓库根目录执行：

```bash
mvn clean package
```

只构建云端接口服务及其依赖：

```bash
mvn -pl saas/meos-control-server -am clean package
```

只构建边缘端接口服务及其依赖：

```bash
mvn -pl local/meos-control-server-edge -am clean package
```

如本地暂不需要执行测试：

```bash
mvn clean package -DskipTests
```

## 本地运行

### 云端 SaaS 服务

启动模块：

```text
saas/meos-control-server
```

默认端口：`8089`

启动前需要准备 Nacos 连接参数：

| 参数 | 说明 |
| --- | --- |
| `NACOS_SERVERS` | Nacos 地址，如 `127.0.0.1:8848` |
| `NACOS_USERNAME` | Nacos 用户名 |
| `NACOS_PASSWORD` | Nacos 密码 |
| `NACOS_NAMESPACE` | Nacos 命名空间 |
| `NACOS_GROUP` | Nacos 配置分组 |

PowerShell 示例：

```powershell
$env:NACOS_SERVERS="127.0.0.1:8848"
$env:NACOS_USERNAME="<username>"
$env:NACOS_PASSWORD="<password>"
$env:NACOS_NAMESPACE="<namespace>"
$env:NACOS_GROUP="<group>"
mvn -pl saas/meos-control-server -am spring-boot:run
```

打包后运行：

```bash
java -jar <云端接口服务可执行JAR>
```

API 文档地址：

```text
http://localhost:8089/doc.html
```

### 边缘端服务

启动模块：

```text
local/meos-control-server-edge
```

默认端口：`8080`

启动前需要准备边缘端 Nacos 参数：

| 参数 | 说明 |
| --- | --- |
| `persagy.nacos.ip` | Nacos IP |
| `persagy.nacos.port` | Nacos 端口 |
| `persagy.nacos.username` | Nacos 用户名 |
| `persagy.nacos.password` | Nacos 密码 |
| `persagy.nacos.namespace` | Nacos 命名空间 |
| `persagy.nacos.group` | Nacos 配置分组 |

PowerShell 示例：

```powershell
mvn -pl local/meos-control-server-edge -am spring-boot:run `
  -Dspring-boot.run.arguments="--persagy.nacos.ip=127.0.0.1 --persagy.nacos.port=8848 --persagy.nacos.username=<username> --persagy.nacos.password=<password> --persagy.nacos.namespace=<namespace> --persagy.nacos.group=<group>"
```

打包后运行：

```bash
java -jar <边缘端接口服务可执行JAR>
```

API 文档地址：

```text
http://localhost:8080/doc.html
```

## 配置文件

| 模块 | 关键配置 |
| --- | --- |
| `saas/meos-control-server` | `bootstrap.yml`, `application.yml`, `application-local.yml`, `application-tenant.yml`, `application-knife4j.yml` |
| `saas/meos-control-common` | `application-db.yml`, `application-redis.yml`, `application-rabbitmq.yml`, `application-task.yml`, `application-actuator.yml` |
| `local/meos-control-server-edge` | `bootstrap.yml`, `application.yml`, `application-local.yml`, `application-flyway.yml` |
| `local/meos-control-common-edge` | `application-db.yml`, `application-redis.yml`, `application-paas.yml`, `application-knife4j.yml` |
| `common` | `application-kafka.yml`, `i18n/messages*.properties` |

云端服务默认引入 `actuator`、`db`、`paas`、`redis`、`kafka`、`rabbitmq`、`task`、`xxljob`、`tenant`、`knife4j` 等 profile 配置。边缘端服务默认引入 `paas`、`db`、`redis`、`knife4j`、`rabbitmq`、`flyway`、`xxljob`、`kafka` 等 profile 配置。

## 测试与检查

执行全部测试：

```bash
mvn test
```

执行指定模块及其依赖测试：

```bash
mvn -pl saas/meos-control-server -am test
mvn -pl local/meos-control-server-edge -am test
```

## 提交前检查

提交前建议至少确认：

```bash
git status --short
mvn -pl <module-path> -am test
```
