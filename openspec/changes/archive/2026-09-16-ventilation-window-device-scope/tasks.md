## 1. 常量与枚举

- [x] 1.1 `VentilationService.ALL_EQUIPMENT_CLASS_CODES` 追加 `DtpRwdClassCodeEnum.CFCSWN` 和 `DtpRwdClassCodeEnum.CFCSEW`
- [x] 1.2 `LocalCacheConstant.VENTILATION_OBJECT_CLASS` 追加 `"CFCSWN"` 和 `"CFCSEW"`
- [x] 1.3 `VentilationEquipTypeEnum` 新增 `NATURAL_VENTILATION("NATURAL_VENTILATION", "key_xxx")` 枚举值
- [x] 1.4 `VentilationEquipStatisticsEnum` 新增 `CFCSWN_NATURAL("CFCSWN_NATURAL", "CFCSWN", "key_xxx")` 和 `CFCSEW_NATURAL("CFCSEW_NATURAL", "CFCSEW", "key_xxx")` 枚举值

## 2. 查询策略核心改造

- [x] 2.1 `RunningMonitorVentilationStrategy.filtrationId()` 方法体改为 `return Collections.emptySet()`，原逻辑注释保留
- [x] 2.2 `RunningMonitorVentilationStrategy.getDtpRwdEquipmentDTOS()` 新增第三个并发分支：查询 CFCSWN+CFCSEW via EQUIPMENT_IN_SPACE 关系，空间范围限定 SFT-01.05.* 穹顶空间
- [x] 2.3 `RunningMonitorVentilationStrategy.listSpaces()` DOME_VENTILATION 分支：额外查询窗类设备 → `listSpaceInByEquipmentId` → 与风机服务空间取并集去重

## 3. 缓存构建/读取改造

- [x] 3.1 `CacheRwdVentilationStrategy.buildCacheData()` equipmentWithColumns 增加 CFCSWN/CFCSEW 的 `listRwdQueryFields()` 并集
- [x] 3.2 `CacheRwdVentilationStrategy.buildCacheData()` 空间过滤逻辑调整：servedSpaceIds 同时保留有风机服务（relObjIds）和窗所在的空间
- [x] 3.3 `CacheRwdVentilationStrategy.buildCacheData()` relationsByType 新增 `EQUIPMENT_IN_SPACE` → 窗类设备列表（需从总设备列表中按 classCode 分离风机和窗类）
- [x] 3.4 `RunningMonitorVentilationStrategy.getVentilationDtpRwdSpaceVOSFromNewCache()` 设备 withColumns 增加 CFCSWN/CFCSEW 扩展字段白名单
- [x] 3.5 `RunningMonitorVentilationStrategy.getVentilationDtpRwdSpaceVOSFromNewCache()` 新增第二次 `listReverse` 查询（graphCode=MechInArch, relCode=Eq2Sp），结果与现有 EQUIPMENT_SERVICE_SPACE 合并

## 4. 业务逻辑适配

- [x] 4.1 `VentilationServiceImpl.equipType()` 新增 CFCSWN/CFCSEW → `NATURAL_VENTILATION` 映射（在 ACVTSF/FFSCAF 判断之后、else 之前）
- [x] 4.2 `VentilationServiceImpl.queryEquipments()` 增加窗类查询路径：通过 `listEquipmentInBySpaceIds(spaceIds, windowParam)` 查询 MechInArch/Eq2Sp，与风机结果按 ID 合并去重
- [x] 4.3 `VentilationServiceImpl.getVentilationDtpRwdSpaceVOS()` 旧路径中的设备查询同步适配（调用 `getDtpRwdEquipmentDTOS` 已包含窗类，确认空间过滤逻辑正确）
- [x] 4.4 `AbstractVentilationIotService.queryEquipGroupId()` 新增 CFCSWN → CFCSWN_NATURAL 分组和 CFCSEW → CFCSEW_NATURAL 分组

## 5. 编译与验证

- [x] 5.1 编译通过：`mvn compile -pl common -am -q`
- [x] 5.2 检查所有 `ALL_EQUIPMENT_CLASS_CODES` 和 `VENTILATION_OBJECT_CLASS` 消费者自动跟随
- [x] 5.3 确认 `VentilationQueryObjStrategy.classList` 已包含窗类（无需改动）
- [x] 5.4 确认 `RwdObjectBusinessFeildDTO.enableVentilationFunType()` 对窗类自动通过（无需改动）
