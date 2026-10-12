# 影响范围分析与潜在风险评估

## 一、改造目标理解

根据产品文档【PRD-260901】和最新方案调整，本次改造的核心目标是：

> **壳配置新增"云端持久化数据显示规则"分组；前端在冷热源相关接口请求头加标识，当壳配置选择"查询距离当前最新的数据"时，后端按（当前时间-间隔 ~ 当前时间）查询且不传分精度 ipType；其余情况一切按现有逻辑执行**

需要明确以下概念：

1. **"查询实时值"** 指的是通过 `queryIpDataOnMoment` 查询最新时刻 IOT 数据的链路（链路1和链路2），而不是通过 `queryIpDataInPeriod` 查询历史时段数据的链路（链路3）。
2. **生效三条件**：前端请求头 `removeDefaultIpType=true` + 壳配置"查询距离当前最新的数据" + 项目级壳配置隔离（RcBasicsConfig.projectId 天然支持）。
3. 产品影响范围限定：冷源、冷热源、热源、空调冷源、空调热源、空调冷热源（空调系列暂不改动）。

## 二、按业务场景的影响分析

### 场景1：设备状态实时查询（影响范围：广）

**涉及的方法**: `queryIotRealData*`, `listInfoCodeValue`, `queryIotRealDataMap`
**当前 ipType 处理**: 硬编码 "15min"（最终调用 `queryIpDataOnMoment`）
**当前时间参数**: 无

**改造建议**:
- 在 `DtpRwdClientServiceImpl.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` 底层增加三条件判断：前端请求头 `removeDefaultIpType=true` 且壳配置选择"查询距离当前最新的数据"时，时间范围=（当前-间隔 ~ 当前）、不传 ipType；否则按现有逻辑执行
- **关键原则**：本次改造只解决"默认值"的问题——显式指定了分精度类型的查询（如 DAY、1d、5min 等）是产品功能需求，保持指定的分精度不变；5 参版（显式传 ipType）与覆盖机制（前端 IPType 请求头/白名单）不受影响
- 壳配置新增分组"云端持久化数据显示规则"：持久化数据查询逻辑（默认"查询同一个时刻点的数据"）+ 距离当前的时间间隔（默认 1h，1-168）
- 影响约 53 处调用点（queryIotRealData* 23 + listInfoCodeValue 22 + queryIotRealDataListByList 8），但只需修改底层 1 个方法；**行为变化仅限前端加标识 + 壳配置"最新数据"的链路**

**兼容性**: 高（默认壳配置值下行为不变）

### 场景2：最近一小时数据查询（影响范围：广）

**涉及的方法**: `listInfoCodeValueInOneHour`
**当前 ipType 处理**: 硬编码 "15min"
**当前时间参数**: 硬编码 startTime=-1h, endTime=now

**改造建议**:
- 与场景1类似，在 `DtpRwdClientServiceImpl.queryIpDataOnMomentInOneHour` 底层修改
- 三条件满足时：时间范围=（当前-间隔 ~ 当前）、不传 ipType；否则保持现有 -1h 窗口
- 注意 `getDtpPointInfo` 方法中有反馈值最近一小时有效性校验，需同步评估

**兼容性**: 中（需评估时间窗口变化对业务逻辑的影响）

### 场景3：持久化数据查询（影响范围：中）

**涉及的方法**: `queryObjectIdPersistentDataList`, `queryObjectIdPersistentDataMap`, `queryObjectIdInfoCodeHistory`
**当前 ipType 处理**: **显式传入**（来自调用方）
**当前时间参数**: **显式传入**（来自调用方或硬编码）

**改造建议**:
- 这些方法是"历史数据查询"而非"实时值查询"，ipType 和时间参数对于历史数据查询是必要条件
- 建议**保持现状不做修改**

**兼容性**: 风险较低（保持现状）

### 场景4：云端特定数据查询（影响范围：中）

