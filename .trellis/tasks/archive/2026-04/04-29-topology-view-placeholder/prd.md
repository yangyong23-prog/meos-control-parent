# 新增 TOPOLOGY_VIEW 模板类型及占位符替换

## Goal

1. 新增信息点模版类型 `TOPOLOGY_VIEW`，用于拓扑视图场景。
2. 支持通过接口入参传入自定义占位符数据（`Map<String, JsonNode>`），在物理世界详情信息点解析时，将这些数据作为数据源替换取值逻辑中的占位符。

## What I already know

- `InfoCodeTemplateTypeEnum` 定义了所有模版类型（DEFAULT, HOVER, DETAIL, TABLE_LIST 等），新增枚举值需遵循现有模式（value + i18n key）。
- `InfoCodeConfigByObjectQueryParam` 是「按对象查询信息点配置」的入参 DTO，当前通过 `paramTimeToConditions()` 将时间范围写入附加数据。
- `queryInfoCodeObjectGroupVo()` 方法中构建 `Map<String, Object> additionalDataValues` 并传递给 `infoCodeGateway.fillInfoCodeDataModel()`。
- `RwdObjectDetailInfoCodeAnalyzeStrategy` 消费 `additionalValues`，构造 `InfoCodeRwdObjectDetailByTemplateContext`。
- `InfoCodeRwdObjectDetailByTemplateContext` 在 `fillSourceMap()` 和 `fillObjects()` 中，通过 `DtpRwdObjectQueryParam.replacePlaceholder(this.sourceMap)` 完成 SpEL 占位符替换。
- 项目规范：DTO 禁止 `@Builder`，需用 `@Data @NoArgsConstructor @AllArgsConstructor`。

## Requirements

1. `InfoCodeTemplateTypeEnum` 新增 `TOPOLOGY_VIEW` 枚举值（value = "TOPOLOGY_VIEW"）。
2. `InfoCodeConfigByObjectQueryParam` 新增 `Map<String, JsonNode> placeholders` 字段（Swagger注解 + Nullable）。
3. `InfoCodeConfigServiceImpl#queryInfoCodeObjectGroupVo` 中将 `placeholders` 放入 `additionalDataValues`（key 建议为 `"PLACEHOLDERS"`）。
4. `InfoCodeRwdObjectDetailByTemplateContext#fillSourceMap` 在构建完自动数据源后，将用户传入的 placeholders 合并到 `sourceMap` 中，**用户传入的优先覆盖同名 key**。
5. 不传入 placeholders 时，现有逻辑完全不受影响。

## Acceptance Criteria

- [ ] `TOPOLOGY_VIEW` 枚举值可正常序列化/反序列化（Jackson + MyBatis Plus IEnum）。
- [ ] 传入 placeholders 后，`DtpRwdObjectQueryParam` 中的 `${xxx}` 占位符可被替换为对应 JsonNode 值。
- [ ] 用户传入的 placeholder 与自动解析出的同名 key 冲突时，用户传入的优先。
- [ ] 不传入 placeholders 时，现有逻辑不受影响。

## Out of Scope

- 不新增独立的解析策略（复用 `RwdObjectDetailInfoCodeAnalyzeStrategy`）。
- 不修改 `DtpRwdObjectQueryParam` 的占位符替换机制本身。
- 不在 `toResult()` 阶段做结果级占位符替换（仅查询参数级替换）。

## Technical Notes

- 关键文件：
  - `InfoCodeTemplateTypeEnum.java`
  - `InfoCodeConfigByObjectQueryParam.java`
  - `InfoCodeConfigServiceImpl.java` (queryInfoCodeObjectGroupVo)
  - `InfoCodeRwdObjectDetailByTemplateContext.java`

## Decision

- 占位符合并时机：`fillSourceMap()` 方法末尾，自动数据源构建完成后、查询执行前，将用户 placeholders 合并到 sourceMap，实现"用户传入优先"。
