# 调试工具信息点配置合并

## Goal

调整 `/debugTool/queryObjectInfoCodes` 接口，使调试工具在查询空调冷源信息点时可以同时读取空调冷源原运行监测类型和空调冷源调试工具运行监测类型的配置，并返回数据字典名称与展示样式。

## Confirmed Facts

- 接口入口是 `DebugToolController#queryObjectInfoCodes`，服务实现是 `DebugToolServiceImpl#queryObjectInfoCodes`。
- 当前接口入参只有 `objectId` 和 `templateTypeIds`，没有直接传运行监测类型；实现需要根据模板类型 id 反查运行监测类型。
- 当前信息点名称来自 `InfoCodeAggregate#getConfigModel().getInfoCodeName()`，不是直接来自数据字典。
- `InfoCodeConfigModel` 已有 `showStyle` 字段，但 `DebugToolInfoCodeConfigStatusVO` 尚未暴露该字段。
- 运行监测一级类型枚举在 `RunningMonitorTypeEnum`，二级类型枚举在 `RunningMonitorSecondTypeEnum`。
- `DtpKnowledgeClientService` 需要提供 `mapInfoCodeAndName(classCode)`，调用方不再继续传已废弃的 `queryCache` 和 `isThrows`。

## Requirements

- `/debugTool/queryObjectInfoCodes` 返回的信息点名称必须优先取数据字典名称。
- 新增“空调冷源调试工具”的运行监测类型，用于承载调试工具专属信息点配置。
- 在运行监测枚举中新增方法，用于获取当前运行监测类型关联的单个调试工具运行监测类型。
- 当前需求中，空调冷源运行监测类型关联空调冷源调试工具运行监测类型；后续新增其他关联时应只扩展枚举映射。
- 当本次查询的运行监测类型存在关联调试工具类型时，接口需要合并原类型和调试工具类型的信息点配置。
- 接口返回 VO 新增 `showStyle` 字段。
- 合并信息点配置时，如果同一信息点在原类型和调试工具类型都配置了 `showStyle`，需要合并两个类型的配置；重复字段或重复数组项优先使用调试工具类型。
- 合并后的信息点仍需保留现有状态字段：绑点状态、实例化规则状态、最新值、单位、时间、精度、数据源等。

## Acceptance Criteria

- [ ] 查询普通运行监测类型且不存在关联调试工具类型时，接口行为保持兼容。
- [ ] 查询空调冷源运行监测类型时，接口同时纳入空调冷源调试工具类型的信息点配置。
- [ ] 合并后的信息点名称来自数据字典；若数据字典缺失该信息点名称，保留现有配置名称作为兜底。
- [ ] 返回结果包含 `showStyle` 字段。
- [ ] 同一信息点在空调冷源和空调冷源调试工具类型都配置 `showStyle` 时，合并两个类型的配置，重复内容优先使用调试工具类型配置。
- [ ] 同一信息点只在一个类型存在时，仍返回该信息点及其状态信息。
- [ ] 代码编译检查通过。

## Out Of Scope

- 不调整 `/debugTool/listTemplateTypeAndClassCodes` 的筛选逻辑，除非实现过程中发现必须显示新增调试工具类型。
- 不新增数据库表结构；默认使用现有信息点模板类型配置表承载新的运行监测类型。
- 不调整非调试工具接口的信息点展示逻辑。

## Open Questions

- 已确认按“用户查询空调冷源时自动合并调试工具类型配置”的方案实现。

## Follow-up Requirements

- `/debugTool/listTemplateTypeAndClassCodes` 不新增 `debugToolRunningMonitorType` 字段。
- 列表接口如果存在被原运行监测类型关联的调试工具运行监测类型，直接从返回结果移除；后端在查询信息点时完成合并。
