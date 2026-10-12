# 缓存表结构与输入主键基础实施计划

## 1. Base Input ID

- [x] 在基础模型包新增 `BaseInputIdDO`，使用字符串 `IdType.INPUT` 主键。
- [x] 覆盖 `clearSaveParameter()`，保留输入 ID 并复用父类审计字段清理。
- [x] 新增聚焦单元测试，覆盖 ID、创建审计字段、更新审计字段和返回实例。

## 2. Cloud and Edge Schema

- [x] 在云端和边缘端分别新增同版本、同内容的 Flyway `V7.260701.0.1__cache_object_model.sql`。
- [x] 创建 `cache_object`、`cache_object_key_rel`、`cache_object_rel`。
- [x] 创建 `cache_object_model_view`、`cache_template_object_detail`。
- [x] 创建 `cache_rule`、`cache_rule_key_rel`。
- [x] 使用 `add_table_index` 建立查询索引，包含规则关联的缓存键查询和 `rule_id` 引用检查。
- [x] 确认没有外键、业务唯一约束或旧表修改，并确认两端迁移内容一致。

## 3. Validation

- [x] 运行 `BaseInputIdDOTest`。
- [x] 运行 `mvn -pl common -DskipTests compile`。
- [x] 静态检查七张表字段、长度、空值、审计字段、主键和索引。
- [x] 运行 `git diff --check` 并检查本任务范围。
- [x] 使用 `trellis-check` 完成规范与质量验证。

## Review and Stop Point

完成以上实现和验证后停止，不启动子任务 02；向用户提交 `BaseInputIdDO`、Flyway 表结构和验证结果供逐项审核。
