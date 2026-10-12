# 监测Dashboard排油烟需求时间

## Goal

监测的 Dashboard 页面（排油烟风机卡片 + 服务空间浮层）通过新增后端接口获取两个维度的"需求时间"：

- **排油烟风机的需求时间**：该风机所有服务空间需求时间的合并结果（展示在风机卡片"运行时段"位置）；
- **服务空间的需求时间**：各服务空间自身的需求时间（展示在服务空间浮层每行"需求时段"位置）。

数据来源为需求管理"厨房排油烟"需求配置中的"排油烟需求时间"（`exhaustTimeIntervals`，承接 UYAI-1556 已交付能力）。

用户价值：排油烟监测页面真实展示风机与空间的排油烟需求时段（合并去重、有序），替代未合并的原始数据展示。

## Background

### 页面场景（用户提供配图）

- 图1：按楼层分组的"排油烟风机"卡片列表，卡片副行展示"运行时段：07:30-12:00，07:30-12:00"（当前**重复未合并**）；
- 图2：点击"N 个服务空间"展开浮层，每个空间行展示"需求时段：07:30-12:00，07:30-12:00"（同样未合并）。

### 前端协作口径（用户与前端确认）

- **单独新加一个接口**（非改造现有 Dashboard 接口）；
- 前端来传"**空间和空间关联的设备**"；
- **服务空间关系（Eq2Sp）由前端传入**，后端不查询设备-空间关系。

### 现有实现证据

- 需求配置模型：`DemandConfigKitchenRangeHoodValueObject#exhaustTimeIntervals`（`common/.../structure/demand/model/valueobject/kitchen/DemandConfigKitchenRangeHoodValueObject.java#L35-L47`）；
- 配置查询：`DemandConfigGateway#listConfigsByInstances`（`DemandConfigByInstanceBatchQueryParam`，`queryInheritConfig=true` 默认含字段级继承）；
- 模板查询：`BaseDemandTemplateParam#queryAndGetTemplate()`（templateType + date）；
- 时段结构：`DemandTimeIntervalValueObject`（beginTime/endTime/acrossDay；`analyzeTimeInterval(MINUTE, sunRiseAndSet)` 解析，跨天自动拆为两段）；
- 日出日落数据：`WeatherClientService#getSunRiseAndSetCache(projectId, date)`；
- **预计算不适用**：KITCHEN_RANGE_HOOD 未接入预计算（`SUPPORTED_DEMAND_TYPES` 不含），必须走实时配置查询（`listConfigsByInstances`），不能查结果表；
- 现有 controller：`KitChenRangeHoodController`（`/kitchenRangeHood`，common 模块）。

## Requirements

- **R1 新接口**：新增独立接口 `POST /kitchenRangeHood/queryDemandTime`；入参 = `spaces` 数组（**space 与 equipment 共用同一 DTO**：`id`、`relEquipment` 关联设备）；出参 = **原位回填**：需求时间（`demandTimes`）分别挂到 space 与 equipment 对象下。
- **R2 空间需求时间**：按空间ID查询空间级排油烟需求配置（含继承到楼层/建筑/项目；查询日期=当天；厨房排油烟需求无黑名单/屏蔽功能，不涉及该过滤）的 `exhaustTimeIntervals` 并解析为绝对时段。
- **R3 设备需求时间**：设备（风机）时间 = 其关联空间集合（由入参推导）的需求时间合并；后端不查 Eq2Sp 关系。
- **R4 合并规则**：多个时间段 / 多个服务空间汇总时执行"**去重 + 重叠归并**"（相接视为连续合并），结果**按开始时间升序**；跨天时段按解析行为拆段参与合并；含日出日落（SUNRISE/SUNSET）类型的时段按当天日出日落时间绝对化后参与，无日出日落数据时跳过该时段并记录日志。
- **R5 空值语义**：空间无配置或无时段 → 返回空数组；入参中出现过的所有空间ID、设备ID均出现在返回结构中（缺配置时为 `[]`）。
- **R6 纯查询增量**：无数据库结构变更、无配置写入、不改运行监控、不查 Eq2Sp、不接入预计算。

## 已决策记录

| 编号 | 决策点 | 结论 | 依据 |
| --- | --- | --- | --- |
| D1 | 接口形态 | 新加独立接口（前端传空间和设备） | 用户与前端确认 |
| D2 | 入参结构 | 空间 + 空间关联的设备（空间为纲，设备随空间传入） | 用户确认 |
| D3 | 服务空间关系 | 由前端传入，后端不查 Eq2Sp | 用户确认 |
| D4 | 合并口径 | 去重 + 重叠归并，按开始时间排序 | 用户确认 |
| D5 | 黑名单/屏蔽空间 | 不涉及（厨房排油烟需求无黑名单和屏蔽功能） | 用户确认（10-09 澄清） |

## Acceptance Criteria

- [ ] AC1 接口按入参原位回填：space 与 equipment（同一 DTO）的 `demandTimes` 均返回（合并排序后）；响应结构与入参对称、顺序一致。
- [ ] AC2 空间时间正确性：单条、多条、完全相同去重、重叠归并、包含吸收（含跨天拆段）均正确；空间未配置/无该字段 → `[]`。
- [ ] AC3 设备时间正确性：设备时间 = 其关联空间时间的并集合并；多个空间时段重叠/相同正确归并去重。
- [ ] AC4 空场景：空入参/空 id → 注解校验拦截；项目无排油烟模板 → 全部 `[]`；设备无关联空间 → `[]`。
- [ ] AC5 `common` 模块编译通过（JDK 8）；合并工具/服务单元测试通过。
- [ ] AC6（联调）前端页面对接正常：风机卡片与空间浮层展示合并后的需求时段【需环境验证】。

## Out of Scope

- 前端页面改动（后端提供接口契约，前端另行对接）。
- 黑名单/屏蔽空间过滤（厨房排油烟需求无此功能，不涉及）。
- 查询日期参数化（固定当天；后续如需支持指定日期再扩展）。
- Eq2Sp 服务空间关系查询（D3：前端传入）。
- 需求配置写入/下发、预计算接入、运行监控改动。

## Notes

- 技术设计见 `design.md`；执行计划见 `implement.md`。
- 核心实现路径：新接口（Controller）→ 新服务（模板查询 + 空间配置批量查询 + 时段解析合并）→ 新时间合并工具（排序+扫描归并，"HH:mm" 输出）。
