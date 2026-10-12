## Context

`LocalCacheConstant.OBJECT_DETAIL_MAP` 是承载中台对象静态字段（localId、localName、classCode、objType 等）的进程内本地缓存，声明为 `ConcurrentHashMap<String, RwdObjectBusinessFeildDTO>`（初始容量 4096，非上限）。

当前访问全部集中在 `LocalRedisCacheHandler` 的 3 处：

- L113 `getObjectBusinessFeildAndSystemInfo`：`OBJECT_DETAIL_MAP.get(objectId)`（命中即返回，未命中走下方回源）
- L208 `getObjectBusinessFeild`：`OBJECT_DETAIL_MAP.get(objectId)`（miss 时查 bdtp 并 put）
- L220 `getObjectBusinessFeild`：`OBJECT_DETAIL_MAP.put(objectId, dto)`

缓存条目存在「先 put、后就地富化」的两阶段语义：`getObjectBusinessFeild` 在 L220 放入的 DTO 此时 `systemId`/`systemType` 仍为空；随后 `getObjectBusinessFeildAndSystemInfo`（L124~L187）对**同一个缓存内对象引用**执行 `wirteObjectClassBelongModelType()` 及关系查询，就地 set `systemId`/`systemType`。L120 的短路判断（`systemId` 非空且 `systemType` 非空即直接返回）依赖这个就地改写生效。

约束：common 模块已依赖 Caffeine 且实测可用——`mvn -pl common dependency:tree` 解析到 `com.github.ben-manes.caffeine:caffeine:jar:2.9.3:compile`（scope=compile，版本由父 pom `paas-cloud` / spring-boot BOM 管理，本地 `.m2` 仓库已存在该 jar）。现有生产代码 `CacheConfiguration`、`CaffeineCacheConfigurationImpl`（均为 `@SpringBootConfiguration`）已在使用 `Caffeine.newBuilder()`、`expireAfter`、`maximumSize` 等完整 API，可编译、可启动，进一步佐证依赖确在 classpath 上。运行环境为 JDK 1.8，而 2.9.3 正是 Caffeine 的 JDK 8 兼容线（3.x 起要求 JDK 11），版本安全；实施时须使用 2.9.3 支持的 API（`expireAfterWrite(long, TimeUnit)` 可用），不得误用 3.x 独有 API。

问题：该缓存进程存活期内永不过期、永不更新，bdtp 静态数据变更（如 localName 改名）后无法自动收敛，只能重启。

## Goals / Non-Goals

**Goals:**

- 让 `OBJECT_DETAIL_MAP` 中的条目在写入后 2 小时自动过期，过期后下一次访问回源重查 bdtp，使静态数据变更在最多 2 小时内自动收敛，无需重启。
- 保持现有对外行为不变：miss 回源、就地富化、返回语义与调用方（如 `RunningLogHandle`）契约完全兼容。
- 改动面最小化，仅触及缓存声明与 3 处调用点。

**Non-Goals:**

- 不改造富化逻辑本身（`getObjectBusinessFeildAndSystemInfo` 中查关系、拼 systemId/systemType 的代码一行不动）。
- 不处理 `SYSTEM_INFO` 等其他本地缓存的同类过期问题。
- 不做 TTL 配置化（固定 2 小时写死）。
- 不设置缓存容量上限（`maximumSize`）。
- 不改造回源失败时的降级行为（既有吞异常返回 null 的逻辑保留）。
- 不引入事件驱动的主动失效。

## Decisions

### 决策 1：用 Caffeine 承载 TTL，而非「时间戳全量清空」或「定时任务清空」

- **选择**：将 `OBJECT_DETAIL_MAP` 声明为 Caffeine `Cache<String, RwdObjectBusinessFeildDTO>`，`expireAfterWrite(2, TimeUnit.HOURS)`。
- **理由**：Caffeine 逐条按各自写入时间过期，过期时间点天然错开，不会在固定时刻产生「全服务同时回源」的尖峰；而时间戳全量清空 / 定时 clear 都会造成周期性同步回源峰值，该 handler 在运行日志与控制链路上被高频调用，尖峰不可取。项目已依赖 Caffeine，无新增成本。
- **备选**：
  - 保留 Map + 记录 `lastLoadTime` 超 2h 整体 clear —— 改动更小但有同步尖峰，否决。
  - Guava Cache —— 与 Caffeine 能力重叠，项目已用 Caffeine，不引入第二套，否决。
  - 事件驱动失效（bdtp 发布时主动 invalidate）—— 云边两端都可能持有该缓存，跨端事件同步复杂度远超「2 小时收敛」需求，否决。

