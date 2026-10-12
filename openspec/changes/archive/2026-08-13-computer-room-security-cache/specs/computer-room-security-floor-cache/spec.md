## ADDED Requirements

### Requirement: 缓存基础设施注册

系统 SHALL 在 `CacheObjectModelKeyConfig` 中注册机房安全缓存键 `COMPUTER_ROOM_SECURITY_LIST_SPACE` 和 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT`，并在 `CacheObjectModelStatusService.supportedRunningMonitorTypes` 中添加 `COMPUTER_ROOM_SECURITY`。

#### Scenario: 缓存键注册

- **WHEN** 系统启动并加载 `CacheObjectModelKeyConfig`
- **THEN** SHALL 存在常量 `COMPUTER_ROOM_SECURITY_LIST_SPACE = "COMPUTER_ROOM_SECURITY_listSpace"` 和 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT = "COMPUTER_ROOM_SECURITY_listEquipment"`

#### Scenario: 缓存状态注册

- **WHEN** `CacheObjectModelStatusService` 初始化 `supportedRunningMonitorTypes`
- **THEN** SHALL 包含 `RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY`
- **AND** `isReady(COMPUTER_ROOM_SECURITY)` SHALL 返回 `markReady()` 后的状态

### Requirement: 楼层级空间缓存写入

系统 SHALL 在 `CacheRwdComputerRoomSecurityStrategy.buildCacheData()` 中构建楼层级空间缓存。先从 InfoCode 获取 roomFuncType 列表查询机房安全空间，再提取这些空间所在楼层 ID，查询每个楼层的全部空间，将楼层全量空间写入新缓存。

#### Scenario: 机房安全空间定位与楼层扩展

- **WHEN** `buildCacheData()` 执行
- **THEN** 系统 SHALL 通过 `getRoomFuncTypes()` 获取机房安全功能类型列表
- **AND** 通过 BDTP Feign 查询匹配 roomFuncType 的空间
- **AND** 从查询结果中提取所有不重复的 floorId
- **AND** 通过 BDTP Feign 查询这些 floorId 对应的全量空间

#### Scenario: 空间缓存写入新缓存表

- **WHEN** 楼层全量空间查询完成
- **THEN** 系统 SHALL 调用 `CacheObjectModelWriteService.replaceModelCacheKey()` 将空间列表写入 `COMPUTER_ROOM_SECURITY_LIST_SPACE` 缓存键
- **AND** 同时保留旧 `cache_rwd_object` 写入逻辑不删除

### Requirement: 楼层级设备缓存写入

系统 SHALL 查询楼层全量空间对应的设备，将设备数据和设备-空间关系写入新缓存。设备查询 `withColumns` SHALL 包含 `monitoringSurveillanceStatus`、`bimId`、`bimLocation`。

#### Scenario: 设备数据查询

- **WHEN** 楼层全量空间已获取
- **THEN** 系统 SHALL 通过 BDTP Feign 查询这些空间 ID 对应的全部设备
- **AND** `withColumns` SHALL 包含 `listSimpleWithColumns` 基础列、`relObjIds`、`realTimeStreamProtocol`、`monitoringSurveillanceStatus`、`bimId`、`bimLocation`
- **AND** `withColumns` SHALL NOT 包含 `equipCriticalLevel`（最终值来自 Knowledge，不缓存 BDTP 侧值）

#### Scenario: 设备缓存与关系写入

