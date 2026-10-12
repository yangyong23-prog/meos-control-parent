# Air Terminal API Contracts

## Scenario: listSpaceEquipment basic response projection

### 1. Scope / Trigger

- Trigger: `/airTerminal/listSpaceEquipment` returns object and terminal business data, while model-view data is loaded by the frontend through the dedicated model diagram API.
- Applies when changing the controller response type, terminal object field projection, or the internal graph result used by cache rebuilds.

### 2. Signatures

- API: `POST /airTerminal/listSpaceEquipment`
- Request body: `AirTerminalSpaceWithStatusQueryParam`
- Response body: `R<AirTerminalSpaceEquipmentVO>`
- Internal service result: `AirTerminalSpaceWithStatusVO`
- `AirTerminalSpaceWithStatusQueryParam` 和 `RunningMonitorSpaceQueryParam` 均不包含
  `useWhiteList` 等空间范围开关。

### 3. Contracts

- The space response must not declare or serialize `bimId`, `bimLocation`, or `outline`.
- The equipment response must not declare or serialize `bimId` or `bimLocation`.
- Monitoring/control state and relation IDs keep their existing semantics. The endpoint-specific space response does not declare or serialize `tdbRule` or `co2Rule`.
- `listSpaceEquipment(param, true)` and the READY new-cache path do not populate `tdbRule` or `co2Rule`. `unableMonitor` keeps its existing rule-existence semantics; the internal `useCache=false` cache-rebuild path retains the complete shared graph.
- The controller converts the internal graph result to the endpoint-specific response. Do not add ignores to the shared graph VOs because `listSpaceWithStatus` and the new cache rebuild strategy still use them.
- `useCache=true` fallback queries and READY object-cache reads request only fields used by this endpoint; they must not request model-view fields.
- `useCache=false` is the cache-rebuild contract and must continue returning the complete internal graph result, including fields needed to write the model-view cache.
- The READY endpoint path reads the object cache and existing relation/rule sources only; model-view fields are obtained by the separate model diagram API.
- 前端不能通过 `useWhiteList` 选择空间范围。`listSpaceEquipment(..., true)` 必须读取
  `SPACE_RANGE_TERMINAL`：`WHITE_LIST` 选择白名单 Key，`FULL` 选择项目空间 Key；READY 只决定同一
  范围读取新版缓存还是实时 RWD，不能改变范围。
- `listSpaceEquipment(..., false)` 是缓存构建内部调用，必须始终计算项目全量空间，不读取壳配置或缩小为
  白名单范围。

### 4. Validation Matrix

| Path | Expected behavior |
| --- | --- |
| READY new object cache | Return endpoint fields without exposing model-view fields or `tdbRule` / `co2Rule` |
| Not READY RWD query | Convert the real-time graph object at the controller boundary and omit model-view and rule-flag fields |
| RWD fallback with `useCache=true` | Request endpoint fields only and do not populate `tdbRule` / `co2Rule` |
| 壳配置为 `WHITE_LIST`、未 READY | 调用固定白名单查询入口，从白名单 Key 取对象 ID 后实时查询 RWD |
| 壳配置为 `WHITE_LIST`、READY | 只读白名单空间 Key；空关联正常返回空空间 |
| 壳配置为 `FULL` | 使用项目空间 Key 或项目全量实时查询，不受请求体影响 |
| Cache rebuild with `useCache=false` | Preserve the complete internal graph result, including rule flags, for cache persistence |
| `/airTerminal/listSpaceWithStatus` | Preserve the existing shared graph response contract |

### 5. Good / Base / Bad Cases

- Good: 壳配置为 `WHITE_LIST` 时，调用方不能通过查询参数改为项目全量空间。
- Base: 壳配置缺失时使用 `FULL` 默认值，按项目全量空间查询。
- Bad: 将 `useWhiteList` 重新加入任意查询参数，导致调用方可以绕过已保存的空间范围配置。

### 6. Tests Required

- 编译 `common`，确认两个空间查询参数均没有 `getUseWhiteList` 或 `setUseWhiteList` 调用残留。
- 聚焦服务测试应断言：`WHITE_LIST` 在 READY 和未 READY 时都选择白名单范围；`FULL` 选择项目范围；
  `useCache=false` 不读取白名单范围。

### 7. Wrong vs Correct

#### Wrong

```java
spaceQueryParam.setUseWhiteList(true);
```

请求体是调用方输入，不能作为空间权限和缓存范围的事实来源。

#### Correct

```java
boolean useWhitelistSpaceRange = useCache
        && this.airTerminalQuerySpace.isWhitelistSpaceRangeConfigured();
List<DtpRwdSpaceDTO> spaces = useWhitelistSpaceRange
        ? this.runningMonitorAirTerminalStrategy.listWhitelistSpaces(spaceQueryParam)
        : this.runningMonitorAirTerminalStrategy.listSpaces(spaceQueryParam);
```

仅业务查询读取持久化壳配置；固定白名单入口不从查询参数取得范围。缓存构建传入
`useCache=false`，因此始终使用项目全量入口从 RWD 计算完整空间。

## Scenario: dedicated model-view field query

### 1. Scope / Trigger

- Trigger: the frontend loads model-view fields after `/airTerminal/listSpaceEquipment` returns the basic object and business response.
- Applies to the shared cache model API used by air terminal and lighting after their new object model is READY.

### 2. Signatures

- API: `POST /cacheRwd/listModelViewFieldsByObjectId`
- Request body: `CacheRwdModelViewFieldsQueryParam`
- Request fields: `runningMonitorType`, non-empty `objectIds`
- Response body: `R<List<Map<String, Object>>>`

