# 运行分析主页兼容空调供热和空调冷热源系统

## Goal

运行分析"日运行结果（首页）"当前仅完整适配部分系统类型（历史项目旧版 ACCC/ACCH/ACCI 与新项目 RCC 新版 ACAC/ACAH/ACAI 并存交付），本迭代让首页在六类系统类型下均按项目实际交付实例正确展示，做到"按实例化结果自适应"。

## Requirements

### 背景事实

- 历史项目交付旧版系统：中央供冷 ACCC、中央供热 ACCH、中央冷热源 ACCI；新项目按 RCC 体系交付新版：中央空调供冷全系统 ACAC、中央空调供热全系统 ACAH、中央空调冷热源全系统 ACAI。
- 新旧对应关系：ACAC↔ACCC（供冷）、ACAH↔ACCH（供热）、ACAI↔ACCI（冷热源）。同一供能属性下新旧系统可能在同一项目并存（如旧冷站改造新增 ACAC 实例），需定义并存优先级。
- 能效指标数据通过 BDTP 获取日维度数据，无需业务单独计算。

### 需求条目（来源：【PRD-260902】）

1. **室内温度曲线多系统适配（高）**
   - 既有规则：空间取值范围为中央供冷/供热/冷热源系统实例（旧版三类）服务的空间，且制定了"运行标准-空调需求"的空间；空间筛选仅选择空气（干球）温度（实际值）（tdb）有实例化规则的空间。
   - 适配为：空间取值范围扩展为项目交付的六类系统实例通过"系统服务于空间（Sy2Sp）"关系服务的空间并集；其余规则不变。

2. **能效指标多系统适配（中）**
   - 系统实例范围：取六类系统对象类中监测监控状态（monitoringSurveillanceStatus）=2（可监不可控）或 3（可监可控）的实例；状态 1 或空值不展示。
   - 指标项：系统EER（sysCOP）、主机COP（chillersCOP）、冷冻泵输配系数（wTFch）、冷却泵输配系数（wTFc）、冷却塔输送系数（cTHeatFactor）、热不平衡率（thermalUnbalanceRate）；**新增** 热源COP（heatersCOP）、热水输配系数（wTFht）。
   - 按各系统实例展示有实例化规则的指标；若系统没有任何一个指标有实例化规则则不展示该系统卡片（待开发确认）。
   - 多个系统实例独立展示。

3. **蓄冷/蓄热时段取值多系统适配（中）**
   - 既有规则：项目下全部系统实例的"蓄冷/蓄热时间段"分别取并集，系统实例枚举为旧版三类。
   - 适配为：取并集的系统实例范围扩展至项目交付的全部六类系统实例，不限监测监控状态；并集计算规则与 BDTP 上数时间点取值展示逻辑不变；新版系统实例无对应信息点实例化规则时自然不参与并集（待开发确认原取值逻辑）。

### 代码现状（调研确认，2026-09-09）

**① 能效指标（日运行结果 statistics 接口）** — `RunAnalysisServiceImpl.statistics()`（common 模块）

- 指标常量在 `RunAnalysisService` 接口：`COLD_INFO_CODES`（供冷 6 项：sysCOP/chillersCOP/wTFch/wTFc/cTHeatFactor/thermalUnbalanceRate）、`COLD_HEAT_INFO_CODES`（冷热源 3 项：sysCOP/chillersCOP/wTFch）。
- 现状仅对三类构建卡片：ACCI(COLD_HEAT_INFO_CODES, order=3)、ACAC(COLD_INFO_CODES, order=1)、ACCC(COLD_INFO_CODES, order=2，仅当日为供冷季才构建)。**ACAH/ACAI/ACCH 完全没有卡片**。
- `buildEfficiency()` 行为：按监测监控状态 2/3 过滤实例（`filterMonitoringSurveillanceStatus()`）；按实例化指标规则自适应展示（实例无规则跳过；该类整体无规则则卡片不返回）；卡片内按 monitoringSurveillanceStatus 降序、名称排序；多实例独立展示。
- `DashBoardInfoCodeEnum`（common）缺 heatersCOP/wTFht 的映射（名称 i18n key、显示顺序）；但 `info_code_config` 字典中 ACAH/ACCH/ACCI 已有两者定义（"热源COP（效率）"/"热水输配系数"，关键指标组），实例化规则体系已支持。i18n 资源：common/src/main/resources/i18n/messages_zh|en.properties。
- 新老并存时现状逻辑会同时出卡（如 ACCC+ACAC 并存 → 两张卡片），“并存优先级”未实现。

