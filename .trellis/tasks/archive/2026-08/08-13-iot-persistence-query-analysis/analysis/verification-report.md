# 二次核验报告

> 核验日期：2026-08-13（与首轮分析同日，第二轮）
> 核验方式：以全量正则检索（`.listInfoCodeValue(`、`.listInfoCodeValueInOneHour(`、`.queryIotRealData*`、`.queryObjectIdPersistent*`、`.listInfoCodeValueByCloud`、`queryIpDataInPeriod`、`listIpDataInPeriod`、`queryIPDataOnMoment`、`bizDtpRwdService.`、`dtpRwdClientService.` 等全模式）对 4 份文档进行逐一比对

## 核验结论

首轮分析文档存在 **调用点遗漏** 和 **统计数字偏差**。本轮核验已补全全部遗漏并修正 4 份文档。以下为完整核验记录。

---

## 一、发现的遗漏调用点（已全部补入文档）

### 1.1 `listInfoCodeValue` 调用点遗漏 8 处

| 类 | 行号 | 说明 |
|---|---|---|
| `RunningMonitorServiceImpl` | 168 | batchQueryInfoCode 内 |
| `VentilationServiceImpl` | 149, 287 | buildSpaceStatusVO / buildEquipmentStatusVO |
| `TerminalStatisticsServiceImpl` | 76, 137 | 终端统计 |
| `RiskEventService` | 534 | 系统图风险事件 |
| `QualityIntervalInfoCodeAnalyzeStrategy` | 217 | 质量区间策略 |
| `ElevatorHomePageCardDataHandler` | 150 | processInfoCode |
| `PowerDistributionHomePageCardDataHandler` | 75 | 配电首页卡片（原文档误标为"间接"） |
| `ParkingHomePageCardDataHandler` | 69 | 停车首页卡片（原文档误标为"间接"） |

### 1.2 `listInfoCodeValueInOneHour` 调用点遗漏 14 处

| 类 | 行号 | 说明 |
|---|---|---|
| `AbstractLightingService` | 3176 | 照明 |
| `RunningMonitorAirConditioningColdSourceStrategy` | 180 | 运行监测-冷源 |
| `BaseObjectQueryParam` | 468 | **通用对象查询参数基类过滤逻辑，影响面最广** |
| `AlarmMessageAnalyzeStrategy` | 121 | 报警消息策略（异步并行） |
| `SecurityServiceImpl` | 601, 737, 863, 1072 | 原文档仅列 L494 |
| `SecurityDashboardQueryContext` | 422 | 安防仪表盘上下文 |
| `ColdSourceHomePageCardDataHandler` | 392 | 值班冷源卡片 |
| `AirConditioningCHPageCardDataHandler` | 193 | 空调CH值班卡片 |
| `AirConditioningColdSourcePageCardDataHandler` | 204 | 空调冷源值班卡片 |
| `AirConditioningHeatSourcePageCardDataHandler` | 228 | 空调热源值班卡片 |
| `HeatSourceHomePageCardDataHandler` | 246, 266 | 热源值班卡片 |
| `SecurityHomePageCardDataHandler` | 261 | 安防值班卡片 |
| `PassengerFlowHomePageCardDataHandler` | 71, 113 | 客流值班卡片 |
| `PassengerFlowServiceImpl` | 68 | 客流服务 |

### 1.3 `queryIotRealDataListByList` 调用点遗漏 8 处

| 类 | 行号 | 说明 |
|---|---|---|
| `AbstractLightingService` | 1520, 2476, 2507, 2700, 2764, 3191, 3669 | 照明 7 处（原文档完全未列） |
| `CommonServerCloud` | 213 | SaaS 端公共服务 |

### 1.4 `listInfoCodeValueByCloud` 调用点遗漏 13 处（运行分析模块）

| 类 | 行号 |
|---|---|
| `RunAnalysisReportEnvQuality` | 406, 436, 496 |
| `RunAnalysisReportEnergyAnalysis` | 119, 255 |
| `RunAnalysisReportCoolingEnergy` | 147 |
| `RunAnalysisReportCoolingTempTarget` | 154 |
| `RunAnalysisReportCoolingSysRun` | 992 |
| `AirConditioningControlZones` | 257, 289, 475, 487 |

原文档 6 处 → 实际 19 处，**严重低估**。

### 1.5 `listInfoCodeValue1dByCloud` 调用点遗漏 1 处

