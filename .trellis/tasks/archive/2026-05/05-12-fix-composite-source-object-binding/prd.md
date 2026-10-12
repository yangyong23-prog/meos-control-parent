# Fix Composite Source Object Binding

## Goal

修复 RWD 对象详情取值逻辑在主查询使用复合 SpEL 表达式引用多个 source 时，无法正确识别 source key 并导致主对象与关联对象归属错配的问题。

## Requirements

* 保持现有 `${key}` 占位符识别逻辑优先，避免改变已有模板行为。
* 当原有识别未命中时，自动从 SpEL root 引用中识别 source key，例如 `#root['ACAirNetworkSupply']`、`#root.FreshAirNetworkFresh`。
* source 查询使用 `relationTo.objFrom: "${objectId}"` 时，若查询结果带有 `relObjIds`，优先按 `relObjIds` 回写 rootObjectId 与 source 的映射。
* 只有无法根据 `relObjIds` 建立归属时，保留原有按 rootObjectIds 广播的兜底行为。
* 不新增取值逻辑配置字段，不修改对外入参格式。

## Acceptance Criteria

* [x] `#unionCollection(#root['ACAirNetworkSupply'], #root['FreshAirNetworkFresh'])` 能自动识别出两个 source key。
* [x] 多个 root objectId 入参时，主查询结果能按 source 查询阶段建立的映射归属到对应 root objectId。
* [x] 旧的直连 `${key}` 占位符场景继续走原逻辑。
* [x] 没有 `relObjIds` 的查询结果仍按原广播兜底处理。
* [x] 相关测试通过，或说明无法运行的原因。

## Definition of Done

* 代码改动尽量限定在 `InfoCodeRwdObjectDetailByTemplateContext`。
* 补充或更新覆盖该归属逻辑的测试。
* 编译/测试检查完成。

## Technical Approach

在 `fillObjects()` 计算 sourceKeys 时保留现有 `DtpRwdObjectQueryParam.placeholderKey(item)` 字符串匹配。若结果为空，再从 queryParam 的 placeholderString 中解析 `#root['key']` 和 `#root.key` 形式的 SpEL root 引用。

在 `fillSourceMap()` 处理查询结果时，将 `sourceKeys.contains(OBJECT_ID)` 的无条件广播收紧为：如果对象返回了 `relObjIds`，先用 relObjIds 与当前 rootObjectIds 建立 source 映射；没有 relObjIds 时继续保留原广播行为。

## Out of Scope

* 不调整 `SpelUtils#unionCollection` 的返回语义。
* 不新增模板配置项。
* 不重构取值逻辑整体流程。

## Technical Notes

* 主要代码路径：`common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/context/InfoCodeRwdObjectDetailByTemplateContext.java`
* 相关占位符替换：`common/src/main/java/com/persagy/apccontrol/basic/outside/feign/model/param/rwd/DtpRwdObjectQueryParam.java`
* 后端质量规范：`.trellis/spec/backend/index.md`、`.trellis/spec/backend/quality-guidelines.md`
