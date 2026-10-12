## 1. 枚举注册

- [x] 1.1 在 `DtpRwdClassCodeEnum` 中新增 `WSDHHE("WSDHHE", "生活热水换热器")` 枚举值
- [x] 1.2 在 `DtpRwdSystemClassCodeEnum` 中新增 WSDH 系统类编码枚举值（参照 ACAC 模式，添加系统和系统映射两个值）
- [x] 1.3 在 `NowRunningLogCategory` 中新增一级分类 `StatusAndControl_WS("StatusAndControl_WS", "WSDH", null, "key_statusandcontrol_ws", 1, "#root.systemName", "#root.systemId", 12)`
- [x] 1.4 在 `NowRunningLogCategory` 中新增二级分类 `StatusAndControl_WS_WSDHHE` 和 `StatusAndControl_WS_WSDHHP`
- [x] 1.5 在 `NowRunningLogCategory` 的 static 块中：将 `StatusAndControl_WS` 添加到 `systemCategoryByClassCodeMap`；在 `newLoggingSystem()` 中添加 `StatusAndControl_WS`

## 2. 配置注册

- [x] 2.1 在 `RunningLogTemplateConfig.twinClassCodeInfoCodes` 中注册 WSDHHE 的 7 个信息点：`Arrays.asList("enableCtrl", "highVaporValveOpeningSet", "highVaporValveSwitchSet", "highValveOpeningSet", "highValveSwitchSet", "lowValveOpeningSet", "lowValveSwitchSet")`
- [x] 2.2 在 `RunningLogTemplateConfig.twinClassCodeInfoCodes` 中注册 WSDHHP 的 2 个信息点：`Arrays.asList("equipSwitchSet", "equipFreqSet")`
- [x] 2.3 在 `LocalCacheConstant.OBJECT_SYSTEM_CLASS` 集合中添加 `"WSDHHE"` 和 `"WSDHHP"`

## 3. 策略类实现

- [x] 3.1 新增 `WSQueryObjStrategy` 类，继承 `CategoryQueryObjStrategyAbstract`，标注 `@Service`，classList 为 `ImmutableSet.of("WSDHHE", "WSDHHP")`
- [x] 3.2 实现 `queryObj` 方法：调用 `querySystemObj(objId, DtpRwdSystemClassCodeEnum.WATER_SUPPLY_HOT_SYSTEM, classCode, NowRunningLogCategory.StatusAndControl_WS)`，标注 TODO 等待第三方实现

## 4. i18n 国际化

- [x] 4.1 在 i18n 资源文件中添加 key `key_statusandcontrol_ws`（生活热水）
- [x] 4.2 在 i18n 资源文件中添加 key `key_statusandcontrol_ws_wsdhhe`（生活热水换热器）
- [x] 4.3 在 i18n 资源文件中添加 key `key_statusandcontrol_ws_wsdhhp`（生活热水水泵）

## 5. 编译验证

- [x] 5.1 执行 Maven 编译，确认无编译错误
