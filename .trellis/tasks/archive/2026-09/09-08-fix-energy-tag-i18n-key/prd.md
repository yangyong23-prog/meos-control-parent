# 能耗模型标签查询恢复按 tagName 且名称对齐第三方字典

## Goal

运行分析能耗统计/曲线场景调用第三方 energy-research-server `/common/api/project/energy/model/item/tag/filter`
（按标签名称过滤）时，`tagNames` 传参与第三方 `energy_model_item_tag` 字典名称对齐的名称
（按请求语言解析，如 zh"供冷系统用电量"/en"Cooling System Electricity Consumption"），
不再传 i18n key 或过时名称，恢复能耗模型可匹配。

## Background（根因摘要）

- 线上日志（HILON）：请求 `tagNames: ["key_ffd567238d36487bb75fd52074e46ca2"]`，响应 `content=[]`。
- `EnergyModelTagEnum implements BaseEnum<String>`，`value()` 返回 `name`；7.250901.0 国际化
  把 `name` 的 i18n key 直接序列化泄漏到请求体（`BaseEnumSerializer` 写 `value()`，javap 实证）。
- 第三方字典（energy_model_item_tag，2025-10 起版本）中"冷站"已更名"供冷系统用电量"等，
  字典名称与我方 7.25 前的旧字面值（冷站/热站/冷热源/空调末端）不再完全一致，
  且英文名以第三方字典口径为准（我方旧 en 翻译 Chiller Plant 等不可匹配）。
- 原 name key（key_ffd 等）被运行日志分类/运行监测类型等模块共享，不可直接改其资源值。

## Requirements

1. 恢复 tag/filter 接口按 tagName 查询；`tagNames` 传解析后的字典名称（`List<String>`），
   不直接承载 `EnergyModelTagEnum`（序列化仍会泄漏 i18n key）。
2. 字典名称来源：`EnergyModelTagEnum` 4 个查询标签（COOL/HEAT/COOL_HEAT/AIR_TERMINAL）的
   `name` 使用**本枚举独立 i18n key**（不与运行日志分类等共享），zh/en 资源值 =
   第三方字典名称（zh：供冷系统用电量/热站/冷热源/空调末端；
   en：Cooling System Electricity Consumption/Heat Station/Cooling and Heating Source/Air Conditioning Terminal）。
3. TOTAL 不参与查询（不在 ENERGY_ITEM_TAG_LIST），name 资源与代码保持原样。
4. 不改动共享 key（key_ffd/key_9e43/key_56a2 等）资源值，其它模块 UI 文案零影响；
   展示层 `getEnergyName()`（energyName key）行为不变。

## Acceptance Criteria

- [x] 编译通过（`mvn compile -pl common -am`，JDK8）
- [x] 请求体形态：`tagNames` 为 `List<String>`，zh 环境序列化 `["供冷系统用电量"]` 等字典名称，
      不可能出现 `key_` 前缀（单测断言 getName() 解析值）
- [x] 共享 key 资源值未改动（git diff 仅新增独立 key 条目）
- [x] 回归单测通过：新 key zh/en 解析断言（EnergyModelTagEnumTagNameTest，2/2）
- [ ] 测试环境对 HILON（或任一 zh 项目）验证：能耗统计/曲线接口返回非空能耗模型数据，
      请求体为 `tagNames: ["供冷系统用电量"]` 形态（前提：目标项目第三方字典为该口径）

## Notes

- 发布路径：当前分支 `feat_v7.260901.0_运行分析`；若线上已发布版本需要补丁，
  由用户确认是否另行出 hotfix 分支（不在本任务代码范围内）。
