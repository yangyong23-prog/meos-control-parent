# Journal - jiyinchuan (Part 1)

> AI development session journal
> Started: 2026-04-21

---



## Session 1: 支持机房安全类型设备列表和网格图模版缓存

**Date**: 2026-04-21
**Task**: 支持机房安全类型设备列表和网格图模版缓存
**Branch**: `feat_v7.260402.0_机房安全`

### Summary

在 InfoCodeTemplateTypeModel.allowCache() 方法中添加 COMPUTER_ROOM_SECURITY 类型对 GRID 和 TABLE_LIST 模版类型的缓存支持。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `9e22ab563c` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 2: 新增拓扑视图模板类型及占位符替换

**Date**: 2026-04-29
**Task**: 新增拓扑视图模板类型及占位符替换
**Branch**: `feat_v7.260501.0_信息点配置`

### Summary

新增 TOPOLOGY_VIEW 模板类型枚举值；InfoCodeConfigByObjectQueryParam 新增 Map<String, Object> placeholders 字段；在 RwdObjectDetail 解析流程中支持用户传入占位符覆盖自动解析数据源；新增 i18n key_topology_view_template

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `84f4e87c75` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 3: 修复复合数据源对象归属识别

**Date**: 2026-05-12
**Task**: 修复复合数据源对象归属识别
**Branch**: `feat_v7.260501.0_信息点配置`

### Summary

修复 RWD 信息点配置复合 SpEL source key 识别，并在 relation 查询返回 relObjIds 时优先按 root objectId 精确归属；补充回归测试和 backend 规格说明。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `736b518424` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 4: Fix run overview optional outputs

**Date**: 2026-06-03
**Task**: Fix run overview optional outputs
**Branch**: `feat_v7.260601.0_复盘报告`

### Summary

Aligned run analysis report overview optional fields with product display semantics: hidden or invalid optional sections now return null, selected cooling systems are filtered by report config, and backend spec documents the contract.

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `29e0753bcc` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 5: 调试工具信息点配置合并

**Date**: 2026-06-12
**Task**: 调试工具信息点配置合并
**Branch**: `feat_v7.260602.0_信息点配置`

### Summary

实现调试工具关联运行监测类型的信息点配置合并，补充数据字典名称、showStyle 合并和列表接口隐藏调试工具专用类型。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `b3e927246d` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 6: 乙二醇泵开启中关闭中状态

**Date**: 2026-06-18
**Task**: 乙二醇泵开启中关闭中状态
**Branch**: `feat_v7.260602.0_乙二醇泵支持开启中关闭中`

### Summary

为ACCCGP供冷乙二醇泵补齐equipSwitchSet触发rcRunStatus计算配置，并归档任务。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `85da1a7230` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 7: 修复达标率计算死循环

**Date**: 2026-06-24
**Task**: 修复达标率计算死循环
**Branch**: `feat_v7.260602.0_达标率计算`

### Summary

修复 SpaceAnalyseTaskHandlerV2 达标率计算在午夜边界回溯时可能因 LocalTime 回绕导致死循环的问题，补充回归测试和后端 LocalTime 边界循环规范。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `f79037739d` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 8: UYAI-1040 缓存对象按设备类查询完整字段

**Date**: 2026-07-27
**Task**: UYAI-1040 缓存对象按设备类查询完整字段
**Branch**: `feat_v7.260701.0_性能优化`

### Summary

按 classCode 配置查询缓存完整字段，末端混合设备类使用字段并集，写服务直接消费完整快照；编译与差异检查通过。

### Main Changes

- Detailed change bullets were not supplied; see the summary above.

### Git Commits

| Hash | Message |
|------|---------|
| `b754be327b` | (see git log) |

### Testing

- Validation was not recorded for this session.

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 9: 收口并归档 UYAI-1040 任务

**Date**: 2026-07-29
**Task**: 收口并归档 UYAI-1040 任务
**Branch**: `feat_v7.260701.0_性能优化`

### Summary

依据当前代码整理需求配置结果预计算与缓存对象模型任务文档，归档全部 16 个已完成任务。

### Main Changes

- 收口最终缓存对象模型契约和提交证据
- 归档需求配置任务、缓存模型父任务及 14 个子任务

### Git Commits

| Hash | Message |
|------|---------|
| `e79ab34963` | (see git log) |

### Testing

- [OK] 16 个任务上下文校验及 JSON 校验通过
- [OK] git diff --check 通过；本轮仅修改文档和 Trellis 元数据

### Status

[OK] **Completed**

### Next Steps

- 在发布环境验证数据库迁移、RWD/Redis、XXL-JOB 和云边同步


## Session 10: 安防动态查询与设备统计优化

**Date**: 2026-08-13
**Task**: 安防动态查询与设备统计优化
**Branch**: `feat_v7.260802.0_性能优化`