- **WHEN** 设备数据查询完成
- **THEN** 系统 SHALL 调用 `replaceModelCacheKey()` 将设备列表写入 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT` 缓存键
- **AND** SHALL 调用 `replaceObjectRelations()` 将设备-空间关系写入 `cache_object_rel` 表（relCode=`EQUIPMENT_SERVICE_SPACE`）

#### Scenario: monitoringSurveillanceStatus 存储

- **WHEN** 设备数据写入缓存
- **THEN** `monitoringSurveillanceStatus` 值 SHALL 存在于 `DtpRwdEquipmentDTO.undefinedProperties` 中
- **AND** `CacheObject` 表 SHALL 在 `monitoringSurveillanceStatus` 独立列中存储该值

### Requirement: 写入侧不含 Knowledge 调用

`buildCacheData()` SHALL NOT 调用任何 Knowledge 服务（`mapClassCodeAndName`、`mapClassCodeTypeAndTag`）。Knowledge 数据仅在读取侧实时获取。

#### Scenario: 写入侧无 Knowledge 依赖

- **WHEN** `buildCacheData()` 执行
- **THEN** 系统 SHALL NOT 调用 `dtpKnowledgeClientService.mapClassCodeAndName()`
- **AND** 系统 SHALL NOT 调用 `dtpKnowledgeClientService.mapClassCodeTypeAndTag()`

### Requirement: 缓存就绪标记

`buildCacheData()` 成功完成后 SHALL 调用 `markReady(COMPUTER_ROOM_SECURITY)` 标记缓存就绪。

#### Scenario: 构建成功后标记就绪

- **WHEN** 空间和设备的缓存写入全部成功
- **THEN** 系统 SHALL 调用 `CacheObjectModelStatusService.markReady(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY)`

#### Scenario: 构建失败不标记就绪

- **WHEN** 缓存写入过程中发生异常
- **THEN** 系统 SHALL NOT 调用 `markReady()`
- **AND** `isReady(COMPUTER_ROOM_SECURITY)` SHALL 保持返回 `false`

### Requirement: 读取侧新缓存分支

`RunningMonitorComputerRoomSecurityStrategy.listDashboardSpacesComputerRoomSecurity()` SHALL 在入口处判断 `isReady(COMPUTER_ROOM_SECURITY)`。为 `true` 时走新缓存读取路径，为 `false` 时 fallback 到当前实时查询链路。

#### Scenario: 缓存就绪走新缓存路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `true`
- **THEN** 系统 SHALL 从 `CacheObjectRepository.listByCacheKey()` 读取楼层全量空间和设备
- **AND** 从 `CacheObjectRelRepository` 读取设备-空间关系
- **AND** 执行运行时过滤和组装后返回

#### Scenario: 缓存未就绪走 fallback 路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `false`
- **THEN** 系统 SHALL 走当前 `listDashboardSpacesComputerRoomSecurity` 实时查询链路
- **AND** 不调用新缓存读取逻辑

### Requirement: 读取侧 roomFuncType 过滤

从新缓存读取楼层全量空间后，系统 SHALL 通过 InfoCode Feign 获取机房功能类型列表，从全量空间中筛出机房安全空间。

#### Scenario: 过滤出机房安全空间

- **WHEN** 楼层全量空间已从缓存读取
- **THEN** 系统 SHALL 调用 `getRoomFuncTypes()` 获取机房功能类型列表
- **AND** 从空间列表中过滤出 `roomFuncType` 在列表中的空间
- **AND** 保留不在列表中的空间用于后续楼层级 API 消费

#### Scenario: InfoCode 服务不可用

- **WHEN** `getRoomFuncTypes()` 调用失败
- **THEN** 系统 SHALL fallback 到实时查询链路

### Requirement: 读取侧白名单过滤

系统 SHALL 在 roomFuncType 过滤后执行白名单过滤。白名单来源于 DB `InstanceConfig` 查询，三态逻辑：null=不过滤、空集=返回空列表、非空=只保留白名单内空间。

#### Scenario: 白名单为 null（未配置）

- **WHEN** `getWhitelistSpaceIds()` 返回 `null`
- **THEN** 系统 SHALL 不做白名单过滤，保留全部空间

#### Scenario: 白名单为空集合（已配置但为空）

- **WHEN** `getWhitelistSpaceIds()` 返回空集合
- **THEN** 系统 SHALL 返回空列表

#### Scenario: 白名单非空

- **WHEN** `getWhitelistSpaceIds()` 返回非空白名单
- **THEN** 系统 SHALL 只保留 ID 在白名单中的空间

### Requirement: 读取侧 excludeClassCodes 过滤

系统 SHALL 通过 Knowledge Feign 获取排除设备类型列表（`getFilterExcludeClassCodes`），从设备列表中移除匹配的设备。

#### Scenario: 排除指定类型设备

- **WHEN** 设备列表已从缓存读取
- **THEN** 系统 SHALL 调用 `getFilterExcludeClassCodes()` 获取排除列表
- **AND** 从设备列表中移除 `classCode` 在排除列表中的设备

### Requirement: 读取侧 Knowledge 数据实时填充

系统 SHALL 在读取侧实时调用 `mapClassCodeAndName()` 和 `mapClassCodeTypeAndTag()` 获取设备名称和标签数据，填充到设备 DTO 中返回。

#### Scenario: 设备名称实时填充

- **WHEN** 设备列表过滤完成
- **THEN** 系统 SHALL 调用 `dtpKnowledgeClientService.mapClassCodeAndName()` 获取 classCode→名称映射
- **AND** 将名称数据填充到返回的设备对象中

#### Scenario: 设备等级实时填充

- **WHEN** 设备列表过滤完成
- **THEN** 系统 SHALL 调用 `dtpKnowledgeClientService.mapClassCodeTypeAndTag()` 获取设备标签数据
- **AND** 将 `equipCriticalLevel` 填充到返回的设备对象中

#### Scenario: roomFuncTypeName 实时填充

- **WHEN** 空间列表过滤完成
- **THEN** 系统 SHALL 调用 Knowledge Feign 获取 roomFuncType code→name 映射
- **AND** 将 `roomFuncTypeName` 填充到返回的空间对象中

### Requirement: 缓存读取零 BDTP Feign

新缓存读取路径 SHALL NOT 调用任何 BDTP Feign 接口查询空间或设备数据。

#### Scenario: 新缓存路径无 BDTP 调用

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 且走新缓存路径
- **THEN** 系统 SHALL NOT 调用 BDTP 空间查询接口
- **AND** 系统 SHALL NOT 调用 BDTP 设备查询接口
- **AND** 空间和设备数据 SHALL 全部来自本地 `cache_object` 和 `cache_object_rel` 表

### Requirement: IoT 规则缓存写入

系统 SHALL 在 `buildCacheData()` 中查询楼层全量设备的 IoT 规则实例化状态，写入 `cache_rule` / `cache_rule_key_rel` 表。

#### Scenario: 规则查询与写入

- **WHEN** 楼层全量设备已写入 `cache_object`
- **THEN** 系统 SHALL 按 classCode 分组设备 ID
- **AND** 调用 `abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes()` 查询 4 个信息点（RUN_STATUS、SWITCH_STATUS、ON_OFF_STATUS、DEPLOY_WITHDRAWALARM_STATUS）的实例化状态
- **AND** 为每个已实例化的 (objectId, infoCode) 构建 `CacheRule`
- **AND** 调用 `CacheRuleRepository.replaceRules(CRS_listEquipment, rules)` 写入缓存
- **AND** `CacheRule.type` SHALL 等于 `cacheIotRuleRepository.cacheIotRuleType()`

#### Scenario: 规则与设备生命周期一致

- **WHEN** 规则写入缓存
- **THEN** 规则缓存键 SHALL 复用 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT`
- **AND** 每次缓存构建时替换该缓存键下的全部规则，孤儿记录自动清理

