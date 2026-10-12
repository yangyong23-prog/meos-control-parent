# 云端IOT持久化信息点查询调用链路分析报告

> 分析日期：2026-08-13
> 分析范围：meos-control-parent 项目（saas/ + common/ 模块）
> 分析目标：为"移除查询实时值的 ipType 和时间参数"需求提供现状全景

---

## 一、总体架构概述

### 1.1 类层次结构

```
AbstractIotDataCacheServer<T>  (common模块, 抽象基类)
├── IotDataCacheServerCloud<DtpPointInfo>     (saas/meos-control-common, 云端实现)
└── IotDataCacheServerEdge<FeedbackInfoMsgMqttVO>  (local/meos-control-common-edge, 边缘端实现)
```

云端实现 `IotDataCacheServerCloud` 通过 Feign 调用 RWD（运行时数据平台）的两个核心查询接口：

1. **`queryIpDataOnMoment`** — 查询**最近时刻**的 IOT 数据（内部固定 ipType="15min"，无时间范围）
2. **`queryIpDataInPeriod`** — 查询**时间段内**的持久化 IOT 数据（需要 ipType + startTime + endTime）

### 1.2 底层查询链路

```
AbstractIotDataCacheServer / 业务Service
  → BizDtpRwdService (业务层)
    → DtpRwdClientService / DtpRwdClientServiceImpl (Feign客户端)
      → AbstractDtpPersistServer (持久化数据服务)
        → dtpRwdClient / abstractDtpPersistServer 发送HTTP请求到RWD
```

### 1.3 关键接口分工

| 查询类别 | 底层方法 | ipType | 时间参数 | 数据范围 |
|----------|----------|--------|----------|----------|
| **实时值查询** | `queryIpDataOnMoment` | 硬编码"15min" | 无 | 最新时刻 |
| **最近一小时** | `queryIpDataOnMomentInOneHour` | 硬编码"15min" | 当前时间-1h ~ 现在 | 最近1小时 |
| **持久化查询(时段)** | `queryIpDataInPeriod` | 显式传入 | 显式传入 | 指定时段 |
| **云端特定时刻** | `queryIpDataOnMoment(3参)` | 显式传入 | 可选传入 | 指定时刻精度 |

---

## 二、核心方法清单与ipType/时间参数使用分析

凡**涉及 ipType 和时间的调用点**都将是后续修改的重点。以下按方法分组详述。

### 2.1 AbstractIotDataCacheServer (抽象基类定义)

#### 2.1.1 `queryIotRealData*` 系列（实时值查询）

这些方法通过
`bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` 
→ `dtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)`
→ `abstractDtpPersistServer.queryIPDataOnMoment(groupCode, projectId, null, null, periodDTO)`
查询，内部 ipType 固定为 "15min"，**不传时间参数**。

涉及的方法签名：
- `queryIotRealData(groupCode, projectId, objectId, infoCodeCfg)` — 第67行
- `queryIotRealData(groupCode, projectId, objectId, classCode, infoCode)` — 第69行
- `queryIotRealDataVO(groupCode, projectId, objectId, infoCodeCfg)` — 第71行
- `queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode)` — 第73行
- `queryIotRealDataList(groupCode, projectId, objectId, infoCodeCfgs)` — 第87行
- `queryIotRealDataMap(groupCode, projectId, objectId, infoCodes)` — 第96行
- `queryIotRealDataList(groupCode, projectId, objectId, classCode, infoCodes)` — 第109行

**ipType使用**: 硬编码 "15min"（在 DtpRwdClientServiceImpl.queryIpDataOnMoment 第1158行）
**时间参数使用**: 无
**影响评估**: 这些方法已经在用"无时间参数"的方式查询，但 ipType="15min" 是硬编码的。

#### 2.1.2 `listInfoCodeValue` 系列（最近60分钟查询）

方法调用链：
`listInfoCodeValue` → `queryIotRealDataListByList` → `bizDtpRwdService.queryIpDataOnMoment` + 知识库数据填充

涉及方法：
- `getInfoCodeValueBy60Minute(projectId, classCode, objectId, infoCode)` — 第203行
- `listInfoCodeValue(projectId, classCode, objectId, infoCodes)` — 第220行
- `listInfoCodeValue(projectId, classCode, objectIds, infoCode)` — 第236行
- `listInfoCodeValue(projectId, classCode, objectIds, infoCodes)` — 第252行（抽象方法）

**ipType使用**: 硬编码 "15min"（最终调用 DtpRwdClientServiceImpl 第1158行）
**时间参数使用**: 无

#### 2.1.3 `listInfoCodeValueInOneHour`（最近一小时数据查询）

方法调用链：
`listInfoCodeValueInOneHour(objectIds, infoCodes)`
→ `bizDtpRwdService.queryIpDataOnMomentInOneHour`
→ `dtpRwdClientService.queryIpDataOnMomentInOneHour`
→ `abstractDtpPersistServer.queryIPDataOnMoment` with ipType="15min", startTime=1h前, endTime=now

**ipType使用**: 硬编码 "15min"（DtpRwdClientServiceImpl 第1193行）
**时间参数使用**: 硬编码 startTime=当前时间-1小时, endTime=当前时间（第1194-1195行）

#### 2.1.4 `listInfoCodeValueByCloud`（云端信息点数据查询, 7.250602.0版本）

方法调用链：
`listInfoCodeValueByCloud(ipType, objectIds, infoCodes, startTime, endTime)`
→ `abstractDtpPersistServer.listIpDataInPeriod(energyDataQueryParam)`
  - `ipType`: 从参数传入 → 设置到 `energyDataQueryParam.ipType`
  - `startTime/endTime`: 从参数传入，格式化后设置

