# 需求配置结果预计算

## 目标

通过新增需求配置结果预计算能力，将继承计算后的稳定核心配置持久化到结果表，查询方读取后再补充营业日历、当前DTX/RWD运行态等查询期数据，避免每次请求重复执行空间层级查询、父级配置递归查询和字段级继承合并，同时保证结果与原实时查询一致。

## 实施状态

- 主体功能提交为 `62cd8a3f16`；查询契约、实例标签和屏蔽对象完善已由
  `998faed3f8`、`1aa2f2822a`、`457c4fbd93` 提交，稳定核心配置与查询期补全由
  `e79be52e5b` 收口，最终 Gateway 边界由 `0329d3d4ff` 提交。
- 已交付结果主表、快照表、结果读接口、刷新仓储、事件监听、云端初始化 XXL-JOB、Debug 初始化入口和 InfoCode 调用方切换。
- 当前支持范围统一收敛在 `DemandConfigResultRefreshRepository`，本期仅启用空调需求；照明场景维度保留在数据模型和通用契约中，但未启用运行时计算。
- 主体提交已完成受影响模块测试：`mvn -pl saas/meos-control-compute,saas/meos-control-server -am test`，公共模块 39 个测试和计算模块 6 个测试通过，服务模块编译通过。
- 本轮收口的最终工作树已通过 `mvn -pl common -DskipTests compile`，共编译 2630 个生产源文件；
  5 个聚焦测试类通过隔离编译和 Surefire 执行，共 31 个测试通过。
- 当时标准聚焦命令曾在 `testCompile` 阶段被并行缓存对象模型测试的 API 漂移阻断；
  相关缓存模型代码随后已收口并提交，该历史阻断不再表示本任务存在未提交工作树。
- 已通过 `git diff --check`。未执行真实数据库迁移、线上数据量压测和 XXL-JOB 控制台手工触发验证，这些验证需在部署环境完成。

## 背景与问题

当前需求配置查询链路在每次调用时实时计算继承关系：

- 查询模板、场景、实例、配置和配置组后组装聚合根。
- 进入继承处理时，需要调用 RWD 查询空间层级。
- 递归查询父级配置并处理对象级继承、字段级继承。
- 字段级继承需要反射遍历配置字段进行合并。

根因是配置数据不变时，查询仍重复执行同一套远程调用和继承计算。需要把最终配置提前计算并存储，查询时跳过实时继承链路。

## 范围

### 当前接入边界

- 本期需求配置结果预计算仅接入空调需求 `DemandTypeEnum.AIRCONDITIONING`。
- 空调需求没有场景，当前生成的结果统一使用 `rel_scene_id = null`。
- 只有照明需求使用场景，但本期不生成照明结果；安防、通风等其他需求类型也暂不接入。
- 本期接入边界只限制运行时初始化、监听和计算入口，不得从数据模型、业务键、读取参数或通用刷新契约中删除场景能力；后续照明接入时必须使用真实 `sceneId` 隔离结果。
- 支持结果预计算的需求类型必须由结果刷新仓储中的单一集合统一管理。Listener、XXL-JOB、Debug 和 Repository 内部保护只能调用统一的模板类型列表或支持判断，不得分别硬编码空调；未来扩展支持范围时只调整该集合。
- 初始化、DTX、配置变更、模板变更、黑名单和屏蔽事件均必须先限制到空调需求。
- 后续接入照明时，需要在现有场景业务键能力上补充模板与场景分步创建、变更、删除期间的刷新和清理规则；本期不提前启用照明监听或结果计算。

### 表与持久化

- 新增 `demand_config_result` 主表，仅保存最新的最终配置。
- 新增 `demand_config_result_snapshot` 快照表，保存首次初始化或写入后由应用产生的结果历史版本，不追溯回填此前历史。
- 主表字段包含项目、模板类型、实例类型、实例 ID、最终配置 JSON、自定义配置标识、屏蔽空间标识、继承链路 JSON、关联模板、关联场景、创建/更新信息；关联场景保持可空，空调写入 `NULL`，未来照明写入真实场景 ID。
- 快照表字段与主表一致，并额外增加 `valid_end_time`。
- 应用层维护历史归档，不使用数据库触发器。
- 业务代码维护唯一性，不依赖 UNIQUE 约束，以兼容云边同步。

### 读取接口

- 保留现有 `DemandConfigGatewayImpl.listConfigsByInstances()` 行为不变。
- 新增 Controller 端点：`POST /demand/config/listConfigResultsByInstances`。
- 新接口使用独立的 `DemandConfigResultBatchQueryParam`，继承 `BaseDemandTemplateParam`，模板按
  `templateId > templateTag > date` 优先级解析；按标签或日期解析时要求 `templateType`。
