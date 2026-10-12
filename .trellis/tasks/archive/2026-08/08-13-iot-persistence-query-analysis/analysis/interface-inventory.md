# 接口清单和影响范围汇总

## 一、所有受影响的类和方法清单

### 1.1 核心抽象类（common模块）

| 类 | 方法 | 行号 | ipType处理 | 时间参数处理 |
|---|---|---|---|---|
| `AbstractIotDataCacheServer` | `queryIotRealData(String,String,String,RcInfoCodeCfg)` | 67 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealData(String,String,String,String,String)` | 69 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealDataVO(String,String,String,RcInfoCodeCfg)` | 71 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealDataVO(String,String,String,String,String)` | 73 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealDataList(String,String,String,List)` | 87 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealDataMap(String,String,String,Set,Set)` | 96 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `queryIotRealDataList(String,String,String,String,List)` | 109 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `listInfoCodeValue(String,DtpRwdClassCodeEnum,String,Collection)` | 220 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `listInfoCodeValue(String,DtpRwdClassCodeEnum,Collection,String)` | 236 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `listInfoCodeValue(String,DtpRwdClassCodeEnum,Collection,Collection)` | 252 | **抽象方法→硬编码15min** | 无 |
| `AbstractIotDataCacheServer` | `getInfoCodeValueBy60Minute(String,DtpRwdClassCodeEnum,String,String)` | 203 | 委派→硬编码15min | 无 |
| `AbstractIotDataCacheServer` | `listInfoCodeValueInOneHour(Collection,Collection)` | 264 | **抽象方法→硬编码15min** | **硬编码(-1h~now)** |
| `AbstractIotDataCacheServer` | `listInfoCodeValue1dByCloud(Set,Set)` | 275 | 硬编码 DAY | null(null) |
| `AbstractIotDataCacheServer` | `listInfoCodeValueByCloud(DtpRwdPersistenceTimeTypeEnum,Collection,Collection,LocalDateTime,LocalDateTime)` | 293 | **显式传入** | **显式传入** |
| `AbstractIotDataCacheServer` | `queryObjectIdPersistentDataList(DtpRwdPersistenceTimeTypeEnum,Collection,Collection,String,String,Boolean)` | 467 | **显式传入** | **显式传入** |
| `AbstractIotDataCacheServer` | `queryObjectIdPersistentDataMap(DtpRwdPersistenceTimeTypeEnum,Collection,Collection)` | 513 | **显式传入** | 计算当天0点~now |
| `AbstractIotDataCacheServer` | `queryObjectIdInfoCodeHistory(DtpRwdPersistenceTimeTypeEnum,Collection,Collection)` | 453 | **显式传入** | 24小时前~now |
| `AbstractCommonServer` | `queryObjectIdInfoCodeHistory(DtpRwdPersistenceTimeTypeEnum,Collection,Collection)` | 580 | **显式传入** | 24小时前~now |

### 1.2 云端实现（saas/meos-control-common模块）

| 类 | 方法 | 行号 | ipType处理 | 时间参数处理 |
|---|---|---|---|---|
| `IotDataCacheServerCloud` | `listInfoCodeValue(String,DtpRwdClassCodeEnum,Collection,Collection)` | 87 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `listInfoCodeValueInOneHour(Collection,Collection)` | 159 | 委派→硬编码15min | 委派→硬编码(-1h~now) |
| `IotDataCacheServerCloud` | `queryIotRealData(String,String,String,RcInfoCodeCfg)` | 207 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealData(String,String,String,String,String)` | 213 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataVO(String,String,String,RcInfoCodeCfg)` | 219 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataVO(String,String,String,String,String)` | 225 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataList(String,String,String,List)` | 242 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataMap(String,String,String,Set,Set)` | 273 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataList(String,String,String,String,List)` | 302 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryIotRealDataListByList(String,String,Collection,Collection)` | 345 | 委派→硬编码15min | 无 |
| `IotDataCacheServerCloud` | `queryObjectIdInfoCodeHistory(DtpRwdPersistenceTimeTypeEnum,Collection,Collection)` | 452 | **显式传入** | 24小时前~now |
| `RunningMonitorServerCloud` | `多个查询方法` | 55-121 | 委派→硬编码15min | 无 |
| `ControlConsoleStrategyServerCloud` | `mapRecentRunningStatus` | 74 | **显式传入FIFTEEN_MINUTES** | 硬编码 |

### 1.3 DTP-RWD底层服务

