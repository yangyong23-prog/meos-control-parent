## Why

空调机组控制组（ACAHG）已在 DtpRwdClassCodeEnum 中定义（v7.260502.0），但半剖图显隐藏配置中缺少对应的显隐配置项，导致前端无法控制该对象类在半剖图中的显示与隐藏。PRD【PRD-0601】要求新增此配置，逻辑与已有的"空调机组显隐配置"（ACATAH）一致。

## What Changes

- 在 ShellConfigItemEnum 中新增 `GRAPH_HSEC_ACAHG`（空调机组控制组显隐配置）枚举项，归属 `group_hsec_show` 分组，valSource 指定 classCode 为 `acahg`
- 在 ShellConfigItemEnum 中新增配套的 `GRAPH_HSEC_ACAHG_LIST`（添加需隐藏设备）枚举项，与已有 ACATAH_LIST / ACATFU_LIST 模式一致

## Capabilities

### New Capabilities
- `acahg-hsec-visibility`: 空调机组控制组在半剖图中的显隐配置能力，支持显示/隐藏切换及指定隐藏设备列表

### Modified Capabilities
（无，本次为纯新增配置项，不修改已有行为）

## Impact

- **代码影响**：`common` 模块 `ShellConfigItemEnum.java`，新增2个枚举实例
- **前端影响**：前端壳配置页面将自动渲染新增的显隐配置项，并根据项目交付的 ACAHG 设备数量判断是否展示
- **数据影响**：无数据库变更，枚举变更通过壳配置机制自动生效
