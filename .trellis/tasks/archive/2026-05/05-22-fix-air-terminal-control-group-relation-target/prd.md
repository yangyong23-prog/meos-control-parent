# 修正空调机组控制组关联对象字段位置

## Goal

澄清并修正 `/airTerminal/listSpaceEquipment` 中空调机组控制组（`ACAHG`）关联空调机组（`ACATAH`）ID 的返回位置，确保该关系表达在空调机组控制组设备对象上，而不是被误认为空间对象上的受控设备关系。

## What I already know

* 用户反馈：空调机组控制组关联的空调机组应该放到空调机组控制组对象上，不是空间上。
* 当前代码中 `AirTerminalSpaceGraphVO` 已历史存在 `controlledRelEquipmentIds` 字段，含义是空间受控关联设备 ID。
* 上一轮新增字段在 `AirTerminalEquipmentGraphVO` 上，并通过 `AirTerminalConverter#toEquipmentGraphVo` 仅在 `classCode == ACAHG` 时从设备 DTO 的 `relObjIds` 映射。
* 底层 `RunningMonitorAirTerminalStrategy` 已在设备查询阶段将 `ACAHG -> ACATAH` 的 `EQUIP_IN_GROUP` 关系写入 `ACAHG` 设备 DTO。
* 当前工作树存在用户/本地调试改动：`AirTerminalController#listSpaceEquipment` 将 `useCache` 从 `true` 改为 `false`，本任务不会擅自回退。

## Assumptions

* 本次修正不应破坏空间对象历史语义：`spaces[].controlledRelEquipmentIds` 仍表示空间关联/受控设备，而不是 `ACAHG -> ACATAH`。
* 如果前端要求完全看不到空间对象上的同名字段，那属于已有 API 契约变更，需要单独确认。

## Open Questions

* 已解决：需要改设备对象字段名，避免 `spaces[]` 和 `equipments[]` 上同名字段造成误解。

## Requirements

* `ACAHG` 关联的 `ACATAH` ID 必须只填充到 `equipments[]` 中对应的空调机组控制组设备对象上。
* 设备对象字段命名为 `controlledAirHandlingUnitIds`，表达“空调机组控制组控制/关联的空调机组 ID”。
* 不允许把 `ACAHG -> ACATAH` 关系写入 `spaces[]` 中的空间对象。
* 底层查询仍需在 `CacheRwdObject` 构建前完成关系填充，缓存命中路径不额外查关系。
* 关联出来的 `ACATAH` 必须限制在末端设备范围内。

## Acceptance Criteria

* [ ] `/airTerminal/listSpaceEquipment` 返回中，`classCode=ACAHG` 的设备对象包含 `controlledAirHandlingUnitIds`。
* [ ] 非 `ACAHG` 设备对象不填充该控制组关联字段。
* [ ] 空间对象上的 `controlledRelEquipmentIds` 保持历史语义，不承载 `ACAHG -> ACATAH` 关系语义。
* [ ] 编译通过。

## Definition of Done

* 代码遵守后端规范和 RWD 关系方向规范。
* 不引入接口层/缓存命中路径的额外关系查询。
* 如调整响应契约，更新 `.trellis/spec/backend/air-terminal-api-contracts.md`。

## Out of Scope

* 不修改 RWD 关系结构。
* 不改变空间与设备服务/所在空间关系的既有计算逻辑，除非用户明确要求移除空间字段。
* 不处理无关的 `json/` 目录和本地调试改动。

## Technical Notes

* 空间 VO：`common/src/main/java/com/persagy/apccontrol/basic/model/vo/airterminal/AirTerminalSpaceGraphVO.java`
* 设备 VO：`common/src/main/java/com/persagy/apccontrol/basic/model/vo/airterminal/AirTerminalEquipmentGraphVO.java`
* 接口组装：`common/src/main/java/com/persagy/apccontrol/basic/service/airterminal/impl/AirTerminalServiceImpl.java`
* 设备查询：`common/src/main/java/com/persagy/apccontrol/basic/structure/common/strategy/impl/RunningMonitorAirTerminalStrategy.java`
* 转换器：`common/src/main/java/com/persagy/apccontrol/basic/structure/airterminal/converter/AirTerminalConverter.java`
