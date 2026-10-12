## Context

### 现有架构

壳配置（Shell Config）系统采用 **Scene → Group → Tab → Items** 四层枚举驱动架构，所有配置通过枚举类定义，无需数据库表结构变更：

```
ShellConfigSceneEnum（11个场景）
    ↓ getListByScene() 按 scene 过滤
ShellConfigGroupEnum（60+分组）
    ↓ 按 group 分组
ShellConfigItemEnum（800+配置项）
    ↓ convert() 转换为 ShellConfigItem 实体
ShellConfigController → ShellConfigServiceImpl
    ↓ query(): 获取默认项 + 查询已保存值 + getConfigValueByScene() 合并
    ↓ save(): convertToBasicConfigTypeByScene() 转换类型 → JSON 序列化存入 rc_basics_config 表
```

**关键代码路径（以 weather 场景为参照）**：

1. **查询路径** `ShellConfigServiceImpl.query()` (L144-182)：
   - 调用 `ShellConfigItemEnum.getListByScene(scene)` 获取默认配置项
   - 调用 `ShellConfigGroupEnum` 按 group 分组组装
   - 调用 `getConfigValueByScene()` 将已保存的值合并到默认项

2. **保存路径** `ShellConfigServiceImpl.save()` (L1210-1229)：
   - 调用 `ShellConfigSceneEnum.convertToBasicConfigTypeByScene(scene)` 转换为 `BasicsConfigTypeEnum`
   - 将配置项序列化为 JSON 存入 `rc_basics_config` 表的 `result` 字段

3. **值合并路径** `getConfigValueByScene()` (L191-588)：
   - 有 `systemType` 的 group → 走实例查询分支（查询系统列表、按实例合并）
   - 无 `systemType` 的 group → 走 `else` 简单值合并分支 (L557-578)：直接按 code 匹配合并值

4. **类型列表** `BasicsConfigTypeEnum.getList()` (L141-152)：
   - 遍历所有枚举值（排除 `CACHE_OBJECT_MODEL_STATUS`），返回给前端
   - 新增类型自动出现在列表中

### 约束条件

1. **枚举驱动扩展**：新增场景必须通过枚举项定义，不修改任何现有枚举值
2. **无 systemType 的简单场景**：新场景的 group 不关联系统实例，走 `else` 简单值合并路径
3. **条件显示前端处理**：参数 2（时间间隔）仅在参数 1 为"查询距离当前最新的数据"时显示，此条件显示逻辑由前端自行处理，后端不需要编码依赖关系
4. **不涉及消费侧**：本变更仅涉及壳配置的存储和查询，不涉及配置的消费逻辑

## Goals / Non-Goals

**Goals:**
- 新增 `persistent_data_rule` 场景，包含 1 个分组和 2 个配置项
- 完全复用现有查询/保存/值合并代码路径，零逻辑改动
- 遵循枚举驱动扩展模式，参照 `weather` 场景实现

**Non-Goals:**
- 不修改 `ShellConfigServiceImpl` 的查询/保存/值合并逻辑
- 不修改 `ShellConfigController` 的接口路由
- 不引入 `ShellConfigItemExtendEnum` 子分区绑定（简单场景不需要）
- 不处理条件显示逻辑（前端职责）
- 不涉及消费侧逻辑（读取配置值并执行查询的业务逻辑）

## Decisions

### 1. 新建独立 Scene

**决策**：新增 `SCENE_PERSISTENT_DATA_RULE("persistent_data_rule", ...)`，不挂靠任何现有场景

**理由**：
- "云端持久化数据显示规则"是独立的功能域，与现有 11 个场景（alg/control/space/policy/weather/eff/graph/hsec/report/mpc/whitelist_space）语义不重叠
- 独立场景保证配置数据的独立存储（`rc_basics_config` 表中 `type` 字段为 `shell_config_persistent_data_rule`）
- 参照 `weather` 场景（2 个 item、无 systemType）作为参考实现

**替代方案**：
- ❌ 挂靠 `SCENE_WEATHER`：语义不匹配，且会导致 weather 场景的配置数据混入
- ❌ 挂靠 `SCENE_ALG`：alg 场景有大量 systemType 关联的 group，复杂度不匹配

### 2. Group 无 systemType

**决策**：`GROUP_PERSISTENT_DATA_RULE("group_persistent_data_rule", "云端持久化数据显示规则", "", false)`

**理由**：
- 持久化数据查询规则是项目级配置，不关联具体系统实例
- `systemType` 为空字符串，`getConfigValueByScene()` 自动走 `else` 简单值合并分支
- 与 `weather` 场景的 `GROUP_WEATHER_CHANGE("group_weather_change", ..., "", false)` 模式一致

**影响路径验证**：
- `getConfigValueByScene()` L557-578：无 systemType 的 group 遍历 items，按 code 匹配已保存值并合并
- 该分支不包含任何 scene 特定逻辑，对所有无 systemType 的 group 通用