| 类 | 方法 | 行号 | ipType处理 | 时间参数处理 |
|---|---|---|---|---|
| `DtpRwdClientServiceImpl` | `queryIpDataOnMoment(String,String,List,List)` | 1156 | **硬编码"15min"** | **不设置** |
| `DtpRwdClientServiceImpl` | `queryIpDataOnMomentInOneHour(Set,Set)` | 1191 | **硬编码"15min"** | **硬编码(-1h~now)** |
| `DtpRwdClientServiceImpl` | `queryIpDataOnMoment(Set,Set,DtpRwdPersistenceTimeTypeEnum,LocalDateTime,LocalDateTime)` | 1211 | **显式传入** | **显式传入(可为null)** |
| `DtpRwdClientServiceImpl` | `queryIPDataInPeriod(String,String,Set,Set,LocalDateTime,LocalDateTime,String)` | 1172 | **显式传入** | **显式传入** |
| `BizDtpRwdServiceImpl` | `queryIpDataInPeriod(EnergyDataQueryParam)` | 290 | 委派(param.ipType) | 委派(param.startTime/endTime) |
| `BizDtpRwdServiceImpl` | `queryIpDataOnMoment(String,String,List,List)` | 306 | 委派→硬编码15min | 无 |
| `BizDtpRwdServiceImpl` | `queryIpDataOnMomentInOneHour(Set,Set)` | 311 | 委派→硬编码15min | 委派→硬编码(-1h~now) |

### 1.4 信息点配置策略

| 类 | 方法 | 行号 | ipType处理 | 时间参数处理 |
|---|---|---|---|---|
| `IotPersistenceInfoCodeAnalyzeStrategy` | `fillInfoCodeDataModel` | 54 | **从timeType解析** | **从配置/参数传入** |
| `IotPersistenceMultiInfoCodeAnalyzeStrategy` | `fillInfoCodeDataModel` | 52 | **从timeType解析** | **从配置/参数传入** |
| `IotPersistenceTrendInfoCodeAnalyzeStrategy` | `fillInfoCodeDataModel` | 50 | **从timeType解析** | 硬编码(当天0点~now) |
| `IotStatisticsInfoCodeAnalyzeStrategy` | `fillInfoCodeDataModel` | 47 | **硬编码FIVE_MINUTES** | 24小时前~now |
| `IotCloudStatisticsInfoCodeAnalyzeStrategy` | `fillInfoCodeDataModel` | 48 | **硬编码FIVE_MINUTES** | 24小时前~now |
| `IotInfoCodeHandlerAnalyzeStrategy` | `mapObjectIdInfoCodeAndModels` | 188 | 硬编码15min | 硬编码(-1h~now) |

### 1.5 直接调用底层服务的类

| 类 | 方法 | 行号 | 调用的方法 | 说明 |
|---|---|---|---|---|
| `CloudIotDataEvent` | `queryObjectIdAndInfoCodes` | 258 | `bizDtpRwdService.queryIpDataOnMoment` | 绕过抽象类 |
| `ColdSourceHomePageCardDataHandler` | `objectInfoCodeValueStatistics` | 274 | `dtpRwdClientService.queryIpDataOnMoment(..., DAY, null, null)` | 绕过抽象类 |
| `DashBoardServiceImpl` | 多个方法 | 148 | `dtpRwdClientService.queryIpDataOnMoment` | 绕过抽象类 |
| `AbstractRunningMonitorServer` | 多个方法 | 334 | `dtpRwdClientService.queryIpDataOnMoment` | 绕过抽象类 |
| `InfoCodeSetExtendConfigAnalyzeStrategy` | `fillInfoCodeDataModel` | 382,433 | `bizDtpRwdService.queryIpDataOnMoment` | 绕过抽象类 |
| `AirTerminalServiceImpl` | 历史数据查询 | 2379 | `dtpRwdClientService.queryIPDataInPeriod("15min")` | 绕过抽象类 |
| `ShellConfigServiceImpl` | 壳配置历史数据 | 1430 | `dtpRwdClientService.queryIPDataInPeriod("1d")` | 绕过抽象类 |
| `RunAnalysisServiceImpl` | `queryRwdObjectValueMap` | 342 | `abstractDtpPersistServer.listIpDataInPeriod` | 绕过抽象类 |
| `SpaceAnalyseTaskHandler` | 空间分析定时任务 | 259 | `dtpRwdClientService.queryIPDataInPeriod("5min")` | saas-compute |
| `SpaceAnalyseTaskHandlerV2` | 空间分析定时任务V2 | 295 | `dtpRwdClientService.queryIPDataInPeriod("15min")` | saas-compute |
| `ObjectInfoCodeGroupTask` | 信息点分组定时任务 | 186 | `abstractRunningMonitorServer.queryCommonIotRealDataList` | 定时任务 |
| `SystemStatisticsServiceImpl` | 累计客流量统计 | 309-313 | `bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, groupCode)`（"1d"） | 绕过抽象类 |
| `DashBoardServiceImpl` | `batchQueryPeriodData` | 553 | `abstractDtpPersistServer.listIpDataInPeriod` | 绕过抽象类 |