### Summary

完成安防通行空间缓存路由、设备统计动态聚合和异常状态对象去重；通用 IoT 查询优化移出本任务。

### Git Commits

| Hash | Message |
|------|---------|
| `a91387afe2` | (see git log) |

### Status

[OK] **Completed**


## Session 11: 完成安防 Dashboard 需求结果预计算接入

**Date**: 2026-08-17
**Task**: 完成安防 Dashboard 需求结果预计算接入
**Branch**: `feat_v7.260802.0_性能优化`

### Summary

完成安防 Dashboard ARMING/PASSING 需求配置结果接入预计算，统一支持集合、结果读取与聚焦测试；补齐新增代码 Javadoc 并提交 UYAI-1197。

### Git Commits

| Hash | Message |
|------|---------|
| `80c70c5dbb` | (see git log) |

### Status

[OK] **Completed**


## Session 12: 完成安防性能优化父任务收尾

**Date**: 2026-08-17
**Task**: 完成安防性能优化父任务收尾
**Branch**: `feat_v7.260802.0_性能优化`

### Summary

删除依赖真实环境的集成验证开发子任务，将 58 个聚焦测试和编译证据迁移到父任务，并把真实接口、SSE 与性能复测收敛为发布前环境验证项；父任务三个代码子任务全部完成并归档。

### Git Commits

| Hash | Message |
|------|---------|
| `9824b8b0d8` | (see git log) |

### Status

[OK] **Completed**


## Session 13: IOT云端持久化查询分精度壳配置消费改造

**Date**: 2026-08-26
**Task**: IOT云端持久化查询分精度壳配置消费改造
**Branch**: `feat_v7.260901.0_分精度改造`

### Summary

完成PRD-260901后端消费改造：请求头removeDefaultIpType短路+壳配置persistent_data_query_logic（分精度去留）与persistent_data_time_interval（时间范围）两个独立配置；改造queryIpDataOnMoment(4参)/queryIpDataOnMomentInOneHour，无标识接口行为不变；SSE订阅参数转存+推送线程ThreadLocal清理；spec沉淀查询契约与日志边界规范；编译通过。
## Session 20: 缓存楼层建筑多值不落独立列
<!-- trellis-session: v=2 fp=73c62e1e121f75de -->

**Date**: 2026-09-16
**Task**: 缓存楼层建筑多值不落独立列
**Branch**: `feat_v7.260802.3_排气窗监测支持`

### Summary

缓存对象与模型视图的楼层、建筑独立列增加多值判定：值包含逗号即置空不落列，覆盖逗号分隔字符串与 JSON 数组形态（UYAI-1440）

### Main Changes

- 两个转换器统一实现 readSingleValue：包含逗号即置空，两字段独立判断且全局生效
- spec 契约同步 cache-object-model.md 三处（Contracts、Matrix、Tests）

### Git Commits

| Hash | Message |
|------|---------|
| `4c561d6189` | (see git log) |
| `cae96c8f1a` | (see git log) |

### Status

[OK] **Completed**


## Session 14: Cache锁体系优化任务收尾

**Date**: 2026-08-28
**Task**: Cache锁体系优化任务收尾
**Branch**: `feat_v7.260802.0_缓存优化`

### Summary

完成缓存锁三层架构重构（触发/计算/保存锁）、策略保存阶段事务化、旧缓存死代码清理、isSupported简化、Stopwatch全面替换PerformanceTracker、策略构建日志按策略类独立打印、机房缓存构建并行度提升至30、缓存构建占用异常提示文案国际化

### Git Commits

| Hash | Message |
|------|---------|
| `855ae01068` | (see git log) |
| `509186546c` | (see git log) |
| `c454984f93` | (see git log) |
| `416e0f1a8a` | (see git log) |
| `64e3e2a0f9` | (see git log) |

### Status

[OK] **Completed**


## Session 15: 修复缓存对象表并发写入超时

**Date**: 2026-09-01
**Task**: 修复缓存对象表并发写入超时
**Branch**: `feat_v7.260901.0_缓存优化`

### Summary

为缓存对象模型写入增加项目级保存锁与有限锁获取重试，恢复每批1000条边界，补齐单元测试并完成JDK 8全模块编译验证。

### Git Commits

| Hash | Message |
|------|---------|
| `90189ee0e9` | (see git log) |

### Status

[OK] **Completed**


## Session 16: 运行策略接口查询IOT改造为查询最新一条（分精度）

**Date**: 2026-09-04
**Task**: 运行策略接口查询IOT改造为查询最新一条（分精度）
**Branch**: `feat_v7.260901.0_分精度改造`

### Summary

