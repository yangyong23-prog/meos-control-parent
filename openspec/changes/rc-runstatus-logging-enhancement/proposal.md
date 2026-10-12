## Why

线上经常反馈设备状态显示不正确，特别是设备"一直卡在开启中（状态2）或关闭中（状态3）"的问题。当前的 rcRunStatus 计算逻辑（
`ComputeIotDataHandler`）缺少完整的日志记录，导致排查问题时无法获取关键信息：

- 不知道计算何时触发、由哪个信息点触发
- 不知道从 Redis 缓存读取到的 onlineStatus、runStatus 实际值
- 不知道走了哪个决策分支、为什么返回这个状态值
- 不知道是否因为设备特殊逻辑跳过了计算

由于缺乏这些日志，线上问题只能靠猜测和复现，排查效率极低。

## What Changes

- **增强 checkControlRealStatus() 方法日志**：记录控制指令模式下的完整计算上下文（输入值、决策路径、结果）
- **增强 getTriggerFormulaInfoCodeValue() 方法日志**：记录表达式模式下的变量值和计算结果
- **增强 computeInfoCodeDataMap() 方法日志**：记录配置检查和跳过计算的原因
- **统一日志格式**：使用 `[rcRunStatus-场景]` 前缀便于检索
- **保持日志量可控**：只对几十个有 triggerFormulaInfo 配置的设备记录日志

## Capabilities

### New Capabilities

- `rcrunstatus-calculation-logging`: rcRunStatus 计算过程的可观测性日志，包括计算输入、决策路径、计算结果和异常信息

### Modified Capabilities

<!-- 无现有能力修改 -->

## Impact

**影响的代码**：

- `local/meos-control-compute-edge/websocket/handler/ComputeIotDataHandler.java`
    - `checkControlRealStatus()` 方法
    - `getTriggerFormulaInfoCodeValue()` 方法
    - `computeInfoCodeDataMap()` 方法

**影响的系统**：

- 边缘计算模块（meos-control-compute-edge）
- 日志系统（增加日志输出）

**性能影响**：

- 极小：只有几十个设备会触发额外日志
- 每条日志约 200-300 字节
- 每 5 分钟最多增加几十条日志

**风险评估**：

- 低风险：纯日志增强，不改变业务逻辑
- 可回滚：通过日志级别控制即可关闭