**ipType使用**: **显式传入**（第309行：`energyDataQueryParam.setIpType(ipType.value())`）
**时间参数使用**: **显式传入**（第310-311行）
**影响评估**: 本方法**直接暴露了ipType和时间参数**，是改造的重点目标之一。

#### 2.1.5 `listInfoCodeValue1dByCloud`（云端天级数据查询）

方法调用链：
`listInfoCodeValue1dByCloud(objectIds, infoCodes)`
→ `dtpRwdClientService.queryIpDataOnMoment(objectIds, infoCodes, DtpRwdPersistenceTimeTypeEnum.DAY, null, null)`
  - `ipType`: **DAY**（第278行硬编码）
  - `startTime/endTime`: **null**

**ipType使用**: 硬编码 DAY
**时间参数使用**: null（不传）
**影响评估**: ipType 已固定，但若改用壳配置控制，需要在此处判断。

#### 2.1.6 `queryObjectIdPersistentDataList`（持久化数据查询）

方法调用链：
`queryObjectIdPersistentDataList(ipType, objectIds, infoCodeList, startTime, endTime, isReplace)`
→ `abstractCommonServer.getRwdInfo(objectIds, infoCodeList)` → 构建 EnergyDataQueryParam
→ `bizDtpRwdService.queryIpDataInPeriod(bodyDto)`

**ipType使用**: **显式传入**（第491行：`bodyDto.setIpType(ipType.value())`）
**时间参数使用**: **显式传入**（第489-490行）
**影响评估**: 本方法是持久化数据查询的核心方法，ipType和时间参数直接传递给底层。

#### 2.1.7 `queryObjectIdPersistentDataMap`（持久化数据Map查询）

直接委派给 `queryObjectIdPersistentDataList`（第520行），ipType 和 startTime/endTime 由本方法计算（当天0点~现在）。

**ipType使用**: **显式传入**
**时间参数使用**: 由本方法计算（DayUtils.getStartTimeOfDayString / getNowTimeString）

#### 2.1.8 `queryObjectIdInfoCodeHistory`（IOT历史数据查询）

**ipType使用**: **显式传入**（第454行参数）
**时间参数使用**: 硬编码 startTime=24小时前, endTime=现在

#### 2.1.9 `queryObjectIdPersistentDataList` / `queryObjectIdInfoCodeHistory` on AbstractCommonServer

与 AbstractIotDataCacheServer 上的同名方法类似，直接调用 `bizDtpRwdService.queryIpDataInPeriod(bodyDto)`。
**ipType使用**: **显式传入**
**时间参数使用**: 硬编码（24小时前~现在）

### 2.2 IotDataCacheServerCloud 云端实现

#### queryIotRealDataListByList（核心批量查询方法）

第345-367行，被 `listInfoCodeValue` 委派调用。
内部流程：
1. 根据 objectIds 查询中台对象列表
2. 调用 `bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)`
3. 通过 `processInfoCodeConfigAndValue` 处理结果

**ipType使用**: 硬编码 "15min"（委派给 queryIpDataOnMoment）
**时间参数使用**: 无

### 2.3 DtpRwdClientServiceImpl 底层实现

#### `queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` — 第1156-1169行

```java
periodDTO.setIpType("15min");  // 硬编码
// startTime/endTime 不设置
abstractDtpPersistServer.queryIPDataOnMoment(groupCode, projectId, null, null, periodDTO);
```

#### `queryIpDataOnMomentInOneHour(objectIds, infoCodes)` — 第1191-1208行

```java
periodDTO.setIpType("15min");                              // 硬编码
periodDTO.setStartTime(DateUtils.currentTimeOneHourAgo());  // 硬编码
periodDTO.setEndTime(DateUtils.getNowTimeStr());            // 硬编码
```

#### `queryIpDataOnMoment(objectIds, infoCodes, ipType, startTime, endTime)` — 第1211-1225行

（3参数的版本）ipType 和时间参数**显式传入**，用于 `listInfoCodeValue1dByCloud` 等云端查询。

---

## 三、完整调用链路图（按业务模块）

### 3.1 运行监测模块 (RunningMonitor)

```
前端API: /running-monitor/batchQueryInfoCode
  → RunningMonitorServiceImpl.batchQueryInfoCode()                     [148行]
    → AbstractIotDataCacheServer.listInfoCodeValue()                   [168行]
    → AbstractIotDataCacheServer.queryIotRealData()                    [133行]
        → IotDataCacheServerCloud.queryIotRealData()
          → queryIotRealDataVO → queryIotRealDataList → bizDtpRwdService.queryIpDataOnMoment()

AbstractRunningMonitorServer
  → queryIotRealData(groupCode, projectId, objectId, classCode, infoCode)  [多个位置: 345,582,719,986行]
  → queryIotRealDataMap(groupCode, projectId, spaceIds, infoCodes)          [719行]
  → dtpRwdClientService.queryIpDataOnMoment(3参)                             [334行] (直接调用)
  → queryCommonIotRealDataList()                                             [1157行] (委托给抽象类)

RunningMonitorServerCloud (SaaS)
  → queryIotRealData()        [55-62行]
  → queryIotRealDataVO()      [85-121行]

ObjectInfoCodeGroupTask (定时任务)
  → abstractRunningMonitorServer.queryCommonIotRealDataList()              [186行]
    → IotDataCacheServerCloud.queryIotRealDataList()
```

### 3.2 工况策略模块 (Strategy)

