## 1. Common 模块 — 接口与钩子

- [x] 1.1 在 `common/.../structure/bizcheck/notify/` 下创建纯接口 `BizCheckNotifier`，定义 `void notifyIfAbnormal(String bizCode)` 方法，无 Spring 注解
- [x] 1.2 在 `BizCheckDomainServiceImpl#startCheck` 末尾追加钩子代码：通过 `applicationContext.getBeansOfType(BizCheckNotifier.class)` 获取所有实现，for 循环 + try/catch 调用 `notifyIfAbnormal(bizCode)`，catch 记 error 日志不向上传播

## 2. Saas Common 模块 — 配置类

- [x] 2.1 在 `saas/meos-control-common/.../saas/bizcheck/notify/` 下创建 `BizCheckNotifyProperties`，使用 `@ConfigurationProperties(prefix = "meos.bizcheck.notify")`，字段：`enabled`（boolean，默认 true）、`groupCode`（String，默认 YuShu）、`positionNames`（List<String>，默认 [业务顾问]）
- [x] 2.2 在 `application-saas*.yml` 中添加 `meos.bizcheck.notify` 配置块（enabled、group-code、position-names）

## 3. Saas Common 模块 — 渲染策略族

- [x] 3.1 在 `saas/meos-control-common/.../saas/bizcheck/notify/render/` 下创建接口 `NotifyTextRenderer`，定义 `BizCheckStrategyTypeEnum supports()` 和 `List<String> render(BizCheckResultVO vo)`
- [x] 3.2 创建 `NotifyTextRendererRegistry`，通过 `applicationContext.getBeansOfType(NotifyTextRenderer.class)` 获取所有实现，提供 `NotifyTextRenderer dispatch(BizCheckStrategyTypeEnum type)` 路由方法
- [x] 3.3 实现 `WorkCalendarNotifyRenderer`：三个细项（供冷系统服务空间营业时间、所属空调季节、空调工况）各自独立成行；营业时间合并当日+次日系统列表按 localName 排序去重；仅 status=EXCEPTION 的细项输出
- [x] 3.4 实现 `HvacOperationStandardNotifyRenderer`：模板异常输出"运行模板"行，温度门限异常输出"温度门限值"行，同时异常各占一行
- [x] 3.5 实现 `UnitPriceNotifyRenderer`：渲染 1 行"单价配置检查-当日电价数据：状态描述"
- [x] 3.6 实现 `SystemRelationshipDiagramNotifyRenderer`：渲染 1 行，列出全部系统并标注（已发布/未发布），多系统用顿号分隔

## 4. Saas Common 模块 — 收件人解析与发送

- [x] 4.1 在 `saas/meos-control-common/.../saas/bizcheck/notify/sender/` 下创建 `NotifyReceiverResolver`，注入 `GroupUserClient`，实现 `List<String> resolvePhoneNumbers(String projectId)` 方法，实时同步调用 `queryByPositionNamesAndProjectId`，参数来自 `BizCheckNotifyProperties`
- [x] 4.2 在同一包下创建 `DingTalkNotifySender`，注入 `IMessageApi`，实现 `void send(String phone, String messageContent)` 方法，调用 `sendDingTalkPersonalMessage`；单个发送失败记 warn 日志继续下一个

## 5. Saas Common 模块 — 通知编排实现

- [x] 5.1 在 `saas/meos-control-common/.../saas/bizcheck/notify/` 下创建 `DingTalkBizCheckNotifier`，实现 `BizCheckNotifier` 接口，标注 `@Service`
- [x] 5.2 在 `DingTalkBizCheckNotifier#notifyIfAbnormal` 中实现完整编排：①检查 enabled 配置 → ②调用 `BizCheckServiceImpl#getCheckResults` 查询结果 → ③按 checkStatus=REJECTED AND 内层 status=EXCEPTION 筛选 → ④通过 Registry 分派到各 Renderer 渲染 → ⑤计算 allLines.size() 作为异常总条数 → ⑥按消息模板组装文本 → ⑦调用 `NotifyReceiverResolver` 解析收件人 → ⑧调用 `DingTalkNotifySender` 逐人发送 → ⑨记录 info 日志

## 6. 联调验证

- [ ] 6.1 云端启动验证：确认 `BizCheckNotifier` Bean 被正确注册，`getBeansOfType` 返回 `DingTalkBizCheckNotifier`
- [ ] 6.2 边端启动验证：确认 `getBeansOfType(BizCheckNotifier.class)` 返回空 Map，startCheck 正常执行无异常
- [ ] 6.3 手动触发测试：前端触发检查，确认钉钉通知消息格式与模板一致（含异常总条数字段）
- [ ] 6.4 自动触发测试：XXL-Job 自动触发检查，确认通知正常发送
- [ ] 6.5 异常场景测试：关闭 enabled 配置，确认不发送；模拟 Feign 不可用，确认主流程不受影响
