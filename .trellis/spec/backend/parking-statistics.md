# Parking Statistics API Contracts

> 停车场项目级统计数据展示契约（v7.260901.0）。

---

## Scenario: 项目级车位统计（规则感知）

### 1. Scope / Trigger

- 接口：`POST /parking/parkingSpace/statistics`（PROJECT / FLOOR 级）
- 需求背景：停车系统对接第三方，只提供项目级空闲车位与车辆总数；空闲车位需按实例化规则感知取值，新增车辆总数展示
- 变更点：空闲车位（VACANT_PARKING_SPACE）规则分支、新增 CAR_TOTAL、无停车空间项目行为

### 2. Signatures

```java
// ParkingService
Map<String, Integer> getParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam param);
// 参数：instanceType = PROJECT / FLOOR，instanceId 对应实例 ID
// 返回：车位类型 → 数量（Integer，可为 null），PROJECT 级额外含 CAR_TOTAL
```

### 3. Contracts

**返回结构**：`Map<String, Integer>`（value 可为 null；无规则不返 key，有规则无 IOT 值返 null）

| key | 类型 | 语义 |
|-----|------|------|
| PARKING_SPACE 等 6 类车位 key | Integer | 统计类，无数据返 0（行为不变） |
| VACANT_PARKING_SPACE | Integer / null / 缺失 | 见下方规则分支 |
| CAR_TOTAL（车辆总数，仅 PROJECT） | Integer / null / 缺失 | 项目对象 `carNum` 有实例化规则才返；无 IOT 值返 null（前端 `--`） |

**空闲车位规则分支**（仅 PROJECT 级）：

| 条件 | 行为 |
|------|------|
| 任一停车位（CFPKPS）有 `parkingStatus` 实例化规则 | 统计 parkingStatus="无车" 数量；全部无 IOT 数据返 0 |
| 所有停车位均无规则 + 项目对象 `availableParkingSpaceNumber` 有规则 | 取项目对象值；无 IOT 值返 null |
| 所有停车位均无规则 + 项目对象也无规则 | 不返回 key |
| 无停车空间项目 | 跳过车位类型聚合（不返 6 类 key），空闲车位/车辆总数仍按项目对象规则判断 |

**FLOOR 级**：保持改造前行为（基于 CFPKPS parkingStatus 聚合），不执行规则逻辑，不返回 CAR_TOTAL。

**规则-取值链路一致性**（硬规范）：
- 规则查询：`AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(classCode→ids, infoCodes)`（部署端自适应）
- 取值：`listInfoCodeValueInOneHour(objectIds, infoCodes)`（60 分钟窗口）
- 项目对象：classCode = `DtpRwdClassCodeEnum.PROJECT`（"project"），objectId = `ContextUtil.getProjectId()`
- 三个信息点均为通用 IOT 信息点，链路必须一致；若新增信息点为持久化类型（IOT_PERSISTENCE 等），须切换 `mapObjectIdAndExistsRuleInfoCodesByCloud` + 持久化取值链路

### 4. Validation & Error Matrix

- 无停车空间 → 不返 6 类车位 key，项目对象路径正常执行（不再返回空 Map 中断）
- 实例化规则查询返回空 → 视为"无规则"，走兜底路径
- IOT 值非数字（NumberFormatException）→ 按无值处理，返回 null 并 warn 日志

### 5. Good/Base/Bad Cases

- Good：CFPKPS 有规则 → `{"PARKING_SPACE": 100, "VACANT_PARKING_SPACE": 30, ..., "CAR_TOTAL": 70}`
- Base：CFPKPS 无规则 + 项目对象 availableParkingSpaceNumber 有规则 → `VACANT_PARKING_SPACE` = 项目对象值
- Bad：CFPKPS 无规则 + 项目对象无规则 → 不返回 `VACANT_PARKING_SPACE` key；`carNum` 无规则 → 不返回 `CAR_TOTAL` key

### 6. Tests Required

`ParkingServiceImplTest`（9 用例，JUnit 5 + Mockito spy）：
- CFPKPS 有规则 → 保留聚合值（含无 IOT 数据返 0 断言 6 key 均在）
- CFPKPS 无规则 + 项目对象规则：有值 / 无值（null）/ 无规则（key 缺失）三态
- carNum 三态：有值 / 无值（null）/ 无规则（key 缺失）
- 无停车空间项目：跳过聚合 + 项目对象路径生效
- FLOOR 级：行为不变 + `mapObjectIdAndExistsRuleInfoCodes` 从未调用（verify never）

### 7. Wrong vs Correct

#### Wrong
```java
// ❌ 过度设计：null 值不需要 Object 泛型，丢失类型安全
Map<String, Object> result = new LinkedHashMap<>();
result.put(CAR_TOTAL, null);
```

#### Correct
```java
// ✅ Map<String, Integer> value 允许 null（HashMap 支持），"有规则无值" 直接放 null
Map<String, Integer> result = new LinkedHashMap<>();
result.put(CAR_TOTAL, this.getProjectInfoCodeValue(InfoCodeEnum.CAR_NUM)); // 返回 Integer（可 null）
```

---

## Common Mistake: 先 initData 再 clear

**Symptom**: 项目级统计先初始化 6 类 key=0，无停车空间时又 clear()，逻辑绕且难读

**Fix**: result 从空 Map 开始，有停车空间才聚合（跳过模式）；FLOOR 分支需要 6 key=0 时在分支内自行 initData

**Prevention**: 该跳过的分支直接跳过，不要"先初始化再清空"；该中断的用 guard clause 提前 return
