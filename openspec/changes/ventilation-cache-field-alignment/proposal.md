## Why

`ventilation-cache-object-model` 迁移中，`CacheRwdVentilationStrategy.buildCacheData()` 的 RWD 查询列集合为手写硬编码，与 `CacheObjectExtensionFieldConfig` 白名单不对齐：

- 空间查询列缺少 `bimId`、`bimLocation`、`outline`（SPACE 白名单允许但未查询）
- 设备查询列缺少 `bimId`（ACVTSF/FFSCAF 白名单允许但未查询）

白名单投影只做"允许"不做"补查"（`prepareScope()` 直接序列化传入 DTO，不重新查 RWD），未查询的列在 `objectData`/`cache_object_model_view` JSON 中即为缺失。旧缓存写入 `cacheDashboardSpaceData()` 明确携带 `bimLocation`、`outline`，说明前端值班首页消费这些字段；READY 后前端拿到的空间数据缺少轮廓/定位字段，构成功能回归风险。

根因是通风写入策略未复用框架提供的 `listRwdQueryFields(classCode)`（公共字段+对象白名单+模型视图白名单的并集），而末端（`CacheRwdAirTerminalWhiteListStrategy`）和照明（`CacheRwdLightingStrategy`）均使用该方法自动对齐。

## What Changes

- `CacheRwdVentilationStrategy.buildCacheData()` 空间查询列改为 `listRwdQueryFields(SPACE)` 并集 + 业务追加列（`name`、`roomFuncType` 已含于白名单）
- 设备查询列改为 `listRwdQueryFields(FFSCMF/FFSCAF/ACVTSF)` 三类并集 + 业务追加列（`name`、`relObjIds`）
- 固化规范：新缓存对象模型写入策略的 RWD 查询列 SHALL 基于 `listRwdQueryFields()` 组装，禁止手写与白名单脱节的列集合
- 发布后触发一次 VENTILATION 缓存重建，使补齐字段落库

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `cache`: 新增"新缓存写入查询列与白名单对齐"要求，VENTILATION 写入列补齐 bimId/bimLocation/outline

## Impact

- **`CacheRwdVentilationStrategy`**: 唯一代码改动点，仅调整 `buildCacheData()` 中两个 withColumns 集合的组装方式
- **读取路径、白名单配置、关系写入**: 不改动
- **数据**: 需触发一次 VENTILATION 缓存重建（XXL-JOB 或 DTX 事件），重建前存量缓存字段仍缺失
- **性能**: 查询列增加 3 个字段，Feign 次数不变，影响可忽略
