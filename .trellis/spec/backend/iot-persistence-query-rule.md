# IOT Persistence Query Rule

## Scenario: shell-config driven default ip-type removal

### 1. Scope / Trigger

- Trigger: PRD-260901 云端持久化数据查询规则改造 — 仅当 前端请求头 `removeDefaultIpType=true` **且** 壳配置"云端持久化数据显示规则"= `latest`（查询距离当前最新的数据）双条件满足时，固定传 5min 分精度，时间范围按壳配置间隔查询。
- Applies when changing `DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` / `queryIpDataOnMomentInOneHour`, the request-header detection in `LocalContextStorage`, the SSE subscription param pass-through, or the two shell config items `PERSISTENT_DATA_QUERY_LOGIC` / `PERSISTENT_DATA_TIME_INTERVAL`.
- Regression note (UYAI-1348): 曾将分支条件仅按请求头判断（commit 4c561d6189），导致 same_time + 标识 也走 interval 窗口且跳过 `substitutionParameter`、前端时间参数失效；修复后恢复"标识 && latest"双条件门控。
- Product amendment (2026-09-07): latest 分支由改造初版"不传 ipType"改为**固定传 `5min`**——DTP 持久化服务对缺省 ipType 默认按 15min 处理，latest 语义下取数结果不对；传 5min 时 DTP 仍可返回 15min 等更大分精度的数据，latest"不限精度取最新"语义不受破坏。

### 2. Signatures

- Request header: `removeDefaultIpType` (value must be `"true"`), read via `LocalContextStorage.isRemoveDefaultIpType()`.
- Shell config (enum `ShellConfigItemEnum`, scene `persistent_data_rule` / group `group_persistent_data_rule`):
  - `persistent_data_query_logic` — `same_time`(default, 查询同一时刻点固定分精度数据) | `latest`(查询距离当前最新的数据，固定传 5min 分精度)
  - `persistent_data_time_interval` — hours, default `"1"`, min 1 max 168 (validated by the shell-config save side, NOT by consumers); **仅在 latest 语义下消费**
- DTP request: `QueryIPDataInPeriodDTO` — `ipType`, `startTime`, `endTime`.
- Time format in the active branch: `yyyy-MM-dd HH:mm:ss` (`DateUtils.DTF_SDF_SECOND`), same as the legacy branch.

### 3. Contracts

- 双条件门控（helper `isLatestDataQueryLogic()`，缺失/未知值/读取异常回退 `false` = same_time 语义）：
  - 仅 `isRemoveDefaultIpType() == true && 壳配置 == latest` 时走 active 分支：固定设 `ipType="5min"`（DTP 对缺省 ipType 默认 15min，会导致 latest 取数不对；传 5min 仍可查到更大分精度的数据），`startTime = now - interval`、`endTime = now`（同一 `LocalDateTime now = DateUtils.now()` 派生，禁止混用 `DateUtils.getNowTimeStr()`）
  - 其余一切组合（same_time / 缺失 / 异常，或无请求头标识）走 legacy 分支：`ipType="15min"` + `substitutionParameter`（InOneHour 版保留 `currentTimeOneHourAgo()` ~ `getNowTimeStr()` 窗口）
- `&&` 短路：无请求头标识不读任何壳配置（零额外 DB 查询）；有标识但配置非 latest 只读一次 query_logic 配置即回退 legacy，不读 interval
- `substitutionParameter` 只在 legacy 分支执行：命中替换系统白名单（`runningMonitorType`）时用前端 `IPDataStartTime/IPDataEndTime/IPType` 覆盖，保证 same_time 场景前端时间参数生效
- Missing/blank config value falls back to `ShellConfigItemEnum.getDefaultValues()`; exceptions fall back to default (`false` for logic, parsed default for interval) with an error log.
- The explicit 5-arg `queryIpDataOnMoment(objectIds, infoCodes, ipType, startTime, endTime)` is untouched — explicit resolution is a product feature.
- SSE pass-through: `SseEmitterController` captures the header into `SseSubscribeParam.removeDefaultIpType` at subscription time; `CloudIotDataEvent.getSseEmitterObject` sets `LocalContextStorage.setRemoveDefaultIpType(true)` before pushing and clears it in `finally` to avoid thread-pool pollution. SSE 推送查询走底层 4 参方法，同一门控自动生效。

### 4. Validation Matrix

| Condition | Behavior |
| --- | --- |
| Header absent | Legacy branch: `ipType=15min` + `substitutionParameter`; InOneHour uses `currentTimeOneHourAgo()` ~ `getNowTimeStr()`; 不读壳配置 |
| Header present + logic=`same_time` / 缺失 / 异常 | Legacy branch: `ipType=15min` + `substitutionParameter`（命中白名单时用前端时间/精度替换）；不使用 interval 窗口 |
| Header present + logic=`latest` | Active branch: `ipType=5min`, time = now - interval ~ now |
| Config item missing/blank | `isLatestDataQueryLogic()` 回退 `false` → legacy; interval 回退 enum 默认值（仅 latest 分支消费） |
| Config read exception | Log error, fall back to default (legacy); never throw |
| `substitutionParameter` in active branch | Forbidden — active 分支必为 latest，前端替换参数与 latest 语义互斥 |

