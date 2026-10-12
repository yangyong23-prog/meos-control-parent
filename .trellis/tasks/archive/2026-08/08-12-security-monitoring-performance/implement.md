# 安防运行监测性能优化实施计划

## 实施前置

- [ ] 确认 PRD 的性能验收门槛并更新 AC8。
- [x] 读取 backend/cache-object、demand-result 及相关安防规范。
- [ ] 固化同一项目的数据规模、请求参数、优化前响应样本和阶段耗时。
- [x] 确认设备接口仍一次返回模型图字段；不新增模型图接口或前端二次合并流程。

## 子任务执行顺序

### 1. `08-12-security-static-cache`

- [x] 完成设备、空间、`service_space`/`in_space` 关系缓存构建、刷新、删除和 READY 路由。
- [x] 完成 `function`、`listDeploymentSpace`、`listEquipment` 静态读取切换。
- [x] 验证设备接口普通字段和 `bimId`、`bimLocation`、`outline` 同响应返回。
- [x] 子任务验收通过后，交付缓存读取契约和调用计数证据。

### 2. 并行推进 `08-12-security-dashboard-demand` 与 `08-12-security-dynamic-statistics`

- [x] Dashboard 子任务：切换静态关联，接入 ARMING/PASSING 结果预计算并固定读取结果表，结果 miss 按无配置处理。
- [x] 动态统计子任务：完成通行空间一次聚合、设备统计范围收窄和异常边界，未修改通用 IoT 查询。
- [x] 两个子任务分别完成结果对照、聚焦测试和模块编译。

### 3. 发布前环境验证（非开发子任务）

- [ ] 在可访问环境执行跨接口旧/新路径结构化回归。
- [ ] 串行验证 SECURITY 未 READY、READY 冷读、READY 预热及动态数据实时性。
- [ ] 记录 P50/P95、阶段耗时、调用次数、响应体大小和错误率，形成性能报告。
- [ ] 发现问题回流对应代码范围；所有发布证据齐全后再进行环境验收。

## 发布前环境验证门

- [ ] 5 条已有性能基线接口均不得回退，并达到启动前确认的性能门槛。
- [ ] READY 路径无目标 RWD 静态调用，未 READY 路径完整可用。
- [ ] 动态 IoT、规则、异常事件和 SSE 状态没有静态旧值泄漏。
- [ ] 设备接口模型图字段完整且前端现有消费无需改动。

## 验证命令

根据实际改动模块收敛测试类，至少执行：

```powershell
mvn -pl common -DskipTests compile
mvn -pl common test
mvn -pl common,saas/meos-control-server,local/meos-control-server-edge -am -DskipTests compile
git diff --check
python ./.trellis/scripts/task.py validate 08-12-security-monitoring-performance
```

真实数据库集成测试与接口压测由可访问部署环境的验证方串行执行，不能与真实浏览器验证并发占用共享资源。

## 性能复测协议

- 使用与基线相同的项目快照、部署规格和网络环境。
- 每个场景至少预热 3 次、采集 10 次，报告 P50/P95，不以单次最好结果验收。
- 同时报告总耗时、静态对象、关系、需求配置、IoT、异常事件、聚合阶段、响应体大小和下游调用次数。

## 高风险文件与回滚点

- 安防服务/策略：保留旧方法作为未就绪路径，按子任务逐项切换。
- 缓存构建与状态服务：写入失败不标 READY，范围替换失败不删除上一份完整数据。
- 需求结果支持范围：按模板类型启用；安防初始化失败不影响已有需求类型。
- IoT 公共查询实现：共享层改动必须跑现有调用方回归；收益不稳定时回滚共享改动。
- 模型图字段：与设备数据一起返回，不新增请求；回滚时保持原设备接口契约。
