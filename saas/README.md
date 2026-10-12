# saas

`saas` 是能源监控项目的云端模块组，面向 SaaS 多租户部署场景。该目录是 Maven 聚合模块，包含云端公共能力、计算能力和接口服务；其中只有 `meos-control-server` 是可直接启动的 Spring Boot 应用。

## 模块结构

```text
saas
|-- pom.xml
|-- meos-control-common     # 云端公共能力模块
|-- meos-control-compute    # 云端计算能力模块
`-- meos-control-server     # 云端接口服务启动模块
```

模块依赖关系：

```text
meos-control-server
|-- meos-control-common
`-- meos-control-compute
    `-- meos-control-common
        `-- common
```

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `meos-control-common` | 云端公共服务能力，依赖根模块 `common`，承载数据库、Redis、RabbitMQ、Paas 多租户等通用配置与能力 |
| `meos-control-compute` | 云端计算能力，依赖 `common` 和 `meos-control-common`，包含计算任务、扩展能力、XXL-JOB 等 |
| `meos-control-server` | 云端接口服务，依赖 `meos-control-common` 和 `meos-control-compute`，通过 Spring Boot Maven Plugin 打成可执行 JAR |

## 技术栈

| 类别 | 组件 |
| --- | --- |
| 基础框架 | Java 8, Spring Boot, Spring Cloud, Maven |
| 配置与注册 | Nacos |
| 数据访问 | MySQL, Druid, MyBatis Plus, Flyway |
| 缓存与消息 | Redis, RabbitMQ, Kafka |
| 任务调度 | XXL-JOB, Spring Scheduling |
| 接口文档 | Knife4j, springdoc-openapi |
| 测试 | JUnit, Mockito |

## 运行入口

启动类：

```text
saas/meos-control-server/src/main/java/com/persagy/apccontrol/ApcControlServerApplication.java
```

默认服务端口：

```text
8089
```

Actuator 管理端口：

```text
8091
```

接口文档地址：

```text
http://localhost:8089/doc.html
```

## 配置文件

`meos-control-server/src/main/resources/application.yml` 默认引入以下 profile：

```text
actuator
db
paas
redis
kafka
rabbitmq
task
xxljob
tenant
knife4j
```

主要配置文件：

| 文件 | 说明 |
| --- | --- |
| `meos-control-server/src/main/resources/bootstrap.yml` | Nacos 配置中心、服务注册发现、共享配置 |
| `meos-control-server/src/main/resources/application.yml` | 服务端口、profile 引入、Feign、弱电系统关系配置 |
| `meos-control-server/src/main/resources/application-local.yml` | 本地调试配置，包含 Druid 监控、trace 日志和启动任务开关 |
| `meos-control-server/src/main/resources/application-tenant.yml` | 租户数据库初始化配置 |
| `meos-control-server/src/main/resources/application-knife4j.yml` | Knife4j UI 配置 |
| `meos-control-common/src/main/resources/application-db.yml` | MySQL、Druid、dynamic-datasource、MyBatis Plus 配置 |
| `meos-control-common/src/main/resources/application-redis.yml` | Redis 配置 |
| `meos-control-common/src/main/resources/application-rabbitmq.yml` | RabbitMQ 配置 |
| `meos-control-common/src/main/resources/application-paas.yml` | Paas 多租户、操作日志、XSS、分布式锁配置 |
| `meos-control-common/src/main/resources/application-task.yml` | Spring 异步任务线程池配置 |
| `meos-control-common/src/main/resources/application-actuator.yml` | Actuator 健康检查、管理端口和指标配置 |
| `meos-control-compute/src/main/resources/application-xxljob.yml` | XXL-JOB 执行器配置 |
| `../common/src/main/resources/application-kafka.yml` | Kafka 配置 |

## 外部配置

启动前需要通过环境变量、Nacos 或启动参数提供运行环境配置。

### Nacos

| 配置项 | 说明 |
| --- | --- |
| `NACOS_SERVERS` | Nacos 地址，如 `127.0.0.1:8848` |
| `NACOS_USERNAME` | Nacos 用户名 |
| `NACOS_PASSWORD` | Nacos 密码 |
| `NACOS_NAMESPACE` | Nacos 命名空间 |
| `NACOS_GROUP` | Nacos 配置分组 |

