# 实施记录：需求配置结果预计算

## 实施状态

- 状态：主体功能、查询期补全和 Gateway 边界均已完成并提交，本轮归档；
  部署环境验证保留为发布前检查。
- 主体代码提交：`62cd8a3f16`
- 稳定核心配置与查询期补全收口：`e79be52e5b`
- 最终 Gateway 查询边界：`0329d3d4ff`
- 任务编号：`UYAI-1040`
- 当前分支：`feat_v7.260701.0_性能优化`

## 已完成内容

1. 新增云端和边缘端 `demand_config_result`、`demand_config_result_snapshot` 迁移脚本及查询索引。
2. 新增结果表和快照表 Entity、Mapper、Service、Repository，支持当前结果查询、已有有效快照窗口的历史结果查询、分片批量插入、更新、删除和快照关闭。
3. 新增 `DemandConfigResultRefreshRepository`，统一实现局部刷新、模板刷新、模板类型刷新、DTX 刷新、黑名单清理和屏蔽标识更新。
4. 结果批量同步使用模板/场景范围写锁，在锁和事务内查询旧结果、计算差集、删除旧结果、upsert 新结果并维护快照。
5. 结果业务键写入前按业务键去重；当前表不增加 UNIQUE 约束，以兼容云边同步。
6. 新增 `POST /demand/config/listConfigResultsByInstances`，使用继承 `BaseDemandTemplateParam` 的
   `DemandConfigResultBatchQueryParam`，支持 `templateId > templateTag > date` 模板解析、当前/历史结果查询、
   必传 `instanceType`、可选 `instanceIds`、指定类型空 ID、场景、屏蔽过滤和大实例 ID 集合
   500-ID 分片查询，返回 `instanceId`、`shielded` 和 `finalConfig`，按需增加 `instanceTag`。
7. `DemandConfigInfoCodeAnalyzeStrategy` 已切换为查询需求配置结果，不再执行实时继承链路。
8. 配置新增、编辑、删除、清空和取消引用通过事务内事件刷新当前实例及下级实例。
9. 模板新增生成结果，模板删除清理结果；模板 UPDATE / APPLIED 不刷新。当前不订阅场景事件，保留未来照明场景业务键能力。
10. DTX 发布按项目异步加锁刷新支持的模板类型；DTX 规则发布不刷新需求配置结果。
11. 黑名单新增删除结果、黑名单移除重算结果；屏蔽新增或移除只更新 `is_shielded`，不删除原始需求配置、不重算最终配置。
12. 新增 `demandConfigResultInitTaskHandler`，任务定义自动创建但无调度表达式，首次上线由运维手工触发。
13. 初始化任务支持 XXL-JOB 分片和可选集团/项目过滤；单项目失败不阻断后续项目，任务结束统一返回失败。
14. Debug 初始化接口直接调用刷新仓储处理当前请求上下文，不复用全集团任务遍历逻辑。
15. 统一使用 JSpecify 空值注解、`ObjectMapperUtils`、`this.` / `super.`、项目 Javadoc 和方法顺序规范。
16. 模板类型全量刷新预取一次需求空间和屏蔽空间，并以 3 个模板为并发窗口复用公共 TTL 线程池。
17. 保留原 `DemandConfigAirconditoningAware` 类和实现，新增结果专用
    `DemandConfigAirconditioningQueryEnricher`；结果刷新改走 `DemandConfigGateway.listStableConfigsByInstances()`，
    结果查询反序列化后再执行查询期补全，旧实时和 origin 查询继续走原生命周期。
18. 稳定 Gateway 入口强制开启继承并关闭实例名称，只跳过原空调 Aware 的 `afterAggregateInit`，
    普通和 origin 查询继续按原顺序执行全部生命周期。
19. 结果JSON序列化后防御性删除空调 `valueSelect/workCondition`，避免历史脏原始配置进入主表或快照表。
20. 补齐结果补全器、旧 Aware 兼容和 Gateway 边界回归：当前/未来日期走全局日历、历史走日标签、
    仅 PROJECT 补工况；普通和 origin 入口保留原 Aware，stable 仅跳过原 Aware，其他生命周期不受影响。

## 关键正确性结论

- 批量刷新实时结果为空时仍会在当前模板/场景边界删除旧结果，避免物理世界范围收缩后残留数据。
- 差集查询位于写锁事务内，避免锁外查询旧结果后并发写入造成漏删。
- `isCustomizedConfig` 和 `isShielded` 使用真实业务值，不通过默认值方法覆盖。
- 结果和快照 XML 批量写入前补齐主键及审计字段；批量写入不使用 `saveBatch`。
- 当前仅空调需求启用结果预计算；照明有场景但本期不启用，场景维度仍保留在业务键、查询、快照和锁范围中。
- 结果读接口 miss 时返回空列表，不回退实时计算；原实时接口行为保持不变。
- 结果读接口按公共模板解析契约获取模板；模板不存在时记录项目级警告日志并返回空列表，
  不查询结果表或慢链路；参数校验和系统异常继续向上抛出。
- 结果读接口的公共参数使用独立 `instanceType` 和 `instanceIds`；服务层直接设置仓储独立的实例类型
  和实例 ID 条件。`instanceType` 必传，实例 ID 为空时查询该类型全部结果。
