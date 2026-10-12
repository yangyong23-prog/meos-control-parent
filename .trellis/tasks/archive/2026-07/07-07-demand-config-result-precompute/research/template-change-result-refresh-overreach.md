# Research: template-change-result-refresh-overreach

- Query: DemandConfigResultRefreshEventListener#onDemandTemplateChangeEvent 中模板变更触发需求配置结果刷新是否过度；分类判断 INSERT / UPDATE / REMOVE / APPLIED 的刷新必要性。
- Scope: internal
- Date: 2026-07-08

## Findings

### Files found

- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/enumeration/DemandTemplateChangeTypeEnum.java` — 定义模板变更事件类型：INSERT / UPDATE / REMOVE / APPLIED。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/evnet/DemandTemplateChangeEvent.java` — 模板变更事件消息，携带 oldTemplate/newTemplate。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java` — 结果预计算刷新监听器；当前 REMOVE 删除结果，其余类型全部 refreshByTemplate。
- `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java` — 模板增删改、启停、应用日历的事件发布点。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java` — refreshByTemplate / refreshByTemplateType / refreshInstances 的实际刷新范围和删除语义。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java` — 模板删除后级联删除原始配置，且不再发布配置删除刷新事件。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/scene/DemandSceneGatewayImpl.java` — 模板删除后级联删除场景。
- `common/src/main/java/com/persagy/apccontrol/basic/model/param/demand/BaseDemandTemplateParam.java` — 当前模板解析规则。
- `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandTemplateRepositoryImpl.java` — templateId + null/future date 直接按模板 ID 解析当前模板。
- `.trellis/spec/backend/demand-config-result-precompute.md` — 结果预计算刷新契约。

### Code patterns

- `DemandTemplateChangeTypeEnum` 只有四类：INSERT 新增、UPDATE 编辑、REMOVE 删除、APPLIED 应用。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/enumeration/DemandTemplateChangeTypeEnum.java:20-38`。
- `DemandTemplateChangeEvent.Message` 事件载荷为 `dataType`、`oldTemplate`、`newTemplate`；注释说明新增、删除、切换应用模板可有 oldTemplate，新增、编辑、应用模板可有 newTemplate。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/evnet/DemandTemplateChangeEvent.java:38-65`。
- 当前结果刷新监听器：取 newTemplate 优先，否则 oldTemplate；REMOVE 调 `deleteTemplateResults(template.getId())`；其他类型直接 `refreshByTemplate(template.getId())`。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:106-119`。
- 模板新增发布 INSERT：`saveCustomTemplate` 新增自定义模板后发布 `INSERT`，old=null，new=demandTemplates。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:445-474`。
- 模板编辑发布 UPDATE：`updateTemplates` 可改模板名称、启用状态；启停过程中还会调用清空/应用日历逻辑，最后统一发布 `UPDATE`。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:486-540`。
- 模板删除发布 REMOVE：删除前清未来日历，再删除模板，最后 old=toBeDelTemplates，new=null。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:552-572`。
- 清未来/指定日期日历发布 APPLIED：`clearTemplateCalendarOnAllowApplyDate` 清掉模板日历后，对受影响模板发布 `APPLIED`，old=entry.getValue，new=null。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:406-433`。
- 手动/系统应用模板发布 APPLIED：`applyTemplate` 编辑已有模板日历时 old/new 都有；新增日历时 old=null、new=模板。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:631-700`。
- 事件发布器只按 old/new 模板 ID 交集发布单模板事件；old null 表示 INSERT/新增应用，new null 表示 REMOVE/清应用。见 `saas/meos-control-common/src/main/java/com/persagy/apccontrol/common/service/demand/impl/DemandCalendarServiceImpl.java:583-617`。
- 模板删除后，原始配置级联删除处设置 `publishEvent(false)`，避免再次触发配置删除刷新；因此结果表删除必须由模板事件直接处理。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java:873-889`。
- 模板删除后场景也由模板事件级联删除。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/scene/DemandSceneGatewayImpl.java:223-241`。
- `refreshByTemplate` 当前以 `DemandInstanceTypeEnum.toAllInstanceTypeAndInstanceIds()` + `autoFillEmptyInstanceIds(true)` 触发模板全实例范围实时计算。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:151-159`。
- spec 推荐模板类型刷新要先预取需求空间实例范围，并关闭自动补全，避免不必要全量计算。见 `.trellis/spec/backend/demand-config-result-precompute.md:63-85`。
- `refreshByTemplateType` 已按模板类型预取需求空间；空范围会清理该模板类型旧结果，非空范围对同类型模板受控并行刷新。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java`。
- `refreshByQueryParam` 已从单纯 upsert 调整为模板/场景边界内的差集同步：先查询旧结果业务键，再删除本次有效实时结果中不再存在的旧结果。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java`。
- `refreshInstances` 对明确实例范围才会删除实时未命中或 finalConfig 为空的旧结果。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:83-126`、`444-477`。
- 结果行业务键保存 `relTemplateId`、`relSceneId`，来自聚合根 identifier，不按日历另存模板类型级结果。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigResultRefreshRepositoryImpl.java:383-401`。
- 当前 `BaseDemandTemplateParam#queryAndGetTemplate` 在 templateId 存在时优先按 `getByTemplateIdAndDate(false, templateId, date)` 解析；结果刷新传入 templateId 且未传 date。见 `common/src/main/java/com/persagy/apccontrol/basic/model/param/demand/BaseDemandTemplateParam.java:48-70`。
- `DemandTemplateRepositoryImpl#getByTemplateIdAndDate` 在 date 为 null 或非历史日期时直接 `getByTemplateId`，不会通过模板日历重映射。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandTemplateRepositoryImpl.java:47-62`。
- 结果表读取按请求 `templateId` 查询 `relTemplateId`，不按当前日历日期自动切换模板。见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java:396-413`。