**涉及的方法**: `listInfoCodeValueByCloud`, `listInfoCodeValue1dByCloud`
**当前 ipType 处理**: **显式传入**（listInfoCodeValueByCloud）或硬编码 DAY（listInfoCodeValue1dByCloud）
**当前时间参数**: **显式传入**（listInfoCodeValueByCloud）

**改造建议**:
- 这些方法主要被运行分析报表模块调用
- ipType 和时间参数是报表生成的核心入参，不建议移除
- 建议保持现状

**兼容性**: 风险较低（保持现状）

### 场景5：信息点配置策略层（影响范围：中）

**涉及的方法**: 各 InfoCodeAnalyzeStrategy 子类的 `fillInfoCodeDataModel`
**当前 ipType 处理**: 从配置模型的 `timeType` 解析或硬编码
**当前时间参数**: 从配置/参数传入或硬编码

**改造建议**:
- 策略层负责的是"数据解析"而非"数据获取"，ipType 和时间参数来自业务配置
- 是否需要修改取决于底层 `queryObjectIdPersistentDataList` 是否改变签名
- 建议保持策略层不变，仅调整底层方法

**兼容性**: 较低（取决于底层方法签名是否变化）

## 三、潜在风险点详细分析

### 风险1：`getDtpPointInfo` 一小时有效性校验

**位置**: `IotDataCacheServerCloud.java` 第438-440行

```java
// 反馈值，取最近一小时
LocalDateTime currentTime = DateUtils.now().withSecond(0).withNano(0).plusMinutes(-60);
return currentTime.isBefore(dateTime) || currentTime.equals(dateTime) ? pointInfo : null;
```

**风险描述**: 此方法对反馈值做了"最近一小时"的有效性校验。如果移除 `listInfoCodeValueInOneHour` 中的时间范围参数（startTime=-1h, endTime=now），此处的校验会基于 `energyDataResponse.getIpTime()` 来判断，可能因为数据时间更早而被全部过滤掉。

**缓解措施**: 与 `listInfoCodeValueInOneHour` 的时间范围改造联动评估，确保校验逻辑与查询范围一致。

### 风险2：`listInfoCodeValue` 的60分钟语义

**位置**: `getInfoCodeValueBy60Minute` 方法名暗示"60分钟"，Javadoc 也描述为"最近60分钟信息点数据"

**风险描述**: 如果移除时间限制，方法返回的不再是"最近60分钟"数据，需要修改方法命名和Javadoc，并确认调用方是否依赖此语义。

### 风险3：ipType/时间参数的覆盖机制（第四轮核验补全，三来源两生效点）

**生效点**（读取 extra 并覆盖 ipType/时间的地方）：
- `DtpRwdClientServiceImpl.substitutionParameter()` L1242-1250 — 覆盖 `queryIpDataOnMoment`(4参) 和 `queryIpDataOnMomentInOneHour` 的 periodDTO
- `AbstractIotDataCacheServer.queryObjectIdPersistentDataList` L479-486 — 当 `isReplace=true` 时覆盖 startTime/endTime/ipType
- **不生效**：`queryIpDataOnMoment` 5参版（L1211）与 `listInfoCodeValueByCloud` 不经过覆盖逻辑

**覆盖值来源一：前端 HTTP 请求头**
- `CustomHeaderThreadLocalInterceptor.preHandle` L36-41：从请求头读取 `IPDataStartTime`、`IPDataEndTime`、`IPType`、`runningMonitorType` 写入 context.extra。**前端可以不改变 API body，仅通过请求头影响查询的 ipType 和时间范围**，改造时必须确认前端是否还在使用这些 header（或改造后需保留兼容）。

