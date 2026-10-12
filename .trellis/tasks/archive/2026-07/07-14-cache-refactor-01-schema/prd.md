# 缓存表结构与输入主键基础

## Goal

先交付并审核七张缓存表的云端、边缘端一致结构与输入型字符串主键基础能力，为后续持久化任务提供稳定契约，不接入任何生产读写链路。

## Dependencies

- 父任务：`07-13-cache-object-model-refactor`。
- 本任务是第 1 个审核单元，无前置子任务。

## Requirements

- 新增 `BaseInputIdDO`，字符串业务主键由调用方输入，`clearSaveParameter()` 不得清空 `id`，只清理创建和更新审计字段。
- 云端和边缘端同版本、同内容的 Flyway 创建 `cache_object`、`cache_object_key_rel`、`cache_object_rel`、`cache_object_model_view`、`cache_template_object_detail`、`cache_rule`、`cache_rule_key_rel`。
- 字段、长度、JSON 类型、审计字段和索引与父任务技术设计一致；对象和模型视图使用对象 ID 主键，其他表使用稳定 bigint ID。
- 不新增外键和业务组合唯一约束；索引必须覆盖后续按项目、缓存键、对象、关系和规则业务键查询的前缀。
- 本项目同时提供云端和边缘端迁移；云边同步登记、数据传输和同步完成感知由同步项目组负责。

## Out Of Scope

- 不新增七张表的 Mapper、Repository 或业务 Service。
- 不修改监听器、XXL-JOB、Redis 状态或任何读取消费者。
- 不接入末端、照明生产链路。

## Acceptance Criteria

- [x] `BaseInputIdDO` 的保存参数清理测试证明输入 ID 被保留。
- [x] 七张表的主键、字段和索引可由静态审查及 Flyway 校验确认，云端和边缘端迁移内容完全一致。
- [x] 公共模块编译通过，且没有生产读写行为变化。
- [x] 本任务 diff 只包含表结构、基础实体能力及其聚焦测试。

## Review Gate

完成实现和质量检查后暂停，等待用户审核表结构与基础主键设计；审核通过前不得开始子任务 02。
