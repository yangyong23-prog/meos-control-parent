# Research: addOrUpdateConfig deleted-instance refresh safety

- Query: 独立审查 `DemandConfigGatewayImpl.addOrUpdateConfig` 在已有 `configGroupId` 时，将 `onlyDelConfigById(toBeDelConfigIds)` 改为 `DemandConfigByInstanceRepositoryRemoveParam(configIds)` + `delConfigByInstance(removeParam)` 是否安全。
- Scope: internal
- Date: 2026-07-08

## Findings

### Overall conclusion

结论：这次改动的方向是必要且整体安全的。原 `onlyDelConfigById` 只删除原始配置，不发布 `DemandConfigChangeEvent`，会让“从分组中移除的实例”对应的 `demand_config_result` 无法同步重算。改为 `delConfigByInstance` 后，仓储会在删除前按 ID 回查旧配置，删除后发布 DELETE 事件，结果刷新监听器可用旧配置定位实例并刷新当前实例及下级实例。

主要可接受风险：本链路会额外触发一次结果重算和一次分组清理监听；在同一 `addOrUpdateConfig` 后续保存新入参实例时还会触发 ADD/EDIT 事件，因此同一个分组保存动作可能产生多次同步刷新。该风险是性能/重复计算风险，不是数据正确性风险。当前未发现会误删“正在编辑的分组”的证据。

### 1. addOrUpdateConfig existing configGroupId semantics

- `addOrUpdateConfig` 读取 `configGroupId` 后，若存在分组，会把查询参数的实例条件清空，只按分组维度查询已有 `dbDemandConfigs`，因此得到的是该组当前 DB 中的配置集合，而不是仅入参实例集合；见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/gateway/DemandConfigGatewayImpl.java:636-644`。
- 已有分组分支先更新分组名，再遍历 `dbDemandConfigs`，把 `demandInstanceId` 不在入参 `instanceIds` 中的配置 ID 加入 `toBeDelConfigIds`，并从 `dbDemandConfigs` 中移除；见 `DemandConfigGatewayImpl.java:682-701`。
- 这个语义与注释一致：存在分组时，“移除入参实例中不存在的实例”；后续 `addOrEditConfig` 只对入参 `instanceIds` 执行新增/编辑，见 `DemandConfigGatewayImpl.java:756-759`。
- 新写法仅把删除实现从纯删除换成事件型删除，删除 ID 集合仍来自同一 `toBeDelConfigIds`，不扩大被删除的原始配置范围；见 `DemandConfigGatewayImpl.java:688-700` 和 `DemandConfigRepositoryImpl.java:212-227`。

### 2. delConfigByInstance delete behavior and event payload

- `DemandConfigByInstanceRepositoryRemoveParam` 默认 `publishEvent = true`，本次构造参数只设置 `configIds`，因此会发布删除事件；见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/param/DemandConfigByInstanceRepositoryRemoveParam.java:16-24`。
- `delConfigByInstance` 在删除前用 `configIds/templateId/sceneId/instanceIds` 回查待删配置，并显式选择 `id/relGroupId/relTemplateId/relSceneId/demandInstanceType/demandInstanceId`；见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository/DemandConfigRepositoryImpl.java:205-221`。
- 删除后若 `publishEvent` 为 true，会按 `relTemplateId + relSceneId` 分组发布 `DemandConfigChangeEvent.Detail(DataTypeEnum.DELETE, Config(oldConfig, null))`；见 `DemandConfigRepositoryImpl.java:225-235`。
- 对当前结果刷新和分组清理来说，DELETE 事件的 `oldConfig` 字段足够：结果刷新只需要 old/new 中任一配置的实例类型和实例 ID；分组清理只需要旧配置的 `relGroupId`；事件里选出的列已经覆盖这些字段。相关消费见 `DemandConfigResultRefreshEventListener.java:85-95` 和 `DemandConfigGatewayImpl.java:937-950`。

### 3. DemandConfigResultRefreshEventListener DELETE handling

- 配置变化监听器用 `Objects.nonNull(newConfig) ? newConfig : oldConfig` 统一提取受影响实例，因此 DELETE 事件会使用 `oldConfig` 定位实例；见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/listener/DemandConfigResultRefreshEventListener.java:78-90`。
- 对每个受影响实例，监听器调用 `refreshByInstanceAndChildren(templateId, sceneId, instanceType, singleton(instanceId))`，而不是直接删除结果；注释明确指出删除自身配置后可能露出上级继承值，需要重算；见 `DemandConfigResultRefreshEventListener.java:91-96`。
- `refreshByInstanceAndChildren` 会把当前实例和 `instanceService.listChildrenInstanceIdsByInstance` 返回的子级实例一起纳入刷新，再调用 `refreshInstances`；见 `DemandConfigResultRefreshRepositoryImpl.java:128-149`。
- `refreshInstances` 打开继承查询、关闭屏蔽过滤，实时计算后 upsert 有最终配置的结果，并删除实时链路未命中的结果，能覆盖“删除自身配置后继承上级值”与“删除后无有效结果”两种情况；见 `DemandConfigResultRefreshRepositoryImpl.java:83-125`。
- 删除结果会按业务键调用 `removeResults`，并关闭开放快照；见 `DemandConfigResultRefreshRepositoryImpl.java:467-477` 和 `DemandConfigResultRepositoryImpl.java:131-157`。

