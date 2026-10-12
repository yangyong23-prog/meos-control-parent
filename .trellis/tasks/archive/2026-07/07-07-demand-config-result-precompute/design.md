# 技术设计：需求配置结果预计算

## 总体方案

新增结果表保存继承合并后的稳定核心需求配置。本期仅为空调需求生成结果；写入链路在空调配置、模板、实例或 DTX 数据变化后同步刷新受影响范围，读取链路直接读结果表并补充查询日期、当前DTX/RWD运行态和本地化相关数据。照明及其他需求类型暂不接入，现有实时查询接口对外行为保持不变。接入范围限制在运行时入口，底层数据模型、业务键、读取参数和刷新契约继续支持未来照明按真实 `sceneId` 接入。

## 数据模型

### `demand_config_result`

用于保存最新最终配置。实际表结构如下：

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | bigint | 否 | 无 | 主键 |
| `project_id` | varchar(50) | 否 | 无 | 项目 ID |
| `demand_template_type` | varchar(32) | 否 | 无 | 模板类型 |
| `demand_instance_type` | varchar(32) | 否 | 无 | 实例类型：PROJECT / BUILDING / FLOOR / SPACE |
| `demand_instance_id` | varchar(64) | 否 | 无 | 实例 ID |
| `final_config_json` | JSON | 是 | NULL | 最终合并后的 `DemandConfigValueObject` 完整序列化 |
| `is_customized_config` | tinyint | 否 | 0 | 是否自身存在自定义配置 |
| `is_shielded` | tinyint | 否 | 0 | 是否屏蔽空间 |
| `inherit_instances_json` | JSON | 是 | NULL | 从自身到项目的继承链路 |
| `rel_template_id` | bigint | 否 | 无 | 关联 `demand_template.id` |
| `rel_scene_id` | bigint | 是 | NULL | 关联 `demand_scene.id`；本期空调结果统一为 NULL，未来照明结果写入真实场景 ID |
| `create_user` | varchar(64) | 是 | NULL | 创建人或更新来源 |
| `create_time` | datetime | 否 | 无 | 创建时间 |
| `update_user` | varchar(64) | 是 | NULL | 更新人或更新来源 |
| `update_time` | datetime | 否 | 无 | 更新时间 |

实际索引：

- `PRIMARY KEY (id)`
- `INDEX idx_project_template_scene_instance(project_id, rel_template_id, rel_scene_id, demand_instance_type, demand_instance_id)`
- `INDEX idx_project_template_instance(project_id, rel_template_id, demand_instance_type, demand_instance_id)`
- `INDEX idx_project_template_type_instance(project_id, demand_template_type, demand_instance_type, demand_instance_id, rel_template_id, rel_scene_id)`

不增加 UNIQUE 约束，避免影响云边同步；唯一性由 Repository 层按包含可空 `rel_scene_id` 的业务键维护。不得因为本期只生成空调结果而从业务键或索引中删除场景维度。

### `demand_config_result_snapshot`

字段与主表一致，额外包含：

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `valid_end_time` | datetime | 否 | 无 | 当前历史版本生效截止时间 |

应用层维护 SCD2 语义：

- 插入新结果时，同时插入快照，`valid_end_time = '9999-12-31'`。
- 更新结果前，先关闭旧快照，再插入新快照。
- 删除结果前，关闭当前快照。
- 主表变更与快照变更必须保持一致事务边界。
- 首次初始化或首次写入只为当前稳定结果创建开放快照；SCD2 历史从该写入日开始积累，不反推、回填更早版本，也不修改旧关闭快照。

## 服务边界

新增独立刷新仓储 `DemandConfigResultRefreshRepository`。该仓储负责：

- 根据实例集合刷新结果表。
- 根据空调模板批量刷新结果表。
- 根据 DTX 变更实例刷新当前实例及下级实例。
- upsert 主表并维护快照表。
- 更新屏蔽空间标识。
- 过滤黑名单实例，避免写入结果表。

