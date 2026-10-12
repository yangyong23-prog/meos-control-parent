# Research: result gateway context review

- Query: 专项审查 C：实时网关计算链路、聚合上下文、RWD/外部调用和性能边界，重点检查预计算调用实时网关时的 RWD/DB 循环、参数默认值、继承链路、场景边界和物理世界变更边界。
- Scope: internal
- Date: 2026-07-09

## Findings

### Files Found

- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGateway.java` - 实时需求配置网关接口，暴露 `listConfigsByInstances` / `listOriginConfigs` 等入口。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java` - 实时聚合计算主流程，固定执行模板、场景、实例、配置、继承和名称填充。
- `common/src/main/java/com/persagy/apccontrol/basic/model/context/demand/DemandConfigAggregateInitByInstanceContext.java` - 实时聚合上下文，负责自动补实例、过滤黑名单/屏蔽、继承父级、空间功能类型、名称填充。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/param/DemandConfigByInstanceBatchQueryParam.java` - 实时查询参数，含 `autoFillEmptyInstanceIds`、`queryInheritConfig`、`filterShieldedSpaces` 默认值。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigRepositoryImpl.java` - 原始需求配置仓储，负责配置增删改事件发布和 DB 查询委托。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/param/DemandConfigByInstanceRepositoryBatchQueryParam.java` - 原始配置仓储批量查询参数。
- `common/src/main/java/com/persagy/apccontrol/basic/service/InstanceService.java` - 实例服务接口，含父级映射、下级展开、空间/楼层/建筑范围查询。
- `common/src/main/java/com/persagy/apccontrol/basic/service/impl/InstanceServiceImpl.java` - 实例服务实现，父级/下级查询主要调用 RWD。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java` - 预计算刷新仓储，实际构造实时网关参数并 upsert/delete 结果。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java` - 预计算刷新事件监听器，连接 DTX、配置、模板、场景、黑名单和屏蔽事件。
- `common/src/main/java/com/persagy/apccontrol/basic/service/demand/impl/DemandQueryServiceImpl.java` - 新预计算读接口服务，直接查询结果表。

### Must Fix

1. `DemandConfigResultRefreshRepositoryImpl.java:135-138`, `DemandConfigResultRefreshRepositoryImpl.java:697-712`, `DemandConfigResultRefreshRepositoryImpl.java:826-839`
   - Risk: `toUpsertParam()` 在 stream 中逐条调用 `isShielded()`，每个 SPACE 都执行一次 `demandInstanceRepository.listConfig(...)`。149 项目、1 万对象、14 模板、20 场景下，单场景 1 万空间就可能变成 1 万次 DB 查询；模板/场景维度叠加后不可控。
   - Trigger: `refreshInstances`、`refreshByTemplateType`、`refreshByScene` 或初始化任务批量实时计算后 upsert 大量 SPACE 结果。
   - Recommendation: 按模板类型和本批 SPACE IDs 一次性批量查询屏蔽空间，构造 `Set<String> shieldedSpaceIds` 传入转换；非 SPACE 仍直接 false。不要在结果行转换函数内访问 DB。

2. `DemandConfigGatewayImpl.java:306-308`, `DemandConfigAggregateInitByInstanceContext.java:529-570`, `DemandConfigResultRefreshRepositoryImpl.java:109-117`, `DemandConfigResultRefreshRepositoryImpl.java:456-465`
   - Risk: 实时网关无 `fillInstanceName` 参数，`listConfigsByInstances()` 总是调用 `context.fillInstanceName()`；预计算刷新链路虽然不需要实例名称，仍会对未预填名称的对象批量调用 `dtpRwdClientService.mapObjectIdAndLocalName(...)`。这是预计算生成路径上的不必要 RWD 外部调用。
   - Trigger: `refreshInstances` 显式实例刷新且 `autoFillEmptyInstanceIds=false`，上下文未经过 `preFillInstanceDetail()`；删除/配置变更/黑名单局部刷新都会命中。模板类型全量刷新在自动补范围时可预填空间名称，但楼层/建筑/项目名称仍可能补查。
   - Recommendation: 给 `DemandConfigByInstanceBatchQueryParam` 增加默认 false 的 `fillInstanceName` 或内部刷新专用开关；旧 Controller/旧实时接口如需兼容名称，可显式开启。预计算刷新应关闭名称填充。

3. `DemandConfigResultRefreshRepositoryImpl.java:456-480`, `DemandConfigResultRefreshRepositoryImpl.java:624-640`, `DemandConfigResultRefreshRepositoryImpl.java:647-656`, `DemandConfigResultRefreshEventListener.java:57-66`
   - Status: 已解决。模板类型/模板/场景批量刷新已按模板和场景边界对比旧结果业务键与本次有效实时结果业务键，删除旧表中不再有效的结果；黑名单进入导致实时链路不返回时也能通过差集删除清掉旧结果。
   - Risk: `refreshByQueryParam()` 只 upsert 当前实时返回结果；当 DTX 后物理世界范围收缩、对象被删除、空间进入黑名单、需求空间不再匹配，但该模板类型仍有其他需求空间时，旧结果不会被删除。只有“整个模板类型无需求空间”才 `deleteResultsByTemplateType()`。
   - Trigger: DTX 发布触发 `refreshByTemplateType()`；某些 SPACE/FLOOR/BUILDING 从需求范围消失或 RWD 对象不存在，但同类型还有其他空间，当前刷新只覆盖新范围。
   - Recommendation: 模板类型/模板场景全量刷新必须按当前应存在的业务键集合和结果表旧业务键做差，删除旧表中不再属于当前范围的结果；或在 DTX 事件能拿到变更实例时走 `refreshByDtxChangedInstances()`/明确 delete。黑名单进入也应验证是否能删除所有旧结果，而不是只依赖当前实时返回。

4. `DemandConfigResultRefreshRepositoryImpl.java:122-126`, `DemandConfigResultRefreshRepositoryImpl.java:141-148`, `DemandConfigResultRefreshRepositoryImpl.java:727-737`, `DemandConfigResultRefreshRepositoryImpl.java:758-772`
   - Risk: 删除定位参数 `toDeleteParam()` 每个缺失实例都会通过 `BaseDemandTemplateParam#queryAndGetTemplate()` 查一次模板。实时链路返回空或大量实例 finalConfig 为空时，会出现 N 次模板查询。
   - Trigger: 批量刷新一批对象但实时链路算不到结果，例如父级不存在、空间进入黑名单、对象已删除、无有效继承配置。
   - Recommendation: 在 `refreshInstances()` 开始处解析一次模板和模板类型，后续 delete param 复用；`toMissingDeleteParams()` 接收模板类型或模板对象，避免循环查模板。