### 1.6 补充：其他调用者（本次核验补全）

| 类 | 方法 | 行号 | 调用的方法 |
|---|---|---|---|
| `RunningMonitorServiceImpl` | `batchQueryInfoCode` | 168 | `listInfoCodeValue` |
| `VentilationServiceImpl` | `buildSpaceStatusVO`/`buildEquipmentStatusVO` | 149,287 | `listInfoCodeValue` |
| `TerminalStatisticsServiceImpl` | `getTerminalEquipGroupStatistics`/`getInfoCodeValueMap` | 76,137 | `listInfoCodeValue` |
| `RiskEventService` | 风险事件查询 | 534 | `listInfoCodeValue` |
| `QualityIntervalInfoCodeAnalyzeStrategy` | `mapObjectIdInfoCodeAndValue` | 217 | `listInfoCodeValue` |
| `ElevatorHomePageCardDataHandler` | `processInfoCode` | 150 | `listInfoCodeValue` |
| `PowerDistributionHomePageCardDataHandler` | `execute` | 75 | `listInfoCodeValue` |
| `ParkingHomePageCardDataHandler` | `execute` | 69 | `listInfoCodeValue` |
| `BaseObjectQueryParam` | 通用对象查询过滤 | 468 | `listInfoCodeValueInOneHour` |
| `AlarmMessageAnalyzeStrategy` | `fillInfoCodeDataModel` | 121 | `listInfoCodeValueInOneHour` |
| `RunningMonitorAirConditioningColdSourceStrategy` | 状态查询 | 180 | `listInfoCodeValueInOneHour` |
| `SecurityServiceImpl` | 安防统计 | 601,737,863,1072 | `listInfoCodeValueInOneHour` |
| `SecurityDashboardQueryContext` | 安防仪表盘 | 422 | `listInfoCodeValueInOneHour` |
| `ColdSourceHomePageCardDataHandler` | `execute` | 392 | `listInfoCodeValueInOneHour` |
| `AirConditioningCHPageCardDataHandler` | `execute` | 193 | `listInfoCodeValueInOneHour` |
| `AirConditioningColdSourcePageCardDataHandler` | `querySystemIdInfoCodeAndValues` | 204 | `listInfoCodeValueInOneHour` |
| `AirConditioningColdSourcePageCardDataHandler` | `querySystemIdInfoCodeAndValues` | 206 | `listInfoCodeValue1dByCloud` |
| `AirConditioningHeatSourcePageCardDataHandler` | `execute` | 228 | `listInfoCodeValueInOneHour` |
| `HeatSourceHomePageCardDataHandler` | `execute` | 246,266 | `listInfoCodeValueInOneHour` |
| `SecurityHomePageCardDataHandler` | `execute` | 261 | `listInfoCodeValueInOneHour` |
| `PassengerFlowHomePageCardDataHandler` | `execute` | 71,113 | `listInfoCodeValueInOneHour` |
| `PassengerFlowHomePageCardDataHandler` | `execute` | 70 | `queryObjectIdInfoCodeHistory`(FIFTEEN_MINUTES) |
| `SystemStatisticsServiceImpl` | 客流统计 | 205 | `listInfoCodeValue`（**跨行调用**：方法名与`(`分行） |
| `AbstractLightingService` | 照明回路查询 | 1520,2476,2507,2700,2764,3176,3191,3669 | `queryIotRealDataListByList`/`listInfoCodeValueInOneHour` |
| `CommonServerCloud` | 公共云端服务 | 213 | `queryIotRealDataListByList` |
| `PassengerFlowServiceImpl` | `homeStatistics` | 68,70,109 | `listInfoCodeValueInOneHour`/`queryObjectIdPersistentDataMap`(HOUR)/`queryObjectIdPersistentDataList`(DAY) |
| `ParkingServiceImpl` | 停车场设备/车位状态查询 | 316,676 | `listInfoCodeValueInOneHour` |
| `DashBoardServiceImpl` | `listAbnormalState` | 827 | `listInfoCodeValueInOneHour`（在离线/控返不一致查询） |
| `RunAnalysisReportEnvQuality` | 环境质量报表 | 406,436,496 | `listInfoCodeValueByCloud` |
| `RunAnalysisReportEnergyAnalysis` | 能耗分析报表 | 119,255 | `listInfoCodeValueByCloud` |
| `RunAnalysisReportCoolingEnergy` | 制冷能耗报表 | 147 | `listInfoCodeValueByCloud` |
| `RunAnalysisReportCoolingTempTarget` | 制冷温度目标 | 154 | `listInfoCodeValueByCloud` |
| `RunAnalysisReportCoolingSysRun` | 制冷系统运行 | 992 | `listInfoCodeValueByCloud` |
| `AirConditioningControlZones` | 空调控制区域 | 257,289,475,487 | `listInfoCodeValueByCloud` |

