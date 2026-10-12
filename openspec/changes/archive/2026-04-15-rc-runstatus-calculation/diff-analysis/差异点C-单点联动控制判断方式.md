# 差异点 C：单点/联动控制的判断方式

> **优先级**: 🟡 P1  
> **状态**: 待评估是否重构  
> **关联文件**: `InfoCodeLinkControlHandler.java`，`triggerFormula`（数据库配置）

---

## 一、差异概述

产品流程图中存在一个**显式的判断节点**，将计算流程分叉为"联动控制"和"单点控制"两条路径。而现有代码通过公式内容和 Redis
缓存隐式决定控制方式，没有明确的业务层分支。

---

## 二、产品文档要求

产品流程图（rcRunStatus计算逻辑.png）中的明确分叉：

```
D[进行"开启中"计算]
     │
     ▼
E{判断控制方式}
     │
     ├─ 联动控制 ──→ F[用 enableCtrl 信息点]
     │                  ├─ 获取：enableCtrl, runStatus, onlineStatus
     │                  └─ 按4个分支计算 rcRunStatus
     │
     └─ 单点控制 ──→ G[用 equipSwitchSet 信息点]
                        ├─ 获取：equipSwitchSet, runStatus, onlineStatus
                        └─ 按4个分支计算 rcRunStatus（规则与联动控制相同）
```

### 两条路径的信息点差异

| 控制方式 | 使用信息点            | 计算规则                  |
|------|------------------|-----------------------|
| 联动控制 | `enableCtrl`     | 相同：在线+值=1+runStatus判断 |
| 单点控制 | `equipSwitchSet` | 相同：在线+值=1+runStatus判断 |

> **注意**：两条路径的计算规则完全相同，只是使用的信息点名称不同。

### 判断依据

产品流程图中**未明确说明**如何判断是"单点控制"还是"联动控制"，仅绘制了两个分叉路径。这是需要与产品团队确认的悬而未决问题。

---

## 三、现有代码实现

### 3.1 普通设备：通过公式内容隐式判断

现有代码不做显式判断，而是通过数据库配置的 `triggerFormula` 内容来决定使用哪个信息点：

```javascript
// 联动控制公式（使用 enableCtrl）
(onlineStatus == nil || onlineStatus != 1)
  ? -1
  : (enableCtrl == nil
      ? (runStatus == nil ? -1 : runStatus)
      : (enableCtrl == 0
          ? (runStatus == nil || runStatus == 0 ? 0 : 3)
          : (runStatus == nil || runStatus == 0 ? 2 : 1)))

// 单点控制公式（使用 equipSwitchSet，结构完全相同）
(onlineStatus == nil || onlineStatus != 1)
  ? -1
  : (equipSwitchSet == nil
      ? (runStatus == nil ? -1 : runStatus)
      : (equipSwitchSet == 0
          ? (runStatus == nil || runStatus == 0 ? 0 : 3)
          : (runStatus == nil || runStatus == 0 ? 2 : 1)))
```

**数据库中的配置策略**（`info_code_config.extend_info.triggerFormulaInfo`）：

- 联动控制设备：配置含 `enableCtrl` 变量的公式
- 单点控制设备：配置含 `equipSwitchSet` 变量的公式

**实际判断逻辑（sourceIotType=0 模式）**：

当走 `checkControlRealStatus()` 方法时，代码并不直接区分单点/联动，而是：

```java
// 直接读取 runStatus 和 onlineStatus，不关心是哪种控制方式
String runStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "runStatus", null);
String onlineStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "onlineStatus", null);
// 进行中：直接返回2或3（不区分控制方式）
// 完成：在线则返回runStatus，否则返回-1
```

单点/联动的区分**实际发生在触发时机**：MQTT消息上报的是 `equipSwitchSet` 还是 `enableCtrl`，由此决定触发哪个 `infoCodeCfg`
配置，从而间接决定用哪个公式。

### 3.2 特殊设备：通过 Redis 强控缓存判断

**文件**：`InfoCodeLinkControlHandler.java`，方法：`checkTriggerFormulaInfo()`

对于特殊设备类型（ACCCCC、ACCCSC），代码会额外检查当前设备的联动/单点状态：

```java
if ("ACCCCC".equals(infoCodeCfg.getClassCode())) {
    // 从 Redis 读取强控缓存（由业务逻辑写入）
    String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
    
    if (("1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("enableCtrl"))
            || (!"1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("equipSwitchSet"))) {
        // 联动控制时，只计算含 enableCtrl 的公式
        // 单点控制时，只计算含 equipSwitchSet 的公式
        return null;  // 公式与控制方式不匹配，跳过
    }
}
```