### Requirement: API 2 空间设备列表缓存读取

`InstanceServiceImpl.listEquipmentsInBySpaceIdsComputerRoomSecurity(spaceId)` SHALL 在入口处判断 `isReady(COMPUTER_ROOM_SECURITY)`。为 `true` 时从缓存读取设备，为 `false` 时 fallback 到当前实时查询链路。

#### Scenario: 缓存就绪走新缓存路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `true`
- **THEN** 系统 SHALL 从 `CacheObjectRelRepository` 按 `rel_object_id = spaceId` 查出设备 ID 集合
- **AND** 从 `CacheObjectRepository` 按设备 ID 查设备数据
- **AND** 从 `CacheRuleRepository.mapInfoCodesByObjectIds()` 查可运行状态
- **AND** 执行受控过滤、DTO→VO 转换、Knowledge 实时填充后返回

#### Scenario: 缓存未就绪走 fallback 路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `false`
- **THEN** 系统 SHALL 走当前实时查询链路（BDTP Feign + IoT Feign + Knowledge Feign）

### Requirement: API 2 可运行状态本地读取

API 2 新缓存路径 SHALL 通过 `CacheRuleRepository.mapInfoCodesByObjectIds()` 本地查询判断设备可运行状态，0 IoT Feign。

#### Scenario: operableRunning 判断

