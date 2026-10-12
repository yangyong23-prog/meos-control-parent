# rcRunStatus 计算日志增强设计

## 设计目标

在 `ComputeIotDataHandler` 的 rcRunStatus 计算链路中增加结构化日志，使线上问题排查时间从小时级降低到分钟级。

## 日志策略

### 核心原则

1. **只记录有价值的信息**：不是每个方法都加日志，而是覆盖关键决策点
2. **结构化格式**：使用统一前缀和字段顺序，便于 grep 检索
3. **完整上下文**：一条日志包含输入、决策、输出，减少跨行关联
4. **日志量可控**：只有几十个配置了 triggerFormulaInfo 的设备会触发日志

### 日志前缀规范

```
[rcRunStatus-计算]      - 正常计算流程
[rcRunStatus-跳过]      - 跳过计算（配置缺失或设备特殊逻辑）
[rcRunStatus-异常]      - 计算异常
[rcRunStatus-状态变化]  - rcRunStatus 值发生变化（可选，后续增强）
```

## 关键日志点设计

### 日志点 1: checkControlRealStatus() - 控制指令模式

**位置**：`ComputeIotDataHandler.checkControlRealStatus()`

**触发条件**：`sourceIotType == 0` 或 `null`（控制指令状态检查模式）

**日志时机**：

- ✅ 每次计算完成后记录（成功路径）
- ✅ 异常时记录（异常路径）

**日志内容**：

```java
// 辅助方法：将状态值转换为中文
private String getStatusName(String status) {
    switch (status) {
        case "-1":
            return "无效";
        case "0":
            return "停止";
        case "1":
            return "运行";
        case "2":
            return "开启中";
        case "3":
            return "关闭中";
        default:
            return "未知(" + status + ")";
    }
}

// 成功路径 - 进行中状态
ControlLogUtil.

info(
    "[rcRunStatus-计算] 设备:{} | 触发:{}={} | 数据状态:进行中 | "+
            "缓存:onlineStatus={}, runStatus={}({}) | 决策:进行中→直接返回 | 结果:{}({})",
    objectId, triggerInfoCode, triggerValue,
    onlineStatus, runStatus, getStatusName(runStatus),

result,

getStatusName(result));

// 成功路径 - 完成状态
        ControlLogUtil.

info(
    "[rcRunStatus-计算] 设备:{} | 触发:{}={} | 数据状态:完成 | "+
            "缓存:onlineStatus={}, runStatus={}({}) | 决策:{} | 结果:{}({})",
    objectId, triggerInfoCode, triggerValue,
    onlineStatus, runStatus, getStatusName(runStatus),

decisionReason,result,

getStatusName(result));
```

**示例输出**：

```
// 场景1：进行中的开启指令
[rcRunStatus-计算] 设备:DEV001 | 触发:equipSwitchSet=1 | 数据状态:进行中 | 缓存:onlineStatus=1, runStatus=0(停止) | 决策:进行中→直接返回 | 结果:2(开启中)

// 场景2：设备离线
[rcRunStatus-计算] 设备:DEV002 | 触发:equipSwitchSet=0 | 数据状态:完成 | 缓存:onlineStatus=0, runStatus=1(运行) | 决策:设备离线→返回-1 | 结果:-1(无效)

// 场景3：正常关闭
[rcRunStatus-计算] 设备:DEV003 | 触发:equipSwitchSet=0 | 数据状态:完成 | 缓存:onlineStatus=1, runStatus=0(停止) | 决策:在线且已停止 | 结果:0(停止)

// 场景4：关闭中状态（重点关注）
[rcRunStatus-计算] 设备:DEV004 | 触发:equipSwitchSet=0 | 数据状态:完成 | 缓存:onlineStatus=1, runStatus=1(运行) | 决策:设定关闭但实际运行→关闭中 | 结果:3(关闭中)

// 场景5：开启中状态（重点关注）
[rcRunStatus-计算] 设备:DEV005 | 触发:equipSwitchSet=1 | 数据状态:完成 | 缓存:onlineStatus=1, runStatus=0(停止) | 决策:设定开启但实际停止→开启中 | 结果:2(开启中)
```

