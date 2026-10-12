# InfoCode API Contracts

## Scenario: listTemplateTypes objectTypes filter

### 1. Scope / Trigger

- Trigger: changing `/infoCodeConfig/listTemplateTypes` request fields that filter template types.
- Applies to both `/infoCodeConfig/listTemplateTypes` and the legacy `/equipment/infoCodeConfig/listTemplateTypes`, because both use `InfoCodeConfigTemplateTypeQueryParam`.

### 2. Signatures

- API: `POST /infoCodeConfig/listTemplateTypes`
- Legacy API: `POST /equipment/infoCodeConfig/listTemplateTypes`
- Request body: `InfoCodeConfigTemplateTypeQueryParam`
- Service: `InfoCodeConfigService#listTemplateTypes(InfoCodeConfigTemplateTypeQueryParam param)`
- DAO query target: `InfoCodeTemplateTypeQueryDaoParam`

### 3. Contracts

- `objectType`: optional `DtpRwdObjectTypeEnum`; keeps single-object-type filtering.
- `objectTypes`: optional `Set<DtpRwdObjectTypeEnum>`; filters template types with an `IN` condition.
- `notQueryObjectCount=true` path must map `objectTypes` directly to `InfoCodeTemplateTypeQueryDaoParam`.
- Object-count query path must map `objectTypes` to `InfoCodeObjectQueryParam`, then to `InfoCodeTemplateTypeQueryDaoParam`.
- If both `objectType` and `objectTypes` are present, keep the resource-layer intersection behavior: equality on `objectType` plus `IN` on `objectTypes`.
- Response body stays `R<List<InfoCodeTemplateVO>>`; do not change response fields for this filter.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| `objectType` only | Existing single-value filter applies. |
| `objectTypes` only | Template type query applies `object_type IN (...)`. |
| Both fields present and overlap | Return records matching both conditions. |
| Both fields present and do not overlap | Return an empty list from existing query behavior. |
| `objectTypes` empty or null | Do not apply the multi-value filter. |

### 5. Good/Base/Bad Cases

- Good: `{ "objectTypes": ["equipment", "space"] }` returns templates whose object type is either equipment or space.
- Base: `{ "objectType": "equipment" }` keeps the existing single-value behavior.
- Bad: adding `objectTypes` only to the controller param but leaving MapStruct mappings ignored makes one query path silently ignore the new filter.

### 6. Tests Required

- Compile check must pass because `InfoCodeConverter` uses `unmappedTargetPolicy = ReportingPolicy.ERROR`.
- Generated mapper or unit test should show:
  - `InfoCodeConfigTemplateTypeQueryParam.objectTypes` maps to `InfoCodeObjectQueryParam.objectTypes`.
  - `InfoCodeConfigTemplateTypeQueryParam.objectTypes` maps to `InfoCodeTemplateTypeQueryDaoParam.objectTypes`.
  - `InfoCodeObjectQueryParam.objectTypes` maps to `InfoCodeTemplateTypeQueryDaoParam.objectTypes`.
- Resource test should assert `objectTypes` produces an `IN` filter when data setup is available.

### 7. Wrong vs Correct

#### Wrong

```java
@Mapping(target = "objectTypes", ignore = true)
InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeConfigTemplateTypeQueryParam source);
```

#### Correct

```java
InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeConfigTemplateTypeQueryParam source);
```

Let MapStruct map same-name collection fields so both direct template queries and object-count queries share the same request contract.

## Scenario: listTemplateTypes excludeClassCodes filter

### 1. Scope / Trigger

- Trigger: changing `/infoCodeConfig/listTemplateTypes` request fields that exclude template types by class code.
- Applies to both `/infoCodeConfig/listTemplateTypes` and the legacy `/equipment/infoCodeConfig/listTemplateTypes`, because both use `InfoCodeConfigTemplateTypeQueryParam`.

### 2. Signatures