**② 室内温度曲线（listEnergyCurveData 接口 → queryIndoorTemp）**

- 空间取值枚举仅 ACCC/ACCI/ACCH 三类系统实例 → 通过 Sy2Sp（SYSTEM_SERVICE_SPACE）取服务空间 → `RunningMonitorRunAnalysisStrategy.listExistsRuleSpaceIdBySystemIds()` 筛有 tdb 实例化规则空间 → 筛有空调需求模板（运行标准-空调需求）空间 → 5 分钟 tdb 均值曲线。改造点仅 classCode 列表扩为六类。

**③ 蓄冷/蓄热时段（能耗趋势图 tagData）**

- 展示端 `queryCoolHeatTag()` 直接读 `history_storage_result` 表（按日期），无系统枚举；**系统实例枚举发生在数据上数侧**：`local/meos-control-compute-edge/.../task/HistoryStorageTaskHandler.java`（xxl-job `historyStorageHandler`，每天 05:00 跑昨天）枚举 ACCI/ACCC/ACCH 三类实例 ID → 调算法 `POST /tag/getHistoryStorage` → 15 分钟粒度标签并集（0无/1蓄冷/2蓄热）→ 写表并同步云端。改造点：classCode 列表扩为六类（新版实例无蓄冷规则时算法自然不返回，不参与并集）。
- 云端 saas/meos-control-compute 无该 job（运行分析页面仅在云端 common/server 提供，edge server 无 runanalysis controller）；云端另存算法直推入口 `POST /runAnalysis/history/storageTag/sync`（无系统枚举）。

**④ 边界**：`listAnalysisType()`/`getAnalysisDataTree()`（下钻入口，仅判 ACCC/ACAC 供冷实例）属于过程调节分析，按 PRD 不在本迭代范围。

### 决策与设计假设（按 PRD 字面收敛，待最终规划摘要批准）

- D1 指标池：六类系统统一使用 PRD 表格 8 项指标全集（sysCOP/chillersCOP/wTFch/wTFc/cTHeatFactor/thermalUnbalanceRate/heatersCOP/wTFht）作为允许范围，最终展示行由实例化规则自适应过滤（供热类实例的字典与规则里无冷侧指标，自然只展示供热行；供冷类反之）。
- D2 卡片构建：现状季节门控保留并对称扩展 —— ACCC 仅供冷季构建（现状）、ACAC/ACCI 全年（现状）；ACCH 仅供暖季构建、ACAH/ACAI 全年（对称推断，需产品在验收时确认）。ACAI 与 ACCI 行为一致。
- D3 卡片排序（order）：供冷类在前、供热类次之、冷热源殿后；新旧同供能并存时各自独立出卡（PRD 未定义隐藏/优先级规则，"多个系统实例独立展示"按字面执行）。
- D4 蓄冷/蓄热：仅改上数 job（HistoryStorageTaskHandler）实例枚举为六类；展示端与并集规则不动；新版实例无蓄冷规则时算法自然不返回、不参与并集（C2 确认成立）。云端算法直推入口 storageTagSync 无系统枚举、不改。
- D5 室内温度曲线：queryIndoorTemp 的 classCode 列表扩为六类，其余过滤链路（Sy2Sp、tdb 规则、空调需求模板）不动。
- D6 新增指标展示元数据：DashBoardInfoCodeEnum 增补 heatersCOP/wTFht（code/aliasCode/i18n key/order，id=16 独立分组防外溢系统图），i18n 资源（zh/en）增补 key。
- D7 ACAI 字典缺口（实现期发现 2026-09-09；SQL 补丁已回退）：ACAI 对象类在 info_code_config 基线（R__init_info_code_config.sql）中缺失全部 8 项池能效指标定义（对照 ACAC 4 项、ACAH 3 项均在该文件维护）。初版方案为补 6 项字典行（saas + local 两份），经核查后回退：本仓库内 info_code_config 表仅被 RcInfoCodeCfg → Redis 链路消费（系统图/照明信息点展示）；能效卡链路的指标知识（mapInfoCodeAndData → 外部 dtp-knowledge-server）与实例化规则（→ 外部 dtp-rwd 孪生服务）均为外部服务，无法在本仓库证实其读取该表，补行无法闭环证明 ACAI 出卡。**结论：ACAI 能效卡能力取决于字典/实例化真实入口（外部 dtp 服务侧或项目实例化配置），本迭代 ACAI 可能不出卡，遗留待字典方/产品确认**；代码仍保留 ACAI 六类构建（自适应，字典就绪后自动出卡）。