### 日志点 2: getTriggerFormulaInfoCodeValue() - 表达式模式

**位置**：`ComputeIotDataHandler.getTriggerFormulaInfoCodeValue()`

**触发条件**：`sourceIotType == 1`（信息点表达式计算模式）

**日志时机**：

- ✅ 公式计算完成后记录
- ✅ 异常时记录

**日志内容**：

```java
ControlLogUtil.info(
    "[rcRunStatus-公式计算] 设备:{} | 触发:{}={} | 变量:{} | 公式结果:{}({})",
    objectId, triggerInfoCode, triggerValue,
    variablesJson, result, getStatusName(result));
```

**示例输出**：

```
[rcRunStatus-公式计算] 设备:DEV001 | 触发:equipSwitchSet=1 | 变量:{onlineStatus=1, equipSwitchSet=1, runStatus=0} | 公式结果:2(开启中)
```

### 日志点 0: checkTriggerFormulaInfo() - 配置检查（common模块）

**位置**：`InfoCodeLinkControlHandler.checkTriggerFormulaInfo()`

**触发条件**：每次调用

**日志时机**：

- ✅ 所有返回路径都记录
    - 跳过场景：DEBUG 级别（无配置、设备特殊逻辑）
    - 通过场景：INFO 级别（记录配置检查通过的设备）

**日志内容**：

```java
// 场景1：无配置
ControlLogUtil.debug(
    "[rcRunStatus-配置检查] 设备:{} | 信息点:{} | 结果:跳过 | 原因:无triggerFormulaInfo配置",
    objectId, infoCodeCfg ==null?"null":infoCodeCfg.getInfoCode());

// 场景2：离心式冷水机组/冷却塔控制单元 - 单点联动模式不匹配
        ControlLogUtil.

debug(
    "[rcRunStatus-配置检查] 设备:{} | 类型:离心式冷水机组 | enableCtrl={} | 结果:跳过 | 原因:单点联动模式不匹配",
    objectId, enableCtrl);

// 场景3：冷却塔 - 单点控制模式
ControlLogUtil.

debug(
    "[rcRunStatus-配置检查] 设备:{} | 类型:冷却塔 | enableCtrl={} | 结果:跳过 | 原因:单点控制模式",
    objectId, enableCtrl);

// 场景4：空调换热器 - 单点控制模式
ControlLogUtil.

debug(
    "[rcRunStatus-配置检查] 设备:{} | 类型:空调换热器 | equipSwitchSet={} | 结果:跳过 | 原因:单点控制模式",
    objectId, enableCtrl);

// 场景5：通过检查
ControlLogUtil.

info(
    "[rcRunStatus-配置检查] 设备:{} | 信息点:{} | 结果:通过 | 触发信息点:{} | sourceIotType:{}",
    objectId, infoCodeCfg.getInfoCode(), 
    triggerFormulaInfo.

getTriggerInfoCode(),
    triggerFormulaInfo.

getSourceIotType());
```

**示例输出**：

```
// 场景1：无配置
[rcRunStatus-配置检查] 设备:DEV001 | 信息点:equipSwitchSet | 结果:跳过 | 原因:无triggerFormulaInfo配置

// 场景2：冷水机组单点模式
[rcRunStatus-配置检查] 设备:DEV002 | 类型:离心式冷水机组 | enableCtrl=1 | 结果:跳过 | 原因:单点联动模式不匹配

// 场景3：通过检查
[rcRunStatus-配置检查] 设备:DEV003 | 信息点:equipSwitchSet | 结果:通过 | 触发信息点:rcRunStatus | sourceIotType:0
```

**日志级别说明**：

- **跳过场景（DEBUG）**：无配置、设备特殊逻辑等跳过计算的情况使用 DEBUG 级别，因为这些场景调用频繁（所有信息点都会调用），默认不输出
- **通过场景（INFO）**：配置检查通过的设备会触发后续的 rcRunStatus 计算，使用 INFO 级别记录，便于追踪哪些设备配置了
  rcRunStatus 计算规则