| 类 | 行号 |
|---|---|
| `AirConditioningColdSourcePageCardDataHandler` | 206 |

### 1.6 `queryObjectIdPersistentDataList` 调用点遗漏 1 处

| 类 | 行号 |
|---|---|
| `PassengerFlowServiceImpl` | 109（DAY 精度，30天日维度统计） |

### 1.7 直接调用底层服务的遗漏 6 处

| 类 | 行号 | 调用的方法 | ipType |
|---|---|---|---|
| `AirTerminalServiceImpl` | 2379 | `dtpRwdClientService.queryIPDataInPeriod` | "15min" |
| `ShellConfigServiceImpl` | 1430 | `dtpRwdClientService.queryIPDataInPeriod` | "1d" |
| `RunAnalysisServiceImpl` | 342 | `abstractDtpPersistServer.listIpDataInPeriod` | 显式传入 |
| `SpaceAnalyseTaskHandler` (saas-compute) | 259 | `dtpRwdClientService.queryIPDataInPeriod` | "5min" |
| `SpaceAnalyseTaskHandlerV2` (saas-compute) | 295 | `dtpRwdClientService.queryIPDataInPeriod` | "15min" |
| `ObjectInfoCodeGroupTask` (common) | 186 | `abstractRunningMonitorServer.queryCommonIotRealDataList` | 硬编码15min |

原文档 6 处 → 实际 11 处（含 4 处定时任务/直接调用）。

---

## 二、统计数字修正记录

| 项目 | 原文档 | 核验后 | 变化原因 |
|---|---|---|---|
| 实时值查询（链路1） | 约 35+ 处 | 约 57 处（25+21+10+1） | 原为单一估算值，现按方法族拆分 |
| 最近一小时（链路2） | 约 14 处 | 约 28 处 | 遗漏 14 处 |
| 持久化查询（链路3，通过抽象类） | 约 14 处 | 约 11 处 + 5 处直接调用 | 原统计把直接调用混入 |
| 云端特定查询（链路4） | 约 6 处 | 约 19 处 + 1 处直接调用 | 运行分析报表 6 个策略类遗漏 |
| 绕过抽象类的直接调用 | 6 处 | 11 处 | 补全 5 处 |
| **总计** | — | **约 128 处** | 新增合计行 |

---

## 三、准确性问题核验结果

### 3.1 行号核对

全部已核实的关键行号均与实际代码一致（核验方式：Read 文件 + Grep 行号比对）：

- `AbstractIotDataCacheServer`：L67/69/71/73/87/96/109/203/220/236/252/264/275/293/453/467/513 ✓
- `AbstractCommonServer.queryObjectIdInfoCodeHistory` L580 ✓
- `IotDataCacheServerCloud`：L87/159/207/213/219/225/242/273/302/345/452 ✓
- `DtpRwdClientServiceImpl`：L1156/1172/1191/1211/1242 ✓
- `BizDtpRwdServiceImpl`：L290/306/311 ✓
- 各业务调用点行号：与 Grep 结果逐一对齐 ✓

**未发现行号错误**。

### 3.2 调用链路图核对

- 链路1-4 的分层结构（业务层 → AbstractIotDataCacheServer → BizDtpRwdService → DtpRwdClientServiceImpl → AbstractDtpPersistServer → RWD）与实际代码一致 ✓
- 补充了 `substitutionParameter` 参数覆盖机制（原文档未体现）✓

### 3.3 影响范围复核

- 原文档影响级别表中：照明（中→高）、安防（中→高）、值班（中→高）、运行分析（补充细节）已修正
- 新增模块：通风、壳配置、仪表盘、定时任务 ✓
- 新增隐性影响点：`BaseObjectQueryParam` 通用过滤逻辑 ✓

---

## 四、新增风险点（核验后补入 impact-analysis.md）

| 风险编号 | 风险 | 要点 |
|---|---|---|
| 风险3（补强） | substitutionParameter 参数覆盖 | ipType/时间参数有"代码传入"与"上下文覆盖"两个来源，需定义壳配置门控与覆盖机制的关系 |
| 风险8（新增） | BaseObjectQueryParam 通用过滤 | 所有继承该参数类的对象列表查询在设置过滤条件时都会触发 IOT 查询，影响面远大于单一业务模块 |
| 风险9（新增） | 定时任务调用链路 | xxl-job 任务（空间分析/信息点分组）不受前端 API 约束，失败无前端报错，需日志监控 |
| 风险10（新增） | 运行分析报表精度敏感 | 报表模块实际有 19 处 `listInfoCodeValueByCloud` + 1 处直接调用，对 ipType/时间高度敏感 |