Gateway 和 Listener 只负责触发刷新，不直接散落 upsert 与快照归档细节。
通用刷新方法继续接收可空 `sceneId`。本期所有空调入口传 `null`，但 Repository 的查询、差集删除、upsert、快照和锁范围必须能以非空真实 `sceneId` 隔离数据，为未来照明接入保留能力。
结果刷新仓储实现维护唯一的支持需求类型集合，并派生支持的模板类型列表。Listener、初始化任务、Debug 接口以及仓储公开方法统一调用 `listSupportedTemplateTypes()`、`supportsTemplateType(...)` 或 `supportsTemplate(...)`；未来增加需求类型时不再修改各触发入口。照明在加入支持集合前仍必须先完成场景刷新和清理规则。

## 读取链路

新增 `DemandQueryService.listConfigResultsByInstances(param)`：

1. 接收继承 `BaseDemandTemplateParam` 的 `DemandConfigResultBatchQueryParam`，调用
   `queryAndGetTemplate()` 按 `templateId > templateTag > date` 解析模板；模板不存在时记录包含模板查询条件的
   项目级警告日志并返回空列表，不访问结果表；参数校验或系统异常不在此分支吞掉。
2. 使用解析后的模板 ID 查询预计算结果，不直接信任请求中的原始模板 ID。
3. 按查询参数和日期命中结果数据；未传日期或当前/未来日期查询 `demand_config_result`，历史日期查询 `demand_config_result_snapshot`。历史日期在首次初始化/写入之前或无有效快照窗口时直接 miss，不读当前主表。
4. 未命中直接返回空列表。
5. 转换为轻量 `DemandConfigResultVO<T>`，反序列化 `final_config_json` 为稳定核心 `finalConfig`；请求开启
   `queryInstanceTag` 时增加 `instanceTag`。
6. 对VO集合调用结果专用 `DemandConfigAirconditioningQueryEnricher`，按请求日期过滤空调季，并仅在 PROJECT 查询时
   补充项目级 `valueSelect/workCondition`。该类不提供聚合根入口，也不接入原实时 Gateway。
7. 返回 `instanceId`、`shielded`、可选 `instanceTag` 和补全后的 `finalConfig`。
8. 该接口不返回实例名称，不调用 RWD 填充实例名称；PROJECT 工况补全内部允许按旧实时语义读取当前
   DTX绑点和RWD系统/泵组，其他实例类型不得调用项目工况服务。

`DemandConfigResultBatchQueryParam` 保留批量结果读取需要的 `sceneId`、独立的 `instanceType` /
`instanceIds`、`autoFillEmptyInstanceIds`、`filterShieldedSpaces` 和 `fillInstanceName`。查询服务将
`instanceType` 和 `instanceIds` 直接写入仓储独立的实例类型、实例 ID 查询条件：

- `instanceType` 必传；查询服务始终设置实例类型条件。
- `instanceIds` 为 `null` 或空集合：不设置实例 ID 条件，查询指定类型全部结果。
- `checkAndSet()` 先将 `instanceIds` 中的 `null` 元素过滤到新集合并回写，避免修改调用方传入的
  不可变集合；实例 ID 条件沿用结果服务 500-ID 分片，清理后为空时查询指定类型全部结果。
- `instanceType` 按 JSpecify `@NonNull` 建模并使用 Bean Validation `@NotNull`；标准 `@NotEmpty` 不支持枚举。
  `instanceIds` 和清理前的集合元素按 JSpecify `@Nullable` 建模。

`fillInstanceName` 默认值为 `false`，对不含名称字段的轻量 VO 无操作，不得触发 RWD。
`autoFillEmptyInstanceIds` 也不触发 RWD，指定类型空 ID 直接表示该类型全部结果。
`filterShieldedSpaces` 默认值为 `true`，查询条件使用 `is_shielded = false`；显式传入 `false` 时不增加
屏蔽条件，并在同一次按需查询中投影 `is_shielded` 到响应 `shielded`，不额外访问数据库。
`queryInstanceTag` 默认值为 `false`；开启后结果查询额外投影实例类型和自定义配置标识，并按前端对象范围读取实例配置状态。
由于黑名单对象不会写入结果表，结果查询需要将请求范围内结果表缺失的黑名单对象补成轻量结果行，
其中 `finalConfig` 为 `null`、`instanceTag.inBlacklisted` 为 `true`；默认屏蔽过滤也必须作用于补充行。
随后按请求对象范围批量调用现有需求策略自定义标签查询；未显式传入对象 ID 时使用最终响应范围，禁止在结果转换循环中逐条调用外部接口。
其中 `instanceTag.customConfig` 固定取结果行的 `is_customized_config`，表示原始 `demand_config` 自身配置；PROJECT 查询期从独立工况表生成的 `valueSelect/workCondition` 不参与该字段计算。
标签字段为空时不序列化 `instanceTag`。