- API: `POST /infoCodeConfig/listTemplateTypes`
- Legacy API: `POST /equipment/infoCodeConfig/listTemplateTypes`
- Request body: `InfoCodeConfigTemplateTypeQueryParam`
- New request field: `excludeClassCodes: Set<String>`
- Compatibility alias: `excludeClassCode`
- Service: `InfoCodeConfigService#listTemplateTypes(InfoCodeConfigTemplateTypeQueryParam param)`
- DAO query target: `InfoCodeTemplateTypeQueryDaoParam.excludeClassCodes`

### 3. Contracts

- `excludeClassCodes`: optional set of class codes to exclude from template type results.
- `excludeClassCode`: accepted as a JSON alias for callers that use the singular field name.
- Single-value JSON input is accepted for the collection field.
- `notQueryObjectCount=true` path must map `excludeClassCodes` directly to `InfoCodeTemplateTypeQueryDaoParam`.
- Object-count query path must map `excludeClassCodes` to `InfoCodeObjectQueryParam`, then to `InfoCodeTemplateTypeQueryDaoParam`.
- Resource-layer query applies `class_code NOT IN (...)` only when the collection is non-empty.
- Response body stays `R<List<InfoCodeTemplateVO>>`; do not change response fields for this filter.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| `excludeClassCodes` absent or null | Existing query behavior is unchanged. |
| `excludeClassCodes` empty | Do not apply the exclusion filter. |
| `excludeClassCodes` non-empty | Return template types whose `classCode` is not in the set. |
| `excludeClassCode` provided | Deserialize into `excludeClassCodes` through the alias. |
| Both include filters and `excludeClassCodes` are present | Keep resource-layer intersection behavior: include filters apply and excluded class codes are removed. |

### 5. Good/Base/Bad Cases

- Good: `{ "excludeClassCodes": ["pump", "fan"] }` returns templates whose class code is neither `pump` nor `fan`.
- Base: existing request body without `excludeClassCodes` keeps current behavior.
- Bad: adding `excludeClassCodes` only to the controller param but leaving MapStruct mappings ignored makes one query path silently ignore the new filter.

### 6. Tests Required

- Compile check must pass because `InfoCodeConverter` uses `unmappedTargetPolicy = ReportingPolicy.ERROR`.
- Generated mapper or unit test should show:
  - `InfoCodeConfigTemplateTypeQueryParam.excludeClassCodes` maps to `InfoCodeObjectQueryParam.excludeClassCodes`.
  - `InfoCodeConfigTemplateTypeQueryParam.excludeClassCodes` maps to `InfoCodeTemplateTypeQueryDaoParam.excludeClassCodes`.
  - `InfoCodeObjectQueryParam.excludeClassCodes` maps to `InfoCodeTemplateTypeQueryDaoParam.excludeClassCodes`.
- Resource test should assert `excludeClassCodes` produces a `NOT IN` filter when data setup is available.

### 7. Wrong vs Correct

#### Wrong

```java
@Mapping(target = "excludeClassCodes", ignore = true)
InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeConfigTemplateTypeQueryParam source);
```

#### Correct

```java
InfoCodeTemplateTypeQueryDaoParam toTemplateQueryDaoParam(InfoCodeConfigTemplateTypeQueryParam source);
```

Let MapStruct map same-name collection fields so both direct template queries and object-count queries share the same request contract.

## Scenario: INFO_CODE_CONFIG_EVENT placeholders

### 1. Scope / Trigger

- Trigger: changing the `INFO_CODE_CONFIG_EVENT` SSE event parameter fields that feed InfoCode config/data queries.
- Applies to `InfoCodeConfigEvent.EventParam` and the `InfoCodeConfigByObjectQueryParam` created inside `InfoCodeConfigEvent#getSseEmitterObject`.

### 2. Signatures

- SSE event: `SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT`
- Event param class: `InfoCodeConfigEvent.EventParam`
- Downstream query param: `InfoCodeConfigByObjectQueryParam`

### 3. Contracts

