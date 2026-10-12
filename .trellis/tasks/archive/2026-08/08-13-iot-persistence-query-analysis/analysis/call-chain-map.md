# 调用链路图

## 总览：IOT 数据查询的4条主链路

```
┌─────────────────────────────────────────────────────────────────────┐
│                    业务层调用入口                                     │
│  (Controller → Service → Strategy → 信息点配置框架)                    │
└───────────────┬───────────────┬───────────────┬─────────────────────┘
                │               │               │
    ┌───────────▼───┐  ┌────────▼──────┐  ┌─────▼──────────────┐
    │ 链路1: 实时值  │  │ 链路2: 最近一小时│  │ 链路3: 持久化查询  │
    │ queryIpData   │  │ listInfoCode   │  │ queryObjectId     │
    │ OnMoment      │  │ ValueInOneHour │  │ PersistentData*   │
    └───────┬───────┘  └────────┬───────┘  └─────┬──────────────┘
            │                   │                  │
            │                   │                  │
    ┌───────▼───────────────────▼──────────────────▼──────────────┐
    │           DtpRwdClientServiceImpl (Feign客户端)               │
    │  queryIpDataOnMoment()  /  queryIpDataOnMomentInOneHour()   │
    │  queryIPDataInPeriod()                                       │
    └───────────────────────────┬─────────────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │ AbstractDtpPersistServer│
                    │ queryIPDataOnMoment()   │
                    │ listIpDataInPeriod()    │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼───────────┐
                    │    RWD 持久化数据平台    │
                    └───────────────────────┘
```

## 链路1: 实时值查询（queryIpDataOnMoment）

```
入口方法:
  queryIotRealData() / queryIotRealDataVO() / queryIotRealDataList()
  queryIotRealDataMap() / queryIotRealDataListByList()
  listInfoCodeValue() / getInfoCodeValueBy60Minute()

调用链:
  AbstractIotDataCacheServer.queryIotRealData()
    → IotDataCacheServerCloud.queryIotRealDataVO()
      → IotDataCacheServerCloud.queryIotRealDataList()
        → IotDataCacheServerCloud.queryIotRealDataListByList()
          ┌─────────────────────────────────────────────────────┐
          │ 1. bizDtpRwdService.queryObjectList()              │
          │    → 查询对象ID → classCode 映射                    │
          │ 2. bizDtpRwdService.queryIpDataOnMoment()           │
          │    → DtpRwdClientServiceImpl.queryIpDataOnMoment()  │
          │      → ipType = "15min" (硬编码)                    │
          │      → 无 startTime/endTime                        │
          │      → abstractDtpPersistServer.queryIPDataOnMoment()│
          └─────────────────────────────────────────────────────┘
          → processInfoCodeConfigAndValue() 处理结果

参数传递:
  - ipType: 硬编码 "15min" (DtpRwdClientServiceImpl L1158)
  - 时间参数: 不传递 (查询最新时刻)
  - ⚠️ 参数覆盖: 发送请求前调用 substitutionParameter(periodDTO) (L1160)，
    当请求上下文 isNeedReplacedSystem=true 时，ipType/startTime/endTime 会被
    context.extra 中的 IPTYPE/IPDATA_START_TIME/IPDATA_END_TIME 覆盖 (L1242-1250)

    覆盖值来源（详见 impact-analysis.md 风险3）:
    ① 前端 HTTP 请求头 IPDataStartTime/IPDataEndTime/IPType/runningMonitorType
       (CustomHeaderThreadLocalInterceptor L36-41)
    ② 后端白名单 checkSystemReplacementTime: 6类运行监测系统统一5min精度
       (CommonConst L169-180; 调用点 CloudIotDataEvent L223,
        AirConditioning{CH,ColdSource,HeatSource}PageCardDataHandler L90/92/97)
    ③ ColdSourceHomePageCardDataHandler L128-133 自行设置 extra

ipType传递路径:
  DtpRwdClientServiceImpl.queryIpDataOnMoment() → periodDTO.setIpType("15min")
  → abstractDtpPersistServer.queryIPDataOnMoment(groupCode, projectId, null, null, periodDTO)
  → RWD HTTP请求

调用者统计: 约 23 处 (queryIotRealData* 系列) + 22 处 (listInfoCodeValue 系列, 含 1 处跨行调用)
             + 8 处 (queryIotRealDataListByList 系列) + 1 处 (queryCommonIotRealDataList)
```

