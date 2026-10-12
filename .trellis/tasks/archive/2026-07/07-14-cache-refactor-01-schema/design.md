# 缓存表结构与输入主键基础技术设计

## 1. Scope and Boundary

本任务只建立后续缓存对象模型需要的数据库和基础主键契约：

- 公共模块新增 `BaseInputIdDO`，用于调用方提供字符串业务主键的实体。
- 云端和边缘端各新增一份同版本、同内容的 Flyway 迁移，创建七张缓存表及查询索引。
- 不新增七张表实体、Mapper、Repository、Service，不接入生产读写。
- 不处理云边同步登记、数据传输或完成感知。

## 2. BaseInputIdDO Contract

`BaseInputIdDO` 位于现有 `com.persagy.apccontrol.basic.model.base` 包，继承 `BaseDO`：

- `id` 类型为 `String`。
- 使用 `@TableId(value = "id", type = IdType.INPUT)`，主键由业务调用方输入。
- `clearSaveParameter()` 复用 `BaseDO` 的创建和更新审计字段清理，不修改 `id`。
- Lombok、序列化和继承对象方法与 `BaseAssignIdDO` 保持一致。

聚焦单元测试设置 ID 与四个审计字段，调用 `clearSaveParameter()` 后验证 ID 保留、审计字段清空，并验证返回当前实例。

## 3. Cloud and Edge Flyway Migration

迁移文件固定为两份：

`saas/meos-control-server/src/main/resources/db/flyway/meos_control/V7.260701.0.1__cache_object_model.sql`

`local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/V7.260701.0.1__cache_object_model.sql`

两份脚本内容完全一致。所有表使用项目现有的 `create table if not exists` 和 `add_table_index` 迁移风格，不增加外键或业务组合唯一约束。

### 3.1 Shared Column Rules

- `project_id varchar(50) not null`。
- 对象、关系、规则引用 ID 使用 `varchar(64)` 或 `bigint`，与来源主键一致。
- 编码字段：`class_code`、`object_type`、规则 `type` 使用 `varchar(32)`；`graph_code`、`rel_code`、`info_code`、运行监测类型使用 `varchar(64)`。
- 缓存键和模板键使用 `varchar(255)`。
- 审计字段统一为 `create_time datetime(6) not null`、`create_user varchar(64) null`、`update_time datetime(6) not null`、`update_user varchar(64) null`。
- 扩展数据使用 MySQL `json` 类型；对象和视图扩展 JSON 允许历史脏数据为 SQL `NULL`，业务写入阶段将保证正常值为 JSON 对象。

### 3.2 Tables and Indexes

#### `cache_object`

- 字段：`id varchar(64)`、`project_id`、`local_name varchar(100)`、`class_code`、`object_type`、`floor_id varchar(64)`、`building_id varchar(64)`、`monitoring_surveillance_status json`、`object_data json`、审计字段。`monitoring_surveillance_status` 为后续子任务 02 评审确认的多 code 独立列。
- 主键：`id`。
- 索引：`idx_project(project_id)`、`idx_building(project_id, building_id)`、`idx_floor(project_id, floor_id)`。

#### `cache_object_key_rel`

- 字段：`id bigint`、`project_id`、`cache_key varchar(255)`、`object_id varchar(64)`、审计字段。
- 主键：`id`。
- 索引：`idx_cache_key(project_id, cache_key)`。

#### `cache_object_rel`

- 字段：`id bigint`、`project_id`、`object_id varchar(64)`、`graph_code varchar(64)`、`rel_code varchar(64)`、`rel_object_id varchar(64)`、审计字段。
- 主键：`id`。
- 索引：`idx_object(project_id, object_id, graph_code, rel_code)`、`idx_rel_object(project_id, rel_object_id, graph_code, rel_code)`。

#### `cache_object_model_view`

- 字段：`id varchar(64)`、`project_id`、`local_name varchar(100)`、`class_code`、`object_type`、`building_id varchar(64)`、`floor_id varchar(64)`、`view_data json`、审计字段。
- 主键：`id`。
- 索引：`idx_project(project_id)`、`idx_building(project_id, building_id)`、`idx_floor(project_id, floor_id)`。

#### `cache_template_object_detail`

- 字段：`id bigint`、`project_id`、`template_key varchar(255)`、`object_id varchar(64)`、`info_code varchar(64)`、`result_json json not null`、审计字段。
- 主键：`id`。
- 索引：`idx_template_object_info(project_id, template_key, object_id, info_code)`。

#### `cache_rule`

- 字段：`id bigint`、`project_id`、`object_id varchar(64)`、`info_code varchar(64)`、`type varchar(32)`、审计字段。
- 主键：`id`。
- 索引：`idx_project_object_rule(project_id, object_id, info_code, type)`。

#### `cache_rule_key_rel`

- 字段：`id bigint`、`project_id`、`cache_key varchar(255)`、`rule_id bigint`、`running_monitor_type varchar(64)`、`running_monitor_second_type varchar(64) null`、审计字段。
- 主键：`id`。
- 索引：`idx_cache_key(project_id, cache_key)`、`idx_rule(project_id, rule_id)`；后者用于删除缓存键后批量确认规则是否仍被其他键引用。

## 4. Compatibility and Rollback

- 新表没有生产消费者，迁移落库后不会改变现有读写行为。
- 迁移不修改旧表，不影响 `cache_rwd_object` 和 `cache_iot_rule`。
- 若后续任务未启用，新表可保持空表；本任务不提供删表回滚脚本。
- 本任务保证云端和边缘端结构一致；同步接入、传输顺序和完成感知由其他项目组交付。

## 5. Validation

- 运行 `BaseInputIdDOTest` 验证输入主键清理契约。
- 编译 `common` 模块。
- 静态核对迁移中的七张表、主键、JSON 字段、审计字段和全部索引。
- 检查迁移文件同时存在于云端和边缘端模块、内容完全一致，且没有 Mapper、仓储或生产消费者变更。