---

## 五、结论

1. 首轮分析的核心架构判断（4 条主链路、底层改造策略）**正确**，无需推翻。
2. 首轮分析存在**系统性调用点遗漏**，主要原因是首轮检索模式不完整（只检索了 `abstractIotDataCacheServer.` 和 `iotDataCacheService.` 两个接收者名，漏掉了 `this.`、`iotDataCacheServerEdge.`、直接底层调用等模式），导致：运行分析报表（漏 13 处）、照明（漏 8 处）、值班卡片（漏 15 处）、直接调用（漏 6 处）等大面积遗漏。
3. 全部 4 份文档已在本轮核验中修正完毕。
4. 建议后续设计方案评审时，以修正后的 `interface-inventory.md` 第二节统计表（总计约 128 处调用点）作为影响范围基线。

---

# 附：第三轮核验补充（2026-08-17）

> 第三轮改用**跨行正则（multiline）与裸方法名全量检索**，发现第二轮仍存在 4 处遗漏，根源是：①跨行调用（方法名在行尾、`(` 在下一行）无法被单行正则匹配；②`listIpDataInPeriod` 的 3 参重载（带 projectId/groupCode）与直接 `abstractDtpPersistServer.listIpDataInPeriod` 调用未被 `queryIpDataInPeriod` 检索覆盖。已全部补入 4 份文档。

## 第三轮新增遗漏（4 处）

| # | 类 | 行号 | 遗漏内容 | 类别 |
|---|---|---|---|---|
| 1 | `SystemStatisticsServiceImpl` | 205-206 | `listInfoCodeValue(projectId, null, objectSets, infoCodes)` **跨行调用** | listInfoCodeValue 第 22 处 |
| 2 | `SystemStatisticsServiceImpl` | 309-313 | `bizDtpRwdService.listIpDataInPeriod(bodyDto, projectId, groupCode)`（3参重载，ipType="1d"，累计客流量 accdInPeopleFlow） | 直接调用第 12 处 |
| 3 | `PassengerFlowHomePageCardDataHandler` | 70 | `queryObjectIdInfoCodeHistory(FIFTEEN_MINUTES, objectIds, infoCodeList)` | queryObjectIdInfoCodeHistory 第 3 个调用者 |
| 4 | `DashBoardServiceImpl` | 553 | `abstractDtpPersistServer.listIpDataInPeriod(...)`（batchQueryPeriodData，ipType=enumTime 显式传入） | 直接调用第 13 处 |

## 第三轮统计数字更新

| 项目 | 第二轮值 | 第三轮值 |
|---|---|---|
| `listInfoCodeValue` | ~21 处 | ~22 处（+1 跨行调用） |
| `queryObjectIdInfoCodeHistory` | 2 个调用者 | 3 个调用者 |
| 绕过抽象类的直接调用 | 11 处 | **13 处** |
| **总计** | ~128 处 | **~132 处** |

## 第三轮确认无误项

- 全部关键行号（AbstractIotDataCacheServer / IotDataCacheServerCloud / DtpRwdClientServiceImpl / BizDtpRwdServiceImpl 及全部业务调用点）经 Read+Grep 逐一复核，无行号错误 ✓
- 跨行调用仅发现 `SystemStatisticsServiceImpl` L205 一处，其余方法无跨行调用 ✓
- `mapObjectIdAndExistsRuleInfoCodes*`、`queryInfoPointPeroid`、`queryIotRealDataRedisVO`（云端返回 null）、`queryObjectIdAndManualAutoSetRecoverTime` 等不涉及 ipType/时间参数的 IOT 周边查询已确认不在本次改造范围内 ✓
- `HomePageCardIotDataCloud`/`DashboardLightingStatisticsDataCloud`/`DtpPersistServerClientServiceImpl` 走的是 `homepage*Statistics` 等统计接口，非 ipType/时间参数类持久化查询，已确认排除 ✓
- 边缘端（local 模块）调用点不受云端改造影响，已确认排除 ✓

## 检索方法论修正（对后续类似任务的教训）

1. 方法调用检索必须同时覆盖：`接收者.方法(` 同行为、跨行（multiline）、直接调用底层服务、重载方法（如 `listIpDataInPeriod(param, projectId, groupCode)` 3 参版）。
2. 对同一语义的底层方法（queryIpDataOnMoment / queryIPDataOnMoment / queryIpDataInPeriod / queryIPDataInPeriod / listIpDataInPeriod）需要全部枚举，不可只搜一个。