`DemandConfigInfoCodeAnalyzeStrategy` 使用该查询服务替换原实时 `DemandConfigGateway` 调用：

- 继续按当前日期定位空调模板，并按当前季节提取温度、湿度等配置。
- 查询范围限定为 `templateId + date + SPACE + spaceIds`，显式关闭实例名称填充。
- 使用结果 VO 的 `instanceId` 和 `finalConfig` 映射 InfoCode，不重建 `DemandConfigAggregate`。
- 结果表或历史快照表 miss 时按无配置处理，不回退实时继承计算。

结果表读取需要沿用现有需求配置 Mapper 的历史日期语义：历史日期通过 `demand_config_result_snapshot`
按 `update_time` / `valid_end_time` 有效期窗口过滤，并用 `row_number()` 保留业务键对应的最新历史版本。
轻量接口仍必须按需查询字段，避免读取继承链路 JSON、审计字段等无关列。
读取参数保留场景过滤语义：`sceneId` 未传时不增加场景条件，传入时按真实场景 ID 过滤。该契约独立于本期是否生成照明结果。

结果表不保存以下查询期字段：营业日历过滤后的 `seasonQuality`、项目 `valueSelect/workCondition`、
当前DTX绑点、RWD系统/泵组和本地化名称。刷新仓储统一调用
`DemandConfigGateway.listStableConfigsByInstances()`；普通 `listConfigsByInstances()` 和 `listOriginConfigs()`
继续通过原 `DemandConfigAirconditoningAware` 执行既有生命周期。历史结果查询使用请求日期的日标签和历史项目自定义记录，
其中工况服务继续按旧链路组合当前DTX/RWD模型。
稳定入口内部强制 `queryInheritConfig=true`、`fillInstanceName=false`，并仅在 `afterAggregateInit` 阶段跳过
原空调查询期 Aware；其他生命周期仍执行。原 Aware 类、普通查询和 origin 查询不做行为调整。
聚合转结果时还会在转换后的 `JsonNode` 上防御性删除 `globalConfig.valueSelect/workCondition`，兼容历史脏原始配置；
不得直接修改聚合对象，以免影响共享继承引用和 `isCustomizedConfig`。
普通实时入口继续保留删除前生命周期行为：PROJECT 未传 `configDate` 时向工况服务透传 `null`；任一聚合缺失最终配置、`environmentQuality` 或 `tdbQuality` 时，当次整批季节重写立即结束，且不额外读取默认季节配置。

新增 Controller 端点调用该查询服务方法。新端点和旧端点并存，调用方自行选择。

## 写入与重算链路

### 局部重算

配置增删改相关方法完成原有写入后，查询当前实例及下级实例，批量计算并 upsert 结果表：

- `addOrUpdateConfig(templateId, instance)`
- `clearConfig(templateId, instance)`
- `cancelReference(templateId, instance)`

实际事务策略：配置变更事件使用 `@TransactionalEventListener(BEFORE_COMMIT)`，原写入与局部结果刷新同事务提交；结果刷新失败时回滚原写入，避免消费方读到陈旧结果。
刷新计算必须走稳定核心配置入口，不能把营业日历空调季裁剪或项目工况写入主表/快照表。

### 批量重算

空调模板、DTX 事件影响范围更大，按事件来源定位受影响实例集合后批量重算：

- 空调模板新增：模板下所有实例。
- 空调模板删除：删除模板下已有结果和开放快照。
- DTX 发布消息：变更实例及其所有下级实例。

