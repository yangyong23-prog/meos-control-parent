# Running Monitor Strategy Contracts

## Scenario: type strategy dispatch for object range APIs

### 1. Scope / Trigger

- Trigger: adding or changing a `RunningMonitorStrategy` implementation, or changing object ranges returned by `/instance/listSpaces/{type}`, `/instance/listEquipments/{type}`, or `/instance/listDashboardSystems/{type}`.
- Applies to `RunningMonitorTypeEnum`, `RunningMonitorStrategy` implementations, and the `RunningMonitor*QueryParam` object-range filters.

### 2. Signatures

- API: `POST /instance/listEquipments/{runningMonitorType}`
- API: `POST /instance/listSpaces/{runningMonitorType}`
- Dispatch: `RunningMonitorTypeEnum#getRunningMonitorStrategy()`
- Strategy bean: `@Component(<Strategy>.BEAN_NAME)` plus `RunningMonitorTypeEnum.<TYPE>(..., <Strategy>.BEAN_NAME)`

### 3. Contracts

- `RunningMonitorTypeEnum.strategyBeanName` must point to the dedicated strategy bean whenever the type has non-default space, equipment, dashboard, or exception filtering behavior.
- A blank `strategyBeanName` creates `StaticRunningMonitorStrategy`, whose default object-range methods return empty lists.
- Strategy methods should prefer `RunningMonitorSpaceQueryParam`, `RunningMonitorEquipmentQueryParam`, and `RunningMonitorSystemQueryParam`; these parameters already apply common filters such as relation filters, `filterInfoCodeRules`, `filterInfoCodeValueMap`, and `excludeInfoCodeValues`.
- `RunningMonitorSpaceQueryParam` must not declare a `useWhiteList` or other caller-controlled space-range flag. For the air-terminal cache-model consumers, `SPACE_RANGE_TERMINAL` selects the fixed `listWhitelistSpaces(...)` entry or the existing project-full `listSpaces(...)` entry; historical callers of `listSpaces(...)` keep their project-full behavior.
- For simple single-source RWD queries, prefer `DtpRwdObjectQueryParam.orderCustoms` for stable ordering. Use in-memory sorting only after the strategy merges multiple result sources or sorts by computed values that RWD cannot order.
- Keep strategy methods focused on object scope and filtering. Do not send controls, query running logs, or assemble runtime graph state in the strategy layer.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| Type has custom strategy and enum references `BEAN_NAME` | Controller dispatch reaches the custom strategy. |
| Type has custom strategy class but enum leaves `strategyBeanName` blank | Controller dispatch uses `StaticRunningMonitorStrategy`; object-range APIs can return empty lists. |
| Query uses `filterInfoCodeRules` | `BaseObjectQueryParam` removes objects without matching instantiated rule info codes. |
| Query uses `excludeInfoCodeValues` | `BaseObjectQueryParam` removes objects whose recent info-code value matches the excluded value. |
| Strategy needs a simple field order, such as `localName` | Put a `DtpRwdObjectOrderParam` into `DtpRwdObjectQueryParam.orderCustoms`. |

### 5. Good/Base/Bad Cases

- Good: `WATER_SUPPLY(..., RunningMonitorWaterSupplyStrategy.BEAN_NAME)` and the strategy implements `listEquipments` through `param.queryObject(...)` with `orderCustoms` for simple field ordering.
- Base: a type intentionally has no object range behavior and leaves `strategyBeanName` blank, accepting the static empty-list behavior.
- Bad: adding `RunningMonitorWaterSupplyStrategy#listEquipments` while leaving `WATER_SUPPLY(..., null)`, because `/instance/listEquipments/WATER_SUPPLY` still returns the static empty result.

### 6. Tests Required

- Compile `common` after enum or strategy changes.
- For custom object ranges, add or update a focused test when practical to assert:
  - `RunningMonitorTypeEnum.<TYPE>.getRunningMonitorStrategy()` resolves the expected strategy bean;
  - the strategy delegates to `param.queryObject(...)` or the intended custom RWD query;
  - info-code rule/value filters remain supplied through the query parameter rather than duplicated ad hoc.
  - simple field ordering is expressed through `DtpRwdObjectQueryParam.orderCustoms` unless the strategy merges multiple data sources.

### 7. Wrong vs Correct

#### Wrong

```java
WATER_SUPPLY("WATER_SUPPLY", "key_...", null);
```

#### Correct

```java
WATER_SUPPLY("WATER_SUPPLY", "key_...", RunningMonitorWaterSupplyStrategy.BEAN_NAME);
```
