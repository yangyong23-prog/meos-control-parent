## Context

边缘端 `ComputeIotDataHandler` 在接收 MQTT 消息后，对空气源热泵控制组（ACAHC）和空气源热泵（ACCCAH）的 runStatus 信息点执行了二次计算：通过 `updateACAHCInfoValue` → `getGroupObjectMessage`（allEquipRule=true 时取任一热泵运行状态）覆盖控制组的 BDTP 直采 runStatus 原值。改写后的值经 `pushMessage` 推送给前端（SSE/WebSocket），导致前端收到与详情接口（从 BDTP 直查）不一致的值，表现为运行状态闪动。

云端架构无需改动：云端没有 `ComputeIotDataHandler`（不处理 MQTT），没有值改写逻辑，SSE 为 5 分钟定时任务直接从 BDTP 查询（`bizDtpRwdService.queryIpDataOnMomentInOneHour`），与详情接口取值口径一致。

## Goals / Non-Goals

**Goals:**
- 去除边缘端 `ComputeIotDataHandler.updateACAHCInfoValue` 的调用，使 MQTT 消息中 ACAHC/ACCCAH 的 runStatus 保持 BDTP 直采原始值
- 消除空气源热泵控制组详情页运行状态闪动

**Non-Goals:**
- 不修改 `checkComputeInfoCodeDataCondition` 触发条件逻辑
- 不修改 `getGroupObjectMessage` 规则选择逻辑（系统图/关系图路径保留，PRD 明确"本迭代不做处理"）
- 不修改云端任何代码
- 不修改详情接口读取路径（`listInfoCodeConfigsByObjectId` → `listInfoCodeValueInOneHour`）

## Decisions

**决策1：只去掉 `updateACAHCInfoValue` 的调用（L169），不删方法本身**

理由：`updateACAHCInfoValue` 方法（L276-291）被 `computeInfoCodeData`（L169）调用。去掉调用即可阻断值改写。保留方法本身（不再被触发）降低变更风险，避免影响可能存在的其他引用。

备选方案：直接删除 `updateACAHCInfoValue` 方法 + 调用行。不采用——虽然更彻底，但增加不必要的 diff 面积。

**决策2：不修改 `checkComputeInfoCodeDataCondition`**

理由：`checkComputeInfoCodeDataCondition`（L126-129）判断是否为热泵+runStatus，返回 true 触发 `computeInfoCodeData`。去掉 `updateACAHCInfoValue` 调用后，`computeInfoCodeData` 中的其他逻辑（如果存在）仍正常执行。触发条件本身不是"特殊逻辑"，PRD 要求去除的是"用热泵值覆盖控制组值"的逻辑，而非整个二次计算流程。

**决策3：不处理系统图/关系图的 `getGroupObjectMessage`**

理由：PRD 明确"详情跟系统图运行状态逻辑不是同一套，本迭代不做处理"。`getGroupObjectMessage` 走 `loadRealInfoCodeGroupTree` 路径（系统图/关系图），不在详情/SSE 路径上。

## Risks / Trade-offs

- **关系图取值口径不一致** → 去掉 `updateACAHCInfoValue` 后，详情直接取 BDTP 直采值，关系图仍走 `getGroupObjectMessage` 规则选择。PRD 已认可此差异，前端染色由前端处理。
- **`updateACAHCInfoValue` 方法成为死代码** → 方法保留但不再被调用，可在后续迭代中清理。
- **rcRunStatus 衍生计算不受影响** → `insert` 方法只写 rcRunStatus 和控制值到 Redis，runStatus 不写 Redis，去除 `updateACAHCInfoValue` 不影响 rcRunStatus 链路。
