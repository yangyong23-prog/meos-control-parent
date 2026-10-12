## ADDED Requirements

### Requirement: 新缓存写入查询列与白名单对齐

新缓存对象模型写入策略在查询 RWD 对象时，withColumns SHALL 基于 `CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode)` 组装（多 classCode 时取并集），业务追加列仅允许在此基础上增加，SHALL NOT 手写与白名单脱节的完整列集合。

#### Scenario: VENTILATION 空间写入列对齐

- **WHEN** `CacheRwdVentilationStrategy.buildCacheData()` 查询空间 `DtpRwdSpaceDTO`
- **THEN** withColumns SHALL 包含 `listRwdQueryFields(SPACE)` 的全部字段
- **AND** SHALL 包含 `bimId`、`bimLocation`、`outline`
- **AND** 缓存重建后 `VENTILATION_listSpace` 键下空间对象的 objectData JSON SHALL 含有非空的 `bimLocation`、`outline`（RWD 中存在值时）

#### Scenario: VENTILATION 设备写入列对齐

- **WHEN** `CacheRwdVentilationStrategy.buildCacheData()` 查询设备 `DtpRwdEquipmentDTO`
- **THEN** withColumns SHALL 包含 FFSCMF、FFSCAF、ACVTSF 三类 `listRwdQueryFields()` 结果的并集
- **AND** SHALL 包含 `bimId`
- **AND** ACVTSF 的 `funcType` 与既有消费方式（`getUndefinedProperties().get("funcType")`）SHALL 不变

#### Scenario: READY 后空间读取字段完整

- **WHEN** `isReady(VENTILATION)` 返回 true 且缓存已按对齐后列集合重建
- **THEN** `listDashboardSpaces` 返回的 `VentilationDtpRwdSpaceVO` SHALL 包含 `bimLocation`、`outline` 字段值
- **AND** 字段值 SHALL 与旧路径（`cacheDashboardSpaceData` 时代）返回一致

#### Scenario: 白名单演进自动跟随

- **WHEN** `CacheObjectExtensionFieldConfig` 中 SPACE 或通风设备类的白名单新增字段
- **THEN** VENTILATION 写入查询列 SHALL 自动包含新增字段，无需修改写入策略代码