- 只有配置了 triggerFormulaInfo 的设备才会在默认日志级别下输出日志

### 日志点 3: computeInfoCodeDataMap() - 配置检查与结果处理

**位置**：`ComputeIotDataHandler.computeInfoCodeDataMap()`

**触发条件**：每次调用

**日志时机**：

- ⚠️ 当 `triggerFormulaInfo == null` 时（跳过计算）- WARN 级别
- ✅ 当 `infoCodeValue != null` 时（计算成功）- INFO 级别
- ✅ 当 `infoCodeValue == null` 时（计算失败）- ERROR 级别

**日志内容**：

```java
// 场景1：配置检查未通过，跳过计算
ControlLogUtil.warn(
    "[rcRunStatus-跳过] 设备:{} | 触发:{} | 原因:配置检查未通过 | 说明:详见[rcRunStatus-配置检查]日志",
    source.getObjectId(),
    source.

getInfoCode());

// 场景2：计算成功，准备推送
        ControlLogUtil.

info(
    "[rcRunStatus-计算成功] 设备:{} | 触发:{} | 结果信息点:{} | 计算值:{}({}) | 说明:准备推送",
    source.getObjectId(),
    source.

getInfoCode(),
    triggerFormulaInfo.

getTriggerInfoCode(),

infoCodeValue,

getStatusName(infoCodeValue));

// 场景3：计算失败（返回null）
        ControlLogUtil.

error(
    "[rcRunStatus-计算失败] 设备:{} | 触发:{} | 结果信息点:{} | 原因:计算结果为null",
    source.getObjectId(),
    source.

getInfoCode(),
    triggerFormulaInfo.

getTriggerInfoCode());
```

**示例输出**：

```
// 场戣1：配置检查未通过
[rcRunStatus-跳过] 设备:DEV001 | 触发:equipSwitchSet | 原因:配置检查未通过 | 说明:详见[rcRunStatus-配置检查]日志

// 场戣2：计算成功
[rcRunStatus-计算成功] 设备:DEV002 | 触发:equipSwitchSet | 结果信息点:rcRunStatus | 计算值:2(开启中) | 说明:准备推送

// 场戣3：计算失败
[rcRunStatus-计算失败] 设备:DEV003 | 触发:equipSwitchSet | 结果信息点:rcRunStatus | 原因:计算结果为null
```

### 日志点 4: pushMessage() - 推送结果

**位置**：`ComputeIotDataHandler.pushMessage()`

**触发条件**：当信息点为 rcRunStatus 时

**日志时机**：

- ✅ 推送成功后记录 - INFO 级别
- ✅ 推送失败后记录 - ERROR 级别

**日志内容**：

```java
try{
        iWSMessageService.pushMessage(msgMqttVO);

// 推送成功
    ControlLogUtil.

info(
        "[rcRunStatus-已推送] 设备:{} | 信息点:{} | 值:{}({}) | 状态:{} | 结果:推送成功",
        msgMqttVO.getObjectId(),
        msgMqttVO.

getInfoCode(),
        msgMqttVO.

getDataValue(),

getStatusName(msgMqttVO.getDataValue()),
        msgMqttVO.

getDataStatus());

        }catch(
Exception e){
        // 推送失败
        ControlLogUtil.

error(
        "[rcRunStatus-已推送] 设备:{} | 信息点:{} | 值:{}({}) | 结果:推送失败 | 异常:{}",
        msgMqttVO.getObjectId(),
        msgMqttVO.

getInfoCode(),
        msgMqttVO.

getDataValue(),

getStatusName(msgMqttVO.getDataValue()),
        e.

getMessage(),e);
        }
```

**示例输出**：

```
// 场戣1：推送成功
[rcRunStatus-已推送] 设备:DEV001 | 信息点:rcRunStatus | 值:2(开启中) | 状态:1 | 结果:推送成功

// 场戣2：推送失败
[rcRunStatus-已推送] 设备:DEV002 | 信息点:rcRunStatus | 值:3(关闭中) | 结果:推送失败 | 异常:网络超时
```

