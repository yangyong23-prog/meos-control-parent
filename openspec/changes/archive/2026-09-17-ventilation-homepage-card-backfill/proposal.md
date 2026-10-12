## Why

值班首页（`/duty/homePage?VENTILATION`）的通风小卡片分组由 `queryEquipGroupId()` 从项目实际设备动态生成（N 组，可能包含窗类 CFCSWN/CFCSEW），但云端聚合接口 `dtpPersistServerClient.homepageFanEquipmentStatusStatistics` 对「无统计数据的分组」不返回对应 key（实测：查询 6 组仅返回 3 组），导致这些分组的小卡片直接消失（窗类、FFSCAF 等卡片缺失）。

更严重的是返回空 Map（`{}`）时：`subStat` 为空且非 null，绕过了 `buildNullData` 既有兜底，一张卡片都不会展示。

需求方结论：无统计数据的分组也要展示卡片，`equipmentTotal` 取入参设备数量，其余统计保持 0。

## What Changes

- 在 `VentilationHomePageCardDataHandler.execute()` 中、调用 `buildChildCardDateVO` 之前，以入参 `queryMap` 为基线补齐 `subStat`：
  - DTP 未返回（或整体为空/为 null）的分组补 `new DutyHomePageDTO()`，并设置 `total = 该组 objectIds.size()`（其余字段默认 0）
- 补齐基线完全动态：N = 当前项目实际查出的分组数，不硬编码组数、不枚举 `VentilationEquipStatisticsEnum.values()`
- 不改动通用逻辑：`AbstractHomePageCardDataHandler.buildChildCardDateVO`、`HomePageCardIotDataCloud`、`HomePageCardIotDataEdge`、`DutyHomePageGroupDTO` 均保持原样；照明/空调末端/厨房等其它运行监测类型行为零影响
- 云（saas）与边缘端（local）部署同时生效（handler 位于 common 模块共用）

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `duty-homepage`: 新增「通风值班首页小卡片按入参分组补齐」需求——小卡片集合恒等于入参动态分组集合，缺统计分组以 `equipmentTotal`=入参设备数、其余 0 展示

## Impact

- **common 模块**：`VentilationHomePageCardDataHandler.java` 一处新增补齐逻辑（+ `DutyHomePageDTO` import）
- **saas / local 模块**：零改动（适配器与 DTO 不动）
- **接口契约**：`childCardList` 恒包含全部入参分组；缺统计卡片的 `runningTotal/interveneTotal/lessOpenTotal/moreOpenTotal` 为 0；`cardTag.errorEventTotal` 不受影响
- **行为变化**：DTP 返回空 Map 时从「0 张卡」变为「N 张卡」——正是需求要求
- **同类隐患（本次范围外）**：照明/空调末端/厨房走同一收口逻辑，存在同类缺卡可能，如产品确认可另开变更
