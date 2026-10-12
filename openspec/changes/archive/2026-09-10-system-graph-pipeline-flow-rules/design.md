## Context

关系图编辑器通过 `SystemGraphData.data`（压缩 JSON）存储完整图纸数据，其中 `customizeContent` 字段定位为"前端更新、前端消费、后端不消费"的自定义内容区。当前 `editSystemGraph` 的 CUSTOMIZE 分支直接整块覆盖 `customizeContent`，`recalculatePipeline` 重算管线时生成全新的 `SystemGraphData` 且不搬运任何老的 `customizeContent`。

前端已按新协议开发完成：`type=3, code="pipelineFlowRules"`，请求 data 按 `{version, lines: {[lineId]: {modes: {[modeKey]: 规则|null}}}}` 组织，采用 PATCH 语义（只发被操作的 modeKey），显式 `null` 表示删除。

涉及的接口：
- `POST /systemGraphEditor/editSystemGraph/{duplicateId}` — 编辑态写入（核心改动）
- `GET /systemGraphEditor/getSystemGraphData/{duplicateId}` — 编辑态读取（零改动）
- `POST /systemGraphEditor/recalculatePipeline/{duplicateId}` — 重算管线（搬运保留）
- `GET /system/graph/instance/{id}` — 运行态读取（零改动）

本项目 HTTP 层反序列化走 Spring Boot 默认 Jackson（无自定义 MessageConverter），`dto.getData()` 得到的 `LinkedHashMap` 会保留显式 null 值（key 存在、值为 null）；存储层使用 fastjson 1.x 处理 `SystemGraphData.data` 的解析与序列化，fastjson `toJSONString` 默认丢弃 null 值。

## Goals / Non-Goals

**Goals:**

- `customizeContent` 中多种自定义数据（rect、pipelineFlowRules）按 code 平级共存、互不覆盖
- `pipelineFlowRules` 支持 PATCH 深合并：lineId 级与 modeKey 级独立增删改，显式 null 递归删除，空壳自动清理
- 重算管线后 `pipelineFlowRules` 完整保留（整块搬运、不过滤）
- 编辑态与运行态查询零改动自然透传

**Non-Goals:**

- 不清理历史已存在的顶层平铺 `lines`/`version` 僵尸数据（QA 老协议遗留，前端忽略）
- 不为 pipelineFlowRules 做 DTO/VO 强类型建模（后端不消费其内容）
- 不处理 `recalculatePipeline` 的并发锁问题（已有竞争窗口，本次不扩大）
- 不修改 DTO/VO 结构或新增 API 端点
- 不对重算后 lineId 死链做过滤（后端不理解规则内容，死链原样保留由前端处理）

## Decisions

### D1: 存储结构 — `customizeContent[code]` 平级字典

**选择**: `pipelineFlowRules` 作为 `customizeContent` 的一个 key，与已有的 `rect` 等平级，key 名即前端传入的 `dto.code`

**理由**:
- `customizeContent` 已是多用途字典桶，平级结构天然支持多种自定义内容共存
- 前端读取路径统一：`data.customizeContent?.pipelineFlowRules`
- 按 code 合并后，保存 rect 不再冲掉 pipelineFlowRules，反之亦然

**替代方案**:
- 新增 `EnumUpdateGraphType.PIPELINE_FLOW_RULES(4)` 作为一等公民 — 过重，需改枚举、VO、多条链路，且后端不消费此数据

### D2: 合并语义 — 非 pipelineFlowRules 整体替换，pipelineFlowRules 三层深合并

**选择**: CUSTOMIZE 分支中，`code != "pipelineFlowRules"` 时 `customizeContent[code] = dto.data`（整体替换，rect 等全量数据语义不变）；`code == "pipelineFlowRules"` 时调用合并器做三层深合并

**理由**:
- rect 等已有 code 的前端协议是全量快照，整体替换即正确行为
- pipelineFlowRules 的前端协议是局部 diff（PATCH），必须深合并
- 深合并规则：`lines[lineId]=null` 删整条线；`modes[modeKey]=null` 删该模式；非 null 值整体覆盖该层 key；请求未出现的 key 不动；合并后 modes 空则移除该 lineId；lines 空则移除整个 pipelineFlowRules key

**替代方案**:
- 所有 code 都做递归深合并 — 语义过度，会破坏 rect 的全量替换协议
- 按未知 code 透传 + 已知 code 硬编码特判 — 当前只有一个特殊 code，YAGNI

### D3: 合并器独立成类 — `PipelineFlowRulesMerger`

**选择**: 在 systemgraph 服务包内新增独立合并器类（静态工具方法），`SystemGraphDataServiceImpl` 的 CUSTOMIZE 分支只做分发调用