### 3. 参数 1 valSource 使用 JSON 选项列表

**决策**：`persistent_data_query_logic` 的 `valSource` 设为 JSON 数组 `[{"name":"查询同一个时刻点的数据","value":"same_time"},{"name":"查询距离当前最新的数据","value":"latest"}]`

**理由**：
- 遵循现有模式，如 `ALG_COOLING_SHIELD_ALL_ENABLED` 的 `valSource` 格式：`[{"name":"屏蔽","value":"true"},{"name":"不屏蔽","value":"false"}]`
- 前端通过 `valSource` 渲染单选项，通过 `defaultValues` 回显默认值
- `valueType` 设为 `null`，与现有单选项配置一致

**替代方案**：
- ❌ 使用 relation 查询动态选项：选项固定且数量少，不需要动态查询

### 4. 参数 2 纯数字输入，valSource 为 null

**决策**：`persistent_data_time_interval` 的 `valSource` 设为 `null`，通过 `min`/`max`/`step` 约束输入范围

**理由**：
- 遵循现有数字输入模式，如 `ALG_PROJECT_POWER_ON` 的参数定义
- `min=1, max=168, step=1` 约束整数范围 1-168
- `unit="h"` 标识单位
- `defaultValues="1"` 对应 PRD 默认值 1h
- 条件显示（仅参数 1 为 latest 时显示）由前端处理，后端不在 `valSource` 中编码依赖关系

**替代方案**：
- ❌ 在 `valSource` 中编码 `{"dependOn":"persistent_data_query_logic","showWhen":"latest"}`：用户明确确认"条件显示后端不用管"

### 5. i18n 键命名

**决策**：新增 `key_shell_config_persistent_data_rule`，中英文各一条

**理由**：
- 遵循 `BasicsConfigTypeEnum` 的 explain 字段命名模式：`key_shell_config_<scene_name>`
- 参照现有 `key_shell_config_graph`、`key_shell_config_hsec` 等键的命名规律
- boundary 字段复用 `key_f64a1fbe41b54eb891c43d52e1ff6355`（所有 shell config 类型共享）

**i18n 值**：
- 中文：`key_shell_config_persistent_data_rule=云端持久化数据显示规则`
- 英文：`key_shell_config_persistent_data_rule=Cloud Persistent Data Display Rule`

### 6. convertToBasicConfigTypeByScene 新增 switch case

**决策**：在 `ShellConfigSceneEnum.convertToBasicConfigTypeByScene()` 的 switch 中新增 `case SCENE_PERSISTENT_DATA_RULE: return BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE;`

**理由**：
- 该方法是保存路径的核心映射，必须为每个场景提供映射
- 新增 case 不影响现有 case 的执行
- default 分支返回 null，新增 case 确保新场景能正确映射到数据库存储类型

## Risks / Trade-offs

### 1. 前端条件显示依赖约定

**风险**：参数 2 的条件显示逻辑完全由前端实现，后端 `valSource` 不包含依赖信息

**影响**：如果前端实现错误，可能导致参数 2 在不满足条件时仍然提交保存

**缓解**：
- 后端不校验条件显示关系，参数 2 无论参数 1 为何值都会被保存
- 这是可接受的行为：即使参数 1 为 `same_time` 时参数 2 有值，消费侧也不会使用（消费侧根据参数 1 的值决定是否读取参数 2）
- 与用户确认一致："条件显示后端不用管，是前端自己处理的"

### 2. 默认值回显

**风险**：首次打开配置页面时，数据库无保存记录，需要正确回显默认值

**缓解**：
- `getListByScene()` 返回枚举定义的默认项，`defaultValues` 字段携带默认值
- `getConfigValueByScene()` 的 `else` 分支在无已保存值时，`item.setValue(null)` 保持空，前端通过 `ShellConfigItem.getShellConfigValue()` 获取值时空值回退到 `defaultValues`
- 与 `weather` 场景的默认值回显机制完全一致

## Migration Plan

### 部署步骤

1. **发布 common 模块**
   - 包含 4 个枚举类的新增项
   - 包含 2 个 i18n 资源文件的新增键
   - 无需数据库迁移（复用 `rc_basics_config` 表）
   - 无需配置变更

2. **验证**
   - 调用壳配置查询接口，传入 `scene=persistent_data_rule`，确认返回 2 个配置项
   - 调用 `BasicsConfigTypeEnum.getList()` 接口，确认列表中出现 `shell_config_persistent_data_rule` 类型
   - 保存配置后重新查询，确认值正确回显

### 回滚策略

- 回滚 common 模块代码即可
- 无数据迁移，无数据库变更
- 回滚后新场景消失，不影响其他场景

## Open Questions

无未决问题。所有设计决策已在探索阶段经用户确认：
1. ✅ 新建独立 Scene（代码 `persistent_data_rule`）
2. ✅ 条件显示后端不用管，前端自行处理
3. ✅ 不涉及消费侧逻辑
