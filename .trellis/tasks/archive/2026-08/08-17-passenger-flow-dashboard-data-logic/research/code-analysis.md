# 代码分析：客流 dashboard 数据取值逻辑

## 1. CFDRG 门组查询入口

**唯一查询点**: `PassengerFlowServiceImpl.getGateGroupList()`

```java
// PassengerFlowServiceImpl.java:92-95
public List<DtpRwdEquipmentDTO> getGateGroupList(DtpRwdObjectQueryParam param) {
    param.setClassCodeEnum(DtpRwdClassCodeEnum.CFDRG);
    return dtpRwdClientService.listEquipment(param);
}
```

CFDRG 在整个 common 模块中仅被此方法查询，无其他调用点。

**调用链**:
- `PassengerFlowController.getPassagewayHeadCount()` (POST /passengerFlow/getPassagewayHeadCount)
  → `PassengerFlowServiceImpl.getPassagewayHeadCount()`
    → `getGateGroupList()` — 获取所有门组（无过滤）
    → 合并 projectId + 门组 → 查询 IOT 数据 → 返回统计

**结论**: 楼层地图（5.2）的门组数据来源与 Dashboard（5.1）共用同一入口。在 `getPassagewayHeadCount()` 中增加规则过滤即可同时覆盖两个需求。

## 2. 实例化规则查询机制

### 2.1 核心 API

`AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds, infoCodes)`

**云端实现** (`IotDataCacheServerCloud`):
```java
// 遍历所有 objectId × infoCode 组合，调用批量实例化查询
List<ObjectInstantiateQueryParam> queryParams = new ArrayList<>();
for (Set<String> objectIds : classCodeAndObjectIds.values()) {
    for (String objectId : objectIds) {
        for (String infoCode : infoCodes) {
            queryParams.add(ObjectInstantiateQueryParam.batchInstance(objectId, infoCode));
        }
    }
}
return dtpRwdClientService.mapObjectIdAndInstantiateInfoCodes(projectId, queryParams);
```

**边缘端实现** (`IotDataCacheServerEdge`):
```java
// 收集所有 objectId，查询孪生规则
Set<String> allObjectIds = classCodeAndObjectIds.values().stream()
    .flatMap(Collection::stream).collect(Collectors.toSet());
return dtpRwdClientService.listTwinObjectRuleById(queryParam).stream()
    .filter(item -> infoCodes.contains(item.getInfoCode()))
    .collect(Collectors.groupingBy(...));
```

**关键发现**: 云端和边缘端实现均**不依赖 classCode 做查询**，classCode 仅作为 Map 的 grouping key。因此项目对象（无 classCode）可使用任意占位 classCode 进行规则查询。

### 2.2 现有过滤模式对比

| 模式 | 方法 | 逻辑 | 适用场景 |
|------|------|------|----------|
| 并集 | `RunningMonitorRunAnalysisStrategy.listExistsRuleSpaceIdBySystemIds()` | `CollectionUtils.containsAny(entry.getValue(), infoCodeList)` | 任意一个 infoCode 有规则即保留 |
| 交集 | `BaseRunningMonitorStrategy.filterInstantiateRuleToIntersection()` | `v.size() == infoCodes.size()` | 所有 infoCode 都有规则才保留 |

**本需求适用并集逻辑**（任意一个 infoCode 有规则即展示）。

### 2.3 项目对象规则查询

项目对象无 classCode，但 `mapObjectIdAndExistsRuleInfoCodes` 不依赖 classCode 做查询：
- 可构造 `Map.of("CFDRG", Set.of(projectId))` 作为 classCodeAndObjectIds
- 或使用 `Map.of("PROJECT", Set.of(projectId))` 等任意占位 key

## 3. 现有代码改动点

### 3.1 Dashboard + 楼层地图（R1 + R2）

**文件**: `PassengerFlowServiceImpl.java`

**改动位置**: `getPassagewayHeadCount()` 方法

**改动内容**:
1. 获取所有门组后，调用 `mapObjectIdAndExistsRuleInfoCodes` 查询门组的 `accmInPeopleFlow`/`accmOutPeopleFlow` 规则
2. 过滤掉无规则的门组（并集逻辑）
3. 同样查询项目对象的规则，决定是否保留项目统计行

**改动前伪代码**:
```
gateGroupList = getGateGroupList()
objectIdNameMap = {projectId: "项目"} + gateGroupList
query IOT data for all objectIds
return statistics
```

**改动后伪代码**:
```
gateGroupList = getGateGroupList()
filteredGateGroups = filterByInstantiateRule(gateGroupList, [accmInPeopleFlow, accmOutPeopleFlow])
objectIdNameMap = {}
if projectHasRule(projectId, [accmInPeopleFlow, accmOutPeopleFlow]):
    objectIdNameMap.put(projectId, "项目")
objectIdNameMap.putAll(filteredGateGroups)
query IOT data for all objectIds
return statistics
```

### 3.2 值班首页（R3）

**结论**: 无需改动。`PassengerFlowHomePageCardDataHandler.execute()` 仅取项目级 `accmInPeopleFlow`，不依赖门组。

## 4. 涉及的 infoCode

| infoCode | 枚举常量 | 含义 |
|----------|----------|------|
| `accmInPeopleFlow` | `InfoCodeEnum.ACCM_IN_PEOPLE_FLOW` | 累计进入人次 |
| `accmOutPeopleFlow` | `InfoCodeEnum.ACCM_OUT_PEOPLE_FLOW` | 累计离开人次 |

已在 `PassengerFlowService` 接口中定义为:
```java
String ACCM_IN_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCM_IN_PEOPLE_FLOW.value();
String ACCM_OUT_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCM_OUT_PEOPLE_FLOW.value();
List<String> CUMULATIVE_PERSON_TIME_INFO_CODE_LIST = Lists.newArrayList(ACCM_IN_PEOPLE_FLOW_STRING, ACCM_OUT_PEOPLE_FLOW_STRING);
```

## 5. 依赖注入

`PassengerFlowServiceImpl` 已注入:
- `DtpRwdClientService` — RWD 对象查询
- `AbstractIotDataCacheServer` — IOT 数据缓存（含 `mapObjectIdAndExistsRuleInfoCodes`）

无需新增依赖。