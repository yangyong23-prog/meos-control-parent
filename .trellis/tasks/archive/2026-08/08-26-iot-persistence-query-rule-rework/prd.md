# IOT云端持久化数据查询规则改造

## Goal

按 PRD-260901 落地"云端持久化数据查询规则"：在壳配置中选择"查询距离当前最新的数据"时，前端加标识的接口改为按（当前-间隔 ~ 当前）查询且不传分精度，其余场景行为与现状完全一致。

用户价值：解决 IOT 信息点按同一时刻点 5min 精度取值导致"最新数据看不到"的问题，且影响范围收窄到前端加标识的接口，改动小、回归可控。

## Requirements

### R1 三条件门控生效规则

- 前端请求头 `removeDefaultIpType=true`（值必须为 true）+ 壳配置"持久化数据查询逻辑"="查询距离当前最新的数据" + 项目级壳配置隔离，三条件同时满足时生效
- 生效行为：使用默认分精度（硬编码 "15min"）的查询 → 时间范围=（当前-间隔 ~ 当前），**不传分精度**
- 其余组合（无标识 / 标识非 true / 壳配置=同一时刻点默认值）按现有逻辑执行，与现状完全一致
- 无 Nacos 配置项、无 default-ip-type 配置项

### R2 关键原则：只改默认值

- 显式指定分精度的查询（DAY、1d、5min、1d 等）是产品功能需求，保持指定不变
- 本次改造不触碰任何显式分精度调用点

### R3 壳配置新增分组（枚举已由同事完成，直接复用）

- 同事已完成的枚举定义：
  - `ShellConfigSceneEnum.SCENE_PERSISTENT_DATA_RULE("persistent_data_rule")`（修改后即时生效）
  - `ShellConfigGroupEnum.GROUP_PERSISTENT_DATA_RULE("group_persistent_data_rule")`
  - `ShellConfigItemEnum.PERSISTENT_DATA_QUERY_LOGIC`（code `persistent_data_query_logic`，默认 `same_time`，可选 `latest`）
  - `ShellConfigItemEnum.PERSISTENT_DATA_TIME_INTERVAL`（code `persistent_data_time_interval`，默认 1h，范围 1-168）
  - `BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE` 映射已建
- 后端待做：读取封装（参照 CacheRwdServiceImpl L478-501 模式，缺失回退默认值；无缓存直查，保证"修改后即时生效"；请求头标识短路，无标识不读壳配置）

### R4 后端改造点（已三轮核对定稿）

- 底层 2 个查询方法增加判断：
  - `DtpRwdClientServiceImpl.queryIpDataOnMoment(groupCode, projectId, objectIds, infoCodes)` L1156（硬编码 "15min" 版）
  - `DtpRwdClientServiceImpl.queryIpDataOnMomentInOneHour(objectIds, infoCodes)` L1191（硬编码 "15min" + (-1h~now) 版）
- 其余约 136 处调用点无需改动（统一走底层 2 个方法）
- SSE 实时推送：前端行为不变（订阅请求头加标识），后端订阅建立时（HTTP 线程内）检测请求头，有标识则转存到 `SseSubscribeParam` 新增字段，推送线程按"订阅参数标识 + 壳配置"判断精度
- 控制台（dtp-meos-server）：控制服务自行改造，不在本次后端范围

### R5 涉及接口（前端已梳理，后端三轮核对）

- 去重后 44 个：✓ 涉及 17 个（需加请求头标识），✗ 不涉及 27 个
- 详细清单及调用链证据见 `.trellis/tasks/08-13-iot-persistence-query-analysis/analysis/design-review.md` 3.3
- ✓ 涉及的 17 个中，`listRunningStrategy` ×3 为显式 5min 分精度，加了标识行为也不变

### R6 影响范围（产品限定）

- 冷源、冷热源、热源、空调冷源、空调热源、空调冷热源（**空调系列暂不改动**）
- 非接口链路不受影响：空间分析任务 ×2、运行分析报表、值班冷源卡片（显式 DAY）、边缘端
- 页面展示配合：最新数据模式下系统图、空间三维模型不再显示"更新时间"

## Acceptance Criteria

- [ ] 请求头无标识：任意壳配置下行为与改造前一致
- [ ] 请求头 `=true` + 壳配置"同一时刻点"（默认）：行为与改造前一致
- [ ] 请求头 `=true` + 壳配置"最新数据" + 间隔=1h：按（当前-1h ~ 当前）查询，不传分精度
- [ ] 请求头 `=true` + 壳配置"最新数据" + 间隔=168h：按（当前-168h ~ 当前）查询，不传分精度
- [ ] 显式指定分精度的查询（DAY、1d、5min）：保持指定分精度不变
- [ ] 最新数据模式下时间范围内无新数据：页面显示 --
- [ ] SSE 推送：订阅请求头有标识 + 壳配置最新数据 → 推送查询不传分精度；否则与改造前一致
- [ ] 边缘端、历史数据/报表、定时任务不受影响

## Out of Scope

- 控制服务（dtp-meos-server）改造（由其自行改造）
- 生活给水、气源系统、光伏系统
- 空调系列（暂不改动）：本次前端不给空调系列接口加请求头标识，后端无需任何模块开关逻辑；后续迭代启用时前端再加标识即可
- 显式分精度调用点的任何改动

## Notes

- 全部调用链证据来自 08-13-iot-persistence-query-analysis 任务的 7 份分析文档，经八轮核验 + 三轮接口核对
- 请求头名称已确定：`removeDefaultIpType`（值必须为 true）
- 壳配置枚举已由同事完成（`persistent_data_query_logic` 默认 same_time / 可选 latest；`persistent_data_time_interval` 默认 1h 范围 1-168）；后端只需读取封装
