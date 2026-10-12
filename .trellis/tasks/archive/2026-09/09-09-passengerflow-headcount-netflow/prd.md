# 客流出入口统计增加客流人数计算

## Goal

`POST /passengerFlow/getPassagewayHeadCount`（出入口页面统计，返回项目行 + 门组行，各含统计值与 24 小时图）在**统计值与逐小时图表点**上各新增「客流人数」展示值，计算逻辑统一为：进入 − 离开；进入或离开任一侧无数据时，客流人数无结果（前端展示 `--`）。

## Requirements

### R1: 统计行新增客流人数（项目 + 门组）

- 项目统计：客流人数 = 累计进入人次（accmInPeopleFlow）− 累计离开人次（accmOutPeopleFlow）；任意一个没有数据结果为 `--`。
- 门组统计：同上公式与缺失规则。
- 实现：`PassengerFlowHomeStatistics` 新增字段 `netPeopleFlow`，由 `iotToStatistics` 在组装 `inPeopleFlow`/`outPeopleFlow` 完成后计算；两侧均有值才计算差值，任一为 null 则保持 null（前端按 null 显示 `--`）。
- 既有字段 `inPeopleFlow`、`outPeopleFlow` 及其数据源（accm moment 值）、v7.260802.0 引入的规则过滤逻辑**均保持不变**。

### R2: 逐小时图表点新增客流人数

- 计算逻辑同 R1（差值公式），作用在每个小时点上。
- 实现：`TendencyFigure` 新增字段 `spNetPeopleFlow`，按**沿用 accd 同源逐点差值**口径（已与用户确认）：每小时点客流人数 = 该点 `spInPeopleFlow` − `spOutPeopleFlow`（accd 当日累计差分 HOUR 持久化值，图表数据源不变）；该小时点 in/out 任一为 null（无数据小时）则 `spNetPeopleFlow` 为 null（前端断点/`--`）。
- 24 点补齐逻辑、既有 `spInPeopleFlow`/`spOutPeopleFlow` 保持现状。

### R3: 缺失语义与命名（已与用户确认）

- 客流人数任一输入缺失时，后端返回 **null**（不返回 0、不返回 `--` 文本），数值字段保持 `Long` 类型。
- 字段命名：统计行 `netPeopleFlow`；图表点 `spNetPeopleFlow`（与既有 in/out 命名风格一致）。

## Acceptance Criteria

- [ ] `PassengerFlowHomeStatistics` 响应含 `netPeopleFlow`：项目行与门组行均返回 进入−离开 差值（两侧均有值）
- [ ] in/out 任一侧无数据（null）时 `netPeopleFlow` 为 null（不回 0）
- [ ] `TendencyFigure` 每小时点含 `spNetPeopleFlow`：该点 in/out 均有值时为差值；任一侧为 null 时为 null
- [ ] 图表数据源仍为 accd HOUR 持久化，未引入新查询、未改变既有字段值
- [ ] 接口签名、既有字段、规则过滤、24 点补齐行为零变化（纯增量字段，前后端兼容）
- [ ] SSE 复用方（PASSENGER_FLOW_HEAD_TOTAL 等）不受影响
- [ ] saas 云端与 local 边缘共用 common 代码，双端行为一致；`mvn compile` 通过

## Notes

- 涉及信息点：`accmInPeopleFlow`/`accmOutPeopleFlow`（累计进出人次）、`accdInPeopleFlow`/`accdOutPeopleFlow`（当日累计差分人次，HOUR 持久化）。两者均为 IOT_PERSISTENCE 类型，本任务不触碰规则检查与取值链路。
- 本任务不改图表数据源口径（已确认沿用 accd），仅做同源逐点差值。
- 前端“客流人数”标签文案与 `--` 展示为前端职责，后端以 null 表达缺失。
