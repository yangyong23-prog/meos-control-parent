# 集成执行与验证清单

## 代码修复

- [x] 将关系查询迁出对象模型写服务：对象 Key 与通用关系拆成独立方法；对象入口接收完整 RWD 快照并统一投影对象与模型视图，关系入口只接收关系枚举和带 `id/relObjIds` 的 RWD 对象集合；更新末端、照明、白名单、模板调用链。
- [x] 根据新方案停止末端、照明旧版运行结果缓存构建，并将未 READY 消费分支统一改为实时查询。
- [x] 已明确 `cache_info_code_template` 继续作为模板过滤元数据；末端白名单只保存新版 Key，二者均不依赖旧版运行结果缓存。
- [x] 删除末端、照明消费端的旧缓存读取分支，确认 READY 后仍只读新版且不回退。
- [x] 模型视图接口未 READY 时按对象 ID 实时查询 RWD；对象和规则实时查询允许保留实时服务内部既有 Redis 短缓存。

- [x] 补充边缘端 `V7.260701.0.1__cache_object_model.sql`，并保证与云端迁移内容完全一致；数据同步实现仍由云边同步项目组负责。
- [x] 将云端、边缘端的缓存对象模型任务类收拢到 `task.cacheobjectmodel`，参数放入其 `param` 子包，测试目录镜像生产包结构；Bean 名和 XXL-JOB handler 名保持不变。
- [x] 将末端白名单从新旧双写收敛为只替换新版 Key；切换全量或删除时只清理新版键关联。
- [x] 末端白名单空间 READY 后读取新版对象模型，未 READY 时读取新版 Key 中的配置 ID 并实时查询 RWD。
- [x] 删除末端空间设备 HTTP 参数和 `RunningMonitorSpaceQueryParam` 中的 `useWhiteList`，业务查询只按壳配置选择固定白名单或项目空间范围；缓存构建继续固定使用项目全量空间。
- [x] 修正首版白名单 ID 一次性迁移：数据库未 READY 且新版 Key 为空时先读取旧表；有效旧 ID 直接迁移并在同一事务补写 `WHITE_LIST`，不受默认 `FULL` 阻断；无旧 ID 时按当前壳配置处理，READY 后不再回读历史残留。
- [x] 策略调度合并 `buildCacheData(...)` 返回值；任一策略返回 `false` 时整次构建失败、不打印成功日志且不写 READY。
- [x] 已确认取消末端白名单及其他缓存键的七表数量核验；启用不再依赖期望数与实际数比较。
- [x] 移除一级类型对象模型上一版快照、对象主表跨 Key 差集和无所有权主表全局孤儿清理，统一收敛为单 Key 短事务替换；模板元数据继续维护自身范围，照明关系改为每个 Key 独立删除并重建。
- [x] 空结果只删除新版键关联，不删除或写入旧版运行结果缓存，不扫描对象、模型视图或无所有权关系主表；照明关系按当前 Key 替换前后回路 ID 分方向删除后写入本次结果，不枚举其他 Key；手动初始化传播新写入异常，DTX 事件边界只记录故障。
- [x] 审计对象、视图、明细和规则读取的 READY 与部分命中语义；规则业务读取直接按对象、信息点和规则类型查询主表，缓存 Key 只保留写入归属。
- [x] `/airTerminal/listSpaceEquipment` 通过接口专用响应移除空间和设备模型视图字段，READY 对象读取不再请求这些字段；共享 Graph VO 与 `useCache=false` 重建路径保持不变。
- [x] 新增 `/cacheRwd/listModelViewFieldsByObjectId`，READY 后按对象 ID 批量查询模型视图，仅投影 `id`、`view_data`，平铺实际字段并返回部分命中结果。
- [x] 新增 `/cacheRwd/listAirTerminalControlSpaces`，READY 时按项目全量空间 Key 查询、未 READY 时实时计算，固定返回四个基础字段且不受当前白名单模式影响。
- [x] `/debug/cache` 只保留当前项目缓存对象模型 init；移除 validate 入口和核验任务薄入口，类型仍限制为末端、照明。
- [x] 单类型完整构建成功后在构建锁内持久化 `READY` 并刷新 Redis；构建失败保持原状态且不写 `FAILED`。
- [x] 为云端 READY 启动恢复和边缘启动/定时收敛任务类增加共享 `project.cache.rwd.enabled` 条件注册；默认注册，显式关闭时两个 Bean 均不注册，两端重启后生效，并补充条件注解契约、轻量 Spring 上下文与原行为聚焦测试。
- [x] 移除独立数量核验任务、服务、报告及其生产注册，保留 13 号历史任务记录契约变更。
- [x] 将本次缓存表 Mapper 多值写入统一调整为 1000 条分片；普通主键、对象 ID 和缓存 Key `IN` 读取改为一次 SQL，复合业务键与关系范围 `OR` 条件继续保持 500 条，旧 `cache_rwd_object` 的 MyBatis-Plus 写入不在本次调整范围。
- [x] 审计本次新增代码的过度封装：将照明关系读取收回运行监测策略，将模板明细写入收回扩展组件，将缓存模型任务公共 Support 收回唯一任务处理器，并内联三个无独立职责的单调用转发方法；保留事务、仓储、生命周期、策略和扩展点边界。

## 数据库验证

- [x] 执行云端 Flyway 迁移并重复执行确认幂等。
- [x] 使用单项目 10,000 对象、20 模板键和共享关联构造七表基准。
- [x] 记录七表数量、500 条批次、SQL 长度、`EXPLAIN ANALYZE`、事务时长和 MySQL 容器内存观测。
- [x] 结果保存于 `research/integration-validation-results.md`。