### 确认项（PRD 请求开发确认，已确认结论）

- C1 能效指标查询逻辑：现状为按系统类构建卡片（ACCI/ACAC/ACCC）→ 监测状态 2/3 实例 → 实例化指标规则过滤 → 无规则实例不出卡、整类无规则不出卡；数据走 BDTP 日维度（queryRwdObjectValueMap）。本迭代范围扩至六类、指标全集 8 项。
- C2 蓄冷/蓄热取值逻辑：展示端直接读 history_storage_result；并集实例枚举在 edge compute 上数 job（xxl-job historyStorageHandler）。本迭代将上数枚举扩至六类；新版实例无对应信息点规则时自然不返回、不参与并集（与 PRD 描述一致，无需额外判断）。
- C3 新旧并存优先级：PRD 未给出隐藏/优先级规则；按"多个系统实例独立展示"字面执行（新旧同供能并存各自出卡）。已在 D3 注明，需产品验收时确认。

## Acceptance Criteria

- [ ] 能效（六类）：每类系统存在监测状态 2/3 实例且至少一个 8 项池内指标有实例化规则时，出对应卡片；ACCH 仅在供暖季出卡、ACCC 仅在供冷季出卡、ACAC/ACAH/ACAI/ACCI 全年（D2）。⚠ ACAI 因字典缺口（D7）本迭代预期不岀卡，验收以 ACAC/ACAH/ACCC/ACCH/ACCI 为准，ACAI 列为遗留项。
- [ ] 能效（指标行）：卡片指标行名称、单位、显示顺序正确（新增 heatersCOP/wTFht 名称按 PRD 显示名；排序在各自卡内合理）；供热类卡不出现冷侧指标行；数据为 BDTP 日维度值。
- [ ] 能效（自适应）：整类无实例化指标规则时不出卡；无指标规则的实例不出现在卡内；新旧同供能并存时各自独立成卡（D3）。
- [ ] 能效（回归）：存量仅 ACCC/ACAC/ACCI 项目：ACCC（供冷季）/ACAC/ACCI 卡仍正常出卡；存量 ACCI 卡因 D1 统一池可能新增行（如实例化过 wTFc/heatersCOP/wTFht），需与产品确认视为预期补全。
- [ ] 室内温度曲线：仅交付 ACAC/ACAH/ACAI 的新项目（无旧版实例）曲线非空；新旧混合项目覆盖六类系统服务空间并集；tdb 规则/空调需求模板过滤不变。
- [ ] 蓄冷/蓄热标签：上数 job 覆盖六类系统实例（不限监测状态）；含新版蓄冷实例的项目 tagData 正确体现并集（需重跑 job 或等待次日任务验证）；纯供冷无蓄冷项目仍返回空。
- [ ] 展示元数据：DashBoardInfoCodeEnum/i18n 增补后，指标行名称无 key 泄漏；zh/en 均显示正确。

## Out of Scope

- 过程调节分析（下钻分析）的多系统适配（后续单独迭代）
- 设备控制日志的多系统适配（后续单独迭代）

## Notes

- 迭代号 26.0902；git 分支 feat_v7.260902.0_运行分析，当前分支 clean。
- 需求来源：【PRD-260902】运行分析主页兼容空调供热和空调冷热源系统（临时文档，评审用，已不保留）。
- 2026-09-09 决策记录：曾为 ACAI 补 6 项字典行（R__init_info_code_config.sql ×2）后按评审回退，回退原因见 D7（info_code_config 在能效卡链路的消费未闭环）；ACAH/ACCH 字典已具备，不受影响。
- 2026-09-11 产品需求变更：本任务六类适配代码已全部回滚至适配前状态（回滚提交 `19ff6d84a6`，7 files +110/-166；处理任务 09-11-run-analysis-requirement-change）。
