## Context

### 现有架构

壳配置（Shell Config）系统的BDTP同步目前完全由边缘端（`local/meos-control-compute-edge`）的定时任务 `InnerSchdule` 驱动：

```
SaaS端（ShellConfigServiceImpl.save）
    ↓ 保存到 rc_basics_config 表
    ↓ 通过数据同步机制同步到边缘端
边缘端（InnerSchdule.synShellConfigToTwin，每5分钟）
    ↓ 查询壳配置
    ↓ 组装参数
    ↓ 调用BDTP孪生服务接口
BDTP（/dtp-twin-server-edge/twc/conf/*）
```

现有同步任务（6个）：
- `wirteEquipExclusions()` - 冷站不能同时开启的冷机
- `saveOpenRule()` - 空调冷源冷机开启限制规则
- `saveClassConfs()` - 设备频率设定
- `saveRunningTimeClassConfs()` - 累计运行时间
- `savePumpControlMode()` - 泵控制模式
- `saveFlowDemandConf()` - 流量需求配置
- `saveSupplyRH()` - 湿度参数

### 约束条件

1. **SaaS/边缘职责分离**：SaaS端只负责配置存储，边缘端负责BDTP推送
2. **保持轮询模式**：不引入变更检测/消息队列机制，降低复杂度
3. **空值处理策略**：不同参数有不同空值推送策略（传空数组 vs 不推送）
4. **Feign接口必须双端实现**：SaaS端保持抛异常实现（边缘端专属能力）

## Goals / Non-Goals

**Goals:**
- 新增3个BDTP同步方法，覆盖空调冷源（ACAC）、冷源（ACCC）、空调冷热源（ACAI）
- 扩展Feign接口，支持 `saveStartIntervals` 和 `reviseObjectInSys` 端点
- 完全遵循现有定时轮询架构，保持5分钟同步周期
- 支持空值场景的正确处理（传空数组或不推送）

**Non-Goals:**
- 不修改SaaS端保存逻辑，不引入变更检测机制
- 不引入消息队列或事件驱动架构
- 不改变现有6个同步任务的逻辑
- 不优化查询性能（暂不考虑一次性查询所有壳配置）

## Decisions

### 1. 延续定时轮询模式

**决策**：在 `InnerSchdule.synShellConfigToTwin()` 中新增3个方法调用

**理由**：
- 与现有架构完全一致，降低理解成本
- 容错能力强（失败后下次仍能同步）
- SaaS/边缘职责边界清晰
- 5分钟延迟对壳配置场景可接受

**替代方案**：
- ❌ 变更即时推送（SaaS需感知BDTP，耦合增加，需补偿机制）
- ❌ 混合模式（轮询+消息通知，复杂度高，收益不明显）

### 2. 一个方法处理多系统

**决策**：
- `saveStartIntervals()` 同时处理ACAC和ACCC
- `saveShieldControl()` 同时处理ACAC和ACCC

**理由**：
- 两个系统的处理逻辑几乎相同（只是系统类型不同）
- 减少定时任务调用次数
- 降低代码重复

**替代方案**：
- ❌ 拆成4个独立方法（代码重复多，维护成本高）

### 3. `saveHostUnitLimit` 独立方法

**决策**：`saveHostUnitLimit()` 独立实现，不与 `saveOpenRule()` 合并

**理由**：
- 虽然都调用 `saveExclusions`，但paramCode不同（`equipStartInterval` vs `equipExclusions`）
- 系统不同（ACAI vs ACAC）
- 保持方法职责单一

**替代方案**：
- ❌ 合并到 `saveOpenRule()`（逻辑耦合，难以理解，违反单一职责）

### 4. Feign接口设计

**决策**：
- `saveStartIntervals`: `POST /twc/conf/saveStartIntervals`，参数格式与 `saveExclusions` 类似
- `reviseObjectInSys`: `POST /twc/revise/reviseObjectInSys`，参数为 `{objectId, revise}`