补齐 PRD-260901 遗留的"控制台自行改造"缺口：冷机/冷热站/末级泵组三个 listRunningStrategy 接口（UYAI-1373）的 IOT 持久化查询由固定 15min period 消费（`queryObjectIdPersistentDataList`，不经双条件门控）改为直接消费已验证的 4 参 moment 门控入口 `queryIpDataOnMoment`；latest 双条件命中时自动查最新一条（不传 ipType + interval 窗口），回退场景由 moment legacy（15min + 前端替换参数）承接，与改造前输出等价。saas 侧 5 处替换（StrategyServiceImpl ×2、DataBoardServiceImpl ×3），`queryManualLastpumbParameter` 签名降为单参，末级泵组 updateTime 跟随实际数据时间，泵组运行状态逐条消费修正取值错位缺陷；common/local 零改动。边缘三接口经核实数据源（Twin moment/EquipRecord）天然最新，无需改动。spec 沉淀控制台面板 latest 消费场景（含"直接换 moment 不自行门控/不去重"模式与 5min 精度不可 moment 化边界）。

### Git Commits

| Hash | Message |
|------|---------|
| `341738a5e1` | (see git log) |
| `336bf3f85d` | (see git log) |

### Testing

- [OK] common/saas meos-control-common/saas meos-control-server(-am)/local meos-control-common-edge 编译通过（JDK 8）
- [OK] 运行环境行为回归：无头 / same_time+替换头 / latest+interval / 壳配置异常 四场景通过

### Status

[OK] **Completed**


## Session 17: 照明需求目标接口按指定日期查询支持

**Date**: 2026-09-07
**Task**: 照明需求目标接口按指定日期查询支持
**Branch**: `feat_v7.260901.0_照明接口适配`

### Summary

需求：dtp-meos-server-edge 外部服务需按任意日期查询照明需求目标。/lighting/getDemandTarget 新增选填 date 入参（缺省当天向后兼容），服务内模板、场景、临时任务、funcTypeConfigs 汇总四处'系统当天'统一替换为目标日期；@Operation tags 标记 DTP_MEOS_SERVER_EDGE。发现并修复 funcTypeConfigs 汇总窗口硬编码 DateUtils.now() 的遗漏点（fillFuncType2CircuitSetting/processTaskConfigs/mergeAndSplitTimeRanges）。JDK8 编译通过，commit 6802f3cf5d。

### Git Commits

| Hash | Message |
|------|---------|
| `6802f3cf5d` | (see git log) |

### Status

[OK] **Completed**


## Session 18: latest 查询逻辑固定传 5min 分精度

**Date**: 2026-09-07
**Task**: latest 查询逻辑固定传 5min 分精度（UYAI-1199）
**Branch**: `feat_v7.260901.0_分精度改造`

### Summary

产品反馈 PRD-260901 迭代修正：latest（壳配置"查询距离当前最新的数据"）生效分支原设计"不传分精度"，但 DTP 持久化服务对缺省 ipType 默认按 15min 处理导致查询结果不对。改动：`DtpRwdClientServiceImpl.queryIpDataOnMoment(4参)` 与 `queryIpDataOnMomentInOneHour` 两个 latest 分支时间窗口逻辑不变，固定 `periodDTO.setIpType("5min")`（传 5min 仍可查到 15min 等更大分精度数据，latest 语义不受破坏）；同步修正两处分支注释与 `isLatestDataQueryLogic` javadoc，并更新 `.trellis/spec/backend/iot-persistence-query-rule.md` 全部固化点（Scope/Signatures/Contracts/Validation Matrix/Good-Base-Bad/Tests/Correct 代码块/Scenario 2 契约），新增 2026-09-07 产品修正说明。门控双条件、legacy 分支（15min+替换参数）、显式分精度调用点均未触碰。

### Git Commits

| Hash | Message |
|------|---------|
| `8c8e9da028` | `[7.260901.0](UYAI-1199)fix: latest查询逻辑固定传5min分精度` |

### Testing

- [OK] `mvn -pl common -am compile` 编译通过（JDK 8：LibericaJDK-8，系统默认 JDK25 与旧版 Lombok 不兼容）
- [TODO] 运行环境行为回归：latest+标识 → ipType=5min 且能取到 15min 等更大分精度数据；无头/same_time/显式分精度不变（待运行环境验证）

### Status

[OK] **Completed**


## Session 17: 能耗标签第三方查询i18n key泄漏修复

**Date**: 2026-09-08
**Task**: 能耗标签第三方查询i18n key泄漏修复
**Branch**: `feat_v7.260901.0_运行分析`

### Summary

