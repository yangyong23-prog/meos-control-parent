# 差异点 A：rcRunStatus 计算触发条件

> **优先级**: 🔴 P0  
> **状态**: 待产品确认  
> **关联文件**: `ComputeIotDataHandler.java`

---

## 一、差异概述

产品文档明确定义了三个前置条件，**全部满足**才触发 rcRunStatus 计算。而现有代码以 MQTT 消息类型（CTRL/FEEDBACK）和
dataStatus 为判断依据，**不包含** `status=0 AND result=0` 的限制，触发范围更宽。

---

## 二、产品文档要求

产品流程图（rcRunStatus计算逻辑.png）定义的三个必要条件：

```
收到MQTT孪生消息
       │
       ▼
  同时满足以下三个条件？
  ├─ 条件1：classCode 在计算范围内（rc_info_code_cfg）
  ├─ 条件2：enableCtrl=1（联动启用）OR equipSwitchSet=1（单点开启）
  └─ 条件3：status=0（开始执行）AND result=0（进行中）
       │
  ─────┴─────────────────────────────
  是 → 进行"开启中"计算        否 → 结束，不计算
```

### 条件1：classCode 在计算范围内

- **含义**：设备类型代码必须在 `rc_info_code_cfg` 表配置的计算范围内
- **目的**：只对需要计算运行状态的设备类型进行计算，不处理所有设备

### 条件2：enableCtrl=1 OR equipSwitchSet=1

- **含义**：设备必须处于已启用/已开启状态
- **目的**：只对已受控的设备计算运行状态
- **逻辑**：满足任一条件即可（OR 关系）

### 条件3：status=0 AND result=0

- **含义**：MQTT消息中的 `status=0`（开始执行）且 `result=0`（进行中）
- **目的**：仅在"开启指令正在执行"过程中触发计算
- **关键**：这是将计算限定为"开启指令进行中"的核心条件
- **注意**：这是设备**反馈**的执行状态，不是下发指令本身

---

## 三、现有代码实现

### 代码路径

**文件**：`ComputeIotDataHandler.java`，方法：`checkComputeInfoCodeDataCondition()`

```java
public boolean checkComputeInfoCodeDataCondition(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
    // 手自动设定信息点为自动时，特殊处理
    if (checkManualAutoSetIsAuto(source)) {
        return true;
    }
    
    // 基础前置检查（有 infoCodeCfg 且数据不为空）
    if (infoCodeCfg == null || StrUtil.isBlank(source.getDataValue()) 
        || source.getDataStatus() == null 
        || CommonConst.STRING_VALUE_INFO_CODES.contains(source.getInfoCode())) {
        return false;
    }

    TriggerFormulaInfo triggerFormulaInfo = infoCodeCfg.getExtendInfo() == null 
        ? null : infoCodeCfg.getExtendInfo().getTriggerFormulaInfo();

    // ──────────────────────────────────────────────────────────
    // 孪生数据（FEEDBACK类型）的判断
    if (EnumIotDataType.FEEDBACK.getCode().equals(source.getType())) {
        if (source.getDataStatus() != 1) {  // ← 仅 dataStatus=1（完成）才继续
            return false;
        }
        // 空气源热泵特殊处理
        boolean heatGroup = "ACAHC".equals(source.getClassCode()) || "ACCCAH".equals(source.getClassCode());
        if (heatGroup && InfoCodeEnum.RUN_STATUS.getCode().equals(source.getInfoCode())) {
            return true;
        }
    }

    // ──────────────────────────────────────────────────────────
    // 控制数据（CTRL类型）的判断
    if (EnumIotDataType.CTRL.getCode().equals(source.getType()) && triggerFormulaInfo != null) {
        // 防止完成状态的消息先于进行中状态到达
        FeedbackInfoMsgMqttVO beforeData = this.iotDataCacheService.queryIotCtrlDataVO(...);
        if (beforeData != null && beforeData.getId().equals(source.getId()) 
            && beforeData.getDataStatus() != null && beforeData.getDataStatus() == 2) {
            throw new BusinessException(...);  // 丢弃乱序消息
        }
        return true;  // ← CTRL类型有配置直接计算
    }
    
    return false;
}
```

### 代码触发逻辑

```
收到MQTT消息
     │
     ├─ [CTRL类型控制消息]
     │   有 triggerFormulaInfo 配置？
     │   ├─ 是 → ✅ 触发计算（无论 status/result 是什么）
     │   └─ 否 → ❌ 不计算
     │
     └─ [FEEDBACK类型反馈消息]
         dataStatus == 1（完成）？
         ├─ 是 → ✅ 触发计算
         └─ 否 → ❌ 不计算（进行中的反馈不计算）
```

---

## 四、差异对比

### 条件对比表

