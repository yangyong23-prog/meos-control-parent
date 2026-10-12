# 调试工具信息点配置合并设计

## Scope

本任务改动范围集中在 common 后端：

- 调试工具接口服务：`DebugToolServiceImpl`
- 调试工具接口返回 VO：`DebugToolInfoCodeConfigStatusVO`
- 运行监测类型枚举：`RunningMonitorTypeEnum`
- 必要时同步二级枚举：`RunningMonitorSecondTypeEnum`

## Current Flow

`queryObjectInfoCodes` 当前流程：

1. 根据 `objectId` 查询对象及 `classCode`。
2. 使用 `templateTypeIds` 初始化 `InfoCodeInitByObjectParam`。
3. `infoCodeGateway.of(param)` 生成信息点聚合根。
4. 从 `InfoCodeConfigModel` 提取 `infoCode -> infoCodeName`。
5. 查询规则、实时值、精度、绑点状态。
6. `dataAssembly` 组装 `DebugToolInfoCodeConfigStatusVO`。

## Target Flow

1. 根据入参 `templateTypeIds` 查询模板类型，得到本次查询涉及的 `RunningMonitorTypeEnum`。
2. 调用新增枚举方法获取关联的单个调试工具运行监测类型。
3. 如果存在关联类型：
   - 查询相同对象类、相同模板类型维度下关联调试工具类型的模板类型 id。
   - 将原 `templateTypeIds` 与关联模板类型 id 合并后传给 `infoCodeGateway.of`。
4. 将聚合根按信息点编码合并：
   - 信息点名称通过数据字典 `DtpKnowledgeClientService#mapInfoCodeAndName(String)` 获取，配置名称仅兜底。
   - 原类型和调试工具类型的 `showStyle` 都存在时做结构化合并，重复字段或重复数组项使用调试工具配置。
5. 返回 VO 时填充 `showStyle`。

## Enum Contract

在 `RunningMonitorTypeEnum` 中新增类似方法：

```java
@Nullable
public RunningMonitorTypeEnum toDebugToolRunningMonitorType()
```

当前映射：

- `AIR_CONDITIONING_COLD_SOURCE` -> `AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL`
- 其他类型 -> `null`

新增运行监测类型：

- 一级类型：`AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL`
- 二级类型：如配置链路需要同步二级类型，则新增同名二级枚举并映射回一级类型。

## showStyle Merge Rule

以 `infoCode` 为唯一键：

- 原类型有、调试工具类型无：使用原类型 `showStyle`。
- 原类型无、调试工具类型有：使用调试工具类型 `showStyle`。
- 两边都是 JSON 对象：合并对象字段，重复字段使用调试工具类型。
- 两边都是 JSON 数组：按 `infoCode`、`code`、`key`、`name` 等稳定字段合并数组元素，重复元素使用调试工具类型。
- 两边格式不兼容或无法解析为 JSON：使用调试工具类型 `showStyle`。
- 两边都无：返回 `null`。

## Compatibility

- 未配置关联调试工具类型时，接口继续只使用原 `templateTypeIds`。
- 数据字典查不到名称时，不抛错，沿用配置名称兜底。
- 关联模板类型查不到时，不抛错，沿用原类型配置。
- 现有入参结构保持不变。

## Validation

- 编译 common 模块或至少编译受影响模块。
- 重点检查枚举 JSON 反序列化、MapStruct unmapped 策略以及新增 VO 字段对现有调用方的兼容性。

## Follow-up Contract

- `DebugToolTemplateTypeAndClassCodeVO` 不暴露关联调试工具类型字段。
- `listTemplateTypeAndClassCodes` 组装结果时过滤所有被 `RunningMonitorTypeEnum#toDebugToolRunningMonitorType()` 返回的调试工具运行监测类型，避免前端重复展示；调试工具信息点合并仍由后端在 `queryObjectInfoCodes` 中处理。
