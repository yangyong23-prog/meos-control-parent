# 设备运行状态（rcRunStatus）计算逻辑设计文档

## 1. 架构设计概览

### 1.1 核心设计理念

系统采用**配置驱动的动态计算引擎**架构，将状态计算逻辑从代码中抽离，存储在数据库配置中，运行时动态解析执行。

```
┌──────────────────────────────────────────────────────────────┐
│                    信息点配置层 (info_code_config)             │
│                                                               │
│  extend_info.triggerFormulaInfo:                             │
│  ├─ triggerFormula: 计算表达式                                │
│  ├─ triggerInfoCode: 触发后的目标信息点 (rcRunStatus)          │
│  ├─ variables: 变量列表 [onlineStatus, equipSwitchSet, ...]   │
│  └─ sourceIotType: 数据源类型 (0=控制指令, 1=信息点表达式)      │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────┐
│                  IoT 数据接收层 (MQTT)                         │
│                                                               │
│  FeedbackInfoMsgMqttVO:                                      │
│  ├─ objectId: 设备ID                                          │
│  ├─ infoCode: 信息点代码 (equipSwitchSet/runStatus等)          │
│  ├─ dataValue: 数据值                                         │
│  ├─ type: 数据类型 (FEEDBACK/CTRL)                           │
│  └─ dataStatus: 数据状态 (1=完成, 2=进行中)                    │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────┐
│               公式计算引擎 (ComputeIotDataHandler)             │
│                                                               │
│  计算流程:                                                    │
│  1. checkComputeInfoCodeDataCondition() - 前置条件判断         │
│  2. computeInfoCodeData() - 执行计算                          │
│  3. getTriggerFormulaInfoCodeValue() - 获取公式计算结果        │
│  4. AviatorEvaluator.execute() - 表达式引擎执行                │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────┐
│                  状态输出层 (rcRunStatus)                      │
│                                                               │
│  状态值映射:                                                  │
│  -1 = 无效 (设备离线或数据缺失)                                 │
│   0 = 停止                                                    │
│   1 = 运行                                                    │
│   2 = 开启中 (过渡状态)                                        │
│   3 = 关闭中 (过渡状态)                                        │
└──────────────────────────────────────────────────────────────┘
```

### 1.2 关键组件

| 组件                             | 文件位置                                                                           | 职责          |
|--------------------------------|--------------------------------------------------------------------------------|-------------|
| **TriggerFormulaInfo**         | `common/model/entity/TriggerFormulaInfo.java`                                  | 公式配置实体      |
| **ComputeIotDataHandler**      | `local/meos-control-compute-edge/websocket/handler/ComputeIotDataHandler.java` | 边缘计算核心处理器   |
| **InfoCodeLinkControlHandler** | `local/meos-control-common-edge/handle/InfoCodeLinkControlHandler.java`        | 触发公式验证      |
| **LocalRedisCacheHandler**     | `common/handler/LocalRedisCacheHandler.java`                                   | 配置加载到 Redis |

## 2. 核心计算逻辑

### 2.1 触发公式（triggerFormula）

系统使用两种变体的公式，本质逻辑相同：

**变体1：使用 equipSwitchSet（设备开关设置）**

```javascript
(onlineStatus == nil || onlineStatus != 1) 
  ? -1 
  : (equipSwitchSet == nil 
      ? (runStatus == nil ? -1 : runStatus) 
      : (equipSwitchSet == 0 
          ? (runStatus == nil || runStatus == 0 ? 0 : 3) 
          : (runStatus == nil || runStatus == 0 ? 2 : 1)))
```

**变体2：使用 enableCtrl（启用控制）**

```javascript
(onlineStatus == nil || onlineStatus != 1) 
  ? -1 
  : (enableCtrl == nil 
      ? (runStatus == nil ? -1 : runStatus) 
      : (enableCtrl == 0 
          ? (runStatus == nil || runStatus == 0 ? 0 : 3) 
          : (runStatus == nil || runStatus == 0 ? 2 : 1)))
```

### 2.2 状态映射决策树

