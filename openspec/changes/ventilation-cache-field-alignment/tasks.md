## 1. 写入列对齐

- [x] 1.1 `CacheRwdVentilationStrategy.buildCacheData()` 空间查询列改为 `CacheObjectExtensionFieldConfig.listRwdQueryFields(DtpRwdClassCodeEnum.SPACE.value())` 基础上追加 `name`（保留现有业务列，不缩水）
- [x] 1.2 设备查询列改为 FFSCMF、FFSCAF、ACVTSF 三类 `listRwdQueryFields()` 并集，追加 `name`、`relObjIds`
- [x] 1.3 确认对齐后空间列包含 `bimId`、`bimLocation`、`outline`，设备列包含 `bimId`、`bimLocation`、`funcType`、`monitoringSurveillanceStatus`
- [x] 1.4 确认 `listEquipmentsForCacheBuild(spaceIds, equipmentWithColumns)` 签名与调用方式不变

## 2. 验证与测试

- [x] 2.1 编译通过，无语法错误
- [ ] 2.2 触发一次 VENTILATION 缓存重建，查询 `cache_object_model_view` 中 `VENTILATION_listSpace` 对象的 JSON，确认 `bimLocation`、`outline` 非空（RWD 有值的空间）
- [ ] 2.3 查询 `VENTILATION_listEquipment` 对象 JSON，确认 ACVTSF/FFSCAF 的 `bimId` 落库
- [ ] 2.4 READY 状态下调用 `listDashboardSpaces`，对比返回 VO 的 `bimLocation`、`outline` 与旧路径一致
- [x] 2.5 验证 `queryEquipGroupId()` 消费方式不变（getClassCode、getId、getUndefinedProperties.get("funcType")）
- [ ] 2.6 验证 `duty/homePage?VENTILATION` 接口设备分组不受影响
