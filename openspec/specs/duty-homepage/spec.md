# duty-homepage Specification

## Purpose

值班首页（`/duty/homePage`）通风卡片的数据构建规范：覆盖异常事件与聚合统计的并行查询、大卡片标签字段、小卡片分组统计与按入参分组补齐。
## Requirements
### Requirement: 通风值班首页异常事件查询与聚合接口并行执行

通风值班首页 `execute` 方法 SHALL 将异常事件数量查询（`getExceptionEventCountByObjIds`）通过 `CompletableFuture.supplyAsync` 提交到 `BusinessThreadPoolExecutor.ttlExecutor()` 线程池异步执行，同时主线程执行聚合接口（`queryHomePageCardGroupIotData`）。异常事件查询只需 `objIds`（来自 `queryEquipGroupId`），不依赖聚合接口结果，因此可以并行。wall-clock 从串行之和降为两者之最大值。

#### Scenario: 正常并行执行
- **WHEN** `queryEquipGroupId` 返回非空设备分组
- **THEN** 异常事件查询通过 `CompletableFuture.supplyAsync` 异步提交，主线程同步执行聚合接口，两者完成后 `allOf().join()` 汇合结果

#### Scenario: 聚合接口返回 null 时短路
- **WHEN** 聚合接口 `queryHomePageCardGroupIotData` 返回 null
- **THEN** execute 直接返回带 `runningMonitorType` 的空 `CardDateVO`，不等待异步异常事件 future，不调用 `buildChildCardDateVO`

### Requirement: 通风值班首页大卡片只展示异常事件数

通风值班首页大卡片 `CardTagVO` SHALL 只设置 `errorEventTotal`（来自并行查询结果），`offlineTotal` 和 `orderFailAlarmTotal` SHALL 保持默认值 0。`execute` 方法不再调用父类 `buildCardTagVO`，改为内联构造 `CardTagVO`。

#### Scenario: 大卡片字段值
- **WHEN** execute 正常完成
- **THEN** 返回的 `CardTagVO` 中 `errorEventTotal` 为异常事件数量，`offlineTotal` 为 0，`orderFailAlarmTotal` 为 0

### Requirement: 通风值班首页小卡片默认可跳转

通风值班首页所有小卡片 SHALL 设置 `canJump = true`，不再通过 `queryDemandTemplateType` 查询需求模板类型来判断跳转。`execute` 方法不再调用 `queryCanJump`。

#### Scenario: 小卡片跳转标识
- **WHEN** `buildChildCardDateVO` 构建小卡片列表完成
- **THEN** 每个小卡片的 `canJump` 字段为 `true`

### Requirement: 通风值班首页小卡片按入参分组补齐

通风值班首页 `execute` SHALL 保证小卡片集合恒等于入参分组集合：在调用 `buildChildCardDateVO` 之前，以 `queryMap` 为基线补齐 `subStat` 中缺失的分组。补齐的分组 SHALL 设置 `total` 为该组入参设备数量（`objectIds.size()`），其余统计字段保持 0。

补齐 SHALL 仅遍历 `queryMap`（分组数动态，随项目设备构成变化），SHALL NOT 枚举固定设备类型或 `VentilationEquipStatisticsEnum.values()`。`subStat` 为 null 时 SHALL 以空 Map 起步。SHALL NOT 改动 `buildChildCardDateVO`、`HomePageCardIotDataCloud`、`HomePageCardIotDataEdge`、`DutyHomePageGroupDTO` 的现有逻辑。

#### Scenario: DTP 返回部分分组

- **WHEN** 聚合接口仅返回 `queryMap` 中部分分组的统计（如查询 N 组仅返回 M 组）
- **THEN** 小卡片列表 SHALL 包含全部 N 组；未返回的 N-M 组卡片 `equipmentTotal` = 该组入参设备数
- **AND** 这些补齐卡片的 `runningTotal`、`interveneTotal`、`lessOpenTotal`、`moreOpenTotal` SHALL 均为 0

#### Scenario: DTP 返回空 Map

- **WHEN** 聚合接口返回空 Map
- **THEN** 小卡片列表 SHALL 包含全部 N 组，每张卡片 `equipmentTotal` = 该组入参设备数，其余统计为 0

#### Scenario: DTP 返回全量分组

- **WHEN** 聚合接口返回 `queryMap` 中全部分组的统计
- **THEN** 小卡片列表 SHALL 与补齐前一致，SHALL NOT 出现重复卡片

#### Scenario: subStat 为 null

- **WHEN** `subStat` 为 null（边端理论路径）
- **THEN** SHALL 以空 Map 起步补齐并输出全部 N 组卡片，SHALL NOT 抛异常

#### Scenario: 分组数动态

- **WHEN** 项目实际查出的分组数为 N（可能包含窗类 CFCSWN/CFCSEW，也可能不含）
- **THEN** 卡片集合 SHALL 恒等于该 N 组，SHALL NOT 包含 `queryMap` 之外的分组

#### Scenario: 其它运行监测类型不受影响

- **WHEN** 照明/空调末端/厨房等运行监测类型执行各自的值班首页卡片构建
- **THEN** 其行为 SHALL 保持不变（本次不改动通用构建逻辑）

