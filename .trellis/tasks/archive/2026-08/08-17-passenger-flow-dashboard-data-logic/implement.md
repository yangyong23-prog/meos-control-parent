# 实施计划：客流 dashboard 数据取值逻辑

## 改动文件

| # | 文件 | 改动类型 |
|---|------|----------|
| 1 | `common/.../passengerflow/impl/PassengerFlowServiceImpl.java` | 修改 |

## 实施步骤

### Step 1: 提取规则过滤方法

在 `PassengerFlowServiceImpl` 中新增 private 方法 `filterByPassengerFlowRule`:

```java
/**
 * 按客流信息点实例化规则过滤对象（并集逻辑：任意一个 infoCode 有规则即保留）
 *
 * @param objectIds 待过滤的对象 ID 集合（含项目 ID 和门组 ID）
 * @return 有规则的对象 ID 集合
 */
private Set<String> filterByPassengerFlowRule(Set<String> objectIds) {
    if (CollectionUtils.isEmpty(objectIds)) {
        return Collections.emptySet();
    }
    Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(
            DtpRwdClassCodeEnum.CFDRG.value(), objectIds);
    Map<String, Set<String>> objectIdAndExistsRuleInfoCodes =
            abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                    classCodeAndObjectIds, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
    // 并集过滤：任意一个 infoCode 有规则即保留
    return objectIdAndExistsRuleInfoCodes.entrySet().stream()
            .filter(entry -> CollectionUtils.containsAny(
                    entry.getValue(), CUMULATIVE_PERSON_TIME_INFO_CODE_LIST))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
}
```

### Step 2: 重构 `getPassagewayHeadCount()`

**当前代码（L54-84）**:

```java
public List<PassengerFlowHomeStatistics> getPassagewayHeadCount() {
    String projectId = ContextUtil.getProjectId();
    DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
    dtpRwdObjectQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
    List<DtpRwdEquipmentDTO> gateGroupList = getGateGroupList(dtpRwdObjectQueryParam);
    List<PassengerFlowHomeStatistics> resultStatistics = new LinkedList<>();
    if (CollectionUtils.isNotEmpty(gateGroupList)) {
        Map<String, String> objectIdNameMap = new LinkedHashMap<>();
        objectIdNameMap.put(projectId, LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"));
        objectIdNameMap.putAll(gateGroupList.stream().collect(HashMap::new,
                (map, item) -> map.put(item.getId(), item.getLocalName()),
                HashMap::putAll
        ));
        Set<String> gateGroupId = objectIdNameMap.keySet();
        List<InfoCodeValueVO> infoCodeDayList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                gateGroupId, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
        Map<String, Map<String, Map<LocalDateTime, Object>>> infoCodeHoverMap = this.abstractIotDataCacheServer.queryObjectIdPersistentDataMap(
                DtpRwdPersistenceTimeTypeEnum.HOUR, gateGroupId, GATE_GROUP_INFO_CODE_LIST);
        Map<String, List<InfoCodeValueVO>> infoCodeDayMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(infoCodeDayList)) {
            infoCodeDayMap = infoCodeDayList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
        }
        Map<String, List<InfoCodeValueVO>> finalInfoCodeDayMap = infoCodeDayMap;
        objectIdNameMap.forEach((objectId, objectName) ->
                resultStatistics.add(PassengerFlowHomeStatistics.iotToStatistics(
                        objectId, objectName,
                        finalInfoCodeDayMap.getOrDefault(objectId, new ArrayList<>()),
                        infoCodeHoverMap.getOrDefault(objectId, new HashMap<>(16)))));
    }
    return resultStatistics;
}
```

**目标代码**:

```java
public List<PassengerFlowHomeStatistics> getPassagewayHeadCount() {
    String projectId = ContextUtil.getProjectId();
    DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
    dtpRwdObjectQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
    List<DtpRwdEquipmentDTO> gateGroupList = getGateGroupList(dtpRwdObjectQueryParam);
    List<PassengerFlowHomeStatistics> resultStatistics = new LinkedList<>();

    // 1. 收集所有待检查对象 ID
    Set<String> allObjectIds = new LinkedHashSet<>();
    allObjectIds.add(projectId);
    if (CollectionUtils.isNotEmpty(gateGroupList)) {
        gateGroupList.forEach(item -> allObjectIds.add(item.getId()));
    }

    // 2. 按客流信息点实例化规则过滤
    Set<String> validObjectIds = filterByPassengerFlowRule(allObjectIds);
    boolean projectHasRule = validObjectIds.contains(projectId);
    logProjectInstance().info("客流规则过滤", Arrays.asList(
            Pair.of("过滤前门组数", gateGroupList.size()),
            Pair.of("过滤后有效对象数", validObjectIds.size()),
            Pair.of("项目是否有规则", projectHasRule)
    ));

    if (CollectionUtils.isEmpty(validObjectIds)) {
        return resultStatistics;
    }

    // 3. 构建 objectId → name 映射（项目在首位）
    Map<String, String> objectIdNameMap = new LinkedHashMap<>();
    if (projectHasRule) {
        objectIdNameMap.put(projectId, LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"));
    }
    if (CollectionUtils.isNotEmpty(gateGroupList)) {
        gateGroupList.stream()
                .filter(item -> validObjectIds.contains(item.getId()))
                .forEach(item -> objectIdNameMap.put(item.getId(), item.getLocalName()));
    }

    // 4. 查询 IOT 数据
    Set<String> queryObjectIds = objectIdNameMap.keySet();
    List<InfoCodeValueVO> infoCodeDayList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
            queryObjectIds, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
    Map<String, Map<String, Map<LocalDateTime, Object>>> infoCodeHoverMap = this.abstractIotDataCacheServer.queryObjectIdPersistentDataMap(
            DtpRwdPersistenceTimeTypeEnum.HOUR, queryObjectIds, GATE_GROUP_INFO_CODE_LIST);
    Map<String, List<InfoCodeValueVO>> infoCodeDayMap = new HashMap<>(16);
    if (CollectionUtils.isNotEmpty(infoCodeDayList)) {
        infoCodeDayMap = infoCodeDayList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
    }
    Map<String, List<InfoCodeValueVO>> finalInfoCodeDayMap = infoCodeDayMap;
    objectIdNameMap.forEach((objectId, objectName) ->
            resultStatistics.add(PassengerFlowHomeStatistics.iotToStatistics(
                    objectId, objectName,
                    finalInfoCodeDayMap.getOrDefault(objectId, new ArrayList<>()),
                    infoCodeHoverMap.getOrDefault(objectId, new HashMap<>(16)))));

    return resultStatistics;
}
```

### Step 3: 无需新增 import

现有 `java.util.*` 通配符已覆盖 `LinkedHashSet`、`Collections`、`Map`、`Set` 等所需类型，无需新增 import。

## 验收检查

- [ ] 编译通过（`mvn compile -pl common`）
- [ ] 项目有规则 + 门组有规则：返回项目 + 门组
- [ ] 项目有规则 + 门组无规则：仅返回项目
- [ ] 项目无规则 + 门组有规则：仅返回有规则的门组
- [ ] 全部无规则：返回空列表
- [ ] 门组列表为空 + 项目有规则：返回项目（原有 bug 修复）
- [ ] 项目统计行始终在返回列表首位
- [ ] 边缘端环境同样正确过滤

## 回滚

若需回滚，恢复 `getPassagewayHeadCount()` 原始代码并删除 `filterByPassengerFlowRule()` 方法即可，无数据库变更，无配置变更。