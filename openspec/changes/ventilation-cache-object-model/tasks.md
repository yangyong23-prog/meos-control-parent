## 1. 缓存键配置与启用范围

- [x] 1.1 在 `CacheObjectModelKeyConfig` 中新增 `VENTILATION_LIST_SPACE = "VENTILATION_listSpace"` 和 `VENTILATION_LIST_EQUIPMENT = "VENTILATION_listEquipment"` 两个常量
- [x] 1.2 在 `CacheObjectModelStatusServiceImpl.SUPPORTED_RUNNING_MONITOR_TYPES` 中加入 `VENTILATION`
- [x] 1.3 验证 `InfoCodeTemplateTypeEnum.allowCache()` 已包含 VENTILATION（确认无需改动）

## 2. 缓存写入重构

- [x] 2.1 在 `CacheRwdVentilationStrategy` 中注入 `CacheObjectModelWriteService`
- [x] 2.2 重写 `buildCacheData()` 方法：查询原始 `DtpRwdSpaceDTO`（1 Feign）和 `DtpRwdEquipmentDTO`（3 Feign，含 filtrationId 过滤），不再调用 `cacheDashboardSpaceData()` 和 `homePageListListEquipCacheData()`
- [x] 2.3 调用 `cacheObjectModelWriteService.replaceModelCacheKey(VENTILATION_LIST_SPACE, spaces, [], [])` 写入空间
- [x] 2.4 调用 `cacheObjectModelWriteService.replaceModelCacheKey(VENTILATION_LIST_EQUIPMENT, equipments, [EQUIPMENT_SERVICE_SPACE relQuery], [])` 写入设备（含关系）
- [x] 2.5 确认 `cacheDashboardSpaceData()` 旧写入逻辑保留不动（后续可独立废除）

## 3. 空间读取路径重构

- [x] 3.1 在 `RunningMonitorVentilationStrategy` 中注入 `CacheObjectModelStatusService`、`CacheObjectReadService`、`CacheObjectRelRepository`
- [x] 3.2 重写 `listDashboardSpaces()`：READY 时通过 `cacheObjectReadService.listByCacheKey()` 读取空间和设备 DTO
- [x] 3.3 通过 `cacheObjectRelRepository.listReverse()` 按空间 ID 反查设备→空间关系
- [x] 3.4 调用 `getStringMapMap()` 和 `getAirSeasonType()` 获取需求配置和空调季
- [x] 3.5 在内存中组装 `VentilationDtpRwdSpaceVO`（复用 `putCfg()` 逻辑）
- [x] 3.6 结果由 `redisCacheTemplate.asyncRefreshTemplate()` 短缓存兜底
- [x] 3.7 未 READY 时 fallback 到旧路径（`cacheRwdDomainService.getByCustomIndexKey()`）

## 4. 设备读取路径重构

- [x] 4.1 在 `AbstractVentilationIotService` 中注入 `CacheObjectModelStatusService` 和 `CacheObjectReadService`
- [x] 4.2 重写 `listEquipments(boolean isUseCache)`：READY 且 isUseCache 时通过 `cacheObjectReadService.listByCacheKey(VENTILATION_LIST_EQUIPMENT, DtpRwdEquipmentDTO.class, null)` 读取
- [x] 4.3 未 READY 时 fallback 到旧路径（`cacheRwdDomainService.getByCustomIndexKey()`）
- [x] 4.4 验证 `queryEquipGroupId()` 消费方式不变（getClassCode, getId, getUndefinedProperties.get("funcType")）

## 5. 验证与测试

- [x] 5.1 编译通过，无语法错误
- [x] 5.2 验证字段投影：ACVTSF funcType 能正确写入和恢复
- [x] 5.3 验证字段投影：FFSCAF/FFSCMF 无扩展字段丢失
- [x] 5.4 验证字段投影：DtpRwdSpaceDTO 所有字段完整
- [x] 5.5 验证 READY 状态时序：空间缓存→设备缓存→模板数据→markReady
- [x] 5.6 验证 fallback 路径：未 READY 时行为与切换前一致
- [x] 5.7 验证信息点配置自动切换：READY 后 `shouldUseNewModel()` 返回 true
- [x] 5.8 验证 `duty/homePage?VENTILATION` 接口返回正确设备分组
- [x] 5.9 验证 `listDashboardSpaces` 返回完整的 `VentilationDtpRwdSpaceVO`（含 relObjects、tdbQuality 等）
