# 修复污水处理关系查询复用 queryParam 导致 relationFrom/relationTo 同时下发

## Goal

修复 `RunningMonitorSewageStrategy.listEquipments` 在楼层（FLOOR）实例场景下复用被污染的 `queryParam`，导致关系子查询同时携带 `relationFrom` 与 `relationTo` 下发 DTP 的问题。修复后关系子查询参数来源独立、干净，恢复"每次查询仅带单个方向关系条件"的预期语义。采用调用端修复（方案 3）：`updateRelatedPumps` 不再复用主查询 `queryParam`，改为构造干净参数。

## Background（已确认事实）

问题链路（仅 FLOOR 实例分支触发）：

1. `RunningMonitorSewageStrategy.listEquipments`（`common/.../strategy/impl/RunningMonitorSewageStrategy.java:91`）创建唯一 `queryParam`，第 99 行传给 `listEquipmentInByFloorIds`。
2. `DtpRwdClientServiceImpl.listEquipmentInByFloorIds`（DtpRwdClientServiceImpl.java:948-956）未防御性复制，直接向入参写入 `relationFrom(EQUIPMENT_IN_FLOOR, objTo=楼层)`，查询结束后残留在同一对象上。
3. 同一 `queryParam` 在第 126 行传入 `updateRelatedPumps`，第 184-190 行被修改 `classCode`/`infoCode` 并复制为三个并发副本，副本继承残留 `relationFrom`。
4. `DtpRwdClientServiceImpl.listSubmersibleSewagePumpByEquipmentIds`（DtpRwdClientServiceImpl.java:911-913）`dtpRwdConvert.copy` 逐字段复制（DtpRwdConvertImpl.java:642 含 relationFrom/relationTo）后 `setRelationTo(EQ_IN_EQ, objFrom=集水坑)` → 双关系同时下发。

影响与对照：

- 泵查询被额外附加"泵须在楼层内"的条件，非本楼层泵被静默过滤；若因此查空还会错误走旧关系回退分支。
- PROJECT 分支（第 101 行 `listEquipment`）不修改入参，无此问题。
- `listLocationInsideByEquipmentIds`（:891-894）已在提交 746826a4c0 中做防御性复制，不受影响。

已有工程能力：

- 单测基建：JUnit5 + Mockito；`common/src/test/.../structure/common/strategy/impl/` 已有同类策略测试（RunningMonitorLightingStrategyTest 等）；`ContextUtil`、`BusinessThreadPoolExecutor.ttlExecutor()` 均有既有测试用法，可纯 Mockito 构造策略实例验证。

## Requirements

- R1 关系子查询参数独立构造：`updateRelatedPumps` 不再复用/修改 `listEquipments` 的 `queryParam`，改为构造仅含关系查询所需字段的干净参数（`classCode=WSSTSP`、`infoCode=MONITORING_SURVEILLANCE_STATUS`+列表、`withColumns` 沿用原查询），确保 `relationFrom`/`relationTo` 均为空，由被调服务方法自行设置方向条件。
- R2 行为等价：关系查询的 `withColumns`、信息点过滤、`classCode` 与改造前一致；`listSpaces` → `listEquipments` 链路及 PROJECT 分支行为不变；三个并发查询仍各用独立参数副本。
- R3 不改动 `DtpRwdClientServiceImpl` 对外行为：`listEquipmentInByFloorIds` 修改入参的隐患本身不修（避免影响 Security/Parking/Instance 等其他调用方）；不在被调服务方法内清理关系字段。
- R4 新增聚焦回归测试：`RunningMonitorSewageStrategyTest`（`common/src/test/.../structure/common/strategy/impl/`），模拟 FLOOR 场景并模仿 `listEquipmentInByFloorIds` 的入参污染行为，断言关系查询捕获参数无残留 `relationFrom`（修复前失败、修复后通过），同时断言 `classCode=WSSTSP`、`infoCode=MONITORING_SURVEILLANCE_STATUS` 保持。

## Acceptance Criteria

- [ ] FLOOR 实例下，调用端传给 `listSubmersibleSewagePumpByEquipmentIds` 的参数无残留 `relationFrom`；服务方法内部仍按原逻辑设置唯一方向关系（新关系 `relationTo(EQ_IN_EQ)`；回退 `relationFrom(EQ_LOCATION_IN_EQ)`）。
- [ ] 关系查询参数 `classCode=WSSTSP`、`infoCode=MONITORING_SURVEILLANCE_STATUS`、`withColumns` 与改造前等价。
- [ ] PROJECT 实例链路与 `listSpaces` 链路行为不变。
- [ ] 新增聚焦回归测试通过：FLOOR 场景下断言关系查询捕获参数无残留 `relationFrom`；该测试在修复前呈红色（能复现问题）。
- [ ] `common` 模块编译通过。

## Out of Scope

- 不修 `DtpRwdClientServiceImpl.listEquipmentInByFloorIds` 修改入参的隐患（方案 1，用户排除）。
- 不在被调服务方法内清理关系字段（方案 2，用户认为冗余）。
- 不调整 DTP 侧对双关系条件的处理。