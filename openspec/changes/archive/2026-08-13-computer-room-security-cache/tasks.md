## 1. 缓存基础设施注册

- [x] 1.1 在 `CacheObjectModelKeyConfig` 中新增常量 `COMPUTER_ROOM_SECURITY_LIST_SPACE = "COMPUTER_ROOM_SECURITY_listSpace"` 和 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT = "COMPUTER_ROOM_SECURITY_listEquipment"`
- [x] 1.2 在 `CacheObjectModelStatusService.supportedRunningMonitorTypes` 中添加 `RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY`

## 2. 写入侧改造 — CacheRwdComputerRoomSecurityStrategy

- [x] 2.1 重构 `buildCacheData()`：保留原有旧缓存写入逻辑，在其后新增新缓存写入分支
- [x] 2.2 实现楼层扩展逻辑：从机房安全空间提取 floorId 集合，查询楼层全量空间
- [x] 2.3 设备查询 `withColumns` 新增 `monitoringSurveillanceStatus`、`bimId`、`bimLocation`（不含 `equipCriticalLevel`）
- [x] 2.4 调用 `replaceModelCacheKey()` 写入楼层全量空间到 `COMPUTER_ROOM_SECURITY_LIST_SPACE`
- [x] 2.5 调用 `replaceModelCacheKey()` 写入楼层全量设备到 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT`
- [x] 2.6 调用 `replaceObjectRelations()` 写入设备-空间关系到 `cache_object_rel`（relCode=`EQUIPMENT_SERVICE_SPACE`）
- [x] 2.7 移除写入侧 Knowledge Feign 调用（不再烘焙 classCodeName、equipCriticalLevel）
- [x] 2.8 实现 IoT 规则查询：按 classCode 分组设备 ID，调用 `abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes()` 查询 4 个信息点的实例化状态
- [x] 2.9 构建 `CacheRule` 集合，调用 `CacheRuleRepository.replaceRules(CRS_listEquipment, rules)` 写入规则缓存
- [x] 2.10 构建成功后调用 `markReady(COMPUTER_ROOM_SECURITY)`，异常时不调用

## 3. 读取侧改造 — RunningMonitorComputerRoomSecurityStrategy

- [x] 3.1 在 `listDashboardSpacesComputerRoomSecurity()` 入口处添加 `isReady(COMPUTER_ROOM_SECURITY)` 判断分支
- [x] 3.2 实现新缓存读取方法：从 `CacheObjectRepository.listByCacheKey()` 读取空间和设备，从 `CacheObjectRelRepository` 读取设备-空间关系
- [x] 3.3 实现 roomFuncType 过滤：调用 `getRoomFuncTypes()` 从楼层全量空间中筛出机房安全空间
- [x] 3.4 实现白名单过滤：调用 `getWhitelistSpaceIds()` 按三态逻辑过滤空间
- [x] 3.5 实现 excludeClassCodes 过滤：调用 `getFilterExcludeClassCodes()` 从设备列表中移除排除类型
- [x] 3.6 实现 Knowledge 数据实时填充：调用 `mapClassCodeAndName()`、`mapClassCodeTypeAndTag()` 填充设备名称和等级，填充 roomFuncTypeName
- [x] 3.7 实现空间-设备组装：按空间分组设备，构建 `ComputerRoomDashboardSpaceVO` 返回
- [x] 3.8 确保 `isReady()=false` 时走当前实时查询链路（fallback）

## 4. 读取侧改造 — InstanceServiceImpl（API 2）

- [x] 4.1 在 `listEquipmentsInBySpaceIdsComputerRoomSecurity()` 入口处添加 `isReady(COMPUTER_ROOM_SECURITY)` 判断分支
- [x] 4.2 实现新缓存读取：从 `CacheObjectRelRepository` 按 `rel_object_id = spaceId` 查设备 ID 集合
- [x] 4.3 从 `CacheObjectRepository` 按设备 ID 查设备数据（`DtpRwdEquipmentDTO`）
- [x] 4.4 从 `CacheRuleRepository.mapInfoCodesByObjectIds()` 查可运行状态，赋值 operableRunning
- [x] 4.5 执行受控过滤（CFCSDR/CFCSFR/SPEGFG 移除 !isControlled）
- [x] 4.6 DTO → VO 转换（`dtpRwdConvert::toEquipmentVo`）
- [x] 4.7 Knowledge 实时填充：调用 `mapClassCodeAndName()` 填充 classCodeName，调用 `mapTypeCodeAndName()` 填充 majorName
- [x] 4.8 确保 `isReady()=false` 时走当前实时查询链路（fallback）

## 5. 读取侧改造 — RunningMonitorComputerRoomSecurityStrategy（API 3）

