## ADDED Requirements

### Requirement: WSDH 一级分类注册
系统 SHALL 在 `NowRunningLogCategory` 中注册生活热水一级分类 `StatusAndControl_WS`，classCode 为 `"WSDH"`，order 为 12，SpEL 表达式为 `#root.systemName` / `#root.systemId`。该分类 SHALL 被添加到 `systemCategoryByClassCodeMap` 和 `newLoggingSystem()` 返回列表中。

#### Scenario: 一级分类通过 classCode 查找
- **WHEN** 调用 `NowRunningLogCategory.systemCategoryByClassCodeMap.get("WSDH")`
- **THEN** 返回 `StatusAndControl_WS` 枚举实例

#### Scenario: 一级分类出现在新版日志系统列表
- **WHEN** 调用 `NowRunningLogCategory.newLoggingSystem()`
- **THEN** 返回列表包含 `StatusAndControl_WS`

### Requirement: WSDHHE 和 WSDHHP 二级分类注册
系统 SHALL 在 `NowRunningLogCategory` 中注册两个二级分类：`StatusAndControl_WS_WSDHHE`（classCode `"WSDHHE"`，parentCode `"StatusAndControl_WS"`）和 `StatusAndControl_WS_WSDHHP`（classCode `"WSDHHP"`，parentCode `"StatusAndControl_WS"`）。

#### Scenario: WSDHHE 二级分类路由
- **WHEN** 设备 classCode 为 `"WSDHHE"` 且一级分类为 `StatusAndControl_WS`
- **THEN** `buildSecondLevelCategory` 返回 `StatusAndControl_WS_WSDHHE`

#### Scenario: WSDHHP 二级分类路由
- **WHEN** 设备 classCode 为 `"WSDHHP"` 且一级分类为 `StatusAndControl_WS`
- **THEN** `buildSecondLevelCategory` 返回 `StatusAndControl_WS_WSDHHP`

### Requirement: WSDHHE 孪生自控信息点注册
系统 SHALL 在 `RunningLogTemplateConfig.twinClassCodeInfoCodes` 中注册 key `"WSDHHE"`，value 为 `["enableCtrl", "highVaporValveOpeningSet", "highVaporValveSwitchSet", "highValveOpeningSet", "highValveSwitchSet", "lowValveOpeningSet", "lowValveSwitchSet"]`。

#### Scenario: WSDHHE 设备孪生消息匹配
- **WHEN** 收到 classCode 为 `"WSDHHE"` 的孪生控制消息，infoCode 为 `"enableCtrl"`
- **THEN** `twinMsgLog` 方法匹配到该信息点并生成运行日志

#### Scenario: WSDHHE 设备未注册信息点被忽略
- **WHEN** 收到 classCode 为 `"WSDHHE"` 的孪生控制消息，infoCode 不在注册列表中
- **THEN** `twinMsgLog` 方法跳过该消息，不生成日志

### Requirement: WSDHHP 孪生自控信息点注册
系统 SHALL 在 `RunningLogTemplateConfig.twinClassCodeInfoCodes` 中注册 key `"WSDHHP"`，value 为 `["equipSwitchSet", "equipFreqSet"]`。

#### Scenario: WSDHHP 设备孪生消息匹配
- **WHEN** 收到 classCode 为 `"WSDHHP"` 的孪生控制消息，infoCode 为 `"equipFreqSet"`
- **THEN** `twinMsgLog` 方法匹配到该信息点并生成运行日志

### Requirement: WSDHHE/WSDHHP 设备类枚举注册
系统 SHALL 在 `DtpRwdClassCodeEnum` 中注册 `WSDHHE("WSDHHE", "生活热水换热器")`。WSDHHP 和 WSDHWT 已存在，无需修改。

#### Scenario: WSDHHE 枚举值查找
- **WHEN** 调用 `DtpRwdClassCodeEnum.valueOf("WSDHHE")`
- **THEN** 返回对应枚举实例，描述为 "生活热水换热器"

### Requirement: WSDH 系统类编码枚举注册
系统 SHALL 在 `DtpRwdSystemClassCodeEnum` 中注册 WSDH 系统类编码枚举值，参照 ACAC 模式添加系统和系统映射两个值。

#### Scenario: WSDH 系统类编码查找
- **WHEN** 通过 WSDH 系统编码查找 `DtpRwdSystemClassCodeEnum`
- **THEN** 返回对应枚举实例

### Requirement: OBJECT_SYSTEM_CLASS 包含 WSDHHE/WSDHHP
系统 SHALL 在 `LocalCacheConstant.OBJECT_SYSTEM_CLASS` 集合中添加 `"WSDHHE"` 和 `"WSDHHP"`，使 `getObjectBusinessFeildAndSystemInfo` 对生活热水设备能执行 EQ_IN_SYSTEM 关系查询。

#### Scenario: WSDHHE 设备系统关系查询
- **WHEN** 调用 `getObjectBusinessFeildAndSystemInfo` 处理 classCode 为 `"WSDHHE"` 的设备
- **THEN** 方法进入 `OBJECT_SYSTEM_CLASS` 分支，通过 EQ_IN_SYSTEM 查询到所属 WSDH 系统

#### Scenario: OBJECT_SYSTEM_CLASS 不包含 WSDHHE 导致查询失败
- **WHEN** `OBJECT_SYSTEM_CLASS` 不包含 `"WSDHHE"` 且设备无 `wirteObjectClassBelongModelType` 预设
- **THEN** `getObjectBusinessFeildAndSystemInfo` 返回 null

### Requirement: WSQueryObjStrategy 占位实现
系统 SHALL 新增 `WSQueryObjStrategy` 类，继承 `CategoryQueryObjStrategyAbstract`，标注 `@Service`，classList 为 `ImmutableSet.of("WSDHHE", "WSDHHP")`。`queryObj` 方法 SHALL 调用 `querySystemObj` 并标注 TODO 等待第三方实现。

#### Scenario: 策略自动注册到工厂
- **WHEN** Spring 容器启动
- **THEN** `RunningLogCategoryQueryObjFactory` 自动注入 `WSQueryObjStrategy`

#### Scenario: WSDHHE 设备查询路由
- **WHEN** 调用 `RunningLogCategoryQueryObjFactory.queryObj` 传入 classCode `"WSDHHE"`
- **THEN** 路由到 `WSQueryObjStrategy.queryObj` 方法

### Requirement: i18n 分类名称国际化
系统 SHALL 在 i18n 资源文件中添加以下 key：`key_statusandcontrol_ws`（生活热水）、`key_statusandcontrol_ws_wsdhhe`（生活热水换热器）、`key_statusandcontrol_ws_wsdhhp`（生活热水水泵）。

#### Scenario: 前端获取分类名称
- **WHEN** 前端请求日志分类树，locale 为中文
- **THEN** `StatusAndControl_WS` 显示为 "生活热水"，`StatusAndControl_WS_WSDHHE` 显示为 "生活热水换热器"，`StatusAndControl_WS_WSDHHP` 显示为 "生活热水水泵"
