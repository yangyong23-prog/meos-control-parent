## Why

通风（VENTILATION）模块当前仅管理风机类设备（FFSCAF/ACVTSF/FFSCMF），且会主动剔除服务 SFT-01.05.00"挑空"空间的风机。PRD-290902 要求扩大管理范围：新增普通窗（CFCSWN）和排烟窗（CFCSEW）通过 MechInArch/Eq2Sp 关系纳入穹顶通风感知；同时取消原有 SFT-01.05.00 剔除规则，重新纳管服务挑空顶层空间的风机设备。

## What Changes

- **新增窗类设备纳入通风管理**：普通窗（CFCSWN）、排烟窗（CFCSEW）通过 MechInArch/Eq2Sp（设备所在空间）关系关联 SFT-01.05.00"挑空"及其下级类型的穹顶空间
- **移除 SFT-01.05.00 剔除规则**：原逻辑中通风风机（ACVTSF）、送风兼消防补风机（FFSCMF）服务 SFT-01.05.00 顶层空间时会被剔除，此规则废除
- **空间范围扩大**：空间范围 = 风机服务空间（MechForArch/Eq2Sp）∪ 窗所在穹顶空间（MechInArch/Eq2Sp）
- **缓存写入/读取适配**：`cache_object_rel` 新增 EQUIPMENT_IN_SPACE 关系类型存储；读取路径增加第二次关系查询并合并
- **首页统计适配**：`VentilationEquipStatisticsEnum` 新增窗类统计分组
- **设备类型分类**：`VentilationEquipTypeEnum` 新增 `NATURAL_VENTILATION`（自然通风）
- **状态计算由前端处理**：后端保证设备列表带 equipType + runStatus + relObjIds，空间卡片的"送风/排风/自然通风"状态由前端聚合

## Capabilities

### New Capabilities
- `ventilation-window-device`: 窗类设备（CFCSWN/CFCSEW）纳入穹顶通风感知范围，包括设备查询、缓存构建/读取、空间并集、设备类型分类、首页统计分组

### Modified Capabilities
- 无现有 spec 级别变更

## Impact

**后端代码影响**:
- `VentilationService` — ALL_EQUIPMENT_CLASS_CODES 常量扩展
- `LocalCacheConstant` — VENTILATION_OBJECT_CLASS 常量扩展
- `VentilationEquipTypeEnum` — 新增 NATURAL_VENTILATION 枚举
- `VentilationEquipStatisticsEnum` — 新增 CFCSWN_NATURAL、CFCSEW_NATURAL 枚举
- `RunningMonitorVentilationStrategy` — 设备查询、空间查询、缓存读取路径
- `CacheRwdVentilationStrategy` — 缓存构建写入路径
- `VentilationServiceImpl` — 设备查询、设备类型分类
- `AbstractVentilationIotService` — 首页设备分组

**不影响**:
- 运行日志（VentilationQueryObjStrategy 已包含窗类）
- 运行日志扩展
- 前端状态展示逻辑
- CacheObjectExtensionFieldConfig（窗类无扩展字段需求）
- RwdObjectBusinessFeildDTO（窗类自动通过 enableVentilationFunType）
