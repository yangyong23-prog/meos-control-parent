# 运行策略接口查询IOT latest 改造 — 技术设计（v2：直接换查询方式）

## 1. 背景与查询链路

PRD-260901 分精度改造（UYAI-1199/1348）落地时"控制台（控制服务）自行改造，不在当时范围"。本次三个运行策略接口（冷机 / 冷热站 / 末级冷冻泵组 listRunningStrategy）查询 IOT 实际值/运行状态走的是固定分精度链路：

```
saas StrategyService/DataBoardService(云端)
  └─ AbstractIotDataCacheServer.queryObjectIdPersistentDataList(ipType=FIFTEEN_MINUTES, ..., isReplace=true)   // 固定 15min，不受双条件门控影响
       └─ BizDtpRwdService.queryIpDataInPeriod → AbstractDtpPersistServer.listIpDataInPeriod   // period 区间接口
```

改造目标链路（已有、已验证的双条件门控入口）：

```
DtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)   // 4 参 moment（UYAI-1199/1348 已联调，spec 固化）
  ├─ 请求头 removeDefaultIpType=true && 壳配置 persistent_data_query_logic=latest
  │    → 不传 ipType、窗口 = persistent_data_time_interval ~ now   （latest：最新数据）
  └─ 其余（无头 / same_time / 缺失/异常）
       → ipType=15min + substitutionParameter（前端 IPDataStartTime/IPDataEndTime/IPType 替换头生效）
```

## 2. 方案（用户确认：直接换查询方式）

**不改 common 公共代码**（不新增共享服务、不动 `DtpRwdClientServiceImpl`、不动 `AbstractIotDataCacheServer`）。仅将 saas 三接口的 5 处 `queryObjectIdPersistentDataList` 调用**直接替换为 4 参 moment 门控查询** `dtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, new ArrayList<>(objectIds), new ArrayList<>(infoCodes))`，结果 `List<EnergyDataResponse>` 经 `iotConverter.toInfoCodeValueVo(EnergyDataResponse)` 转为 `List<InfoCodeValueVO>`（与现链路同一转换器，字段形态一致），再按 objectId+infoCode 归一（每键取数据时间最新一条，防御性）。local 边缘零改动（数据源为 Twin moment 实时值/EquipRecord，天然最新）。

5 处调用点与替换细则：

| # | 位置（saas/meos-control-common） | 用途 | 替换说明 |
|---|---|---|---|
| 1 | `strategy/impl/StrategyServiceImpl.queryColdStationRunningStatus` | 冷机/冷热站系统运行状态（单对象单点） | moment 后取列表首条，原 get(0)/判空结构不变 |
| 2 | `strategy/impl/DataBoardServiceImpl.queryInfoCodeValueMap` | 系统实际值/设定值 toMap(infoCode→value) | moment 后归一，原过滤非空 + toMap 不变（每键单条，无冲突） |
| 3 | `strategy/impl/DataBoardServiceImpl.queryRunOfflineObjectMap` | 大小冷机/设备运行在线统计 | moment 后归一，分组 toMap 不变 |
| 4 | `strategy/impl/DataBoardServiceImpl.queryManualLastpumbParameter` | 末级泵组温差/压差对比值 | moment 后按对象分组，原 per-object toMap(identity) 不变 |
| 5 | `strategy/impl/StrategyServiceImpl.listLastFreezePumpGroupRunningStrategy` 内直查 | 末级泵组运行状态 | moment 后按对象分组取各自首条（修正原"全列表共取 get(0)"缺陷，见 3.2） |

依赖调整：
- `StrategyServiceImpl`、`DataBoardServiceImpl` 注入 `IotConverter`（MapStruct bean，common 已有）。
- 两类的 `AbstractIotDataCacheServer` final 字段在上述 5 处替换后不再被本类任何方法使用（全文已核），移除该注入；`ControlConsoleStrategyServerCloud`（mapRecentRunningStatus 5min，无调用方）不动，其注入保留。

## 3. 语义论证与行为差异说明

### 3.1 回退场景（无头 / same_time / 配置缺失）等价性
- 原 period(15min, null~null) 与 moment legacy(15min 无窗口) 均为"该点 15min 精度最近一条"语义：现状 period(null~null) 若按 obj+infoCode 返回多条，`toMap` 无 merge 必抛异常而系统运行正常，反证 DTP period 端对每 obj+infoCode 至多一条 → 与 moment 语义一致。
- 替换头（IPDataStartTime/IPDataEndTime/IPType）：moment legacy 的 `substitutionParameter` 与原 period 的 isReplace 内联替换读同一 extra 键、同置 dataStatus=1 → 行为一致（UYAI-1348 same_time 前端时间参数生效场景不受影响）。
- 双条件满足时 moment 走 latest 分支（不传 ipType + interval 窗口），替换头被契约禁止 → 与 spec 互斥规则一致。
- 壳配置缺失/读取异常：moment 4 参内部回退 legacy（spec 已固化，不 throw）→ 行为等于回退场景。