- 新接口按查询日期读取结果数据：未传日期或当前/未来日期查询 `demand_config_result`，历史日期查询 `demand_config_result_snapshot`，反序列化 `final_config_json` 后返回轻量 VO，包含 `instanceId`、`shielded` 和 `finalConfig`；仅在 `queryInstanceTag=true` 时增加 `instanceTag`。历史结果仅在已存在有效快照窗口时保证返回；首次初始化/写入日之前继续按 miss 返回空，不回退实时链路，不使用当前主表冒充历史。
- 读取参数继续保留场景过滤语义：未传 `sceneId` 时不按场景过滤，传入时按真实 `sceneId` 查询，不能因本期空调结果均无场景而移除该能力。
- 读取参数保留独立的 `instanceType`、`instanceIds`、`autoFillEmptyInstanceIds`、
  `filterShieldedSpaces`、`queryInstanceTag` 和 `fillInstanceName`，不向公共接口暴露仓储内部的类型-ID 映射。
- `instanceType` 必传；`instanceIds` 为 `null` 或空集合时查询该类型全部结果。`checkAndSet()` 先过滤
  `instanceIds` 中的 `null` 元素并回写新集合，显式 ID 查询继续按 500 个分片，清理后为空时查询指定类型全部结果。
- `instanceType` 使用 JSpecify `@NonNull` 和 Bean Validation `@NotNull`；枚举不能使用只支持字符串、集合、
  Map 和数组的标准 `@NotEmpty`。`instanceIds` 及请求集合元素使用 JSpecify `@Nullable` 表达允许为空的输入契约。
- 结果表 miss 时返回空列表，不回退实时继承查询。
- `fillInstanceName` 默认值为 `false`，仅用于兼容调用方字段；轻量 VO 不包含实例名称，
  即使传入 `true` 也不触发 RWD 实例名称查询。
- `autoFillEmptyInstanceIds` 不在结果读取路径访问 RWD；空 ID 集合直接使用结果表“该类型全部结果”语义。
- `filterShieldedSpaces` 默认值为 `true`，未传时过滤屏蔽对象；显式传入 `false` 时返回屏蔽对象，
  并通过响应 `shielded` 返回结果表或快照表中的真实屏蔽状态。
- `queryInstanceTag` 默认值为 `false`；显式传入 `true` 时按前端对象范围读取实例配置状态并批量查询实例自定义标签，
  在响应中返回 `instanceTag`。黑名单对象不写入结果表，若在请求范围内则从实例配置中补充响应行，返回其
  `instanceId`、`instanceTag.inBlacklisted=true` 和 `finalConfig=null`；默认屏蔽过滤同样适用于补充行，且不允许按结果逐条调用外部接口。
- `instanceTag.customConfig` 只表示稳定结果行记录的原始 `demand_config` 自身配置状态；PROJECT 查询期从独立项目工况表补入的 `valueSelect/workCondition` 不会改变该标识。
- `DemandConfigInfoCodeAnalyzeStrategy` 作为本期首个调用方，通过
  `DemandQueryService.listConfigResultsByInstances()` 查询空间需求配置结果，不再调用
  `DemandConfigGateway.listConfigsByInstances()` 执行实时继承计算。
- InfoCode 调用方继续按当前日期解析空调模板和季节配置；结果表 miss 时按无配置处理，不回退慢链路。
- 结果表和快照表只保存稳定核心配置，不固化营业日历空调季过滤、项目级 `valueSelect/workCondition`、
  当前DTX绑点、RWD系统/泵组或本地化名称。
- `DemandConfigAirconditoningAware` 原类和原实现保持不变，普通实时查询与 `listOriginConfigs()` 继续通过
  原生命周期执行空调季和项目工况逻辑；不得删除、代理或改接到新补全器。
- `DemandConfigAirconditioningQueryEnricher` 仅承接结果表查询后的空调补全，不提供聚合根补全入口。
  PROJECT 结果按查询日期补充项目工况，其他实例类型不访问项目自定义工况服务；所有空调结果按查询日期过滤温度空调季。

### 结果刷新能力