```
StrategyServiceImpl (SaaS)
  → queryObjectIdPersistentDataList(ipType, objectIds, infoCodes, ...)    [159行]
    → AbstractIotDataCacheServer.queryObjectIdPersistentDataList()
      → bizDtpRwdService.queryIpDataInPeriod()
  → queryObjectIdPersistentDataList()                                     [237行]

DataBoardServiceImpl (SaaS)
  → queryObjectIdPersistentDataList()                                     [209行]
  → queryObjectIdPersistentDataList()                                     [310行]
  → queryObjectIdPersistentDataList()                                     [361行]

ControlConsoleStrategyServerCloud (SaaS)
  → queryObjectIdPersistentDataList()                                     [74行]

SpaceAnalyseTaskHandler (saas-compute, 定时任务)
  → dtpRwdClientService.queryIPDataInPeriod(..., "5min")                   [259行] (直接调用)

SpaceAnalyseTaskHandlerV2 (saas-compute, 定时任务)
  → dtpRwdClientService.queryIPDataInPeriod(..., "15min")                  [295行] (直接调用)
```

### 3.3 空调末端模块 (AirTerminal)

```
AirTerminalServiceImpl
  → listInfoCodeValueInOneHour(objectIds, infoCodes)                      [1115行]
  → listInfoCodeValue(projectId, null, objectIds, infoCodes)              [1442行]
  → listInfoCodeValue(projectId, null, objectIds, infoCodes)              [1454行]
  → listInfoCodeValueInOneHour(objectIds, infoCodes)                      [1657行]
  → abstractIotDataCacheServer.listInfoCodeValue(objectIds, infoCodes)    [2077行] (lambda)
  → queryIotRealData(groupCode, projectId, objectId, ...)                 [2209行]
  → dtpRwdClientService.queryIPDataInPeriod(..., "15min")                  [2379行] (直接调用)
```

### 3.4 系统图模块 (SystemGraph)

```
ConsumptionSystemGraphService
  → listInfoCodeValueInOneHour(objectIdList, infoCodeList)               [625行]
  → listInfoCodeValueInOneHour(objectIdList, infoCodeList)               [651行]
  → listInfoCodeValueInOneHour()                                         [1063行]
  → listInfoCodeValueInOneHour()                                         [1077行]

SystemGraphEditorServiceImpl
  → listInfoCodeValueInOneHour()                                         [661行, 796行]

RiskEventService
  → listInfoCodeValue(projectId, null, equipIdList, infoCodes)           [534行]
```

### 3.5 信息点配置框架 (InfoCodeConfig + AnalyzeStrategy)

```
InfoCodeConfigServiceImpl.filterDynamicInfoCode()
  → abstractIotDataCacheServer.listInfoCodeValue(projectId, null, ...)   [564行]

InfoCodeConfigQueryContext (request context)
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour(allObjectIds, allIotInfoCodes)  [238行]

InfoCodeGatewayImpl.buildIotDataContext()
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour()              [642行]
  → abstractIotDataCacheServer.queryObjectIdIotSetInfoCodeAndRecoverTime() [650行]

BaseObjectQueryParam (通用对象查询参数基类)
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, infoCodes) [468行]

AlarmMessageAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, allInfoCodes) [121行] (异步并行)

QualityIntervalInfoCodeAnalyzeStrategy.mapObjectIdInfoCodeAndValue()
  → abstractIotDataCacheServer.listInfoCodeValue(projectId, null, ...)   [217行]

ShellConfigServiceImpl
  → abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodesByCloud() [716行] (孪生规则，非数据查询)
  → dtpRwdClientService.queryIPDataInPeriod(..., "1d")                    [1430行] (直接调用)
```

### 3.6 信息点解析策略 (InfoCodeAnalyzeStrategy)

```
IotPersistenceInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractIotDataCacheServer.queryObjectIdPersistentDataList(timeTypeEnum, ...)  [85行]

IotPersistenceMultiInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractIotDataCacheServer.queryObjectIdPersistentDataList(timeType, ...)      [86行]

IotPersistenceTrendInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractIotDataCacheServer.queryObjectIdPersistentDataList(timeTypeEnum, ...)  [72行]

IotStatisticsInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractIotDataCacheServer.queryObjectIdInfoCodeHistory(FIVE_MINUTES, ...)     [62行]

IotCloudStatisticsInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
  → abstractCommonServer.queryObjectIdInfoCodeHistory(FIVE_MINUTES, ...)           [63行]

IotInfoCodeHandlerAnalyzeStrategy.mapObjectIdInfoCodeAndModels()
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, infoCodes)    [193行]
```

### 3.7 运行分析报表 (RunAnalysis)

```
AbstractRunAnalysisReportStrategy
  → listInfoCodeValueByCloud(DAY, objectIds, infoCodes, null, null)                 [446行]
  → listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)                                  [1014行]

RunAnalysisReportRunOverview
  → listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)                                  [341行]

RunAnalysisReportDownloadService
  → listInfoCodeValueByCloud(DAY, ...)                                              [788行]
  → listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)                                  [1358行]
  → listInfoCodeValueByCloud(DAY, ...)                                              [1533行]

RunAnalysisReportEnvQuality
  → listInfoCodeValueByCloud(...)                                                   [406, 436, 496行]

RunAnalysisReportEnergyAnalysis
  → listInfoCodeValueByCloud(...)                                                   [119, 255行]

RunAnalysisReportCoolingEnergy
  → listInfoCodeValueByCloud(...)                                                   [147行]

RunAnalysisReportCoolingTempTarget
  → listInfoCodeValueByCloud(...)                                                   [154行]

RunAnalysisReportCoolingSysRun
  → listInfoCodeValueByCloud(...)                                                   [992行]

AirConditioningControlZones
  → listInfoCodeValueByCloud(...)                                                   [257, 289, 475, 487行]

RunAnalysisServiceImpl.queryRwdObjectValueMap()
  → abstractDtpPersistServer.listIpDataInPeriod(...)                                [342行] (直接调用)
```