### 4. group cleanup listener safety

- `DemandConfigGatewayImpl.onDemandConfigRemoveEvent` 是普通 `@EventListener(DemandConfigChangeEvent.class)`，会消费本次 `delConfigByInstance` 发布的 DELETE 事件；见 `DemandConfigGatewayImpl.java:920-926`。
- 监听器只从 DELETE 明细的旧配置提取 `relGroupId`，然后按同一 `templateId/sceneId/configGroupIds` 重新查询是否仍有关联配置；存在关联则不删，不存在才删分组；见 `DemandConfigGatewayImpl.java:927-962`。
- 在本次 `addOrUpdateConfig` 已有 `configGroupId` 分支中，被删除的是“不在入参实例集合中”的旧配置；入参中仍保留的实例配置在删除前没有被删，且 `dbDemandConfigs.removeIf` 只移除待删对象。因此分组中只要还存在任一保留实例配置，分组清理查询能看到它，不会删除当前正在编辑的分组；见 `DemandConfigGatewayImpl.java:687-701` 和 `DemandConfigGatewayImpl.java:945-962`。
- 若入参实例集合完全不包含该分组当前任何 DB 配置，删除事件会使分组清理监听器删除旧分组；但后续 `addOrEditConfig` 仍用同一个 `configGroupId` 保存新实例，并不会创建新分组。这是需要业务确认的边界：如果 UI 允许“复用已有分组 ID 但替换为全新实例集合”，当前监听器有机会在保存新配置前删掉该分组记录。常规编辑语义一般会保留至少一个旧实例或使用现有分组编辑实例，不触发误删；但该全替换边界最好加测试或保护。
- 另一个独立小问题：`onDemandConfigRemoveEvent` 在 `groupIds` 为空时 `return`，会跳出整个方法而不是继续处理后续 detail；当前事件通常单 DELETE detail，风险较低，但不是本次改动引入。

### 5. DTX onlyDelConfigById path