### Should Fix

1. `DemandConfigResultRefreshRepositoryImpl.java:184-205`, `DemandConfigResultRefreshRepositoryImpl.java:1040-1065`, `InstanceServiceImpl.java:589-610`, `InstanceServiceImpl.java:455-497`, `InstanceServiceImpl.java:500-585`
   - Risk: `refreshByInstanceAndChildren()` 对输入实例逐个调用 `listChildrenInstanceIdsByInstance()`；每个项目级实例内部会分别查建筑、楼层、空间 3 次 RWD；楼栋/楼层也有多次 RWD。随后 `resolveChildInstanceType()` 对每个下级 ID 又可能重复查询项目全量建筑/楼层或楼栋楼层集合。
   - Trigger: 项目级/楼栋级配置变更，或一次事件包含多个上级实例；在 1 万对象规模下，子级展开和类型判断会成为 RWD 调用热点。
   - Recommendation: 批量化子级展开和类型归类：一次按 source type 查询 building/floor/space 集合，构造 `id -> DemandInstanceTypeEnum` 映射；项目级不要在每个 child 上重复查全量 building/floor。

2. `DemandConfigAggregateInitByInstanceContext.java:630-704`, especially `DemandConfigAggregateInitByInstanceContext.java:673-699`
   - Risk: 自动补实例时，若某些实例类型已传非空 ID、其他实例类型为空，当前用 `SetUtils.disjunction(input, allScope)` 做对称差集，不是交集或并集。它会排除已在需求范围内的入参 ID，并把未指定的其他范围 ID 加入，语义容易误用。
   - Trigger: 调用方开启 `autoFillEmptyInstanceIds=true` 且传入混合参数，例如指定部分 SPACE 同时 FLOOR/BUILDING 为空。预计算 `refreshByScene()` / `refreshByTemplate()` 当前传的是全类型空集合，暂不触发该混合风险；其他实时调用可能触发。
   - Recommendation: 明确 auto fill 语义：只填空集合；非空集合应保留原值或与需求范围取交集。补充单测覆盖混合实例类型。

3. `DemandConfigAggregateInitByInstanceContext.java:1030-1057`, `DemandConfigAggregateInitByInstanceContext.java:195-206`
   - Risk: 继承处理会按 SPACE 批量查 `roomFuncType`，LazyMap 在处理非 SPACE 父级空间功能类型时又可能调用 `demandStrategy.listDemandSpace()`。虽然不是逐对象必然调用，但并发继承中 LazyMap 写入 `ConcurrentHashMap` 后每个未预填上级实例仍可能触发 RWD。
   - Trigger: `autoFillEmptyInstanceIds=false` 的局部刷新、包含 FLOOR/BUILDING/PROJECT 且需要对象级继承的配置。
   - Recommendation: 局部刷新时也尽量批量预取当前实例及其父级/子级的 roomFuncType 范围；避免在 LazyMap loader 中调用 RWD，至少用批量缓存兜住同批次对象。