### 3.8 照明模块 (Lighting)

```
AbstractLightingService
  → queryIotRealDataListByList(groupCode, projectId, objectIdList, infoCodeList)   [1520行]
  → queryIotRealDataListByList(projectId, circuitIds, ...)                         [2476行]
  → queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes)          [2507行]
  → queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes)          [2700行]
  → queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes)          [2764行]
  → listInfoCodeValueInOneHour(circuitIds, finalInfoCodes)                         [3176行]
  → queryIotRealDataListByList(groupCode, projectId, objectIdList, infoCodeList)   [3191行]
  → queryIotRealDataListByList(projectId, circuitIds, {MANUAL_AUTO_SET})           [3669行]
  → queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode)        [2878行]
  → queryIotRealDataVO(groupCode, projectId, projectId, "", OUTDOOR_ILLUMINANCE)   [2908行]
```

### 3.9 电梯模块 (Elevator)

```
ElevatorServiceImpl
  → queryIotRealDataMap(groupCode, projectId, objectIds, {LOCK_STATUS})            [100行]
  → queryIotRealDataMap(groupCode, projectId, objectIds, {RUN_STATUS})             [103行]
  → queryIotRealDataMap(groupCode, projectId, objectIds, {ONLINE_STATUS})          [106行]
```

### 3.10 安防模块 (Security)

```
SecurityServiceImpl
  → listInfoCodeValueInOneHour()                                                   [494行]
  → listInfoCodeValueInOneHour(equipmentIdList, infoCodeList)                      [601行]
  → listInfoCodeValueInOneHour()                                                   [737行]
  → listInfoCodeValueInOneHour(equipmentIdList, infoCodeList)                      [863行]
  → listInfoCodeValueInOneHour(equipmentIds, ...)                                  [1072行]

SecurityDashboardQueryContext
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour()                        [422行]

RunningMonitorSecurityStrategy
  → listInfoCodeValueInOneHour(equipmentIdList, infoCodeList)                      [300行, 375行]
```

### 3.11 值班模块 (Duty/HomePage)

```
ElevatorHomePageCardDataHandler
  → listInfoCodeValue(projectId, null, ids, infoCodes)                             [150行]
  → listInfoCodeValue(projectId, null, ids, infoCodes)                             [165行]

WaterSupplyHomePageCardDataHandler
  → listInfoCodeValue(projectId, null, ids, infoCodes)                             [146, 187, 213行]

RunAnalysisHomePageCardDataHandler
  → listInfoCodeValue(projectId, null, spaceIds, TDB)                              [128行]

PowerDistributionHomePageCardDataHandler
  → listInfoCodeValue(projectId, null, ruleIds, {L_U_LOAD_RATE})                   [75行]

ParkingHomePageCardDataHandler
  → listInfoCodeValue(projectId, null, ids, {PARKING_STATUS})                      [69行]

AirConditioningCHPageCardDataHandler
  → CommonConst.checkSystemReplacementTime(AIR_CONDITIONING_C_H_SOURCE)            [90行] (设置覆盖上下文, 5min精度窗口)
  → listInfoCodeValueInOneHour(systemIds, QUERY_SYSTEM_IOT_INFO_CODES)             [193行]

AirConditioningColdSourcePageCardDataHandler
  → CommonConst.checkSystemReplacementTime(AIR_CONDITIONING_COLD_SOURCE)           [92行] (设置覆盖上下文)
  → listInfoCodeValueInOneHour(systemIds, QUERY_SYSTEM_IOT_INFO_CODES)             [204行]
  → listInfoCodeValue1dByCloud(systemIds, QUERY_SYSTEM_IOT_INFO_CODES_CLOUD)       [206行]

AirConditioningHeatSourcePageCardDataHandler
  → CommonConst.checkSystemReplacementTime(AIR_CONDITIONING_HEAT_SOURCE)           [97行] (设置覆盖上下文)
  → listInfoCodeValueInOneHour(systemIds, QUERY_SYSTEM_IOT_INFO_CODES)             [228行]

ColdSourceHomePageCardDataHandler (值班冷源卡片)
  → 自行构建 extra: RUNNING_MONITOR_TYPE=COLD_SOURCE + 5min精度 + 10分钟窗口      [128-133行] (设置覆盖上下文)
  → listInfoCodeValue(projectId, null, objectIds, infoCodes)                       [268行]
  → dtpRwdClientService.queryIpDataOnMoment(objectIds, ..., ipType=DAY)             [274行] (直接使用dtpRwdClientService)
  → listInfoCodeValueInOneHour(objects, RUN_OFFLINE)                               [392行]

HeatSourceHomePageCardDataHandler
  → listInfoCodeValueInOneHour(objIds, infoCode)                                   [246, 266行]

SecurityHomePageCardDataHandler
  → listInfoCodeValueInOneHour(equipmentIds, SECURITY_INFO_CODES)                  [261行]

PassengerFlowHomePageCardDataHandler
  → queryObjectIdInfoCodeHistory(FIFTEEN_MINUTES, objectIds, infoCodeList)         [70行]
  → listInfoCodeValueInOneHour(objectIds, infoCodeList)                            [71, 113行]
```