- 新增独立结果刷新仓储 `DemandConfigResultRefreshRepository`，统一承接结果重算、upsert、快照归档和屏蔽状态更新。
- 通用刷新与持久化契约保留可空 `sceneId`：本期空调入口只传 `null`，未来照明入口传真实场景 ID，二者使用不同业务键和写锁范围。
- 配置增删改后，对当前实例及其所有下级实例执行局部重算并 upsert 结果表。
- `addOrUpdateConfig(templateId, instance)`、`clearConfig(templateId, instance)`、`cancelReference(templateId, instance)` 均需触发局部重算。
- 空调模板新增后，对该模板下所有实例批量重算；模板删除后删除对应结果。
- 模板编辑、应用关系变化不改变最终配置结果，不触发重算。
- DTX 发布消息导致实例对象、空间层级或对象属性变化时，对变更实例及其下级实例批量重算。
- 配置、模板和黑名单等事务内事件在提交前同步刷新；DTX 发布通过异步事件按项目锁执行，失败由事件边界记录并允许幂等重跑。

### 数据生成与初始化

- `demand_config_result` 数据由云端生成，并通过云边同步下发到边缘端。
- 本模块不承担边缘端全量初始化职责。
- 云端 XXL-JOB 遍历集团和项目，仅刷新空调需求模板类型，调用 Gateway 稳定核心配置入口获取继承后当前配置，再写入结果主表并从任务执行时开始建立开放快照。
- “全量初始化”只表示覆盖所有有效集团/项目/当前模板范围，不表示按历史日期重放；它不能回填执行日之前的历史，也不能修复旧关闭快照。
- 本次从“存动态最终值”调整为“存稳定核心值”后必须重新执行一次全量初始化；否则旧JSON中已被裁掉的空调季无法在查询时恢复。

### 黑名单与屏蔽空间

- 黑名单实例不写入结果表，新接口查不到即等价于被过滤。
- 屏蔽空间写入结果表，并通过 `is_shielded = 1` 标识。
- 屏蔽空间新增或取消时仅更新 `is_shielded`，不重算配置内容。
- `filterShieldedSpaces` 由新接口查询条件处理，默认过滤屏蔽对象；放开过滤时响应透传 `shielded`。

## 已收敛决策

- 本任务交付结果表、刷新能力和新增读取接口，并将 `DemandConfigInfoCodeAnalyzeStrategy` 作为首个调用方
  切换到结果查询；其他现有调用方不在本期强制切换，避免扩大回归面。
- 结果刷新失败不得静默吞掉。默认要求同事务内刷新失败时回滚当前写入，避免主配置已变更但结果表陈旧。
- 若实现阶段发现部分批量事件无法承受同事务同步刷新成本，需要在设计中明确补偿机制、错误日志、告警和重试入口后再调整策略。

## 非目标

- 不改变现有实时查询和原始配置查询的生命周期实现；不得删除或改写 `DemandConfigAirconditoningAware`，
  不得把新增结果补全器注入原 Gateway 替代旧生命周期。
- 不在结果表 miss 时自动穿透或回写。
- 不把临时需求文档目录登记为任务上下文或实现依赖。
- 不使用数据库触发器实现快照归档。
- 不在边缘端执行全量初始化。
- 不强制切换现有调用方到新接口。
- 不在本期接入照明、安防、通风等非空调需求类型的结果预计算。

## 技术约束

- 新增表需兼容人大金仓和云边同步场景。
- 主表不包含 `config_date`，与现有 `demand_config` 最新数据模式保持一致。
- 快照归档由 Java 代码控制：插入、更新、删除均需维护快照有效期。
- 主表 upsert 与快照归档需要在一致事务边界内完成。
- 结果业务键、查询条件和写锁范围必须包含可空场景维度，不得将 `rel_scene_id = null` 固化为全需求类型约束。
- 新接口查询参数使用 `DemandConfigResultBatchQueryParam`，通过 `BaseDemandTemplateParam` 支持
  `templateId / templateTag / date / templateType` 解析，并保持原批量查询中适用于结果读取的过滤语义；响应保持轻量
  `List<DemandConfigResultVO<T>>`，包含 `instanceId`、`shielded`、可选 `instanceTag` 和 `finalConfig`。
- 原接口调用方可以按需切换到新接口，未切换调用方不受影响。

## 验收标准

- 数据库迁移包含 `demand_config_result` 和 `demand_config_result_snapshot` 建表脚本及必要索引。
- Entity、Mapper、Repository 支持结果表和快照表的新增、查询、更新、归档。
- 结果刷新仓储统一提供单实例刷新、批量刷新、upsert、快照归档、屏蔽状态更新能力。
- 新 Controller 端点可按原查询参数从结果表返回包含 `instanceId`、`shielded`、可选 `instanceTag` 和 `finalConfig` 的轻量 VO。
- 新 Controller 端点支持按模板 ID、模板标签或业务日期解析模板，解析优先级为
  `templateId > templateTag > date`；模板不存在时记录项目级警告日志并返回空列表，不继续查询结果表，
  参数校验失败和系统异常仍按原异常语义返回。
