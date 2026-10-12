# 运行策略接口查询IOT latest 改造 — 实施清单（v2：直接换查询方式）

## 前置说明
- common / local 均无代码改动；改动收敛在 saas/meos-control-common 的 2 个类。
- 替换目标：`dtpRwdClientService.queryIpDataOnMoment(groupCode, projectId, new ArrayList<>(objectIds), new ArrayList<>(infoCodes))`（4 参，接口 `DtpRwdClientService#queryIpDataOnMoment(String,String,List,List)`），结果经 `iotConverter.toInfoCodeValueVo(EnergyDataResponse)` 直接转换收集（moment 按 对象+信息点 返回最新一条，跟随全站消费范式，不做二次去重）。

## Step 1. StrategyServiceImpl（saas/meos-control-common/.../service/strategy/impl/）
- [x] 依赖：新增注入 `IotConverter iotConverter`（构造器 `@RequiredArgsConstructor` 加 final 字段）；替换完成后 `abstractIotDataCacheServer` 字段不再被使用则删除（同时清理 import）。
- [x] `queryColdStationRunningStatus(String acccSystemId)`：
  - 原：`abstractIotDataCacheServer.queryObjectIdPersistentDataList(FIFTEEN_MINUTES, singletonList(acccSystemId), singletonList(RUN_STATUS), null, null, true)` → `get(0)`。
  - 新：moment 查询（objectIds=[acccSystemId]，infoCodes=[RUN_STATUS practicalCode]）→ 空结果返回空列表；转换后取首条（不存在返回 null，保持原语义）。
- [x] `listLastFreezePumpGroupRunningStrategy` 泵组运行状态直查（约行 159-179）：
  - 原：period 查询 + `forEach(x -> objectIdAndRunStatus.put(x.getObjectId(), 由 infoCodeValues.get(0) 解析))`（多对象取值缺陷见 design 3.2.2）。
  - 新：moment 查询（objectIds=lastFreezePumpGroupIds，infoCodes=[RUN_STATUS]）→ 直接转换 → 逐条解析 `x` 自身值写入 `objectIdAndRunStatus`（每个泵组取自己的状态）；`resultItem.setUpdateTime` 改为取实际数据时间（回退窗口刻度）。
- [x] 复核类内无其他 `queryObjectIdPersistentDataList` / `abstractIotDataCacheServer` 引用。
  - 附：`queryManualLastpumbParameter` 调用同步为单参（窗口变量 theLast30String/lastEndTime 仅用于查询，随改造移除）。

## Step 2. DataBoardServiceImpl（saas/meos-control-common/.../service/strategy/impl/）
- [x] 依赖：新增注入 `IotConverter iotConverter`；`abstractIotDataCacheServer` 无引用后删除字段与 import。
- [x] `queryInfoCodeValueMap(Collection objects, Collection infoCodes)`：
  - 原：period(15min, null~null, true) → 过滤非空 → `toMap(infoCode, value)`（无 merge）。
  - 新：moment → 直接转换 → 原过滤 + toMap 不变（moment 每键单条）。
- [x] `queryRunOfflineObjectMap(Collection objects)`：
  - 原：period(15min, RUN_ONLINE_STATUS, null~null, true) → 分组 `toMap(infoCode, value)`。
  - 新：moment → 直接转换 → 原分组 toMap 不变。
- [x] `queryManualLastpumbParameter(...)`：
  - 原：period(15min, MANUAL_LASTPUMB_INFO_CODE_LIST, theLast30String~lastEndTime, true) → 按 objectId 分组 → per-object `toMap(infoCode, identity)`。
  - 新：moment（objectIds=objIds，infoCodes=MANUAL_LASTPUMB_INFO_CODE_LIST）→ 直接转换 → 原分组与 per-object toMap 不变；saas 接口与实现签名同步简化为单参 `queryManualLastpumbParameter(String systemId)`（窗口参数仅用于旧查询）。
- [x] 复核类内无其他 `queryObjectIdPersistentDataList` / `abstractIotDataCacheServer` 引用。

## Step 3. 编译验证
- [x] `mvn -pl saas/meos-control-common -am compile`（含 common 依赖，JDK 8，BUILD SUCCESS）。
- [x] `mvn -pl local/meos-control-common-edge -am compile`（common 未改动，确认无连锁，BUILD SUCCESS）。
- [ ] GetProblems 复查两个改动文件无编译/规范告警（语言服务器未就绪，以编译通过 + diff 人工复查代替）。

## Step 4. 行为回归（运行/联调环境，对照 design §4 矩阵与 §3.2 差异说明）
- [ ] 场景 1 无请求头：三个接口输出与改造前一致（对比基线）：
  - 冷机/冷热站：运行状态、实际值/设定值、台数一致；
  - 末级泵组：对比值与运行状态一致（数据未刷新时与基线相同；若近期有刷新则展示更新的刻度，属 design 3.2.1 预期）。
- [ ] 场景 2 请求头 removeDefaultIpType=true + 壳配置 same_time（默认）：输出同场景 1；带 `IPDataStartTime/IPDataEndTime/IPType` 替换头 → 返回前端指定时刻的 15min 精度数据（UYAI-1348 回归点）。
- [ ] 场景 3 请求头 + 壳配置 latest：三接口各取值时间戳为最新一条（不限于 15min 刻度）；interval 从默认 1h 改为其它值后查询窗口随动。
- [ ] 场景 4 壳配置缺失/读取异常：接口正常返回（回退 15min），无 500。
- [ ] moment 既有消费方冒烟：冷源首页卡片 / 运行监控 moment 查询各一次（确认环境正常，非本任务改动引入）。

> 2026-09-04：代码改动与编译已完成（Step 1~3 通过，git diff 复核无意外改动），Step 4 行为回归待运行环境验证。

## 回滚点
- 任一调用点输出与基线不符且不属 design §3.2 差异说明：恢复该处原 `queryObjectIdPersistentDataList` 调用（含窗口参数与消费结构），仅保留其它无差异点位的替换。
- 若需整体回退：git checkout 两个改动文件即可（无 common/local 改动）。

## 交付物
- [ ] saas：`StrategyServiceImpl`、`DataBoardServiceImpl`（5 处替换 + 依赖调整）
- [ ] 任务 prd.md 验收核对；design §3.2 差异说明如需修正同步更新文档
- [ ] 如契约范围变化（如未来启用 5min mapRecentRunningStatus），同步评估 `.trellis/spec/backend/iot-persistence-query-rule.md`