| 条件                               | 产品文档                       | 现有代码                             | 差异类型                  |
|----------------------------------|----------------------------|----------------------------------|-----------------------|
| classCode 在计算范围                  | ✅ 明确前置条件（rc_info_code_cfg） | ✅ 通过有无 triggerFormulaInfo 配置隐式实现 | **逻辑等价**，实现方式不同       |
| enableCtrl=1 OR equipSwitchSet=1 | ✅ 明确前置过滤条件                 | ⚠️ 不作前置过滤，包含在公式计算内部处理            | **不同**：时机不同（前置 vs 后置） |
| status=0 AND result=0            | ✅ 核心限制条件                   | ❌ 无此判断                           | **缺失**                |
| 消息类型处理                           | 不区分类型，统一三条件判断              | 区分 CTRL/FEEDBACK 两条路径            | **实现路径不同**            |

### 触发场景对比

| 场景                                             | 产品文档          | 现有代码               | 结果差异   |
|------------------------------------------------|---------------|--------------------|--------|
| 开启指令（CTRL，dataValue=1，dataStatus=2进行中）         | ✅ 计算（满足条件3）   | ✅ 计算（CTRL类型）       | **一致** |
| 开启指令（CTRL，dataValue=1，dataStatus=1完成）          | ❌ 不计算（条件3不满足） | ✅ 计算（CTRL类型）       | **不同** |
| 设备反馈运行状态（FEEDBACK，runStatus=1，dataStatus=1）    | ❌ 不计算（条件3不满足） | ✅ 计算（FEEDBACK类型完成） | **不同** |
| 设备在线状态变化（FEEDBACK，onlineStatus=1，dataStatus=1） | ❌ 不计算         | ✅ 计算（FEEDBACK类型完成） | **不同** |
| 设备开关状态变化（CTRL，equipSwitchSet=0，dataStatus=2）   | ❌ 不计算（条件2不满足） | ✅ 计算（CTRL类型）       | **不同** |

---

## 五、影响分析

### 5.1 计算频率

- **产品文档**：仅在"开启指令进行中"时计算，频率最低
- **现有代码**：任何相关 CTRL/FEEDBACK 消息都触发，频率更高

**性能影响**：现有代码计算次数更多，但每次计算耗时极短（读 Redis + 简单运算），影响有限。

### 5.2 状态更新时机

产品文档流程只在开启指令时计算，后续的状态更新由"监听MQTT消息"和"定时查询"
两种机制完成（见[差异点D](./差异点D-状态更新机制.md)）。

现有代码在设备状态反馈消息（runStatus、onlineStatus等变化）时也会触发计算，**某种程度上覆盖了产品文档中"监听MQTT消息"更新状态的需求
**。

### 5.3 条件2（enableCtrl/equipSwitchSet 前置过滤）的缺失

现有代码不做前置过滤，而是把 `enableCtrl/equipSwitchSet` 的值包含在公式计算中处理。这在功能上是等价的，只是结构不同：

- **产品文档方式**：不满足条件2 → 不触发计算
- **现有代码方式**：触发计算，但公式中 `enableCtrl=0`（关闭）时输出 0 或 3，不输出 2（开启中）

最终结果并不完全等价：产品文档要求 `enableCtrl=0` 时"不计算"（rcRunStatus 不变），而现有代码会将 rcRunStatus **更新为 0 或 3
**（关闭指令路径）。

### 5.4 条件3（status/result）缺失的影响

目前不确定 MQTT 消息中是否包含 `status` 和 `result` 字段。若：

- **有这两个字段**：现有代码未做限制，可能在非开启指令时错误触发计算
- **无这两个字段**：产品文档的条件3可能是设计上的约束，实际通过 CTRL/FEEDBACK 分类替代实现

---

## 六、深入分析：status/result 字段在代码中的处理

### 6.1 MQTT 消息字段定义

`FeedbackInfoMsgMqttVO` 中的字段定义：

```java
// 第96-98行：dataStatus（JSON别名：status）
@Schema(description = "数据状态 反馈值：数据状态 1-正常，2-异常   控制值状态：  0:开始执行，1:执行中，2完成，3:保护期 ")
@JsonAlias({"dataStatus", "status"})
private Integer dataStatus;

// 第108-110行：result
@Schema(description = "控制值状态  0:未完成，1:成功,2:失败，3:超时，4:强制终止，5:控制异常")
@JsonAlias({"result"})
private Integer result;
```

**关键发现**：MQTT 消息中的 `status` 字段会映射到 Java 的 `dataStatus` 字段，代码中的 `status` 实际上是 `dataStatus`。

### 6.2 checkProcessData() 的实际判断逻辑

```java
// FeedbackInfoMsgMqttVO.java 第243-245行
public boolean checkProcessData() {
    return type == 1 && result == 0 && (dataStatus == 1) && ("8".equals(cmdLevel) || "12".equals(cmdLevel));
}
```

判断条件解析：

| 条件                | 含义          | 对应产品文档                   |
|-------------------|-------------|--------------------------|
| `type == 1`       | 控制指令类型      | 等价                       |
| `result == 0`     | 进行中（代码：未完成） | 等价                       |
| `dataStatus == 1` | 执行中         | **不等于** `status=0`（开始执行） |
| `cmdLevel`        | 控制等级        | 无对应                      |

