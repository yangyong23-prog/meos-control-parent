## ADDED Requirements

### Requirement: BizCheckNotifier 云边解耦接口
系统 SHALL 在 `common` 模块提供纯接口 `BizCheckNotifier`，定义 `notifyIfAbnormal(String bizCode)` 方法，不含任何 Spring 注解。`BizCheckDomainServiceImpl#startCheck` 末尾 SHALL 通过 `applicationContext.getBeansOfType(BizCheckNotifier.class)` 自动发现所有实现并逐个调用。边端（local 模块）MUST NOT 提供任何 `BizCheckNotifier` 实现。

#### Scenario: 云端 startCheck 完成后触发通知
- **WHEN** `BizCheckDomainServiceImpl#startCheck` 全部检查项入库完成
- **THEN** 系统通过 `getBeansOfType(BizCheckNotifier.class)` 获取所有实现，对每个实现调用 `notifyIfAbnormal(bizCode)`

#### Scenario: 边端无 BizCheckNotifier 实现
- **WHEN** 边端应用启动并执行 `startCheck`
- **THEN** `getBeansOfType(BizCheckNotifier.class)` 返回空 Map，循环跳过，不执行任何通知逻辑

#### Scenario: 通知失败不阻塞主流程
- **WHEN** 任一 `BizCheckNotifier.notifyIfAbnormal` 抛出异常
- **THEN** 异常 MUST 被 catch 并记录 error 日志，`startCheck` 主流程正常返回，不向上传播异常

### Requirement: 异常筛选口径
系统 SHALL 仅筛选同时满足以下两个条件的检查结果进入通知：外层 `checkStatus = REJECTED` 且内层 `details` 中存在 `status = EXCEPTION` 的子项。内层渲染时 MUST 仅取 `status = EXCEPTION` 的子项。

#### Scenario: 外层 REJECTED 且内层有 EXCEPTION
- **WHEN** 某检查项 `checkStatus = REJECTED` 且其 `details` 中存在 `status = EXCEPTION` 的子项
- **THEN** 该检查项 MUST 纳入通知渲染

#### Scenario: 外层 REJECTED 但内层无 EXCEPTION
- **WHEN** 某检查项 `checkStatus = REJECTED` 但其 `details` 中所有子项 `status ≠ EXCEPTION`
- **THEN** 该检查项 MUST NOT 纳入通知

#### Scenario: 外层非 REJECTED
- **WHEN** 某检查项 `checkStatus ≠ REJECTED`（如 COMPLETED / NORMAL / PENDING）
- **THEN** 该检查项 MUST NOT 纳入通知，无论内层状态如何

#### Scenario: 全部检查项均无异常
- **WHEN** 所有检查项均不满足筛选条件
- **THEN** 系统 MUST NOT 发送任何通知

### Requirement: 异常总条数统计
系统 SHALL 在消息模板中"业务类型"与"异常详情"之间输出"异常总条数"字段。异常总条数 MUST 等于所有异常检查项经 Renderer 渲染后的明细行数之和。

#### Scenario: 多个检查项各自产出行数不同
- **WHEN** WorkCalendar 渲染 3 行 + HVAC 渲染 2 行 + UnitPrice 渲染 1 行 + SystemRelationshipDiagram 渲染 1 行
- **THEN** 异常总条数 MUST 为 7

#### Scenario: 仅有单条异常
- **WHEN** 仅 UnitPrice 渲染 1 行
- **THEN** 异常总条数 MUST 为 1

### Requirement: 消息模板格式
系统 SHALL 按以下模板组装钉钉通知文本：

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

其中：
- `PROJECT_NAME` 为当前项目名称
- `checkEndTime` 为所有异常项中最大的 checkEndTime
- `bizCodeName` 为 bizCode 对应的中文名
- `totalCount` 为异常总条数
- 各 detailLine 由对应 Renderer 输出

#### Scenario: 供冷业务多检查项异常
- **WHEN** 供冷业务下 WorkCalendar / HVAC / UnitPrice / SystemRelationshipDiagram 均有异常
- **THEN** 消息 MUST 按模板输出，异常总条数为各 Renderer 行数之和，异常详情各行按策略类型枚举顺序排列

#### Scenario: 仅单项异常
- **WHEN** 仅 UnitPrice 存在异常
- **THEN** 消息 MUST 按模板输出，异常总条数为 1，异常详情仅 1 行

### Requirement: WorkCalendar 渲染规则
WorkCalendarNotifyRenderer SHALL 将 WorkCalendar 检查结果渲染为 1~3 行文本，三个细项各自独立成行：
1. 供冷系统服务空间营业时间：合并当日与次日系统列表，按 `localName` 排序去重，格式为 `系统名（状态）`，多个系统用顿号分隔
2. 所属空调季节：格式为 `当日-值；次日-值`
3. 空调工况：格式为 `当日-值；次日-值`

仅 `status = EXCEPTION` 的细项 MUST 输出对应行。

#### Scenario: 三个细项全部异常
- **WHEN** systemBusinessStatus / airSeasonStatus / acSeasonStatus 均为 EXCEPTION
- **THEN** 渲染 3 行，各行按上述格式输出

#### Scenario: 仅空调工况异常
- **WHEN** 仅 acSeasonStatus 为 EXCEPTION
- **THEN** 渲染 1 行，格式为 `工作日历检查-空调工况：当日-供冷；次日-未配置`

