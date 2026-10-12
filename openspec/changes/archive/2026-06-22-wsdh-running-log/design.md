## Context

运行日志系统基于 `RunningLog` 新版实体和 `NowRunningLogCategory` 分类枚举驱动。当前已覆盖 10 个模块（冷热源、空调末端、照明、通风、安防、污水处理等），新增「生活热水」模块需要在该体系中注册分类、信息点和对象查询策略。

生活热水为系统级模块（类比 ACAC 空调冷源全系统），classCode 为 `WSDH`，包含两个设备类：WSDHHE（生活热水换热器）和 WSDHHP（生活热水水泵）。本次为完全新增功能，不兼容旧版 `RcRunningLog` / `RunningLogCategoryEnum`。

数据流关键路径：IoT MQTT 消息 → `PushIOTEventListener` → `RunningLogHandle` → `RunningLogEdgeNowServiceImpl.twinMsgLog()` → `setCategory()` → `buildLogCategory()` → `NowRunningLogCategory` 路由 → `save()`。该路径完全数据驱动，注册枚举和配置后自动承接。

## Goals / Non-Goals

**Goals:**
- 在新版日志体系中注册生活热水的一级分类和二级分类
- 注册 WSDHHE/WSDHHP 的孪生自控信息点，使孪生下发的控制指令能自动记录日志
- 确保 `LocalCacheConstant.OBJECT_SYSTEM_CLASS` 包含 WSDHHE/WSDHHP，使 `getObjectBusinessFeildAndSystemInfo` 能通过 EQ_IN_SYSTEM 关系正确路由到 WSDH 系统
- 新增 WSQueryObjStrategy 占位实现，供后续第三方接入
- 确保日志查询、分类筛选、SSE 播报等消费侧能力自动承接

**Non-Goals:**
- 旧版日志（`RcRunningLog` / `RunningLogCategoryEnum`）兼容
- 新增日志模板（复用现有模板一~五、七、八）
- 新增日志来源类型（复用现有 COMMAND/SCHEDULE/INTERVENTION 等）
- 照明相关模板（九~十二）的实现
- WSQueryObjStrategy 的完整查询逻辑实现（仅占位）

## Decisions

### D1: 一级分类采用系统级模式（类比 ACAC）

**选择**：`StatusAndControl_WS` 的 classCode 设为 `"WSDH"`，注册到 `systemCategoryByClassCodeMap`

**理由**：生活热水有对应的系统实例（WSDH），设备通过 EQ_IN_SYSTEM 关系归属于系统。与 ACAC 模式一致，`buildLogCategory` 中会先查 `systemCategoryByClassCodeMap` 匹配系统级分类，再自动构建二级分类。

**替代方案**：非系统级模式（类比通风/安防），classCode 为 null，需 `wirteObjectClassBelongModelType` 预设归属。排除理由——WSDHHE/WSDHHP 无歧义归属，不需要预设，且系统级模式能自动查询系统名称。

### D2: 不在 wirteObjectClassBelongModelType 中添加生活热水分支

**选择**：不添加

**理由**：WSDHHE/WSDHHP 唯一归属于 WSDH 系统，不存在歧义（不像 ACVTSF 可属于通风/厨房排油烟/空调末端）。通过 `OBJECT_SYSTEM_CLASS` + EQ_IN_SYSTEM 关系查询即可正确路由。`wirteObjectClassBelongModelType` 仅用于有歧义的设备类。

### D3: WSQueryObjStrategy 占位实现

**选择**：参照 `ACACQueryObjStrategy` 骨架创建，`queryObj` 方法内部调用 `querySystemObj`，但标注 TODO 等第三方实现

**理由**：用户明确要求查询逻辑留占位，后续第三方接入时替换实现。策略类通过 Spring `@Service` 自动注入 `RunningLogCategoryQueryObjFactory`，无需修改工厂代码。

### D4: 二级分类按设备类 classCode 映射

**选择**：为 WSDHHE 和 WSDHHP 各创建一个二级分类枚举值，parentCode 为 `StatusAndControl_WS`

**理由**：与现有模式一致（如 `StatusAndControl_KEF_ACKLSF`），`buildSecondLevelCategory` 通过 `parentCode + classCode` 映射二级分类，前端可按设备类筛选日志。

### D5: 控制消息 release 值过滤

**选择**：无需额外改动，`RunningLogTemplateConfig.bdtpMeosFiltrationInfoCodeVal` 已全局过滤 `release` 值

**理由**：PRD 要求过滤信息点值=release的消息，现有代码已实现。

## Risks / Trade-offs

- **[WSDHHE 枚举缺失]** `DtpRwdClassCodeEnum` 中尚无 `WSDHHE`，需同步新增。→ 在本次变更中一并添加，确保编译通过。
- **[OBJECT_SYSTEM_CLASS 遗漏]** 若遗漏注册 WSDHHE/WSDHHP，`getObjectBusinessFeildAndSystemInfo` 会返回 null，导致旧版日志路径跳过（符合预期），但其他依赖该方法的业务逻辑也可能受影响。→ 通过设计评审确认影响范围。
- **[WSQueryObjStrategy 占位]** 占位实现可能在集成测试阶段暴露问题。→ 标注 TODO，明确后续第三方接入时需替换。