**注意**：使用 try-catch 包裹推送逻辑，确保能捕获到推送异常。仅对 rcRunStatus 信息点记录推送日志，其他信息点保持原样。

### 日志点 5: 异常处理

**位置**：所有 catch 块

**日志时机**：

- ✅ 任何异常

**日志内容**：

```java
ControlLogUtil.error(
    "[rcRunStatus-异常] 设备:{} | 触发:{}={} | 方法:{} | 异常:{} | 堆栈:{}",
    objectId, infoCode, dataValue,
    methodName, e.getMessage(),e);
```

## 实现细节

### 1. checkControlRealStatus() 增强

```java
private String checkControlRealStatus(FeedbackInfoMsgMqttVO source,
                                      TriggerFormulaInfo triggerFormulaInfo) {
    try {
        Set<String> infoCodes = triggerFormulaInfo.getVariables().stream()
                .map(InfoCodeInfo::getInfoCode)
                .collect(Collectors.toSet());

        String runStatus = this.iotDataCacheService.queryIotRealData(
                source.getObjectId(), "runStatus", null);
        String onlineStatus = this.iotDataCacheService.queryIotRealData(
                source.getObjectId(), "onlineStatus", null);

        String result;
        String decisionReason;

        // 如果当前IOT数据处于进行中的状态
        if (source.checkProcessData()) {
            result = "1".equals(source.getDataValue()) ? "2" : "3";
            decisionReason = String.format("进行中数据直接判断(dataValue=%s)",
                    source.getDataValue());

            ControlLogUtil.info(
                    "[rcRunStatus-计算] 设备:{} | 触发:{}={} | 数据状态:2(进行中) | " +
                            "缓存:onlineStatus={}, runStatus={} | 决策:{} → {} | 结果:{}",
                    source.getObjectId(),
                    source.getInfoCode(), source.getDataValue(),
                    onlineStatus, runStatus,
                    decisionReason, result, result);

            return result;
        }

        // 返回运行状态
        boolean hasOnlineCheck = infoCodes.contains("onlineStatus");

        if (hasOnlineCheck) {
            boolean isOnline = "1".equals(onlineStatus) || "1.0".equals(onlineStatus);
            result = isOnline ? runStatus : "-1";
            decisionReason = String.format("在线状态检查(onlineStatus=%s, 在线=%s)",
                    onlineStatus, isOnline);
        } else {
            result = runStatus;
            decisionReason = "无在线状态检查，直接返回runStatus";
        }

        ControlLogUtil.info(
                "[rcRunStatus-计算] 设备:{} | 触发:{}={} | 数据状态:1(完成) | " +
                        "缓存:onlineStatus={}, runStatus={} | 决策:{} → {} | 结果:{}",
                source.getObjectId(),
                source.getInfoCode(), source.getDataValue(),
                onlineStatus, runStatus,
                decisionReason, result, result);

        return result;

    } catch (Exception e) {
        ControlLogUtil.error(
                "[rcRunStatus-异常] 设备:{} | 触发:{}={} | 方法:checkControlRealStatus | 异常:{}",
                source.getObjectId(),
                source.getInfoCode(), source.getDataValue(),
                e.getMessage(), e);

        if (e instanceof BusinessException) {
            throw e;
        }
    }
    return null;
}
```

### 2. getTriggerFormulaInfoCodeValue() 增强

