## ADDED Requirements

### Requirement: VENTILATION 新缓存对象模型写入

系统 SHALL 将 VENTILATION 的空间数据和设备数据写入新缓存对象模型表（`cache_object`、`cache_object_model_view`、`cache_object_key_rel`），不再写入旧表 `cache_rwd_object`。写入时存储原始 `DtpRwdSpaceDTO` 和 `DtpRwdEquipmentDTO`，不存储复合 `VentilationDtpRwdSpaceVO`。设备→空间关系 SHALL 通过 `replaceModelCacheKey` 的 `relationQueryParams` 参数写入 `cache_object_rel` 表。

#### Scenario: 缓存构建写入新表

- **WHEN** XXL-JOB 或 DTX 事件触发 VENTILATION 缓存构建
- **THEN** 系统 SHALL 查询原始 `DtpRwdSpaceDTO`（1 Feign）和 `DtpRwdEquipmentDTO`（3 Feign，含 filtrationId 过滤）
- **AND** 通过 `cacheObjectModelWriteService.replaceModelCacheKey()` 分别写入空间键和设备键
- **AND** 设备写入时 SHALL 传入 `EQUIPMENT_SERVICE_SPACE` 关系查询参数
- **AND** 总 Feign 调用 SHALL 为 4 次（不含需求配置和空调季查询）

#### Scenario: 空间缓存存储原始 DTO

- **WHEN** 写入空间缓存数据
- **THEN** 系统 SHALL 存储 `DtpRwdSpaceDTO` 而非 `VentilationDtpRwdSpaceVO`
- **AND** SPACE classCode 白名单字段（roomFuncType, bimId, bimLocation, outline）SHALL 写入 objectData JSON
- **AND** 公共字段（id, classCode, localName, floorId, buildingId）SHALL 写入独立列

#### Scenario: 设备缓存字段投影安全

- **WHEN** 写入 ACVTSF 类型设备
- **THEN** funcType SHALL 通过 ACVTSF 白名单写入 objectData JSON
- **AND** 读取时 funcType SHALL 恢复到 `DtpRwdEquipmentDTO.undefinedProperties`
- **AND** `queryEquipGroupId()` 的消费方式（getClassCode, getId, getUndefinedProperties.get("funcType")）SHALL 不变

### Requirement: VENTILATION READY 状态启用

系统 SHALL 将 VENTILATION 加入 `CacheObjectModelStatusServiceImpl.SUPPORTED_RUNNING_MONITOR_TYPES` 集合。READY 状态在缓存构建和模板写入完成后设置，保证读取时数据已就绪。

#### Scenario: isSupported 返回 true

- **WHEN** 调用 `cacheObjectModelStatusService.isSupported(RunningMonitorTypeEnum.VENTILATION)`
- **THEN** 系统 SHALL 返回 `true`

#### Scenario: READY 时序保证

- **WHEN** VENTILATION 缓存构建执行
- **THEN** 系统 SHALL 先写入空间缓存、设备缓存和模板数据
- **AND** 最后调用 `markReady(VENTILATION)` 设置 READY 状态
- **AND** READY 后 `isReady(VENTILATION)` SHALL 返回 true

### Requirement: VENTILATION 缓存键配置

系统 SHALL 在 `CacheObjectModelKeyConfig` 中新增 `VENTILATION_LIST_SPACE` 和 `VENTILATION_LIST_EQUIPMENT` 两个缓存键常量，命名规则与末端一致。

#### Scenario: 缓存键定义

- **WHEN** VENTILATION 缓存写入或读取
- **THEN** 系统 SHALL 使用 `CacheObjectModelKeyConfig.VENTILATION_LIST_SPACE`（值为 `"VENTILATION_listSpace"`）作为空间缓存键
- **AND** SHALL 使用 `CacheObjectModelKeyConfig.VENTILATION_LIST_EQUIPMENT`（值为 `"VENTILATION_listEquipment"`）作为设备缓存键

### Requirement: VENTILATION 空间读取路径切换

`RunningMonitorVentilationStrategy.listDashboardSpaces()` SHALL 在 READY 状态下从新缓存读取原始空间和设备 DTO，通过 `cache_object_rel` 表读取设备→空间关系，再实时查询需求配置和空调季，最后在内存中组装 `VentilationDtpRwdSpaceVO`。未 READY 时 SHALL fallback 到旧路径。

#### Scenario: READY 时读取新缓存

- **WHEN** `isReady(VENTILATION)` 返回 true
- **THEN** 系统 SHALL 通过 `cacheObjectReadService.listByCacheKey()` 读取空间和设备
- **AND** SHALL 通过 `cacheObjectRelRepository.listReverse()` 读取设备→空间关系
- **AND** SHALL 调用 `getStringMapMap()` 和 `getAirSeasonType()` 获取需求配置和空调季
- **AND** SHALL 在内存中组装 `VentilationDtpRwdSpaceVO`
- **AND** 结果 SHALL 由 `redisCacheTemplate.asyncRefreshTemplate()` 短缓存兜底

#### Scenario: 未 READY 时 fallback 旧路径

- **WHEN** `isReady(VENTILATION)` 返回 false
- **THEN** 系统 SHALL fallback 到旧路径（`cacheRwdDomainService.getByCustomIndexKey()`）
- **AND** 旧路径行为 SHALL 与切换前完全一致

### Requirement: VENTILATION 设备读取路径切换

`AbstractVentilationIotService.listEquipments(boolean)` SHALL 在 READY 状态下从新缓存读取 `DtpRwdEquipmentDTO`，未 READY 时 fallback 到旧路径。

#### Scenario: READY 时读取设备新缓存

- **WHEN** `isReady(VENTILATION)` 返回 true 且 `isUseCache` 为 true
- **THEN** 系统 SHALL 通过 `cacheObjectReadService.listByCacheKey(VENTILATION_LIST_EQUIPMENT, DtpRwdEquipmentDTO.class, null)` 读取设备
- **AND** 返回的 `DtpRwdEquipmentDTO` 列表 SHALL 可被 `queryEquipGroupId()` 正确消费

#### Scenario: 未 READY 时 fallback 旧路径

- **WHEN** `isReady(VENTILATION)` 返回 false
- **THEN** 系统 SHALL fallback 到旧路径（`cacheRwdDomainService.getByCustomIndexKey()`）

### Requirement: VENTILATION 信息点配置自动切换

`InfoCodeConfigServiceImpl.shouldUseNewModel()` 和 `InfoCodeGatewayImpl.mapTemplateTypeAndObjects()` SHALL 在 VENTILATION READY 后自动走新缓存路径，无需额外改动。

#### Scenario: 信息点配置查询自动切换

- **WHEN** `isReady(VENTILATION)` 返回 true
- **AND** `InfoCodeTemplateTypeEnum.allowCache(VENTILATION, templateType)` 返回 true
- **THEN** `shouldUseNewModel()` SHALL 返回 true
- **AND** 信息点配置查询 SHALL 从新缓存读取

#### Scenario: 模板重建自动切换

- **WHEN** `isSupported(VENTILATION)` 返回 true
- **THEN** `CacheTemplateRebuildService.prepareTemplate()` SHALL 走新路径写入模板数据到新表