### 3.2 有意的行为差异（需回归时按此核对）
1. 末级泵组两处原窗口为"近 15~30min"（排除最新未完成刻度的兜底写法）：换 moment 后取"最新已落库刻度"——数据刷新时展示更及时，长期无新数据（超 30min）时不再返回空值（moment 返回最近一条）；替换头存在时两路径均被前端参数覆盖，无差异。
2. 末级泵组运行状态直查原实现 `infoCodeValues.get(0)` 取列表首条值赋给每个对象（多对象时取值错位的显式缺陷）：换 moment 后逐条消费，每个泵组取自己的最新状态（行为修正为正确值；各对象值相同时输出与原来一致）。

### 3.3 结果转换
`EnergyDataResponse.ipTime`（yyyyMMddHHmmss）→ `InfoCodeValueVO.dataTime/updateTime`（`toDataTime()`，MapStruct 已映射）。moment 4 参（`queryIPDataOnMoment`）返回语义为每 objectId+infoCode 一条最新数据（与全站 36 处 InOneHour/moment 既有消费范式一致，均直接消费不归一）；本任务跟随该范式直接转换收集。moment 返回 null/空时统一返回空集合（保持原方法空结果语义：原链路对 DTP null 亦返回空列表，下游判空逻辑不变）。

## 4. 行为矩阵（验收对照）

| 场景 | 改造前 | 改造后 |
|---|---|---|
| 无请求头 removeDefaultIpType | period 15min（null / 15~30min 窗口） | moment legacy：15min + 替换头（若有）→ 输出一致；泵组窗口差异见 3.2.1 |
| 有请求头 + same_time / 缺失 / 异常 | 同上 | 同上（前端替换参数生效，UYAI-1348 回归点） |
| 有请求头 + latest | 固定 15min（滞后） | moment latest：不传 ipType、窗口=interval~now、每 obj+infoCode 取时间最新一条 |
| 双条件满足 + 替换头 | （新组合，改造前无） | latest 优先，替换头不生效（契约互斥） |

## 5. 改动清单与影响面

| 文件（saas/meos-control-common） | 改动 |
|---|---|
| `service/strategy/impl/StrategyServiceImpl.java` | 2 处换 moment 4 参 + IotConverter 注入 + 移除 abstractIotDataCacheServer 注入 |
| `service/strategy/impl/DataBoardServiceImpl.java` | 3 处换 moment 4 参 + IotConverter 注入 + 移除 abstractIotDataCacheServer 注入 |

common / local：无改动。影响面收敛为上述 2 个文件（HotColdStationStrategyServiceImpl 云端经 strategyService/dataBoardService 复用同一链路，无需直接改动）。

## 6. 风险与对策

- moment 与 period 为 DTP 不同接口，回退输出等价性依据 3.1 论证（不依赖 DTP 服务端实现细节）；若回归发现差异，以 3.2 差异说明核对，超出说明的差异按 implement 回滚点恢复该处原调用。
- moment latest 若返回多条：moment 契约按 对象+信息点 单条返回（全站消费范式佐证）；如后续契约变化导致同键多条，与全站 moment 消费方同受影响，届时统一处理。
- `mapRecentRunningStatus`（5min、无调用方）不纳入：5min 精度 moment 无法表达，若未来启用需另行评估（任务 Notes 记录）。

## 7. 验证计划

- 编译：`mvn -pl saas/meos-control-common -am compile`（含 common 依赖）；`mvn -pl local/meos-control-common-edge -am compile` 确认无连锁影响。
- 行为矩阵回归（运行/联调环境）：
  1. 无头：三接口输出与改造前对比一致；
  2. 请求头 + same_time：一致；带替换头 → 前端指定时刻 15min 数据生效；
  3. 请求头 + latest：运行状态/实际值/设定值/台数/泵组对比值时间戳为最新一条，interval 配置调整后窗口随动；
  4. 壳配置缺失/异常：无 500，回退行为同场景 1。
- moment 既有消费方（冷源首页卡片、运行监控 moment 查询）冒烟一次（本任务未触碰 common，仅为环境确认）。