- **WHEN** 设备列表已从缓存读取
- **THEN** 系统 SHALL 调用 `cacheRuleRepository.mapInfoCodesByObjectIds(deviceIds, [RUN_STATUS, SWITCH_STATUS, ON_OFF_STATUS, DEPLOY_WITHDRAWALARM_STATUS], type)`
- **AND** 如果返回 Map 中存在该 objectId 则 `operableRunning = true`，否则 `operableRunning = false`
- **AND** 系统 SHALL NOT 调用 IoT 规则查询接口

### Requirement: API 2 Knowledge 数据实时填充

API 2 新缓存路径 SHALL 实时调用 `mapClassCodeAndName()` 和 `mapTypeCodeAndName()` 获取设备名称和专业名称。

#### Scenario: classCodeName 实时填充

- **WHEN** 设备 VO 转换完成
- **THEN** 系统 SHALL 调用 `dtpKnowledgeClientService.mapClassCodeAndName()` 获取 classCode→名称映射
- **AND** 将 `classCodeName` 填充到设备 VO 中

#### Scenario: majorName 实时填充

- **WHEN** 设备 VO 转换完成
- **THEN** 系统 SHALL 调用 `dtpKnowledgeClientService.mapTypeCodeAndName()` 获取 typeCode→名称映射
- **AND** 将 `majorName` 填充到设备 VO 中

### Requirement: 写入侧设备 withColumns 含 bimId 和 bimLocation

设备缓存写入时 SHALL 包含 `bimId` 和 `bimLocation` 字段，以支持 API 3 楼层图前端显示。

#### Scenario: bimId 和 bimLocation 存入缓存

- **WHEN** 设备数据写入 `cache_object`
- **THEN** `bimId` 和 `bimLocation` 值 SHALL 存在于 `DtpRwdEquipmentDTO.undefinedProperties` 中
- **AND** 这些值来自 BDTP 查询结果（非 Knowledge 覆盖）

### Requirement: API 3 楼层设备列表缓存读取

`RunningMonitorComputerRoomSecurityStrategy.listEquipmentsFromRwdFlor(floorId)` SHALL 在入口处判断 `isReady(COMPUTER_ROOM_SECURITY)`。为 `true` 时从缓存读取设备，为 `false` 时 fallback 到当前实时查询链路。

#### Scenario: 缓存就绪走新缓存路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `true`
- **THEN** 系统 SHALL 从 `CRS_LIST_SPACE` 缓存空间中按 `floorId` + `classCode=SPACE` 过滤出 spaceIds
- **AND** 从 `CacheObjectRelRepository` 按 spaceIds 查设备 ID 集合
- **AND** 从 `CacheObjectRepository` 按设备 ID 查设备数据
- **AND** 执行 excludeClassCodes 过滤、DTO→VO 转换、Knowledge 实时填充后返回

#### Scenario: 缓存未就绪走 fallback 路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `false`
- **THEN** 系统 SHALL 走当前实时查询链路（BDTP Feign ×2 + Knowledge Feign ×3）

### Requirement: API 3 Knowledge 数据实时填充

API 3 新缓存路径 SHALL 实时调用 Knowledge Feign 获取设备名称和等级数据。

#### Scenario: classCodeName 和 equipCriticalLevel 实时填充

- **WHEN** 设备 VO 转换完成
- **THEN** 系统 SHALL 调用 `dtpKnowledgeClientService.mapClassCodeAndName()` 获取 classCode→名称映射
- **AND** 调用 `dtpKnowledgeClientService.mapClassCodeTypeAndTag()` 获取 equipCriticalLevel 标签值
- **AND** 将 `classCodeName` 和 `equipCriticalLevel` 填充到设备 VO 中

### Requirement: API 4 异常事件过滤缓存读取