**理由**：
- 遵循PRD接口定义
- 与现有接口命名和路径模式一致

### 5. 空值处理策略

**决策**：

| 参数 | 空值策略 | 实现方式 |
|-----|---------|---------|
| 冷机间隔开启（ACAC/ACCC） | 传空数组 `[]` | 值空时仍组装参数，但 `paramArray=[]` |
| 屏蔽指令（ACAC/ACCC） | 不为空时才推送 | 值空时跳过（PRD："不为空时"） |
| 主机台数限制（ACAI） | 传空数组 `[]` | 值空时仍组装参数，但 `paramArray=[]` |

**理由**：
- 遵循PRD明确的空值要求
- `reviseObjectInSys` 传空无意义（revise必须为0或1），所以只在有值时推送

## Risks / Trade-offs

### 1. 同步延迟风险

**风险**：定时任务每5分钟执行一次，配置变更后最长延迟5分钟才能同步到BDTP

**影响**：对自动化控制逻辑可能产生短暂不一致

**缓解**：
- 5分钟延迟在当前业务场景可接受（控制逻辑本身有保持时间参数，通常30分钟）
- 如未来需要即时同步，可引入变更检测+消息队列机制

### 2. 定时任务膨胀

**风险**：同步任务从7个增加到10个，`synShellConfigToTwin()` 方法变长

**影响**：可维护性降低

**缓解**：
- 每个同步方法职责单一，易于理解
- 可考虑未来提取公共方法（如"查询系统列表→获取壳配置→推送BDTP"模板）
- 当前规模仍可接受

### 3. BDTP接口调用失败

**风险**：BDTP服务不可用时，同步任务失败

**影响**：配置不同步，控制逻辑可能使用旧值

**缓解**：
- 现有任务已有异常捕获和日志记录
- 下次定时任务会重新同步
- 建议增加监控告警（非本次范围）

### 4. 壳配置JSON格式变化

**风险**：前端保存的壳配置值格式变化（如 `ALG_AIR_COOLING_START_INTERVAL_RULE` 的JSON结构）

**影响**：解析失败，同步中断

**缓解**：
- 参考现有 `saveOpenRule()` 的解析逻辑
- 增加JSON解析异常处理和日志记录
- 与前端确认值格式约定

## Migration Plan

### 部署步骤

1. **发布边缘端**（`meos-control-compute-edge`）
   - 包含新增Feign接口和实现
   - 包含新增3个同步方法
   - 无需配置变更（`local.sync.shell-config` 默认true）

2. **验证同步**
   - 观察日志输出，确认新方法正常执行
   - 检查BDTP接口调用是否成功
   - 验证配置值是否正确同步

### 回滚策略

- 回滚边缘端代码即可
- 无数据迁移，无数据库变更
- 回滚后恢复原有7个同步任务

## Open Questions

### 1. `ALG_AIR_COOLING_START_INTERVAL_RULE` 的值格式

**问题**：该参数的JSON数组格式中，除了 `selected`（设备ID数组）外，间隔时间字段的key名是什么？

**推断**：可能是 `interval` 或 `startTime`

**待确认**：与前端或产品确认实际保存的JSON结构

### 2. `ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE` 的系统类型

**问题**：该参数属于 `group_alg_cooling_common_condition`，对应系统类型是 `ACAI`（空调冷热源），但枚举定义中systemType为"ACAI"，是否正确？

**影响**：影响查询系统列表时的classCode

**待确认**：检查 `DtpRwdSystemClassCodeEnum` 中ACAI的定义

### 3. `reviseObjectInSys` 接口是否支持批量

**问题**：该接口是否支持一次传入多个系统ID，还是必须逐个调用？

**影响**：影响 `saveShieldControl()` 的实现方式（循环调用 vs 批量调用）

**待确认**：查看BDTP接口文档或测试验证