批量重算仍按同步语义交付。若实现阶段发现一次事件影响范围过大，需要补充批处理边界、失败重试入口和告警策略后再调整。

模板类型全量刷新只在编排线程完成模板查询、RWD 需求范围预取、屏蔽空间预取和并发任务等待，
不启动覆盖整个模板类型的外层数据库事务。每个模板最终通过
`DemandConfigResultRepository.syncRefreshScope(...)` 进入独立事务并持有对应模板/场景写锁。
任一模板失败时向调用方返回失败，已完成模板可通过幂等重跑再次对齐，不能假设外层事务能够回滚并发子任务。

### 并发写入

- 模板/场景写范围必须使用分布式锁串行化，并持有到 Spring 事务完成。
- 获取锁必须设置有限等待上限，禁止业务线程无限阻塞。
- 超时未获取锁时不得继续查询或写入该范围结果，应记录项目、模板、场景和锁 Key，并向调用方返回失败以便重试。
- 锁租期继续使用 watchdog 或等价自动续期机制，不使用可能短于事务执行时间的固定租期。

### 屏蔽空间

实例屏蔽状态变化只更新结果表 `is_shielded` 字段，不重新计算最终配置。该更新也需要维护主表与快照表一致性。

## 初始化与失败处理

- `JobTask` 仅保留 `@XxlJob("demandConfigResultInitTaskHandler")` 薄入口，业务逻辑集中在 `DemandConfigResultInitTaskHandler`。
- 初始化任务继承 `BaseXXJob` 并实现 `TecXxJobHandler`，复用集团、项目和 XXL-JOB 分片能力。
- 任务定义通过 `InitXxlJob` 自动创建为无调度任务，部署不会自动触发执行；首次上线由运维手工触发。
- 空参数表示全量初始化；`groupCodes` 和 `projectIds` 可用于灰度或失败重跑。非空非法 JSON 直接失败，不能降级为全量初始化。
- `BaseXXJob#processHandle(...)` 保持既有任务的异常传播语义；只有初始化使用的 `processHandleWithoutTimeCheck(...)` 会捕获单项目失败、继续后续项目，并在任务结束后统一返回失败。
- 每个项目执行前初始化集团、租户和项目上下文，并在 `finally` 中清理，避免异常或线程复用导致上下文串线。
- 首次上线以及本次稳定/动态存储边界调整后，运维必须重新全量触发初始化任务；旧动态JSON中已删除的季节项无法在查询阶段恢复。
- 全量初始化只重建执行时的当前稳定结果，并以执行日的写入时间创建开放快照。它不回放初始化日之前的配置，不回填更早历史，也不修复旧关闭快照。

## 边界与兼容

- 旧接口保持不变，降低回归风险。
- 新接口 miss 不降级，避免悄悄回到慢链路。
- 云端负责生成结果数据，边缘端通过同步获得结果表数据。
- 初始化、调试和事件监听只允许空调需求进入刷新仓储；非空调事件直接忽略。
- 空调仅在结果刷新仓储的统一支持集合中声明一次，其他入口不得直接硬编码 `DemandTypeEnum.AIRCONDITIONING`。
- 本期不监听需求场景变化，也不生成照明结果；后续接入照明时基于已保留的真实 `sceneId` 业务键和读取/刷新契约补充场景结果生命周期。
- 当前空调运行时统一使用 `sceneId = null`，但不得把“无场景”固化为结果表或通用 Repository 的永久约束。
- 不使用数据库触发器，降低跨数据库兼容风险。
- 本期只切换 `DemandConfigInfoCodeAnalyzeStrategy` 到结果查询，其他现有调用方保持原链路。

## 测试设计

