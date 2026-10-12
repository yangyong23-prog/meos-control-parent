## Context

机房安全五个定制接口每次请求走实时查询链路：

**API 1 — Dashboard 空间列表** (`DashboardGridChartController#querySpaceComputerRoomSecurity`)：
1. `listSpacesComputerRoomSecurity()` — InfoCode Feign → BDTP Feign 查空间
2. `listEquipmentsFromRwdComputerRoomSecurity()` — BDTP Feign 查设备
3. Knowledge Feign ×4 — 名称/标签/排除列表
4. DB — 白名单

总 Feign 约 8 次，BDTP 是最大瓶颈，整体耗时约 14.9 秒。

**API 2 — 空间设备列表** (`InstanceController#listEquipmentsInBySpaceIdsComputerRoomSecurity`)：
1. `listEquipmentInBySpaceIds(spaceId)` — BDTP Feign 查单空间设备
2. `mapObjectIdAndExistsRuleInfoCodes()` — IoT Feign 查可运行状态
3. Knowledge Feign ×2 — classCodeName / majorName

总 Feign 4 次，BDTP + IoT 是主要瓶颈。

**API 3 — 楼层设备列表** (`InstanceController#listEquipmentsComputerRoomSecurity`)：
1. `listObjectsByTackValueLogic(floorId)` — BDTP Feign 按楼层查空间，过滤 classCode=SPACE 得到 spaceIds
2. `param.queryObject()` — BDTP Feign 按 spaceIds 查设备
3. Knowledge Feign ×3 — excludeClassCodes + classCodeName + equipCriticalLevel

总 Feign 5 次，BDTP 是主要瓶颈。

**API 4 — 异常事件过滤** (`AbnormalDataController#listAbnormalEventNew` + `exceptionFilterConditionNew`)：
1. 入参 `instanceIds` 来自 API 1 返回的空间和设备 ID 集合（前端两步调用）
2. `getFilterExcludeClassCodes()` — Knowledge Feign ×2（并行）
3. `dtpRwdClientService.listObject()` — BDTP Feign 查门禁受控状态（CFCSDR/CFCSFR/SPEGFG + monitoringSurveillanceStatus）
4. `dtpRwdClientService.listEquipment()` — BDTP Feign 查排除类型设备

总 Feign 4 次，BDTP 2 次串行是主要瓶颈。仅前端查询路径，无 SSE 推送路径。

**API 5 — 风险事件查询** (`DangerEventServiceImpl#listByDangerObject`，COMPUTER_ROOM_SECURITY 分支)：
1. 入参 `objectIds` 来自 API 1 返回的空间和设备 ID 集合
2. `exceptionEventClientService.listDangerEvent()` — ExceptionEvent Feign（并行）
3. `exceptionEventClientService.listDangerInstanceTree()` — ExceptionEvent Feign（并行）
4. `CompletableFuture.allOf(future, future1).join()` — 等待两个并行任务
5. `dtpRwdClientService.mapObjectIdAndLocalName()` — BDTP Feign 查 ID→名称映射（串行，与前两步无数据依赖）

总 Feign 3 次，BDTP 1 次串行且与 ExceptionEvent 无依赖却等待后执行是主要瓶颈。

VENTILATION 模块已完成新缓存对象模型切换（`ventilation-cache-object-model`），提供参照模式。规则缓存基础设施（`cache_rule` / `cache_rule_key_rel` / `CacheRuleRepository`）已完备，AIR_TERMINAL 已消费规则读取接口。

**约束**：
- 旧 `cache_rwd_object` 写入保留，新旧缓存并存
- `meosCache=true` 已移除，定制接口不走通用旧缓存路径
- 缓存数据需支持未来楼层级 API 消费（不仅限于 Dashboard API）
- API 2、API 3、API 4 和 API 5 的写入侧由同一缓存构建任务统一完成，它们只做读取消费

## Goals / Non-Goals

**Goals:**

