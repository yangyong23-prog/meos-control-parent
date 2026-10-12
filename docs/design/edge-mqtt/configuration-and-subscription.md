# 配置与订阅初始化

边缘 MQTT 配置由 `local/meos-control-common-edge/src/main/resources/application-paas.yml` 提供默认值，实际部署参数通过外部配置注入。边缘计算服务 `meos-control-compute-edge` 引入 `starter-mqtt`，并在启动初始化阶段向 DTP Twin 注册需要订阅的对象类。

## Maven 依赖

`local/meos-control-compute-edge/pom.xml` 引入：

```xml
<dependency>
    <groupId>com.persagy</groupId>
    <artifactId>starter-mqtt</artifactId>
</dependency>
```

这表示 MQTT 接收能力由边缘计算服务承接。不要在 SaaS 模块新增该依赖来实现 MQTT 接入。

## 配置项

`application-paas.yml` 中 `paas.mqtt` 的关键配置如下：

| 配置 | 作用 |
| --- | --- |
| `enabled` | 是否启用边缘 MQTT 能力，默认 `true`。 |
| `url` | broker 地址，来自 `${mqtt.url}`。 |
| `username` / `password` | 用户名密码认证参数。 |
| `auth-type` | 认证方式，当前默认 `USERNAME`。 |
| `cleanSession` | MQTT clean session 设置。 |
| `ca-crt` / `client-key` / `client-crt` | TLS 证书文件。 |
| `receive-topics` | 边缘端订阅的 topic 列表。 |
| `keep-alive` | MQTT keep alive。 |
| `completion-timeout` | 发送或订阅操作超时时间。 |
| `client` | 客户端名称，当前为 `${spring.application.name}_client`。 |
| `client-id-random` | 是否追加随机 clientId，避免多实例冲突。 |
| `comsumer` | 消费批量、缓冲队列、拒绝策略和消费线程池配置。 |

当前默认订阅 topic：

| topic 配置 | 语义 |
| --- | --- |
| `${mqtt.topic.meos-control.name}` | Twin 控制值或控制侧点位消息，接收后按控制值处理。 |
| `${mqtt.topic.meos-compute.name}` | Twin 反馈值消息，接收后按反馈值处理。 |
| `${mqtt.topic.event-change.name}` | 异常事件消息。 |
| `${mqtt.topic.danger-event.name}` | 风险事件消息。 |
| `${mqtt.topic.dtp-meos.name}` | DTP-MEOS 业务消息。 |

## 消费线程池

`paas.mqtt.comsumer` 控制 MQTT 消费缓冲和线程池：

| 配置 | 当前值 | 说明 |
| --- | --- | --- |
| `consumer-batch-size` | `50` | 批量消费一次处理的数据量。 |
| `consumer-buffer-queue-size` | `102400` | MQTT 消费缓冲队列长度。 |
| `handler` | `drop` | 队列阻塞时丢弃，避免主线程执行导致断联重连风险。 |
| `consumer-thread.core-pool-size` | `5` | 消费线程核心数。 |
| `consumer-thread.maximum-pool-size` | `50` | 消费线程最大数。 |
| `consumer-thread.capacity` | `1024` | 消费线程等待队列容量。 |

`local/meos-control-server-edge/src/main/resources/bootstrap.yml` 中的 `server.thread.prefix: async-mqtt-` 是边缘服务线程命名前缀，不等同于 MQTT broker 配置。

## 订阅初始化入口

`MqttSubscribeInitServiceImpl` 实现 `SystemInitService`，并标记：

- `@Component`
- `@Order(0)`
- `@Async`
- `@ComponentScan("com.persagy.framework.mqtt")`

启动时执行 `init()`：

1. 读取 `paas.mqtt.enabled`。
2. 未启用时直接返回，并记录“不进行MQTT订阅”。
3. 从 Redis `RedisKey.CLASS_NAME` 读取对象类。
4. 从 `InfoCodeConfigService#getClassCodesForTwinSubscribe` 读取需要 Twin 订阅的信息点对象类。
5. 如果对象类为空，返回并记录“未配置设备类不进行MQTT订阅”。
6. 合并对象类，构造 `MqttSubscribe` 列表。
7. 循环调用 `DtpTwinClient#addComputeSubscribe(projectId, groupCode, mqttSubscribes)`。
8. Twin 返回成功后退出循环；失败或异常时等待 10 秒重试。

## 订阅对象构造

`buildRCList` 以对象类编码构造 `MqttSubscribe`：

| 字段 | 规则 |
| --- | --- |
| `classCode` | 直接取对象类编码；如果包含 `&`，只取 `&` 前面的主类编码。 |
| `infoCode` | 当前初始化逻辑不填，表示按对象类订阅。 |

订阅初始化的职责是“注册边缘计算服务关注哪些类”，不是在本地生成 topic。topic 仍由 `paas.mqtt.receive-topics` 控制。

## 失败与重试

订阅初始化使用无限循环直到成功。维护时需要注意：

- 如果 Twin 服务不可用，初始化线程会每 10 秒重试。
- 如果对象类配置为空，不会重试，需要先确认 Redis 和信息点配置是否已初始化。
- 如果 `paas.mqtt.enabled=false`，订阅初始化和模拟接口都会按 MQTT 条件关闭。

## 维护要点

- 新增边缘接收 topic 时，先更新 `paas.mqtt.receive-topics`，再在 `MqttReceiveServiceImpl` 增加分流逻辑。
- 新增需要 Twin 推送的对象类时，优先检查 `InfoCodeConfigService#getClassCodesForTwinSubscribe` 是否能产出对应 classCode。
- 不要在 SaaS 配置文件中复制 `paas.mqtt` 作为接入配置。
- `comsumer` 是当前配置键名，修改拼写会影响 starter 绑定，除非同步检查 starter-mqtt 的属性绑定。
