## Why

MEOS 能源监控系统中的设备运行状态（rcRunStatus）计算逻辑分布在多个代码文件和数据库配置中，缺乏统一的文档说明。当前的状态计算通过触发公式（triggerFormula）动态执行，涉及
onlineStatus、equipSwitchSet/runStatus 等多个信息点的组合判断，产生了"开启中"（状态2）和"关闭中"（状态3）这两个重要的过渡状态。

由于缺乏系统化的文档记录，开发团队在维护、排查问题和扩展新功能时，需要深入阅读代码和 SQL
迁移脚本才能理解完整的状态计算逻辑。这增加了维护成本，也容易在修改时引入错误。

本变更旨在通过深入分析现有代码，形成完整的技术文档，记录状态计算的完整机制，为后续的系统维护和优化提供依据。

## What Changes

- **文档化状态计算逻辑**：记录从信息点配置到最终状态展示的完整数据流
- **梳理公式计算机制**：解析 triggerFormula 的结构、变量和执行引擎（Aviator）
- **明确状态映射规则**：建立 onlineStatus、equipSwitchSet、runStatus 到 rcRunStatus 的完整映射表
- **识别部署差异**：对比边缘部署（local）和 SaaS 云部署（saas）在状态计算上的异同
- **提出优化建议**：基于分析结果，识别潜在风险点并给出改进方向

## Capabilities

### New Capabilities

- `device-state-calculation-doc`: 设备运行状态计算逻辑的完整技术文档，包括架构设计、数据流向、状态映射规则和业务场景分析

### Modified Capabilities

<!-- 本变更不涉及现有 spec 的修改，仅为文档化工作 -->

## Impact

**影响的代码和系统**：

- 文档分析涉及的代码：
    - `ComputeIotDataHandler.java`（边缘计算核心处理类）
    - `InfoCodeLinkControlHandler.java`（触发公式验证）
    - `TriggerFormulaInfo.java`（公式配置实体）
    - `DemandCommandTask.java`（照明需求指令计算）

**数据库配置**：

- `info_code_config` 表的 `extend_info.triggerFormulaInfo` 字段
- 相关的 Flyway 迁移脚本（V6.0.0.4, V7.250403.0.0, V7.251002.0.0 等）

**影响范围**：

- 本变更为纯文档化工作，**不修改任何现有代码逻辑**
- 不影响现有 API、依赖或系统行为
- 为后续的代码优化和重构提供基础
