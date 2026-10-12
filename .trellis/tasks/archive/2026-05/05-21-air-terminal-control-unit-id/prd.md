# airTerminal listSpaceEquipment 返回控制机组ID

## Goal

在 `/airTerminal/listSpaceEquipment` 返回设备列表时，如果设备是空调机组控制组（`ACAHG`），在该设备对象上返回它通过 `EQUIP_IN_GROUP` 关系关联的空调机组 ID，方便前端识别控制组下的控制机组。

## Requirements

* 仅为空调机组控制组（`DtpRwdClassCodeEnum.ACAHG_GROUP`）补充关联设备 ID。
* 字段放在设备返回对象 `AirTerminalEquipmentGraphVO` 上，命名为 `controlledRelEquipmentIds`，类型为 `Set<String>`。
* 关联关系使用 `DtpRwdRelationTypeEnum.EQUIP_IN_GROUP`，查询方向遵循 RWD 规范：查询组下设备时使用 `setRelationTo(...setObjFrom(groupIds))`。
* 关系查询必须在底层设备查询阶段完成，写入返回的设备 DTO，使后续 `CacheRwdObject` 序列化时已经包含该字段。
* 接口层和缓存命中路径不能为了 `controlledRelEquipmentIds` 再单独查询关系。

## Acceptance Criteria

* [ ] `/airTerminal/listSpaceEquipment` 返回的 `ACAHG` 设备包含 `controlledRelEquipmentIds`。
* [ ] 非 `ACAHG` 设备不额外填充该字段。
* [ ] 没有关联空调机组的 `ACAHG` 设备返回空集合。
* [ ] 缓存构建出的设备对象已包含该字段，缓存命中时不再补查关系。
* [ ] MapStruct 转换编译通过。
* [ ] 相关模块编译或测试通过，或记录无法运行的原因。

## Definition of Done

* 代码遵循后端规范与 RWD 关系方向规范。
* 不引入 N+1 查询。
* 保持现有接口结构与缓存逻辑兼容。

## Technical Approach

在 `AirTerminalEquipmentGraphVO` 增加 `controlledRelEquipmentIds` 字段。复用 `RunningMonitorAirTerminalStrategy#queryBaseIdData` 中已有的 `ACAHG -> ACATAH` 关系查询，保留 `ACAHG` 控制组 ID 到 `ACATAH` 空调机组 ID 集合的映射；在 `queryAllEquipments` 合并完底层设备 DTO 后，将映射写入 `ACAHG` 设备 DTO 的 `relObjIds`。`AirTerminalConverter#toEquipmentGraphVo` 仅在设备 classCode 为 `ACAHG` 时，把 DTO 的 `relObjIds` 映射到 VO 的 `controlledRelEquipmentIds`。

## Out of Scope

* 不修改 RWD 数据结构或新增数据库迁移。
* 不改变现有设备筛选逻辑。
* 不扩展其他控制组类型。

## Technical Notes

* 入口：`common/src/main/java/com/persagy/apccontrol/basic/controller/airteminal/AirTerminalController.java`
* 设备底层查询：`common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorAirTerminalStrategy.java`
* 接口组装：`common/src/main/java/com/persagy/apccontrol/basic/service/airterminal/impl/AirTerminalServiceImpl.java`
* 返回 VO：`common/src/main/java/com/persagy/apccontrol/basic/model/vo/airterminal/AirTerminalEquipmentGraphVO.java`
* 转换器：`common/src/main/java/com/persagy/apccontrol/basic/structure/airterminal/converter/AirTerminalConverter.java`
* 参考规范：`.trellis/spec/backend/rwd-relation-direction.md`、`.trellis/spec/backend/quality-guidelines.md`
