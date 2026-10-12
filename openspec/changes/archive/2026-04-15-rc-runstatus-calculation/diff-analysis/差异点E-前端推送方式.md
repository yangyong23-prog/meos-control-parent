# 差异点 E：前端推送方式（SSE vs WebSocket）

> **优先级**: ⚠️ 待澄清  
> **状态**: 需确认产品意图  
> **关联文件**: `WsMessageServiceImpl.java`，`ComputeIotDataHandler.java`

---

## 一、差异概述

产品流程图明确使用"SSE推送"描述前端推送行为。但现有代码的 rcRunStatus 消息**实际走的是 WebSocket 推送**，并不走 SSE
通道。这可能是产品文档用词不严格，也可能是实现与需求不一致。

---

## 二、产品文档要求

产品流程图（rcRunStatus计算逻辑.png）在多处节点标注：

```
→ SSE推送 rcRunStatus 给前端
```

**SSE（Server-Sent Events）**：

- 单向通道，服务器主动推送到客户端
- 基于 HTTP 协议，轻量级
- 适合服务器到客户端的实时状态推送

---

## 三、现有代码实现

### 3.1 推送入口

**文件**：`WsMessageServiceImpl.java`，方法：`pushMessage()`

```java
@Override
public void pushMessage(FeedbackInfoMsgMqttVO source) {
    // ──────────────────────────────────────────────────
    // 路径1：WebSocket 推送（默认行为）
    for (WSClient client : WSClientCache.getProjectClients(projectCfg.getProjectId())) {
        try {
            if (WebSocketTypeEnum.PARAM_SET.equals(source.getWebType()) 
                    && client.getFunctionType() != null) {
                // 有功能类型过滤配置时，按配置推送
                Set<String> filterCodes = LocalCacheConstant.WEBSOCKET_CONFIG.get(client.getFunctionType().getCode());
                if (filterCodes.contains(WebsocketConfig.getUniqueKey(source.getClassCode(), source.getInfoCode()))) {
                    client.send(JSON.toJSONString(source));
                }
            } else {
                client.send(JSON.toJSONString(source));  // 无过滤，直接推
            }
        } catch (Exception e) {
            log.error("websocket推送客户端数据失败.......");
        }
    }

    // ──────────────────────────────────────────────────
    // 路径2：SSE 推送（仅特定 webType 才触发）
    if (WebSocketTypeEnum.PAGE_BROADCAST.equals(source.getWebType())) {
        // 页面播报类型 → SSE推送
        sseHandlerContext.getStrategy(SseEmitterEventEnum.PAGE_BROADCAST).doOperation(...);
    } else if (WebSocketTypeEnum.INFO_EXTEND_SET.equals(source.getWebType())) {
        // 信息点扩展配置类型 → SSE推送
        sseHandlerContext.getStrategy(SseEmitterEventEnum.INFO_EXTEND_SET).doOperation(...);
    }
}
```

### 3.2 rcRunStatus 消息的 webType

**文件**：`ComputeIotDataHandler.java`，方法：`computeInfoCodeDataMap()`

```java
FeedbackInfoMsgMqttVO infoMsgMqttVO = new FeedbackInfoMsgMqttVO();
BeanUtil.copyProperties(source, infoMsgMqttVO);
infoMsgMqttVO.setInfoCode(triggerFormulaInfo.getTriggerInfoCode());  // "rcRunStatus"
infoMsgMqttVO.setDataValue(infoCodeValue);
infoMsgMqttVO.setDataStatus(1);
infoMsgMqttVO.setInvalid(0);
infoMsgMqttVO.setType(EnumIotDataType.FEEDBACK.getCode());
// ← webType 未设置，沿用 source 的 webType，默认为 PARAM_SET
return infoMsgMqttVO;
```

`WsMessageServiceImpl.pushMessage()` 中：

```java
source.setWebType(source.getWebType() == null ? WebSocketTypeEnum.PARAM_SET : source.getWebType());
```