## 链路2: 最近一小时查询（listInfoCodeValueInOneHour）

```
入口方法:
  listInfoCodeValueInOneHour(objectIds, infoCodes)

调用链:
  IotDataCacheServerCloud.listInfoCodeValueInOneHour()
    → bizDtpRwdService.queryIpDataOnMomentInOneHour(objectIds, infoCodes)
      → DtpRwdClientServiceImpl.queryIpDataOnMomentInOneHour(objectIds, infoCodes)
        ┌──────────────────────────────────────────────────────────┐
        │ periodDTO.setIpType("15min")                // 硬编码    │
        │ periodDTO.setStartTime(当前时间 - 1小时)     // 硬编码    │
        │ periodDTO.setEndTime(当前时间)               // 硬编码    │
        │ substitutionParameter(periodDTO)                        │
        │ abstractDtpPersistServer.queryIPDataOnMoment(           │
        │   groupCode, projectId, null, null, periodDTO)          │
        └──────────────────────────────────────────────────────────┘
  → getDtpPointInfo() 有效性校验 (反馈值取最近一小时)
    → LocalDateTime.now() - 60min，早于此时间的数据丢弃

参数传递:
  - ipType: 硬编码 "15min" (DtpRwdClientServiceImpl L1193)
  - startTime: 硬编码 currentTimeOneHourAgo() (L1194)
  - endTime: 硬编码 getNowTimeStr() (L1195)
  - ⚠️ 参数覆盖: 同样调用 substitutionParameter(periodDTO) (L1197)，覆盖值来源同链路1（见上）

调用者统计: 约 36 处 (含安防、值班、客流、停车、照明、系统图、信息点配置、通风、仪表盘等模块)
```

## 链路3: 持久化查询（queryIpDataInPeriod）

```
入口方法:
  queryObjectIdPersistentDataList() / queryObjectIdPersistentDataMap()
  queryObjectIdInfoCodeHistory() / listInfoCodeValueByCloud()

调用链 (以 queryObjectIdPersistentDataList 为例):
  AbstractIotDataCacheServer.queryObjectIdPersistentDataList(ipType, objectIds, infoCodeList, startTime, endTime, isReplace)
    → abstractCommonServer.getRwdInfo(objectIds, infoCodeList)
      → 构建 EnergyDataQueryParam (对象ID + 信息点列表)
    → bodyDto.setStartTime(startTime)       // 来自调用方传入
    → bodyDto.setEndTime(endTime)           // 来自调用方传入
    → bodyDto.setIpType(ipType.value())     // 来自调用方传入
    → bodyDto.setDataStatus(1)
    → bizDtpRwdService.queryIpDataInPeriod(bodyDto)
      → DtpRwdClientServiceImpl.queryIpDataInPeriod()
        → abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, queryParam)
          → RWD HTTP请求
    → iotConverter.toInfoCodeValueVo() 转换结果

参数传递:
  - ipType: 从调用方传入 (DtpRwdPersistenceTimeTypeEnum)
  - startTime/endTime: 从调用方传入 (String)

ipType来源:
  - IotPersistenceInfoCodeAnalyzeStrategy: 从配置模型的 timeType 解析
  - IotPersistenceMultiInfoCodeAnalyzeStrategy: 从配置模型的 timeType 解析
  - IotPersistenceTrendInfoCodeAnalyzeStrategy: 从配置模型的 timeType 解析
  - IotStatisticsInfoCodeAnalyzeStrategy: 硬编码 FIVE_MINUTES
  - StrategyServiceImpl: 硬编码 FIFTEEN_MINUTES
  - DataBoardServiceImpl: 硬编码 FIFTEEN_MINUTES (L209/L310/L361)
  - ControlConsoleStrategyServerCloud: 硬编码 FIFTEEN_MINUTES
  - WeatherServiceImpl: 从方法参数传入
  - PassengerFlowServiceImpl: 硬编码 HOUR (L70) / DAY (L109)
  - PassengerFlowHomePageCardDataHandler: 硬编码 FIFTEEN_MINUTES (L70, queryObjectIdInfoCodeHistory)

调用者统计: 约 15 处 (通过抽象类: 12 persistent + 3 queryObjectIdInfoCodeHistory) + 7 处直接调用底层 (见下)
```