`RunningMonitorComputerRoomSecurityStrategy.exceptionFilterConditionNew()` SHALL 在 `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 时，通过 `CacheObjectRepository.listByObjectIds()` 按设备 ID 批量查缓存数据替代 2 次 BDTP Feign。为 `false` 时 fallback 到当前 BDTP 实时查询链路。

#### Scenario: 缓存就绪走新缓存路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `true`
- **THEN** 系统 SHALL 调用 `CacheObjectRepository.listByObjectIds(equipmentIds, {id, classCode, monitoringSurveillanceStatus})` 查询缓存设备
- **AND** 从结果中按 `classCode ∈ {CFCSDR, CFCSFR, SPEGFG}` 过滤出门禁设备
- **AND** 通过 `DtpRwdObjectDTO.isControlled()` 判断门禁受控状态
- **AND** 系统 SHALL NOT 调用 `dtpRwdClientService.listObject()`
- **AND** 系统 SHALL NOT 调用 `dtpRwdClientService.listEquipment()`

#### Scenario: 排除类型设备从缓存过滤

- **WHEN** 缓存设备数据已通过 `listByObjectIds` 获取
- **THEN** 系统 SHALL 从结果中按 `classCode ∈ filterExcludeClassCodes` 过滤出排除类型设备 ID
- **AND** 将匹配的 ID 作为 `excludeEquipmentIds` 执行 `events.removeIf()`
- **AND** 系统 SHALL NOT 调用 BDTP 设备查询接口

#### Scenario: Knowledge Feign 保留

- **WHEN** 新缓存路径执行
- **THEN** 系统 SHALL 仍调用 `getFilterExcludeClassCodes()` 获取排除列表（Knowledge Feign ×2）
- **AND** 该调用与 D2 策略一致，不缓存 Knowledge 数据

#### Scenario: 缓存未就绪走 fallback 路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `false`
- **THEN** 系统 SHALL 走当前 BDTP 实时查询链路（listObject + listEquipment）
- **AND** 不调用新缓存读取逻辑

### Requirement: API 4 缓存读取零 BDTP Feign

API 4 新缓存路径 SHALL NOT 调用任何 BDTP Feign 接口。2 次 BDTP Feign 合并为 1 次本地 DB 查询。

#### Scenario: 新缓存路径无 BDTP 调用

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 且走新缓存路径
- **THEN** 系统 SHALL NOT 调用 `dtpRwdClientService.listObject()`
- **AND** 系统 SHALL NOT 调用 `dtpRwdClientService.listEquipment()`
- **AND** 设备数据 SHALL 全部来自本地 `cache_object` 表

### Requirement: API 5 风险事件缓存读取与 3 路并行

`DangerEventServiceImpl.listByDangerObject()` 的 COMPUTER_ROOM_SECURITY 分支 SHALL 在 `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 时，通过 `CacheObjectRepository.listByObjectIds(objectIds, {id, localName})` 替代 BDTP Feign `mapObjectIdAndLocalName()`，并将其与 2 次 ExceptionEvent Feign 改为 3 路 `CompletableFuture` 并行执行。为 `false` 时 fallback 到当前 BDTP 实时查询链路。

#### Scenario: 缓存就绪走新缓存路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `true`
- **THEN** 系统 SHALL 启动 3 路 `CompletableFuture` 并行执行：
  - 路由 1：`exceptionEventClientService.listDangerEvent()` 查询风险事件
  - 路由 2：`exceptionEventClientService.listDangerInstanceTree()` 查询风险事件实例树
  - 路由 3：`CacheObjectRepository.listByObjectIds(objectIds, {id, localName})` 从缓存查询 ID→名称映射
- **AND** 3 路全部完成后通过 `CompletableFuture.allOf().join()` 等待
- **AND** 从缓存结果构建 `Map<String, String> objectIdAndLocalName`
- **AND** 系统 SHALL NOT 调用 `dtpRwdClientService.mapObjectIdAndLocalName()`

#### Scenario: 缓存未就绪走 fallback 路径

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 返回 `false`
- **THEN** 系统 SHALL 走当前实时查询链路（2 并行 ExceptionEvent Feign + 1 串行 BDTP Feign）
- **AND** 不调用新缓存读取逻辑

### Requirement: API 5 缓存读取零 BDTP Feign

API 5 新缓存路径 SHALL NOT 调用 BDTP Feign 接口。原 BDTP `mapObjectIdAndLocalName` 替换为本地 DB 查询。

#### Scenario: 新缓存路径无 BDTP 调用

- **WHEN** `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 且走新缓存路径
- **THEN** 系统 SHALL NOT 调用 `dtpRwdClientService.mapObjectIdAndLocalName()`
- **AND** ID→名称映射 SHALL 来自本地 `cache_object` 表
