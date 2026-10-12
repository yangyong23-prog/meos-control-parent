# 停车场项目级统计展示 - 技术设计

## 1. 架构与边界

- 改动范围（common 模块）：`ParkingServiceImpl` / `ParkingService` / `ParkingController` / `ParkingStatistics`（SSE）/ `InfoCodeEnum` / 车辆总数 key 常量
- 不改动：网格图统计 `getParkingSpaceGridStatistics`、楼层统计 `getFloorParkingSpaceStatistics`、设备统计 `getParkingEquipStatistics`、`fillFloorParkingSpaceStatistic` 内部逻辑（FLOOR 级行为保持现状）
- 新逻辑仅在 `getParkingSpaceStatistics` 的 PROJECT 分支生效

## 2. 数据流与契约

### 2.1 PROJECT 级统计流程（改造后）

```
getParkingSpaceStatistics(PROJECT)
├─ ① 现有聚合：getParkingSpaceGridStatistics → PARKING_SPACE / CHARGING 等 6 类 key
│    （网格无停车空间时为空，不再直接 return 空 Map，继续执行下面步骤）
├─ ② 空闲车位 VACANT_PARKING_SPACE 规则覆盖：
│    mapObjectIdAndExistsRuleInfoCodes(CFPKPS→项目停车位ids, [parkingStatus])
│    ├─ 非空（存在任一停车位有规则）→ 保留 ① 的聚合统计值（=“无车”数；全部无 IOT 数据为 0）
│    └─ 空（所有停车位均无规则）→ 项目对象 availableParkingSpaceNumber 路径：
│         ├─ 项目对象有规则且 IOT 有值 → key = 实际值
│         ├─ 项目对象有规则但 IOT 无值 → key = null（前端 --）
│         └─ 项目对象无规则 → 不返回 key
└─ ③ 车辆总数 CAR_TOTAL：
     mapObjectIdAndExistsRuleInfoCodes(project→projectId, [carNum])
     ├─ 项目对象有规则 → key = IOT 值（无值 → null，前端 --）
     └─ 项目对象无规则 → 不返回 key
```

### 2.2 返回契约

- 返回类型保持 `Map<String, Integer>`（value 可为 null），REST 与 SSE 同步
- key 存在性 = 实例化规则存在性；值：车位统计类 Integer（无数据返 0），点值类 Integer 或 null（前端 --）
- 现有 6 个车位类型 key（PARKING_SPACE / VACANT_PARKING_SPACE / CHARGING_PARKING_SPACE / VACANT_CHARGING_PARKING_SPACE / FAST_CHARGING_PARKING_SPACE / VACANT_FAST_CHARGING_PARKING_SPACE）行为不变
- 车辆总数 key 命名：`CAR_TOTAL`（与现有大写下划线风格一致；不放入 ParkingSpaceTypeEnum，避免 `initData()` 遍历污染网格/楼层接口，使用独立常量类）

### 2.3 契约示例

```json
// CFPKPS 有规则
{ "PARKING_SPACE": 100, "VACANT_PARKING_SPACE": 30, "CHARGING_PARKING_SPACE": 10,
  "VACANT_CHARGING_PARKING_SPACE": 2, "FAST_CHARGING_PARKING_SPACE": 5,
  "VACANT_FAST_CHARGING_PARKING_SPACE": 1, "CAR_TOTAL": 70 }

// CFPKPS 无规则 → 项目对象 availableParkingSpaceNumber 兜底
{ "PARKING_SPACE": 100, "VACANT_PARKING_SPACE": 25, "...": "...", "CAR_TOTAL": 75 }

// CFPKPS 无规则且项目对象 availableParkingSpaceNumber 无规则；carNum 无规则
{ "PARKING_SPACE": 100, "...": "..." }   // 无 VACANT_PARKING_SPACE、无 CAR_TOTAL
```

## 3. 关键实现点

1. **实例化规则查询**：`AbstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, infoCodes)`（部署端自适应：云端持久化规则 / 边缘孪生规则）
   - CFPKPS 的 ids 口径与网格统计一致（停车空间下的 CFPKPS）：复用 `getParkingSpaceList(null)` + `getParkingSpaceBySpaceIds`，在 `getParkingSpaceStatistics` 内新增私有方法获取（独立一次查询，dashboard 场景可接受）
   - 项目对象：classCode = `DtpRwdClassCodeEnum.PROJECT`（"project"），objectId = `ContextUtil.getProjectId()`
2. **IOT 取值**：`AbstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, infoCodes)`（现有 `vacantParkingSpaceList` 同链路）
   - 停车位 + `[PARKING_STATUS]`（现有逻辑复用）
   - projectId + `[AVAILABLE_PARKING_SPACE_NUMBER]` / `[CAR_NUM]`（客流模块有同模式先例）
3. **规则-取值链路一致性**（硬规范，见客流教训）：
   - `parkingStatus` 已确认为"运行参数"（通用 IOT 信息点）→ 规则查 `mapObjectIdAndExistsRuleInfoCodes`，取值 `listInfoCodeValueInOneHour`，链路一致
   - `availableParkingSpaceNumber` / `carNum` 默认按通用 IOT 信息点处理（同一链路）；若字典到位后发现为持久化类型（IOT_PERSISTENCE 等），必须切换 `mapObjectIdAndExistsRuleInfoCodesByCloud` + 持久化取值链路
4. **无停车空间项目（边界）**：网格统计为空时不再直接返回空 Map；空闲车位走"CFPKPS 无规则"兜底（项目对象 availableParkingSpaceNumber），车辆总数独立判断——符合 PRD 项目级统计意图（AC4）
5. **JSON null 序列化**：实现时确认全局 Jackson 配置；若 NON_NULL，null 值 key 被省略，与"无规则不返字段"在响应层面一致（前端按 key 缺失显示 --/不展示的语义由前端契约区分，本迭代以 key 存在性 = 规则存在性为准）

## 4. 兼容性与回滚

- FLOOR 级：逻辑不变，仅返回泛型 Map<String, Object>（值仍为 Integer），现有前端兼容
- SSE `PARKING_STATISTICS`：复用同一服务方法，结构自动同步；`ParkingStatistics.getSseEmitterObject` 内局部变量类型同步改 `Map<String, Object>`
- 回滚：还原 `getParkingSpaceStatistics` 方法体与返回类型即可，无数据迁移、无表结构变更

## 5. 重要权衡

- 不侵入 `getParkingSpaceGridStatistics` / `fillSingleFloorParkingSpaceStatistic`：保证网格图与楼层接口零风险；代价是 PROJECT 级聚合时楼层空闲车位仍会计算（现有行为延续，无新增成本）
- 规则判断的停车位 ids 独立查询一次：与聚合查询重复，但避免修改现有方法签名与内部逻辑，影响面最小
- `CAR_TOTAL` 独立常量而非枚举：避免污染 `SpaceParkIngSpaceStatisticVO.initData()` 的 6 key 初始化（该 VO 被网格/楼层接口复用）