- `placeholders`: optional `Map<String, Object>` on `InfoCodeConfigEvent.EventParam`.
- `InfoCodeConfigEvent` must copy `EventParam.placeholders` to `InfoCodeConfigByObjectQueryParam.placeholders`.
- `InfoCodeConfigServiceImpl` forwards non-empty placeholder maps through `BaseInfoCodeAnalyzeStrategy.PLACEHOLDERS`.
- Placeholder keys and values keep the existing InfoCode query contract: caller values override automatically resolved placeholder sources.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| `placeholders` absent or null | Existing SSE behavior is unchanged. |
| `placeholders` empty | Downstream service treats it as absent. |
| `placeholders` non-empty | Values are available to InfoCode data filling and override auto-resolved placeholder sources. |
| Required event fields missing | Existing `EventParam` validation still rejects the subscription. |

### 5. Good/Base/Bad Cases

- Good: event param includes `{ "placeholders": { "spaceIds": ["s1"] } }`, and the map reaches `InfoCodeConfigByObjectQueryParam`.
- Base: existing event param without `placeholders` still returns config data.
- Bad: adding `placeholders` only to `EventParam` but not calling `queryParam.setPlaceholders(...)` silently drops caller values.

### 6. Tests Required

- Compile check must pass for `common`.
- A focused unit test should assert that `InfoCodeConfigEvent` copies `EventParam.placeholders` into `InfoCodeConfigByObjectQueryParam` when practical to mock `InfoCodeConfigService`.
- Regression coverage should include the absent/null placeholder path to protect existing SSE subscribers.

### 7. Wrong vs Correct

#### Wrong

```java
private @Nullable Map<String, Object> placeholders;
// Missing queryParam.setPlaceholders(eventParam.getPlaceholders());
```

#### Correct

```java
private @Nullable Map<String, Object> placeholders;
queryParam.setPlaceholders(eventParam.getPlaceholders());
```

## Scenario: debugTool queryObjectInfoCodes associated debug-tool type merge

### 1. Scope / Trigger

- Trigger: changing `/debugTool/queryObjectInfoCodes` response fields or the template-type merge behavior used by the debug tool.
- Trigger: changing `/debugTool/listTemplateTypeAndClassCodes` response behavior that hides debug-tool-only running monitor types.
- Applies to debug-tool information point delivery status queries that use `DebugToolObjectInfoCodesQueryParam.templateTypeIds`.
- Use this contract when a running monitor type needs a debug-tool-only associated running monitor type.

### 2. Signatures

- API: `POST /debugTool/queryObjectInfoCodes`
- Request body: `DebugToolObjectInfoCodesQueryParam`
  - `objectId: String`
  - `templateTypeIds: Set<Long>`
- Response body: `R<Collection<DebugToolInfoCodeConfigStatusVO>>`
- Service: `DebugToolService#queryObjectInfoCodes(DebugToolObjectInfoCodesQueryParam param)`
- API: `POST /debugTool/listTemplateTypeAndClassCodes`
- Response body: `R<Collection<DebugToolTemplateTypeAndClassCodeVO>>`
- Enum association: `RunningMonitorTypeEnum#toDebugToolRunningMonitorType()`

### 3. Contracts