**覆盖值来源二：后端主动设置（运行监测类型白名单）**
- `CommonConst.checkSystemReplacementTime` L169-180：运行监测类型 ∈ {COLD_HEAT_SOURCE, COLD_SOURCE, AIR_CONDITIONING_COLD_SOURCE, AIR_CONDITIONING_HEAT_SOURCE, AIR_CONDITIONING_C_H_SOURCE, PHOTOVOLTAIC}（`SYSTEMS_THAT_NEED_TO_BE_REPLACED` L111-119）时，设置 IPDATA_START_TIME=5分钟精度-5分钟、IPDATA_END_TIME=5分钟精度、IPTYPE="5min"，实现“统一数据时间”
- 调用点 4 处：`CloudIotDataEvent` L223、`AirConditioningCHPageCardDataHandler` L90、`AirConditioningColdSourcePageCardDataHandler` L92、`AirConditioningHeatSourcePageCardDataHandler` L97

**覆盖值来源三：值班卡片自行设置**
- `ColdSourceHomePageCardDataHandler.execute` L128-133：自行构建 extra（RUNNING_MONITOR_TYPE=COLD_SOURCE + 5min 精度 + 10分钟窗口）写入 context

**判定条件**：`CommonConst.isNeedReplacedSystem(extra)` L159-162 — extra 含 runningMonitorType 且类型在白名单内

**缓解措施**: 改造时需完整追踪这些来源，并明确门控语义：三条件（前端请求头标识 + 壳配置"最新数据" + 项目级壳配置）同时满足时按（当前-间隔 ~ 当前）查询且不传 ipType，否则现有覆盖机制（请求头/后端白名单/卡片设置）完全按旧逻辑执行、不受影响；"最新数据"模式会改变上述 6 类运行监测系统的"统一数据时间"逻辑（固定取最近 5min 精度窗口），需要评审是否同步迁移。

### 风险4：边缘端不受影响但需验证

**位置**: `IotDataCacheServerEdge` 与 `IotDataCacheServerCloud` 继承自同一抽象基类

**风险描述**: `AbstractIotDataCacheServer` 中的非抽象方法（如 `getInfoCodeValueBy60Minute`, `listInfoCodeValue` 的重载方法、`queryObjectIdPersistentDataList`、`queryObjectIdPersistentDataMap`、`listInfoCodeValueByCloud`、`listInfoCodeValue1dByCloud` 等）被两端共享。如果修改这些方法的签名或行为，需确保边缘端不受影响。边缘端调用这些继承方法的点包括：IotDataCacheServerEdge 内部（L81、L125）、LightingServiceImpl(edge) L216/612/822、CommonServerEdge L220/329、RunningMonitorServiceImpl(edge) L199/365/423、LightingCircuitPageListQueryContext L279。

### 风险5：RWD 侧默认值行为

**位置**: RWD 持久化数据平台的 `queryIPDataOnMoment` 接口

**风险描述**: 如果移除 ipType 参数，需要确认 RWD 侧是否能正确处理 null/空 ipType，或者是否有默认值逻辑。

### 风险6：壳配置变更的生效时机

**位置**: 新增壳配置分组

**风险描述**: 壳配置为 DB 存储，变更后下次查询即可生效，不存在热加载不一致问题。需要考虑：
- 壳配置读取的缓存策略（如需缓存，变更后需失效缓存）
- 项目开关与前端标识组合切换时的平滑过渡方案

### 风险7：DebugController 的特殊性

**位置**: `DebugController.java` 第87行

```java
result = this.iotDataCacheService.queryIotRealDataVO(redisCacheDTO.get("groupCode"), 
    redisCacheDTO.get("projectId"), redisCacheDTO.get("objectId"), 
    redisCacheDTO.get("classCode"), redisCacheDTO.get("infoCode"));
```

**风险描述**: 调试控制器允许用户手动传入参数，可能绕过正常业务流程。如果改造影响了实时值查询，调试功能可能行为异常。

### 风险8：`BaseObjectQueryParam` 通用过滤逻辑的隐性影响（本次核验新增）

**位置**: `BaseObjectQueryParam` L467-468 — 通用对象查询参数基类的 `filterByInfoCodeValue`（信息点值过滤）中调用了 `listInfoCodeValueInOneHour(objectIds, infoCodes)`

