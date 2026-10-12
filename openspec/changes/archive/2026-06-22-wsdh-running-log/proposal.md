## Why

运行日志系统当前已覆盖冷热源、空调末端、照明、通风、安防、污水处理等 10 个模块，但缺少「生活热水」模块的日志记录与查询能力。PRD 要求本次新增生活热水模块的运行控制类日志，且为完全新增功能，不兼容旧版日志体系。

## What Changes

- 新增生活热水一级分类 `StatusAndControl_WS`（系统级，类比 ACAC），classCode 为 `WSDH`
- 新增生活热水两个二级分类：WSDHHE（生活热水换热器）、WSDHHP（生活热水水泵）
- 注册 WSDHHE 的 7 个孪生自控信息点（enableCtrl、highVaporValveOpeningSet、highVaporValveSwitchSet、highValveOpeningSet、highValveSwitchSet、lowValveOpeningSet、lowValveSwitchSet）
- 注册 WSDHHP 的 2 个孪生自控信息点（equipSwitchSet、equipFreqSet）
- 新增 WSQueryObjStrategy 分类对象查询策略（占位实现，待第三方接入）
- 在 `LocalCacheConstant.OBJECT_SYSTEM_CLASS` 中注册 WSDHHE/WSDHHP，使系统关系查询（EQ_IN_SYSTEM）能正确路由到 WSDH 系统
- 在 `DtpRwdClassCodeEnum` 中新增 WSDHHE 枚举值
- 在 `DtpRwdSystemClassCodeEnum` 中新增 WSDH 枚举值
- 日志模板完全复用现有模板一~五、七、八，无需新增模板
- 日志来源类型无需新增，完全复用现有 COMMAND/SCHEDULE/INTERVENTION/CHANGE_INTERVENTION_TIME/TWIN_COMMAND

## Capabilities

### New Capabilities
- `wsdh-running-log`: 生活热水模块运行日志的完整生命周期——分类注册、孪生自控信息点注册、系统关系路由、分类对象查询策略、日志记录与查询

### Modified Capabilities
<!-- 无现有 spec 的需求变更 -->

## Impact

- **枚举注册**：`NowRunningLogCategory`、`DtpRwdClassCodeEnum`、`DtpRwdSystemClassCodeEnum`、`LocalCacheConstant`
- **配置注册**：`RunningLogTemplateConfig.twinClassCodeInfoCodes`
- **新增策略类**：`WSQueryObjStrategy`（占位）
- **i18n 资源**：新增分类名称国际化 key
- **自动承接的查询能力**：新版日志分页查询、二级分类查询、分类树、设备列表筛选、SSE 播报——均基于 firstLevelCategory/secondLevelCategory 数据驱动，无需额外改动
- **明确不受影响**：`PushIOTEventListener`、`RunningLogHandle`、`RunningLogEdgeNowServiceImpl`、`RunningLogSourceTypeEnum`、`RunningLogKeyWordEnum`、`RunningLogCategoryEnum`（旧版）、`RunningLogTemplateEnum`、照明模板（九~十二）