## 链路4: 云端特定查询（listInfoCodeValueByCloud / listInfoCodeValue1dByCloud）

```
listInfoCodeValueByCloud(ipType, objectIds, infoCodes, startTime, endTime):
  → 构建 EnergyDataQueryParam
  → abstractDtpPersistServer.listIpDataInPeriod(energyDataQueryParam)
    → RWD HTTP请求

listInfoCodeValue1dByCloud(objectIds, infoCodes):
  → dtpRwdClientService.queryIpDataOnMoment(objectIds, infoCodes, DAY, null, null)
    → periodDTO.setIpType("DAY")  // 硬编码
    → periodDTO.setDataStatus(1)
    → abstractDtpPersistServer.queryIPDataOnMoment(...)

调用者:
  - AbstractRunAnalysisReportStrategy L446: listInfoCodeValueByCloud(DAY, ...)
  - AbstractRunAnalysisReportStrategy L1014: listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)
  - RunAnalysisReportRunOverview L341: listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)
  - RunAnalysisReportDownloadService L788: listInfoCodeValueByCloud(DAY, ...)
  - RunAnalysisReportDownloadService L1358: listInfoCodeValueByCloud(FIFTEEN_MINUTES, ...)
  - RunAnalysisReportDownloadService L1533: listInfoCodeValueByCloud(DAY, ...)
  - RunAnalysisReportEnvQuality L406/436/496: listInfoCodeValueByCloud(...)
  - RunAnalysisReportEnergyAnalysis L119/255: listInfoCodeValueByCloud(...)
  - RunAnalysisReportCoolingEnergy L147: listInfoCodeValueByCloud(...)
  - RunAnalysisReportCoolingTempTarget L154: listInfoCodeValueByCloud(...)
  - RunAnalysisReportCoolingSysRun L992: listInfoCodeValueByCloud(...)
  - AirConditioningControlZones L257/289/475/487: listInfoCodeValueByCloud(...)
  - AirConditioningColdSourcePageCardDataHandler L206: listInfoCodeValue1dByCloud(...)
```

## 绕过 AbstractIotDataCacheServer 的直接调用