```
开始计算 rcRunStatus
    │
    ├─ [第1层] 检查在线状态 (onlineStatus)
    │   ├─ onlineStatus == nil (离线/无数据)
    │   │   └─▶ 返回 -1 (无效)
    │   │
    │   ├─ onlineStatus != 1 (非在线状态)
    │   │   └─▶ 返回 -1 (无效)
    │   │
    │   └─ onlineStatus == 1 (在线) ──▶ 继续第2层
    │
    ├─ [第2层] 检查开关设置 (equipSwitchSet / enableCtrl)
    │   ├─ 值为 nil (无设定值)
    │   │   └─ [第3层] 检查实际运行状态 (runStatus)
    │   │       ├─ runStatus == nil ──▶ 返回 -1 (无效)
    │   │       └─ runStatus 有值 ────▶ 返回 runStatus (0或1)
    │   │
    │   ├─ 值为 0 (设定为关闭)
    │   │   └─ [第3层] 检查实际运行状态 (runStatus)
    │   │       ├─ runStatus == nil ────▶ 返回 0 (停止)
    │   │       ├─ runStatus == 0 ──────▶ 返回 0 (停止)
    │   │       └─ runStatus == 1 ──────▶ 返回 3 (关闭中) ⚡
    │   │
    │   └─ 值为 1 (设定为开启)
    │       └─ [第3层] 检查实际运行状态 (runStatus)
    │           ├─ runStatus == nil ────▶ 返回 2 (开启中) ⚡
    │           ├─ runStatus == 0 ──────▶ 返回 2 (开启中) ⚡
    │           └─ runStatus == 1 ──────▶ 返回 1 (运行)
    │
    └─ 结束
```

### 2.3 状态映射真值表

| onlineStatus | equipSwitchSet | runStatus | **rcRunStatus** | 状态说明       |
|--------------|----------------|-----------|-----------------|------------|
| nil / ≠1     | -              | -         | **-1**          | 无效（设备离线）   |
| 1            | nil            | nil       | **-1**          | 无效（数据缺失）   |
| 1            | nil            | 0         | **0**           | 停止         |
| 1            | nil            | 1         | **1**           | 运行         |
| 1            | 0              | nil       | **0**           | 停止         |
| 1            | 0              | 0         | **0**           | 停止         |
| 1            | 0              | 1         | **3**           | 🔴 **关闭中** |
| 1            | 1              | nil       | **2**           | 🟢 **开启中** |
| 1            | 1              | 0         | **2**           | 🟢 **开启中** |
| 1            | 1              | 1         | **1**           | 运行         |

### 2.4 "开启中/关闭中"的语义

**开启中 (状态2)**：

- 条件：设定值为开启(1)，但实际运行状态还未到达开启(0或nil)
- 场景：下发开启指令后，设备正在启动过程中
- 业务含义：指令已下发，设备响应中，尚未完全启动

**关闭中 (状态3)**：

- 条件：设定值为关闭(0)，但实际运行状态还在运行(1)
- 场景：下发关闭指令后，设备还在运转
- 业务含义：指令已下发，设备响应中，尚未完全停止

## 3. 数据流向

### 3.1 完整数据流

```
阶段1: 配置加载
├─ info_code_config 表
│   └─ 加载 triggerFormulaInfo 到 Redis Hash
│       └─ Redis: infoCodeConfigTrigger:{classCode}
│
阶段2: IoT 数据上报 (MQTT)
├─ 设备上报 equipSwitchSet=1 (开启指令)
│   └─ ComputeIotDataHandler.checkComputeInfoCodeDataCondition()
│       └─ InfoCodeLinkControlHandler.checkTriggerFormulaInfo()
│           └─ 验证配置有效性，处理设备特殊逻辑
│
阶段3: 公式计算执行
├─ getTriggerFormulaInfoCodeValue()
│   ├─ sourceIotType == 0 (控制指令模式)
│   │   └─ checkControlRealStatus()
│   │       ├─ 从 Redis 缓存读取 onlineStatus, runStatus
│   │       ├─ 如果 dataStatus == 2 (进行中)
│   │       │   └─ 直接返回: dataValue=1 ? "2" : "3"
│   │       └─ 否则返回 runStatus (带在线状态校验)
│   │
│   └─ sourceIotType == 1 (表达式模式)
│       └─ 构建变量映射表
│           └─ AviatorEvaluator.execute(triggerFormula, variables)
│
阶段4: 结果处理与推送
├─ computeInfoCodeDataMap()
│   └─ 构造新的 FeedbackInfoMsgMqttVO
│       ├─ infoCode = "rcRunStatus"
│       ├─ dataValue = "2" 或 "3"
│       └─ type = FEEDBACK
│
└─ pushMessage()
    ├─ iotDataCacheService.insert() → 写入 Redis
    └─ iWSMessageService.pushMessage() → 推送到前端 WebSocket
```

### 3.2 关键代码位置

