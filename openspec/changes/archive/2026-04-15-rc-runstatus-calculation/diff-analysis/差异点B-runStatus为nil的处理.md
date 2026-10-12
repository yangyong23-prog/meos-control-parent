# 差异点 B：runStatus=nil 时的状态返回值

> **优先级**: 🔴 P0  
> **状态**: 待产品确认  
> **关联文件**: `ComputeIotDataHandler.java`，`triggerFormula`（数据库配置）

---

## 一、差异概述

当设备的实际运行状态 `runStatus` 为 `nil`/`null` 时，产品文档与现有代码给出了**完全不同**的结果。这是最核心的业务逻辑差异，直接影响前端设备状态展示。

---

## 二、产品文档要求

产品流程图在"联动控制"和"单点控制"两个分支中，均有明确的第3个分支：

```
联动控制分支3：enableCtrl=1、onlineStatus=1、runStatus 的值为空
→ rcRunStatus = -1（无效）

单点控制分支3：equipSwitchSet=1、onlineStatus=1、runStatus 的值为空
→ rcRunStatus = -1（无效）
```

**产品文档业务语义**：

> 设备在线、控制已启用，但无法获取实际运行状态数据 → 状态不可判断 → 标记为"无效"

对应的计算公式（产品文档期望版本）：

```javascript
if enableCtrl == 1:
    if onlineStatus == 1:
        if runStatus == nil:     // 无值
            rcRunStatus = -1    // 无效
        elif runStatus == 0:    // 停止
            rcRunStatus = 2     // 开启中
        elif runStatus == 1:    // 运行
            rcRunStatus = 1     // 运行
    else:                       // 离线或无值
        rcRunStatus = -1        // 离线
```

---

## 三、现有代码实现

现有代码存在**两条执行路径**，行为不同：

### 路径1：sourceIotType=0（控制指令模式）

**方法**：`ComputeIotDataHandler.checkControlRealStatus()`

```java
private String checkControlRealStatus(FeedbackInfoMsgMqttVO source, TriggerFormulaInfo triggerFormulaInfo) {
    String runStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "runStatus", null);
    String onlineStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "onlineStatus", null);

    // ────────────────────────────────────────────────────────
    // 分支1：进行中（dataStatus == 2）
    if (source.checkProcessData()) {
        // 直接根据 dataValue 返回，不读取 runStatus
        result = "1".equals(source.getDataValue()) ? "2" : "3";
        return result;  // runStatus=nil 时：dataValue=1 → 返回 "2"（开启中）
    }

    // ────────────────────────────────────────────────────────
    // 分支2：完成（dataStatus == 1）
    boolean isOnline = "1".equals(onlineStatus) || "1.0".equals(onlineStatus);
    result = isOnline ? runStatus : "-1";
    // runStatus=null，在线时：result = null（不是"-1"也不是"2"）
    return result;
}
```

**路径1 行为总结**：

| dataStatus | runStatus | 返回值  | 说明                      |
|------------|-----------|------|-------------------------|
| 2（进行中）     | null（未读）  | "2"  | 不依赖runStatus，直接返回       |
| 1（完成）      | null      | null | 返回null，上层不更新rcRunStatus |
| 1（完成）      | "0"       | "0"  | 返回runStatus原值（0=停止）     |
| 1（完成）      | "1"       | "1"  | 返回runStatus原值（1=运行）     |

> **关键发现**：路径1完成状态下，runStatus=null 时返回 `null`，而非 `-1` 或 `2`。返回 `null` 意味着上层调用方不会更新
> rcRunStatus（维持原值），这与产品文档主动推送 `-1` 的行为完全不同。

### 路径2：sourceIotType=1（Aviator 表达式模式）

**触发公式**（数据库配置 `triggerFormula`，以 equipSwitchSet 变体为例）：

```javascript
(onlineStatus == nil || onlineStatus != 1)
  ? -1
  : (equipSwitchSet == nil
      ? (runStatus == nil ? -1 : runStatus)
      : (equipSwitchSet == 0
          ? (runStatus == nil || runStatus == 0 ? 0 : 3)
          : (runStatus == nil || runStatus == 0 ? 2 : 1)))
//            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//            当 equipSwitchSet=1 时：runStatus=nil → 返回 2（开启中）
```

**路径2 行为总结**（equipSwitchSet=1，onlineStatus=1）：

| runStatus | 公式结果       | 说明                         |
|-----------|------------|----------------------------|
| nil       | **2**（开启中） | `runStatus == nil` 满足第一个条件 |
| 0         | **2**（开启中） | `runStatus == 0` 满足第一个条件   |
| 1         | **1**（运行）  | 第二个分支                      |

---

## 四、三方对比

