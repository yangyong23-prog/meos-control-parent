# 缓存表 floorId/buildingId 多值时不落独立列

## Goal

`cache_object` 与 `cache_object_model_view` 的 `floor_id`/`building_id` 独立列仅承载"单一楼层/建筑归属"语义。RWD 对个别设备类会返回逗号分隔的多个 ID，此时该字段不写入独立列（存 null），对全部设备类生效。

## Background

- 两个独立列的唯一写入点为 `CacheObjectRecordConverter#toCacheObject` 与 `CacheObjectModelViewRecordConverter#toModelView`，当前从 RWD 实际字段原样读取，不做格式判断。
- `buildingId`/`floorId` 已在 `CacheObjectExtensionFieldConfig#FORBIDDEN_EXTENSION_FIELDS` 中，不会写入 `object_data`/`view_data` JSON；独立列是唯一存储来源，置空后重建（`toRwdObject`）时该字段即为 null。
- `upsertBatch` 使用 `on duplicate key update`，null 会正常覆盖旧值，缓存重建后自动修正存量数据。

## Requirements

1. 判定规则：RWD 可能返回单值字符串、逗号分隔字符串或 JSON 数组三种形态；字段值只要包含英文逗号即判定为多值。
2. 多值处理：该字段不写入独立列（置 null）。
3. 非多值场景保持现有行为、原样写入：不含逗号的单值字符串、空串；字段缺失（null）时为 null。
4. `floorId` 与 `buildingId` 各自独立判断，互不影响。
5. 规则对全部 classCode 生效，不按设备类白名单区分。
6. 不修改查询端（`CacheObjectModelViewQueryDaoParam.floorIds/buildingIds` 等）逻辑。

## Acceptance Criteria

- [ ] RWD 返回 `"id1,id2"` 或 JSON 数组 `["id1","id2"]` 时，两表对应字段独立列写入 null
- [ ] RWD 返回不含逗号的单值 `"id1"` 时原样写入；字段未返回（null）时行为不变
- [ ] `floorId` 多值仅置空 floorId，`buildingId` 单值仍正常写入（反向同理）
- [ ] 单元测试覆盖上述场景并通过（common 模块）
- [ ] `mvn -pl common compile` 编译通过

## Notes

- 行为变化：多值对象不再被按楼层/建筑过滤的查询命中（如安防策略 `equals(s.getFloorId(), floorId)`），属预期效果。
- 存量多值数据由下一次缓存全量重建自然覆盖，无需单独数据修复。