| 组件         | 文件路径                                      | 核心方法                                                 |
|------------|-------------------------------------------|------------------------------------------------------|
| **公式配置**   | `info_code_config.extend_info`            | 数据库存储                                                |
| **配置加载**   | `LocalRedisCacheHandler.java:862-863`     | `hSet(RedisKey.buildInfoCodeConfigTriggerHashKey())` |
| **前置检查**   | `ComputeIotDataHandler.java:90-143`       | `checkComputeInfoCodeDataCondition()`                |
| **配置验证**   | `InfoCodeLinkControlHandler.java:115-155` | `checkTriggerFormulaInfo()`                          |
| **公式计算**   | `ComputeIotDataHandler.java:361-388`      | `getTriggerFormulaInfoCodeValue()`                   |
| **控制状态检查** | `ComputeIotDataHandler.java:397-419`      | `checkControlRealStatus()`                           |
| **表达式执行**  | `ComputeIotDataHandler.java:382`          | `AviatorEvaluator.execute()`                         |

## 4. 业务场景应用

### 4.1 照明需求管理

在 `DemandCommandTask.java` 中，照明需求计算流程：

```
1. 获取当前时间的照明需求场景 (getTodayAllLightingSceneDTO)
   ↓
2. 空间状态计算 (spaceCompute)
   ├─ 全开 (ALL_OPEN)
   ├─ 全关 (ALL_CLOSE)
   ├─ 部分开启 (PARTIAL_OPEN)
   └─ 反馈调节 (FEEDBACK_ADJUSTMENT)
   ↓
3. 生成控制指令 (getEquipRecord)
   ├─ infoCode: 控制信息点
   ├─ infoValue: "1"开启/"0"关闭
   └─ 保存到 equip_record 表
   ↓
4. 指令执行 (commandBatchExecuteService)
   ↓
5. 设备响应，上报状态
   MQTT → equipSwitchSet=1
   触发公式计算 → rcRunStatus=2 (开启中)
   ↓
6. 设备完全启动
   MQTT → runStatus=1
   触发公式计算 → rcRunStatus=1 (运行)
```

### 4.2 特殊设备处理

`InfoCodeLinkControlHandler.checkTriggerFormulaInfo()` 中对特定设备类型进行了特殊处理：

- **离心式冷水机组 (ACCCCC)**：根据联动/单点控制模式选择使用 enableCtrl 或 equipSwitchSet
- **冷却塔控制单元 (ACCCSC)**：单点控制时不进行计算
- **空调换热器 (ACGEHE)**：单点控制时不进行计算

### 4.3 前端展示

前端根据 `rcRunStatus` 的值进行设备染色：

| rcRunStatus | 前端展示  | 颜色 | 业务含义      |
|-------------|-------|----|-----------|
| -1          | 灰色/无效 | ⚪  | 设备离线或数据异常 |
| 0           | 停止    | 🔵 | 设备已停止     |
| 1           | 运行    | 🟢 | 设备正常运行    |
| 2           | 开启中   | 🟡 | 设备正在启动    |
| 3           | 关闭中   | 🟠 | 设备正在停止    |

## 5. 边缘计算 vs SaaS 云部署

### 5.1 架构对比

| 维度       | 边缘部署 (local)            | SaaS 部署 (saas) |
|----------|-------------------------|----------------|
| **触发方式** | MQTT 实时触发               | 定时任务/事件触发      |
| **计算引擎** | AviatorEvaluator        | 可能使用 QLExpress |
| **缓存依赖** | 本地 Redis                | 云 Redis        |
| **延迟**   | 毫秒级                     | 秒级             |
| **离线能力** | ✅ 支持                    | ❌ 依赖网络         |
| **公式配置** | `info_code_config` 表    | 相同的表结构         |
| **核心类**  | `ComputeIotDataHandler` | 可能有不同的实现       |

### 5.2 配置一致性

从 SQL 迁移脚本来看，**两个部署形态使用相同的公式配置**：

```sql
-- local 和 saas 的 SQL 脚本完全一致
"triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : ..."
```

这说明：

1. **计算逻辑是统一的**，只是执行环境不同
2. **配置可以互斥**，边缘和云端共享同一套状态映射规则
3. **差异在于执行时机**：边缘实时计算 vs 云端批量计算

## 6. 技术亮点

### 6.1 表达式引擎选择

系统使用 **Aviator** 作为表达式引擎：

```java
Object execute = AviatorEvaluator.execute(
    triggerFormulaInfo.getTriggerFormula(), 
    infoCodeAndDataValue
);
```

