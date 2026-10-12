# 修复机房安防缓存构建覆盖共享对象字段导致object_data丢字段

## Goal

DTX 发布 MQ 消息触发全量缓存构建后，机房安防策略以"手写字段子集"写入楼层全量设备，覆盖照明等策略已写入的共享 cache_object 行，导致 LTLTCC 等对象的 object_data 只剩 `{"major"}`；改为使用"全量登记字段并集"组装查询字段，并补充契约与回归测试防止复发。

## Background

- **现象**：MQ（DTX 发布）触发构建后 `cache_object.object_data` 只剩 `{"major": ...}`；`/cacheRwd/triggerBuildCache`（LIGHTING）触发后数据完整。
- **路径差异**：MQ → `CacheRwdBuildTriggerEvent(null)` → `buildCache(null)` 全量构建（包含 COMPUTER_ROOM_SECURITY 类型）；接口 → `buildCache(LIGHTING)` 按类型过滤，机房安防策略被跳过（`strategyType != runningMonitorType` 时 continue）。
- **根因**：`CacheRwdComputerRoomSecurityStrategy` 的"楼层全量设备"查询无 classCode 过滤，但查询字段为手写小清单（`listSimpleWithColumns + relObjIds + realTimeStreamProtocol + monitoringSurveillanceStatus + major + bimId/bimLocation + floorId/buildingId`），缺少任何 classCode 白名单业务字段（如 LTLTCC 的 serviceAreaType/funcType/lampFuncType）。
- **覆盖机制**：cache_object 行按对象 ID 主键跨缓存 Key 共享（`replaceObjectIds` 仅维护 Key→对象关系）；UPSERT 为 `object_data = values(object_data)` 无条件整列覆盖；机房安防晚于照明策略执行时后写覆盖前写。
- **受影响面**：所有"在 `OBJECT_EXTENSION_FIELDS`/`MODEL_VIEW_FIELDS` 登记了字段、且位于机房楼层空间关系内"的设备类对象（LTLTCC、ACVTSF、SPVSCM/CFCSDR 等安防类……）。
- **既有契约**：`.trellis/spec/backend/cache-object-model.md` 已约定"混合 classCode 范围只合并各 classCode 集合后发起原范围查询"，机房安防现状违反该契约。

## Requirements

- R1：机房安防"楼层全量设备"写入的查询字段必须覆盖"全部已登记 classCode 的完整写入字段并集"（基础字段 + 所有对象扩展字段 + 所有模型视图字段 + 通用字段），确保与任何写入方字段集一致，覆盖幂等不丢字段。
- R2：保留机房原有额外字段需求：`relObjIds`（关系快照必需）。
- R3：字段并集逻辑收敛到 `CacheObjectExtensionFieldConfig`（新增全量并集静态方法），策略内不得再手写字段清单。
- R4：其他写入方（照明/通风/末端/末端白名单/安防/项目结构/模板重建）不改动；cache_object 表结构、写服务、各缓存 Key 对象范围与关系语义均不变。
- R5：规范同步：`cache-object-model.md` 补充"无法枚举 classCode 的混合范围写入必须使用全量登记并集"契约。
- R6：写入方字段完整性回归门禁（防回流，方案 A）：为 VENTILATION、模板重建（TemplateRebuild）补齐"写入字段边界测试"（对齐已有机房/照明/安防测试模式）；模板重建链路对"返回对象 classCode ≠ 模板 classCode"记录告警日志，暴露跨类配置异常。

## Acceptance Criteria

- [ ] `CacheRwdComputerRoomSecurityStrategy` 设备查询 withColumns = 全量并集 + `relObjIds`；包含 LTLTCC（serviceAreaType/funcType/lampFuncType/major）、ACVTSF（funcType）、安防类（isControl/manufacturer/cameraType/hasArmingFunc/realTimeStreamProtocol）等全部登记字段。
- [ ] 新增配置契约测试：全量并集与 base ∪ 全部 OBJECT_EXTENSION_FIELDS.values ∪ 全部 MODEL_VIEW_FIELDS.values ∪ UNIVERSAL_OBJECT_FIELDS ∪ UNIVERSAL_MODEL_VIEW_FIELDS 完全相等（防漏、防多），并显式包含 major/bimId/bimLocation 与 LTLTCC 三字段。
- [ ] 新增机房策略写入边界测试：捕获设备查询 withColumns，断言包含全量并集与 relObjIds（防回退到手写清单）。
- [ ] `mvn -pl common test` 编译与全量单测通过（含既有测试无回归）。
- [ ] spec 更新合入（R5），并写明"写入方必须使用配置类方法组装字段 + 边界测试矩阵"约定。
- [ ] 新增 `CacheRwdVentilationStrategyWriteBoundaryTest`：捕获空间/设备查询字段，断言包含 `listRwdQueryFields` 并集与附加字段（name、relObjIds）。
- [ ] 新增模板重建字段边界测试：断言 `queryTemplateObjectsForCacheWrite` 的 RWD 查询字段按模板 classCode 组装；"跨类对象"告警防御有对应用例。
- [ ] 发布后动作（非代码验收项）：重新触发一次缓存构建（MQ 或接口），刷新历史被覆盖数据。

## Notes

- 诊断证据链、改动清单与验证命令见本任务 `implement.md`。
- RWD 查询列增多属于正确性必要成本，不引入额外优化设计。