---

# 附：第四轮核验补充（2026-08-17）

> 第四轮改用**兜底检索**：全库搜索 `setIpType(`（ipType 写入点全量清单）与 `IPDATA_START_TIME`/`isNeedReplacedSystem`（覆盖机制溯源）。调用点清单未再新增遗漏（`setIpType` 的 14 处命中点均已覆盖，`DtpPersistServerEdge` L72 为边缘端响应字段非查询参数，已排除），但发现 **ipType/时间参数覆盖机制存在第三个来源：前端 HTTP 请求头**，以及后端白名单机制的完整入口清单，风险3 已重写补强。

## 第四轮重大发现：覆盖机制的完整链路（此前文档不完整）

**两个生效点**：
- `DtpRwdClientServiceImpl.substitutionParameter()` L1242-1250（作用于 `queryIpDataOnMoment` 4参版与 `queryIpDataOnMomentInOneHour`）
- `AbstractIotDataCacheServer.queryObjectIdPersistentDataList` L479-486（isReplace=true）
- 不生效：`queryIpDataOnMoment` 5参版（L1211）与 `listInfoCodeValueByCloud`

**三个覆盖值来源**：

| 来源 | 位置 | 机制 |
|---|---|---|
| 前端 HTTP 请求头 | `CustomHeaderThreadLocalInterceptor` L36-41 | 请求头 `IPDataStartTime`/`IPDataEndTime`/`IPType`/`runningMonitorType` 写入 context.extra，前端不改 body 即可影响 ipType/时间 |
| 后端白名单 | `CommonConst.checkSystemReplacementTime` L169-180 | 6 类运行监测类型（COLD_HEAT_SOURCE/COLD_SOURCE/AIR_CONDITIONING_COLD_SOURCE/AIR_CONDITIONING_HEAT_SOURCE/AIR_CONDITIONING_C_H_SOURCE/PHOTOVOLTAIC，白名单 `SYSTEMS_THAT_NEED_TO_BE_REPLACED` L111-119）统一设置 5min 精度时间窗口；调用点 4 处：`CloudIotDataEvent` L223、`AirConditioningCHPageCardDataHandler` L90、`AirConditioningColdSourcePageCardDataHandler` L92、`AirConditioningHeatSourcePageCardDataHandler` L97 |
| 值班卡片自行设置 | `ColdSourceHomePageCardDataHandler` L128-133 | 直接构建 extra（COLD_SOURCE + 5min 精度 + 10分钟窗口） |

**判定条件**：`CommonConst.isNeedReplacedSystem(extra)` L159-162。

**对改造的影响**：
1. “前端 API 入参保持不变”需要扩展到“请求头也不变”：若移除 ipType/时间参数，必须确认前端不再依赖这 4 个请求头，或保留兼容。
2. 6 类运行监测系统的“统一数据时间”逻辑（固定最近 5min 精度窗口）依赖覆盖机制，改造后需同步重设计或迁移。
3. `queryIpDataOnMoment` 5参版与 `listInfoCodeValueByCloud` 不走覆盖逻辑，若新机制（壳配置）需统一行为，要考虑这些路径的差异。

## 第四轮确认无误项

- `setIpType` 14 处命中点逐一核对：`DtpRwdClientServiceImpl` 5 处（L1158/1174/1193/1215/1249）✓、`AbstractIotDataCacheServer` 2 处（L309/491）✓、`AbstractCommonServer` 1 处（L588）✓、`IotDataCacheServerCloud` 1 处（L460）✓、`RunAnalysisServiceImpl` 1 处（L331）✓、`SystemStatisticsServiceImpl` 1 处（L312）✓、`DashBoardServiceImpl` 1 处（L548）✓、`WeatherServiceImpl` 1 处（L87，响应 VO 字段非查询参数）✓、`DtpPersistServerEdge` 1 处（L72，边缘端响应字段）✓ —— 全部已收录或已排除
- 无新增调用点遗漏 ✓
- 覆盖机制溯源完整 ✓

---

# 附：第五轮自查收敛（2026-08-17）

> 第五轮为**交付前最终自查**：以裸方法名检索（`\blistInfoCodeValue\b` 等，不限接收者）交叉比对全部调用点，并用文档数字一致性 grep 扫描陈旧数字。调用点清单确认收敛（无新增），发现并修复以下问题：

