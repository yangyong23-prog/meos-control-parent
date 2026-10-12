## Context

业务可用性检查（BizCheck）是 MEOS-Control 平台用于校验项目配置完整性的能力，覆盖 4 个检查项：工作日历、空调运行标准、单价配置、系统关系图。

**现状调用链：**
```
XXL-Job (cron 0 * * * * ?)           前端手动触发
        │                                   │
        ▼                                   ▼
BizCheckHandler@XxlJob            BizCheckController
        │                                   │
        ▼                                   ▼
BizCheckServiceImpl.triggerAutoCheck   BizCheckServiceImpl.triggerCheck
        │                                   │
        └──────────────┬────────────────────┘
                       ▼
        BizCheckDomainServiceImpl.startCheck(config, checkType)
                       │
        ┌──────────────┼──────────────────┐
        │   getBeansOfType(BizCheckStrategy)  │
        │   for each: PENDING → execute → completeCheck │
        └──────────────┴──────────────────┘
                       │
                   【入库完成】 ← 钩子插入点
```

**关键约束：**
- 项目存在 saas（云）与 local（边）两套部署，仅云端需要此功能。
- `saas/pom.xml` 已引入 `mid-organization-api`（`GroupUserClient`）与 `mid-message-api`（`IMessageApi`），无需新增 SDK。
- 项目通行 `applicationContext.getBeansOfType(Xxx.class)` 自动发现模式（如 `BizCheckStrategy`）。
- 通知失败不能阻塞检查主流程。

## Goals / Non-Goals

**Goals:**
- 在 `startCheck` 全部检查项入库后，自动筛选异常项（外层 `checkStatus=REJECTED` AND 内层 `status=EXCEPTION`），按 PRD 模板组装文本并通过钉钉推送给业务顾问。
- 通过"common 纯接口 + saas 实现 + `getBeansOfType` 自动发现"实现云边解耦，边端零文件、零感知。
- 支持 4 个检查项的差异化明细渲染，每个异常细项独占一行。
- 异常总条数 = 渲染后明细行数（数法 B），嵌入消息模板"业务类型"与"异常详情"之间。
- 失败保护：通知任意环节抛错仅记日志，不影响 `startCheck` 主流程。

**Non-Goals:**
- 不做去重（连点连发）。
- 不做 scope（云/边/项目维度）路由；固定云端唯一 groupCode。
- 不做异步/线程池；本期同步发送。
- 不做前端配置面（启用开关、岗位列表纯 yml 配置）。
- 不修改 BizCheck 自身的检查规则、状态枚举、入库结构。
- 不建立通知持久化（不存通知记录表，不做重发）。

## Decisions

### Decision 1：钩子位置 = `BizCheckDomainServiceImpl#startCheck` 末尾追加

**选项对比：**

| 选项 | 描述 | 评估 |
|---|---|---|
| A. 装饰器包装 BizCheckService | AOP/包装类拦截 triggerAutoCheck/triggerCheck | ✗ 增加抽象层，维护成本高 |
| B. Spring Event | startCheck 末尾 publish 事件 | ✗ 项目无统一事件总线规范 |
| C. 独立扫描定时任务 | 二级定时扫库推送 | ✗ 不实时，需去重表 |
| **D. startCheck 末尾直接调** | **末尾追加 6~8 行 `getBeansOfType` 循环** | **✓ 与 BizCheckStrategy 同款模式** |

**理由：** 自动 + 手动两路都汇聚到 `startCheck`，天然统一；代码量极小，显式可读。

### Decision 2：云边隔离 = `getBeansOfType(BizCheckNotifier.class)` + 边端零文件

**选项对比：**

| 选项 | 描述 | 评估 |
|---|---|---|
| A. `@ConditionalOnProperty` | 边端配置关 | ✗ common 仍需默认实现 |
| B. `@Primary` + Noop 默认 | common Noop + saas @Primary | ✗ 用户明确不要注解条件装配 |
| **C. 纯接口 + 自动发现** | **common 纯接口；saas @Service 实现；边端无文件 → Map 为空** | **✓ 选定** |

**理由：** 与 `BizCheckStrategy` 模式完全一致；边端无 Bean → 循环零开销直接跳过；common 接口零依赖 Spring。

### Decision 3：异常筛选口径 = 外层 `checkStatus=REJECTED` AND 内层 `status=EXCEPTION`

**理由：** PRD 与用户决策明确，仅双交集算异常。排除 NORMAL/COMPLETED/PENDING/TO_BE_CHECKED/FAILED 噪音。内层渲染时仅取 `status=EXCEPTION` 的子项。

### Decision 4：异常总条数 = 实际渲染明细行数（数法 B）