| 场景                        | 产品文档       | 代码路径1（sourceIotType=0）进行中 | 代码路径1（sourceIotType=0）完成 | 代码路径2（sourceIotType=1） |
|---------------------------|------------|---------------------------|--------------------------|------------------------|
| 开关=1，在线，runStatus=**nil** | **-1（无效）** | **2（开启中）**                | **null（不更新）**            | **2（开启中）**             |
| 开关=1，在线，runStatus=0       | 2（开启中）     | 2（开启中）                    | 0（停止）⚠️                  | 2（开启中）                 |
| 开关=1，在线，runStatus=1       | 1（运行）      | 2（开启中）⚠️                  | 1（运行）                    | 1（运行）                  |

> **注意**：路径1进行中模式在 `runStatus=0` 时也返回2，在 `runStatus=1` 时也返回2（因为这时 dataStatus=2
> 表示指令还在执行中，设备虽然在运行但指令还未完成）。

---

## 五、差异的业务影响

### 场景1：设备启动时未上报 runStatus

```
状态：onlineStatus=1，enableCtrl=1，runStatus=缓存中无数据（nil）

产品文档期望：
  rcRunStatus = -1（无效）
  前端显示：灰色/无效

现有代码（路径1进行中）：
  rcRunStatus = 2（开启中）
  前端显示：黄色/开启中

现有代码（路径1完成）：
  rcRunStatus = null，不更新
  前端显示：维持上一次的状态

现有代码（路径2公式）：
  rcRunStatus = 2（开启中）
  前端显示：黄色/开启中
```

**用户体验差异**：

- 产品文档：设备出现在"无效"状态，用户可能误以为设备故障
- 现有代码：设备直接进入"开启中"状态，用户体验更积极

### 场景2：设备持续无 runStatus 数据（设备异常，只上报了控制指令但无反馈）

```
产品文档期望：
  rcRunStatus 持续为 -1（无效），提醒用户设备异常

现有代码（路径1进行中）：
  rcRunStatus = 2（开启中），设备表现为"永远在开启中"
  ← 这是一个潜在问题：若设备故障，状态会长时间显示"开启中"
```

---

## 六、设计文档的描述

`archive/2026-04-14-rc-runstatus-calculation/design.md` 第2.2节状态映射决策树：

```
值为 1 (设定为开启)
  └─ runStatus == nil ────▶ 返回 2 (开启中) ⚡
```

`spec.md` 需求1：

```
4. 如果设定值为 1（开启）：
   - 实际状态为 0 或 nil → 返回 2（开启中）
```

**结论**：设计文档的描述与 `sourceIotType=1`（公式模式）路径一致，与产品文档不一致。

---

## 七、待确认问题

### 问题1：runStatus=nil 的业务语义是什么？

- **产品观点**：无运行数据 = 无法判断状态 = 无效 → 返回 -1
- **现有代码观点**：已下发开启指令但设备未反馈运行数据 = 正在启动 = 开启中 → 返回 2

两种语义均有合理性，**需与产品团队明确确认哪种是业务预期**。

### 问题2：runStatus=null 时"不更新"（路径1完成状态）是否符合预期？

路径1完成状态下 runStatus=null 时返回 null，导致不更新 rcRunStatus（维持原值）。这与产品文档"主动返回 -1"有本质区别，需确认。

---

## 八、建议

### 方案A：遵循产品文档（runStatus=nil → -1）

**修改 triggerFormula（sourceIotType=1 路径）**：

```javascript
// 修改前：
(runStatus == nil || runStatus == 0 ? 2 : 1)

// 修改后（产品文档版本）：
(runStatus == nil ? -1 : (runStatus == 0 ? 2 : 1))
```

**修改 checkControlRealStatus（sourceIotType=0 路径）**：

```java
// 修改前（进行中分支）：
result = "1".equals(source.getDataValue()) ? "2" : "3";

// 修改后：需要读取runStatus，若runStatus=null则返回"-1"
// （但这样会改变"进行中"状态的语义，需谨慎评估）
```

**优点**：与产品文档严格一致，状态明确  
**缺点**：设备启动初期显示"无效"，用户体验较差

### 方案B：保持现有代码（runStatus=nil → 2）

**优点**：用户体验更好，设备启动即显示"开启中"  
**缺点**：若设备异常无法上报runStatus，会长时间显示"开启中"

### 方案C：引入超时机制（折中方案）

```
进入"开启中"状态后：
- N秒内没有收到 runStatus 数据 → 更新为 -1（无效，可能故障）
- N秒内收到 runStatus=1 → 更新为 1（运行）
- N秒内收到 runStatus=0 → 维持 2（仍在开启中）
```

**优点**：平衡用户体验与状态准确性  
**缺点**：增加复杂度，需要定义超时阈值（N秒）

---

**文档版本**: v1.0  
**创建时间**: 2026-04-15  
**基于代码**: `ComputeIotDataHandler.java`（已审核确认）