**风险描述**: 这是对象列表查询的公共过滤机制，所有继承该参数类的对象查询（设备、空间、回路等列表查询）在设置了 filterInfoCodes/excludeInfoCodes 时都会触发 IOT 数据查询。影响面比单个业务模块广得多，改造后需要重点回归对象列表查询场景。

### 风险9：定时任务调用链路（本次核验新增）

**位置**: `SpaceAnalyseTaskHandler` L259、`SpaceAnalyseTaskHandlerV2` L295（saas-compute）、`ObjectInfoCodeGroupTask` L186（common）

**风险描述**: 这些是 xxl-job 定时任务，直接调用 `dtpRwdClientService.queryIPDataInPeriod` / `abstractRunningMonitorServer.queryCommonIotRealDataList`。它们不受前端 API 入参约束，但同样受底层查询参数改造影响；定时任务失败通常不会立即暴露（无前端报错），需要监控日志。

### 风险10：运行分析报表精度敏感（本次核验补强）

**位置**: `RunAnalysisServiceImpl.queryRwdObjectValueMap` L342 直接调用 `abstractDtpPersistServer.listIpDataInPeriod`；`listInfoCodeValueByCloud` 的全部 18 处调用（含 RunAnalysisReportEnvQuality、RunAnalysisReportEnergyAnalysis、RunAnalysisReportCoolingEnergy、RunAnalysisReportCoolingTempTarget、RunAnalysisReportCoolingSysRun、AirConditioningControlZones）

**风险描述**: 报表模块对 ipType（DAY/FIFTEEN_MINUTES）和时间范围高度敏感，任何默认值变化都可能导致报表数据错误。原分析低估了该模块的调用点数量（原 6 处 → 实际 18 处 + 1 处直接调用）。

## 四、与前端团队对接要点

1. **改造范围**: 前端加标识 + 壳配置"最新数据"的实时值查询 API — 接口格式不变，数据内容可能变化；未加标识的 API 完全不变
2. **前端入参不变**: 所有 Controller 接口签名不变；现有 `IPType` 等请求头继续按现有方式传递
3. **前端配合（简要）**: ①在冷热源模块目标接口请求头加 `removeDefaultIpType=true`；②壳配置页新增分组"云端持久化数据显示规则"；③壳配置=最新数据时系统图/空间三维模型不显示"更新时间"

4. **测试计划**: 
   - 仅回归前端加了标识的接口对应页面（冷热源模块）
   - 重点测试最新数据模式的时间范围查询、无数据显示 --、不同信息点最新时间不一致的展示

## 五、建议的改造策略

### 方案A：最小改动（推荐）

仅在 `DtpRwdClientServiceImpl` 底层修改：
1. `queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` — 增加三条件判断：请求头标识 + 壳配置"最新数据" → 时间范围=（当前-间隔 ~ 当前）且不传 ipType
2. `queryIpDataOnMomentInOneHour(objectIds, infoCodes)` — 同上
3. 壳配置定义：`ShellConfigItemEnum` 新增 2 配置项、`ShellConfigGroupEnum` 新增分组
4. 其他方法保持现状

**优点**: 影响范围可控（仅前端加标识的接口可能变化），风险最低
**缺点**: 分类 D 的 13 处直接调用底层未覆盖（其中 SSE 推送、定时任务见设计评审文档第三章评审结论）

### 方案B：全面改造

在方案A的基础上，额外改造：
1. 分类 D 的 13 处直接调用底层统一接入三条件判断
2. `listInfoCodeValueByCloud` 等方法增加壳配置感知
3. 处理 `BaseObjectQueryParam` 通用过滤链路的参数注入

**优点**: 覆盖所有场景，统一管控
**缺点**: 改造范围大，测试成本高

### 推荐方案

采用**方案A**作为第一阶段（配合 PRD-260901 冷热源范围），SSE 推送/控制台链路按设计评审文档第三章结论逐项确认后，再评估是否扩展方案B。