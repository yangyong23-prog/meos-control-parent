## Context

当前壳配置系统已支持多种参数的BDTP同步（冷机排除规则、设备频率设定、运行时间配置、流量需求配置等），这些同步均通过 `InnerSchdule` 定时任务每5分钟执行一次。

本次需求涉及的8个"频率调节范围"参数（ACAC 4个 + ACCC 4个）已在前端实现配置和保存，但尚未接入BDTP同步流程。这些参数的壳配置值格式为"min,max"（如"40,50"），需拆分为 `vFDMinFreqCtrl` 和 `vFDMaxFreqCtrl` 两个独立参数推送至BDTP。

与现有 `saveClassConfs()` 的区别：现有方法按设备类（classCode）推送，本次需求按控制组实例（objectId）推送，调用不同的BDTP端点（saveList vs saveClassConfs）。

## Goals / Non-Goals

**Goals:**
- 将8个FREQ_RANGE壳配置参数按控制组实例级别推送至BDTP
- 壳配置范围值正确拆分为vFDMinFreqCtrl/vFDMaxFreqCtrl两个参数
- 空值时推送paramValue=""，确保BDTP侧状态与壳配置一致
- 默认值从"35,40"调整为"40,50"

**Non-Goals:**
- 不做增量判断（Redis缓存比对），采用全量推送
- 不对已保存的旧默认值做数据迁移
- 不修改现有saveClassConfs()的逻辑
- 不新增Feign接口

## Decisions

### 1. 推送方式：全量推送，不做增量判断

**选择**: 每次定时任务执行时全量推送所有FREQ_RANGE参数值

**理由**: 
- 控制组实例数量有限，全量推送开销可控
- 与 `saveFlowDemandConf` 保持一致，减少代码复杂度
- 避免Redis缓存比对的额外维护成本

**备选方案**: 使用Redis缓存做增量判断（类似saveClassConfs），但因控制组实例数量少，收益不大。

### 2. 空值处理：推送paramValue=""

**选择**: 壳配置值为空时，推送 `paramValue: ""`

**理由**:
- PRD示例中明确展示了空值推送的格式
- BDTP侧需要知道配置被清空，而非"从未配置"
- 与删除操作语义不同（deleteByConf是移除配置，空值推送是显式清空）

### 3. paramLabel取值：使用item.getName()

**选择**: paramLabel使用 `item.getName()` 即"频率调节范围"

**理由**: 使用枚举定义的name字段，与其他同步方法保持一致（如saveFlowDemandConf中使用"流量需求配置"），且更具描述性。

**备选方案**: 使用PRD示例中的"冷源参数"，但该标签过于笼统，无法区分不同参数。

### 4. 默认值变更：修改枚举定义，不迁移历史数据

**选择**: 将8个FREQ_RANGE枚举的defaultValues从"35,40"改为"40,50"

**理由**:
- defaultValues仅影响未保存过配置的新项目
- 已保存过配置的项目使用item.getValue()，不受默认值影响
- 历史数据迁移成本高且无必要

## Risks / Trade-offs

- **[全量推送频率]** 每5分钟全量推送8个FREQ_RANGE参数的所有控制组实例 → 风险低，控制组实例数量有限（通常每个系统4-8个），单次推送数据量小
- **[值拆分异常]** 壳配置值格式异常（非"min,max"格式）→ 在代码中做split后长度校验，异常时跳过该条并记录WARN日志
- **[默认值变更兼容性]** 已保存"35,40"的项目不会自动更新 → 符合预期，用户手动修改时才会生效
