# 需求管理新增厨房排油烟

## Goal

在需求管理中新增独立的"厨房排油烟"需求大类（与空调/照明/安防/通风并列）。用户在需求管理页面可完成：需求模板创建、场景与日期设置、需求空间树浏览、按实例层级（项目/建筑/楼层/空间）配置"排油烟需求时间"、配置继承、黑名单与屏蔽空间。

用户价值：为厨房排油烟集中风机提供按日期/场景生效的排油烟需求时间配置能力，体验与现有需求管理一致。

## Background

### 业务口径（用户提供）

需求空间 = 同时满足以下条件的空间：

1. 项目下存在设备类 `ACKLSF`（排油烟风机，一般叫集中风机/大风机）且风机定位 `fanPos=1`（集中风机）的设备；
2. 该设备存在"设备服务于空间"关系：图类型 `MechForArch`、边类型 `Eq2Sp`，from 设备，to 空间（对应 `DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE`，`common/.../model/enumtype/dtprwd/DtpRwdRelationTypeEnum.java#L50`）；
3. 服务空间的信息点 `needExaustSmoke=1`（是否需要排油烟）。

配置内容：仅"排油烟需求时间"——多时间段列表，结构同通风需求的 `DemandTimeIntervalValueObject`（开始/结束时间 + 是否跨天，支持日出日落等特殊时间类型）。

### 现有实现证据

- 上述空间筛选口径已在运行监控侧实现，可直接复用：`RunningMonitorKitChenRangeHoodStrategy#listSpaces`（`common/.../structure/common/strategy/impl/RunningMonitorKitChenRangeHoodStrategy.java#L66-L84`）。
- 需求管理扩展模式：`DemandTypeEnum`（大类）→ `DemandStrategy#listDemandSpace`（大类策略）→ `DemandTemplateTypeEnum`（模板类型）→ `DemandConfigAnalyzeStrategy`（配置解析策略）；模块文档见 `docs/design/demand-management/strategy-and-business-types.md`。
- 信息点枚举已存在：`InfoCodeEnum.FAN_POS`、`InfoCodeEnum.NEED_EXAUST_SMOKE`（`common/.../model/base/InfoCodeEnum.java#L308-L309`）。
- "KITCHEN_RANGE_HOOD（厨房油烟机）"术语与 i18n key（`key_69d5825940c14669b2fd0621f24a8f7a`）已在运行监控、运行日志、SSE、系统类编码等处使用。
- 黑名单/屏蔽空间过滤由查询服务层统一处理（对策略透明），新大类自动生效。

## Requirements

- R1 需求大类：新增独立需求大类"厨房排油烟"（枚举值 `KITCHEN_RANGE_HOOD`）；`/demand/template/listTemplateType` 在项目存在需求空间时返回该大类。
- R2 模板类型：新大类下挂 1 个模板类型（`KITCHEN_RANGE_HOOD`）。
- R3 空间范围：按业务口径三条件筛选；支持实例层级过滤（建筑/楼层/空间）、黑名单空间过滤、屏蔽空间过滤。
- R4 配置内容：仅"排油烟需求时间"一个字段；支持项目级配置向建筑/楼层/空间字段级继承（`InheritConfigTypeEnum.FIELD`，与通风一致）；不含送风需求时间、不含参与反馈调节。
- R5 命名与编码：i18n key 复用 `key_69d5825940c14669b2fd0621f24a8f7a`；`productCode = kitchenRangeHoodDemand`。
- R6 预计算：本期不接入（`SUPPORTED_DEMAND_TYPES` 不变；预计算刷新/初始化/DTX 等入口按既有机制自动跳过本大类）。
- R7 无数据库结构变更；无新增 i18n 资源。

## 已决策记录

| 编号 | 决策点 | 结论 | 依据 |
| --- | --- | --- | --- |
| D1 | 架构定位 | 独立需求大类（非通风子场景） | 用户确认 |
| D2 | 配置字段 | 仅排油烟需求时间 | 用户确认 |
| D3 | 预计算 | 本期不接入，走实时继承计算 | 用户确认；与照明现状一致 |
| D4 | 命名与编码 | 枚举 `KITCHEN_RANGE_HOOD`；i18n key 复用；productCode `kitchenRangeHoodDemand` | 用户确认 |

## Acceptance Criteria

- [ ] AC1 `/demand/template/listTemplateType` 在项目有需求空间时返回新大类（demandType=KITCHEN_RANGE_HOOD、productCode=kitchenRangeHoodDemand），含 1 个模板类型；项目无需求空间时不返回（沿用"有空间才展示"语义）。
- [ ] AC2 需求空间树/实例树/楼层空间树按 R3 口径正确返回：ACKLSF 且 fanPos=1 的设备所服务、且 needExaustSmoke=1 的空间被纳入；fanPos≠1、无 Eq2Sp 服务关系、needExaustSmoke≠1 的空间被排除。
- [ ] AC3 配置保存后 `/demand/config/listConfigsByInstance` 正确返回排油烟需求时间（JSON 往返解析正确；空配置、多时间段、跨天时段均正确）。
- [ ] AC4 继承：项目级配置可被建筑/楼层/空间继承（字段级）；未配置层级返回上级继承结果。
- [ ] AC5 黑名单/屏蔽空间过滤对新大类生效。
- [ ] AC6 预计算入口对本大类不生效：`supportsTemplateType(KITCHEN_RANGE_HOOD)=false`，刷新/初始化任务不含新模板类型；实时配置查询链路正确。
- [ ] AC7 `common` 模块及依赖它的模块编译通过。

## Out of Scope

- 需求配置结果预计算接入（D3；后续迭代可一行枚举补上）。
- 运行监控/运行监测改动（`RunningMonitorKitChenRangeHoodStrategy` 已存在，本期不改）。
- 配置下发/边缘联动、外部系统对接（`externalListConfigResultsByInstance` 沿用基类空实现）。
- 前端页面改动（后端提供接口契约，前端另行对接）。
- 预置默认时间值（默认空配置，由用户配置）。

## Notes

- 技术设计见 `design.md`；执行计划见 `implement.md`。
- 核心实现路径：枚举接线（3 处 switch 补 case）+ 新大类策略（委托运行监控查询）+ 新配置值对象 + 新解析策略。
