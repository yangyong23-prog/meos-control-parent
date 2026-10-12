# 运行策略接口查询IOT改造为查询最新一条（分精度）

## Goal

将"运行策略控制台"三个 listRunningStrategy 接口中查询 IOT 持久化数据的行为接入 PRD-260901 分精度改造双条件门控：当前端请求头 `removeDefaultIpType=true` 且壳配置 `persistent_data_query_logic=latest` 时，改为"查询距离当前最新的数据（不传分精度，取最新一条）"；其余场景保持改造前行为（固定 5min/15min 分精度 + 前端替换参数）。PRD-260901 落地时"控制台（控制服务）自行改造，不在当时范围"，本次补齐该缺口。

## Requirements

- 接口范围（均已与用户确认）：
  1. `/strategyIntervention/chillerStrategy/listRunningStrategy`（冷机运行策略）
  2. `/hotColdStationStrategyIntervention/listRunningStrategy`（冷热站运行策略）
  3. `/strategyIntervention/lastFreezePumpGroup/listRunningStrategy`（末级冷冻泵组运行策略）
- 端侧范围（已确认）：saas 云端 + local 边缘两侧实现均需覆盖（common 控制器云边共用）。
- 改造语义（已确认）：沿用 PRD-260901 双条件门控
  - `LocalContextStorage.isRemoveDefaultIpType()==true &&` 壳配置 `persistent_data_query_logic==latest` → 查询最新一条（不传分精度 ipType；时间窗口取壳配置 `persistent_data_time_interval` 小时 ~ now，与 `DtpRwdClientServiceImpl.queryIpDataOnMoment` 4 参改造保持一致）
  - 其余一切组合（无请求头 / same_time / 配置缺失或异常）→ 保持现状：固定分精度 + 前端 `IPDataStartTime/IPDataEndTime/IPType` 替换参数生效
- 结果侧语义：接口内每个 objectId+infoCode 只消费"时间最新"的一条数据（面板"当前实际值/运行状态"语义），不允许因不传分精度后区间内出现多精度多时刻数据而重复取值或 toMap 冲突。
- 兼容性约束：
  - 公共方法 `AbstractIotDataCacheServer.queryObjectIdPersistentDataList` 被多个业务模块（运行监控/客流/infoCodeConfig 等 ~12 处）调用，其既有语义（固定 ipType + isReplace 替换）不得被本任务改变；latest 能力必须通过新增方法/重载注入，或仅改造策略链路自己的调用。
  - 显式指定分精度（DAY 等）的查询保持指定不变。
  - 非接口链路（SSE 推送线程等）不在本次范围内，除非其复用本接口链路。

## Acceptance Criteria

- [ ] 冷机接口：请求头 `removeDefaultIpType=true` + 壳配置 latest 时，冷站运行状态、系统实际值/设定值、大小冷机运行台数均取自"最新一条"数据（查询不传分精度，时间窗口取壳配置间隔）；same_time / 无请求头时行为与改造前一致（15min + 前端替换参数生效）。
- [ ] 冷热站接口：运行状态与系统参数同样满足上述 latest/回退 行为。
- [ ] 末级冷冻泵组接口：泵组运行状态与供回水温差/压差对比值同样满足上述 latest/回退 行为。
- [ ] 双条件不满足时（含壳配置缺失/异常回退），输出与改造前完全一致，无回归。
- [ ] 除上述三个接口链路外，`queryObjectIdPersistentDataList` 及其余调用方行为零变化。
- [ ] saas 与 local 两个工程 `mvn compile` 通过；运行期手工验证 latest/same_time/无头 三场景。

## Notes

- 云端现状调查结果（研究闭环）：三个接口在 saas 侧的 IOT 持久化查询全部经 `AbstractIotDataCacheServer.queryObjectIdPersistentDataList`，固定 `FIFTEEN_MINUTES` 精度、isReplace=true，共 5 处调用点：
  1. `StrategyServiceImpl.queryColdStationRunningStatus`（冷机/冷热站共用，15min、null~null、取 get(0)）
  2. `DataBoardServiceImpl.queryInfoCodeValueMap`（querySystemParameter/queryHotColdStationSystemParameter 共用，15min、null~null、toMap）
  3. `DataBoardServiceImpl.queryRunOfflineObjectMap`（大小冷机/冷热站设备运行台数统计，15min、null~null）
  4. `DataBoardServiceImpl.queryManualLastpumbParameter`（末级泵组对比值，15min、近 15~30min 窗口）
  5. `StrategyServiceImpl.listLastFreezePumpGroupRunningStrategy` 内直查（泵组运行状态，15min、近 15~30min 窗口、取 get(0)）
  - `ControlConsoleStrategyServerCloud.mapRecentRunningStatus`（5min、近 5~10min 窗口）经全仓核查无任何调用方（双端均为无人调用的实现），**不纳入本次改造**。
- 边缘 local 侧结论：三个接口实现的数据源为 DTP Twin moment 实时查询（`IotDataCacheService.queryIotRealDataVO`，语义=最新一条有效数据）、Redis 实时缓存（runStatus 等）与本地执行记录 `EquipRecord` desc（设定值/运行状态），**无 15min/5min 分精度持久化查询点，天然满足"最新一条"语义，边缘无需代码改动**；验收以云端改造 + 边缘回归验证为准。
- saas 冷热站 `HotColdStationStrategyServiceImpl` 无 fillManual* 方法（该组方法仅存在于 local 版且基于 EquipRecord，不查分精度）。
