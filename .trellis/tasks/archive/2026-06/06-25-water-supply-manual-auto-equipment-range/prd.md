# 生活热水一键手自动设备范围

## Goal

为系统设置中的生活热水一键手动/一键自动功能补充设备范围查询逻辑，使前端调用
`/instance/listEquipments/WATER_SUPPLY` 时能够返回当前系统实例下可切换手自动的设备。

## Requirements

- 查询入口：`POST /instance/listEquipments/WATER_SUPPLY`。
- 主要参数：
  - `filterEquipmentInSystemIds` 指定当前系统实例 ID。
  - `filterInfoCodeRules` 包含 `manualAutoSet`，用于保留已实例化“手自动状态设定”的设备。
  - `excludeInfoCodeValues.localRemoteStatus = "0"`，用于排除“本地远程状态=本地”的设备。
  - `withColumns` 至少支持 `id`、`classCode`、`classCodeName`、`localName` 等前端展示字段。
- 设备范围必须是当前系统实例下的设备对象，即 RWD 系统到设备关系（MechSubset / Sy2Eq）。
- 返回结果供前端按“设备类-设备”层级选择设备。
- 一键手动业务依赖该范围选择设备；结束时间、备注、到期自动恢复等操作逻辑不在本次后端策略查询改动范围内。
- 一键自动业务依赖同一设备范围选择设备；二次确认文案与立即解除手动控制不在本次后端策略查询改动范围内。

## Acceptance Criteria

- [x] `RunningMonitorWaterSupplyStrategy#listEquipments` 不再返回空列表。
- [x] `RunningMonitorTypeEnum.WATER_SUPPLY` 分派到 `RunningMonitorWaterSupplyStrategy`，保证前端接口实际调用生活热水策略。
- [x] 生活热水设备查询会按 `filterEquipmentInSystemIds` 返回系统下设备。
- [x] 生活热水设备查询保留 `filterInfoCodeRules` 对 `manualAutoSet` 的实例化规则过滤。
- [x] 生活热水设备查询保留 `excludeInfoCodeValues.localRemoteStatus = "0"` 对本地设备的排除。
- [x] 返回设备按中文名称稳定排序，便于前端按设备类-设备展示。
- [x] 未查询到设备时返回空列表，不抛业务异常。

## Notes

- 参考实现：`RunningMonitorAirConditioningColdSourceStrategy#listEquipments`。
- 当前任务是单点策略补充，按轻量任务处理，PRD-only。
