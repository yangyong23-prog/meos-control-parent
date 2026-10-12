# 运行分析主页兼容空调供热和空调冷热源系统 — 技术设计

## 架构与边界

- 展示端（能效卡片、室内温度曲线）位于 `common` 模块 `RunAnalysisServiceImpl`，仅供云端 saas server 部署（edge server 无 runanalysis controller）。
- 蓄冷/蓄热上数位于 `local/meos-control-compute-edge` xxl-job `historyStorageHandler`；展示端只读 `history_storage_result` 表。云端 saas compute 无对应 job（算法直推入口 `storageTagSync` 无系统枚举，不改）。
- 本迭代改动 2 个模块、4 个文件内方法 + 1 个枚举 + 2 个 i18n 资源文件，无 DDL、无接口契约变化（`statistics`/`listEnergyCurveData` 响应结构不变）。

## 改动设计

### 1. 能效指标六类化（common）

**常量（RunAnalysisService 接口）**
- 新增统一指标全集 `EFFICIENCY_INFO_CODES`（8 项，显式枚举，见下）；供冷/供热/冷热源类共用该池，最终展示行由实例化规则自适应过滤。
- 删除或保留 `COLD_INFO_CODES`/`COLD_HEAT_INFO_CODES`：全仓库引用点仅 `RunAnalysisServiceImpl`（见调研）。设计删除，防止将来再次误用分池；若评审担心历史引用，可保留并标注 Deprecated（二选一，倾向删除并同步清理）。

```java
List<String> EFFICIENCY_INFO_CODES = Arrays.asList(
    DashBoardInfoCodeEnum.SYS_COP.getCode(),           // sysCOP  系统EER(含二次泵)
    DashBoardInfoCodeEnum.CHILLERS_COP.getCode(),      // chillersCOP 主机COP
    DashBoardInfoCodeEnum.WTF_CH.getCode(),            // wTFch 冷冻泵输配系数
    DashBoardInfoCodeEnum.WTF_C.getCode(),             // wTFc 冷却泵输配系数
    DashBoardInfoCodeEnum.CT_HEAT_FACTOR.getCode(),    // cTHeatFactor 冷却塔输送系数
    DashBoardInfoCodeEnum.THERMAL_UNBALANCE_RATE.getCode(), // thermalUnbalanceRate 热不平衡率
    DashBoardInfoCodeEnum.HEATERS_COP.getCode(),       // heatersCOP 热源COP(新增)
    DashBoardInfoCodeEnum.WTF_HT.getCode());           // wTFht 热水输配系数(新增)
```

**statistics() 六类构建（季节门控对称扩展）**

| classCode | 指标池 | order | 门控 | 说明 |
|---|---|---|---|---|
| ACAC | 8 项池 | 1 | 全年 | 现状不变 |
| ACCC | 8 项池 | 2 | 供冷季 | 现状门控不变 |
| ACAH | 8 项池 | 3 | 全年 | 新增 |
| ACCH | 8 项池 | 4 | 供暖季 | 新增（对称 ACCC） |
| ACAI | 8 项池 | 5 | 全年 | 新增 |
| ACCI | 8 项池 | 6 | 全年 | 现状 order=3 → 6 |

- 新增 `checkSeasonToHeating()` 判断供暖季（`SEASON_HEATING`），对称现有 `checkSeasonToCooling()`。
- 卡片顺序按"新版优先于旧版、供冷→供热→冷热源"；存量单系统项目无顺序感知，混合项目（ACAC+ACCC 并存）顺序不变。

**buildEfficiency() 内部**
- 第 254 行 `mapObjectIdAndExistsRuleInfoCodes` 的 map key 由硬编码 `COLD_SYSTEM.value()` 改为实际入参 classCode（cloud/edge 实现实际只摊平 objectIds 查询，key 无过滤作用，改后语义正确）。
- 第 254 行查询的 infoCodes 参数改为统一池入参（buildEfficiency 已接收 infoCodes 形参，调用点传 EFFICIENCY_INFO_CODES 即可）。
- 其余逻辑（监测状态 2/3 过滤、实例规则过滤、整类无规则不出卡、实例内按 DashBoardInfoCodeEnum order 排序）不变。

### 2. DashBoardInfoCodeEnum + i18n（common）

```java
HEATERS_COP("16", "heatersCOP", "heatersCOP", "key_<新>", 7),
WTF_HT("16", "wTFht", "wTFht", "key_<新>", 8),
```

