# 调试工具信息点配置合并实施计划

## Checklist

- [x] 加载后端和 InfoCode API 规范，确认新增字段/枚举的约束。
- [x] 在 `RunningMonitorTypeEnum` 新增空调冷源调试工具类型和关联调试工具类型方法。
- [x] 确认模板配置链路本次不需要新增二级类型，使用现有 nullable `runningMonitorSecondType` 承载调试工具模板配置。
- [x] 在 `DebugToolInfoCodeConfigStatusVO` 新增 `showStyle` 字段。
- [x] 调整 `DebugToolServiceImpl#queryObjectInfoCodes`：
  - 根据入参模板 id 查询模板类型；
  - 推导单个关联调试工具类型；
  - 查询并合并关联类型模板 id；
  - 组装 `infoCode -> 名称` 时优先使用数据字典名称；
  - 组装 `infoCode -> showStyle` 时合并原类型和调试工具类型配置，重复内容优先使用调试工具配置；
  - 在 `dataAssembly` 填充 `showStyle`。
- [x] 回归普通类型无关联时的逻辑兼容。
- [x] `DebugToolTemplateTypeAndClassCodeVO` 不新增关联字段，`listTemplateTypeAndClassCodes` 直接过滤关联的调试工具运行监测类型。
- [x] 运行编译/测试命令。

## Validation Commands

- `mvn -pl common -am -DskipTests compile` 通过。
- `mvn -pl common -Dtest=DebugToolServiceImplTest test` 通过，4 个测试成功。
- `mvn -pl common test` 通过，31 个测试成功。

## Risk Points

- 当前接口只传 `templateTypeIds`，关联类型模板 id 需要通过模板类型反查，避免错误扩大查询范围。
- `InfoCodeConverter` 使用严格映射策略，新增字段如果触达 MapStruct VO 需确认不会产生 unmapped 编译失败。
- 数据字典按 `classCode` 查询，若合并的调试工具配置使用同一对象类编码，名称覆盖才是稳定的。

## Rollback

- 回滚枚举新增值、VO 字段和 `DebugToolServiceImpl` 合并逻辑即可恢复现有行为。