- API 1: BDTP Feign 从 2~3 次降为 0（-100%），总 Feign 从 ~8 次降至 ~6 次
- API 2: BDTP + IoT Feign 从 2 次降为 0（-100%），总 Feign 从 4 次降至 2~3 次
- API 3: BDTP Feign 从 2 次降为 0（-100%），总 Feign 从 5 次降至 3 次
- API 4: BDTP Feign 从 2 次降为 0（-100%），总 Feign 从 4 次降至 2 次（仅保留 Knowledge）
- API 5: BDTP Feign 从 1 次降为 0（-100%），总 Feign 从 3 次降至 2 次（ExceptionEvent），BDTP 替换为本地 DB 且 3 路并行执行
- 缓存范围覆盖楼层级全量空间+设备+IoT 规则，五个 API 共享同一缓存
- 新旧缓存并存，`isReady()=false` 时自动 fallback 到实时查询
- 设备查询新增 `monitoringSurveillanceStatus`、`bimId`、`bimLocation` 列
- IoT 规则缓存写入 `cache_rule`，读取侧可运行状态 0 Feign

**Non-Goals:**

- 不缓存 Knowledge 数据（classCodeName、equipCriticalLevel、majorName 保持实时查询）
- 不缓存 `equipCriticalLevel` 的 BDTP 侧值（最终被 Knowledge 值覆盖，不存入缓存）
- 不废除旧 `cache_rwd_object` 写入
- 不修改 IoT 实时数据值查询逻辑（仅缓存规则实例化状态）

## Decisions

### D1: 楼层级全量缓存，读取侧过滤

**选择**: 缓存机房安全空间所在楼层的**全部空间和设备**，读取时按 roomFuncType 过滤出机房安全空间。

**原因**: 未来楼层级 API 会用到同楼层非机房空间的数据，楼层级全量缓存可双重消费：Dashboard API 过滤返回机房空间，楼层级 API 直接用全量。

**备选方案**:
- 只缓存机房安全空间 → 未来楼层级 API 无法复用，需重新构建缓存

### D2: 写入侧不含 Knowledge Feign

**选择**: `mapClassCodeAndName`、`mapClassCodeTypeAndTag`、`getFilterExcludeClassCodes`、`getRoomFuncTypeName` 全部在读取侧实时查询，写入侧不调用任何 Knowledge 服务。

**原因**:
- Knowledge 数据可能频繁变更（名称、标签），实时查询保证新鲜度
- 写入侧依赖越少，构建失败率越低，构建速度越快
- Knowledge Feign 均为轻量查询，读取侧可并行执行，实际延迟很低

**备选方案**:
- 烘焙到 `undefinedProperties` → Knowledge 数据变更后缓存不更新，需等下次构建，实时性差

### D3: 读取侧多层运行时过滤

**选择**: 读取侧按以下顺序执行过滤（全部内存操作）：

1. **roomFuncType 过滤** — InfoCode Feign 获取机房功能类型列表，从楼层全量空间中筛出机房安全空间
2. **白名单过滤** — DB 查 InstanceConfig，三态逻辑：null=不过滤、空集=返回空列表、非空=只保留白名单内空间
3. **excludeClassCodes 过滤** — Knowledge Feign 获取排除设备类型（TJJG + pipeline），从设备列表中移除

**原因**: 不同 API 可能需要不同过滤组合（如楼层级 API 不过滤 roomFuncType），运行时过滤比写入时过滤更灵活。

### D4: 缓存键命名与状态注册

**选择**:
- `CacheObjectModelKeyConfig` 新增 `COMPUTER_ROOM_SECURITY_LIST_SPACE = "COMPUTER_ROOM_SECURITY_listSpace"` 和 `COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT = "COMPUTER_ROOM_SECURITY_listEquipment"`
- `CacheObjectModelStatusService.supportedRunningMonitorTypes` 新增 `RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY`

**原因**: 与 VENTILATION、AIR_TERMINAL 命名规则一致，`isReady()` 机制自动管理缓存就绪状态。

### D5: 设备查询 withColumns 扩展

**选择**: 设备查询 `withColumns` 新增 `monitoringSurveillanceStatus`、`bimId`、`bimLocation`，数据自然存入 `DtpRwdEquipmentDTO.undefinedProperties`，`CacheObject` 表已有独立列支持 `monitoringSurveillanceStatus`。

