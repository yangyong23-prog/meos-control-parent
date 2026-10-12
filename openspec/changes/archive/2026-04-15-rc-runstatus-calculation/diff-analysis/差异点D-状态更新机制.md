# 差异点 D：状态更新机制（开启中状态的后续处理）

> **优先级**: 🟡 P1  
> **状态**: 待规划实现  
> **关联文件**: `ComputeIotDataHandler.java`（现有），待新增定时查询模块

---

## 一、差异概述

产品文档明确要求在设备进入"开启中"（rcRunStatus=2）状态后，**同时启动两种监控机制**保障状态最终一致性。现有代码只有 MQTT
触发路径，**完全没有定时查询机制**。

---

## 二、产品文档要求

### 双重保障机制

产品流程图中，当 rcRunStatus=2（开启中）时：

```
当 rcRunStatus=2 开启中时
      │
      ├──────────────────────────────────────────────────
      │                                                 │
      ▼                                                 ▼
方式一：监听MQTT消息                           方式二：定时查询 runStatus
（实时监控）                                    （兜底补偿）
      │                                                 │
      ├─ runStatus=1（运行）                             ├─ runStatus=1（运行）
      │    → 更新 rcRunStatus=1                         │    → 更新 rcRunStatus=1
      │    → SSE推送                                    │    → 结束定时查询  ✅ 产品图明确
      │                                                 │    → SSE推送
      └─ runStatus=0（关闭）                            └─ 其他情况
           → rcRunStatus=0                                   → 继续定时查询  ⚠️ 逻辑推断
           → SSE推送
```

### 方式一：监听 MQTT 消息

**触发**：当收到设备上报的 `runStatus` 变化 MQTT 消息时

**处理规则**：

| runStatus | 操作                     | 说明          |
|-----------|------------------------|-------------|
| 1（运行）     | 更新 rcRunStatus=1，SSE推送 | 设备已成功启动     |
| 0（关闭）     | rcRunStatus=0，SSE推送    | 设备未启动成功，已关闭 |

### 方式二：定时查询 runStatus

**触发**：rcRunStatus 进入2（开启中）后自动启动

**处理规则**：

| runStatus | 操作                            | 说明              |
|-----------|-------------------------------|-----------------|
| 1（运行）     | 更新 rcRunStatus=1，结束定时查询，SSE推送 | ✅ 产品图明确描述       |
| 其他情况      | 继续定时查询                        | ⚠️ 逻辑推断（产品图未明确） |

**设计意图**：作为 MQTT 消息的补偿机制，防止消息丢失导致状态永久停留在"开启中"。

---

## 三、现有代码实现

### 3.1 MQTT 触发路径

**文件**：`ComputeIotDataHandler.java`

现有代码通过 MQTT 消息触发 rcRunStatus 计算，**功能上覆盖了产品文档的"方式一"**：

```java
// MQTT消息到达时触发
public boolean checkComputeInfoCodeDataCondition(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
    // ... 
    // CTRL 类型（控制指令）→ 触发计算
    if (EnumIotDataType.CTRL.getCode().equals(source.getType()) && triggerFormulaInfo != null) {
        return true;
    }
    // FEEDBACK 类型（设备反馈，包括runStatus变化）→ 完成状态时触发计算
    if (EnumIotDataType.FEEDBACK.getCode().equals(source.getType())) {
        if (source.getDataStatus() == 1) {
            return true;  // ← runStatus 变化的反馈会在这里触发重新计算
        }
    }
    return false;
}
```

当设备上报 `runStatus=1` 的 FEEDBACK 消息时，会触发 rcRunStatus 重新计算，自然从2变为1。**这等价于产品文档的"方式一"**。

### 3.2 定时查询：不存在

在全项目中检索定时查询 runStatus 相关逻辑：

- `ComputeIotDataHandler.java`：无定时逻辑
- `InfoCodeLinkControlHandler.java`：无定时逻辑
- 相关 Task/Scheduled 类：**未找到任何与 rcRunStatus 关联的定时查询机制**

**结论**：**现有代码完全没有实现产品文档要求的定时查询机制。**

---

## 四、差异对比

| 机制             | 产品文档        | 现有代码               | 差异说明              |
|----------------|-------------|--------------------|-------------------|
| MQTT 消息触发更新    | ✅ 方式一       | ✅ 通过 FEEDBACK 消息触发 | **功能等价**，触发方式略有不同 |
| 定时查询 runStatus | ✅ 方式二（明确要求） | ❌ 不存在              | **完全缺失**          |
| 超时兜底机制         | ✅（通过定时查询体现） | ❌ 无任何超时处理          | **缺失**            |

