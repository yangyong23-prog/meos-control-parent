## Requirements

### Requirement: Edge MQTT runStatus shall not be overridden by heat pump group logic

边缘端 `ComputeIotDataHandler` 在处理 ACAHC（空气源热泵控制组）和 ACCCAH（空气源热泵）的 runStatus 信息点 MQTT 消息时，MUST NOT 通过 `getGroupObjectMessage` 规则选择逻辑（allEquipRule=true 时取任一热泵运行状态）覆盖控制组的 BDTP 直采 runStatus 原值。MQTT 消息中的 runStatus SHALL 保持 BDTP 直采原始值，直接通过 `pushMessage` 推送给前端。

#### Scenario: 控制组 runStatus 推送值为 BDTP 直采值

- **WHEN** 边缘端收到 ACAHC 控制组的 runStatus MQTT 消息（dataValue=0，停止）
- **THEN** `pushMessage` 推送给前端的 dataValue MUST 为 0（BDTP 直采原始值），不被热泵运行状态覆盖

#### Scenario: 热泵 runStatus 推送值为 BDTP 直采值

- **WHEN** 边缘端收到 ACCCAH 热泵的 runStatus MQTT 消息（dataValue=1，运行）
- **THEN** `pushMessage` 推送给前端的 dataValue MUST 为 1（BDTP 直采原始值），不触发 `updateACAHCInfoValue` 改写

### Requirement: Edge push runStatus shall be consistent with detail API value

边缘端 SSE/WebSocket 推送的 ACAHC/ACCCAH runStatus 值 SHALL 与详情接口（`listInfoCodeConfigsByObjectId`）返回的 runStatus 值口径一致，均取自 BDTP 直采值，消除前端运行状态闪动。

#### Scenario: 详情和推送值一致无闪动

- **WHEN** 用户打开空气源热泵控制组详情页，详情接口返回 runStatus=0（停止）
- **THEN** 后续 SSE/WebSocket 推送的 runStatus MUST 也为 0（停止），前端不会出现"先停止后运行"的闪动

#### Scenario: 云端无需改动

- **WHEN** 云端 SSE 5 分钟定时任务执行 `INFO_CODE_CONFIG_EVENT` 或 `CLOUD_IOT_DATA_CHANGE` 策略
- **THEN** 查询的 runStatus 来自 BDTP 中台（`bizDtpRwdService.queryIpDataOnMomentInOneHour`），MUST 与详情接口取值一致，无需改动云端代码