- `checkAndSet()` 将 `instanceIds` 的 `null` 元素过滤到新集合后回写；清理后为空时按指定类型全部结果查询，
  且不会原地修改调用方集合。
- `instanceType` 使用 JSpecify `@NonNull` 和 Bean Validation `@NotNull`；标准 `@NotEmpty` 不支持枚举类型。
  `instanceIds` 和清理前集合元素使用 JSpecify `@Nullable`。
- `fillInstanceName` 默认值为 `false`，在最终结果轻量 VO 路径中为兼容性非操作参数；
  `autoFillEmptyInstanceIds` 不触发 RWD。
- `filterShieldedSpaces` 默认值为 `true`；显式关闭过滤时，同一次结果表或快照表查询会返回屏蔽对象，
  并将 `is_shielded` 透传到轻量 VO 的 `shielded` 字段，不增加额外查询。
- `queryInstanceTag` 默认值为 `false`；显式开启时按前端对象范围读取实例配置并批量查询自定义实例标签，返回 `instanceTag`，
  不在结果循环中逐条调用外部接口。结果表不保存黑名单对象时，从实例配置补充请求范围内的黑名单对象，返回空 `finalConfig`；
  默认屏蔽过滤同样生效。
- 结果表和快照表只保存稳定核心配置：完整季节配置、继承结果和空间功能类型合并结果；不保存营业日历裁剪结果、
  项目 `valueSelect/workCondition`、当前DTX/RWD对象或本地化名称。
- PROJECT 结果查询按日期调用项目工况服务，SPACE/FLOOR/BUILDING 不调用；所有空调结果按查询日期过滤季节，
  非空调模板直接跳过空调补全器。DTX规则或营业日历变化因此在下一次查询立即生效，无需刷新结果表。
- `instanceTag.customConfig` 只取结果行 `isCustomizedConfig`，表示原始 `demand_config` 自身配置；PROJECT 查询期从独立项目工况表补入动态字段后仍不改变该标签。
- 普通 `listConfigsByInstances()` 继续在 PROJECT 未传日期时向工况服务透传 `null`；任一实时聚合缺失最终配置/环境品质/温度品质时整批提前结束，且首条不完整时不额外读取默认季节。
- `DemandConfigAirconditoningAware` 与当前分支 `HEAD` 内容一致；Gateway 不注入结果补全器，普通和 origin
  查询继续调用原 Aware，stable 查询只跳过原 Aware 并保留其他生命周期。
- 全量初始化只重建全项目范围的当前稳定结果，并从任务执行日写入开放快照；不回填初始化日之前历史，不修复旧关闭快照。查询该日之前的日期继续 miss，不回退实时或当前主表。

## 验证结果

已执行：

```text
mvn -pl saas/meos-control-compute,saas/meos-control-server -am test
mvn -pl common "-Dtest=DemandQueryServiceImplTest,DemandConfigResultServiceImplTest,DemandConfigInfoCodeAnalyzeStrategyTest" test
mvn -pl common -DskipTests compile
javac -encoding UTF-8 -source 8 -target 8 -cp <common test classpath> -d common/target/test-classes <5 focused test files>
mvn -pl common surefire:test "-Dtest=DemandConfigAirconditoningAwareTest,DemandConfigAirconditioningQueryEnricherTest,DemandConfigGatewayImplTest,DemandQueryServiceImplTest,DemandConfigResultRefreshRepositoryImplTest"
```

- 公共模块：39 个测试通过。
- 计算模块：6 个测试通过。
- 服务模块：编译通过。
- 本次参数兼容修复：聚焦纯单元测试 15 个通过；本轮早期 `common` 生产代码编译通过。
- 本次查询期补全收口：最终工作树执行 `mvn -pl common -DskipTests compile` 成功，编译 2630 个生产源文件；
  5 个聚焦测试文件隔离编译通过，5 个测试类共 31 个测试通过。
- 标准聚焦测试命令仍在 `testCompile` 阶段被并行缓存对象模型测试的 31 个无关 API 漂移错误阻断，
  主要位于 `CacheRwdAirTerminalWhiteListStrategyTest` 和 `CacheObjectExtensionProjectorTest`；
  本任务未修改相关缓存重构文件。
- `git diff --check`：通过。

## 部署前验证

- 在目标 MySQL / 人大金仓环境执行 Flyway 迁移验证。
- 对结果当前查询、历史查询、按模板类型删除和快照关闭 SQL 执行 EXPLAIN。
- 使用接近生产规模的数据验证模板并发 3、批量大小 500 和写锁 120 秒等待上限。
- 在 XXL-JOB 控制台确认任务已自动创建、无调度表达式且不会自动执行。
- 首次上线先按集团或项目过滤灰度触发，再执行全量初始化；本次稳定/动态存储边界调整后也必须全量重跑，
  失败项目根据日志单独重跑。旧动态JSON中已经删除的季节项无法靠查询期补全恢复；此重跑只重建当前结果和执行日开放快照，不修复更早历史。

## 回滚与重跑

- 新接口和旧实时接口并存，调用方可切回旧链路。
- 初始化和模板刷新具备当前结果幂等重跑能力；单模板已提交而其他模板失败时，可重新执行相同项目或模板类型。重跑不能倒推初始化日之前历史或重写旧关闭快照。
- 表结构需要回滚时按发布流程处理迁移脚本；业务代码不得依赖临时需求文档目录。
