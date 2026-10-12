## Why

空气源热泵控制组详情页运行状态出现闪动：详情接口返回 BDTP 直采的正确值（如"停止"），随后边缘端 SSE/WebSocket 推送了被 `updateACAHCInfoValue` 改写后的错误值（如"运行"），导致前端先对后错。根因是边缘端 `ComputeIotDataHandler` 在处理 MQTT 消息时，通过 `getGroupObjectMessage`（allEquipRule=true 时取任一热泵运行状态）覆盖了控制组的 BDTP 直采 runStatus 原值，改写后的值被推送给前端。PRD 要求去除该特殊逻辑，直接使用 BDTP 数据直采的运行状态。

## What Changes

- 去除边缘端 `ComputeIotDataHandler.updateACAHCInfoValue` 的调用（L169），使 MQTT 消息中空气源热泵控制组（ACAHC）/空气源热泵（ACCCAH）的 runStatus 保持 BDTP 直采原始值，不再被 `getGroupObjectMessage` 规则选择逻辑覆盖
- 改动后边缘端 SSE/WebSocket 推送的 runStatus 与详情接口（从 BDTP 直查）取值口径一致，消除闪动
- 云端不需要改动：云端没有 `ComputeIotDataHandler`（不处理 MQTT），没有值改写逻辑，SSE 为 5 分钟定时任务直接从 BDTP 查询
- 系统图/关系图的 `getGroupObjectMessage` 规则选择逻辑不在本次改动范围内（PRD 明确"详情跟系统图运行状态逻辑不是同一套，本迭代不做处理"）

## Capabilities

### New Capabilities
- `heat-pump-runstatus-direct`: 空气源热泵控制组运行状态取值规范——边缘端 MQTT 消息处理链路中不再对控制组 runStatus 做规则选择覆盖，直接使用 BDTP 直采值推送

### Modified Capabilities
<!-- 无已有 spec 需要修改 -->

## Impact

- **核心文件**: `local/meos-control-compute-edge/src/main/java/com/persagy/apccontrol/websocket/handler/ComputeIotDataHandler.java` — 去除 L169 `updateACAHCInfoValue(source)` 调用
- **影响模块**: 仅边缘端 `meos-control-compute-edge`，云端 `saas/` 无需改动
- **关联逻辑**:
  - `RunningMonitorServerEdge.getGroupObjectMessage`（规则选择逻辑）不受影响，仍保留用于系统图/关系图路径
  - `checkComputeInfoCodeDataCondition`（触发条件）不需要修改
  - `IotDataCacheServiceImpl.insert`（Redis 写入）不受影响，runStatus 本来就不写 Redis
- **不受影响**: 详情接口（`listInfoCodeConfigsByObjectId` → `listInfoCodeValueInOneHour` → BDTP 直查）、云端 SSE 定时任务