## 第五轮发现并修复的问题

| # | 问题 | 位置 | 修复 |
|---|---|---|---|
| 1 | 行号错误：ElevatorHomePageCardDataHandler 第二处调用实际在 L165（processStatusValues 方法体），文档误写 L164 | iot-persistence-query-analysis.md 3.11 节 | 164行→165行 ✓ |
| 2 | 陈旧数字：改造优先级建议中“约35处/约14处/约8处/约6处”未随二三轮更新 | interface-inventory.md 第三节 L146-164 | 更新为 57/28/11/19+1 ✓ |
| 3 | 陈旧数字：影响分析场景1“约35处” | impact-analysis.md 场景1 | 更新为 57 处（附分解）✓ |
| 4 | 内容重复：第四轮补充时 ColdSourceHomePageCardDataHandler 条目重复 | iot-persistence-query-analysis.md 3.11 节 | 删除旧块，保留含覆盖机制标注的新块 ✓ |
| 5 | 缺失标注：三个 AirConditioning*PageCardDataHandler 的 checkSystemReplacementTime 调用（L90/92/97）未在 3.11 节标注 | iot-persistence-query-analysis.md 3.11 节 | 已补 ✓ |

## 第五轮新增排除确认（误报筛查）

- `RunAnalysisServiceImpl` L692-693（ElectricityPriceQueryParam/电价服务）— 非 IOT 持久化，排除 ✓
- `InfoCodeRwdObjectDetailByTemplateContext` L596-597（InfoCodeConfigByObjectQueryParam/配置查询）— 非 IOT 持久化，排除 ✓
- `WeatherClientServiceImpl` L82/107（天气服务）— 非 IOT 持久化，排除 ✓
- `FeignParamPageExecutor` L45/77（分页执行器内部复制参数）— 非调用点，排除 ✓
- `AbstractCommonServer` L567（getRwdInfo 辅助方法内部）— 已收录 ✓

## 最终一致性状态

- 调用点总计：**~132 处**（listInfoCodeValue 22 + queryIotRealData* 25 + queryIotRealDataListByList 10 + listInfoCodeValueInOneHour 28 + queryObjectIdPersistentDataList/Map 11 + queryObjectIdInfoCodeHistory 3 + listInfoCodeValueByCloud 19 + listInfoCodeValue1dByCloud 1 + 直接调用底层 13）
- 四份文档数字全部一致，无残留陈旧数字
- 全部行号经 Read/Grep 双向核验
- 覆盖机制（三来源两生效点）完整记录

**结论：文档已收敛，可以交付。**

---

# 附：第六轮修复（2026-08-17）

> 第六轮以逐行深度阅读核验为主，发现接口清单表格遗漏 `PassengerFlowServiceImpl` L70（`queryObjectIdPersistentDataMap` HOUR），导致持久化计数少 1。

## 第六轮修复

| # | 问题 | 位置 | 修复 |
|---|---|---|---|
| 1 | 接口清单遗漏：`PassengerFlowServiceImpl` L70（`queryObjectIdPersistentDataMap` HOUR）未在表格中记录 | interface-inventory.md 1.6 表 | 补入 L70，更新行号列 68,109→68,70,109 ✓ |
| 2 | 数字不一致：持久化计数 ~11→~12，总计 ~132→~133 | interface-inventory.md 统计表 | ✓ |
| 3 | 数字不一致：call-chain-map 链路3 "12 处"已含 3 处 history 但总数应为 15 | call-chain-map.md 链路3 | 修正为 15 处（12 persistent + 3 history） ✓ |
| 4 | 数字不一致：主文档分类 C 计数 34→35 | iot-persistence-query-analysis.md 分类 C | ✓ |

## 最终计数

**~136 处**（22+23+8+36+12+3+18+1+13 = 136）

---

# 附：第七轮修复（2026-08-18）

> 第七轮以"全 Controller 逐目录完整检索"代替全局 Grep（规避 25 条结果截断漏检），发现分析文档遗漏 3 处调用点 + 设计评审文档遗漏 1 个接口。

## 第七轮发现并修复的问题