#### Scenario: 营业时间多系统合并去重
- **WHEN** 当日和次日存在相同 localName 的系统
- **THEN** MUST 合并去重，按 localName 排序输出，每个系统标注状态

### Requirement: HVAC 运行标准渲染规则
HvacOperationStandardNotifyRenderer SHALL 将 HVAC 运行标准检查结果渲染为 1~2 行文本：
1. 运行模板异常时输出 `空调运行标准检查-运行模板：{状态描述}`
2. 温度门限值异常时输出 `空调运行标准检查-温度门限值：{状态描述}`

模板异常与温度门限值异常同时存在时各占一行。

#### Scenario: 仅模板异常
- **WHEN** 运行模板状态为 EXCEPTION，温度门限值状态非 EXCEPTION
- **THEN** 渲染 1 行 `空调运行标准检查-运行模板：未配置`

#### Scenario: 模板和温度门限值均异常
- **WHEN** 运行模板和温度门限值均为 EXCEPTION
- **THEN** 渲染 2 行，分别输出运行模板和温度门限值的异常描述

### Requirement: 单价配置渲染规则
UnitPriceNotifyRenderer SHALL 将单价配置检查结果渲染为 1 行文本：`单价配置检查-当日电价数据：{状态描述}`

#### Scenario: 电价数据缺失
- **WHEN** UnitPrice 检查结果 `status = EXCEPTION`
- **THEN** 渲染 1 行 `单价配置检查-当日电价数据：数据缺失`

### Requirement: 系统关系图渲染规则
SystemRelationshipDiagramNotifyRenderer SHALL 将系统关系图检查结果渲染为 1 行文本：`系统关系图检查-是否发布关系图：{系统1}（已发布）、{系统2}（未发布）...`

MUST 列出所有系统（含已发布和未发布），每个系统标注发布状态，多个系统用顿号分隔。

#### Scenario: 部分系统未发布
- **WHEN** 系统关系图检查存在已发布和未发布的系统
- **THEN** 渲染 1 行，列出全部系统并标注状态，如 `中央空调供冷全系统（已发布）、中央空调供冷全系统-XN02（未发布）`

#### Scenario: 全部系统已发布
- **WHEN** 系统关系图检查结果 status = EXCEPTION（但所有系统均已发布）
- **THEN** 仍渲染 1 行，列出全部系统均标注（已发布）

### Requirement: 收件人解析
系统 SHALL 通过 `GroupUserClient.queryByPositionNamesAndProjectId(groupCode, positionNames, projectId)` 实时同步查询收件人手机号。groupCode 来自配置 `meos.bizcheck.notify.group-code`，positionNames 来自配置 `meos.bizcheck.notify.position-names`。MUST NOT 缓存查询结果。

#### Scenario: 正常查询到业务顾问
- **WHEN** 配置 group-code=YuShu, position-names=[业务顾问]，查询返回 2 个手机号
- **THEN** 系统 MUST 向这 2 个手机号分别发送钉钉消息

#### Scenario: 未查询到任何收件人
- **WHEN** 查询返回空列表
- **THEN** 系统 MUST 记录 warn 日志并跳过发送，不抛异常

### Requirement: 钉钉同步发送
系统 SHALL 对每个收件人手机号同步调用 `IMessageApi.sendDingTalkPersonalMessage` 发送钉钉消息。任一手机号发送失败 MUST 仅记 warn 日志，继续发送其余手机号。

#### Scenario: 部分发送失败
- **WHEN** 3 个收件人中第 2 个发送时 Feign 调用超时
- **THEN** 第 1 个正常发送，第 2 个记 warn 日志，第 3 个继续发送

#### Scenario: 全部发送成功
- **WHEN** 所有收件人均发送成功
- **THEN** 系统 MUST 记录 info 日志，含 bizCode 和收件人数量

### Requirement: 通知配置
系统 SHALL 支持 `meos.bizcheck.notify` 前缀的 YAML 配置，包含：
- `enabled`：布尔值，是否启用通知（默认 `true`）
- `group-code`：组织编码（默认 `YuShu`）
- `position-names`：岗位名称列表（默认 `[业务顾问]`）

当 `enabled = false` 时，DingTalkBizCheckNotifier MUST 跳过全部通知逻辑。

#### Scenario: 配置关闭通知
- **WHEN** `meos.bizcheck.notify.enabled = false`
- **THEN** DingTalkBizCheckNotifier.notifyIfAbnormal 直接返回，不查询不发送

#### Scenario: 使用默认配置
- **WHEN** 未配置 `meos.bizcheck.notify` 相关项
- **THEN** enabled 默认 true，group-code 默认 YuShu，position-names 默认 [业务顾问]

### Requirement: 不去重不限制触发频率
系统 MUST NOT 对通知做去重或限频处理。每次 `startCheck` 完成入库后，只要存在异常项就发送通知，无论与上次通知内容是否相同。

#### Scenario: 连续手动触发
- **WHEN** 用户在短时间内连续点击手动触发按钮，且每次 startCheck 均检测到异常
- **THEN** 每次入库完成后 MUST 各发送一次通知

#### Scenario: 定时任务连续检测到异常
- **WHEN** XXL-Job 连续多次执行自动检查，且每次结果均存在异常
- **THEN** 每次入库完成后 MUST 各发送一次通知