- [x] 5.1 在 `listEquipmentsFromRwdFlor()` 入口处添加 `isReady(COMPUTER_ROOM_SECURITY)` 判断分支
- [x] 5.2 实现新缓存空间定位：从 `CRS_LIST_SPACE` 缓存空间中按 `floorId` + `classCode=SPACE` 内存过滤出 spaceIds
- [x] 5.3 实现新缓存设备读取：从 `CacheObjectRelRepository` 按 spaceIds 查设备 ID，从 `CacheObjectRepository` 取设备数据
- [x] 5.4 实现 excludeClassCodes 过滤：调用 `getFilterExcludeClassCodes()` 从设备列表中移除排除类型
- [x] 5.5 DTO → VO 转换（`computerRoomConverter::toEquipmentVo`）
- [x] 5.6 Knowledge 实时填充：调用 `mapClassCodeAndName()` 填充 classCodeName，调用 `mapClassCodeTypeAndTag()` 填充 equipCriticalLevel
- [x] 5.7 确保 `isReady()=false` 时走当前实时查询链路（fallback）

## 6. 读取侧改造 — RunningMonitorComputerRoomSecurityStrategy（API 4 异常事件过滤）

- [x] 6.1 在 `exceptionFilterConditionNew()` 闭包构建时添加 `isReady(COMPUTER_ROOM_SECURITY)` 判断，缓存就绪时走新路径
- [x] 6.2 实现新缓存读取：调用 `CacheObjectRepository.listByObjectIds(equipmentIds, {id, classCode, monitoringSurveillanceStatus})` 一次查询替代 2 次 BDTP Feign
- [x] 6.3 实现门禁受控过滤：从缓存结果中按 `classCode ∈ {CFCSDR, CFCSFR, SPEGFG}` 过滤门禁设备，通过 `isControlled()` 判断受控状态，构建 `doorIdAndControlled` Map
- [x] 6.4 实现排除类型过滤：从缓存结果中按 `classCode ∈ filterExcludeClassCodes` 过滤排除设备 ID，执行 `events.removeIf()`
- [x] 6.5 确保 `isReady()=false` 时走当前 BDTP 实时查询链路（fallback）

## 7. 读取侧改造 — DangerEventServiceImpl（API 5 风险事件）

- [x] 7.1 在 `listByDangerObject()` 的 COMPUTER_ROOM_SECURITY 分支入口处添加 `isReady(COMPUTER_ROOM_SECURITY)` 判断
- [x] 7.2 实现新缓存路径：将 `dtpRwdClientService.mapObjectIdAndLocalName(objectIds)` 替换为 `CacheObjectRepository.listByObjectIds(objectIds, {id, localName})`，从结果构建 `Map<String, String> objectIdAndLocalName`
- [x] 7.3 实现 3 路并行：将缓存查询与 2 次 ExceptionEvent Feign 改为 3 路 `CompletableFuture.runAsync()` 并行执行，`CompletableFuture.allOf().join()` 等待全部完成
- [x] 7.4 确保 `isReady()=false` 时走当前实时查询链路（fallback）

## 8. DashboardGridChartServiceImpl 清理

- [x] 8.1 移除 `querySpaceComputerRoomSecurity()` 中的 `meosCache=true` 设置（如尚未移除）


## 9. 依赖注入

- [x] 9.1 `CacheRwdComputerRoomSecurityStrategy` 注入 `CacheObjectModelWriteService`、`CacheObjectModelStatusService`、`AbstractIotDataCacheServer`、`CacheRuleRepository`、`CacheIotRuleRepository`
- [x] 9.2 `RunningMonitorComputerRoomSecurityStrategy` 注入 `CacheObjectRepository`、`CacheObjectRelRepository`、`CacheObjectModelStatusService`
- [x] 9.3 `InstanceServiceImpl` 注入 `CacheObjectRepository`、`CacheObjectRelRepository`、`CacheRuleRepository`、`CacheObjectModelStatusService`
- [x] 9.4 `DangerEventServiceImpl` 注入 `CacheObjectRepository`、`CacheObjectModelStatusService`

## 10. 验证与测试

- [ ] 10.1 验证缓存构建后 `isReady()` 返回 `true`，构建失败时返回 `false`
- [ ] 10.2 验证五个 API 新缓存路径返回数据与实时查询路径一致（空间数量、设备数量、字段完整性）
- [ ] 10.3 验证 API 1 新缓存路径无 BDTP Feign 调用
- [ ] 10.4 验证 API 2 新缓存路径无 BDTP Feign 和 IoT Feign 调用
- [ ] 10.5 验证 API 3 新缓存路径无 BDTP Feign 调用
- [ ] 10.6 验证 API 4 新缓存路径无 BDTP Feign 调用
- [ ] 10.7 验证 API 5 新缓存路径无 BDTP Feign 调用
- [ ] 10.8 验证白名单三态逻辑（null/空集/非空）均正确生效
- [ ] 10.9 验证 excludeClassCodes 过滤正确移除指定类型设备
- [ ] 10.10 验证 `cache_rule` 写入正确，`operableRunning` 判断与实时查询结果一致
- [ ] 10.11 验证 `bimId` 和 `bimLocation` 在缓存读取后正确返回给 API 3
- [ ] 10.12 验证 API 4 门禁受控过滤（`isControlled()`）与 BDTP 实时查询结果一致
- [ ] 10.13 验证 API 5 的 `objectIdAndLocalName` 缓存结果与 BDTP `mapObjectIdAndLocalName` 一致
- [ ] 10.14 验证 API 5 的 3 路并行执行正确性（风险事件、实例树、名称映射结果均完整）