- DTX 照明发布上下文仍调用 `demandConfigRepository.onlyDelConfigById(this.toBeDelDemandConfigIdAndData.keySet())` 做纯删除；见 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/context/lighting/DemandLightingDtxPublishChangeContext.java:638-646`。
- `onlyDelConfigById` 的接口语义就是“根据配置ID仅删除配置”，实现只调用 `removeByIdIgnore`，不发布配置变更事件；见 `DemandConfigRepository.java:81-87` 和 `DemandConfigRepositoryImpl.java:194-201`。
- 保留 DTX 纯删除是合理的：`DemandConfigResultRefreshEventListener` 对 `DtxPublishMessageEvent` 有独立监听，按所有模板类型触发 `refreshByTemplateType`，并且有 DTX 锁；见 `DemandConfigResultRefreshEventListener.java:51-67`。
- `refreshByTemplateType` 会按模板类型查模板、预取需求空间范围并刷新各模板，覆盖 DTX 导致的原始配置删除结果重算；见 `DemandConfigResultRefreshRepositoryImpl.java:172-185` 和 `DemandConfigResultRefreshRepositoryImpl.java:323-353`。
- 因此 DTX 不改成事件型删除可以避免在 DTX 大批量删除时额外触发大量单实例配置删除刷新，符合“已有独立重算覆盖”的判断。

### 6. duplicate refresh, missed refresh, transaction timing, performance

- 漏刷新：本次改动修复了已有分组编辑移除实例时的漏刷新。旧纯删除不会发布事件；新删除会发布 DELETE，刷新监听器可重算被移除实例及子级；见 `DemandConfigRepositoryImpl.java:196-201`、`DemandConfigRepositoryImpl.java:228-235`、`DemandConfigResultRefreshEventListener.java:85-96`。
- 重复刷新：同一次 `addOrUpdateConfig` 中，先删除被移除实例发布 DELETE 刷新，后续 `addOrEditConfig` 对入参实例发布 ADD/EDIT 刷新；见 `DemandConfigGatewayImpl.java:696-700`、`DemandConfigGatewayImpl.java:756-759`、`DemandConfigRepositoryImpl.java:157-160`。这些实例集合通常不同；若有下级范围重叠，会重复重算部分子级。
- 事务时序：`addOrUpdateConfig` 外层有事务；结果刷新监听器是 `@TransactionalEventListener(BEFORE_COMMIT, fallbackExecution = true)`，会在提交前同步刷新，符合任务要求“配置增删改后同步重算，失败回滚”；见 `DemandConfigGatewayImpl.java:599-601`、`DemandConfigResultRefreshEventListener.java:78-79` 和任务 PRD 中刷新失败不得静默吞掉的约束。
- 分组清理时序：分组清理监听器是普通 `@EventListener`，在事件发布时同步执行，早于后续 `addOrEditConfig`。这让“全量替换分组实例集合”的边界存在误删分组风险；见 `DemandConfigGatewayImpl.java:925-962` 和 `DemandConfigGatewayImpl.java:756-759`。
- 性能：`delConfigByInstance(configIds)` 相比 `onlyDelConfigById` 多一次按 ID 回查和事件发布；每个被删实例会触发 `refreshByInstanceAndChildren`，其中会查子级实例并调用实时配置继承链路；见 `DemandConfigRepositoryImpl.java:212-221`、`DemandConfigResultRefreshEventListener.java:91-96`、`DemandConfigResultRefreshRepositoryImpl.java:138-149`。对常规分组编辑是可接受成本，但批量移除大量楼层/楼栋/项目级配置时需要关注写入耗时。

## Recommendations

1. 保留本次改动：已有分组编辑中删除不再属于分组的实例时，必须发布 DELETE 事件，才能让结果表同步重算。
2. 增加回归用例：已有 `configGroupId`，DB 中有 A/B，入参只保留 A，保存后 B 的结果表应重算为继承上级或删除，分组仍存在。
3. 增加边界用例：已有 `configGroupId`，DB 中有 A/B，入参替换为全新 C/D，验证普通 `@EventListener` 分组清理是否会先删除该 `configGroupId`。若业务允许这种全替换，建议在 `onDemandConfigRemoveEvent` 中跳过本次正在编辑的分组，或把分组清理改为提交前/提交后基于最终状态检查。
4. 保持 DTX `onlyDelConfigById` 不变：DTX 有 `DtxPublishMessageEvent` 独立全量/模板类型刷新，改成配置删除事件会造成大量重复刷新。
5. 若后续发现保存分组时刷新耗时明显增加，可考虑在 `addOrUpdateConfig` 内聚合本次删除和新增/编辑实例，发布一个合并事件或由专门刷新入口批量刷新，避免每个实例单独展开子级。

## Caveats / Not Found

- 未运行测试，结论基于静态代码审查。
- 未读取或引用临时需求文档目录。
- 未使用 git diff；审查基于工作区当前文件内容和用户指定的变更点。
- 当前任务 `implement.jsonl` 和 `check.jsonl` 仍为 seed 示例行，未提供额外实现/检查上下文。

## Final Disposition

本节记录代码提交 `62cd8a3f16` 的最终处置。

- 保留 `addOrUpdateConfig` 中使用 `delConfigByInstance(...)`：删除前回查旧配置并发布 DELETE 事件，结果监听器才能重算被移除实例及其下级实例。
- `configIds` 是全局唯一主键，按 ID 回查无需额外项目过滤；Repository 仍从当前上下文限制其他查询边界。
- 只有黑名单事件允许删除原始需求配置，屏蔽事件不会进入该路径。
- DTX 内部批量清理继续使用 `onlyDelConfigById(...)`，因为 DTX 发布事件会统一刷新结果；改为逐配置事件会造成重复刷新。
- 分组完全替换的历史边界不是本次结果预计算引入的问题，当前未修改既有分组清理语义；如业务允许复用同一分组 ID 全量替换实例，应单独补回归测试。