### Type-by-type assessment

- `REMOVE`: 删除模板后实时链路查不到模板，且原始配置/场景级联删除不会再发布配置删除刷新；按旧模板 ID 删除结果表是正确且必要的。
- `INSERT`: 新增模板本身通常没有需求配置、场景或应用日历结果；直接 `refreshByTemplate` 多数是空跑。若新增模板带初始配置或后续复制配置，最终结果应由配置/场景事件或显式初始化触发；单纯 INSERT 不应无条件刷新。
- `UPDATE`: 当前发布点主要代表模板名称、启用状态变更。模板名称不影响最终配置 JSON、继承链、业务键；启停状态通过 clear/apply 日历产生 APPLIED 事件，但结果刷新以 relTemplateId 为业务键并不按日历重映射，因此 UPDATE 本身不应无条件刷新。若未来模板可编辑影响配置计算的结构字段，再单独纳入刷新条件。
- `APPLIED`: 表示模板日历应用关系变化，包括清除日历、编辑已有日期、为日期新增应用。当前结果表以模板 ID/场景/实例为业务键，刷新链路不传 date，`templateId` 查询不会通过日历重映射；因此 APPLIED 通常不改变某个模板 ID 下已保存的最终配置，直接 `refreshByTemplate` 属于过度刷新。若将来结果表按日期或“当前生效模板”维度存储，APPLIED 才需要按受影响日期和模板类型重算。

## Related specs

- `.trellis/spec/backend/demand-config-result-precompute.md`: 结果表必须保存与实时计算一致的最终配置；删除模板/场景后不能再走实时刷新，必须按旧 ID 清理；模板类型批量刷新应预取需求范围，避免自动补全导致全量计算。
- `.trellis/tasks/07-07-demand-config-result-precompute/prd.md`: 模板保存、启用、停用后批量重算是任务原始要求；实现阶段需要结合事件粒度避免无效重算。
- `.trellis/tasks/07-07-demand-config-result-precompute/design.md`: 批量重算按同步语义交付，但性能不可接受时需补充边界；屏蔽空间只更新标识不重算配置内容。
- `.trellis/tasks/07-07-demand-config-result-precompute/implement.md`: 要检查“模板变更刷新模板下所有实例”，但不要求所有枚举类型无条件刷新。

## Caveats / Not Found

- 未发现 `DemandTemplateChangeTypeEnum` 除 INSERT / UPDATE / REMOVE / APPLIED 外的其他类型。
- 未引用临时需求文档目录。
- 未直接改代码；本文件仅记录研究结论。
- 如果业务方期望结果表支持“按日期查询当前应用模板”的物化结果，现有结果表键和读取链路尚未体现日期维度；这会改变 APPLIED 的结论，需要另行设计。

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终模板事件策略，优先于前面的阶段性判断。

| 事件类型 | 最终处理 | 原因 |
| --- | --- | --- |
| `INSERT` | 刷新新增模板 | 新模板需要生成对应实例的预计算结果；实时结果为空时范围同步保持幂等。 |
| `REMOVE` | 按旧模板 ID 删除结果并关闭快照 | 模板删除后实时链路无法再解析模板，必须直接清理。 |
| `UPDATE` | 不刷新 | 模板名称、启停等元数据不改变指定模板 ID 下的最终配置和业务键。 |
| `APPLIED` | 不刷新 | 应用日历只影响调用方选择哪个模板，不改变各模板自身已经物化的最终配置。 |

本期只启用无场景空调需求，不订阅场景事件。后续启用照明时遵循同类策略：场景新增生成结果、场景删除清理结果、场景编辑不重算最终配置。
