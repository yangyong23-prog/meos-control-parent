# Design：能耗模型标签查询恢复按 tagName 且名称对齐第三方字典

## 结论

调用保持 `/common/api/project/energy/model/item/tag/filter`（按 tagName 过滤）；
`EnergyModelItemParam.tagNames` 改为 `List<String>`，由调用方解析 `EnergyModelTagEnum`
**独立 i18n key**（name 字段换成不与其它模块共享的新 key）后传入字典名称。
请求语言 zh 时传"供冷系统用电量"等，en 时传第三方字典英文名。

## 方案演化记录（评审驱动收敛）

| 版本 | 方案 | 结果 |
| --- | --- | --- |
| v1 | 新增 `getContractName()` 固定 zh 解析 | 用户否决：复杂 |
| v2 | `name` 恢复 7.25 前中文字面值（冷站等） | 用户否决：转向第三方按 code 接口 |
| v3 | 切 `/tag/code` 传枚举 code（COOL） | 用户否决：枚举 code 与第三方字典 code（ACCC_E 等）不是一回事 |
| v4 | 直接改 name 共享 key 的 zh/en 资源值 | 用户否决：key_ffd 等被运行日志分类（RunningLogCategoryEnum.ACCC）、运行监测类型等共享，改值污染其它模块 UI |
| v5（终版） | 枚举 name 换独立 key + 资源配第三方字典名称 + tagNames 解析传参 | 本设计 |

## 根因与修复机制

- 根因：`tagNames` 直接承载 `EnergyModelTagEnum`，序列化（`BaseEnumSerializer` 写 `value()`）
  输出 `name` 的 i18n key ⇒ 泄漏；同时第三方字典 2025-10 起"冷站"更名"供冷系统用电量"，
  旧名称与旧 en 翻译（Chiller Plant 等）均不可匹配。
- 修复机制：请求参数改传**解析后的字典名称**（`List<String>`，枚举不再参与序列化），
  名称来源为本枚举独立 key，按请求语言解析；与其它模块共享的旧 key 完全隔离。

## 关键事实核查

1. `name` key 共享关系（grep 实证）：key_ffd/key_9e43/key_56a2 被 `RunningLogCategoryEnum`、
   `RunningMonitorTypeEnum`、`SubscribeTypeEnum` 等引用为日志分类/监测类型 UI 文案 ⇒ 不可改值；
2. TOTAL 不在 `ENERGY_ITEM_TAG_LIST`（`RunAnalysisService`），不参与查询，保持原样；
3. `EnergyModelTagEnum.getName()` 解析 `name` 字段 key（`LocaleMessageUtil.getMessage`，
   按请求 LocaleContext），`tagNames` 传其返回值；
4. 序列化机制：`BaseEnumSerializer` 写 `value()`（javap 实证），`tagNames` 若仍承载枚举必泄漏 key，
   故类型必须为 `List<String>`。

## 改动点

1. `EnergyModelTagEnum.java`
   - COOL/HEAT/COOL_HEAT/AIR_TERMINAL 的 `name` 常量：共享 UUID key →
     独立语义 key `key_energy_model_tag_cool/heat/cool_heat/air_terminal`；
   - `name` 字段注释：与第三方字典名称对齐、勿与日志分类等共享 key；
   - TOTAL 不动（仍 key_4379）；`value()`/`getReasonPhrase()`/`getName()`/`getEnergyName()` 逻辑不动。
2. `messages_zh.properties` / `messages_en.properties`
   - 各新增 4 条：zh = 供冷系统用电量/热站/冷热源/空调末端；
     en = Cooling System Electricity Consumption/Heat Station/Cooling and Heating Source/Air Conditioning Terminal
   - 共享 key（key_ffd 等）值保持原样。
3. `EnergyModelItemParam.java`：`tagNames: List<String>`（原 `List<EnergyModelTagEnum>`），
   字段名保持 tagNames。
4. `RunAnalysisServiceImpl#queryEnergyItemRwdIdList`：
   `setTagNames(Collections.singletonList(tagName.getName()))`。
5. 测试：`EnergyModelTagEnumTagNameTest`（zh/en 解析断言 ×2）。

## 兼容性与风险

- 其它模块（运行日志/运行监测/SSE 订阅）共享 key 值零改动 ⇒ UI 文案无影响；
- 展示层 `getEnergyName()`（energyName key）不变 ⇒ 能源统计图例名称（冷源能耗等）无影响；
- `EnergyModelTagEnum` name 旧 key 无人再引用（该枚举内换新 key 后旧 key 仍被其它模块使用，
  不存在孤儿问题）；
- 语义前提：目标项目（HILON）第三方字典为 2025-10 后口径（"供冷系统用电量"等），
  联调时以请求体 `tagNames: ["供冷系统用电量"]` 返回非空为验收；
- 无数据库/缓存/配置变更。

## 回滚方案

5 文件单提交，`git revert` 完整回滚；无其它联动。