## 最终回归

- [x] common 缓存对象模型聚焦测试；本轮调整后重新执行状态服务、模板许可枚举、末端白名单模型视图、运行监测和照明路由 5 个测试类，共 10 个用例全部通过。
- [x] 七表 Mapper 集成测试代码保留 3 个用例；此前隔离 MySQL 记录只作 SQL 兼容性参考，不代表真实数据库环境验收，按最终要求不再执行数据库验证。
- [x] 删除历史数量核验专用测试；按用户要求本轮不新增或运行测试，通过静态调用链检查和云端、边缘端编译确认构建成功直接 READY、失败状态不变及单个手动初始化任务注册语义，READY 恢复继续保留。
- [x] edge READY 收敛测试（1 个），连续两轮验证 READY 到达后不再查询数据库。
- [x] 全模块 fork 编译、`git diff --check`、未使用 import 和禁止异常/JSON API 静态扫描。
- [x] 逐条核对云端与边缘端 Flyway 一致性、单个手动初始化 XXL-JOB、取消的 13 号历史任务、云边同步交付边界和 20 个子任务映射。

本轮路由和许可规则调整共执行 10 个受影响的纯单元用例，`Failures=0`、`Errors=0`；未执行数据库用例。

最终收口未新增或执行测试；`common`、`meos-control-compute`、`meos-control-compute-edge` 均以 `-DskipTests compile` 编译通过，`git diff --check` 和新增文件 trailing-whitespace 扫描通过。

本次 `/airTerminal/listSpaceEquipment` 响应投影调整未新增或执行测试；定向 `git diff --check` 和生成的 MapStruct 转换代码检查通过，确认专用响应没有模型视图字段。对照确认首次全模块编译成功后，`AirTerminalConverter` 使用了错误的嵌套类型注解位置 `@NonNull Outer.Inner`，该写法会中断当前 Java 8 MapStruct/Lombok 注解处理链，并连带产生大量历史类 `onConstructor_`、`Fields` 未生成的误导错误。改为将注解标在嵌套类型 simple-name 上的 `Outer.@NonNull Inner` 后，保留完整 JSpecify 空值契约并恢复全模块编译；未修改 POM、构建配置或历史业务类。

本次 `/cacheRwd/listModelViewFieldsByObjectId` 未新增或执行测试，也未连接数据库；`common` 使用 `-DskipTests clean compile` 编译通过，接口相关文件定向 `git diff --check` 通过。查询只投影 `id`、`view_data`，几千个对象 ID 使用一次普通 `IN` SQL，未新增逐对象 SQL。

本次统一批量写入分片仅修改 `CacheBatchExecutor` 的固定上限，从 500 条调整为 1000 条；所有使用该执行器的缓存 Mapper 多值写入同步生效。后续性能复核将普通主键、对象 ID 和缓存 Key `IN` 读取调整为一次 SQL，模板明细、规则完整业务键及关系范围等复合 `OR` 条件仍按 500 条控制长度；旧 `cache_rwd_object` 的 MyBatis-Plus `saveBatch` 保持不变。按要求未新增或运行测试，也未连接数据库。

先前记录的 Debug init + validate 方案已被最新契约取代：当前只保留按请求上下文执行指定一级类型的 init，不调用 XXL-JOB 全集团遍历；validate、数量核验任务及 `FAILED` 生产写入均取消。全仓残留搜索、`git diff --check`、`mvn.cmd -pl saas/meos-control-server -am -DskipTests compile` 和 `mvn.cmd -pl local/meos-control-compute-edge -am -DskipTests compile` 均通过；仅有项目既有编译告警和换行符提示，未运行测试或数据库验证。

13 号取消任务已按 Trellis 标准归档；其余实现与后续接口收敛均已提交，最终文档审核和
父子任务归档在 2026-07-29 的 UYAI-1040 收尾中完成。

本轮过度封装收敛后，`common`、`meos-control-common`、`meos-control-compute`、`meos-control-server` 以 `-DskipTests compile` 编译通过；`git diff --check`、删除类残留引用和新增文件尾随空白检查通过。未运行测试或数据库验证。

本轮 READY 自动任务开关改为两端任务类的 `@ConditionalOnProperty`，不再向 `NacosSwitchConfig` 增加字段，也不在任务方法中运行时判断。云端聚焦测试 4 个、边缘聚焦测试 3 个全部通过，覆盖注解四属性、`false` 不注册 Bean 以及原 READY 恢复/收敛行为；`meos-control-compute`、`meos-control-compute-edge` 及其依赖以 `-DskipTests compile` 编译通过。未运行数据库或真实浏览器验证。

本轮对象模型写入 API 收敛后，`common` 编译通过；6 个聚焦测试类共 14 个纯单元用例全部通过，覆盖对象投影、关系入参转换与事务前校验、末端/照明调用边界，以及模板正反向关系按主体合并。审查补充了非法目标 ID、重复主体和双向同类型关系合并回归断言；旧准备服务、写入计划和关系查询参数残留搜索、`git diff --check` 及改动文件尾随空白检查通过。未执行数据库或真实浏览器验证。

## 完成与归档结论

- 缓存对象模型主体提交：`e79be52e5b`。
- READY 自动任务开关提交：`766713b4ad`。
- 完整缓存字段查询提交：`b754be327b`。
- 对象 Key 与通用关系写入边界提交：`39f285d200`。
- 集成 PRD 的代码级验收项均已完成，当前代码与
  `.trellis/spec/backend/cache-object-model.md` 一致。
- 真实 RWD、Redis、XXL-JOB、生产数据库迁移和生产规模性能验证保留为发布环境检查，
  不阻塞本任务及父任务归档。
