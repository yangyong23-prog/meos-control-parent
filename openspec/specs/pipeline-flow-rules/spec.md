## ADDED Requirements

### Requirement: customizeContent 按 code 合并

`editSystemGraph` 接口在 `type=CUSTOMIZE` 时，SHALL 将 `dto.data` 写入 `customizeContent[dto.code]`，保留 `customizeContent` 中其他 code 的已有数据不变。

#### Scenario: 保存 pipelineFlowRules 不影响 rect

- **WHEN** 副本的 `customizeContent` 中已有 `rect` 数据，前端调用 `POST /systemGraphEditor/editSystemGraph/{duplicateId}`，请求体包含 `{ type: 3, code: "pipelineFlowRules", data: {...} }`
- **THEN** 系统将 `data` 深合并到 `customizeContent.pipelineFlowRules`，`customizeContent.rect` 保持不变

#### Scenario: 保存 rect 不影响 pipelineFlowRules

- **WHEN** 副本的 `customizeContent` 中已有 `pipelineFlowRules` 数据，前端调用 `editSystemGraph`，请求体包含 `{ type: 3, code: "rect", data: {...} }`
- **THEN** 系统将 `data` 整体写入 `customizeContent.rect`，`customizeContent.pipelineFlowRules` 保持不变

#### Scenario: customizeContent 不存在时的首次保存

- **WHEN** 副本的 `SystemGraphData.data` 中尚无 `customizeContent` 字段，前端调用 `editSystemGraph` 保存任意 code 的自定义数据
- **THEN** 系统创建空的 `customizeContent` 对象并将数据写入对应 code 的 key

### Requirement: pipelineFlowRules 三层深合并（PATCH 语义）

`editSystemGraph` 接口在 `type=CUSTOMIZE` 且 `code="pipelineFlowRules"` 时，SHALL 将 `dto.data` 以 PATCH 语义深合并到现有 `customizeContent.pipelineFlowRules`：请求中出现的 key 才被触碰，未出现的 key 保持不变；`lines[lineId]` 层与 `modes[modeKey]` 层独立合并互不影响。

#### Scenario: 新增一条线的规则

- **WHEN** 存储中 `pipelineFlowRules.lines` 不存在 lineId `"00000000000000002003"`，请求 data 的 `lines` 中携带该 lineId 及非 null 的 modes 数据
- **THEN** 系统在 `pipelineFlowRules.lines` 下新建该 lineId 条目并写入其 modes

#### Scenario: 覆盖某个模式

- **WHEN** 存储中 lineId `"00000000000000002003"` 的 `modes` 已有 key `"1.1"`，请求只携带 `lines["00000000000000002003"].modes["1.1"]` 为新的非 null 规则对象
- **THEN** 系统用新规则整体覆盖该模式的 `source` 和 `ruleGroups`，该线其他 modeKey 的数据保持不变

#### Scenario: 请求未提及的模式不受影响

- **WHEN** 存储中某 lineId 的 `modes` 已有 `"1.1"`、`"1.2"` 两个模式，请求只携带 `"1.1"` 的数据
- **THEN** 合并后 `"1.2"` 的数据原样保留

#### Scenario: 请求未提及的其他线不受影响

- **WHEN** 存储中 `pipelineFlowRules.lines` 已有 lineId `"2003"` 和 `"2004"` 两条线，请求只携带 `"2003"` 的数据
- **THEN** 合并后 `"2004"` 的数据原样保留

#### Scenario: 单次请求混合增删改

- **WHEN** 请求 data 的 `lines` 中同时携带多条线的数据，其中部分 modeKey 为非 null 规则、部分为 null
- **THEN** 系统对每个 key 按各自语义独立处理（非 null 覆盖、null 删除、未提及不动）

### Requirement: null 递归删除协议

`editSystemGraph` 接口在合并 `pipelineFlowRules` 时，SHALL 将请求中显式出现的 `null` 值解释为删除指令：`lines[lineId]` 为 null 时删除整条线，`modes[modeKey]` 为 null 时删除该模式。

#### Scenario: 删除单个模式

- **WHEN** 存储中 lineId `"00000000000000002003"` 的 `modes` 已有 `"1.1"` 和 `"1.2"`，请求携带 `lines["00000000000000002003"].modes = { "1.1": null }`
- **THEN** 合并后该线的 `modes` 中 `"1.1"` 被删除，`"1.2"` 保持不变

#### Scenario: 删除整条线

- **WHEN** 存储中 `pipelineFlowRules.lines` 已有 lineId `"00000000000000002003"`，请求携带 `lines["00000000000000002003"]` 为 null
- **THEN** 合并后 `pipelineFlowRules.lines` 中该 lineId 被删除，其他线保持不变