**强控缓存的来源**：`RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCode)`，由其他业务逻辑（如切换控制模式时）写入。

---

## 四、差异对比

| 维度     | 产品文档        | 现有代码                                        | 差异说明             |
|--------|-------------|---------------------------------------------|------------------|
| 判断位置   | 业务层显式分支     | 配置层隐式（公式内容匹配）                               | 抽象层次不同           |
| 判断依据   | 未明确（待确认）    | 公式变量名 + Redis强控状态（特殊设备）                     | 代码有明确实现，但与产品图对不上 |
| 代码可读性  | 直观（两个独立分支）  | 隐晦（需阅读公式内容才能理解）                             | 现有代码维护成本较高       |
| 控制方式切换 | 产品文档未说明切换逻辑 | 通过 `resetObjectTriggerInfoCodeValue()` 重置缓存 | 现有代码有完整实现        |

---

## 五、普通设备与特殊设备的处理差异

| 设备类型              | 判断机制                                   | 说明             |
|-------------------|----------------------------------------|----------------|
| 普通设备              | 公式内容隐式决定（equipSwitchSet or enableCtrl） | 触发哪个信息点，就用哪个公式 |
| `ACCCCC`（离心式冷水机组） | 公式内容 + Redis强控缓存共同判断                   | 两者必须一致，否则跳过计算  |
| `ACCCSC`（冷却塔控制单元） | 同 ACCCCC                               | 同上             |
| `ACOTU`（冷却塔）      | Redis强控缓存（enableCtrl=1时跳过）             | 联动控制时不计算       |
| `ACGEHE`（空调换热器）   | Redis强控缓存（equipSwitchSet=1时跳过）         | 单点控制时不计算       |

---

## 六、影响分析

### 6.1 功能等价性

在大多数场景下，现有代码的隐式判断与产品文档的显式判断**功能等价**：

- 配置了 enableCtrl 公式的设备 = 联动控制
- 配置了 equipSwitchSet 公式的设备 = 单点控制

### 6.2 潜在问题

1. **公式配置错误时**：若数据库中某设备被错误配置了"联动公式"但实际是单点控制设备，代码无法感知错误，会用错误的信息点计算
2. **控制方式动态变化时**：若一个设备在运行时从联动切换到单点（反之亦然），需要同时更新数据库公式配置，而不能像产品文档那样只改一个状态标志
3. **代码维护难度**：新接手的开发人员很难从代码直接理解"单点/联动分支"逻辑，需要看数据库配置

### 6.3 特殊设备的双重检查

ACCCCC 等特殊设备增加了"公式内容+Redis缓存双重检查"，这意味着：

- 数据库公式和Redis强控状态必须同步
- 若不同步，会导致跳过计算，rcRunStatus 不更新

---

## 七、待确认问题

### 问题1：如何判断一个设备是单点控制还是联动控制？

产品图有显式分叉，但**未说明判断依据**。可能的方案：

- **方案A**：由配置表字段决定（如新增 `controlMode` 字段）
- **方案B**：由公式内容决定（现有代码的方式）
- **方案C**：由设备当前的运行状态决定（动态判断）

### 问题2：控制方式切换时，如何同步更新？

目前切换时需要同时更新 Redis 缓存（通过 `resetObjectTriggerInfoCodeValue`），是否还需要更新数据库配置？

---

## 八、建议

### 方案1：保持现有隐式方式（推荐的最小变更路径）

**理由**：

- 功能上等价于产品文档
- 已在线上稳定运行
- 重构收益有限，风险较大

**需要做的**：

- 增加代码注释，说明"公式内容决定控制方式"的隐式逻辑
- 在 `info_code_config` 配置文档中补充控制方式与公式变量的对应关系

### 方案2：引入显式 controlMode 配置字段（长期重构方案）

**实现思路**：

```java
// 在 rc_info_code_cfg 中新增 controlMode 字段
// "LINKAGE" = 联动控制，"SINGLE" = 单点控制

String controlMode = infoCodeCfg.getControlMode();
if ("SINGLE".equals(controlMode)) {
    infoCodeKey = "equipSwitchSet";
} else {
    infoCodeKey = "enableCtrl";
}
```

**优点**：

- 与产品文档显式分支对齐
- 代码语义清晰，易于维护
- 控制方式切换只需修改一个字段

**缺点**：

- 需要数据库字段变更
- 需要迁移现有数据
- 开发工作量较大

---

**文档版本**: v1.0  
**创建时间**: 2026-04-15  
**基于代码**: `InfoCodeLinkControlHandler.java`（已审核确认）