---

## 五、影响分析

### 5.1 MQTT 消息丢失场景

```
设备状态变化流程：
  1. 下发开启指令 → rcRunStatus=2（开启中）
  2. 设备启动成功，上报 runStatus=1
  3. ← 此消息 MQTT 丢失！
  
产品文档（双重保障）：
  - 方式一：消息丢失，不更新
  - 方式二：定时查询发现 runStatus=1，更新为1，结束查询
  - 结果：N秒后状态自动恢复正确

现有代码：
  - 只有方式一：消息丢失，无法更新
  - 结果：设备永久停留在 rcRunStatus=2（开启中）状态
```

**这是现有代码的已知缺陷**，在 MQTT 消息可靠性不稳定的场景下，设备可能长时间显示错误状态。

### 5.2 设备故障场景

```
场景：下发开启指令后，设备故障，既无 runStatus 更新，也无其他消息

产品文档（双重保障）：
  - 定时查询持续运行，但 runStatus 始终不为1
  - 继续显示"开启中"（⚠️ 逻辑推断，产品未明确超时处理）

现有代码：
  - 同样显示"开启中"，无超时处理
  - 行为与产品文档一致（但都没有超时处理）
```

---

## 六、定时查询机制的技术设计思路

若决定实现定时查询，以下是参考设计方案：

### 方案1：基于 Spring 延迟任务

```java
// 当 rcRunStatus 变为 2 时，注册延迟检查任务
@Async
public void scheduleRunStatusCheck(String objectId, String projectId) {
    int maxRetries = 10;    // 最大查询次数
    int intervalSeconds = 5; // 查询间隔（秒）
    
    for (int i = 0; i < maxRetries; i++) {
        Thread.sleep(intervalSeconds * 1000);
        
        // 查询当前 rcRunStatus
        String currentRcRunStatus = iotDataCacheService.queryIotRealData(objectId, "rcRunStatus", null);
        if (!"2".equals(currentRcRunStatus)) {
            // 已经不是"开启中"，停止查询
            return;
        }
        
        // 查询 runStatus
        String runStatus = iotDataCacheService.queryIotRealData(objectId, "runStatus", null);
        if ("1".equals(runStatus)) {
            // 设备已运行，更新状态
            updateRcRunStatus(objectId, "1");
            return;  // 结束查询
        }
    }
    // 超过最大重试次数，可考虑设为-1或记录告警
}
```

### 方案2：基于 Redis 的分布式延迟队列

```
1. rcRunStatus=2 时，写入 Redis 延迟队列：
   key: pending_check:{objectId}
   ttl: 查询间隔（如5秒）

2. 定时任务扫描 pending_check:* key
   → 到期则查询 runStatus
   → runStatus=1：更新状态，删除key
   → 其他：续期key，继续等待

3. 达到最大等待时间，删除key并告警
```

### 方案3：利用现有 MQTT 覆盖（最小实现）

**最小代价方案**：不引入定时查询，而是确保 MQTT 消息的可靠性：

- 增加 MQTT 消息的持久化配置（QoS=1/2）
- 增加消息去重机制
- 上报端增加定时心跳上报 runStatus

---

## 七、待确认问题

### 问题1：定时查询的间隔和超时时间是多少？

- 设备从下发指令到启动完成通常需要多长时间？
- 查询间隔建议多少秒？
- 最多等待多久？超时后的处理策略是什么？

### 问题2：定时查询的数据来源是什么？

- 是查询 Redis 中的实时缓存？
- 还是重新发请求给设备/中台？
- 是否有现成的 API 可以主动查询 runStatus？

### 问题3：MQTT 消息可靠性的当前情况如何？

- 是否已经有消息丢失的线上问题反馈？
- 消息 QoS 配置是多少？

---

## 八、建议

### 近期（P1）

1. **确认线上是否有设备长时间停留在"开启中"状态的问题**：通过日志 `[rcRunStatus-计算]` 搜索设备一直显示2的情况
2. **与产品团队确认定时查询的具体参数**：间隔、超时、超时后的处理策略

### 中期

3. **实现定时查询机制**：建议采用方案1（Spring延迟任务），与现有代码集成最简单
4. **增加 rcRunStatus=2 的持续时间监控告警**：若超过阈值，触发告警（design.md中也有此建议）

---

**文档版本**: v1.0  
**创建时间**: 2026-04-15  
**基于代码**: `ComputeIotDataHandler.java`（已审核确认，无定时查询）