| # | 问题 | 位置 | 修复 |
|---|---|---|---|
| 1 | 调用点遗漏：`ParkingServiceImpl` L316/L676（`listInfoCodeValueInOneHour` ×2）未在任何分析文档记录 | iot-persistence-query-analysis.md 3.12、interface-inventory.md 1.6 | 已补 ✓ |
| 2 | 调用点遗漏：`DashBoardServiceImpl` L827（`listInfoCodeValueInOneHour`，listAbnormalState 在离线/控返不一致查询）未记录 | iot-persistence-query-analysis.md 3.15、interface-inventory.md 1.6 | 已补 ✓ |
| 3 | 接口遗漏：`/parking`（ParkingController）未在设计评审文档接口清单 | design-review.md 三 | 已补 ✓ |
| 4 | 行号误差：ColdSourceHomePageCardDataHandler listInfoCodeValue 实际 L268（文档写 267） | iot-persistence-query-analysis.md 3.11 | 已修正 ✓ |
| 5 | 计数误差：queryIotRealData* 25→23、queryIotRealDataListByList 10→8、listInfoCodeValueByCloud 19→18（原值含内部委派） | 各文档统计表 | 已修正 ✓ |
| 6 | 计数误差：listInfoCodeValueInOneHour 28→36（原计数与文档自身清单不符） | 各文档统计表 | 已修正 ✓ |
| 7 | 设计评审文档 2.2 涉及规模数字未同步 | design-review.md | 已更新为 136 ✓ |
| 8 | impact-analysis.md 场景1 配置名未随最终方案更新 | impact-analysis.md | 已更新 ✓ |

## 逐 Controller 排查确认不受影响

- `/waterSupply`、`/waterSupplyDrainage`、`/weakPower`、`/bizCheck`、`/system`（SystemListData）、`/dangerEvent`、`/abnormalEvent`、`/air/statistics`、`/dashboardGridChart`、`/aiStrategic` — 服务层无受影响方法调用
- `/kitchenRangeHood`（queryKitchenExhaust 专用）、`/cold-heat`（queryObjectList 对象关系）、`/rwd`（孪生规则）— 不在受影响方法集内
- `/meoi-aim-server` — queryRiskEventList 由 CommonServerCloud 内部调用（归属 /airTerminal 链路）
- 定时任务（SpaceAnalyseTaskHandler/V2、ObjectInfoCodeGroupTask）— 非 API，已在分类 D 记录

## 第七轮后的最终计数

| 家族 | 调用点数 | 验证方式 |
|---|---|---|
| listInfoCodeValue | 22 | 逐目录裸方法名 grep |
| queryIotRealData* | 23 | 逐目录 grep（排除内部委派） |
| queryIotRealDataListByList | 8 | 全项目 grep（排除内部委派） |
| listInfoCodeValueInOneHour | 36 | 逐目录裸方法名 grep |
| queryObjectIdPersistentDataList/Map | 12 | common 6 + saas 6 |
| queryObjectIdInfoCodeHistory | 3 | 全项目 grep |
| listInfoCodeValueByCloud | 18 | 逐目录 grep |
| listInfoCodeValue1dByCloud | 1 | 全项目 grep |
| 直接调用底层 | 13 | 分类 D 表格 13 行 |
| **合计** | **~136** | |

---

# 附：第八轮基线性验证（2026-08-18，最终）

> 第八轮不再依赖全局 Grep（会静默截断结果，是前几轮反复漏检的根源），改用 PowerShell `Select-String` 全量扫描 common/ + saas/ 全部 Java 文件生成 237 行调用基线，再用 Python 脚本逐条分类并与 7 份文档做自动比对。

## 第八轮验证方法与结果

- 基线来源：16 个方法族 × 全部源码文件（common/src/main/java + saas），无截断
- 分类规则：排除抽象定义/接口声明/实现声明/内部委派/注释行/变量名误报后，业务调用点 = 137 处
- 文档覆盖比对：类名 + 行号共现检查 → **misses: 0**（137 处业务调用点全部在文档中有对应记录）
- 计数核对：22 + 23 + 8 + 36 + 12 + 3 + 18 + 1 + 13 = **136**（分类 D 表格 13 行含 14 个调用点，InfoCodeSetExtendConfigAnalyzeStrategy L382+L433 合并为一行）

## 第八轮发现并修复的问题

