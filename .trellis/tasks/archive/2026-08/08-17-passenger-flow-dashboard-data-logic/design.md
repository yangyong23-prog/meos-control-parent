# 设计文档：客流 dashboard 数据取值逻辑

## 1. 概述

在 `PassengerFlowServiceImpl.getPassagewayHeadCount()` 中增加基于 infoCode 实例化规则的过滤，确保 Dashboard 和楼层地图仅展示有规则的门组和项目。

## 2. 数据流

### 2.1 改动前

```
getGateGroupList() → 全部门组
    ↓
if gateGroupList 非空:
    objectIdNameMap = {projectId: "项目"} + 全部门组
    ↓
    query IOT data → 返回统计
else:
    return 空列表
```

**缺陷**: 门组为空时项目也不展示；门组全量展示无过滤。

### 2.2 改动后

```
getGateGroupList() → 全部门组
    ↓
filterByRule(gateGroups, [accmInPeopleFlow, accmOutPeopleFlow]) → 有规则的门组
filterByRule({projectId}, [accmInPeopleFlow, accmOutPeopleFlow]) → 项目是否有规则
    ↓
if 项目无规则 && 门组全过滤:
    return 空列表
    ↓
objectIdNameMap = (项目有规则 ? {projectId: "项目"} : {}) + 有规则的门组
    ↓
query IOT data → 返回统计
```

## 3. 核心设计决策

### 3.1 规则过滤：并集逻辑

| 决策 | 内容 |
|------|------|
| **逻辑** | 任意一个 infoCode（`accmInPeopleFlow` 或 `accmOutPeopleFlow`）有实例化规则即保留 |
| **原因** | PRD 明确"任意一个有实例化规则就展示该统计" |
| **对比** | 现有 `filterInstantiateRuleToIntersection` 使用交集（全部有规则才保留），不适用 |

### 3.2 项目对象 classCode 处理

| 决策 | 内容 |
|------|------|
| **方案** | 使用 `DtpRwdClassCodeEnum.CFDRG.value()` 作为占位 classCode |
| **原因** | `mapObjectIdAndExistsRuleInfoCodes` 的云端/边缘端实现均不依赖 classCode 做实际查询，仅作 Map 分组 key |
| **备选** | 使用任意字符串如 `"PROJECT"`，但用 `CFDRG` 可与门组合并一次查询，减少 RPC 调用 |

### 3.3 门组为空时的项目展示

| 决策 | 内容 |
|------|------|
| **方案** | 移除 `if (CollectionUtils.isNotEmpty(gateGroupList))` 守卫，改为按规则结果判断 |
| **原因** | PRD 明确"门组不交付时项目有总客流数据也可正常显示" |
| **影响** | 当门组为空但项目有规则时，仍需执行 IOT 查询并返回项目统计 |

### 3.4 规则查询合并

将项目对象和门组对象的规则查询合并为一次调用，减少 RPC：

```java
Map<String, Set<String>> classCodeAndObjectIds = new LinkedHashMap<>();
classCodeAndObjectIds.put(DtpRwdClassCodeEnum.CFDRG.value(), allObjectIds); // 项目 + 门组
Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = 
    abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
        classCodeAndObjectIds, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
```

## 4. 接口契约

### 4.1 请求

无变化。`POST /passengerFlow/getPassagewayHeadCount` 无入参，从 `ContextUtil` 获取 projectId。

### 4.2 响应

`List<PassengerFlowHomeStatistics>` 结构不变，条目数可能减少：
- 项目统计行：存在时始终在首位（`LinkedHashMap` 保证顺序）
- 门组统计行：按 `LinkedHashMap` 插入顺序

### 4.3 兼容性

| 维度 | 评估 |
|------|------|
| **API 签名** | 不变 |
| **响应结构** | 不变，仅条目数可能减少 |
| **前端影响** | 无破坏性变更，前端按现有逻辑渲染即可 |
| **边缘端** | `mapObjectIdAndExistsRuleInfoCodes` 边缘端实现走孪生规则，行为一致 |

## 5. 边界情况

| 场景 | 行为 |
|------|------|
| 项目无规则 + 门组全无规则 | 返回空列表 |
| 项目有规则 + 门组全无规则 | 仅返回项目统计行 |
| 项目无规则 + 部分门组有规则 | 仅返回有规则的门组统计行 |
| 项目有规则 + 部分门组有规则 | 返回项目 + 有规则的门组 |
| gateGroupList 为空 + 项目有规则 | 返回项目统计行（修复原有 bug） |
| gateGroupList 为空 + 项目无规则 | 返回空列表 |

## 6. 改动清单

| 文件 | 方法 | 改动 |
|------|------|------|
| `PassengerFlowServiceImpl.java` | `getPassagewayHeadCount()` | 新增规则过滤逻辑，移除门组非空守卫 |
| `PassengerFlowServiceImpl.java` | 新增 private 方法 | 提取规则过滤为独立方法 `filterByPassengerFlowRule()` |

## 7. 日志

新增过滤日志，使用 `logProjectInstance()` 记录：
- 过滤前门组数量
- 过滤后门组数量
- 项目是否有规则