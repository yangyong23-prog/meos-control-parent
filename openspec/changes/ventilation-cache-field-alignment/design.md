## Context

`ventilation-cache-object-model`（已 complete，未归档）将 VENTILATION 缓存切换到新缓存对象模型。验证时发现写入侧 `buildCacheData()` 的 RWD 查询列为手写集合，与 `CacheObjectExtensionFieldConfig` 白名单不对齐，导致白名单允许的字段（SPACE 的 `bimId`/`bimLocation`/`outline`，设备的 `bimId`）在 RWD 查询阶段就没有请求，落库 JSON 中缺失。

字段流转链路（缺失点在 ①）：

```
① 写入策略 withColumns（手写，缺列）
      ↓ Feign 查 RWD，DTO 只含被请求的列
② replaceModelCacheKey → prepareScope() 直接序列化传入 DTO（不补查）
      ↓
③ 白名单投影（只做"允许"，不做"补查"）
      ↓
④ objectData / cache_object_model_view JSON 落库（缺字段）
```

对照证据：

- 旧缓存写入 `cacheDashboardSpaceData()` 的空间列明确包含 `bimLocation`、`outline`，且该列集合按前端消费配置
- 旧读取路径将 `param.getWithColumns()` 合并进查询列，新读取路径固定返回缓存列，写入列不补齐则前端无法获取
- 末端和照明写入侧均使用 `listRwdQueryFields(classCode)` 组列，通风是唯一手写列集合的新模型策略

## Goals / Non-Goals

**Goals:**

- VENTILATION 写入查询列与白名单对齐，READY 后空间数据包含 `bimId`/`bimLocation`/`outline`，设备数据包含 `bimId`
- 组列方式与末端/照明统一为 `listRwdQueryFields()`，消除手工维护脱节风险
- 将"写入列 SHALL 基于 listRwdQueryFields 组装"固化为 spec 要求

**Non-Goals:**

- 不修改 `CacheObjectExtensionFieldConfig` 白名单内容
- 不修改读取路径和关系写入逻辑
- 不恢复读取路径对 `param.getWithColumns()` 的透传（缓存列补齐后固定列已覆盖前端需求）
- 不处理其他类型（末端/照明已对齐，无需改动）

## Decisions

### D1: 组列方式用 listRwdQueryFields 并集，不用手工补列

**选择**: 空间列 = `listRwdQueryFields(SPACE)` ∪ 业务追加列；设备列 = `listRwdQueryFields(FFSCMF)` ∪ `listRwdQueryFields(FFSCAF)` ∪ `listRwdQueryFields(ACVTSF)` ∪ 业务追加列。

**原因**: 与末端（`CacheRwdAirTerminalWhiteListStrategy` L259）和照明（`CacheRwdLightingStrategy` L99）的既有范式一致；白名单后续演进时查询列自动跟随，不会再次脱节。

**备选方案**: 在手写集合里补 3 个字段 → 最小改动但保留脱节根因，下次白名单加字段仍会漏，放弃。

### D2: 业务追加列保留 name 和 relObjIds

**选择**: `listRwdQueryFields()` 并集之外，空间追加 `name`，设备追加 `name`、`relObjIds`。

**原因**: `RWD_QUERY_BASE_FIELDS` 含 id/localId/localName/classCode/objType/buildingId/floorId/monitoringSurveillanceStatus，但不含 `name` 和 `relObjIds`；`relObjIds` 供设备缓存 DTO 兼容旧消费方（`mapRelObjIdAndData`），`name` 为现有查询列，维持字段不缩水。

### D3: 三类设备共用一个查询列并集

**选择**: FFSCMF/FFSCAF/ACVTSF 三类的白名单字段取并集，作为 `getDtpRwdEquipmentDTOS` 统一的 `equipmentWithColumns` 传入。

**原因**: 现有 `listEquipmentsForCacheBuild(spaceIds, equipmentWithColumns)` 签名接受单一列集合，三路并行查询共用；多查的列（如 FFSCMF 查 bimId 但其白名单为空）在投影阶段被丢弃，无副作用。保持签名不变，改动最小。

### D4: 发布后必须触发一次缓存重建

**选择**: 代码发布后通过 XXL-JOB 手动触发或等待下一次 DTX 事件触发 VENTILATION 缓存重建。

**原因**: `replaceModelCacheKey` 是全量替换语义，重建一次即可补齐存量数据；重建前存量缓存字段仍缺失，属可接受的过渡窗口。

## Risks / Trade-offs

- **[outline 字段较大]** → SPACE 白名单本就允许 outline，末端 SPACE 缓存已同样存储，存储量与末端持平，可接受。
- **[重建前过渡窗口]** → 重建前 READY 读取仍缺字段，与当前线上状态一致，不构成新增回归；重建后自动修复。
- **[三类并集多查列]** → FFSCMF 白名单为空但会随并集查询 bimId/bimLocation，投影阶段丢弃，仅多传输少量字段，无正确性影响。
