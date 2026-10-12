# local

`local` 是能源监控项目的本地边缘端模块组，面向边缘侧部署场景，包含边缘端接口服务、公共能力和计算能力。该目录自身是 Maven 聚合模块，真正可启动的 Spring Boot 应用位于 `meos-control-server-edge`。

## 模块结构

```text
local
|-- pom.xml
|-- meos-control-common-edge    # 边缘端公共能力：Redis、SSE 公共模型、历史 WebSocket 兼容、公共处理器、外部服务适配等
|-- meos-control-compute-edge   # 边缘端计算能力：MQTT、SSE 推送、历史 WebSocket 转调兼容、定时任务等
`-- meos-control-server-edge    # 边缘端接口服务：Spring Boot 启动模块
```

模块依赖关系：

```text
meos-control-server-edge
`-- meos-control-compute-edge
    `-- meos-control-common-edge
        `-- common
```

## 运行入口

启动类：

```text
local/meos-control-server-edge/src/main/java/com/persagy/apccontrol/ApcControlServerEdgeApplication.java
```

默认端口：

```text
8080
```

API 文档：

```text
http://localhost:8080/doc.html
```

应用启动后也会在日志中打印实际的 Knife4j 文档地址。

## 配置组成

`meos-control-server-edge/src/main/resources/bootstrap.yml` 中默认引入以下 profile 配置：

```text
paas
db
redis
knife4j
rabbitmq
flyway
xxljob
kafka
```

主要配置文件：

| 文件 | 说明 |
| --- | --- |
| `meos-control-server-edge/src/main/resources/bootstrap.yml` | 服务端口、Nacos 配置中心与注册中心、线程池、profile 引入 |
| `meos-control-server-edge/src/main/resources/application.yml` | 边缘端业务参数、项目参数、弱电系统关系配置 |
| `meos-control-server-edge/src/main/resources/application-local.yml` | 本地调试配置，包含 Druid 监控、trace 日志和部分 RabbitMQ 监听开关 |
| `meos-control-server-edge/src/main/resources/application-flyway.yml` | Flyway 数据库迁移配置，默认 `enabled: false` |
| `meos-control-common-edge/src/main/resources/application-db.yml` | MySQL、Druid、dynamic-datasource、MyBatis Plus 配置 |
| `meos-control-common-edge/src/main/resources/application-redis.yml` | Redis 连接池配置 |
| `meos-control-common-edge/src/main/resources/application-paas.yml` | Paas 多租户、MQTT、Feign 配置 |
| `meos-control-common-edge/src/main/resources/application-knife4j.yml` | Knife4j UI 配置 |
| `meos-control-compute-edge/src/main/resources/application-xxljob.yml` | XXL-JOB 执行器配置 |

数据库迁移脚本位于：

```text
meos-control-server-edge/src/main/resources/db/flyway/
```

## 外部配置项

启动边缘端服务前，至少需要在 Nacos 或启动参数中提供以下配置：

| 配置项 | 说明 |
| --- | --- |
| `persagy.nacos.ip` | Nacos IP |
| `persagy.nacos.port` | Nacos 端口 |
| `persagy.nacos.username` | Nacos 用户名 |
| `persagy.nacos.password` | Nacos 密码 |
| `persagy.nacos.namespace` | Nacos 命名空间 |
| `persagy.nacos.group` | Nacos 配置分组 |
| `mysql.ip` | MySQL IP |
| `mysql.port` | MySQL 端口 |
| `mysql.username` | MySQL 用户名 |
| `mysql.password` | MySQL 密码 |
| `redis.host` | Redis 地址，默认 `127.0.0.1` |
| `redis.port` | Redis 端口，默认 `6379` |
| `redis.password` | Redis 密码，默认空 |
| `mqtt.url` | MQTT Broker 地址 |
| `mqtt.username` | MQTT 用户名 |
| `mqtt.password` | MQTT 密码 |
| `mqtt.topic.meos-control.name` | 边缘端控制消息 Topic |
| `mqtt.topic.meos-compute.name` | 边缘端计算消息 Topic |
| `mqtt.topic.event-change.name` | 事件变更 Topic |
| `mqtt.topic.danger-event.name` | 风险事件 Topic |
| `mqtt.topic.dtp-meos.name` | DTP/MEOS Topic |
| `project.id` | 项目 ID |
| `project.groupcode` | 项目分组编码，用于数据库 Schema 等配置 |
| `project.projectName` | 项目名称，可为空 |
| `external-url.paas.paas-filetransfer.service` | 图标下载服务地址 |
| `external-url.paas.paas-datatransfer.service` | 文件下载服务地址 |
| `minio.url` | MinIO 文件 URL 前缀 |

部分配置带默认值，例如 Redis、Druid 连接池和 Flyway 开关；真实部署仍应以目标环境配置为准。

## 构建

以下命令均从仓库根目录执行。

构建整个边缘端模块组：

```bash
mvn -pl local/meos-control-common-edge,local/meos-control-compute-edge,local/meos-control-server-edge -am clean package
```

构建边缘端接口服务及其依赖：

```bash
mvn -pl local/meos-control-server-edge -am clean package
```

跳过测试构建：

```bash
mvn -pl local/meos-control-server-edge -am clean package -DskipTests
```

构建完成后，Maven 会生成边缘端接口服务的可执行 JAR。

## 本地启动

### 使用 JAR 启动

先构建：

```bash
mvn -pl local/meos-control-server-edge -am clean package -DskipTests
```

再启动：

```bash
java -jar <边缘端接口服务可执行JAR>
```

如果配置不在 Nacos 中，也可以通过启动参数临时覆盖：

```bash
java -jar <边缘端接口服务可执行JAR> \
  --persagy.nacos.ip=127.0.0.1 \
  --persagy.nacos.port=8848 \
  --persagy.nacos.username=<username> \
  --persagy.nacos.password=<password> \
  --persagy.nacos.namespace=<namespace> \
  --persagy.nacos.group=<group>
```

PowerShell 写法：

```powershell
java -jar <边缘端接口服务可执行JAR> `
  --persagy.nacos.ip=127.0.0.1 `
  --persagy.nacos.port=8848 `
  --persagy.nacos.username=<username> `
  --persagy.nacos.password=<password> `
  --persagy.nacos.namespace=<namespace> `
  --persagy.nacos.group=<group>
```

### 使用 IDE 启动

在 IntelliJ IDEA 中运行：

```text
com.persagy.apccontrol.ApcControlServerEdgeApplication
```

建议 VM options 或 Program arguments 中设置：

```text
--persagy.nacos.ip=<ip>
--persagy.nacos.port=<port>
--persagy.nacos.username=<username>
--persagy.nacos.password=<password>
--persagy.nacos.namespace=<namespace>
--persagy.nacos.group=<group>
```

## 测试

执行边缘端模块组测试：

```bash
mvn -pl local/meos-control-common-edge,local/meos-control-compute-edge,local/meos-control-server-edge -am test
```

执行接口服务及依赖测试：

```bash
mvn -pl local/meos-control-server-edge -am test
```

## 开发提示

- `meos-control-server-edge` 负责边缘端 API、登录拦截、Swagger/Knife4j、Flyway、应用启动与业务入口。
- `meos-control-compute-edge` 负责 MQTT 消息、边缘计算任务、SSE 推送、历史 WebSocket 转调兼容和 XXL-JOB 执行器能力。
- `meos-control-common-edge` 负责边缘端公共配置、Redis、公共服务、外部服务客户端和通用处理逻辑。
- Mapper XML 按项目后端规范放在 `resources/mapper/{business}/{Entity}Mapper.xml`。
