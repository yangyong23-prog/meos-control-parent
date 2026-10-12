# 机房安全查询静态对象集成CacheRwdObject

## Goal

将机房安全类型的静态数据查询集成 CacheRwdObject，覆盖以下两个接口：

1. `/dashboardGridChart/querySpace` - 网格图空间查询
2. `/instance/listEquipments/COMPUTER_ROOM_SECURITY` - 设备列表查询
   减少对 DTP RWD 的实时调用，提升查询性能。

## Background

当前 COMPUTER_ROOM_SECURITY 的 `listDashboardSpaces()` 方法每次请求都实时查询 DTP RWD
获取空间和设备数据，而其他监测类型（AIR_TERMINAL、VENTILATION、LIGHTING）已通过 CacheRwdObject 实现了缓存机制。需要对机房安全也实现同样的缓存集成。

机房安全的 `listSpaces()` 方法存在白名单逻辑（通过 `InstanceConfigService.getInstanceConfig(WHITE_LIST, ...)` 查询
`instance_config` 表），白名单查询也需要走 CacheRwdObject 缓存，参考 AIR_TERMINAL 的 `CacheRwdAirTerminalWhiteListStrategy`
实现。

`/instance/listEquipments/COMPUTER_ROOM_SECURITY` 接口特殊之处：传入 `filterEquipmentInSpaceIds`
进行空间维度的设备过滤。因此缓存时需要存储全量设备数据（包含 `relObjIds` 字段即设备所在空间ID），查询时先从缓存读取全量设备，再根据
`filterEquipmentInSpaceIds` 在内存中过滤。参考照明的 `CacheRwdLightingStrategy` 缓存全量设备 + 查询时内存过滤的模式。

## Requirements

- 创建 `CacheRwdComputerRoomSecurityStrategy`，继承 `BaseCacheRwdStrategy`，实现机房安全类型的缓存构建逻辑
    - 缓存空间数据（SPACE）：调用 `listDashboardSpaces()` 获取完整空间+设备数据
    - 缓存设备数据（EQUIPMENT）：单独缓存全量设备数据（含 `relObjIds` 字段），用于 `listEquipments` 接口的缓存查询
- 创建 `CacheRwdComputerRoomSecurityWhiteListStrategy`，继承 `BaseCacheRwdStrategy`，实现机房安全白名单缓存构建
    - 参考 `CacheRwdAirTerminalWhiteListStrategy` 的实现模式
    - 从 `instance_config` 表读取白名单配置，查询对应空间数据后更新缓存
- 改造 `DashboardGridChartServiceImpl.querySpace()` 的 COMPUTER_ROOM_SECURITY 分支优先从缓存读取
    - 优先检查白名单缓存（`buildWhitelistKey`）
    - 白名单缓存不存在时，使用普通缓存
    - 缓存全部未命中时回退到实时查询
- 改造 `RunningMonitorComputerRoomSecurityStrategy.listEquipments()` 支持从缓存读取设备数据
    - 从缓存获取全量设备数据后，根据 `filterEquipmentInSpaceIds` 在内存中过滤
    - 缓存未命中时回退到实时查询
    - 在 `InstanceController.allowMysqlCache()` 中添加 COMPUTER_ROOM_SECURITY 类型
- 修复 `CacheRwdDomainServiceImpl.buildWhitelistKey()` 中硬编码 `AIR_TERMINAL` 的 bug，改为使用传入的
  `runningMonitorType` 参数
- 遵循现有缓存策略模式（参考 CacheRwdVentilationStrategy、CacheRwdLightingStrategy）

## Acceptance Criteria

- [ ] CacheRwdComputerRoomSecurityStrategy 类创建完成，runningMonitorType() 返回 COMPUTER_ROOM_SECURITY
- [ ] CacheRwdComputerRoomSecurityWhiteListStrategy 类创建完成，处理白名单缓存
- [ ] buildCacheData() 方法能正确构建空间和设备的缓存数据（设备含 relObjIds）
- [ ] querySpace 查询接口优先从 cache_rwd_object 表读取数据（先白名单、后普通缓存）
- [ ] listEquipments 查询接口从缓存读取全量设备后按 filterEquipmentInSpaceIds 内存过滤
- [ ] InstanceController.allowMysqlCache() 添加 COMPUTER_ROOM_SECURITY
- [ ] 缓存未命中时回退到实时查询
- [ ] buildWhitelistKey() 方法修复，不再硬编码 AIR_TERMINAL
- [ ] 日志使用 LogComponent 接口，中文直写
- [ ] JavaDoc 注释完整，@since 标签规范

## Technical Notes

-
参考实现：CacheRwdVentilationStrategy（空间+设备缓存模式）、CacheRwdLightingStrategy（设备全量缓存+内存过滤）、CacheRwdAirTerminalWhiteListStrategy（白名单缓存模式）
- 返回 VO 类型：ComputerRoomDashboardSpaceVO（含 roomFuncTypeName、equipmentsInSpace）
- 设备 VO 类型：ComputerRoomEquipmentVo（含 equipCriticalLevel、classCodeName）
- 缓存 key 格式参考：
    - 空间：`cache_rwd_computer_room_security_list_dashboard_spaces`
    - 设备：`cache_rwd_computer_room_security_list_equipments`
- 白名单 key 格式：`WHITELIST_COMPUTER_ROOM_SECURITY_space`
- customIndexKey 使用 SaltMD5Util.MD5WithPrefix 生成
- 缓存构建需设置 meosCache=false 避免递归查询缓存
- buildWhitelistKey bug 修复：`RunningMonitorTypeEnum.AIR_TERMINAL.name()` → `runningMonitorType.name()`
- listEquipments 缓存查询策略：缓存全量设备数据（objectData 含 relObjIds），查询时按 filterEquipmentInSpaceIds 内存过滤
- allowMysqlCache 扩展：在 InstanceController.allowMysqlCache() 中增加
  `runningMonitorType == RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY`

## Key Files

- `common/.../structure/common/strategy/impl/RunningMonitorComputerRoomSecurityStrategy.java` - 机房安全策略（需改造
  listDashboardSpaces + listEquipments 查询流程）
- `common/.../structure/cacherwd/strategy/impl/CacheRwdVentilationStrategy.java` - 通风缓存策略（参考）
- `common/.../structure/cacherwd/strategy/impl/CacheRwdLightingStrategy.java` - 照明缓存策略（设备全量缓存+内存过滤参考）
- `common/.../structure/cacherwd/strategy/impl/CacheRwdAirTerminalWhiteListStrategy.java` - 空调末端白名单策略（参考）
- `common/.../structure/cacherwd/strategy/BaseCacheRwdStrategy.java` - 缓存策略基类
- `common/.../structure/cacherwd/strategy/CacheRwdStrategy.java` - 缓存策略接口
- `common/.../service/dashboard/DashboardGridChartServiceImpl.java` - Dashboard服务（COMPUTER_ROOM_SECURITY分支）
- `common/.../structure/cacherwd/service/CacheRwdDomainServiceImpl.java` - 需修复 buildWhitelistKey bug
- `common/.../controller/InstanceController.java` - 需在 allowMysqlCache 添加 COMPUTER_ROOM_SECURITY
- `common/.../model/vo/computeroom/ComputerRoomDashboardSpaceVO.java` - 返回VO
- `common/.../model/vo/computeroom/ComputerRoomEquipmentVo.java` - 设备VO
- `common/.../model/dto/rwd/DtpRwdEquipmentDTO.java` - relObjIds 设备空间关系字段