```
CloudIotDataEvent (SSE推送)
  → bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)
    → DtpRwdClientServiceImpl.queryIpDataOnMoment()
      → ipType="15min" (硬编码), 无时间参数

ColdSourceHomePageCardDataHandler (值班冷源卡片)
  → dtpRwdClientService.queryIpDataOnMoment(new HashSet<>(objIds), dataInfos, DAY, null, null)
    → DtpRwdClientServiceImpl.queryIpDataOnMoment(3参版本)
      → ipType="DAY" (显式传入), 无时间参数

DashBoardServiceImpl (仪表盘)
  → dtpRwdClientService.queryIpDataOnMoment(systemId, infoCodes, ipType, start, end)

AbstractRunningMonitorServer (运行监测)
  → dtpRwdClientService.queryIpDataOnMoment(systemId, infoCodes, ipType, null, null)

InfoCodeSetExtendConfigAnalyzeStrategy (扩展配置策略)
  → bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)
    → ipType="15min" (硬编码), 无时间参数

AirTerminalServiceImpl (空调末端) L2379
  → dtpRwdClientService.queryIPDataInPeriod(..., "15min", start, end)

ShellConfigServiceImpl (壳配置) L1430
  → dtpRwdClientService.queryIPDataInPeriod(..., "1d", start, end)

RunAnalysisServiceImpl (运行分析) L342
  → abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, queryParam)

SpaceAnalyseTaskHandler (空间分析定时任务, saas-compute) L259
  → dtpRwdClientService.queryIPDataInPeriod(..., "5min", beginTime, endTime)

SpaceAnalyseTaskHandlerV2 (空间分析定时任务V2, saas-compute) L295
  → dtpRwdClientService.queryIPDataInPeriod(..., "15min", beginTime, endTime)

ObjectInfoCodeGroupTask (信息点分组定时任务) L186
  → abstractRunningMonitorServer.queryCommonIotRealDataList(groupCode, projectId, objectId, allInfoCodeConfigs)
    → IotDataCacheServerCloud.queryIotRealDataList() → bizDtpRwdService.queryIpDataOnMoment()

SystemStatisticsServiceImpl (系统统计) L309-313
  → bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, groupCode)  // 3参重载
    → abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, param)
    → ipType="1d", startTime=当天0点, endTime=now (累计客流量 accdInPeopleFlow)

DashBoardServiceImpl (仪表盘 batchQueryPeriodData) L553
  → abstractDtpPersistServer.listIpDataInPeriod(groupCode, projectId, energyDataQueryParam)
    → ipType=enumTime.getName() 显式传入, 时间范围显式传入
```

## IotPersistenceInfoCodeAnalyzeStrategy 信息点配置策略链路

```
前端请求 (获取设备/空间信息点值)
  → Controller
    → Service
      → InfoCodeGatewayImpl
        → buildIotDataContext()  [批量查询IOT数据上下文]
          → abstractIotDataCacheServer.listInfoCodeValueInOneHour(allObjectIds, allIotInfoCodes)
            → 链路2 (最近一小时查询)
          → abstractIotDataCacheServer.queryObjectIdIotSetInfoCodeAndRecoverTime(aggregates)
            → 查询IOT设定信息点恢复时间
        → analyzeInfoCodeConfig()  [按信息点类型分发策略]
          → InfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
            ├── IotPersistenceInfoCodeAnalyzeStrategy
            │     → queryObjectIdPersistentDataList(timeType, ...) → 链路3
            ├── IotPersistenceMultiInfoCodeAnalyzeStrategy
            │     → queryObjectIdPersistentDataList(timeType, ...) → 链路3
            ├── IotPersistenceTrendInfoCodeAnalyzeStrategy
            │     → queryObjectIdPersistentDataList(timeType, ...) → 链路3
            ├── IotStatisticsInfoCodeAnalyzeStrategy
            │     → queryObjectIdInfoCodeHistory(FIVE_MINUTES, ...) → 链路3
            ├── IotCloudStatisticsInfoCodeAnalyzeStrategy
            │     → AbstractCommonServer.queryObjectIdInfoCodeHistory(FIVE_MINUTES, ...) → 链路3
            ├── IotInfoCodeHandlerAnalyzeStrategy
            │     → listInfoCodeValueInOneHour(objectIds, infoCodes) → 链路2
            ├── IotSettingInfoCodeAnalyzeStrategy / IotRunningInfoCodeAnalyzeStrategy
            │     → 委派给 BaseIotInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()
            │       → 从 IotDataContext 中取数据 (无需额外查询)
            └── BasicInfoCodeAnalyzeStrategy
                  → 从 RWD 对象属性查询 (非IOT查询)
```