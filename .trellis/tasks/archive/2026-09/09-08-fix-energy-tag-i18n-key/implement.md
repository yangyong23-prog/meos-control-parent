# Implement：能耗模型标签查询恢复按 tagName 且名称对齐第三方字典

## 方案演化记录（完整记录于 design.md）

v1 getContractName（否决）→ v2 name 中文（否决）→ v3 tag/code 传枚举 code（否决）
→ v4 改共享 key 资源（否决：污染运行日志等 UI）→ v5 终版：枚举独立 key + 传解析名称

## 前置事实核查（已完成）

- [x] `EnergyModelItemParam`/`queryEnergyModelItem` 唯一调用方：`RunAnalysisServiceImpl`（grep）
- [x] 泄漏机制：`BaseEnumSerializer` 写 `BaseEnum.value()`=name key（javap 实证）
- [x] name key 共享：key_ffd/key_9e43/key_56a2 同时被 RunningLogCategoryEnum（运行日志分类）、
      RunningMonitorTypeEnum、SubscribeTypeEnum 引用 ⇒ 资源值不可改（grep + 读码实证）
- [x] TOTAL 不在 `ENERGY_ITEM_TAG_LIST`，不参与查询（读码确认）
- [x] 第三方字典口径：energy_model_item_tag 2025-10 后名称（用户提供 SQL + 中英翻译）
- [x] 新 key 名 `key_energy_model_tag_*` 与现有资源无冲突（grep）

## 改动清单（已完成，编译+单测通过）

1. `EnergyModelTagEnum.java`：COOL/HEAT/COOL_HEAT/AIR_TERMINAL 的 name → 独立 key
   （key_energy_model_tag_cool/heat/cool_heat/air_terminal）；name 字段加"勿共享 key"注释；
   TOTAL 与全部方法逻辑不动
2. `messages_zh.properties` / `messages_en.properties`：各新增 4 条字典名称（zh 中文/en 用户翻译）；
   共享 key 值未动
3. `EnergyModelItemParam.java`：`tagNames: List<String>`（字段名不变，不再承载枚举）
4. `RunAnalysisServiceImpl.java`：`setTagNames(Collections.singletonList(tagName.getName()))`
5. 测试：`EnergyModelTagEnumTagNameTest`（zh/en 字典名称解析断言）

## 验证记录

- [x] `mvn compile -pl common -am`（JDK8）通过
- [x] `mvn test -pl common -Dtest=EnergyModelTagEnumTagNameTest`：2/2 通过
- [x] git diff 复核：5 个文件（枚举 + param + service + 2 资源），无其它模块文件触碰
- [x] 全仓无残留 `tagCodes`/`getCode()` 传参路径

## 剩余步骤

- [ ] Review gate：与用户过最终 diff，确认后按 meos-commit-message 规范提交
- [ ] 联调/线上验证（用户执行）：测试环境 HILON（或任一 zh 项目）调运行分析能耗统计/曲线接口，
      确认请求体 `tagNames: ["供冷系统用电量"]` 形态、响应 content 非空
      （前提：目标项目第三方字典为 2025-10 后口径）
