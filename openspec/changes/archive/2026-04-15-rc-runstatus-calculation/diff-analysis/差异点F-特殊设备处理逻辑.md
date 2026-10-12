# 差异点 F：特殊设备处理逻辑

> **优先级**: 🟡 P1  
> **状态**: 代码已有完整实现，设计文档描述不完整/有误；产品流程图未涉及  
> **关联文件**: `InfoCodeLinkControlHandler.java`

---

## 一、差异概述

产品流程图**未涉及**特殊设备的差异化处理逻辑（产品文档 v2.0 已删除代码分析补充内容）。本差异点记录的是*
*设计文档（archive/design.md）与代码实现之间的差异**。

现有代码中 4 种特殊设备有各自不同的跳过计算条件，本文档基于实际代码进行准确描述。

---

## 二、设计文档（archive/design.md）描述

`design.md` 第4.2节对4种特殊设备的描述：

| 设备类型   | design.md 描述                                |
|--------|---------------------------------------------|
| ACCCCC | 根据联动/单点控制模式选择使用 enableCtrl 或 equipSwitchSet |
| ACCCSC | **单点控制时不进行计算**                              |
| ACOTU  | **未提及**                                     |
| ACGEHE | **单点控制时不进行计算**                              |

---

## 三、现有代码实际逻辑

### 代码位置

**文件**：`InfoCodeLinkControlHandler.java`，方法：`checkTriggerFormulaInfo()`

```java
public TriggerFormulaInfo checkTriggerFormulaInfo(String objectId, RcInfoCodeCfg infoCodeCfg) {
    // ... 基础检查 ...
    
    if ("ACCCCC".equals(infoCodeCfg.getClassCode())) { ... }
    else if ("ACCCSC".equals(infoCodeCfg.getClassCode())) { ... }
    else if ("ACOTU".equals(infoCodeCfg.getClassCode())) { ... }
    else if ("ACGEHE".equals(infoCodeCfg.getClassCode())) { ... }
    
    // 通过检查，返回 triggerFormulaInfo
    return triggerFormulaInfo;
}
```

### 各设备详细逻辑

#### 设备1：ACCCCC（离心式冷水机组）

```java
if ("ACCCCC".equals(infoCodeCfg.getClassCode())) {
    String enableCtrl = getEnableCtrl(objectId, "enableCtrl");  // 从Redis读取强控状态
    
    if (("1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("enableCtrl"))
            || (!"1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("equipSwitchSet"))) {
        return null;  // 跳过计算
    }
}
```

**逻辑说明**：

| enableCtrl 值 | 公式包含             | 动作     | 解释                             |
|--------------|------------------|--------|--------------------------------|
| 1（联动启用）      | `enableCtrl`     | **跳过** | 联动控制时，含 enableCtrl 的公式跳过       |
| 1（联动启用）      | `equipSwitchSet` | **计算** | 联动控制时，含 equipSwitchSet 的公式继续计算 |
| ≠1（非联动）      | `enableCtrl`     | **计算** | 非联动控制时，含 enableCtrl 的公式继续计算    |
| ≠1（非联动）      | `equipSwitchSet` | **跳过** | 非联动控制时，含 equipSwitchSet 的公式跳过  |

> **理解**：enableCtrl=1 时，只有**不含** enableCtrl 的公式（含 equipSwitchSet 的）才计算；enableCtrl≠1 时，只有**不含**
> equipSwitchSet 的公式（含 enableCtrl 的）才计算。即：**联动时用单点公式，非联动时用联动公式**。

**注意**：这个逻辑看起来比较反直觉，实际含义是：若当前控制状态与公式类型**相同**则跳过，**不同**
则计算。可能的业务意图是：只在控制状态"刚切换"时（不一致时）才触发计算。

#### 设备2：ACCCSC（冷却塔控制单元）

```java
} else if ("ACCCSC".equals(infoCodeCfg.getClassCode())) {
    String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
    
    if (("1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("enableCtrl"))
            || (!"1".equals(enableCtrl) && triggerFormulaInfo.getTriggerFormula().contains("equipSwitchSet"))) {
        return null;  // 跳过计算
    }
}
```

**逻辑与 ACCCCC 完全相同。**

#### 设备3：ACOTU（冷却塔）

```java
} else if ("ACOTU".equals(infoCodeCfg.getClassCode())) {
    String enableCtrl = getEnableCtrl(objectId, "enableCtrl");
    
    if ("1".equals(enableCtrl)) {
        return null;  // enableCtrl=1 时，跳过计算
    }
}
```

**逻辑说明**：

| enableCtrl 值 | 动作     | 解释                    |
|--------------|--------|-----------------------|
| 1（联动启用）      | **跳过** | 联动控制时，不计算 rcRunStatus |
| ≠1（非联动）      | **计算** | 非联动控制时，正常计算           |

> **产品文档第5节描述"单点优先，公式匹配判断"有误**。实际逻辑是：联动控制（enableCtrl=1）时完全跳过，没有公式匹配判断。

#### 设备4：ACGEHE（空调换热器）

```java
} else if ("ACGEHE".equals(infoCodeCfg.getClassCode())) {
    String enableCtrl = getEnableCtrl(objectId, "equipSwitchSet");  // 注意：这里读的是 equipSwitchSet
    
    if ("1".equals(enableCtrl)) {
        return null;  // equipSwitchSet=1 时，跳过计算
    }
}
```