**结论**：rcRunStatus 消息的 webType 最终为 `PARAM_SET`，在 `pushMessage()` 中：

- ✅ **走 WebSocket 推送路径**
- ❌ **不走 SSE 推送路径**（PAGE_BROADCAST 和 INFO_EXTEND_SET 均不满足）

### 3.3 SSE 额外推送（sseStrategy）

`ComputeIotDataHandler.sseStrategy()` 方法在某些信息点变化时额外推送 SSE：

```java
public void sseStrategy(FeedbackInfoMsgMqttVO source) {
    // IOT数据变更推送（对所有信息点）
    sshHandlerContext.getStrategy(SseEmitterEventEnum.IOT_DATA_CHANGE).doOperation(source.getProjectId(), source);
    
    // 照明回路对象的特殊SSE推送
    if (DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue().equals(source.getClassCode())) {
        if (InfoCodeEnum.RUN_STATUS.getCode().equals(source.getInfoCode())) {
            sshHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_LOOP).doOperation(...);
        }
        // ...
    }
}
```

但 `sseStrategy()` 的调用时机需要确认是否在 rcRunStatus 计算后被调用。

---

## 四、差异对比

| 推送方式      | 产品文档   | 现有代码实际                                     | 说明      |
|-----------|--------|--------------------------------------------|---------|
| SSE       | ✅ 明确要求 | ⚠️ IOT_DATA_CHANGE事件可能触发，但不直接携带rcRunStatus | 不确定是否等价 |
| WebSocket | 未提及    | ✅ 所有FEEDBACK消息默认走WebSocket                 | 代码额外行为  |

---

## 五、两种推送方式的技术对比

| 特性     | SSE（Server-Sent Events） | WebSocket |
|--------|-------------------------|-----------|
| 通信方向   | 单向（服务器→客户端）             | 双向        |
| 协议     | HTTP                    | WebSocket |
| 资源消耗   | 较低                      | 较高        |
| 适用场景   | 状态推送、通知                 | 实时双向交互    |
| 断线重连   | 浏览器自动重连                 | 需手动处理     |
| 是否支持IE | 不支持（IE）                 | 支持        |
| 现有代码   | 条件触发（特定webType）         | 默认推送      |

---

## 六、待确认问题

### 问题1：产品文档中"SSE推送"是技术要求还是泛指？

可能性1：产品文档严格指定使用 SSE 协议（需修改代码）  
可能性2：产品文档泛指"服务端推送"，WebSocket 满足需求（不需要改动）

**现实情况**：WebSocket 在功能上完全覆盖 SSE 的能力（实时推送），但技术实现不同。

### 问题2：前端是否有针对 SSE 和 WebSocket 的不同处理？

- 若前端只监听 WebSocket，现有代码没问题
- 若前端期望 SSE 通道，需要修改代码让 rcRunStatus 走 SSE

---

## 七、建议

### 方案1：保持现有 WebSocket 推送（推荐）

**理由**：

- WebSocket 功能上满足实时推送需求
- 现有代码已经在线稳定运行
- 不需要额外改动

**需要**：与产品团队确认"SSE"是否只是泛指，统一术语

### 方案2：修改为 SSE 推送（严格遵循产品文档）

若确认需要走 SSE，修改方式：

```java
// 在 computeInfoCodeDataMap() 中，给 rcRunStatus 消息设置 SSE webType
infoMsgMqttVO.setWebType(WebSocketTypeEnum.PAGE_BROADCAST);  // 或新增一个专用类型

// 然后在 WsMessageServiceImpl.pushMessage() 中
// rcRunStatus 消息会走 SSE 推送路径而非 WebSocket
```

**注意**：需要确认前端 SSE 监听是否已经覆盖 rcRunStatus 的推送场景。

---

**文档版本**: v1.0  
**创建时间**: 2026-04-15  
**基于代码**: `WsMessageServiceImpl.java`，`ComputeIotDataHandler.java`（已审核确认）
