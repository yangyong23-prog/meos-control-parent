# 缓存表 floorId/buildingId 多值改存第一个

## Goal

`cache_object` 与 `cache_object_model_view` 的 `floor_id`/`building_id` 独立列只承载"单一主归属"语义。RWD 对个别设备类会返回多个 ID，修订上次"多值不落列（存 null）"的行为：改为**取第一个值**写入独立列，对全部 classCode 生效。

## Background

- 上次提交 `5a2bc6050d69ff12a919b8bbd07ef24dda27ca68`（任务 `09-16-cache-floor-building-multi-value-skip`）将多值场景改为置空不落列；本次为需求变更，只调整两个转换器的统一读取方法。
- 两个独立列的唯一写入点为 `CacheObjectRecordConverter#toCacheObject` 与 `CacheObjectModelViewRecordConverter#toModelView` 中的 `readSingleValue`。
- `buildingId`/`floorId` 已在 `CacheObjectExtensionFieldConfig#FORBIDDEN_EXTENSION_FIELDS` 中，不写入 JSON；独立列是唯一存储来源，`upsertBatch` 使用 `on duplicate key update`，重建后存量数据自然覆盖，无需单独修复。
- 需要兼容的两种多值结构：英文逗号分隔字符串 `"id1,id2"`、JSON 数组 `["id1","id2"]`；另有单值字符串与字段缺失（null）场景保持原有行为。

## Requirements

1. 取值规则（统一收敛到两个转换器的 `readSingleValue`，只兼容两种多值结构）：
   - 值为 `null` → 写入 `null`；
   - 逗号分隔字符串 / JSON 数组（判定：trim 后含英文逗号，或以 `[` 起头）：先 `String.valueOf`，按**首个英文逗号截断**取第一段，再剥离该段首尾的数组/引号/空白装饰（`[`、`]`、`"`、空格），得到第一个值写入；
   - 单值与其余场景保持原有行为原样写入：不含逗号的单值字符串、空串；字段缺失（null）时为 `null`。
2. `floorId` 与 `buildingId` 各自独立判断，互不影响。
3. 规则对全部 classCode 生效，不按设备类白名单区分；不修改查询端逻辑（如 `CacheObjectModelViewQueryDaoParam.floorIds/buildingIds`、安防策略按 floorId 过滤）。
4. 更新两个测试类：多值场景从"断言 null"改为"断言等于第一个值"，覆盖逗号分隔字符串（含空格）、JSON 数组（多元素/单元素）、单值、字段缺失。
5. 更新规范文档 `.trellis/spec/backend/cache-object-model.md` 中描述该行为的 3 处（规则条目、行为对照表行、检查清单项），并同步两个转换器方法注释。

## Acceptance Criteria

- [x] RWD 返回 `"id1,id2"` 时两表对应列写入 `id1`；JSON 数组 `["id1","id2"]` 与单元素 `["id1"]` 均写入 `id1`
- [x] 单值 `"id1"` 原样写入；字段未返回（null）时为 SQL `NULL`；空串保持原样写入
- [x] `floorId` 多值不影响 `buildingId` 写入（反向同理）
- [x] `CacheObjectRecordConverterTest`、`CacheObjectModelViewRecordConverterTest` 更新并通过（common 模块）
- [x] `mvn -pl common compile` 编译通过，相关测试执行通过
- [x] 规范文档 3 处描述与实现行为一致

## Notes

- 行为变化：多值对象将按"第一个值"被楼层/建筑过滤查询命中（此前置空导致不可命中），属本次修订的预期效果。
- 存量数据由下一次缓存重建自然覆盖，无需数据修复。
- 实现不引入 JSON 反序列化解析（不新增 Jackson 依赖）：首段截断 + 装饰剥离对两种结构均成立，且避免缓存热路径的解析开销；单值、空串等非两种结构的输入不进入该处理分支。