```java
private String getTriggerFormulaInfoCodeValue(FeedbackInfoMsgMqttVO source,
                                              TriggerFormulaInfo triggerFormulaInfo,
                                              RcInfoCodeCfg infoCodeCfg) {
    // 控制指令状态检查
    if (triggerFormulaInfo.getSourceIotType() == null || triggerFormulaInfo.getSourceIotType() == 0) {
        return this.checkControlRealStatus(source, triggerFormulaInfo);
    }

    // 表达式模式
    try {
        Map<String, Object> infoCodeAndDataValue = new HashMap<>();
        List<InfoCodeInfo> variables = triggerFormulaInfo.getVariables();

        variables.forEach(infoCodeInfo -> {
            if (source.getInfoCode().equals(infoCodeInfo.getInfoCode())) {
                infoCodeAndDataValue.put(infoCodeInfo.getInfoCode(),
                        new BigDecimal(source.getDataValue()));
            } else {
                infoCodeAndDataValue.put(infoCodeInfo.getInfoCode(),
                        this.queryInfoCodeDataValueFromCache(source, infoCodeInfo, infoCodeCfg));
            }
        });

        Object execute = AviatorEvaluator.execute(
                triggerFormulaInfo.getTriggerFormula(), infoCodeAndDataValue);
        String result = execute == null ? null : execute.toString();

        // 记录公式计算日志
        ControlLogUtil.info(
                "[rcRunStatus-公式计算] 设备:{} | 触发:{}={} | 变量:{} | 公式结果:{}",
                source.getObjectId(),
                source.getInfoCode(), source.getDataValue(),
                JSON.toJSONString(infoCodeAndDataValue),
                result);

        return result;

    } catch (Exception e) {
        ControlLogUtil.error(
                "[rcRunStatus-异常] 设备:{} | 触发:{}={} | 方法:getTriggerFormulaInfoCodeValue | 异常:{}",
                source.getObjectId(),
                source.getInfoCode(), source.getDataValue(),
                e.getMessage(), e);
    }
    return null;
}
```

### 3. computeInfoCodeDataMap() 增强

```java
private FeedbackInfoMsgMqttVO computeInfoCodeDataMap(FeedbackInfoMsgMqttVO source,
                                                     RcInfoCodeCfg infoCodeCfg) {
    TriggerFormulaInfo triggerFormulaInfo = this.infoCodeLinkControlHandler
            .checkTriggerFormulaInfo(source.getObjectId(), infoCodeCfg);

    if (triggerFormulaInfo == null) {
        // 记录跳过计算的日志
        String skipReason = determineSkipReason(source.getObjectId(), infoCodeCfg);
        ControlLogUtil.warn(
                "[rcRunStatus-跳过] 设备:{} | 触发:{} | 原因:{} | 说明:{}",
                source.getObjectId(),
                source.getInfoCode(),
                skipReason,
                "该信息点不会触发rcRunStatus计算");
        return null;
    }

    String infoCodeValue = this.getTriggerFormulaInfoCodeValue(
            source, triggerFormulaInfo, infoCodeCfg);

    if (infoCodeValue != null) {
        FeedbackInfoMsgMqttVO infoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        BeanUtil.copyProperties(source, infoMsgMqttVO);
        infoMsgMqttVO.setInfoCode(triggerFormulaInfo.getTriggerInfoCode());
        infoMsgMqttVO.setDataValue(infoCodeValue);
        infoMsgMqttVO.setDataStatus(1);
        infoMsgMqttVO.setInvalid(0);
        infoMsgMqttVO.setType(EnumIotDataType.FEEDBACK.getCode());
        return infoMsgMqttVO;
    }
    return null;
}

private String determineSkipReason(String objectId, RcInfoCodeCfg infoCodeCfg) {
    if (infoCodeCfg == null || infoCodeCfg.getExtendInfo() == null) {
        return "无extend_info配置";
    }
    if (infoCodeCfg.getExtendInfo().getTriggerFormulaInfo() == null) {
        return "无triggerFormulaInfo配置";
    }
    // 可以根据 InfoCodeLinkControlHandler 的逻辑进一步细化
    return "设备类型特殊逻辑跳过";
}
```

## 日志检索指南

### 常见排查场景

