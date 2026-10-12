### Requirement: 窗类设备纳入通风设备范围
通风模块的设备范围常量（`ALL_EQUIPMENT_CLASS_CODES`、`VENTILATION_OBJECT_CLASS`）SHALL 包含普通窗（CFCSWN）和排烟窗（CFCSEW）。

#### Scenario: 设备常量包含窗类
- **WHEN** 查询 `VentilationService.ALL_EQUIPMENT_CLASS_CODES` 或 `LocalCacheConstant.VENTILATION_OBJECT_CLASS`
- **THEN** 集合 SHALL 包含 `CFCSWN` 和 `CFCSEW`

#### Scenario: 窗类设备被识别为通风系统
- **WHEN** 设备 classCode 为 CFCSWN 或 CFCSEW 且 `RwdObjectBusinessFeildDTO.wirteObjectClassBelongModelType()` 被调用
- **THEN** 设备的 systemType SHALL 被设置为 VENTILATION

### Requirement: 废除 SFT-01.05.00 剔除规则
原 `filtrationId()` 方法中查询 SFT-01.05.00 顶层空间并排除服务该空间的 ACVTSF/FFSCMF 设备的逻辑 SHALL 被废除。方法签名保留，返回值 SHALL 为空集。

#### Scenario: 服务挑空空间的风机不再被剔除
- **WHEN** ACVTSF（funcType=1 或 2）或 FFSCMF 通过 MechForArch/Eq2Sp 服务于 SFT-01.05.00 顶层空间
- **THEN** 该设备 SHALL 保留在通风设备范围内，不被 filtrationId 过滤

#### Scenario: filtrationId 返回空集
- **WHEN** `RunningMonitorVentilationStrategy.filtrationId()` 被调用
- **THEN** SHALL 返回 `Collections.emptySet()`
- **AND** 原逻辑代码 SHALL 以注释形式保留

### Requirement: 窗类设备通过 EQUIPMENT_IN_SPACE 关系查询
窗类设备（CFCSWN/CFCSEW）SHALL 通过 MechInArch/Eq2Sp（设备所在空间）关系查询，与风机类设备的 MechForArch/Eq2Sp（设备服务于空间）关系并行。

#### Scenario: 窗类设备查询使用 EQUIPMENT_IN_SPACE
- **WHEN** `getDtpRwdEquipmentDTOS()` 查询窗类设备
- **THEN** SHALL 使用 `DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE` 关系
- **AND** classCode 限定为 `{CFCSWN, CFCSEW}`
- **AND** 空间范围限定为 `roomFuncType LIKE_RIGHT "SFT-01.05."` 的穹顶空间

#### Scenario: 窗类设备与风机设备并行查询
- **WHEN** `getDtpRwdEquipmentDTOS()` 被调用
- **THEN** 窗类设备查询 SHALL 作为独立并发分支与风机类查询并行执行
- **AND** 结果 SHALL 合并到同一个设备列表

### Requirement: 穹顶通风空间范围取并集
`listSpaces(DOME_VENTILATION)` 返回的空间范围 SHALL 为风机服务空间与窗所在穹顶空间的并集（按空间 ID 去重）。

#### Scenario: 穹顶空间包含窗关联空间
- **WHEN** 调用 `listSpaces(DOME_VENTILATION, instance)` 且存在 CFCSWN/CFCSEW 通过 MechInArch/Eq2Sp 关联 SFT-01.05.* 穹顶空间
- **THEN** 返回结果 SHALL 包含这些穹顶空间
- **AND** SHALL 包含风机通过 MechForArch/Eq2Sp 服务的穹顶空间
- **AND** 结果 SHALL 按空间 ID 去重

#### Scenario: 非穹顶类型不受影响
- **WHEN** 调用 `listSpaces(LOGISTICS_VENTILATION/PARKING_VENTILATION/TOILET_VENTILATION, ...)`
- **THEN** 查询逻辑 SHALL 与变更前一致，不查询窗类设备

### Requirement: 缓存写入支持 EQUIPMENT_IN_SPACE 关系
`CacheRwdVentilationStrategy.buildCacheData()` SHALL 将窗类设备通过 `EQUIPMENT_IN_SPACE` 关系写入 `cache_object_rel` 表。

#### Scenario: 窗类关系写入缓存
- **WHEN** `buildCacheData(VENTILATION)` 执行
- **THEN** `relationsByType` SHALL 包含 `EQUIPMENT_IN_SPACE` → 窗类设备列表
- **AND** 风机类设备 SHALL 继续通过 `EQUIPMENT_SERVICE_SPACE` 写入
- **AND** 空间过滤 SHALL 同时保留有风机服务和窗所在的空间

