# 修复模板缓存queryParam丢失与坏快照复用问题

## Goal

修复 InfoCode 模板缓存（`cache_info_code_template`）的两处缺陷，使模板缓存具备自愈能力，恢复 `PUBLIC_AREA_LIGHTING-001` 等模板的正常对象匹配：

1. 构建时解析失败会把"无 queryParam 的坏快照"固化进 `data_json`；
2. 重建不会重新解析配置，而是把旧 `data_json` 原样复制回来，坏快照永久生效。

## Background（已确认事实）

- 生产环境 `cache_info_code_template` 中 `PUBLIC_AREA_LIGHTING-001` 行的 `data_json` 缺少顶层 `queryParam`（配置表 `object_info_code_template_type` 中该字段为 `id: ${#flattenSet(existsServiceSpaceIdCircuitIds)}`）。
- 手工验证两个 source 查询均有数据：`circuitIds`（507 个 LTLTCC 设备）、`existsServiceSpaceIdCircuitIds`（GeneralZone 空间，带 `relObjIds`）。
- 已触发重建：`update_time` 变化但 `data_json` 内容不变 → 重建未重新解析配置。
- 根因链路（代码证据）：
  - `InfoCodeTemplateTypeModel.parseFilterValueObject` 缓存读取分支（[InfoCodeTemplateTypeModel.java L461-467](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModel.java#L461-L467)）：只要缓存行 `dataJson` 非空就直接复用，`filterValueObjectSupplier`（实时解析）不执行；
  - `CacheInfoCodeTemplate.from`（[CacheInfoCodeTemplate.java L75-84](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/model/entity/cache/CacheInfoCodeTemplate.java#L75-L84)）把上述结果无条件写入 `data_json`；
  - 首次污染来源：解析失败时 `parseFilterValueObject` 只是 `setQueryParam(null)` 后照常返回对象（[InfoCodeTemplateTypeModel.java L449-452](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModel.java#L449-L452)）。

## Requirements

- R1 读取侧自愈：`parseFilterValueObject` 缓存读取分支中，缓存 `dataJson` 存在但 `queryParam` 为 null 时视为未命中，回退 `filterValueObjectSupplier` 实时解析。
- R2 写入侧不固化坏快照：`CacheInfoCodeTemplate.from` 在解析结果 `dataJson` 为 null 或其 `queryParam` 为 null 时，写入 `dataJson = null`（读取侧已有 `orElseGet(supplier)` 兜底）。
- R3 保持既有运行时语义：解析失败时 `toDtpRwdObjectQueryParam()` 仍返回 null（模板不匹配任何对象、查询返回空集合），不得回退为按 classCode 全量查询。

## Acceptance Criteria

- [x] AC1：缓存行 `dataJson` 存在但 `queryParam` 为 null 时，`parseFilterValueObject` 执行实时解析（单测 `shouldResolveLiveWhenCachedQueryParamMissing` 验证通过）。
- [x] AC2：缓存行 `dataJson` 的 `queryParam` 非 null 时，`parseFilterValueObject` 直接复用缓存，不触发 RWD 查询（单测 `shouldReuseCacheWhenQueryParamPresent` 验证通过）。
- [x] AC3：`CacheInfoCodeTemplate.from` 对 `queryParam` 为 null/结果为空时写入 null `data_json`，否则原样保留（3 个单测验证通过）。
- [ ] AC4：生产验证路径待部署后执行（`UPDATE data_json = NULL` + 重建 → `queryParam.id` 出现约 507 个 id；再次重建保持正确）。
- [x] AC5：本次新增 5 个单测全绿；相关存量测试（CacheInfoCodeTemplateRepositoryImplTest、DebugToolServiceImplTest、InfoCodeTemplateTypeEnumCacheTest、InfoCodeConverterTest）全绿。注：common 全量单测存在 1 个与本任务无关的既有失败（CacheObjectModelStatusServiceImplTest 期望支持类型未含 VENTILATION，系通风任务已提交改动未同步断言）。

## Out of Scope

- `DtpRwdObjectQueryParam.replacePlaceholder` 的 null 返回语义（占位符解析为 null → 返回 null）保持现状，不修改。
- 生产数据一次性修复 SQL（运维操作，非代码交付物），仅在验收时作为操作步骤执行。
- 缓存读取时主动回写修复 DB 行（读取自愈 + 重建固化已满足需求，不引入额外写路径）。

## Notes（运维应急步骤，供生产验收使用）

1. `UPDATE cache_info_code_template SET data_json = NULL WHERE custom_key = 'PUBLIC_AREA_LIGHTING-001' AND project_id = 'Pj3501040006';`
2. 触发模板重建（重新保存模板类型或走 LIGHTING 缓存重建入口），确认 `data_json` 包含 `queryParam.id`。
