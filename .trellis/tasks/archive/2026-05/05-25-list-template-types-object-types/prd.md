# 支持模板类型批量对象类型查询

Issue: `UYAI-845`

## Goal

`/infoCodeConfig/listTemplateTypes` 新增 `objectTypes` 多对象类型入参，支持一次按多个 RWD 对象类型筛选模板类型，同时保留现有 `objectType` 单值入参兼容旧调用。

## Requirements

* `InfoCodeConfigTemplateTypeQueryParam` 增加 `objectTypes`，类型与底层 DAO 的 `objectTypes` 保持一致。
* 当接口只查询模板类型时，`objectTypes` 传递到 `InfoCodeTemplateTypeQueryDaoParam` 并使用已有批量过滤条件。
* 当接口需要查询对象数量或对象列表时，`objectTypes` 传递到 `InfoCodeObjectQueryParam`，再传递到底层模板类型查询。
* 保持现有 `objectType` 行为不变，旧调用不需要调整。

## Acceptance Criteria

* [x] 请求体只传 `objectType` 时仍按单个对象类型筛选。
* [x] 请求体传 `objectTypes` 时按多个对象类型筛选模板类型。
* [x] `notQueryObjectCount=true` 与默认查询对象数量两条路径都支持 `objectTypes`。
* [x] MapStruct 编译不因未映射字段失败。

## Definition of Done

* 代码通过编译或至少验证受影响模块的 MapStruct 生成阶段。
* 如无法完整运行测试，记录原因和已执行的替代检查。

## Technical Approach

在 API 入参 DTO 增加 `objectTypes` 字段；在 `InfoCodeObjectQueryParam` 增加同名字段；调整 `InfoCodeConverter`，取消对 `objectTypes` 的忽略，让 MapStruct 直接按同名字段映射到底层参数。

## Decision (ADR-lite)

**Context**: 底层 `InfoCodeTemplateTypeQueryDaoParam` 和 resource 查询已经支持 `objectTypes` 的 `.in(...)` 过滤，当前缺口在接口入参与转换链路。

**Decision**: 复用底层已有 `objectTypes` 字段，不新增 service 分支逻辑，不改变 `objectType`。

**Consequences**: 行为与底层现有单值/多值过滤一致；若调用方同时传 `objectType` 和 `objectTypes`，查询会按现有 DAO 条件取交集。

## Out of Scope

* 不调整其它 InfoCode 查询接口的入参。
* 不改变响应结构、模板排序、对象数量计算逻辑。

## Technical Notes

* Controller: `common/src/main/java/com/persagy/apccontrol/basic/controller/infocodeconfig/InfoCodeConfigController.java`
* API Param: `common/src/main/java/com/persagy/apccontrol/basic/model/param/infocodeconfig/InfoCodeConfigTemplateTypeQueryParam.java`
* Converter: `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/converter/InfoCodeConverter.java`
* Gateway Param: `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/gateway/param/InfoCodeObjectQueryParam.java`
* DAO Param already supports `objectTypes`: `common/src/main/java/com/persagy/apccontrol/basic/resource/infocodeconfig/param/InfoCodeTemplateTypeQueryDaoParam.java`
* Resource already applies `.in(CollectionUtils.isNotEmpty(param.getObjectTypes()), InfoCodeTemplateType::getObjectType, param.getObjectTypes())`.
* Verification: `mvn -pl common -am -DskipTests compile` passed.
* Verification: `mvn -pl common -DskipTests=false test` passed the build, but Surefire still reported `Tests are skipped` from project configuration.
* Verification: generated `InfoCodeConverterImpl` maps `objectTypes` through all three affected conversion paths.