### 6.3 字段值映射对比

| 字段       | 产品文档含义 | 代码中实际值          | 映射关系 |
|----------|--------|-----------------|------|
| `status` | 0=开始执行 | `dataStatus=0`  | 直接对应 |
| `status` | 1=执行中  | `dataStatus=1`  | 直接对应 |
| `status` | 2=完成   | `dataStatus=2`  | 直接对应 |
| `result` | 0=进行中  | `result=0`（未完成） | 语义等价 |
| `result` | 1=成功   | `result=1`（成功）  | 直接对应 |

### 6.4 关键差异：代码未检查 status=0（开始执行）

**产品文档要求**：

```
status=0（开始执行） AND result=0（进行中）
```

**代码实际**（`checkProcessData()`）：

```java
dataStatus == 1（执行中） AND result == 0（进行中）
```

**差异分析**：

- 代码要求 `dataStatus == 1`（执行中）
- 产品文档要求 `status == 0`（开始执行）
- **两者不匹配**：代码跳过了"开始执行"阶段，直接检查"执行中"状态

这意味着：

- **产品文档**：只在指令**刚开始下发**时触发（status=0）
- **代码**：在指令**正在执行中**时触发（dataStatus=1）

### 6.5 checkComputeInfoCodeDataCondition 中的实际判断

在 `checkComputeInfoCodeDataCondition()` 方法中：

```java
// CTRL类型：有配置则直接计算（无status/result检查）
if (EnumIotDataType.CTRL.getCode().equals(source.getType()) && triggerFormulaInfo != null) {
    return true;  // 无任何 status/result 条件检查
}
```

**结论**：整个计算流程中，**没有检查 `status=0`（开始执行）**的条件。

---

## 七、影响分析

### 7.1 计算频率

- **产品文档**：仅在"开启指令开始执行"时计算，频率最低
- **现有代码**：任何相关 CTRL/FEEDBACK 消息都触发，频率更高

### 7.2 状态更新时机

产品文档流程只在开启指令时计算，后续的状态更新由"监听MQTT消息"和"定时查询"
两种机制完成（见[差异点D](./差异点D-状态更新机制.md)）。

现有代码在设备状态反馈消息（runStatus、onlineStatus等变化）时也会触发计算，**某种程度上覆盖了产品文档中"监听MQTT消息"更新状态的需求
**。

### 7.3 条件2（enableCtrl/equipSwitchSet 前置过滤）的缺失

现有代码不做前置过滤，而是把 `enableCtrl/equipSwitchSet` 的值包含在公式计算中处理。

最终结果并不完全等价：产品文档要求 `enableCtrl=0` 时"不计算"（rcRunStatus 不变），而现有代码会将 rcRunStatus **更新为 0 或 3
**（关闭指令路径）。

---

## 八、待确认问题

### 问题1：MQTT 消息中是否有独立的 status 和 result 字段？

根据代码分析：

- `status` 字段映射到 `dataStatus`（开始执行/执行中/完成）
- `result` 字段映射到 `result`（未完成/成功/失败）

产品文档中的 `status=0 AND result=0` 在代码中的对应关系：

- `status=0` → `dataStatus=0`（开始执行）
- `result=0` → `result=0`（进行中/未完成）

### 问题2：为什么代码检查的是 dataStatus=1 而非 dataStatus=0？

可能的原因：

1. 代码认为"开始执行"状态的消息可能不可靠或会被覆盖
2. 代码更关注"正在执行中"的状态判断
3. 产品文档与代码实现之间存在设计偏差

### 问题3：enableCtrl=0/equipSwitchSet=0 时是否应该更新 rcRunStatus？

- **产品文档**：条件2不满足，不触发计算，rcRunStatus 保持不变
- **现有代码**：触发计算，公式会输出 0（停止）或 3（关闭中），主动更新状态

---

## 九、建议

### 方案1：保持现有代码，完善产品文档

**理由**：

- 现有代码的宽触发策略使状态更新更及时
- 在实际运行中已被验证，稳定可靠
- 符合"状态驱动"的实时计算理念

**需要**：

- 与产品团队对齐，确认现有行为符合业务预期
- 补充关闭场景（条件2不满足时）的状态更新说明

### 方案2：新增 status=0 前置过滤（与产品文档严格对齐）

**实现成本**：中等（需确认 MQTT 消息字段，修改 `checkComputeInfoCodeDataCondition`）

**风险**：

- 可能减少触发频率，但也可能遗漏某些状态更新场景
- 需要补充"定时查询"机制作为补偿（见[差异点D](./差异点D-状态更新机制.md)）

**推荐**：与产品团队确认后再决定。

---

**文档版本**: v1.1（补充 status/result 字段深入分析）  
**创建时间**: 2026-04-15  
**基于代码**: `ComputeIotDataHandler.java`、`FeedbackInfoMsgMqttVO.java`（已审核确认）