**原因**:
- `monitoringSurveillanceStatus` 在 API 1 和 API 3 读取侧需要使用
- `bimId` 和 `bimLocation` 是 API 3 楼层图必须返回给前端的字段
- 这些字段的最终值来自 BDTP（无 Knowledge 覆盖），需存入缓存
- `equipCriticalLevel` 的 BDTP 侧值不缓存，因为最终被 Knowledge `mapClassCodeTypeAndTag` 的值覆盖

### D6: 新旧缓存并存

**选择**: `CacheRwdComputerRoomSecurityStrategy.buildCacheData()` 同时执行旧 `cache_rwd_object` 写入和新 `cache_object` 写入。读取侧通过 `isReady(COMPUTER_ROOM_SECURITY)` 判断走新缓存还是走实时查询（不走旧缓存）。

**原因**: 降低切换风险。新缓存就绪后自动切换，`isReady()=false` 时 fallback 到实时查询，确保数据正确性。

### D7: IoT 规则缓存写入 cache_rule

**选择**: 写入侧调用 `abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes()` 查询 4 个信息点（RUN_STATUS、SWITCH_STATUS、ON_OFF_STATUS、DEPLOY_WITHDRAWALARM_STATUS）的实例化状态，构建 `CacheRule` 集合后调用 `CacheRuleRepository.replaceRules(CRS_listEquipment, rules)` 写入 `cache_rule` / `cache_rule_key_rel` 表。读取侧通过 `CacheRuleRepository.mapInfoCodesByObjectIds()` 本地查询判断 operableRunning，0 Feign。

**原因**:
- IoT 规则查询（`mapObjectIdAndExistsRuleInfoCodes`）是 Feign 调用，缓存后读取侧 0 Feign
- `cache_rule` 表基础设施已完备，AIR_TERMINAL 已有消费先例
- 规则与设备生命周期一致，复用 `CRS_listEquipment` 缓存键管理归属
- 规则读取不按缓存键过滤，直接按 `(projectId, objectId, infoCode, type)` 查主表

**备选方案**:
- 不缓存规则，读取侧仍走 IoT Feign → 每次请求多 1 次远程调用，不符合优化目标

### D8: API 2 通过 cache_object_rel 按 spaceId 定位设备

**选择**: API 2 读取侧先从 `cache_object_rel`（EQUIPMENT_IN_SPACE 关系）按 `rel_object_id = spaceId` 查出设备 ID 集合，再从 `cache_object` 按设备 ID 取设备数据。

**原因**:
- `cache_object_rel` 有索引，按 spaceId 精确查询高效
- 与 AIR_TERMINAL 的关系查询模式一致
- 避免加载楼层全量设备再内存过滤，减少数据传输量

**备选方案**:
- 加载楼层全量设备后按 `relObjIds` 内存过滤 → 多一次全量扫描，虽然数据量小（~200）但仍不如关系表精准

### D9: API 3 通过缓存空间 + 关系表定位楼层设备

**选择**: API 3 读取侧先从 `CRS_LIST_SPACE` 缓存空间中按 `floorId` + `classCode=SPACE` 内存过滤出 spaceIds，再从 `cache_object_rel` 按 spaceIds 查设备 ID，最后从 `cache_object` 取设备数据。

**原因**:
- 缓存中已存楼层全量空间，按 floorId + classCode 过滤是纯内存操作
- 复用 `cache_object_rel` 关系表按 spaceIds 查设备，与 API 2 模式一致
- 消除原方法中 2 次 BDTP Feign（空间定位 + 设备查询）
- Knowledge 数据（classCodeName、equipCriticalLevel、excludeClassCodes）仍实时查询

**备选方案**:
- 按 floorId 加载全量设备再内存过滤 → 数据量更大，且无法精确匹配 spaceIds

### D10: API 4 通过 listByObjectIds 批量查缓存替代 BDTP Feign

