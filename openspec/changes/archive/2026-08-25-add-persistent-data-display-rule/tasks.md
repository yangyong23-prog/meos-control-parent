## 1. 新增壳配置场景枚举

- [x] 1.1 在 `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigSceneEnum.java` 中新增枚举项 `SCENE_PERSISTENT_DATA_RULE("persistent_data_rule", "云端持久化数据显示规则", "云端持久化数据的查询逻辑配置；修改后即时生效")`，放置在 `SCENE_WHITELIST_SPACE` 之后
- [x] 1.2 在 `ShellConfigSceneEnum.convertToBasicConfigTypeByScene()` 方法的 switch 中新增 `case SCENE_PERSISTENT_DATA_RULE: return BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE;`，放置在 `case SCENE_WHITELIST_SPACE` 之后

## 2. 新增基础配置类型枚举

- [x] 2.1 在 `common/src/main/java/com/persagy/apccontrol/basic/model/base/BasicsConfigTypeEnum.java` 中新增枚举项 `SHELL_CONFIG_PERSISTENT_DATA_RULE("shell_config_persistent_data_rule", "key_shell_config_persistent_data_rule", "key_f64a1fbe41b54eb891c43d52e1ff6355")`，放置在 `SCENE_CONFIG_WHITELIST_SPACE` 之后

## 3. 新增壳配置分组枚举

- [x] 3.1 在 `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigGroupEnum.java` 中新增枚举项 `GROUP_PERSISTENT_DATA_RULE("group_persistent_data_rule", "云端持久化数据显示规则", "", false)`，放置在 `GROUP_TERMINAL_MONITOR_SPACE` 之后

## 4. 新增壳配置项枚举

- [x] 4.1 在 `common/src/main/java/com/persagy/apccontrol/basic/model/enumtype/ShellConfigItemEnum.java` 中新增枚举项 `PERSISTENT_DATA_QUERY_LOGIC`：
  ```
  PERSISTENT_DATA_QUERY_LOGIC("persistent_data_rule", "group_persistent_data_rule", "云端持久化数据显示规则", "persistent_data_query_logic", "持久化数据查询逻辑", "", "same_time", -1, null, null, "", "①查询同一个时刻点的数据：所有信息点都查询某一个时刻的固定分精度数据；若信息点的最小分精度数据大于设置的分精度则会获取不到数据，比如信息点字典定义的最小分精度数据15min，设置的分精度为5min，则永远查询不到数据\n②查询距离当前最新的数据：不限制分精度，有最新的数据上数到云端即可查询，但不同信息点的最新时间点可能不一致", null, "[{\"name\":\"查询同一个时刻点的数据\",\"value\":\"same_time\"},{\"name\":\"查询距离当前最新的数据\",\"value\":\"latest\"}]", null)
  ```

- [x] 4.2 在 `ShellConfigItemEnum.java` 中新增枚举项 `PERSISTENT_DATA_TIME_INTERVAL`：
  ```
  PERSISTENT_DATA_TIME_INTERVAL("persistent_data_rule", "group_persistent_data_rule", "云端持久化数据显示规则", "persistent_data_time_interval", "请设置距离当前的时间间隔", "h", "1", 1, 1, 168, "整数，范围（1-168）", "仅查询设置的时间范围内的最新数据，若没有最新数据，则页面会显示为--。比如设置3h，3h内没有新的数据上数则显示为--，若在持续上数则显示最新的一个数据", null, null, null)
  ```

- [x] 4.3 以上两个枚举项放置在文件末尾分号 `;` 之前（`GROUP_TERMINAL_MONITOR_SPACE` 对应的 item 之后）

## 5. 新增 i18n 国际化键

- [x] 5.1 在 `common/src/main/resources/i18n/messages_zh.properties` 中新增 `key_shell_config_persistent_data_rule=云端持久化数据显示规则`，放置在 `key_shell_whitelist_space` 附近
- [x] 5.2 在 `common/src/main/resources/i18n/messages_en.properties` 中新增 `key_shell_config_persistent_data_rule=Cloud Persistent Data Display Rule`，放置在 `key_shell_whitelist_space` 附近

## 6. 代码验证和测试

- [x] 6.1 编译验证：确保 common 模块编译通过，无语法错误
- [x] 6.2 枚举一致性验证：确认 `ShellConfigSceneEnum` 的新增 case 与 `BasicsConfigTypeEnum` 的新增项一一对应
- [x] 6.3 模式一致性验证：确认新增枚举项的构造函数参数数量和顺序与现有项一致（ShellConfigItemEnum 为 15 参数）
- [x] 6.4 代码路径验证：确认 `ShellConfigServiceImpl.query()` / `save()` / `getConfigValueByScene()` 无需修改，新场景自动走 `else` 简单值合并分支
- [x] 6.5 i18n 键验证：确认 `BasicsConfigTypeEnum.getExplain()` 调用 `LocaleMessageUtil.getMessage("key_shell_config_persistent_data_rule")` 能正确返回中英文值