### 3.12 系统统计/能耗/客流

```
SystemStatisticsServiceImpl
  → queryIotRealDataMap(groupCode, projectId, objectIds, {ONLINE_STATUS})          [252行]
  → listInfoCodeValue(projectId, null, objectSets, infoCodes)                      [205行] (跨行调用)
  → bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, groupCode)             [309-313行] (直接调用, ipType="1d")

TerminalStatisticsServiceImpl
  → listInfoCodeValue(projectId, null, ids, {RUN_STATUS})                          [76行]
  → listInfoCodeValue(projectId, null, equipIds, infoCodes)                        [137行]

PassengerFlowServiceImpl
  → listInfoCodeValueInOneHour(gateGroupId, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST) [68行]
  → queryObjectIdPersistentDataMap(HOUR, gateGroupId, GATE_GROUP_INFO_CODE_LIST)   [70行]
  → queryObjectIdPersistentDataList(DAY, ...)                                      [109行]

VentilationServiceImpl
  → listInfoCodeValue(projectId, null, spaceIds, infoCodes)                        [149行]
  → listInfoCodeValue(projectId, null, equipIds, infoCodes)                        [287行]

ParkingServiceImpl (停车场)
  → listInfoCodeValueInOneHour(equipIds, statusInfoCodes)                          [316行]
  → listInfoCodeValueInOneHour(parkingSpacesIds, {PARKING_STATUS})                 [676行]

WeatherServiceImpl
  → queryObjectIdPersistentDataList(ipType, objectIds, infoCodeList, ...)          [173行]

WeatherServerServiceImpl
  → queryIotRealDataList(groupCode, projectId, objectId, classCode, infoCodes)     [102行]
```

### 3.13 调试工具

```
DebugToolServiceImpl
  → listInfoCodeValue(projectId, null, objectIds, infoCodes)                       [193行]

DebugController
  → queryIotRealDataVO()                                                           [87行]
```

### 3.14 SSE推送事件

```
CloudIotDataEvent
  → bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes) [258行] (直接使用BizDtpRwdService)
  → CommonConst.checkSystemReplacementTime(runningMonitorType)                       [223行] (设置覆盖上下文, 冷热源类系统统一5min精度)
```

### 3.15 仪表盘

```
DashBoardServiceImpl
  → dtpRwdClientService.queryIpDataOnMoment(systemId, infoCodes, ipType, start, end) [148行] (直接使用dtpRwdClientService)
  → abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, param)        [553行] (batchQueryPeriodData, 直接调用, ipType/time显式传入)
  → abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIds, ABNORMAL_STATE_INFO_CODES) [827行] (listAbnormalState, 在离线/控返不一致查询)
```

### 3.16 公共/云端实现 (CommonServerCloud)

```
CommonServerCloud (SaaS)
  → queryIotRealDataListByList(groupCode, projectId, objectIdList, infoCodeList)  [213行]
```

### 分类 A：硬编码 ipType="15min"，无时间参数（实时值查询）

这些方法通过 `queryIpDataOnMoment` 查询最新时刻数据。

| 方法 | 所在类 | ipType | 时间参数 |
|------|--------|--------|----------|
| `queryIotRealData()` | AbstractIotDataCacheServer | 硬编码15min | 无 |
| `queryIotRealDataVO()` | AbstractIotDataCacheServer | 硬编码15min | 无 |
| `queryIotRealDataList()` | AbstractIotDataCacheServer | 硬编码15min | 无 |
| `queryIotRealDataMap()` | AbstractIotDataCacheServer | 硬编码15min | 无 |
| `listInfoCodeValue()` | AbstractIotDataCacheServer | 硬编码15min | 无 |
| `queryIotRealDataListByList()` | IotDataCacheServerCloud | 硬编码15min | 无 |

**影响调用点数量**: 约 23 处（queryIotRealData* 系列）+ 22 处（listInfoCodeValue 系列，含 1 处跨行调用）+ 8 处（queryIotRealDataListByList 系列）

#### 补充：`substitutionParameter` 参数覆盖机制

`DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` 和 `queryIpDataOnMomentInOneHour` 在发送请求前都会调用 `substitutionParameter(periodDTO)`（L1160、L1197）：

```java
private void substitutionParameter(QueryIPDataInPeriodDTO periodDTO) {
    Map<String, Object> extra = ContextUtil.getContext().getExtra();
    if (CommonConst.isNeedReplacedSystem(extra)) {
        periodDTO.setStartTime(...extra.get(CommonConst.IPDATA_START_TIME)...);
        periodDTO.setEndTime(...extra.get(CommonConst.IPDATA_END_TIME)...);
        periodDTO.setIpType(extra.get(CommonConst.IPTYPE).toString());
        periodDTO.setDataStatus(1);
        ...
    }
}
```

即：当请求上下文标记 `isNeedReplacedSystem` 时，ipType 和时间参数会被 context.extra 中的值**覆盖**。同样的覆盖逻辑也存在于 `AbstractIotDataCacheServer.queryObjectIdPersistentDataList`（L479-486）。

