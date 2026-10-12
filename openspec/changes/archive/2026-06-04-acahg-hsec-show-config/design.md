## Context

壳配置（ShellConfig）是系统管理设备类显隐与参数配置的机制。在半剖图场景（scene=hsec）下，`group_hsec_show` 分组负责管理各对象类的显隐控制。当前已有 ACATAH（空调机组）和 ACATFU（新风机组）的显隐配置，但 ACAHG（空调机组控制组）作为新增对象类（v7.260502.0），缺少对应的显隐配置项。

## Goals / Non-Goals

**Goals:**
- 在 ShellConfigItemEnum 中新增空调机组控制组的显隐配置项及其配套的隐藏设备列表项
- 严格遵循已有的 ACATAH/ACATFU 显隐配置模式

**Non-Goals:**
- 不修改 ShellConfigItemExtendEnum（显隐配置无需 tab 绑定扩展）
- 不修改 ShellConfigServiceImpl（valSource 仅含 classCode，无 relation 查询需求）
- 不新增后端显隐判断逻辑（设备数量判断由前端负责）

## Decisions

### D1: 枚举参数完全复用 ACATAH 模式

**选择**: 照搬 `GRAPH_HSEC_ACATAH` 的参数格式，仅替换 classCode 为 `acahg`

**理由**: 
- ACATAH 是 PRD 明确要求参考的实现
- 显隐配置参数结构已稳定（scene/group/tab/code/name/unit/defaultValues/step/min/max/hint/desc/valueType/valSource/editAuth）
- 保持一致性可降低前端适配成本

**具体参数**:
- `GRAPH_HSEC_ACAHG("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg", "空调机组控制组显隐配置", "/", "show", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null)`
- `GRAPH_HSEC_ACAHG_LIST("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg_list", "添加需隐藏设备", "/", "", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null)`

### D2: valSource 中 classCode 使用小写

**选择**: 使用 `acahg`（小写）

**理由**: 已有的 ACATAH 和 ACATFU 在 valSource 中均使用小写（`acatah`、`acatfu`），前端设备查询依赖此约定。

### D3: 枚举插入位置在 ACATFU_LIST 之后

**选择**: 在第777行 `GRAPH_HSEC_ACATFU_LIST` 之后插入

**理由**: 保持半剖图显隐配置项的聚合性，便于阅读和维护。

## Risks / Trade-offs

- **[枚举顺序变更]** → 风险极低：Java 枚举不依赖声明顺序进行序列化，且 code 字段是唯一标识
- **[前端未适配]** → 低风险：前端已有一套通用的显隐配置渲染逻辑，新增枚举项可自动渲染，无需前端额外开发