### 决策 2：过期策略用 expireAfterWrite，不用 expireAfterAccess

- **选择**：`expireAfterWrite(2h)`。
- **理由**：缓存内多为热点对象（高频访问）。若用 `expireAfterAccess`，热点条目会被每次读操作续期而实际永不过期，localName 变更照样无法收敛，等于白改。`expireAfterWrite` 保证每个条目从**写入**起最多存活 2 小时，到期强制回源。
- **注意**：L220 之后的就地富化（set systemId/systemType）是对 value 对象引用的改写，**不是新的 put**，不会刷新写入时间戳，条目仍按初始 put 时间计算 2h 过期，符合预期。

### 决策 3：调用点由 get 改为 getIfPresent（编译兼容性关键点）

- **选择**：L113、L208 的 `OBJECT_DETAIL_MAP.get(objectId)` 改为 `getIfPresent(objectId)`；L220 的 `put(objectId, dto)` 保持不变。
- **理由**：Caffeine `Cache` 接口**没有单参 `get(K)`**（单参形式是 `get(K key, Function mappingFunction)`，会触发 loader 语义，与现有「miss 后自行回源并富化」流程冲突）。现有代码依赖「miss 返回 null、由调用方自行回源」的语义，对应 Caffeine 的 `getIfPresent`。`put` 两接口签名一致，无需改。
- **备选**：用 `get(key, k -> 回源逻辑)` 让 Caffeine 托管加载 —— 会把富化的两阶段流程强行塞进 loader，且 loader 内无法自然表达「富化依赖 header/systemId 上下文」，改造面反而更大，否决。

### 决策 4：保留「存对象引用 + 就地富化」语义

- **选择**：缓存 value 仍存 `RwdObjectBusinessFeildDTO` 引用，允许 put 后就地改写。
- **理由**：Caffeine 与 ConcurrentHashMap 一样按引用存储 value，就地改写对后续 `getIfPresent` 可见，L120 短路判断继续生效。此语义无需任何额外处理即可保留。
- **并发说明**：多线程可能同时对同一新条目做富化改写，但计算结果一致（幂等），与迁移前行为相同，不额外加锁。

## Risks / Trade-offs

- **[回源失败静默丢数据]** 条目过期恰逢 bdtp/Feign 抖动 → `getObjectBusinessFeild` 吞异常返回 null → `RunningLogHandle`（L73-75）等调用方 `businessFeild == null` 时静默 return，该条运行日志被丢弃；因未写入缓存，后续消息会持续重试回源。→ **缓解**：这是既有失败模式（今天首次访问 / 冷对象同样存在），TTL 仅将频率从「每对象一生一次」提高到「每对象每 2 小时一次」，bdtp 正常时回源为毫秒级。本次不做降级改造（如返回过期旧值），在 proposal 与本设计中记录备查；若后续边缘端 bdtp 链路不稳，可另起变更处理。
- **[周期性回源开销]** 每个活跃对象每 2h 多 1~2 次 Feign（对象详情 + 冷站/热站类关系查询）。→ **缓解**：逐条错开过期无同步尖峰；活跃对象规模有限，增量可忽略。
- **[无容量上限]** 不设 maximumSize，理论上条目数随不同 objectId 增长。→ **缓解**：与迁移前 ConcurrentHashMap 行为一致，且 2h 过期已兜住总量（过期条目被 Caffeine 回收），objectId 基数受项目设备总量约束，不会无界膨胀。
- **[迁移遗漏调用点]** 若存在对 `OBJECT_DETAIL_MAP` 的 Map 专有 API 调用（entrySet/keySet/size/containsKey 等），改类型后会编译失败。→ **缓解**：全仓检索确认仅 3 处 get/put 调用，无遍历/size 类用法；实施后以 `mvn compile` 兜底验证。

## Migration Plan

1. 修改 `LocalCacheConstant.OBJECT_DETAIL_MAP` 声明类型与初始化（`Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build()`）。
2. 修改 `LocalRedisCacheHandler` L113/L208 的 `get` → `getIfPresent`；L220 `put` 不变。
3. `mvn -pl common compile` 验证编译（含调用点无遗漏）。
4. 部署后无需数据迁移（纯进程内缓存，重启即生效新逻辑）。
5. **回滚**：还原上述两处文件即可，无持久化状态、无数据兼容问题。

## Open Questions

- 无。TTL 时长（2h）、过期策略（expireAfterWrite）、是否配置化（否）、是否设容量上限（否）、富化逻辑是否改动（否）均已在探索阶段确认。
