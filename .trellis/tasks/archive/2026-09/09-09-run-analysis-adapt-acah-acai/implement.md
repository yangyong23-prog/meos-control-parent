# 运行分析主页兼容空调供热和空调冷热源系统 — 执行计划

## 前置说明

- 本任务在分支 `feat_v7.260902.0_运行分析` 上进行（当前 clean）。
- 改动均为 common / compute-edge 的 Java 常量与查询范围扩展，无 DDL、无接口契约变化。
- 编码规范：遵循 `.trellis/spec/backend`（LogComponent 日志、DateUtils 时间、无 N+1 等）；实现前执行 `trellis-before-dev` 加载包级规范。

## 实施清单（有序）

### Step 1: 展示元数据（common）

- [ ] `DashBoardInfoCodeEnum` 新增：
  - `HEATERS_COP("16", "heatersCOP", "heatersCOP", "key_<新生成>", 7)`
  - `WTF_HT("16", "wTFht", "wTFht", "key_<新生成>", 8)`
  - ⚠ id 用 "16"（勿并入 "1"，见 design 风险 1）。
- [ ] `i18n/messages_zh.properties`、`messages_en.properties` 各新增 2 个 key（zh：热源COP、热水输配系数；en 对齐风格）。

### Step 2: 六类系统 classCode 常量复用点

- [ ] 在 classCode 枚举集中定义六类空调系统列表（优先 `DtpRwdClassCodeEnum` 增加静态方法/常量，或 RunAnalysisService 常量，实现时选一），供 Step 3/4/5 复用，禁止两处手写散列。

### Step 3: 能效指标六类化（common）

- [ ] `RunAnalysisService` 接口：新增 `EFFICIENCY_INFO_CODES`（8 项，见 design 代码块）；删除 `COLD_INFO_CODES`/`COLD_HEAT_INFO_CODES`（引用点已确认仅 RunAnalysisServiceImpl，改完同步清理）。
- [ ] `RunAnalysisServiceImpl.statistics()`：构建顺序与门控改为六类表（design §1）：
  - ACAC(order1,全年)/ACCC(order2,供冷季)/ACAH(order3,全年)/ACCH(order4,供暖季)/ACAI(order5,全年)/ACCI(order6,全年)，统一传 `EFFICIENCY_INFO_CODES`。
  - 新增 `checkSeasonToHeating()`（对称 `checkSeasonToCooling()`）。
- [ ] `buildEfficiency()`：第 ~254 行 map key 由硬编码 `COLD_SYSTEM.value()` 改为入参 classCode；infoCodes 走形参（调用点已传统一池）。
- [ ] 编译验证（见验证命令 ①）。

### Step 4: 室内温度曲线六类化（common）

- [ ] `RunAnalysisServiceImpl.queryIndoorTemp()`：classCode 列表扩为六类（用 Step 2 常量）。

### Step 5: 蓄冷/蓄热上数六类化（compute-edge）

- [ ] `HistoryStorageTaskHandler.execute()`：classCode 列表扩为六类（用 Step 2 常量；edge 侧如无法引用 common 的枚举方法则在本类/公共处定义，标注来源避免重复）。
- [ ] 编译验证（见验证命令 ②）。

### Step 6: 数据抽查（已完成 2026-09-09）

- [x] 抽查 `info_code_config`（R__init_info_code_config.sql）中 ACAI/ACAH/ACCH 的 8 项池指标覆盖：ACAC 4 项（sysCOP/chillersCOP/wTFch/wTFc）、ACAH 3 项（sysCOP/heatersCOP/wTFht）、ACCH 具备供热项；**ACAI 全缺**。
- [x] 曾为 ACAI 补"运行指标"组 + 6 项指标（saas + local 两份），经评审回退（prd.md D7：info_code_config 在能效卡链路的消费未闭环，补行无法证明 ACAI 出卡）；ACAI 字典补齐列为遗留项，需字典方/产品确认真实入口后再补。

### Step 7: 验证

- [ ] 验证命令：① `mvn -pl common -am compile`（或全量 `mvn compile -T 1C`）；② `mvn -pl local/meos-control-compute-edge -am compile`（Windows PowerShell 注意引号与编码，按仓库既有经验执行）。
- [ ] `GetProblems` 检查改动文件零报错。
- [ ] 手工验证（Test 环境，需一个有 ACAC/ACAH/ACAI 实例的项目）：
  - statistics：供热季/供冷季/过渡季各调一次，检查卡片出卡与门控（D2）、供热卡指标行（仅 heatersCOP/wTFht 且名称/单位/顺序正确）、无规则类型不出卡；⚠ ACAI 因字典缺口预期不岀卡（D7 遗留项，不作为本迭代验收阻塞）。
  - 蓄冷蓄热：手动触发 xxl-job `historyStorageHandler(syncDate=某含蓄冷实例日)` 或等待次日，对比 history_storage_result 标签是否纳入新版实例。
  - 室内温度：纯新版交付项目调 listEnergyCurveData 曲线非空；混合项目空间并集正确。
- [ ] 存量回归抽查：旧项目（ACCC 供冷季 / ACAC / ACCI）statistics 结果与基线对比（ACCI 可能新增行属预期，单独列给产品确认）。

### Step 8: 收尾

- [ ] 更新相关文档（docs/design 运行分析文档如涉及接口行为描述；无则跳过）。
- [ ] `trellis-update-spec` 沉淀本任务的六类系统口径知识（若值得沉淀：如"运行分析能效卡片指标池=8项统一池"）。
- [ ] 提交（commit message 走 meos-commit-message 规范），等待评审合并。

## 风险文件 / 回滚点

| 文件 | 风险 | 回滚点 |
|---|---|---|
| RunAnalysisServiceImpl.java | statistics 主链路 | Step 3 单独提交，可独立 revert |
| RunAnalysisService.java | 常量删除影响编译 | 与 Step 3 同提交 |
| DashBoardInfoCodeEnum.java + i18n | 枚举 id 外溢（已隔离） | Step 1 单独提交，可独立 revert |
| HistoryStorageTaskHandler.java | 上数范围变化影响历史标签口径 | Step 5 单独提交；若已重跑历史数据，回滚后需按旧口径重跑对应日期 |

## 注意

- 蓄冷历史 180 天数据默认不回填（job 只跑昨天）；如需补齐，逐日人工触发 job（成本与算法沟通），与产品确认后再执行。
- 前端 classCode（ACAH/ACCH/ACAI）渲染支持不在本仓库，联调前同步前端同学。
- 供热季门控（ACCH 供暖季出卡）与新旧并存双卡为设计假设，验收时请产品确认（D2/D3）。
- **遗留项**：ACAI 能效卡依赖外部 dtp 字典/实例化入口补齐（本迭代已回退 info_code_config 补丁，见 prd.md D7），需与字典方/产品确认真实字典维护入口后跟进。