**理由**:
- null 语义陷阱（见 D4）+ 三层合并规则值得独立、可单测的单元
- 服务类保持干净，合并逻辑不渗入数据层流程代码
- 单测无需 mock 任何依赖（纯函数式处理两个 Map）

**替代方案**:
- 直接在 CUSTOMIZE 分支内写私有方法 — 可测性差，服务类膨胀
- 策略族 `CustomizeContentMergeStrategy` 按 code 注册 — 当前仅一个特殊 code，机制过重，未来出现第二个深合并 code 再演进

### D4: null 语义的序列化硬约束 — 只走 Map API，禁止 fastjson 往返

**选择**: 合并逻辑全程操作 `Map`/`JSONObject` 的 `containsKey`/`get`/`remove` API；严禁对 incoming data 做 `toJSONString` 再 `parseObject` 的往返转换；存储只写入非 null 值

**理由**:
- 显式 null（`"1.1": null`）是删除协议的载体，必须能在运行时与"key 不存在"区分
- Jackson 反序列化保留显式 null（`containsKey("1.1") == true && get("1.1") == null`），一次 fastjson `toJSONString` 往返即丢失全部显式 null，删除协议静默失效
- 只写入非 null 值，则存储 JSON 中永不出 null，fastjson 序列化安全无歧义
- `version` 字段请求带则覆盖

**替代方案**:
- 引入 `SerializerFeature.WriteMapNullValue` 全局写 null — 影响面不可控，且存储中出现 null 值会污染其他消费方

### D5: 重算管线选择性搬运 — 只搬 pipelineFlowRules

**选择**: `recalculatePipeline` 在 `initSystemGraphData` 之后、`saveSystemGraphData` 之前，从老 `SystemGraphData` 读取 `customizeContent.pipelineFlowRules`，整块塞入新 vo 的 `customizeContent`（新建 JSONObject，仅含该 key）

**理由**:
- "以前的数据丢失就丢失，不动"；只有 pipelineFlowRules 是有业务价值且必须保留的新数据
- 整块搬运不做过滤：后端不理解 lineId 与规则内容的对应关系，重算后 lineId 变化产生的死链由前端处理
- 边界：老 `customizeContent` 为 null 或不含 `pipelineFlowRules` 时跳过，行为与改动前一致

**替代方案**:
- 搬运整个 `customizeContent` — 会把 rect、僵尸 key 一并带过去，违背"以前的逻辑不变"决策

### D6: 查询链路零改动

**选择**: 编辑态与运行态查询链路不做任何修改

**理由**:
- 两条查询链路最终都走 `getSystemGraphDataNoDetailDataById`，fastjson 将 `data` 反序列化到 `SystemGraphInstanceVo`，`customizeContent` 为 `Object` 类型，JSON 结构怎么存就怎么返回
- 运行态后续加工（设备节点、信息点、IOT 值）从不触碰 `customizeContent`
- 数据从存储到返回全程透传，返回的 `pipelineFlowRules` 即历次深合并的累积结果

## Risks / Trade-offs

- **[显式 null 意外丢失]** → 任何绕过 Map API 的转换（如日志打印后再解析、防御性 copy 时的序列化往返）都会让删除协议静默失效。**缓解**: D4 作为硬性条款写入实现与 code review 检查项；首个单测锁死 `"1.1": null` 删除路径
- **[重算后 lineId 死链]** → ADM 重算生成的 lineId 可能与老数据不一致，保留规则中的部分 lineId 指向不存在的线。**缓解**: 已确认按需求整块保留不过滤；前端展示时忽略死链或引导用户重新配置；如后续需治理可改用稳定标识
- **[QA 存量僵尸数据]** → 老副本 `customizeContent` 顶层残留平铺的 `lines`/`version`，与新协议的 `pipelineFlowRules` 平级共存于返回中。**缓解**: 前端只认 `customizeContent.pipelineFlowRules`，忽略顶层平铺 key；数据体积可忽略
- **[recalculatePipeline 并发]** → `editSystemGraph` 有 Redisson 锁保护，`recalculatePipeline` 未加锁，存在竞争窗口。**缓解**: 该窗口改动前已存在，本次不扩大；后续可统一加锁

## Migration Plan

- 无数据库结构变更、无部署依赖，直接发布即可
- 回滚策略：整个变更无状态迁移，回滚代码后新保存的数据中 `customizeContent.pipelineFlowRules` 会因整块覆盖语义暂时失效（被 rect 保存冲掉），功能层面回退到改动前行为，无数据损坏风险

## Open Questions

（无 — 前端 PATCH 协议、null 删除语义、整线删除、空壳清理、重算保留范围均已在需求确认阶段闭环）