线上能耗统计无数据根因：EnergyModelTagEnum 经 value() 序列化把 name 的 i18n key 泄漏进第三方 tagNames 请求体。方案经 5 轮评审收敛：tagNames 改 List<String> 传解析后的第三方字典名称；枚举 4 个查询标签换独立 i18n key（zh/en 对齐 energy_model_item_tag 字典：供冷系统用电量/Cooling System Electricity Consumption 等），共享 key（运行日志分类等）零改动；TOTAL 不参与查询保持原样。编译+单测 2/2 通过，已提交 UYAI-1378，待测试环境 HILON 联调验证。

### Git Commits

| Hash | Message |
|------|---------|
| `b27364c542` | (see git log) |

### Status

[OK] **Completed**


## Session 19: 客流出入口统计新增客流人数计算
<!-- trellis-session: v=2 fp=2887167e19b366a5 -->

**Date**: 2026-09-09
**Task**: 客流出入口统计新增客流人数计算
**Branch**: `feat_v7.260902.0_客流`

### Summary

getPassagewayHeadCount接口按UYAI-1322需求新增客流人数：PassengerFlowHomeStatistics新增netPeopleFlow（项目/门组统计行，accm累计进入-累计离开，任一侧缺失为null前端显示--）、TendencyFigure新增spNetPeopleFlow（24小时图点，accd当日累计差分同源逐点差值，无数据小时点null）。不改数据源与规则过滤，纯增量契约，SSE复用方自动兼容。common模块与全reactor mvn compile(JDK8)通过。

### Git Commits

| Hash | Message |
|------|---------|
| `c7df54e4b7` | [7.260902.0](UYAI-1322)feat: 客流出入口统计新增客流人数计算 |
| `5a2bc6050d` | [7.260802.3](UYAI-1440)fix: 缓存对象楼层建筑多值不落独立列 |
| `c510441b1c` | chore(task): archive 09-16-cache-floor-building-multi-value-skip |

### Testing

- [OK] 两个转换器测试类共 5 个用例通过（逗号分隔/JSON 数组/单值/独立判断）
- [OK] mvn -pl common test 编译与单测通过（JDK 8）

### Status

[OK] **Completed**


## Session 21: 缓存表 floorId/buildingId 多值改存第一个
<!-- trellis-session: v=2 fp=f1a02b14bd710323 -->

**Date**: 2026-09-20
**Task**: 缓存表 floorId/buildingId 多值改存第一个
**Branch**: `feat_v7.260802.3_排气窗监测支持`

### Summary

修订缓存对象与模型视图 floorId/buildingId 独立列的多值行为：由置空改为写入第一个值，兼容逗号分隔字符串与 JSON 数组（含单元素）；单值与空串原样，缺失为 NULL。修改两个转换器的 readSingleValue 与两个测试类，同步 cache-object-model 规范三处；编译与单测通过（UYAI-1488）

### Git Commits

| Hash | Message |
|------|---------|
| `b5ed68ee69` | [7.260802.3](UYAI-1488)fix: 缓存对象楼层建筑多值改存第一个值 |

### Status

[OK] **Completed**


## Session 22: 需求管理新增厨房排油烟
<!-- trellis-session: v=2 fp=3148fb45104478ce -->

**Date**: 2026-10-09
**Task**: 需求管理新增厨房排油烟
**Branch**: `feat_v7.261001.0_厨房排油烟`

### Summary

新增独立需求大类 KITCHEN_RANGE_HOOD：枚举接线、大类策略（空间查询复用运行监控厨房油烟机口径：ACKLSF+fanPos=1→Eq2Sp→needExaustSmoke=1）、仅排油烟需求时间配置（FIELD 继承）。不接入预计算，无 DB 变更。

### Main Changes

- 新增4类+2枚举接线
- 空间口径委托运行监控

### Git Commits

| Hash | Message |
|------|---------|
| `dc863b2a79` | [7.261001.0](UYAI-1556)feat: 需求管理新增厨房排油烟 |
| `065e5d2348` | chore(task): archive 10-09-kitchen-exhaust-demand |

### Testing

- [OK] 全量mvn compile（JDK8）BUILD SUCCESS

### Status

[OK] **Completed**

### Next Steps

- 联调运行验证AC1-AC6


## Session 23: 监测Dashboard排油烟需求时间接口
<!-- trellis-session: v=2 fp=4da52ad4c979b393 -->

**Date**: 2026-10-10
**Task**: 监测Dashboard排油烟需求时间接口
**Branch**: `feat_v7.261001.0_厨房排油烟`

### Summary

新增 POST /kitchenRangeHood/queryDemandTime 接口, 合并去重需求时段原位回填 space 与 equipment; 解析与归并行为内聚 DemandTimeIntervalValueObject, 复用 DemandTimeValueObject 承载绝对时间; 全量复查通过, 单测 12/12

### Git Commits

| Hash | Message |
|------|---------|
| `8a6dca1830` | [7.261001.0](UYAI-1556)feat: 新增厨房排油烟需求时间查询接口 |

### Status

[OK] **Completed**
