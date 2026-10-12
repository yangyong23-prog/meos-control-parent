# 停车场新增项目级统计数据展示

## Goal

项目的停车系统对接第三方数据，无法获取每个车位的状态，只能提供项目级的当前空闲车位和在停车场的总车辆数。本迭代支持：

1. dashboard 页面「项目车位统计」中，**空闲车位** 展示逻辑升级（实例化规则感知 + 项目对象信息点兜底）
2. dashboard 页面 **新增「当前车辆总数」** 展示（项目对象信息点 `carNum`）

来源：【PRD-260901】停车场新增项目级统计数据展示（产品：李萌，2026.08.11），UX 稿见 Figma（node-id=116-3079）。

## Confirmed Facts（代码现状）

- 现有接口 `POST /parking/parkingSpace/statistics`（PROJECT/FLOOR 级）返回 `Map<String, Integer>`，key 为 `ParkingSpaceTypeEnum.value()`（PARKING_SPACE / VACANT_PARKING_SPACE / CHARGING_PARKING_SPACE / VACANT_CHARGING_PARKING_SPACE / FAST_CHARGING_PARKING_SPACE / VACANT_FAST_CHARGING_PARKING_SPACE）
- 现有「空闲车位」VACANT_PARKING_SPACE 无条件基于停车位（CFPKPS）信息点 `parkingStatus`（`InfoCodeEnum.PARKING_STATUS`）值 `"0"`（`ParkingSpaceStatusEnum.VACANT_PARKING_SPACE`，即"无车"）统计，核心方法 `ParkingServiceImpl.vacantParkingSpaceList()`
- 同一服务方法被 SSE 事件 `PARKING_STATISTICS`（`ParkingStatistics`）复用
- 实例化规则查询 API：`AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(classCode→objectIds, infoCodes)` → `Map<objectId, Set<infoCode>>`（云端/边缘实现自动适配）；客流模块 `PassengerFlowServiceImpl.filterByPassengerFlowRule` 为同模式先例
- 项目对象 classCode = `project`（`DtpRwdClassCodeEnum.PROJECT`）
- 项目对象作为 objectId 直接查 IOT 实时数据有先例（客流模块 `listInfoCodeValueInOneHour` 传入 projectId）
- `availableParkingSpaceNumber`、`carNum` 两个信息点在代码库中不存在，需新增 `InfoCodeEnum` 枚举（依赖字典 V5.6.23 提供 i18n key）

## Requirements

### R1 空闲车位展示逻辑（项目级）

| 场景 | 行为 |
| --- | --- |
| 存在任一停车位（CFPKPS）有 `parkingStatus` 实例化规则 | 统计 `parkingStatus`="无车" 的数量；全部获取不到数据也展示为 0 |
| 所有停车位均无 `parkingStatus` 实例化规则 | 获取**项目对象**信息点 `availableParkingSpaceNumber` 展示；获取不到数据显示为 `--` |

### R2 车辆总数展示（新增）

- 仅当**项目对象**信息点 `carNum` 有实例化规则才展示此统计
- 获取项目对象 `carNum` 值展示；获取不到数据显示为 `--`

### R3 依赖

- 字典提需，字典版本 V5.6.23（提供 `availableParkingSpaceNumber`、`carNum` 的 i18n key）

## Open Questions（阻塞规划）

- ~~接口契约改造方案~~ ✅ 已决策：在现有 `/parking/parkingSpace/statistics` 接口返回上拓展，与前端统一约束：无实例化规则不返字段（前端不展示）；有规则返字段，IOT 有值返实际值，无值不返（前端显示 `--`）；统计值无数据返 0
- ~~改造作用域~~ ✅ 已决策：仅项目级（PROJECT）；FLOOR 级无项目对象概念，保持现状
- ~~SSE 是否同步~~ ✅ 已决策：SSE `PARKING_STATISTICS` 复用同一服务方法，随接口自动同步
- ~~字典 V5.6.23 key~~ ✅ 已决策：开发不依赖字典版本，默认按已有处理；`InfoCodeEnum` 新增枚举先用项目惯例占位 key（如 `key_fan_gear` 先例），联调缺 key 时由开发通知字典相关人员

## Key Decisions

- 接口契约：现有接口返回上拓展（保持 `Map<String, Integer>`，value 可为 null），key 存在性 = 实例化规则存在性；`VACANT_PARKING_SPACE`（统计类）无 IOT 数据返 0；`CAR_TOTAL` 等点值类无 IOT 值返 null（前端 `--`）
- 作用域：仅 PROJECT 级生效新逻辑，FLOOR 级保持基于 CFPKPS parkingStatus 的现状
- 实例化规则查询复用 `AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes`（云/边自适应）
- 新信息点 `availableParkingSpaceNumber`、`carNum` 新增 `InfoCodeEnum` 枚举（依赖字典 V5.6.23）

## Acceptance Criteria

- [ ] AC1：`POST /parking/parkingSpace/statistics`（PROJECT 级）在任一停车位（CFPKPS）存在 `parkingStatus` 实例化规则时，返回 `VACANT_PARKING_SPACE` = parkingStatus="无车" 统计数；全部无 IOT 数据时返回 0
- [ ] AC2：所有停车位均无 `parkingStatus` 实例化规则时，`VACANT_PARKING_SPACE` 取值项目对象 `availableParkingSpaceNumber`；项目对象也无该规则或 IOT 无值时，按契约不返字段/返 null（前端 `--`）
- [ ] AC3：项目对象 `carNum` 有实例化规则时返回车辆总数（如 `CAR_TOTAL` key），无规则时不返回；IOT 无值时返 null
- [ ] AC4：无停车位空间的项目：CFPKPS 规则判断自然为"无规则"，走项目对象 `availableParkingSpaceNumber` 兜底路径；`carNum` 判断不受影响
- [ ] AC5：FLOOR 级统计行为与改造前一致（基于 CFPKPS parkingStatus 聚合）
- [ ] AC6：SSE `PARKING_STATISTICS` 事件推送结构与 REST 一致
- [ ] AC7：`InfoCodeEnum` 新增 `availableParkingSpaceNumber`、`carNum` 两个枚举（占位 key），编码为字典 V5.6.23 中的小驼峰写法
- [ ] AC8：改造后现有 6 个车位类型 key（PARKING_SPACE 等）的 Integer 值行为不变（PROJECT/FLOOR）

## Out of Scope

- 前端页面改造（本仓库为后端，前端迭代另行确认；接口契约以本 PRD Key Decisions 为准）
- 网格图统计（`/parkingSpace/grid/statistics`）与楼层车位统计（`/floor/parkingSpace/statistics`）的展示逻辑（保持现状）