### 数据与中间件

| 配置项 | 说明 |
| --- | --- |
| `mysql.ip` | MySQL IP |
| `mysql.port` | MySQL 端口 |
| `mysql.username` | MySQL 用户名 |
| `mysql.password` | MySQL 密码 |
| `redis.host` | Redis 地址，默认 `127.0.0.1` |
| `redis.port` | Redis 端口，默认 `6379` |
| `redis.password` | Redis 密码，默认空 |
| `redis.meos_database.five` | Redis DB 编号，默认 `7` |
| `rabbitmq.host` | RabbitMQ 地址 |
| `rabbitmq.port` | RabbitMQ 端口 |
| `rabbitmq.username` | RabbitMQ 用户名 |
| `rabbitmq.password` | RabbitMQ 密码 |
| `rabbitmq.virtual-host` | RabbitMQ virtual host |
| `kafka.bootstrap-servers` | Kafka 集群地址 |
| `kafka.properties.security.protocol` | Kafka 安全协议，默认 `SASL_PLAINTEXT` |
| `kafka.properties.sasl.mechanism` | Kafka SASL 机制，默认 `PLAIN` |
| `kafka.properties.sasl.jaas.config` | Kafka SASL JAAS 配置 |

### 业务参数

| 配置项 | 说明 |
| --- | --- |
| `groupCode` | 租户初始化时使用的集团编码 |
| `tenant.data.init.default.baseline-version` | 租户库初始化基线版本 |
| `external-url.paas.gateway.service` | 引擎图标服务网关地址 |

## 构建

以下命令均从仓库根目录执行。

构建云端接口服务及依赖：

```bash
mvn -pl saas/meos-control-server -am clean package
```

跳过测试构建：

```bash
mvn -pl saas/meos-control-server -am clean package -DskipTests
```

构建整个云端模块组：

```bash
mvn -pl saas/meos-control-common,saas/meos-control-compute,saas/meos-control-server -am clean package
```

构建完成后，Maven 会生成云端接口服务的可执行 JAR。

## 本地启动

### JAR 启动

```bash
java -jar <云端接口服务可执行JAR>
```

Bash 环境变量示例：

```bash
export NACOS_SERVERS=127.0.0.1:8848
export NACOS_USERNAME=<username>
export NACOS_PASSWORD=<password>
export NACOS_NAMESPACE=<namespace>
export NACOS_GROUP=<group>
java -jar <云端接口服务可执行JAR>
```

PowerShell 环境变量示例：

```powershell
$env:NACOS_SERVERS="127.0.0.1:8848"
$env:NACOS_USERNAME="<username>"
$env:NACOS_PASSWORD="<password>"
$env:NACOS_NAMESPACE="<namespace>"
$env:NACOS_GROUP="<group>"
java -jar <云端接口服务可执行JAR>
```

### IDE 启动

在 IntelliJ IDEA 中运行：

```text
com.persagy.apccontrol.ApcControlServerApplication
```

同时在运行配置中设置 `NACOS_SERVERS`、`NACOS_USERNAME`、`NACOS_PASSWORD`、`NACOS_NAMESPACE`、`NACOS_GROUP` 等环境变量。

## 测试

执行接口服务及依赖测试：

```bash
mvn -pl saas/meos-control-server -am test
```

执行云端模块组测试：

```bash
mvn -pl saas/meos-control-common,saas/meos-control-compute,saas/meos-control-server -am test
```

## 开发注意事项

- 只有 `meos-control-server` 通过 `spring-boot-maven-plugin` 打包为可执行服务。
- `meos-control-common` 和 `meos-control-compute` 是依赖模块，不单独作为应用启动。
- 本地调试建议启用 `local` profile，以加载 `application-local.yml` 中的调试配置。
- Flyway 在 `application.yml` 中默认关闭，租户库初始化逻辑由 `application-tenant.yml` 和相关 starter 配置控制。
- Mapper XML 按项目后端规范放在 `resources/mapper/{business}/{Entity}Mapper.xml`。