**覆盖值的 3 个来源**（第四轮核验补全）：
1. **前端 HTTP 请求头**：`CustomHeaderThreadLocalInterceptor.preHandle`（L36-41）将请求头 `IPDataStartTime`/`IPDataEndTime`/`IPType`/`runningMonitorType` 写入 context.extra —— 前端不改 API body 也能影响 ipType/时间，改造必须确认其兼容性
2. **后端白名单机制**：`CommonConst.checkSystemReplacementTime`（L169-180）对 6 类运行监测类型（冷热源/冷源/空调冷源/空调热源/空调CH源/光伏，见 `SYSTEMS_THAT_NEED_TO_BE_REPLACED` L111-119）主动设置 5min 精度时间窗口（“统一数据时间”）；调用点 4 处：`CloudIotDataEvent` L223、`AirConditioningCHPageCardDataHandler` L90、`AirConditioningColdSourcePageCardDataHandler` L92、`AirConditioningHeatSourcePageCardDataHandler` L97
3. **值班卡片自行设置**：`ColdSourceHomePageCardDataHandler.execute`（L128-133）自行构建 extra（COLD_SOURCE + 5min 精度 + 10分钟窗口）

这是"ipType/时间参数"的第二来源体系，改造时必须一并考虑；详见 impact-analysis.md 风险3。

### 分类 B：硬编码 ipType="15min"，硬编码时间范围（最近一小时）

| 方法 | 所在类 | ipType | 时间参数 |
|------|--------|--------|----------|
| `listInfoCodeValueInOneHour()` | AbstractIotDataCacheServer | 硬编码15min | 硬编码(-1h~now) |

**影响调用点数量**: 约 36 处（含安防、值班、客流、停车、照明、系统图、信息点配置、通风、仪表盘等模块，详见补充清单）

### 分类 C：显式传入 ipType 和时间参数（持久化/云端查询）

| 方法 | 所在类 | ipType | 时间参数 |
|------|--------|--------|----------|
| `queryObjectIdPersistentDataList()` | AbstractIotDataCacheServer | **显式传入** | **显式传入** |
| `queryObjectIdPersistentDataMap()` | AbstractIotDataCacheServer | **显式传入** | 计算当天0点~now |
| `queryObjectIdInfoCodeHistory()` | AbstractIotDataCacheServer | **显式传入** | 24小时前~now |
| `queryObjectIdInfoCodeHistory()` | AbstractCommonServer | **显式传入** | 24小时前~now |
| `listInfoCodeValueByCloud()` | AbstractIotDataCacheServer | **显式传入** | **显式传入** |
| `listInfoCodeValue1dByCloud()` | AbstractIotDataCacheServer | 硬编码 DAY | null |

**影响调用点数量**: 约 34 处（queryObjectIdPersistentDataList/Map 12 + queryObjectIdInfoCodeHistory 3 + listInfoCodeValueByCloud 18 + listInfoCodeValue1dByCloud 1）

### 分类 D：直接调用 DtpRwdClientService/BizDtpRwdService/AbstractDtpPersistServer（绕过抽象类）

| 调用位置 | 调用的方法 | ipType | 时间参数 |
|----------|-----------|--------|----------|
| `CloudIotDataEvent` L258 | `bizDtpRwdService.queryIpDataOnMoment` | 硬编码15min | 无 |
| `ColdSourceHomePageCardDataHandler` L274 | `dtpRwdClientService.queryIpDataOnMoment` | **显式传入 DAY** | 无 |
| `DashBoardServiceImpl` L148 | `dtpRwdClientService.queryIpDataOnMoment` | 带ipType | 带时间 |
| `AbstractRunningMonitorServer` L334 | `dtpRwdClientService.queryIpDataOnMoment` | 带ipType | null,null |
| `InfoCodeSetExtendConfigAnalyzeStrategy` L382,433 | `bizDtpRwdService.queryIpDataOnMoment` | 硬编码15min | 无 |
| `AirTerminalServiceImpl` L2379 | `dtpRwdClientService.queryIPDataInPeriod` | 硬编码"15min" | **显式传入** |
| `ShellConfigServiceImpl` L1430 | `dtpRwdClientService.queryIPDataInPeriod` | 硬编码"1d" | **显式传入** |
| `RunAnalysisServiceImpl` L342 | `abstractDtpPersistServer.listIpDataInPeriod` | **显式传入** | **显式传入** |
| `SpaceAnalyseTaskHandler` L259 (saas-compute) | `dtpRwdClientService.queryIPDataInPeriod` | 硬编码"5min" | **显式传入** |
| `SpaceAnalyseTaskHandlerV2` L295 (saas-compute) | `dtpRwdClientService.queryIPDataInPeriod` | 硬编码"15min" | **显式传入** |
| `ObjectInfoCodeGroupTask` L186 (定时任务) | `abstractRunningMonitorServer.queryCommonIotRealDataList` | 硬编码15min | 无 |
| `SystemStatisticsServiceImpl` L309-313 | `bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, groupCode)` | 硬编码"1d" | **显式传入** |
| `DashBoardServiceImpl` L553 | `abstractDtpPersistServer.listIpDataInPeriod` | **显式传入(enumTime)** | **显式传入** |

**影响调用点数量**: 约 13 处（绕过抽象类直接调用底层服务）

> 补充：`queryObjectIdInfoCodeHistory` 的第 3 个调用者 `PassengerFlowHomePageCardDataHandler` L70（FIFTEEN_MINUTES）已在 3.11 节记录；`SystemStatisticsServiceImpl` L205-206 存在跨行调用形式的 `listInfoCodeValue`（方法名与 `(` 分行），已在 3.12 节记录。

---

## 五、InfoCodeAnalyzeStrategy 子类分析

`IotPersistenceInfoCodeAnalyzeStrategy`（以及同族的策略类）是信息点配置框架中专门处理 IOT 持久化数据的策略。

### 5.1 策略注册机制