- 结果表命中时，新接口返回聚合根结构与旧接口最终配置等价。
- 模板 ID、模板标签和业务日期均可解析模板，且优先级保持 `templateId > templateTag > date`。
- 模板解析为空时抛出非法输入异常，且不查询原始配置、结果表、实时 Gateway 或 RWD。
- 历史日期查询时，新接口仅从结果快照表返回该日期已存在的有效版本；初始化/首次写入日之前保持 miss，不回退实时链路或当前主表。
- 结果表 miss 时，新接口返回空列表，且不调用实时继承查询。
- Controller 轻量 VO 查询不调用 RWD 名称查询。
- 未传 `filterShieldedSpaces` 时过滤屏蔽对象；显式传入 `false` 时返回屏蔽对象及其真实 `shielded` 标识。
- 未传 `queryInstanceTag` 时不查询外部自定义标签；显式传入 `true` 时按前端对象范围读取实例配置，能返回结果表中缺失的黑名单对象，
  默认过滤屏蔽对象，并且只发生一次批量标签查询。
- 必传实例类型、指定类型空 ID、显式 `instanceType + instanceIds`、场景、屏蔽过滤和 500-ID 分批查询均
  保持兼容语义。
- `instanceIds` 中的 `null` 元素在结果仓储、实时 Gateway 和 RWD 调用前被过滤并回写新集合；
  缺少 `instanceType` 时失败，清理后为空时按指定类型全部结果查询。
- `fillInstanceName` 默认值为 `false`，在结果接口中保持非操作，不改变轻量最终结果。
- Gateway 领域方法在 `fillInstanceName=true` 时按现有命名逻辑填充实例名称。
- `DemandConfigInfoCodeAnalyzeStrategy` 查询结果表后仍能按季节映射温度、湿度、二氧化碳和压差配置。
- 结果刷新调用稳定 Gateway 入口，结果查询调用结果专用空调补全器；历史日期季节过滤、PROJECT 工况补全、
  非项目不查询工况服务和非空调跳过均有单元测试。
- 普通和原始 Gateway 必须调用原 Aware，稳定 Gateway 只跳过原 Aware 且继续执行其他生命周期；
  实时可空日期透传、不完整温度品质聚合的整批提前结束，以及 PROJECT 查询期工况不改变 `instanceTag.customConfig` 均有回归测试。
- InfoCode 结果 miss 时不调用 `DemandConfigGateway` 兜底，且不查询 RWD 实例名称。
- 插入结果时写入主表和开放快照。
- 更新结果时关闭旧快照并插入新快照。
- 删除结果时关闭当前快照。
- 配置新增、修改、清除、取消引用后刷新当前实例及下级实例。
- 空调模板、DTX 事件刷新正确影响范围。
- 非空调需求类型事件不会生成或修改结果数据。
- 非空 `sceneId` 能在业务键、结果读取、快照读取和写锁范围中正确隔离数据；该测试只保护未来扩展契约，不代表本期启用照明计算。
- 写锁在有限等待内无法获取时明确失败且不执行结果写入，正常持锁期间由 watchdog 或等价机制续期。
- 屏蔽空间事件只更新 `is_shielded`，不触发配置重算。
- 黑名单实例不写入结果表。

## 风险点

- 重算范围计算错误会导致下级实例结果陈旧。
- 结果表与快照表归档事务边界不一致会导致历史版本断档。
- 新接口返回结构必须与旧接口兼容，否则调用方切换成本会上升。
- 同步重算可能增加写入耗时，需要在实现阶段评估批量处理性能。
- 分布式写锁若无限等待会占用业务线程和数据库资源，必须使用有限等待和明确失败处理。

## 实际落地与验证

- 模板类型全量刷新按模板并发 3 个任务，项目层和模板类型层保持串行，避免多层并发放大 RWD、数据库连接和公共线程池占用。
- 需求空间和屏蔽空间在模板类型边界各查询一次，模板任务复用不可变集合。
- 实时计算在写锁外执行；结果差集查询、删除、upsert 和快照维护在同一模板/场景写锁与事务内完成。
- 批量插入使用 Mapper XML 分片写入，不使用 MyBatis-Plus `saveBatch`；业务键查询、ID 更新和删除分别按 SQL 形态分片。
- 验证命令 `mvn -pl saas/meos-control-compute,saas/meos-control-server -am test` 已通过，公共模块 39 个测试、计算模块 6 个测试通过，服务模块编译通过。
- 未执行真实数据库迁移、EXPLAIN、线上规模压测和 XXL-JOB 控制台手工触发验证，部署前仍需完成这些环境验证。