4. `DemandConfigAggregateInitByInstanceContext.java:227-241`, `DemandConfigGatewayImpl.java:273-276`
   - Risk: `fillTemplate()` / `fillScene()` 没有处理模板或场景不存在，后续直接使用 `template.getDemandTemplateType()` 或 scene 为 null 继续计算。结果刷新在模板被删除/场景被删除时有专门 delete 分支，但并发事件或手工调用传入已失效 ID 时可能 NPE 或产生空场景语义混淆。
   - Trigger: 物理/业务事件乱序、模板/场景刚删除后仍触发刷新，或手工调用旧 ID。
   - Recommendation: 对模板不存在抛明确 invalid-input；对传入 sceneId 但查不到 scene 的情况应返回空并删除对应结果，或抛明确业务异常，由刷新仓储决定删除旧结果。

5. `DemandConfigAggregateInitByInstanceContext.java:803-853`
   - Risk: `removeInvalidInstances()` 只按 `DemandInstanceConfig` 过滤黑名单/屏蔽 SPACE，不验证显式传入的 RWD 对象是否存在。不存在对象最终大概率因无父级/无 finalConfig 被删除，但仍会进入配置查询、聚合构造、名称填充等步骤。
   - Trigger: 局部刷新传入已从 RWD 删除的 SPACE/FLOOR/BUILDING。
   - Recommendation: 对显式实例范围增加可选批量存在性校验，至少在预计算刷新链路中用 RWD 批量查询确认存在；不存在的业务键直接进入 deleteParams，跳过聚合和名称填充。

### Looks OK

1. `DemandConfigByInstanceBatchQueryParam.java:49-63`, `DemandConfigByInstanceBatchQueryParam.java:64-69`
   - Risk: 默认值误用。
   - Trigger: 新建参数不显式设置开关。
   - Observation: 默认 `autoFillEmptyInstanceIds=false`、`queryInheritConfig=true`、`filterShieldedSpaces=false`，与预计算“显式范围刷新、保留屏蔽结果、计算继承结果”的主需求一致。`checkAndSet()` 仅在配置组和实例范围都空时放入全实例类型空集合，是否补全仍由 `autoFillEmptyInstanceIds` 决定。
   - Recommendation: 保持默认值；新增名称开关时也应默认 false，并由旧实时 API 兼容层显式开启。

2. `DemandConfigResultRefreshRepositoryImpl.java:109-117`
   - Risk: 局部刷新误全量。
   - Trigger: 配置增删改或黑名单局部刷新。
   - Observation: `refreshInstances()` 明确 `setAutoFillEmptyInstanceIds(false)`，不会因实例集合为空值被网关补成全量；`queryInheritConfig=true`、`filterShieldedSpaces=false` 符合结果表保存最终配置和屏蔽标识的要求。
   - Recommendation: 保持该参数组合；加测试防止被改回自动补全。

3. `DemandConfigResultRefreshRepositoryImpl.java:624-667`
   - Risk: 模板类型初始化查全项目。
   - Trigger: `refreshByTemplateType()` 全量初始化或 DTX 全量刷新。
   - Observation: 当前先通过 `demandStrategy.listDemandSpace()` 查询需求范围，且 `filterInBlacklistedSpace=true`、`filterInShieldedSpace=false`；空范围直接返回并删除该模板类型旧结果，避免回退成全实例类型空 ID 查询。这符合 spec 中“模板类型无需求空间早退，不查询所有实例”的方向。
   - Recommendation: 保持预取范围策略，但补上“旧结果差集删除”以覆盖部分范围收缩。

4. `DemandQueryServiceImpl.java:1157-1185`, `DemandController.java:218-223`
   - Risk: 新读取接口误触发实时链路或 RWD 名称查询。
   - Trigger: `/demand/config/listConfigResultsByInstances` 查询。
   - Observation: 新接口只 `param.checkAndSet()` 后转换为结果表查询参数，窄列选择 `demandTemplateType`、`demandInstanceId`、`finalConfigJson`，没有调用 `DemandConfigGatewayImpl.listConfigsByInstances()` 或名称填充。
   - Recommendation: 保持该读路径；补充 mock/单测断言不会调用实时网关和 RWD。

