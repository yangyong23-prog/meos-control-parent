## Context

末端（AIR_TERMINAL）和照明（LIGHTING）已完成新缓存对象模型切换。VENTILATION 是最后一个仍在使用旧表 `cache_rwd_object` 的一级类型。

当前 VENTILATION 缓存存在两个问题：

1. **写入性能差**：`buildCacheData()` 需 12 次 Feign 调用（空间查询 1 + 设备查询 3 + 需求配置 4 + 空调季 1 + 重复空间/设备查询 3）
2. **复合 VO 存储不兼容新模型**：旧缓存存储 `VentilationDtpRwdSpaceVO`（内嵌设备列表+需求配置），新缓存的 SPACE classCode 白名单只允许 `{roomFuncType, bimId, bimLocation, outline}`，复合 VO 的 `relObjects`、`tdbQuality`、`o2Target` 等业务字段会被投影丢弃

## Goals / Non-Goals

**Goals:**

- VENTILATION 空间缓存和设备缓存切换到新缓存对象模型
- 写入 Feign 调用从 12 次降至 4 次
- 读取缓存命中时（99%）0 Feign，与旧方案持平
- 读取缓存未命中时（1%）从 10 Feign 降至 5 Feign
- 不修改业务逻辑（`queryEquipGroupId` 消费方式不变）
- 信息点配置/模板重建自动切换

**Non-Goals:**

- 不废除 `cacheDashboardSpaceData` 写入逻辑（本次保留，后续可独立删除）
- 不优化需求配置查询本身（`getStringMapMap` 的 4 次 Feign 仍在读取侧，由 Redis 短缓存兜底）
- 不修改 IoT 聚合接口调用逻辑

## Decisions

### D1: 存原始 DTO，不存复合 VO

**选择**: 存储 `DtpRwdSpaceDTO` 和 `DtpRwdEquipmentDTO`，不存 `VentilationDtpRwdSpaceVO`

**原因**: `DtpRwdSpaceDTO` 的所有字段（id, classCode, localName, floorId, buildingId, roomFuncType, bimLocation, outline）都在 SPACE 白名单或独立列中，不丢字段。复合 VO 的业务字段（relObjects, tdbQuality, o2Target 等）在读取时实时查询组装。

**备选方案**:
- 直接存复合 VO → 字段投影丢弃业务数据，不可行
- 扩展 SPACE 白名单 → 会污染所有 SPACE 类型对象（末端也在用），跨键 upsert 冲突

### D2: 设备→空间关系存入 cache_object_rel

**选择**: 写入设备缓存时，通过 `replaceModelCacheKey` 的 `relationQueryParams` 参数传入 `EQUIPMENT_SERVICE_SPACE` 关系查询，关系数据写入 `cache_object_rel` 表。读取时用 `CacheObjectRelRepository.listReverse()` 按空间 ID 反查关联设备。

**原因**: 关系存储是新缓存模型的原生能力，读取时 0 Feign（本地 DB 查询），且写入时自动管理关系生命周期。

### D3: 需求配置和空调季移至读取侧

**选择**: `getStringMapMap()`（4 Feign）和 `getAirSeasonType()`（1 Feign）从写入侧移到读取侧，由 `redisCacheTemplate` 短缓存兜底。

**原因**:
- 旧方案在写入时查需求配置，构建间隔内配置变更不会反映到缓存
- 新方案在读取时实时查，Redis TTL 过期后获取最新配置，实时性更好
- 99% 请求走 Redis 短缓存，0 Feign
- 1% 未命中时 5 Feign，仍比旧方案的 10 Feign 快 50%

### D4: 缓存键命名

**选择**: `VENTILATION_LIST_SPACE = "VENTILATION_listSpace"`, `VENTILATION_LIST_EQUIPMENT = "VENTILATION_listEquipment"`

**原因**: 与末端 `AIR_TERMINAL_listSpace` / `AIR_TERMINAL_listEquipment` 命名规则一致。

### D5: 写入时做 filtrationId 过滤

**选择**: 写入缓存前执行 `filtrationId()` 过滤挑空顶层设备，存入的是过滤后的设备列表。

**原因**: 读取时不需要重新过滤，直接使用缓存数据。过滤逻辑在写入时一次完成。

## Risks / Trade-offs

- **[Redis 未命中时多 5 Feign]** → 频率极低（~1%），且新方案 5 Feign vs 旧方案 10 Feign，反而更快。Redis TTL 可调。
- **[需求配置实时性变化]** → 旧方案构建间隔内可能过期，新方案 Redis TTL 过期后实时查，更新鲜。这是改善而非风险。
- **[FFSCMF 白名单为空]** → `OBJECT_EXTENSION_FIELDS` 中 FFSCMF 未配置，objectData 为空 JSON。`queryEquipGroupId` 只用 id + classCode，不需要扩展字段，安全。
- **[cacheDashboardSpaceData 后续废除]** → 本次保留写入逻辑不动，设备缓存写入独立。后续可独立删除，不影响设备缓存。
