# 停车场项目级统计展示 - 实施计划

## 实施清单（有序）

1. **InfoCodeEnum 新增枚举**（`common/.../model/base/InfoCodeEnum.java`）
   - `AVAILABLE_PARKING_SPACE_NUMBER("availableParkingSpaceNumber", "key_parking_available_space_number")`
   - `CAR_NUM("carNum", "key_parking_car_num")`
   - 注释仅说明信息点用途（剩余车位数量 / 停车数量，项目对象）

2. **车辆总数 key 常量**
   - 新增独立常量（如 `parking` 包下常量类或复用现有常量类）：`CAR_TOTAL = "CAR_TOTAL"`
   - 不放入 `ParkingSpaceTypeEnum`（避免 `initData()` 遍历污染网格/楼层接口）

3. **ParkingService 接口 + ParkingServiceImpl 实现改造**
   - 签名保持 `Map<String, Integer> getParkingSpaceStatistics`（value 可为 null，无规则不返 key）
   - PROJECT 分支重构：
     - 网格统计为空不再直接 `return new HashMap<>()`，继续执行空闲车位兜底与车辆总数
     - 新增私有方法：获取项目停车位 ids（复用 `getParkingSpaceList(null)` + `getParkingSpaceBySpaceIds`，与网格统计同口径）
     - 空闲车位规则覆盖：`mapObjectIdAndExistsRuleInfoCodes(CFPKPS→ids, [PARKING_STATUS])` 非空 → 保留聚合值；空 → 项目对象 `AVAILABLE_PARKING_SPACE_NUMBER` 取值（无规则不返 key，有规则无值返 null）
     - 车辆总数：`mapObjectIdAndExistsRuleInfoCodes(project→projectId, [CAR_NUM])` 有规则 → `listInfoCodeValueInOneHour` 取值（无值 null）；无规则不返 key
   - 返回 `Map<String, Integer>`（保持 LinkedHashMap 有序，value 可为 null）

4. **ParkingController**：返回类型保持 `R<Map<String, Integer>>`（value 可为 null），无需改动

5. **ParkingStatistics（SSE）**：局部变量保持 `Map<String, Integer>`（value 可为 null），无需改动

## 验证命令

- `mvn -pl common compile`（或项目统一编译命令）验证语法与类型
- 单测（common/src/test）：`ParkingServiceImplTest` 覆盖：
  - CFPKPS 有规则 → VACANT_PARKING_SPACE = 聚合统计值（无 IOT 数据为 0）
  - CFPKPS 无规则 + 项目对象 availableParkingSpaceNumber 有规则有值 → 兜底值
  - CFPKPS 无规则 + availableParkingSpaceNumber 有规则无值 → null
  - CFPKPS 无规则 + availableParkingSpaceNumber 无规则 → 不返 key
  - carNum 有规则有值 / 有规则无值（null）/ 无规则（不返）
  - 无停车空间项目：空闲车位兜底 + 车辆总数仍返回（AC4）
  - FLOOR 级行为不变（AC5）

## 风险文件 / 回滚点

- `ParkingServiceImpl.getParkingSpaceStatistics`（核心改动，回滚点：还原方法体与签名）
- `ParkingService` / `ParkingController` / `ParkingStatistics`（签名联动）
- `InfoCodeEnum`（新增枚举，低风险）

## 提交前检查（task.py start 前）

- [x] prd.md 验收标准全部可测且覆盖（AC1-AC8）
- [x] design.md 已评审（规则-取值链路一致性、无停车空间边界、null 序列化）
- [x] 字典 key 按已有处理，开发不依赖字典版本（联调缺 key 时通知字典相关人员）
- [x] 已确认全局 Jackson null 序列化配置（无 NON_NULL，null 值正常输出）并同步前端契约理解
