## Why

关系图编辑器新增"管线流向规则"配置功能，前端需要通过 `editSystemGraph` 接口将规则数据（按 lineId/modeKey 组织的流动动画规则）持久化到 `SystemGraphData.customizeContent` 中。当前 CUSTOMIZE 分支采用整块覆盖语义（`graphDataObject.put("customizeContent", dto.getData())`），存在两个问题：一是会把 `customizeContent` 中已有的其他自定义数据（如视口矩形 `rect`）一并覆盖；二是无法支持前端按 PATCH 语义对单条线、单个模式进行增、改、删的独立操作。同时 `recalculatePipeline` 重算管线时会创建全新的 `SystemGraphData`，不搬运老的 `customizeContent`，导致用户配置的管线流向规则在重算后丢失。

## What Changes

- `SystemGraphDataServiceImpl#editSystemGraph` CUSTOMIZE 分支改为按 `code` 合并：所有自定义数据以 `code` 为 key 平级共存于 `customizeContent`，不再整块覆盖（`rect` 等已有数据不再被冲掉）
- `pipelineFlowRules` 实现三层深合并（PATCH 语义）：`lines[lineId]` 级与 `modes[modeKey]` 级独立增删改互不影响；值为 `null` 表示删除该层 key（递归 null 删除）；合并后 modes 为空的线自动移除条目；全部删空后移除整个 `pipelineFlowRules` key
- `SystemGraphEditorServiceImpl#recalculatePipeline` 生成新数据后从老 `SystemGraphData` 整块搬运 `customizeContent.pipelineFlowRules`，其余数据（rect 等）保持原有丢失行为不变
- 新增 `PipelineFlowRulesMerger` 合并器类，承载三层合并逻辑并支持独立单元测试
- 编辑态查询 `GET /systemGraphEditor/getSystemGraphData/{id}` 与运行态查询 `GET /system/graph/instance/{id}` 零改动，`customizeContent` 全程透传，返回中 `pipelineFlowRules` 与 `lines`/`rect` 平级

## Capabilities

### New Capabilities
- `pipeline-flow-rules`: 管线流向规则的持久化存储，覆盖编辑态写入（PATCH 深合并 + null 删除协议）、重算管线保留（选择性搬运）、编辑态与运行态查询透传四条链路

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- **代码**: 修改 2 个文件、新增 1 个文件，约 80 行代码
  - `SystemGraphDataServiceImpl.java`（editSystemGraph CUSTOMIZE 分支按 code 合并 + pipelineFlowRules 深合并入口）
  - `SystemGraphEditorServiceImpl.java`（recalculatePipeline 搬运保留 pipelineFlowRules）
  - `PipelineFlowRulesMerger.java`（新增，三层合并器）
- **API 协议**: 无接口与 DTO 结构变更；`type=3, code="pipelineFlowRules"` 的请求 data 语义升级为 PATCH 深合并（前端已按此协议开发）
- **数据库**: 无表结构变更（存储仍在 `system_graph_data.data` 压缩 JSON 内）
- **前端**: 读取路径统一为 `customizeContent.pipelineFlowRules`；QA 存量老协议顶层平铺的 `lines`/`version` 僵尸数据不迁移，前端忽略即可
- **风险**: 重算管线后 ADM 生成的 lineId 可能变化，保留的规则中部分 lineId 成为死链；按决策后端不过滤，死链原样保留由前端处理
