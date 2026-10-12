## Why

机房安全五个定制接口每次请求走实时查询链路：

1. Dashboard 空间列表 `/dashboardGridChart/querySpace/computerRoomSecurity`：包含 2~3 次 BDTP Feign（空间+设备，最大瓶颈）和 5 次 Knowledge Feign，280 空间 → 23,435 设备场景耗时约 14.9 秒
2. 空间设备列表 `/instance/listEquipmentsInBySpaceIds/computerRoomSecurity`：包含 1 次 BDTP Feign + 1 次 IoT Feign + 2 次 Knowledge Feign
3. 楼层设备列表 `/listEquipments/computerRoomSecurity`：包含 2 次 BDTP Feign（空间定位+设备查询）+ 3 次 Knowledge Feign
4. 异常事件过滤 `/abnormalEvent/new/listAbnormalEvent`（COMPUTER_ROOM_SECURITY）：入参 ID 来自 API 1 返回结果，`exceptionFilterConditionNew()` 中 2 次 BDTP Feign（门禁受控状态+排除类型设备）+ 2 次 Knowledge Feign（getFilterExcludeClassCodes）
5. 风险事件查询 `/dangerEvent/listByDangerObject`（COMPUTER_ROOM_SECURITY）：入参 ID 来自 API 1 返回结果，`listByDangerObject()` 中 1 次 BDTP Feign（`mapObjectIdAndLocalName`）+ 2 次 ExceptionEvent Feign（并行），BDTP 调用与 ExceptionEvent 无数据依赖却串行执行

参照 VENTILATION 新缓存模型，将空间、设备和 IoT 规则数据提前缓存到本地 `cache_object` / `cache_object_rel` / `cache_rule` 系列表，读取时从本地数据库获取，消除 BDTP 和 IoT Feign 调用，大幅降低接口耗时。

## What Changes

- 改造 `CacheRwdComputerRoomSecurityStrategy.buildCacheData()`：从机房安全空间扩展到所在楼层全量空间+设备，写入 `cache_object` / `cache_object_rel`，设备查询 `withColumns` 含 `bimId`、`bimLocation`，同时查询 IoT 规则写入 `cache_rule` / `cache_rule_key_rel`，移除 Knowledge 烘焙逻辑
- 改造 `RunningMonitorComputerRoomSecurityStrategy.listDashboardSpacesComputerRoomSecurity()`（API 1）：新增 `isReady()` 判断分支，缓存命中时从本地 DB 读取楼层全量数据，运行时做 roomFuncType 过滤、白名单过滤、excludeClassCodes 排除过滤，Knowledge 数据实时查询
- 改造 `InstanceServiceImpl.listEquipmentsInBySpaceIdsComputerRoomSecurity()`（API 2）：新增 `isReady()` 判断分支，通过 `cache_object_rel` 按 spaceId 查设备 ID，再从 `cache_object` 取设备数据，从 `cache_rule` 读可运行状态（0 Feign），Knowledge 数据实时查询
- 改造 `RunningMonitorComputerRoomSecurityStrategy.listEquipmentsFromRwdFlor()`（API 3）：新增 `isReady()` 判断分支，从缓存空间按 floorId+classCode=SPACE 过滤出 spaceIds，再从 `cache_object_rel` + `cache_object` 查设备，Knowledge 数据实时查询
- 改造 `RunningMonitorComputerRoomSecurityStrategy.exceptionFilterConditionNew()`（API 4）：新增 `isReady()` 判断分支，通过 `CacheObjectRepository.listByObjectIds()` 按设备 ID 批量查 classCode + monitoringSurveillanceStatus，内存过滤替代 2 次 BDTP Feign，Knowledge 数据（getFilterExcludeClassCodes）实时查询
- 改造 `DangerEventServiceImpl.listByDangerObject()`（API 5）：新增 `isReady()` 判断分支，通过 `CacheObjectRepository.listByObjectIds(objectIds, {id, localName})` 替代 BDTP Feign `mapObjectIdAndLocalName()`，并将其与 2 次 ExceptionEvent Feign 改为 3 路并行执行
- 注册缓存基础设施：`CacheObjectModelKeyConfig` 新增 `CRS_LIST_SPACE` / `CRS_LIST_EQUIPMENT` 键，`CacheObjectModelStatusService` 的 `supportedRunningMonitorTypes` 新增 `COMPUTER_ROOM_SECURITY`
- 设备查询 `withColumns` 新增 `monitoringSurveillanceStatus`、`bimId`、`bimLocation`
- 移除 `DashboardGridChartServiceImpl` 中已废弃的 `meosCache=true`
- 旧 `cache_rwd_object` 写入保留不删除，新旧缓存并存

## Capabilities

### New Capabilities
- `computer-room-security-floor-cache`: 机房安全楼层级空间与设备缓存，涵盖写入侧楼层扩展构建、IoT 规则缓存写入、读取侧多层过滤、API 1/2/3/4/5 五个接口的缓存消费、缓存基础设施注册、新旧缓存并存切换

### Modified Capabilities

（无）

## Impact

- **代码文件**：
  - `CacheRwdComputerRoomSecurityStrategy` — 写入侧重构 + IoT 规则写入
  - `RunningMonitorComputerRoomSecurityStrategy` — API 1 + API 3 + API 4 读取侧新增缓存分支
  - `InstanceServiceImpl` — API 2 读取侧新增缓存分支
  - `DangerEventServiceImpl` — API 5 读取侧新增缓存分支 + 3 路并行改造
  - `AbnormalEventServiceImpl` — API 4 调用入口（`listAbnormalEventNew`）
  - `CacheObjectModelKeyConfig` — 新增缓存键
  - `CacheObjectModelStatusService` — 新增支持的运行监控类型
  - `DashboardGridChartServiceImpl` — 清理 `meosCache=true`
- **API 行为**：五个接口响应不变，仅性能提升
- **依赖**：新增对 `CacheObjectRepository`、`CacheObjectRelRepository`、`CacheRuleRepository`、`CacheObjectModelWriteService`、`CacheObjectModelStatusService` 的依赖（均已存在于项目中）
- **数据库**：写入 `cache_object`、`cache_object_rel`、`cache_rule`、`cache_rule_key_rel` 系列表（表结构已存在）