- **id 必须用未占用的 "16"**，不能并入 "1"：`getInfoCodeList("1")` 被 `DashBoardServiceImpl.getSystemEfficiency`（系统图能效后评估）使用，并入会把新指标外溢到系统图页面（超出本迭代范围）。若未来系统图要支持供热能效再调整分组。
- aliasCode 暂与 code 同名：aliasCode 参与后评估 efficiencyCode 反向匹配（getLocalCode），本仓库无法确认能量后评估侧的供热效率编码，联调时需核对；同名策略下匹配不上只影响系统图（不在本迭代验收范围），不会报错。
- i18n：`common/src/main/resources/i18n/messages_zh.properties`、`messages_en.properties` 各加 2 个 key。文案按 PRD 表格显示名：zh `热源COP`、`热水输配系数`；en 对齐现有风格（如 `Heat Source COP`、`Hot Water Pump Rate`，最终以评审为准）。
- order 7/8 使供热指标排在供冷 6 项之后；供热卡只有 2 行有规则，显示顺序即 7、8。

### 3. 室内温度曲线六类化（common）

- `queryIndoorTemp()`：classCode 列表扩为六类（ACCC/ACCI/ACCH + ACAC/ACAH/ACAI），其余链路（Sy2Sp 服务空间 → tdb 实例化规则 → 空调需求模板过滤 → 5min 均值）零改动。

### 4. 蓄冷/蓄热上数六类化（local/meos-control-compute-edge）

- `HistoryStorageTaskHandler.execute()`：classCode 列表扩为六类；不新增监测状态过滤（PRD：不限状态）。新版实例无"蓄冷/蓄热时间段"实例化规则 → 算法不返回该实例 → 不参与并集，符合 PRD 描述。
- 并集规则（全 0 才 NONE、有蓄冷即 COLD、否则 HOT）与 15 分钟粒度不变。
- 六类列表建议抽为 `RunAnalysisService` 风格常量或枚举方法复用（室内温度与蓄冷上数两处都需要"六类系统 classCode"），避免散落重复：新增 `DtpRwdClassCodeEnum.AIR_CONDITION_SYSTEM_CLASS_CODES` 类方法或 common 侧常量（实现时选其一，优先复用现有 enum 集中定义）。

### 5. 数据与兼容

- heatersCOP/wTFht 的字典定义（info_code_config）、实例化规则机制均已存在（ACAH/ACCH/ACCI 关键指标组），**无需 SQL 迁移**。实现期抽查结论：ACAI 对象类在 info_code_config 基线中缺失全部 8 项池指标定义（ACAC/ACAH 均有）；曾补 6 项字典行（saas+local R__init）后按评审回退——info_code_config 在本仓库的消费链（RcInfoCodeCfg→Redis→系统图/照明展示）与能效卡出卡的实例化/知识链路（外部 dtp-rwd / dtp-knowledge-server）未闭环，补行无法证明 ACAI 出卡（详见 prd.md D7）。
- BDTP 日维度查询 `queryRwdObjectValueMap`（ipType=DAY）不改。
- `mapInfoCodeAndData(true, false, classCode)`（unit 来源）按实例 classCode 查询，供热类已有字典 → 正常。

## 兼容性 / 回滚

- **API 兼容**：`statistics` 响应中新增卡片（classCode=ACAH/ACCH/ACAI），响应结构不变；前端若按 classCode 白名单渲染卡片标题，需前端同步支持新 classCode（本仓库无前端代码，列入联调风险，不阻塞后端）。
- **存量行为变化**：存量 ACCI 项目若实例化了 wTFc/heatersCOP/wTFht 等规则，首页冷热源卡可能新增行（D1 预期补全，验收时与产品确认）。
- **蓄冷/蓄热历史数据**：job 每天只上数"昨天"，发布前已生成的 180 天内 history_storage_result 不含新类型实例并集；如需历史补齐需人工逐日触发 `historyStorageHandler(syncDate)`（调用算法服务有成本，默认不重跑，与产品确认）。
- **回滚**：全部为 Java 枚举/常量/方法改动，无 DDL；回滚 = revert 提交 + 重新部署 common/server 与 compute-edge。若发布期间执行过蓄冷历史重跑，回滚后历史标签为六类口径、与旧代码语义不一致，需再按旧口径重跑（或接受）。

## 关键风险

1. DashBoardInfoCodeEnum 新枚举 id 误用 "1" → 系统图能效页面外溢（已用 id="16" 规避）。
2. aliasCode 与能量后评估 efficiencyCode 不匹配 → 仅影响系统图供热能效展示（不在范围）。
3. ACAI 字典指标缺失（已确认）：ACAI 在 info_code_config 无任何能效指标定义；曾补 6 项后回退（prd D7），能效卡实际是否出卡取决于外部 dtp 字典/实例化入口，本迭代 ACAI 预期不岀卡，遗留待字典方/产品确认。
4. 前端 classCode 渲染白名单 → 联调同步项。
5. 供热季门控（D2）、新旧并存双卡（D3）为按 PRD 字面的设计假设 → 验收时产品确认。
