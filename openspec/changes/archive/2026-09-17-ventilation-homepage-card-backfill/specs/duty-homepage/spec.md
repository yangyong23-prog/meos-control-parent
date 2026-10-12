## ADDED Requirements

### Requirement: 通风值班首页小卡片按入参分组补齐

通风值班首页 `execute` SHALL 保证小卡片集合恒等于入参分组集合：在调用 `buildChildCardDateVO` 之前，以 `queryMap` 为基线补齐 `subStat` 中缺失的分组。补齐的分组 SHALL 设置 `total` 为该组入参设备数量（`objectIds.size()`），其余统计字段保持 0。

补齐 SHALL 仅遍历 `queryMap`（分组数动态，随项目设备构成变化），SHALL NOT 枚举固定设备类型或 `VentilationEquipStatisticsEnum.values()`。`subStat` 为 null 时 SHALL 以空 Map 起步。SHALL NOT 改动 `buildChildCardDateVO`、`HomePageCardIotDataCloud`、`HomePageCardIotDataEdge`、`DutyHomePageGroupDTO` 的现有逻辑。

#### Scenario: DTP 返回部分分组

- **WHEN** 聚合接口仅返回 `queryMap` 中部分分组的统计（如查询 N 组仅返回 M 组）
- **THEN** 小卡片列表 SHALL 包含全部 N 组；未返回的 N-M 组卡片 `equipmentTotal` = 该组入参设备数
- **AND** 这些补齐卡片的 `runningTotal`、`interveneTotal`、`lessOpenTotal`、`moreOpenTotal` SHALL 均为 0

#### Scenario: DTP 返回空 Map

- **WHEN** 聚合接口返回空 Map
- **THEN** 小卡片列表 SHALL 包含全部 N 组，每张卡片 `equipmentTotal` = 该组入参设备数，其余统计为 0

#### Scenario: DTP 返回全量分组

- **WHEN** 聚合接口返回 `queryMap` 中全部分组的统计
- **THEN** 小卡片列表 SHALL 与补齐前一致，SHALL NOT 出现重复卡片

#### Scenario: subStat 为 null

- **WHEN** `subStat` 为 null（边端理论路径）
- **THEN** SHALL 以空 Map 起步补齐并输出全部 N 组卡片，SHALL NOT 抛异常

#### Scenario: 分组数动态

- **WHEN** 项目实际查出的分组数为 N（可能包含窗类 CFCSWN/CFCSEW，也可能不含）
- **THEN** 卡片集合 SHALL 恒等于该 N 组，SHALL NOT 包含 `queryMap` 之外的分组

#### Scenario: 其它运行监测类型不受影响

- **WHEN** 照明/空调末端/厨房等运行监测类型执行各自的值班首页卡片构建
- **THEN** 其行为 SHALL 保持不变（本次不改动通用构建逻辑）