#### Scenario: 窗类设备字段写入缓存
- **WHEN** `buildCacheData(VENTILATION)` 查询设备字段
- **THEN** `equipmentWithColumns` SHALL 包含 CFCSWN/CFCSEW 的 `listRwdQueryFields()` 并集

### Requirement: 缓存读取支持双关系类型
`getVentilationDtpRwdSpaceVOSFromNewCache()` SHALL 同时读取 `EQUIPMENT_SERVICE_SPACE` 和 `EQUIPMENT_IN_SPACE` 两种关系，合并到同一个空间→设备映射。

#### Scenario: 读取路径合并双关系
- **WHEN** `isReady(VENTILATION)` 为 true 且调用 `listDashboardSpaces`
- **THEN** SHALL 发起两次 `cacheObjectRelRepository.listReverse()` 查询
- **AND** 第一次查询 graphCode=`MechForArch`, relCode=`Eq2Sp`
- **AND** 第二次查询 graphCode=`MechInArch`, relCode=`Eq2Sp`
- **AND** 两次查询结果 SHALL 合并到同一个 `mapRelObjIdAndData`

#### Scenario: 空间 VO 的 relObjects 包含窗类设备
- **WHEN** 穹顶空间关联了窗类设备
- **THEN** `VentilationDtpRwdSpaceVO.relObjects` SHALL 包含这些窗类设备

### Requirement: 窗类设备类型分类
`VentilationServiceImpl.equipType()` SHALL 将 CFCSWN 和 CFCSEW 映射为 `NATURAL_VENTILATION` 类型。

#### Scenario: 窗类设备映射为自然通风
- **WHEN** `equipType()` 处理 classCode 为 CFCSWN 的设备
- **THEN** `equipType` SHALL 设置为 `VentilationEquipTypeEnum.NATURAL_VENTILATION`

#### Scenario: 排烟窗设备映射为自然通风
- **WHEN** `equipType()` 处理 classCode 为 CFCSEW 的设备
- **THEN** `equipType` SHALL 设置为 `VentilationEquipTypeEnum.NATURAL_VENTILATION`

### Requirement: 模型视图设备查询包含窗类
`VentilationServiceImpl.queryEquipments()` SHALL 同时查询风机类（MechForArch/Eq2Sp）和窗类（MechInArch/Eq2Sp）设备并合并去重。

#### Scenario: 模型视图设备列表包含窗类
- **WHEN** `queryEquipments(projectId, spaceIds, null)` 被调用且 spaceIds 包含穹顶空间
- **THEN** 返回结果 SHALL 包含通过 MechInArch/Eq2Sp 关联这些空间的 CFCSWN/CFCSEW 设备
- **AND** SHALL 包含通过 MechForArch/Eq2Sp 服务这些空间的风机设备
- **AND** 结果 SHALL 按设备 ID 去重

### Requirement: 首页统计支持窗类设备分组
`AbstractVentilationIotService.queryEquipGroupId()` SHALL 将 CFCSWN 分组到 `CFCSWN_NATURAL`，CFCSEW 分组到 `CFCSEW_NATURAL`。

#### Scenario: 窗类设备分组到首页统计
- **WHEN** `queryEquipGroupId()` 遍历设备列表
- **THEN** CFCSWN 设备 SHALL 被归入 `VentilationEquipStatisticsEnum.CFCSWN_NATURAL`
- **AND** CFCSEW 设备 SHALL 被归入 `VentilationEquipStatisticsEnum.CFCSEW_NATURAL`
- **AND** 风机类分组逻辑 SHALL 不变

### Requirement: 设备类型枚举扩展
`VentilationEquipTypeEnum` SHALL 新增 `NATURAL_VENTILATION` 枚举值；`VentilationEquipStatisticsEnum` SHALL 新增 `CFCSWN_NATURAL` 和 `CFCSEW_NATURAL` 枚举值。

#### Scenario: 自然通风设备类型枚举
- **WHEN** 引用 `VentilationEquipTypeEnum.NATURAL_VENTILATION`
- **THEN** value SHALL 为 `"NATURAL_VENTILATION"`

#### Scenario: 窗类统计枚举
- **WHEN** 引用 `VentilationEquipStatisticsEnum.CFCSWN_NATURAL`
- **THEN** value SHALL 为 `"CFCSWN_NATURAL"`，infoCode SHALL 为 `"CFCSWN"`

#### Scenario: 排烟窗统计枚举
- **WHEN** 引用 `VentilationEquipStatisticsEnum.CFCSEW_NATURAL`
- **THEN** value SHALL 为 `"CFCSEW_NATURAL"`，infoCode SHALL 为 `"CFCSEW"`
