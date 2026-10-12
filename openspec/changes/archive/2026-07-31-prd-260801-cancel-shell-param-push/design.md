## Context

`InnerSchdule.saveClassConfs()` 方法（L184-L214）在定时任务 `synShellConfigToTwin()` 中被调用，每5分钟执行一次。该方法内部调用重载的 `saveClassConfs(updateParamList, deleteParamList, class_code_map, shellConfigItemEnum)` 三次，分别处理三组壳配置枚举。

PRD 要求取消前两组（POLICY_DEVICE_FAN_FREQ、POLICY_DEVICE_PUMP_FREQ），保留第三组（POLICY_DEVICE_COLDHOT_FREQ）。

## Decisions

### 1. 最小化改动：仅移除两行调用

**选择**: 在 `saveClassConfs()` 方法中直接移除两行调用，不修改其他逻辑。

**理由**: 
- 需求明确仅取消推送，不涉及数据清理或逻辑重构
- Redis 增量缓存逻辑继续保留，不影响冷热源泵组（ACPPC）的增量比对

**备选方案**: 将整个 `saveClassConfs()` 方法重构为只处理 ACPPC，但因冷热源泵组仍需要增量比对逻辑，保留现有框架更安全。

### 2. 不清理已推送的BDTP旧数据

**选择**: 不做一次性清理推送

**理由**: BDTP 侧旧数据保留不影响业务，下次定时任务执行后新数据不再更新即可