```java
// InfoCodeTypeEnum → Strategy Bean 映射
IOT_PERSISTENCE             → IotPersistenceInfoCodeAnalyzeStrategy
IOT_PERSISTENCE_MULTI       → IotPersistenceMultiInfoCodeAnalyzeStrategy
IOT_PERSISTENCE_TREND       → IotPersistenceTrendInfoCodeAnalyzeStrategy
IOT_STATISTIC               → IotStatisticsInfoCodeAnalyzeStrategy
IOT_CLOUD_STATISTIC         → IotCloudStatisticsInfoCodeAnalyzeStrategy
IOT_INFO_CODE_HANDLER       → IotInfoCodeHandlerAnalyzeStrategy
IOT_RUNNING                 → IotSettingInfoCodeAnalyzeStrategy
IOT_SETTING                 → IotRunningInfoCodeAnalyzeStrategy
BASIC                       → BasicInfoCodeAnalyzeStrategy
```

### 5.2 各策略的查询调用

| 策略类 | 调用的方法 | 传入的 ipType | 传入的时间参数 |
|--------|-----------|--------------|---------------|
| `IotPersistenceInfoCodeAnalyzeStrategy` | `queryObjectIdPersistentDataList()` | **从配置解析 timeType** | **从配置或参数传入** |
| `IotPersistenceMultiInfoCodeAnalyzeStrategy` | `queryObjectIdPersistentDataList()` | **从配置解析 each timeType** | **从参数传入** |
| `IotPersistenceTrendInfoCodeAnalyzeStrategy` | `queryObjectIdPersistentDataList()` | **从配置解析 timeType** | 硬编码(当天0点~now) |
| `IotStatisticsInfoCodeAnalyzeStrategy` | `queryObjectIdInfoCodeHistory()` | **硬编码 FIVE_MINUTES** | 24小时前~now |
| `IotCloudStatisticsInfoCodeAnalyzeStrategy` | `AbstractCommonServer.queryObjectIdInfoCodeHistory()` | **硬编码 FIVE_MINUTES** | 24小时前~now |
| `IotInfoCodeHandlerAnalyzeStrategy` | `listInfoCodeValueInOneHour()` | 硬编码15min | 硬编码(-1h~now) |

### 5.3 策略调用链

```
前端请求（获取信息点数据）
  → Controller
    → InfoCodeConfigServiceImpl (或 XXXService)
      → InfoCodeGatewayImpl.buildIotDataContext()
        → listInfoCodeValueInOneHour()  + queryObjectIdIotSetInfoCodeAndRecoverTime()  [批量获取IOT数据上下文]
      → InfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
        → 各策略内部调用 AbstractIotDataCacheServer 对应方法
```

---

## 六、直接调用底层服务的风险点

以下调用点**绕过了** `AbstractIotDataCacheServer` 的方法封装，直接调用了 `DtpRwdClientService` 或 `BizDtpRwdService`：

1. **CloudIotDataEvent.java L258** — SSE推送事件中直接调用 `bizDtpRwdService.queryIpDataOnMoment`
2. **ColdSourceHomePageCardDataHandler.java L274** — 值班冷源卡片中直接调用 `dtpRwdClientService.queryIpDataOnMoment` 且传递了 DAY ipType
3. **DashBoardServiceImpl.java L148** — 仪表盘中直接调用 `dtpRwdClientService.queryIpDataOnMoment` 带 ipType 和时间
4. **AbstractRunningMonitorServer.java L334** — 运行监测抽象服务中直接调用 `dtpRwdClientService.queryIpDataOnMoment`
5. **InfoCodeSetExtendConfigAnalyzeStrategy.java L382/433** — 扩展配置策略中直接调用 `bizDtpRwdService.queryIpDataOnMoment`
6. **AirTerminalServiceImpl.java L2379** — 空调末端直接调用 `dtpRwdClientService.queryIPDataInPeriod`（"15min"）
7. **ShellConfigServiceImpl.java L1430** — 壳配置直接调用 `dtpRwdClientService.queryIPDataInPeriod`（"1d"）
8. **RunAnalysisServiceImpl.java L342** — 运行分析直接调用 `abstractDtpPersistServer.listIpDataInPeriod`
9. **SpaceAnalyseTaskHandler.java L259 / SpaceAnalyseTaskHandlerV2.java L295** — 空间分析定时任务直接调用 `dtpRwdClientService.queryIPDataInPeriod`（"5min"/"15min"）
10. **ObjectInfoCodeGroupTask.java L186** — 信息点分组定时任务调用 `abstractRunningMonitorServer.queryCommonIotRealDataList`（→ `queryIotRealDataList`）
11. **SystemStatisticsServiceImpl.java L309-313** — 系统统计直接调用 `bizDtpRwdService.listIpDataInPeriod`（ipType="1d"，累计客流量查询）
12. **DashBoardServiceImpl.java L553** — 仪表盘 `batchQueryPeriodData` 直接调用 `abstractDtpPersistServer.listIpDataInPeriod`（ipType/时间显式传入）

这些点如果后续要统一通过壳配置控制 ipType 和时间参数，需要额外改造。

---

## 七、受影响的业务功能模块清单

