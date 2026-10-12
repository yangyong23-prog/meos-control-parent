## 1. 合并器 — PipelineFlowRulesMerger

- [x] 1.1 在 systemgraph 服务包（`com.persagy.apccontrol.basic.service.systemgraph`）下新增 `PipelineFlowRulesMerger` 类，定义常量 `PIPELINE_FLOW_RULES_KEY = "pipelineFlowRules"`，提供静态合并方法（入参：存储中的 pipelineFlowRules JSONObject、incoming data Map，返回合并后结果）
- [x] 1.2 实现三层深合并核心逻辑：遍历 incoming `lines` 的每个 lineId — 值为 null 删除整条线；值非 null 时进入 modes 层，遍历每个 modeKey — 值为 null 删除该模式、值非 null 整体覆盖（只写非 null 值）；请求未出现的 key 不动；全程只走 Map API（`containsKey`/`get`/`remove`），严禁 fastjson 序列化往返（design D4）
- [x] 1.3 实现空壳清理：合并后某线 `modes` 为空时移除该 lineId 条目；`lines` 为空时返回 null（由调用方移除整个 `pipelineFlowRules` key）；`version` 字段 incoming 带则覆盖
- [x] 1.4 处理边界：incoming `lines` 缺失或为 null 时 no-op；null 指向不存在的 lineId/modeKey 时 no-op；新建线 modes 全为 null 时不产生空壳条目

## 2. 编辑态写入 — editSystemGraph CUSTOMIZE 分支改造

- [x] 2.1 修改 `SystemGraphDataServiceImpl#editSystemGraph` 的 CUSTOMIZE 分支：从 `graphDataObject` 获取现有 `customizeContent` JSONObject（不存在时新建），按 `dto.getCode()` 分发 — 非 `pipelineFlowRules` 时 `customizeContent[code] = dto.getData()` 整体替换；`pipelineFlowRules` 时调用 `PipelineFlowRulesMerger` 深合并，合并结果为 null 时从 `customizeContent` 移除该 key
- [x] 2.2 确认 `customizeContent` 整体 put 回 `graphDataObject` 的写入路径不变（压缩、`isUpdateDetail` 等现有流程零改动）

## 3. 重算管线保留 — recalculatePipeline 改造

- [x] 3.1 在 `SystemGraphEditorServiceImpl#recalculatePipeline` 中，`initSystemGraphData` 之后、`saveSystemGraphData` 之前，从老 `SystemGraphData`（`duplicate.getSystemGraphDataId()`）解压读取 `customizeContent.pipelineFlowRules`
- [x] 3.2 将读取到的 `pipelineFlowRules` 整块塞入新 `SystemGraphInstanceVo` 的 `customizeContent`（新建 JSONObject，仅含该 key，不做任何过滤）
- [x] 3.3 边界处理：老数据为 null、`customizeContent` 不存在或不包含 `pipelineFlowRules` 时跳过搬运，行为与改动前一致

## 4. 单元测试

- [x] 4.1 为 `PipelineFlowRulesMerger` 编写单测：新增线、覆盖模式、未提及的 modeKey/lineId 不动、单次请求混合增删改
- [x] 4.2 编写 null 删除协议单测（首个锁死路径）：构造含显式 null 的 incoming Map（模拟 Jackson 反序列化结果，`containsKey == true && get == null`），验证删除单个 modeKey、删除整条线
- [x] 4.3 编写空壳清理单测：modes 删空移除 lineId；lines 删空移除整个 pipelineFlowRules；新建线全 null 不产生空壳
- [x] 4.4 编写合并后序列化单测：合并结果经 `toJSONString` 压缩存储再解析，验证规则数据完整、无 null 值残留、rect 等其他 code 共存

## 5. 验证

- [x] 5.1 执行 `mvn -pl common -am test` 确认编译与全部单测通过
- [ ] 5.2 接口级验证（QA 环境）：调用 `editSystemGraph` 保存 pipelineFlowRules 后调用 `getSystemGraphData`，确认 `customizeContent.pipelineFlowRules` 存在、rect 未被覆盖、历次保存累积
- [ ] 5.3 接口级验证（QA 环境）：调用 `editSystemGraph` 发送 `modes["1.1"] = null` 删除请求，确认查询返回中该 modeKey 已删除、其他数据不受影响
- [ ] 5.4 接口级验证（QA 环境）：调用 `recalculatePipeline` 后确认 `customizeContent.pipelineFlowRules` 完整保留，运行态 `GET /system/graph/instance/{id}` 透传返回该数据