| # | 问题 | 修复 |
|---|---|---|
| 1 | `DataBoardServiceImpl` L209/L310 两处调用点在文档中仅以 L361 含糊提及 | 主文档 3.2 拆出独立块（209/310/361），call-chain-map 补充行号 ✓ |
| 2 | impact-analysis.md 风险10 "19 处" 与最终计数 18 不符 | 修正为 18 ✓ |
| 3 | 主文档 七、模块清单缺"空间/设备通用查询"和"空调模块视图"两行（与设计评审文档不一致） | 已补，prd 模块数 20→22 ✓ |
| 4 | 残留"灰度"表述 3 处（prd.md、impact-analysis.md 风险6/对接要点） | 全部改为"按项目启用" ✓ |
| 5 | 设计评审文档"受影响文件约 50+ 个"与实际 68 个不符 | 更新为约 68 个 ✓ |

## 最终状态

- 调用点：**~136 处**（137 个业务调用点，分类 D 两处合并一行）
- 受影响文件：68 个 Java 类
- 受影响接口：27 个（设计评审文档第三节）
- 业务模块：22 个
- 覆盖检查：misses = 0
- 文档间数字：全部一致，无残留旧配置名/灰度表述

---

# 附：最终方案（2026-08-25，PRD-260901）

> 2026-08-25 收到产品文档【PRD-260901】新增云端持久化数据查询规则，方案定型为**壳配置 + 请求头三条件门控**，并完成非接口链路梳理与评审。

## 最终方案要点

| # | 要点 | 内容 |
|---|---|---|
| 1 | 配置载体 | **壳配置新增分组"云端持久化数据显示规则"**（ShellConfigItemEnum 新增 2 配置项 + ShellConfigGroupEnum 新增分组；RcBasicsConfig 天然按项目隔离） |
| 2 | 配置项语义 | ①持久化数据查询逻辑（默认"查询同一个时刻点的数据"/可选"查询距离当前最新的数据"）②距离当前的时间间隔（默认 1h，1-168，仅最新数据模式出现） |
| 3 | 生效行为 | "最新数据"模式：时间范围=（当前-间隔 ~ 当前）+ **不传 ipType** |
| 4 | 生效条件 | 三条件：前端请求头 `removeDefaultIpType=true` + 壳配置"最新数据" + 项目级壳配置 |
| 5 | 影响范围 | 产品限定冷源/冷热源/热源/空调冷源/空调热源/空调冷热源；空调系列暂不改动 |
| 6 | 非接口链路梳理 | SSE 推送（CloudIotDataEvent L258，定时线程无请求头）、空间分析任务×2（xxl-job，不在影响范围）、运行分析报表（不直接调 IOT 查询）、ObjectInfoCodeGroupTask（HTTP 触发 Callable，可覆盖）、值班冷源卡片（显式 DAY）、控制台（前端提供清单） |

## 非接口链路评审结论（2026-08-25 定案）

- **SSE 推送**：**已定方案**——前端行为不变，仍在订阅请求头加标识；后端在订阅建立时（HTTP 线程内）检测请求头，有标识则写入订阅参数（SseSubscribeParam 新增字段）保存，推送查询处按"订阅参数标识 + 壳配置"判断精度；改动集中在订阅建立处（SseEmitterController/SseEmitterServer）与 CloudIotDataEvent 两处
- **空间分析任务 ×2**：保持现状（查 SPACE_CO2/SPACE_TDB，不在冷热源范围）
- **运行分析报表**：不受影响（不直接调用 IOT 查询方法族）
- **值班冷源卡片** `ColdSourceHomePageCardDataHandler` L274：显式传 DAY，属产品功能需求，保持现状（用户 2026-08-25 明确：显式指定分精度的查询保持指定的不变，本次改造只解决默认值问题）
- **控制台**：前端直接调用控制服务接口，不在本次后端改造范围，由控制服务自行改造

## 关键设计原则（2026-08-25 用户确认）

**本次改造只解决"默认值"的问题**：显式指定了分精度类型的查询（如 DAY、1d、5min 等）是产品功能需求，保持指定的分精度不变；只有使用默认分精度（硬编码 15min）的查询才在"最新数据"模式下不传分精度。

## 前端接口清单（2026-08-26 前端提供）

- 前端已梳理影响接口清单（`信息点分精度影响范围梳理.md`），已补充至设计评审文档 3.3：
  - 前端现状：先查 `strategyIntervention/getInfoCodeDataTimeAndPrecisionType` 获取分精度，随后模块下所有请求带分精度请求头
  - 改造范围：冷源/冷热源/热源【关系图/模型图、设备详情、设备列表、控制台】；空调系列【关系图/模型图、设备详情、设备列表】
  - 控制台接口（dtp-meos-server 前缀）由控制服务自行改造
  - 生活给水、气源系统、光伏系统接口结构类似但不在本次范围

