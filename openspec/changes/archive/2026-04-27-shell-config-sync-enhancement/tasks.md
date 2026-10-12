## 1. 扩展Feign接口层

- [x] 1.1 在 `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/DtpTwinClientService.java` 中新增 `saveStartIntervals(List<JSONObject> paramList)` 接口方法
- [x] 1.2 在 `common/src/main/java/com/persagy/apccontrol/basic/outside/feign/service/DtpTwinClientService.java` 中新增 `reviseObjectInSys(JSONObject param)` 接口方法
- [x] 1.3 在 `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/feign/DtpTwinClient.java` 中新增 `POST /twc/conf/saveStartIntervals` Feign方法
- [x] 1.4 在 `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/feign/DtpTwinClient.java` 中新增 `POST /twc/revise/reviseObjectInSys` Feign方法
- [x] 1.5 在 `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/twin/DtpTwinClientServiceImpl.java` 中实现 `saveStartIntervals` 方法（调用Feign客户端）
- [x] 1.6 在 `local/meos-control-common-edge/src/main/java/com/persagy/apccontrol/common/outside/twin/DtpTwinClientServiceImpl.java` 中实现 `reviseObjectInSys` 方法（调用Feign客户端）
- [x] 1.7 在 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/outside/DtpTwinClientServiceImpl.java` 中新增 `saveStartIntervals` 空实现（抛异常）
- [x] 1.8 在 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/outside/DtpTwinClientServiceImpl.java` 中新增 `reviseObjectInSys` 空实现（抛异常）

## 2. 实现冷机间隔开启限制规则同步

- [x] 2.1 在 `InnerSchdule.java` 中新增 `saveStartIntervals()` 私有方法
- [x] 2.2 实现ACAC（空调冷源）系统查询逻辑（查询 `DtpRwdClassCodeEnum.ACAC_SYSTEM` 系统列表）
- [x] 2.3 实现ACCC（冷源）系统查询逻辑（查询 `DtpRwdClassCodeEnum.COLD_SYSTEM` 系统列表）
- [x] 2.4 实现壳配置值获取逻辑（调用 `abstractCommonServer.getShellConfig()` 获取 `ALG_AIR_COOLING_START_INTERVAL_RULE` 和 `ALG_COOLING_START_INTERVAL_RULE`）
- [x] 2.5 实现JSON解析逻辑（解析壳配置值数组，提取 `selected` 和 `interval` 字段）
- [x] 2.6 实现BDTP参数组装逻辑（组装 `{objectId, paramCode: "equipStartInterval", paramArray: [{objectIds, interval}]}`）
- [x] 2.7 实现空值处理逻辑（配置值为空时，传 `paramArray: []`）
- [x] 2.8 调用 `dtpTwinClientService.saveStartIntervals()` 完成数据推送
- [x] 2.9 添加异常捕获和日志记录（ERROR级别，包含异常堆栈）

## 3. 实现屏蔽所有控制指令同步

- [x] 3.1 在 `InnerSchdule.java` 中新增 `saveShieldControl()` 私有方法
- [x] 3.2 实现ACAC（空调冷源）系统查询逻辑
- [x] 3.3 实现ACCC（冷源）系统查询逻辑
- [x] 3.4 实现壳配置值获取逻辑（获取 `ALG_AIR_COOLING_SHIELD_ALL_ENABLED` 和 `ALG_COOLING_SHIELD_ALL_ENABLED`）
- [x] 3.5 实现配置值转换逻辑（"true" → revise=1, "false" → revise=0）
- [x] 3.6 实现BDTP参数组装逻辑（组装 `{objectId, revise}`）
- [x] 3.7 实现空值处理逻辑（配置值为空时，跳过推送）
- [x] 3.8 调用 `dtpTwinClientService.reviseObjectInSys()` 完成数据推送
- [x] 3.9 添加异常捕获和日志记录
- [x] 3.10 添加配置值格式校验（非标准值时记录WARN日志并跳过）

## 4. 实现主机开启台数限制规则同步

- [x] 4.1 在 `InnerSchdule.java` 中新增 `saveHostUnitLimit()` 私有方法
- [x] 4.2 实现ACAI（空调冷热源）系统查询逻辑（查询 `DtpRwdClassCodeEnum.ACAI_SYSTEM` 系统列表）
- [x] 4.3 实现壳配置值获取逻辑（获取 `ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE`）
- [x] 4.4 实现JSON解析逻辑（解析壳配置值数组，提取 `selected` 和 `countLimit` 字段）
- [x] 4.5 实现BDTP参数组装逻辑（组装 `{objectId, paramCode: "equipStartInterval", paramArray: [{objectIds, maxRuns}]}`）
- [x] 4.6 实现空值处理逻辑（配置值为空时，传 `paramArray: []`）
- [x] 4.7 调用 `dtpTwinClientService.saveExclusions()` 完成数据推送
- [x] 4.8 添加异常捕获和日志记录

## 5. 集成定时任务

- [x] 5.1 在 `InnerSchdule.synShellConfigToTwin()` 方法中新增 `saveStartIntervals()` 调用
- [x] 5.2 在 `InnerSchdule.synShellConfigToTwin()` 方法中新增 `saveShieldControl()` 调用
- [x] 5.3 在 `InnerSchdule.synShellConfigToTwin()` 方法中新增 `saveHostUnitLimit()` 调用
- [x] 5.4 确认任务执行顺序合理（现有任务不受影响）

## 6. 代码验证和测试

- [x] 6.1 编译验证（确保所有模块编译通过，无语法错误）
- [x] 6.2 检查现有同步任务代码风格一致性（日志格式、异常处理、变量命名）
- [x] 6.3 验证Feign接口路径和参数格式与PRD一致
- [ ] 6.4 本地启动边缘端服务，观察定时任务日志输出（确认新方法被调用）
- [ ] 6.5 验证空值场景处理正确（配置为空时的推送行为）