**优势**：

- 轻量级、高性能
- 支持 nil 判断（适合 IoT 场景的数据缺失）
- 三元表达式语法简洁

### 6.2 配置驱动设计

所有的状态计算逻辑都通过**配置**而非硬编码：

```
数据库配置 (triggerFormulaInfo)
    ↓
运行时加载到 Redis
    ↓
动态解析执行
    ↓
结果输出
```

**优势**：

- 修改计算公式无需重新部署
- 不同设备类型可配置不同的公式
- 支持 A/B 测试和灰度发布

### 6.3 状态过渡的精确控制

通过 `sourceIotType` 区分两种计算模式：

```java
if (triggerFormulaInfo.getSourceIotType() == null || 
    triggerFormulaInfo.getSourceIotType() == 0) {
    // 模式0: 控制指令进行中判断 (快速路径)
    return this.checkControlRealStatus(source, triggerFormulaInfo);
} else {
    // 模式1: 信息点表达式计算 (通用路径)
    // 使用 Aviator 执行完整公式
}
```

## 7. 潜在风险与优化建议

### 7.1 风险点

1. **并发问题**
    - `ComputeIotDataHandler` 使用 `ReentrantLock` 防止并发计算
    - 但高并发场景下可能成为瓶颈

2. **缓存一致性**
    - 依赖 Redis 缓存的实时性
    - 如果缓存数据过期，可能导致状态计算错误

3. **公式复杂度**
    - 当前公式已经较复杂，后续维护成本高
    - 缺少公式验证机制

4. **错误处理**
    - 公式执行异常时被捕获但可能被忽略
    - 缺少异常告警机制

### 7.2 优化建议

1. **引入状态机**
    - 将状态转换显式化为状态机，而非隐含在公式中
    - 便于理解和维护

2. **公式版本管理**
    - `triggerFormulaInfo` 应支持版本号
    - 便于回滚和追踪变更历史

3. **监控与告警**
    - 对 `rcRunStatus=2/3` 的持续时间进行监控
    - 如果设备长时间处于"开启中/关闭中"，触发告警
    - 可能表示设备故障或通信异常

4. **公式验证工具**
    - 开发公式语法校验工具
    - 在配置更新前进行验证，防止错误配置上线

5. **单元测试覆盖**
    - 为状态计算逻辑添加完整的单元测试
    - 覆盖所有状态组合的边界情况

## 8. 数据库配置示例

### 8.1 info_code_config 表结构

```json
{
  "view": "1",
  "triggerFormulaInfo": {
    "variables": [
      {"dataType": 0, "infoCode": "onlineStatus"},
      {"dataType": 1, "infoCode": "equipSwitchSet"},
      {"dataType": 0, "infoCode": "runStatus"}
    ],
    "sourceIotType": 0,
    "triggerFormula": "(onlineStatus == nil || onlineStatus != 1) ? -1 : (equipSwitchSet == nil ? (runStatus == nil ? -1 : runStatus) : (equipSwitchSet == 0 ? (runStatus == nil || runStatus == 0 ? 0 : 3) : (runStatus == nil || runStatus == 0 ? 2 : 1)))",
    "triggerInfoCode": "rcRunStatus"
  }
}
```

### 8.2 rcRunStatus 枚举定义

```sql
'[{"code": "-1", "name": "无效"}, 
  {"code": "0", "name": "停止"}, 
  {"code": "1", "name": "运行"}, 
  {"code": "2", "name": "开启中"}, 
  {"code": "3", "name": "关闭中"}]'
```

### 8.3 相关 Flyway 迁移脚本

- `V6.0.0.4__meos_control.sql`
- `V7.250403.0.0__meos_control.sql`
- `V7.251002.0.0__meos_control.sql`
- `V7.251002.0.1__meos_control.sql`

## 9. 总结

MEOS 系统通过**配置驱动的触发公式机制**，实现了灵活的设备状态计算。核心设计思想是：

1. **数据与逻辑分离**：状态计算逻辑存储在配置表中，而非硬编码
2. **实时与批处理结合**：边缘实时计算 + 云端批量计算
3. **精确的状态过渡**：通过对比"设定值"与"实际值"，精确识别过渡状态
4. **高性能表达式引擎**：使用 Aviator 实现毫秒级计算

这种设计在 IoT 监控场景中非常实用，既保证了实时性，又提供了足够的灵活性来适应不同设备类型的状态计算需求。