### 5. Good / Base / Bad Cases

- Good: header + logic=`latest` + interval=3h → query (now-3h ~ now) with ipType=5min.
- Good: header + logic=`same_time` + 前端传 `IPDataStartTime/IPDataEndTime`（替换系统白名单）→ substitutionParameter 生效，用前端时间点查固定分精度数据（UYAI-1348）。
- Base: no header → legacy 15min path unchanged, no shell-config DB query.
- Bad: 门控只按请求头判断（不判 latest）—— same_time 也消费 latest 专属的 interval 窗口、跳过 substitutionParameter，前端时间参数失效（UYAI-1348 根因）。
- Bad: reading both configs unconditionally — every one of the ~136 call sites pays two DB queries and the legacy branch's hardcoded window is silently replaced.

### 6. Tests Required

- Compile `common` (`mvn -pl common -am compile`, JDK 8)。
- Unit/self-test matrix per the Validation Matrix: header absent (legacy equivalence), header+`same_time` (legacy 15min + substitutionParameter, 无 interval 窗口), header+`latest` + interval (`ipType=5min` + configured window).
- Assert both times are formatted with `DTF_SDF_SECOND` and derived from one `DateUtils.now()` call in the active branch.
- Assert `getPersistentDataInterval()` fallback returns `Integer.parseInt(PERSISTENT_DATA_TIME_INTERVAL.getDefaultValues())` (no hardcoded `1`).

### 7. Wrong vs Correct

#### Wrong（UYAI-1348 缺陷形态：仅按请求头门控 + 分精度独立于 latest 判断）

```java
if (LocalContextStorage.isRemoveDefaultIpType()) {
    // 时间固定取壳配置 —— same_time 也错误消费 interval 窗口
    LocalDateTime now = DateUtils.now();
    periodDTO.setStartTime(now.minusHours(this.getPersistentDataInterval()).format(DateUtils.DTF_SDF_SECOND));
    periodDTO.setEndTime(now.format(DateUtils.DTF_SDF_SECOND));
    if (this.isKeepDefaultIpType()) {   // same_time → 15min
        periodDTO.setIpType("15min");
    }
    // 未执行 substitutionParameter → 前端时间参数失效
} else {
    periodDTO.setIpType("15min");
    this.substitutionParameter(periodDTO);
}
```

#### Correct

```java
if (LocalContextStorage.isRemoveDefaultIpType() && this.isLatestDataQueryLogic()) {
    // 壳配置为"查询距离当前最新的数据"：时间窗口取壳配置间隔，固定传 5min 分精度（不传会被持久化服务默认按 15min 处理导致结果不对）
    LocalDateTime now = DateUtils.now();
    periodDTO.setStartTime(now.minusHours(this.getPersistentDataInterval()).format(DateUtils.DTF_SDF_SECOND));
    periodDTO.setEndTime(now.format(DateUtils.DTF_SDF_SECOND));
    periodDTO.setIpType("5min");
} else {
    // 壳配置为查询同一时刻点数据或未带请求头标识：保持改造前逻辑（前端替换参数优先）
    periodDTO.setIpType("15min");
    this.substitutionParameter(periodDTO);
}
```

## Scenario: control-console strategy panel latest consumption (v7.260901.0)

### 1. Scope / Trigger

- Trigger: PRD-260901 遗留的"控制台（控制服务）自行改造"缺口补齐 — 三个运行策略面板接口（冷机 `/strategyIntervention/chillerStrategy/listRunningStrategy`、冷热站 `/hotColdStationStrategyIntervention/listRunningStrategy`、末级泵组 `/strategyIntervention/lastFreezePumpGroup/listRunningStrategy`）的 IOT 持久化查询从固定 15min period 消费切换为 4 参 moment 门控消费。
- Applies when changing the saas panel query chain: `StrategyServiceImpl.queryColdStationRunningStatus` / `listLastFreezePumpGroupRunningStrategy`、`DataBoardServiceImpl.queryInfoCodeValueMap` / `queryRunOfflineObjectMap` / `queryManualLastpumbParameter`，或为其它固定分精度 period 消费点做同类 latest 改造。
- 背景：改造前这些点走 `AbstractIotDataCacheServer.queryObjectIdPersistentDataList(FIFTEEN_MINUTES, ...)`（固定 15min + isReplace 替换），不经双条件门控；PRD-260901 落地时控制台被明确排除在外。

### 2. Signatures

