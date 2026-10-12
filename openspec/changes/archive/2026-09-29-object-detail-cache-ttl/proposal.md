## Why

`LocalCacheConstant.OBJECT_DETAIL_MAP` 缓存中台对象的静态字段（localName、classCode 等），当前实现为普通 `ConcurrentHashMap`，进程存活期内永不过期、永不更新。当 bdtp 侧静态数据变更（例如设备 localName 改名）后，缓存里的旧值不会刷新，导致运行日志、控制链路等展示的名称与实际对不上，目前唯一恢复手段是重启服务清空缓存。

## What Changes

- 将 `LocalCacheConstant.OBJECT_DETAIL_MAP` 由 `ConcurrentHashMap` 替换为 Caffeine 缓存，写入后 2 小时自动过期（`expireAfterWrite`），过期后下一次访问回源重查 bdtp，从而让静态数据变更在最多 2 小时内自动收敛，无需重启。
- TTL 固定为 2 小时，写死在代码中，不做配置化。
- 不设置 `maximumSize`（过期机制已兜住总量，保持改动最小）。
- `LocalRedisCacheHandler` 中 3 处 get/put 调用点适配 Caffeine 接口（get/put 语义与调用形态基本一致）。
- 缓存条目的「就地富化」语义（put 之后再 set `systemId`/`systemType`）保持不变；富化逻辑本身不在本次变更范围内，过期后随缓存重建自然重跑。

## Capabilities

### New Capabilities
- `object-detail-cache-ttl`: 定义 `OBJECT_DETAIL_MAP` 对象静态字段本地缓存的过期与回源行为，包括 TTL 时长、过期策略、富化字段重建以及回源失败时的降级表现。

### Modified Capabilities
<!-- 无：本次仅涉及本地内存缓存 OBJECT_DETAIL_MAP，不改变现有 cache/redis-cache-spec 的 Redis 使用规范需求 -->

## Impact

- 代码：
  - `common/src/main/java/com/persagy/apccontrol/basic/constant/LocalCacheConstant.java`（`OBJECT_DETAIL_MAP` 声明类型）
  - `common/src/main/java/com/persagy/apccontrol/basic/handler/LocalRedisCacheHandler.java`（L113/L208/L220 三处调用点）
- 依赖：复用 common 模块已有的 Caffeine 依赖，不引入新库。
- 运行时行为：每个活跃对象每 2 小时会多触发 1~2 次 bdtp Feign 回源（对象详情 + 关系查询）；各条目按各自写入时间错开过期，无同步尖峰。
- 已知代价（既有行为，非本次引入）：回源时若 bdtp/Feign 抖动导致查询失败，`getObjectBusinessFeild` 吞异常返回 null，`RunningLogHandle` 等调用方会静默丢弃该条数据；TTL 将该失败窗口的出现频率从「每对象一生一次」提高到「每对象每 2 小时一次」，本次不做降级改造。
