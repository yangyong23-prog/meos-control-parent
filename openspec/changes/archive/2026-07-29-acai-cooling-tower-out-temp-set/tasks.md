## 1. Excel 模板修改

- [x] 1.1 在 `【AIR_CONDITIONING_C_H_SOURCE-DETAIL】系统详情页模版.xlsx` 的 ACAI sheet `heatDissipationControlLRY` 分组中新增 `INFO_CODE_SET_EXTEND_CONFIG` 行：type=INFO_CODE_SET_EXTEND_CONFIG, configJson=`{"infoCode":"coolingTowerOutTempSet"}`, group=heatDissipationControlLRY, sort=1
- [x] 1.2 新增 `IOT_SETTING` 行：type=IOT_SETTING, infoCode=coolingTowerOutTempSet, group=heatDissipationControlLRY, sort=2, relateConfig=INFO_CODE_SET_EXTEND_CONFIG-coolWaterReturnTempSetConfig
- [x] 1.3 调整现有 `coolWaterReturnTempSet` 和 `coolWaterDetaTempSet` 行的 sort 值后移（3、4）

## 2. Java 代码修改

- [x] 2.1 在 `InfoCodeSetExtendConfigAnalyzeStrategy` 中新增常量 `AIR_CONDITIONING_C_H_SOURCE_DEFAULT_INFO_CODES`，包含 `coolingTowerOutTempSet`
- [x] 2.2 新增 `isAirConditioningCHSourceExtendConfigDefault` 方法，判断 `runningMonitorType == AIR_CONDITIONING_C_H_SOURCE` 且 `infoCode` 在常量集合中
- [x] 2.3 在 `createDefaultConfig` 方法的 `shouldCreateDefault` 判断链中增加 `isAirConditioningCHSourceExtendConfigDefault` 调用

## 3. 验证

- [x] 3.1 编译通过（`mvn compile` 或 IDE 编译无错误）
- [ ] 3.2 验证 ACAI 系统调用 `listInfoCodeConfigsByObjectId` 接口，无 DB 记录时返回默认 AUTO 配置的 `coolingTowerOutTempSet`
- [ ] 3.3 验证调用 `extendInfoCodeValueConfig` API 推送 ACAI 的 `coolingTowerOutTempSet` 配置，`ExtendSetting` 正确保存到 DB 并通过 `commandSend` 推送到 BDTP
