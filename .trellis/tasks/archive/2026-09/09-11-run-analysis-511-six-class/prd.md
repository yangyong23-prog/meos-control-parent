# 运行分析室内温度与蓄冷蓄热时段六类适配

## Background

- 2026-09-11 用户提供新版产品需求（PRD 5.1.1，两段）：室内温度曲线与蓄冷/蓄热时间段的系统/空间范围适配至六类。
- 前情：旧版六类适配（`cdc80a9c9f`，含能效部分）已于 2026-09-11 因产品需求变更回滚（`19ff6d84a6`）；本任务为新版需求重新实施，范围更小（**不含能效指标部分**）。
- 用户明确要求：开发帮忙确认**原本的取值逻辑**（已调研，见下）。

## 需求原文（用户提供）

### 5.1.1 室内温度曲线

- **既有规则**：空间取值范围为中央供冷系统实例/中央供热系统实例/中央冷热源系统实例服务的空间，且制定了"运行标准-空调需求"的空间。
- **适配为**：空间取值范围扩展为项目交付的六类系统实例（ACCC、ACCH、ACCI、ACAC、ACAH、ACAI）通过"系统服务于空间（Sy2Sp）"关系服务的空间并集，且制定了"运行标准-空调需求"的空间；空间筛选仅选择空气（干球）温度（实际值）（tdb）有实例化规则的空间的规则不变。

### 蓄冷/蓄热时间段

- **既有规则**：项目下全部系统实例（中央供冷系统/中央供热系统/中央冷热源系统）的"蓄冷/蓄热时间段"分别取并集，系统实例枚举为旧版三类。
- **适配为**：取并集的系统实例范围扩展至项目交付的全部六类系统实例（ACCC、ACCH、ACCI、ACAC、ACAH、ACAI），不限监测监控状态；并集计算规则与 BDTP 上数时间点取值展示逻辑不变。新版系统实例无"蓄冷/蓄热时间段"信息点实例化规则时，自然不参与并集计算。

## 既有取值逻辑调研结论（2026-09-11，基于当前代码逐行核对）

### 室内温度曲线（`POST /runAnalysis/listEnergyCurveData` → `queryIndoorTemp` L587-641）

链路：系统实例（旧三类）→ Sy2Sp 关系查空间 → tdb 实例化规则过滤 → 空调需求空间（门槛判断）→ 5min 值查询 → 逐时间点均值。

1. 系统枚举：ACCC/ACCI/ACCH（L591），**不过滤监测监控状态**。
2. 系统→空间：`RunningMonitorRunAnalysisStrategy.listExistsRuleSpaceIdBySystemIds`（L130-155）——`DtpRwdObjectRelationQueryParam(SYSTEM_SERVICE_SPACE)`，objFrom=全部系统实例 → 空间**并集**（天然支持多系统入参）。
3. 空间筛选（需求要求"不变"项）：上步空间经 `mapObjectIdAndExistsRuleInfoCodes` 保留 **tdb 有实例化规则**的空间。
4. 空调需求：`DemandConfigGatewayImpl.listHasConfigSpaceIds(AIRCONDITIONING, spaceIds)` → demandSpaceIds（spaceIds 范围内有 finalConfig 的空间）。
   - ⚠️ **确认发现的偏差**：值查询（L614）实际使用 `spaceIds`（Sy2Sp∩tdb），`demandSpaceIds` 仅作"无需求空间则返回空"的门槛，**未用于过滤取值集合**——**已修正**：取值集合改为 `demandSpaceIds`（fix `934535b343`）。
5. 值计算：5min 粒度 tdb 实际值，按时间点对空间取算术平均（除以空间数）。

### 蓄冷/蓄热时间段（edge job `historyStorageHandler` → 算法 → `history_storage_result` → `queryCoolHeatTag`）

1. 上数 job（`HistoryStorageTaskHandler.execute` L53-125）：查系统实例（旧三类，**无监测状态过滤**，仅取 id）→ 算法 `getHistoryStorage`（15min）→ 每时段"全 0 不蓄 / 有冷蓄冷 / 否则蓄热"生成标签 → 存表（项目级/日，保留 180 天）+ 同步云端。
2. 展示（`queryCoolHeatTag` L665-679）：读 `history_storage_result`（**无系统枚举依赖**）→ 输出标签序列。
   - 与需求"并集计算规则与 BDTP 上数时间点取值展示逻辑不变"一致；"不限监测监控状态"与 job 现状一致（本就无状态过滤）。

## 改动点（初判）

- C1 `RunAnalysisServiceImpl.queryIndoorTemp`：classCode 旧三类 → 六类（其余链路不动）。
- C2 `HistoryStorageTaskHandler`（edge）：classCode 旧三类 → 六类（其余不动）。
- C3 `DtpRwdClassCodeEnum`：重建六类列表静态方法（C1/C2 复用；该静态方法在上次回滚中被删除，需按规范重建，含 @since/this/Javadoc）。
- C4 `DemandConfigGatewayImpl.listHasConfigSpaceIds`：新增 `queryParam.setFilterShieldedSpaces(true)`，过滤屏蔽空间（黑名单已由 demand 存储机制自动排除）。
- Q1 已解决（2026-09-11）：**最终取值集合修正为 demandSpaceIds（三条件交集，与产品文案对齐）**，见 fix 提交 `934535b343`；原"保持既有行为（spaceIds）"方案已废弃。
- Q2 已确认：计算结果已使用实际有 IOT 值的空间数（valueMap 自然过滤），无需额外改动。

不涉及：statistics/能效指标/i18n/DashBoardInfoCodeEnum/SQL。

## Requirements

- R1 室内温度曲线系统枚举扩六类（C1/C3）；既有取值逻辑不变。
- R2 蓄冷/蓄热上数 job 系统枚举扩六类（C2），并集规则与展示不变。
- R3 全量编译通过；行为验证（Test 环境）。

## Acceptance Criteria

- [ ] 室内温度曲线：纯新版项目（仅 ACAC/ACAH/ACAI 实例）曲线非空；混合项目空间并集正确；tdb 规则筛选不变。
- [ ] 蓄冷/蓄热：次日后新版实例纳入并集（job 触发后 `history_storage_result` 内容变化）；无蓄冷规则的新版实例自然不参与。
- [ ] 既有行为回归：旧三类项目曲线与标签展示与改动前一致。
- [ ] 全量编译 EXIT=0（JDK 8）。

## Out of Scope

- 能效指标部分（属旧版需求范围，已随回滚取消；本次需求未包含）。
- 蓄冷历史数据回填（180 天既往数据不重跑，如需另行确认）。
- 前端 classCode 渲染支持（联调同步项）。

## Notes

- 本任务与已回滚任务 `09-09-run-analysis-adapt-acah-acai` 的差异：**不含**能效统一池、季节门控、DashBoardInfoCodeEnum/i18n 改动；仅含两条数据范围扩展。
- 实现顺序与验证命令详见后续 implement.md。
