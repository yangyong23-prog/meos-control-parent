## Why

当前壳配置系统已覆盖自动化需求计算、控制逻辑、室外天气数据来源、关系图、半剖图、报告等多种场景，但缺少对"云端持久化数据查询规则"的配置能力。

根据 260901 迭代 PRD 需求，需在壳配置中新增"云端持久化数据显示规则"左侧分组，支持用户配置持久化数据的查询逻辑：
1. **持久化数据查询逻辑**：控制查询同一时刻点数据还是查询距离当前最新的数据
2. **距离当前的时间间隔**：配合"查询距离当前最新的数据"模式，限制查询时间范围

这两个参数决定了前端页面展示云端持久化数据时的取数策略，是数据展示准确性的关键配置。

## What Changes

- 新增 `persistent_data_rule` 壳配置场景（`ShellConfigSceneEnum`），独立于现有 11 个场景
- 新增 `shell_config_persistent_data_rule` 基础配置类型（`BasicsConfigTypeEnum`），用于数据库存储区分
- 新增 `group_persistent_data_rule` 左侧分组（`ShellConfigGroupEnum`），无 `systemType` 关联
- 新增 2 个壳配置项（`ShellConfigItemEnum`）：`persistent_data_query_logic`（单选）和 `persistent_data_time_interval`（数字输入）
- 新增 i18n 国际化键 `key_shell_config_persistent_data_rule`，覆盖中文和英文资源文件

## Capabilities

### New Capabilities
- `persistent-data-display-rule`: 云端持久化数据显示规则壳配置，支持配置查询逻辑（同一时刻点 / 距离当前最新）和时间间隔（1-168h），数据存储复用现有 `rc_basics_config` 表，查询/保存路径与 `weather` 场景完全一致

### Modified Capabilities
<!-- 无现有能力的需求变更，均为新增能力 -->

## Impact

- **修改文件**（纯新增枚举项，不改动现有逻辑）:
  - `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigSceneEnum.java`（+1 枚举项 +1 switch case）
  - `common/src/main/java/com/persagy/apccontrol/basic/model/base/BasicsConfigTypeEnum.java`（+1 枚举项）
  - `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigGroupEnum.java`（+1 枚举项）
  - `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigItemEnum.java`（+2 枚举项）
  - `common/src/main/resources/i18n/messages_zh.properties`（+1 i18n 键）
  - `common/src/main/resources/i18n/messages_en.properties`（+1 i18n 键）

- **无影响**:
  - `ShellConfigServiceImpl.java`：查询/保存/获取配置值逻辑无需修改，新场景自动走 `else` 简单值合并分支（与 `weather` 场景相同路径）
  - `ShellConfigController.java`：查询/保存接口完全通用，无场景特定路由
  - `ShellConfigItemExtendEnum.java`：简单场景（单分组无子分区）不需要注册
  - `ShellConfigTabEnum.java`：tab 名直接内嵌在 item 定义中
  - 数据库表结构无变更（复用 `rc_basics_config` 表）