```bash
# 1. 查看某个设备的 rcRunStatus 计算历史
grep "设备:DEV001" control.log | grep "rcRunStatus"

# 2. 查找所有计算异常
grep "\[rcRunStatus-异常\]" control.log

# 3. 查找跳过计算的情况
grep "\[rcRunStatus-跳过\]" control.log

# 4. 查看某个时间段的所有计算
grep "2024-04-14 10:" control.log | grep "\[rcRunStatus-计算\]"

# 5. 统计某个设备的状态分布
grep "设备:DEV001" control.log | grep "\[rcRunStatus-计算\]" | \
  grep -o "结果:[0-9-]*" | sort | uniq -c

# 6. 查找特定状态（如一直在开启中）
grep "结果:2" control.log | grep "开启中"
```

### 日志格式说明

```
[rcRunStatus-计算] 
  设备:{objectId}           - 设备ID
  触发:{infoCode}={value}   - 触发计算的信息点及其值
  数据状态:{status}         - 1=完成, 2=进行中
  缓存:{key}={value}        - 从Redis读取的缓存值
  决策:{reason}             - 决策路径说明
  结果:{rcRunStatus}        - 最终计算结果

状态值说明：
  -1 = 无效（设备离线或数据缺失）
   0 = 停止
   1 = 运行
   2 = 开启中
   3 = 关闭中
```

## 实施计划

### Phase 1: 核心日志增强（本次）

- [ ] 增强 `InfoCodeLinkControlHandler.checkTriggerFormulaInfo()` 方法
    - [ ] 添加配置检查的 DEBUG 级别日志
    - [ ] 覆盖所有跳过场景（无配置、冷水机组、冷却塔、换热器）
    - [ ] 添加通过检查的日志

- [ ] 增强 `ComputeIotDataHandler.computeInfoCodeDataMap()` 方法
    - [ ] 修改跳过日志的原因说明
    - [ ] 添加计算成功的 INFO 级别日志
    - [ ] 添加计算失败的 ERROR 级别日志

- [ ] 增强 `ComputeIotDataHandler.pushMessage()` 方法
    - [ ] 添加 rcRunStatus 推送成功的 INFO 级别日志
    - [ ] 添加 rcRunStatus 推送失败的 ERROR 级别日志
    - [ ] 使用 try-catch 包裹推送逻辑

- [ ] 增强 `ComputeIotDataHandler.checkControlRealStatus()` 方法
    - [ ] 添加进行中状态的日志
    - [ ] 添加完成状态的日志
    - [ ] 统一异常日志格式

- [ ] 增强 `ComputeIotDataHandler.getTriggerFormulaInfoCodeValue()` 方法
    - [ ] 添加公式计算日志
    - [ ] 统一异常日志格式

### Phase 2: 高级功能（后续可选）

- [ ] 状态变化检测日志（rcRunStatus 值变化时）
- [ ] 状态持续时间告警（卡在开启中/关闭中超过N分钟）
- [ ] Nacos 动态日志级别配置

## 风险评估

### 性能影响

- **极低**：只有几十个设备触发日志
- **配置检查日志**：DEBUG 级别，默认不输出，无性能影响
- **日志量**：每 5 分钟最多几十条，每条约 200-300 字节
- **总增量**：< 10KB/5分钟，可忽略不计

### 兼容性

- **无破坏性**：纯日志增强，不改变业务逻辑
- **可回滚**：通过降低日志级别即可关闭
- **无需迁移**：不影响现有数据和配置
- **跨模块影响**：
    - `InfoCodeLinkControlHandler`（common 模块）：增加 DEBUG 级别日志，不影响其他调用方
    - `ComputeIotDataHandler`（compute-edge 模块）：增加日志，不影响调用关系

### 推送逻辑改动风险

- **try-catch 包裹推送**：可能改变原有的异常处理行为
- **缓解措施**：
    - 仅对 rcRunStatus 信息点使用 try-catch
    - 其他信息点保持原有逻辑
    - 需要测试确认 `iWSMessageService.pushMessage()` 是否本身已捕获异常

### 维护性

- **日志格式统一**：便于检索和分析
- **上下文完整**：一条日志包含完整信息，减少关联成本
- **异常堆栈保留**：便于深入排查
- **日志链路清晰**：配置检查 → 计算 → 计算成功/失败 → 推送成功/失败