**逻辑说明**：

| equipSwitchSet 值 | 动作     | 解释                    |
|------------------|--------|-----------------------|
| 1（单点开启）          | **跳过** | 单点控制时，不计算 rcRunStatus |
| ≠1（非单点）          | **计算** | 非单点控制时，正常计算           |

> **产品文档第5节描述"联动优先，公式匹配判断"有误**。实际逻辑是：单点控制（equipSwitchSet=1）时完全跳过，且读取的是
`equipSwitchSet` 的缓存，不是 `enableCtrl`。

---

## 四、强控状态缓存的来源

`getEnableCtrl()` 方法：

```java
private String getEnableCtrl(String objectId, String infoCode) {
    // 优先从 Redis 缓存读取
    String enableCtrl = this.redisCacheUtil.get(RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCode));
    
    // Redis 无数据时，从数据库查询
    if (StringUtils.isEmpty(enableCtrl)) {
        RcBasicsConfigQueryDaoParam param = new RcBasicsConfigQueryDaoParam();
        param.setProjectId(ContextUtil.getProjectId());
        param.setFacilityCode(objectId);
        param.setType(BasicsConfigTypeEnum.FORCE_CONTROL_STATUS);
        RcBasicsConfig cfg = rcBasicsConfigService.getValid(param);
        if (cfg != null && OnOffStateEnum.OPEN.getValue().equals(cfg.getResult())) {
            enableCtrl = "1";
        }
    }
    return enableCtrl;
}
```

**缓存写入时机**：切换控制模式时（如从联动切换到单点），业务代码会更新 Redis 缓存 `buildObjectInfoCodeForcedKey`。

---

## 五、差异对比

### 设备代码准确性（代码 vs 设计文档/原描述）

| 设备      | 原描述写法¹     | 代码实际            | 是否一致   |
|---------|------------|-----------------|--------|
| 离心式冷水机组 | ACCCC（4个C） | **ACCCCC**（5个C） | ❌ 拼写错误 |
| 冷却塔控制单元 | ACCCSC     | ACCCSC          | ✅ 一致   |
| 冷却塔     | ACOTU      | ACOTU           | ✅ 一致   |
| 空调换热器   | ACGEHE     | ACGEHE          | ✅ 一致   |

> ¹ 原产品文档 v1.2 第5节（代码分析补充内容，已在 v2.0 删除）中的拼写错误。

### 跳过逻辑准确性（代码 vs 设计文档）

| 设备     | design.md 描述     | 代码实际逻辑                  | 准确性              |
|--------|------------------|-------------------------|------------------|
| ACCCCC | 根据联动/单点模式选择信息点   | 控制状态与公式不匹配时才计算          | ⚠️ 表述角度不同，指向同一逻辑 |
| ACCCSC | **“单点控制时不进行计算”** | 公式不匹配时跳过（联动单点均可能跳过）     | ❌ 描述不准确          |
| ACOTU  | **未提及**          | enableCtrl=1（联动）时跳过     | ❌ 遗漏             |
| ACGEHE | **“单点控制时不进行计算”** | equipSwitchSet=1（单点）时跳过 | ✅ 基本准确           |

---

## 六、各设备逻辑对比汇总

| 设备     | 判断字段           | 跳过条件                                                        | 正确的业务描述              |
|--------|----------------|-------------------------------------------------------------|----------------------|
| ACCCCC | enableCtrl     | enableCtrl=1且公式含enableCtrl，或 enableCtrl≠1且公式含equipSwitchSet | 控制状态与公式匹配时跳过（防止重复计算） |
| ACCCSC | enableCtrl     | 同 ACCCCC                                                    | 同上                   |
| ACOTU  | enableCtrl     | enableCtrl=1时跳过                                             | 联动控制时不计算（此设备仅支持单点控制） |
| ACGEHE | equipSwitchSet | equipSwitchSet=1时跳过                                         | 单点控制时不计算（此设备仅支持联动控制） |

---

## 七、设计文档描述汇总（与第五节对应）

> 第五节已包含设计文档对比，本节为原始 design.md 条文，供直接参考。

`archive/2026-04-14-rc-runstatus-calculation/design.md` 第4.2节实际内容：

```
- 离心式冷水机组 (ACCCCC)：根据联动/单点控制模式选择使用 enableCtrl 或 equipSwitchSet
- 冷却塔控制单元 (ACCCSC)：单点控制时不进行计算
- 空调换热器 (ACGEHE)：单点控制时不进行计算
// 注：ACOTU 在 design.md 中未提及
```

---

## 八、建议行动

### 立即

1. **补充 design.md 关于 ACOTU 的描述**（目前遗漏）：添加 `ACOTU：enableCtrl=1（联动控制）时跳过计算`

2. **修正 design.md 中 ACCCSC 的描述**：将“单点控制时不进行计算”改为“公式类型与当前控制状态不匹配时跳过”

### 后续

3. **与产品团队确认 ACCCCC/ACCCSC 的特殊跳过逻辑的业务意图**：为什么“控制状态与公式类型相同时跳过”？这个逻辑的业务语义是什么？

4. **补充代码注释**，解释各设备跳过逻辑的业务意图，避免维护人员误解

---

**文档版本**: v1.0  
**创建时间**: 2026-04-15  
**基于代码**: `InfoCodeLinkControlHandler.java`（已审核确认）
