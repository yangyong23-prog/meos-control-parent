## ADDED Requirements

### Requirement: 对象静态字段缓存写入后自动过期

系统 SHALL 为 `LocalCacheConstant.OBJECT_DETAIL_MAP`（中台对象静态字段本地缓存）设置写入后过期策略，过期时长固定为 2 小时，使 bdtp 侧静态数据变更无需重启即可自动收敛。

#### Scenario: 条目写入后 2 小时内命中缓存

- **WHEN** 某 objectId 的静态字段被写入 `OBJECT_DETAIL_MAP` 后的 2 小时内再次访问该 objectId
- **THEN** 系统 SHALL 直接返回缓存中的对象，不回源查询 bdtp

#### Scenario: 条目写入满 2 小时后过期回源

- **WHEN** 某 objectId 写入 `OBJECT_DETAIL_MAP` 已满 2 小时后再次访问该 objectId
- **THEN** 该缓存条目 SHALL 已过期，系统 SHALL 重新查询 bdtp 获取最新静态字段并写入缓存

#### Scenario: 静态数据变更后自动收敛

- **WHEN** bdtp 侧某对象的 localName 发生变更，且该对象缓存条目自上次写入起已满 2 小时
- **THEN** 下一次访问该对象 SHALL 拿到变更后的 localName，无需重启服务

### Requirement: 过期策略基于写入时间而非访问时间

系统 SHALL 采用「写入后过期」（expireAfterWrite）语义，缓存条目的过期时间 SHALL 由写入时刻决定，不因后续读取访问而顺延，以避免热点对象被持续续期导致实际永不过期。

#### Scenario: 高频访问不顺延过期

- **WHEN** 某热点 objectId 在写入后的 2 小时内被高频读取访问
- **THEN** 该条目 SHALL 仍在写入满 2 小时时过期，读取访问 SHALL NOT 重置其过期时间

#### Scenario: 就地富化不刷新写入时间

- **WHEN** 某条目写入缓存后，系统对缓存内对象引用就地设置 `systemId`/`systemType`（非重新 put）
- **THEN** 该条目的过期时间 SHALL 仍以初始写入时刻计算，就地富化 SHALL NOT 刷新过期时间

### Requirement: 缓存迁移保持既有对外行为兼容

将 `OBJECT_DETAIL_MAP` 由 `ConcurrentHashMap` 迁移为 Caffeine `Cache` 后，系统 SHALL 保持未命中回源、就地富化、返回值语义与所有调用方（含 `LocalRedisCacheHandler`、`RunningLogHandle`）的现有契约完全一致。

#### Scenario: 未命中返回 null 并由调用方回源

- **WHEN** 访问一个不在缓存中的 objectId
- **THEN** 缓存读取 SHALL 返回 null（对应 Caffeine `getIfPresent` 语义）
- **AND** 调用方 SHALL 按既有逻辑自行回源查询 bdtp 并在成功后写入缓存

#### Scenario: 就地富化对后续读取可见

- **WHEN** 某条目写入缓存后，系统对缓存内对象引用就地设置 `systemId`/`systemType`
- **THEN** 后续对同一 objectId 的读取 SHALL 能读到已富化的 `systemId`/`systemType`
- **AND** 当 `systemId` 与 `systemType` 均非空时，`getObjectBusinessFeildAndSystemInfo` SHALL 直接返回而不重复富化

#### Scenario: 富化逻辑不因迁移而改变

- **WHEN** 实施本次缓存迁移
- **THEN** `getObjectBusinessFeildAndSystemInfo` 中查询关系、拼装 `systemId`/`systemType` 的富化逻辑 SHALL NOT 被修改，仅在缓存过期重建时按原逻辑自然重跑

### Requirement: 回源失败维持既有降级表现

缓存条目过期后回源查询 bdtp 失败时，系统 SHALL 维持迁移前既有的降级表现，本次变更 SHALL NOT 新增回源失败的补偿或降级逻辑。

#### Scenario: 回源异常时返回空

- **WHEN** 某 objectId 缓存过期后回源查询 bdtp 抛出异常
- **THEN** `getObjectBusinessFeild` SHALL 吞掉异常并返回 null（与迁移前一致）
- **AND** 调用方 `RunningLogHandle` 在 `businessFeild` 为 null 时 SHALL 静默跳过该条数据，不写入缓存，后续消息 SHALL 重试回源

### Requirement: 缓存不设置容量上限

系统 SHALL NOT 为 `OBJECT_DETAIL_MAP` 设置 `maximumSize` 容量上限，条目总量 SHALL 由 2 小时过期机制自然回收兜底。

#### Scenario: 仅依赖过期回收总量

- **WHEN** 不同 objectId 持续写入 `OBJECT_DETAIL_MAP`
- **THEN** 系统 SHALL NOT 因容量上限触发提前淘汰
- **AND** 已过期条目 SHALL 由 Caffeine 自动回收，缓存总量受过期时间与 objectId 基数共同约束