**理由：** 用户决策：数法 B 更直观（运维一眼看出有几条要修）。实现自然：`Renderer#render` 返回 `List<String>`，主流程 `allLines.size()` 即为总条数。

**示例：** WorkCalendar 3 细项 + HVAC 2 细项 + UnitPrice 1 细项 + SystemRelationshipDiagram 1 细项 = 总条数 7。

### Decision 5：渲染策略族 = `NotifyTextRenderer` 按 `BizCheckStrategyTypeEnum` 路由

**接口定义：**
```java
public interface NotifyTextRenderer {
    BizCheckStrategyTypeEnum supports();
    List<String> render(BizCheckResultVO vo);
}
```

**4 个 Renderer 渲染规则表：**

| Renderer | 策略类型 | 输出行数 | 渲染逻辑 |
|---|---|---|---|
| `WorkCalendarNotifyRenderer` | WORK_CALENDAR | 1~3 | 三个细项各自独立成行：①供冷系统服务空间营业时间（合并当日+次日系统列表，按 localName 排序去重，格式 `系统名（状态）`）②所属空调季节（当日-值；次日-值）③空调工况（当日-值；次日-值）|
| `HvacOperationStandardNotifyRenderer` | HVAC_OPERATION_STANDARD | 1~2 | 模板异常 → "运行模板：状态描述"；温度门限异常 → "温度门限值：状态描述"；同时异常各占一行 |
| `UnitPriceNotifyRenderer` | UNIT_PRICE_CONFIG | 1 | "当日电价数据：数据缺失/状态描述" |
| `SystemRelationshipDiagramNotifyRenderer` | SYSTEM_RELATIONSHIP_DIAGRAM | 1 | "是否发布关系图：" + 全部系统列表，每个标注（已发布/未发布）|

**Registry 路由：** 使用 `applicationContext.getBeansOfType(NotifyTextRenderer.class)` 获取全部实现，按 `supports()` 返回值路由到对应 Renderer。

### Decision 6：收件人解析 = 实时同步查 GroupUserClient（不缓存）

**调用：** `GroupUserClient.queryByPositionNamesAndProjectId(groupCode, positionNames, projectId)`

**参数来源：** groupCode ← 配置 `meos.bizcheck.notify.group-code`（默认 `YuShu`）；positionNames ← 配置 `meos.bizcheck.notify.position-names`（默认 `[业务顾问]`）；projectId ← 当前检查上下文。

### Decision 7：发送 = 同步循环 `IMessageApi.sendDingTalkPersonalMessage`

**理由：** 用户决策先用同步。单次推送量小（业务顾问通常 ≤ 个位数）。任一手机号发送失败仅记 warn 日志，继续发送其他人。

### Decision 8：失败处理 = try/catch 包裹整个通知流程

**钩子代码模板（startCheck 末尾追加）：**
```java
Map<String, BizCheckNotifier> notifiers =
        applicationContext.getBeansOfType(BizCheckNotifier.class);
for (BizCheckNotifier n : notifiers.values()) {
    try {
        n.notifyIfAbnormal(bizCode);
    } catch (Exception e) {
        log.error("BizCheck通知发送失败，不阻塞主流程. bizCode={}", bizCode, e);
    }
}
```

**DingTalkBizCheckNotifier 内部：** 各环节（查询结果/筛选/渲染/解析收件人/发送）任一异常直接向上抛出，由钩子的 catch 统一兜底。

### Decision 9：消息模板格式

```
【{PROJECT_NAME}可用性检查异常通知】
最近一次检查结束时间：{checkEndTime}
业务类型：{bizCodeName}
异常总条数：{totalCount}
异常详情：
{detailLine1}
{detailLine2}
...
```

- `PROJECT_NAME`：当前项目名称
- `checkEndTime`：所有异常项中最大的 checkEndTime
- `bizCodeName`：根据 bizCode 映射的中文名（如"供冷业务"）
- `totalCount`：渲染后 `allLines.size()`
- 各 detailLine：由 Renderer 输出，格式为 `检查项名-细项名：内容`

## Risks / Trade-offs

| 风险 | 影响 | 缓解 |
|---|---|---|
| Feign 不可用（GroupUserClient / IMessageApi） | 通知发送失败 | try/catch 兜底，不影响主流程；日志可追溯 |
| 同步发送阻塞 startCheck 尾部 | 极端场景下延长检查总耗时 | 通知量小（≤ 个位数手机号），预计 < 几秒；后续可切换为异步 |
| 不去重导致连续推送 | 用户收到重复通知 | 用户决策明确接受；通知内含 checkEndTime 可区分批次 |
| groupCode 硬编码为 YuShu | 多组织项目无法覆盖 | 本期仅玉树项目；后续可通过配置扩展 |