## 前端接口逐个核对（2026-08-26 后端代码级核验）

后端对前端清单去重后 44 个接口逐一追踪调用链，结论：

- **✓ 涉及（17 个，需加请求头标识）**：
  - `system/graph/instance/{id}`（querySystemGraphById → getInfoCodeValues L625 listInfoCodeValueInOneHour）
  - `systemGraphEditor/getSystemGraphDetailById`（listInfoCodeValueInOneHour L661/796）
  - `instance/listObjectsByTackValueLogic`（带信息点值过滤条件时，BaseObjectQueryParam L468 listInfoCodeValueInOneHour）
  - `airconditioning/listEquipmentsToModuleView/{type}`（L180 listInfoCodeValueInOneHour）
  - `infoCodeConfig/listInfoCodeConfigsByObjectId`（queryInfoCodeObjectGroupVo L564 listInfoCodeValue）
  - `infoCodeConfig/listInfoCodeConfigsByTemplateId`（InfoCodeConfigQueryContext.filterDynamicInfoCode 查 IOT 值）
  - `equipment/infoCodeConfig/listInfoCodeConfigsByObject`（同 listInfoCodeConfigsByObjectId）
  - `running-monitor/batchQueryInfoCode`（L168 listInfoCodeValue）
  - `running-monitor/querySystemRunningParam`（L345 queryIotRealData + L334 queryIpDataOnMoment）
  - `running-monitor/queryInfoCodeConfigByObjectIds`（groupInfoCodeConfigs 查持久化 IOT 点位数据）
  - `running-monitor/batchQueryInfoCodeConfig`（同 queryInfoCodeConfigByObjectIds）
  - `strategyIntervention/chillerStrategy/listRunningStrategy`（mapRecentRunningStatus → queryObjectIdPersistentDataList 显式 5min）
  - `strategyIntervention/lastFreezePumpGroup/listRunningStrategy`（同上）
  - `hotColdStationStrategyIntervention/listRunningStrategy`（同上）
  - `weather/queryWeather`（L173 queryObjectIdPersistentDataList 气象站数据）
  - `airTerminal/getObjectTag`（mapEquipmentIdAndTag → CommonServerCloud L213 queryIotRealDataListByList 查 IOT 实际状态）
  - `server-event/sse/subscribe`（订阅后推送信息点值）
- **✗ 不涉及（27 个）**：含 5 个控制服务接口（dtp-meos-server 前缀）+ 22 个对象/配置/事件/模版类查询接口（如 system/graph/menu/config、instance/select、4 个样式配置、knowledge/listInfoCodes、shellConfig/query、shellConfig/queryByInfoCode、abnormalEvent 系列、sse/subscribe?ABNORMAL_EVENT、listTemplateTypes、treeTemplateType、queryConfigEquipmentListHeader、getForceControlStatus、listAccRuntimeClass、listSpacesByRunningMonitor、getNumberOfChillers、queryPumpGroupObjectByPumpId、queryObjectGroupInfoBySystemId、getInfoCodeDataTimeAndPrecisionType 等）

### 二次复核修正记录（2026-08-26）

- `airTerminal/getObjectTag`：初判 ✗ 不涉及（误判为纯对象标签查询），复核发现 mapEquipmentIdAndTag → CommonServerCloud L213 queryIotRealDataListByList 查 IOT 实际状态，修正为 ✓ 涉及
- `server-event/sse/subscribe?ABNORMAL_EVENT`：前端清单有但初版遗漏，补充为 ✗ 不涉及（ABNORMAL_EVENT 事件类型推送异常事件数据，不查信息点值）
- `instance/listObjectsByTackValueLogic`：确认条件性涉及（BaseObjectQueryParam L468 仅在请求带 filterInfoCodeValueMap/excludeInfoCodeValues 时查 IOT）

## 待确认项

- 请求头名称：已确定 `removeDefaultIpType`（值必须为 true）
- 壳配置分组/配置项的 code 命名（需与前端配置页对齐）

## 最终状态

- 配置载体：壳配置（RcBasicsConfig，项目级隔离）
- 生效逻辑：三条件（前端标识 `=true` + 壳配置"最新数据"）满足 → 时间范围=（当前-间隔 ~ 当前）+ 不传 ipType；否则 → 现有逻辑原样执行
- 影响范围：仅前端加标识 + 壳配置最新数据的冷热源链路可能变化；未加标识接口、定时任务行为完全不变