### 3. Contracts

- Query `cache_object_model_view` once for the requested IDs and select only `id` and `view_data`; the resource service may partition a large ID collection, but consumers must not query once per object.
- Each returned map contains `objectId = cache_object_model_view.id` and then exposes the fields actually stored in `view_data` at the top level.
- `objectId` is always authoritative and cannot be overridden by a same-name historical JSON field.
- Model-view fields remain controlled only by the classCode write configuration. This API must not introduce another field map based on running-monitor type, object type, cache key, or query scene.
- Missing IDs affect only those items. Return all rows actually found without requiring a complete hit and without falling back to legacy `cacheRwd` data.
- Query the new table only when `CacheObjectModelStatusService#isReady(runningMonitorType)` is true. A supported type that is not READY queries RWD once for the requested object IDs and projects the same classCode-controlled model-view fields; unsupported types are logged and keep the previous empty-result behavior.
- SQL `NULL` or blank historical `view_data` returns the authoritative `objectId` without model-view fields. Mapper and database exceptions continue upward.

## Scenario: listSpaceEquipment air handling group related unit IDs

### 1. Scope / Trigger

- Trigger: `/airTerminal/listSpaceEquipment` response contract includes an additional equipment field for air handling unit control groups.
- Applies when changing air terminal response VOs or enriching `AirTerminalEquipmentGraphVO`.

### 2. Signatures

- API: `POST /airTerminal/listSpaceEquipment`
- Request body: `AirTerminalSpaceWithStatusQueryParam`
- Response body: `R<AirTerminalSpaceEquipmentVO>`
- Public equipment item type: `AirTerminalSpaceEquipmentVO.EquipmentVO`
- Internal equipment item type: `AirTerminalEquipmentGraphVO`

### 3. Contracts

- Field: `controlledAirHandlingUnitIds`
- Type: `Set<String>`
- Filled only when `equipment.classCode == DtpRwdClassCodeEnum.ACAHG_GROUP.value()` (`ACAHG`).
- Value source: air handling units (`ACATAH`) related to the `ACAHG` group through `DtpRwdRelationTypeEnum.EQUIP_IN_GROUP`.
- Related `ACATAH` results must be intersected with the bottom-level terminal `ACATAH` ID set already queried for the air terminal scope; do this with `.setId(acatahIds)` on the relation query.
- Populate the relation during bottom-level air terminal equipment querying, before `AirTerminalEquipmentGraphVO` is serialized into `CacheRwdObject`.
- Empty relation result: return `Collections.emptySet()` on the `ACAHG` equipment item.
- Non-`ACAHG` equipment: do not populate the field.
- `AirTerminalSpaceGraphVO.controlledRelEquipmentIds` is a separate historical field for space-to-equipment control/service relations; do not use it for `ACAHG -> ACATAH`.
- Cache hit path must not run an extra relation query for this field; it should read the cached equipment VO as-is.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| No spaces and no equipments | Keep existing behavior: return `null` |
| Base query has no `ACAHG` items | Do not run the `EQUIP_IN_GROUP` relation query |
| Base terminal `ACATAH` ID set is empty | Do not run the `EQUIP_IN_GROUP` relation query; all `ACAHG` items get an empty set |
| `ACAHG` has no `EQUIP_IN_GROUP` relation | `controlledAirHandlingUnitIds = Collections.emptySet()` |
| RWD relation query returns one or more terminal-scope `ACATAH` items | Set contains those `ACATAH` IDs grouped by source `ACAHG` ID |

### 5. Good/Base/Bad Cases

- Good: one `ACAHG` group related to two `ACATAH` units returns both unit IDs in `controlledAirHandlingUnitIds`.
- Base: normal equipment such as `ACATAH` or `ACATFU` keeps the field unset.
- Base: space items may still have `controlledRelEquipmentIds` for their own existing relation semantics.
- Bad: querying `EQUIP_IN_GROUP` inside the equipment loop, service response assembly, or cache hit path creates avoidable RWD calls and violates the cache contract.

### 6. Tests Required

- Compile check must pass because `AirTerminalConverter` uses `unmappedTargetPolicy = ReportingPolicy.ERROR`.
- Strategy-level test should mock the `EQUIP_IN_GROUP` relation query and assert:
  - `ACAHG` item receives related unit IDs.
  - `ACAHG` item receives an empty set when no relation exists.
  - non-`ACAHG` item is not populated.
  - `AirTerminalServiceImpl#getSpaceEquipmentFromCache` does not issue an extra relation query.

### 7. Wrong vs Correct

#### Wrong

```java
// Wrong: queries the to-side object with relationFrom + objFrom.
new DtpRwdObjectQueryParam()
        .setClassCodeEnum(DtpRwdClassCodeEnum.ACATAH)
        .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(groupIds));
```

#### Correct

```java
// Correct: group is the from-side, ACATAH is the to-side.
new DtpRwdObjectQueryParam()
        .setId(acatahIds)
        .setClassCodeEnum(DtpRwdClassCodeEnum.ACATAH)
        .setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(groupIds))
        .filterMonitoringSurveillanceStatus()
        .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
```

Map the relation to the response through the DTO produced by the bottom-level query:

```java
// Strategy layer: write related unit IDs to the ACAHG DTO.
equipment.setRelObjIds(acahgIdAndAcatahIds.getOrDefault(equipment.getId(), Collections.emptySet()));

// Converter layer: expose the field only for ACAHG.
if (DtpRwdClassCodeEnum.ACAHG_GROUP.value().equals(source.getClassCode())) {
    return source.getRelObjIds();
}
```