| 模块 | 影响级别 | 主要调用的方法 | 说明 |
|------|---------|--------------|------|
| 空间/设备通用查询 (Instance) | **中** | `listInfoCodeValueInOneHour`（经 BaseObjectQueryParam 过滤） | listSpaces/listEquipments/listObject 等 |
| 运行监测 (RunningMonitor) | **高** | `queryIotRealData*`, `queryIotRealDataMap`, `listInfoCodeValue` | 核心依赖实时IOT数据 |
| 空调模块视图 (AirConditioning) | **中** | `listInfoCodeValueInOneHour`（经 RunningMonitorAirConditioningColdSourceStrategy） | listEquipmentsToModuleView |
| 工况策略 (Strategy) | **高** | `queryObjectIdPersistentDataList` | 策略计算依赖持久化数据 |
| 空调末端 (AirTerminal) | **高** | `listInfoCodeValueInOneHour`, `listInfoCodeValue`, `queryIPDataInPeriod`(直接) | 温度和CO2查询 |
| 系统图 (SystemGraph) | **高** | `listInfoCodeValueInOneHour` | 设备状态查询（含 RiskEventService） |
| 信息点配置框架 | **高** | `listInfoCodeValueInOneHour`, `queryObjectIdPersistentDataList` | 核心解析策略 + BaseObjectQueryParam 通用过滤 |
| 运行分析报表 (RunAnalysis) | **高** | `listInfoCodeValueByCloud`(19处), `listIpDataInPeriod`(直接) | 报表数据生成，ipType/时间高度敏感 |
| 照明 (Lighting) | **高** | `queryIotRealDataListByList`(7处), `queryIotRealDataVO`, `listInfoCodeValueInOneHour` | 回路状态/反馈查询 |
| 电梯 (Elevator) | **中** | `queryIotRealDataMap` | 状态查询 |
| 安防 (Security) | **高** | `listInfoCodeValueInOneHour`(8处含仪表盘上下文与监测策略) | 设备状态查询 |
| 值班首页 (Duty/HomePage) | **高** | `listInfoCodeValue`(9处), `listInfoCodeValueInOneHour`(9处), `listInfoCodeValue1dByCloud`(1处) | 首页卡片数据 |
| 通风 (Ventilation) | **中** | `listInfoCodeValue`(2处) | 空间/设备状态查询 |
| 系统统计 (SystemStatistics/TerminalStatistics) | **中** | `queryIotRealDataMap`, `listInfoCodeValue`(2处) | 统计查询 |
| 客流 (PassengerFlow) | **中** | `queryObjectIdPersistentDataMap`, `queryObjectIdPersistentDataList`, `listInfoCodeValueInOneHour` | 客流数据 |
| 停车场 (Parking) | **中** | `listInfoCodeValueInOneHour` | 停车位状态 |
| 天气 (Weather) | **低** | `queryObjectIdPersistentDataList`, `queryIotRealDataList` | 天气数据 |
| 壳配置 (ShellConfig) | **低** | `queryIPDataInPeriod`(直接, "1d") | 壳配置历史数据 |
| 调试工具 (Debug) | **低** | `queryIotRealDataVO`, `listInfoCodeValue` | 调试功能 |
| SSE推送 | **低** | `bizDtpRwdService.queryIpDataOnMoment` | 事件推送 |
| 仪表盘 (Dashboard) | **低** | `dtpRwdClientService.queryIpDataOnMoment`(直接) | 仪表盘数据 |
| 定时任务 (xxl-job) | **中** | `queryIPDataInPeriod`(直接), `queryCommonIotRealDataList` | 空间分析、信息点分组任务 |

---

## 八、风险评估与兼容性分析

### 8.1 潜在风险

1. **边缘端不受影响**：`IotDataCacheServerEdge` 使用不同的查询路径（通过 `DtpTwinClient` 查询边缘孪生数据），不需要改造。但注意 `AbstractIotDataCacheServer` 的修改不应影响 `IotDataCacheServerEdge` 的行为。

2. **ipType 硬编码风险**：当前大量调用点硬编码 ipType="15min"，如果在 `queryIpDataOnMoment` 底层统一移除 ipType，需确保 RWD 侧有默认值逻辑。

3. **时序数据精度风险**：不同业务场景使用不同的 ipType（DAY, FIFTEEN_MINUTES, MONTH, YEAR 等），移除 ipType 参数可能导致时序数据查询精度不符合预期。

4. **时间范围硬编码风险**：`listInfoCodeValueInOneHour` 硬编码了-1h的时间范围，部分业务依赖此时间窗口进行有效性判断（如安防模块 `getDtpPointInfo` 方法中反馈值最近一小时有效性校验）。

5. **绕过抽象类的直接调用**：13处直接调用底层服务的代码点需要单独处理，无法通过修改 AbstractIotDataCacheServer 统一覆盖（首轮识别6处，第二轮补全至11处，第三轮补全至13处）。

6. **壳配置的生效时机**：壳配置为 DB 存储（RcBasicsConfig），变更后下次查询即生效；如需缓存，需考虑缓存失效策略。

### 8.2 兼容性要求

- **前端 API 入参不变**: 所有 Controller 层的接口签名不需要修改
- **边缘端行为不变**: 边缘端 `IotDataCacheServerEdge` 不要受影响
- **本地测试不受影响**: 壳配置默认值（同一时刻点）下行为与现状一致，且需前端请求头标识同时满足才生效

### 8.3 关注点：`getDtpPointInfo` 方法中的一小时有效性校验

`IotDataCacheServerCloud.getDtpPointInfo()` 第438-440行：

```java
// 反馈值，取最近一小时
LocalDateTime currentTime = DateUtils.now().withSecond(0).withNano(0).plusMinutes(-60);
return currentTime.isBefore(dateTime) || currentTime.equals(dateTime) ? pointInfo : null;
```

如果修改了 `queryIpDataOnMoment` 的查询方式，返回的 `ipTime` 范围可能变化，需要确认此处的一小时有效性校验逻辑是否需要同步调整。