- The request still does not accept `runningMonitorType`; the service must infer the queried running monitor type from `templateTypeIds`.
- `RunningMonitorTypeEnum#toDebugToolRunningMonitorType()` returns at most one associated debug-tool running monitor type.
- If the inferred running monitor type has an associated debug-tool running monitor type, merge the original template type IDs with matching debug-tool template type IDs before querying aggregates.
- Matching debug-tool template types must always use `InfoCodeTemplateTypeEnum.DEFAULT`, and stay constrained by the queried object's `classCode` and original object type.
- Response field `infoCodeName` must prefer the data dictionary name from `DtpKnowledgeClientService#mapInfoCodeAndName(String)`; configuration name is only a fallback.
- Response field `showStyle` comes from merging original running monitor type and debug-tool type `InfoCodeConfigModel.showStyle`.
- For duplicate `infoCode` values, merge `showStyle` structurally when both sides are compatible JSON: object fields are merged with debug-tool fields overriding duplicates; array items are merged by stable item key with debug-tool items overriding duplicates.
- If either `showStyle` value is blank, keep the non-blank value; if both values are not mergeable as compatible JSON, use the debug-tool value.
- Existing status fields, values, units, timestamps, precision, and data sources keep their current source and behavior.
- `/debugTool/listTemplateTypeAndClassCodes` must not expose running monitor types returned by any `RunningMonitorTypeEnum#toDebugToolRunningMonitorType()` association.
- Associated debug-tool running monitor types are backend-only carriers for merged information point templates; callers should still query the original running monitor type and let the backend merge data.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| Object ID does not resolve to an object | Return the existing empty list behavior. |
| Queried template type has no associated debug-tool type | Query only the original `templateTypeIds`. |
| Associated debug-tool type exists but has no matching default template for the object class/type | Query only the original `templateTypeIds`. |
| `/debugTool/listTemplateTypeAndClassCodes` has both an original type and its associated debug-tool type configured | Return the original type only; hide the associated debug-tool type. |
| Data dictionary has an info-code name | Return the dictionary name. |
| Data dictionary misses an info-code name | Fall back to configured `InfoCodeConfigModel.infoCodeName`. |
| Original and debug-tool configs both define JSON object `showStyle` for the same `infoCode` | Merge object fields; debug-tool fields override duplicate fields. |
| Original and debug-tool configs both define JSON array `showStyle` for the same `infoCode` | Merge array items by stable key; debug-tool items override duplicate items. |
| Original and debug-tool configs both define incompatible/non-JSON `showStyle` for the same `infoCode` | Return debug-tool type `showStyle`. |
| Debug-tool config has blank `showStyle` and original config has non-blank `showStyle` | Return original type `showStyle`. |

### 5. Good/Base/Bad Cases

- Good: `AIR_CONDITIONING_COLD_SOURCE` maps to `AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL`; querying AC cold source templates returns the union of original and debug-tool information points.
- Good: `/debugTool/listTemplateTypeAndClassCodes` returns `AIR_CONDITIONING_COLD_SOURCE` but does not return `AIR_CONDITIONING_COLD_SOURCE_DEBUG_TOOL`.
- Base: a normal running monitor type with no association returns the same information points as before, plus the response can include `showStyle` when configured.
- Base: a normal running monitor type with no association is returned normally from `/debugTool/listTemplateTypeAndClassCodes`.
- Bad: using only the original `templateTypeIds` after adding an associated debug-tool type silently hides debug-tool-only information points.
- Bad: looking up debug-tool templates with the original template type instead of `InfoCodeTemplateTypeEnum.DEFAULT` can miss debug-tool-only default configs.
- Bad: looking up debug-tool templates without constraining `classCode` and `objectType` can merge unrelated information point configs.

### 6. Tests Required

- Compile check must pass because `DebugToolConverter` uses `unmappedTargetPolicy = ReportingPolicy.ERROR`.
- Regression coverage should assert:
  - associated debug-tool template IDs are added only when the queried template type maps to a debug-tool type;
  - associated debug-tool template lookup always uses `InfoCodeTemplateTypeEnum.DEFAULT`;
  - ordinary types without an association keep the original template ID set;
  - dictionary names override configured names with configured-name fallback;
  - duplicate JSON `showStyle` is structurally merged with debug-tool values winning conflicts;
  - duplicate non-JSON `showStyle` uses the debug-tool type value.
  - `/debugTool/listTemplateTypeAndClassCodes` hides associated debug-tool running monitor types from the returned type list.

### 7. Wrong vs Correct

#### Wrong

```java
param.setTemplateTypeIds(queryParam.getTemplateTypeIds());
```

This ignores the associated debug-tool type when the queried type has one.

#### Correct

```java
RunningMonitorTypeEnum debugToolRunningMonitorType =
        getDebugToolRunningMonitorType(templateTypeModels);
Set<Long> templateTypeIds = listMergedTemplateTypeIds(
        queryParam.getTemplateTypeIds(), templateTypeModels, debugToolRunningMonitorType, classCode);
param.setTemplateTypeIds(templateTypeIds);
```

Merge associated debug-tool templates only after deriving the association from the original template types and constraining the lookup to the queried object class and template dimensions.