- 新 Controller 端点要求必传 `instanceType`，支持指定类型空 ID、显式 `instanceType + instanceIds`、屏蔽空间和
  大实例 ID 集合 500-ID 分批查询语义；`instanceIds` 中的 `null` 元素会通过新集合过滤，清理后
  为空时查询指定类型全部结果；缺少实例类型时拒绝请求。
- 新 Controller 端点支持历史日期查询，仅在结果快照表已有该日期的有效期窗口时返回对应版本；初始化/首次写入日之前按 miss 契约返回空。
- 新 Controller 端点默认过滤屏蔽对象；显式关闭过滤时返回屏蔽对象及其真实 `shielded` 标识。
- 新 Controller 端点默认不查询实例标签；显式开启 `queryInstanceTag` 时按前端对象范围读取实例配置，补充结果表中不存在的黑名单对象，
  应用屏蔽过滤后批量查询并返回实例标签。
- 新 Controller 端点返回轻量 VO，不触发 RWD 名称查询。
- 结果表 miss 时新接口返回空列表，不调用原实时继承查询兜底。
- `DemandConfigInfoCodeAnalyzeStrategy` 只读取结果表或历史快照表，按 `instanceId + finalConfig` 映射
  温度、二氧化碳、湿度和压差信息点，不再调用实时需求配置网关。
- InfoCode 结果查询显式关闭实例名称填充；结果缺失时不回退实时继承计算。
- 结果刷新只调用 `DemandConfigGateway#listStableConfigsByInstances()`，写入JSON中保留完整稳定季节配置，
  不包含查询时生成的项目工况、当前DTX/RWD对象或本地化名称。
- 结果查询反序列化后调用结果专用空调补全器；历史日期按当日日历标签过滤季节，PROJECT 查询补充项目工况，
  SPACE/FLOOR/BUILDING 查询不调用项目工况服务，非空调模板不调用空调补全器依赖。
- PROJECT 查询期工况补全不修改 `instanceTag.customConfig`；该字段继续仅反映原始 `demand_config` 是否存在自身配置。
- 稳定 Gateway 入口强制开启继承并关闭实例名称，只跳过原 `DemandConfigAirconditoningAware` 的
  `afterAggregateInit` 查询期钩子，继续执行其他生命周期；普通查询和 `listOriginConfigs()` 仍按原顺序执行完整生命周期。
- 聚合转结果时在转换后的JSON节点上移除空调 `valueSelect/workCondition`，历史脏原始配置不会污染结果主表和快照表。
- 原 `listConfigsByInstances()` 行为保持不变：未传配置日期时项目工况服务仍接收 `null`，且任一聚合缺失最终配置/环境品质/温度品质结构时仍结束整批季节重写。
- 空调配置增删改、空调模板新增/删除、DTX 数据变更均能同步维护受影响结果。
- 非空调需求类型的配置、模板、场景、黑名单和屏蔽事件不会触发结果预计算。
- 代码中只有统一支持集合可以直接声明 `DemandTypeEnum.AIRCONDITIONING`；其他结果预计算入口不得直接比较或遍历空调需求类型。
- 数据模型、业务键、读取接口和通用刷新方法仍支持非空真实 `sceneId`，但本期不会主动生成照明结果。
- 刷新失败时不会留下“原配置已提交但结果表未更新且无告警”的状态。
- 屏蔽空间变更仅更新 `is_shielded`，不触发配置重算。
- 黑名单实例不会写入结果表。
- 结果表更新前后正确维护快照表历史版本和 `valid_end_time`。
- 任务实现不得依赖或引用临时需求文档目录。

## 实施回看结论

- 模板新增时生成结果，模板删除时删除结果；模板编辑和应用关系变化不影响最终配置，不触发重算。
- 本期不订阅场景事件。后续接入照明时，场景新增生成结果、场景删除清理结果、场景编辑不重算最终配置。
- 模板类型全量刷新按模板受控并发，当前并发窗口为 3；项目和模板类型层不再叠加并发。
- 模板类型刷新只预取一次需求空间和屏蔽空间，并在每个模板任务中复用，避免重复外部查询和数据库查询。
- 结果业务键写入在分布式写锁和事务内完成；锁使用总计 120 秒有限等待并由 watchdog 续期，超时直接失败且不继续写入。
- 当前表不增加业务唯一索引，改由批内业务键去重、模板/场景写锁和事务内差集同步保证唯一性，兼容云边同步约束。
- XXL-JOB 定义随部署自动创建，但没有调度表达式且不会自动执行；首次上线由运维在控制台手工触发。