## 二、调用点汇总统计

| 调用方式 | 方法特征 | 调用点数 | 改造复杂度 |
|---|---|---|---|
| 通过 `listInfoCodeValue` | 委派→queryIpDataOnMoment, 硬编码15min | ~22处（含1处跨行调用） | 低（统一修改底层） |
| 通过 `queryIotRealData*` | 委派→queryIpDataOnMoment, 硬编码15min | ~23处 | 低（统一修改底层） |
| 通过 `queryIotRealDataListByList` | 委派→queryIpDataOnMoment, 硬编码15min | ~8处 | 低（统一修改底层） |
| 通过 `listInfoCodeValueInOneHour` | 硬编码15min+时间范围 | ~36处 | 中（需确认时间窗口） |
| 通过 `queryObjectIdPersistentDataList/Map` | **显式传入ipType+时间** | ~12处 | **高（需设计兼容方案）** |
| 通过 `queryObjectIdInfoCodeHistory` | **显式传入ipType** | ~3处 | **高** |
| 通过 `listInfoCodeValueByCloud` | **显式传入ipType+时间** | ~18处 | **高（需设计兼容方案）** |
| 通过 `listInfoCodeValue1dByCloud` | 硬编码 DAY | ~1处 | 中 |
| 直接调用 DtpRwdClientService 等底层 | 绕过抽象类 | ~13处 | **高（需逐一改造）** |
| 合计 | | **~136处** | |

> 注：以上为云端部署形态下的调用点统计（含 common 模块中云端可用的调用点）。
> 边缘端（local/meos-control-common-edge）有各自的实现与调用点（IotDataCacheServerEdge 内部委派 + LightingServiceImpl L216/612/822 + CommonServerEdge L220/329 + RunningMonitorServiceImpl(edge) L199/365/423 + LightingCircuitPageListQueryContext L279），不受云端改造影响，但共享 AbstractIotDataCacheServer 中的非抽象方法。

## 三、改造优先级建议

### 第一优先级：安全且影响范围大的改造

1. **`queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)`**
   - DtpRwdClientServiceImpl L1156-1169
   - 影响: 所有实时值查询（约 53 处调用点：queryIotRealData* 23 + listInfoCodeValue 22 + queryIotRealDataListByList 8）
   - 风险: 低（ipType="15min"硬编码，改为壳配置控制）

2. **`queryIpDataOnMomentInOneHour(objectIds, infoCodes)`**
   - DtpRwdClientServiceImpl L1191-1208
   - 影响: 所有"最近一小时"查询（约 36 处调用点）
   - 风险: 中（需确认`getDtpPointInfo`的一小时有效性校验是否受影响）

### 第二优先级：需要设计方案论证的改造

3. **`queryObjectIdPersistentDataList(ipType, objectIds, infoCodeList, startTime, endTime, isReplace)`**
   - 影响: 持久化数据查询（约 12 处调用点）
   - 风险: 高（涉及不同ipType精度的时序数据查询）

4. **`listInfoCodeValueByCloud(ipType, objectIds, infoCodes, startTime, endTime)`**
   - 影响: 云端特定查询（约 18 处 + 1 处直接调用，主要为运行分析报表全部策略类）
   - 风险: 高（报表数据依赖精确的ipType和时间范围）

### 第三优先级：逐一处理的改造

5. **直接调用底层服务的13处代码**
   - 需要逐个分析、逐一修改
   - 风险: 中（每处使用场景不同）
   - 注意定时任务场景（SpaceAnalyseTaskHandler L259、SpaceAnalyseTaskHandlerV2 L295、ObjectInfoCodeGroupTask L186）不受前端 API 影响，但受底层改造影响
   - 注意跨行调用与重载场景（SystemStatisticsServiceImpl L205 跨行调用；L309-313 使用 listIpDataInPeriod 3参重载）

## 四、DtpRwdPersistenceTimeTypeEnum 取值参考

```java
FIVE_MINUTES       // 5分钟精度
FIFTEEN_MINUTES    // 15分钟精度
HOUR               // 小时精度
DAY                // 天精度
MONTH              // 月精度
YEAR               // 年精度
```

当前项目中使用的 ipType 枚举值：
- 实时查询: 硬编码 "15min" (字符串)
- `listInfoCodeValue1dByCloud`: DAY
- `queryObjectIdInfoCodeHistory`: FIVE_MINUTES
- `queryObjectIdPersistentDataMap`: 入参传入
- 报表查询: DAY / FIFTEEN_MINUTES
- 策略查询: FIFTEEN_MINUTES