**选择**: `exceptionFilterConditionNew()` 在 `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 时，通过 `CacheObjectRepository.listByObjectIds(equipmentIds, {id, classCode, monitoringSurveillanceStatus})` 一次本地 DB 查询替代 2 次 BDTP Feign，内存中按 classCode 分别过滤门禁设备和排除类型设备。`getFilterExcludeClassCodes()` 的 Knowledge Feign 保留（符合 D2 策略）。

**原因**:
- API 4 的 `equipmentIds` 来自 API 1 返回结果，完全在缓存写入范围内
- `listByObjectIds` 已有 4 处消费验证（AirTerminal、Lighting、CacheRwdService、InfoCodeConfig），接口稳定
- `cache_object` 表有 `monitoring_surveillance_status` 独立列，`isControlled()` 可直接从缓存读取判断
- 2 次 BDTP Feign 合并为 1 次本地 DB 查询，消除串行远程调用
- Knowledge Feign（`getFilterExcludeClassCodes`）保留读取侧实时查询，与 D2 决策一致

**备选方案**:
- 保持 BDTP Feign 不变 → 每次异常事件查询多 2 次远程调用，不符合优化目标
- 加载楼层全量设备再内存过滤 → 数据量大（~23,435），且需要遍历所有楼层，不如按 ID 精准查询

### D11: API 5 通过 listByObjectIds 替代 BDTP 并改为 3 路并行

**选择**: `DangerEventServiceImpl.listByDangerObject()` 在 `isReady(COMPUTER_ROOM_SECURITY)` 为 `true` 时，将 `dtpRwdClientService.mapObjectIdAndLocalName(objectIds)` 替换为 `CacheObjectRepository.listByObjectIds(objectIds, {id, localName})` 本地 DB 查询，并将其与 2 次 ExceptionEvent Feign 改为 3 路 `CompletableFuture` 并行执行。

**原因**:
- API 5 的 `objectIds` 来自 API 1 返回结果，完全在缓存写入范围内
- `mapObjectIdAndLocalName` 只需 `id` 和 `localName` 两个字段，`listByObjectIds` 支持指定 `withColumns`
- 原代码中 BDTP 调用在 `CompletableFuture.allOf().join()` 之后串行执行，与前两个 ExceptionEvent 查询无数据依赖
- 改为 3 路并行后，BDTP Feign 被本地 DB 替代，且与 ExceptionEvent 查询完全并行，显著降低总耗时
- 从缓存结果构建 `Map<String, String> objectIdAndLocalName` 的逻辑与原方法返回值格式一致

**备选方案**:
- 保持 BDTP Feign 不变，仅调整并行结构 → 仍有 1 次远程调用
- 保持当前 2 并行 + 1 串行结构 → BDTP 调用等待前两个任务完成后才执行，浪费时间

## Risks / Trade-offs

- **[楼层级缓存数据量增大]** → 机房安全空间所在楼层的全量空间/设备比仅机房安全空间多，但相比 BDTP 实时查询的全量传输，本地 DB 查询开销可忽略。
- **[Knowledge Feign 仍在读取侧]** → Knowledge 查询轻量且可并行，实际延迟远低于 BDTP。如果 Knowledge 服务不可用，fallback 到实时查询链路。
- **[IoT 规则缓存构建间隔内状态不一致]** → 设备规则实例化状态变更在下次缓存构建前不反映。与 VENTILATION 缓存行为一致，构建间隔由定时任务控制。
- **[meosCache=true 已移除]** → 旧缓存路径不再生效，新缓存 `isReady()=false` 时走实时查询。过渡期首次请求可能稍慢，但缓存构建完成后立即生效。
- **[缓存构建间隔内数据不一致]** → 空间/设备变更在下次缓存构建前不反映。这与 VENTILATION 缓存行为一致，构建间隔由定时任务控制。
- **[API 4 依赖 API 1 数据]** → `exceptionFilterConditionNew` 的 `equipmentIds` 来自 API 1 返回结果，缓存就绪后两者数据一致。若 API 1 缓存未就绪而 API 4 缓存已就绪（理论上不会发生，同一构建任务），fallback 到实时查询。
- **[API 5 依赖 API 1 数据]** → `listByDangerObject` 的 `objectIds` 来自 API 1 返回结果，与 API 4 同理。缓存就绪后数据一致。