- 改造后查询入口（保持不变，门控已在内部实现并验证）：`DtpRwdClientService.queryIpDataOnMoment(String groupCode, String projectId, List<String> objectIds, List<String> infoCodes)`（4 参，见 Scenario: shell-config driven default ip-type removal 契约）。
- 消费侧私有方法形态（saas `StrategyServiceImpl`/`DataBoardServiceImpl` 各一份）：
  - 入参：`Collection<String> objectIds, Collection<String> infoCodes`；空入参/空结果统一返回空集合。
  - 转换：`iotConverter.toInfoCodeValueVo(EnergyDataResponse)`（MapStruct，dataTime/updateTime 来自 `ipTime`），stream 直接收集，不做二次去重。
- 签名简化：saas `DataBoardService.queryManualLastpumbParameter(String systemId)` 降为单参（原 15~30min 窗口参数仅服务于旧 period 查询，随改造废弃；local 边缘版本就是单参）。
- 面板更新时间：末级泵组 `RunningStrategyVO.updateTime` 取本次实际数据时间（无数据时回退窗口刻度），不再固定显示 15~30min 前刻度。

### 3. Contracts

- 双条件命中（请求头 `removeDefaultIpType=true` + 壳配置 latest）→ moment latest：固定传 `ipType=5min`、窗口 interval~now，每 objectId+infoCode 返回最新一条（latest 分支禁止替换参数，与 moment 改造契约一致）。
- 双条件未命中（无头 / same_time / 缺失/异常）→ moment legacy：`ipType=15min` + `substitutionParameter`（前端替换头生效），与改造前 period(15min) 输出等价：moment 与 period 在 DTP 端均按 对象+信息点 返回单条（若 period 返回多条，消费方无 merge 的 toMap 早已报错，反证单条语义）。
- moment 返回按 对象+信息点 单条（全站 36 处 InOneHour/moment 消费范式佐证），消费方直接转换收集，禁止二次去重/归一。
- 空结果语义：moment 返回 null/空时消费侧统一转空集合，下游判空逻辑与改造前一致。

### 4. Validation Matrix

| 消费点精度 | 是否可 moment 化 | 说明 |
| --- | --- | --- |
| 15min 固定（面板 5 处） | ✓ | moment legacy 固定 15min，回退等价 |
| 15~30min 窗口（末级泵组 2 处） | ✓ | 窗口语义被 moment"最新已落库刻度"吸收：刷新后更及时、长期无新数据不再返空；替换头场景两路径均被前端参数覆盖 |
| 5min 固定（`ControlConsoleStrategyServerCloud.mapRecentRunningStatus`，无调用方） | ✗ | moment 无法表达 5min 精度；该实现无人调用，未纳入改造，若未来启用需另行设计 |

### 5. Good / Base / Bad Cases

- Good: 面板 period 消费点直接换 4 参 moment + IotConverter 转换，latest/same_time/替换头 行为全自动且与全站一致。
- Good: 末级泵组运行状态原 `infoCodeValues.get(0)` 多对象取值错位缺陷，随逐条消费修正为每个泵组取自身最新状态。
- Base: 无请求头 / same_time 场景输出与改造前一致（moment legacy = 15min + 替换）。
- Bad: 在消费侧自实现 latest 门控/壳配置读取（与 `DtpRwdClientServiceImpl` 契约分叉，UYAI-1348 同类风险）；或对 moment 结果做按时间去重归一（moment 已按 对象+信息点 单条返回，纯属多余代码）。

### 6. Tests Required

- 编译：`mvn -pl saas/meos-control-server -am compile`（JDK 8；改动仅 saas meos-control-common，common/local 零改动）。
- 行为回归矩阵（运行环境，任务 implement.md Step 4）：无头 / same_time+替换头（UYAI-1348 回归点）/ latest+interval 调整 / 壳配置缺失异常，三接口各取值核对。
- 断言：latest 场景每对象每信息点取到时间戳最新一条；回退场景与改造前输出一致；泵组 updateTime 跟随实际数据时间。

### 7. Wrong vs Correct

#### Wrong（自实现门控 + 归一，契约分叉）

```java
if (LocalContextStorage.isRemoveDefaultIpType() && 自读壳配置判 latest) {
    // 重复实现 DtpRwdClientServiceImpl 内已固化的判定与窗口，未来契约变更难同步
    ...
    // 再按 objId+infoCode 按时间去重归一
}
```

#### Correct（直接消费已验证的 4 参 moment 入口）

```java
List<EnergyDataResponse> responses = this.dtpRwdClientService.queryIpDataOnMoment(
        ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
        new ArrayList<>(objectIds), new ArrayList<>(infoCodes));
if (CollectionUtils.isEmpty(responses)) {
    return Lists.newArrayList();
}
return responses.stream()
        .map(this.iotConverter::toInfoCodeValueVo)
        .collect(Collectors.toList());
```