5. `DemandConfigAggregateInitByInstanceContext.java:1065-1074`, `InstanceServiceImpl.java:871-1043`
   - Risk: 父级关系方向错误或名称不必要查询。
   - Trigger: 继承计算时填充父级。
   - Observation: 继承父级映射使用 `mapInstanceIdAndAllParentsByDirectQuery(..., false)`，直接查询 FLOOR/SPACE 的 `buildingId` / `floorId` 字段，不查名称；PROJECT/BUILDING/FLOOR/SPACE 父级边界基本正确：BUILDING -> PROJECT，FLOOR -> BUILDING + PROJECT，SPACE -> FLOOR + BUILDING + PROJECT。
   - Recommendation: 保持 `queryName=false`；对父级不存在的情况增加结果删除/告警测试。

6. `DemandConfigResultRefreshEventListener.java:167-182`, `DemandConfigResultRefreshRepositoryImpl.java:398-430`
   - Risk: 屏蔽空间误重算配置。
   - Trigger: 屏蔽空间新增或取消。
   - Observation: 屏蔽事件只调用 `updateShielded()`，不走实时继承计算；黑名单事件走刷新，符合“屏蔽只更新标识、黑名单不写结果”的设计方向。
   - Recommendation: 保持事件分流；确认黑名单进入时旧结果删除覆盖所有模板/场景。

### Code Patterns

- 实时网关固定步骤：`DemandConfigGatewayImpl.java:263-312` 依次执行参数检查、模板、场景、实例、配置、分组、聚合、继承、名称填充。
- 自动补实例与黑名单/屏蔽过滤：`DemandConfigAggregateInitByInstanceContext.java:630-729` 通过 `DemandStrategy.listDemandSpace()` 拉需求空间并按 `filterInBlacklistedSpace` / `filterInShieldedSpace` 控制范围。
- 继承父级批量查询：`DemandConfigAggregateInitByInstanceContext.java:1065-1106` 先查父级实例，再一次性按父级实例查原始配置。
- 预计算局部刷新参数：`DemandConfigResultRefreshRepositoryImpl.java:109-117` 使用显式实例范围、开启继承、不过滤屏蔽。
- 结果读路径窄列查询：`DemandQueryServiceImpl.java:1175-1184` 只读结果表需要的列。

### External References

- No external references used. This review is based on local Trellis specs and source code only.

### Related Specs

- `.trellis/spec/backend/demand-config-result-precompute.md` - 预计算结果必须与实时计算一致，保留继承、屏蔽、需求范围；读接口不得重建聚合或查 RWD 名称。
- `.trellis/spec/backend/quality-guidelines.md` - 禁止循环查询，批量查询优先；缺失业务数据需按业务契约处理。
- `.trellis/spec/backend/rwd-relation-direction.md` - RWD 关系查询方向规范；本次重点代码主要使用直接字段或封装方法，未发现新增关系方向问题。
- `.trellis/spec/guides/cross-layer-thinking-guide.md` - 结果生成、结果表、读接口跨层契约需要一致。

## Caveats / Not Found

- 未运行数据库测试、浏览器测试或接口测试，按用户要求仅做静态审查。
- 未验证 RWD 客户端内部是否有缓存、分片或限流；这里按代码层可见调用次数评估风险。
- 未完整审查所有 `DemandStrategy.listDemandSpace()` 实现，需求空间范围是否包含特殊设备/系统边界需另开专题。
- 未发现 `DemandConfigByInstanceBatchQueryParam` 中存在 `fillInstanceName` 字段；当前实时网关名称填充是无条件行为。

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终处置，优先于前面的历史审查描述。

| 审查项 | 最终处置 |
| --- | --- |
| 屏蔽空间 N+1 查询 | 已修复：按模板类型和空间 ID 分片批量查询，模板类型刷新只预取一次。 |
| 预计算刷新额外查询实例名称 | 已修复：`DemandConfigByInstanceBatchQueryParam` 增加 `fillInstanceName`，结果刷新显式设为 `false`。 |
| 物理世界范围收缩残留旧结果 | 已修复：模板/场景范围使用锁内差集同步。 |
| 缺失实例逐条解析模板 | 已修复：刷新边界复用已解析模板/模板类型，不在实例转换循环中重复查询模板。 |
| 上级实例子级展开和类型判断 | 接受现状并保留观察：当前已按明确刷新范围执行，未获得需要修改既有 `InstanceService` 通用逻辑的性能证据。 |
| `autoFillEmptyInstanceIds` 混合参数语义 | 非本任务范围：结果刷新不使用混合参数；既有通用实时查询行为保持不变。 |
| 局部刷新 roomFuncType 预取 | 接受现状并保留观察：只有性能追踪确认该边界成为热点时再优化。 |
| 模板/场景不存在处理 | 已由结果事件边界覆盖删除；非结果实时网关的既有行为不在本任务修改。 |
| 显式实例 RWD 存在性校验 | 无需新增独立查询：实时计算 miss 会进入结果删除，避免为每次局部刷新再增加一轮外部调用。 |
