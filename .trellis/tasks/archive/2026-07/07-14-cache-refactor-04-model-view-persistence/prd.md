# 模型视图缓存持久化

## Goal

交付 `cache_object_model_view` 的独立持久化与查询仓储，使模型图数据可只访问视图表，不接入生产消费者。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 前置审核：子任务 01 至 03 已通过。

## Requirements

- 新增模型视图实体、Mapper、XML 批量 UPSERT 和 Repository/Service 契约。
- 以对象 ID 为主键，独立保存 `project_id`、`local_name`、`class_code`、`object_type`、`building_id`、`floor_id` 等公共列。
- `view_data` 使用子任务 02 的基础字段与 classCode 增强字段并集；设备默认保存 `bimId`、`bimLocation`，空间默认保存 `bimId`、`bimLocation`、`outline`，并叠加 classCode 增强字段。
- 提供 RWD 实际字段到模型视图实体、模型视图实体到 RWD DTO 的转换能力；独立公共列优先于 `view_data`，扩展 JSON 不得覆盖公共列。
- 支持按项目、对象 ID、建筑和楼层进行批量/按需列查询，仓储不得 JOIN `cache_object` 或加载 `object_data`。
- 为后续对象与视图同事务 UPSERT 提供明确的批量仓储边界。
- 本子任务不新增测试代码；测试和完整验证统一在最终集成子任务执行，本阶段只做编译与静态检查。

## Out Of Scope

- 不修改对象缓存事件或模型图消费者。
- 不实现对象关系、模板明细、规则或 READY 路由。
- 不在本任务做云端双写编排。

## Acceptance Criteria

- [x] 公共列与 `view_data` 可重建旧模型图需要的数据，扩展字段不能覆盖公共列。
- [x] 设备/空间默认字段和 classCode 增强字段的持久化、空增强 Map 行为已实现。
- [x] 模型视图 UPSERT、建筑/楼层过滤和按需投影已实现。
- [x] 查询 SQL 不 JOIN `cache_object`，不读取 `object_data`。
- [x] 本任务 diff 只包含模型视图持久化，不新增生产接线或测试代码。

## Review Gate

完成实现和质量检查后暂停，等待用户审核模型视图表和无 JOIN 查询；审核通过前不得开始子任务 05。