#### Scenario: null 指向不存在的 key

- **WHEN** 请求携带 `lines["2003"].modes["1.1"]` 为 null，但存储中该 lineId 不存在或该线 `modes` 中没有 `"1.1"`
- **THEN** 系统不做任何变更且不报错（no-op）

#### Scenario: 新建线的 modes 全为 null

- **WHEN** 请求携带一个存储中不存在的 lineId，其 modes 中所有 modeKey 均为 null
- **THEN** 系统不产生空壳条目（净效果为 no-op）

### Requirement: 空壳清理

`editSystemGraph` 接口在合并 `pipelineFlowRules` 后，SHALL 自动清理空结构：某条线的 `modes` 为空时移除该 lineId 条目；`lines` 为空时移除整个 `pipelineFlowRules` key。

#### Scenario: 删空某线的所有模式后移除该线

- **WHEN** 存储中 lineId `"2003"` 的 `modes` 只有 `"1.1"` 一个模式，请求携带 `lines["2003"].modes = { "1.1": null }`
- **THEN** 合并后 `pipelineFlowRules.lines` 中不再存在 `"2003"` 条目

#### Scenario: 删光所有线后移除整个 pipelineFlowRules

- **WHEN** 存储中 `pipelineFlowRules` 只有 `"2003"` 一条线且该线仅剩的模式被 null 删除
- **THEN** 合并后 `customizeContent` 中不再存在 `pipelineFlowRules` key

### Requirement: 重算管线保留 pipelineFlowRules

`recalculatePipeline` 在生成新的 `SystemGraphData` 时，SHALL 从老 `SystemGraphData` 的 `customizeContent.pipelineFlowRules` 中整块读取数据并写入新 `SystemGraphData` 的 `customizeContent.pipelineFlowRules`，不做任何过滤或结构重写；其余 `customizeContent` 数据（如 rect）保持原有丢弃行为不变。

#### Scenario: 重算管线时保留规则

- **WHEN** 用户调用 `POST /systemGraphEditor/recalculatePipeline/{duplicateId}`，且老 `SystemGraphData` 的 `customizeContent` 中包含 `pipelineFlowRules`
- **THEN** 新生成的 `SystemGraphData` 的 `customizeContent.pipelineFlowRules` 与老数据完全一致

#### Scenario: 重算管线时无规则可搬运

- **WHEN** 用户调用 `recalculatePipeline`，且老 `SystemGraphData` 的 `customizeContent` 中不包含 `pipelineFlowRules`（或 `customizeContent` 不存在）
- **THEN** 新生成的 `SystemGraphData` 的 `customizeContent` 不包含 `pipelineFlowRules` key，行为与改动前一致

#### Scenario: 重算管线时 rect 照旧丢弃

- **WHEN** 老 `SystemGraphData` 的 `customizeContent` 中同时包含 `rect` 和 `pipelineFlowRules`，用户调用 `recalculatePipeline`
- **THEN** 新生成的 `SystemGraphData` 保留 `pipelineFlowRules`，`rect` 不被搬运（保持改动前行为）

### Requirement: 查询接口透传 pipelineFlowRules

编辑态查询 `GET /systemGraphEditor/getSystemGraphData/{duplicateId}` 与运行态查询 `GET /system/graph/instance/{id}` 返回的 `SystemGraphInstanceVo` SHALL 包含 `customizeContent.pipelineFlowRules` 的完整数据（历次保存深合并后的累积结果），后端查询链路不对该字段做任何加工。

#### Scenario: 编辑态查询返回累积后的规则

- **WHEN** 前端先保存 line `"2003"` 的模式 `"1.1"`，再保存 line `"2004"` 的模式 `"1.1"`，然后调用编辑态查询
- **THEN** 响应体的 `customizeContent.pipelineFlowRules.lines` 中同时包含 `"2003"` 和 `"2004"` 两条线，且与其他 code（如 `rect`）平级共存

#### Scenario: 运行态查询透传规则

- **WHEN** 前端调用运行态 `GET /system/graph/instance/{id}` 查询已发布的系统图实例，且该实例的 `customizeContent` 中包含 `pipelineFlowRules`
- **THEN** 响应体的 `customizeContent.pipelineFlowRules` 与存储数据一致

#### Scenario: 从未保存过规则时无该 key

- **WHEN** 前端查询的系统图从未保存过 `pipelineFlowRules`
- **THEN** 响应体的 `customizeContent` 中不包含 `pipelineFlowRules` key，行为与改动前一致
