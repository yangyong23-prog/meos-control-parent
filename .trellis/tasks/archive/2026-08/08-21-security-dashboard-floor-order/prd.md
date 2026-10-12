# PRD: 修复安防Dashboard楼层顺序未按从高到低排列

## 背景

- Bug 号：UYAI-1311
- 现象：安防 dashboard 楼层顺序错误，未按从高到底排列
- 回归来源：性能优化（UYAI-1197）将安防 Dashboard 的 RWD 远程查询替换为 READY 本地缓存读取后，丢失了排序行为

## 根因

- 优化前：`SecurityDashboardQueryContext` 通过 `dtpRwdClientService.listFloor/listBuilding` 查询楼层/建筑，该客户端内部显式按 `floorSequenceId` 倒序（楼层从高到低）、`buildingSequenceId` 升序排序返回
- 优化后：READY 缓存路径改用 `cacheObjectRepository.listByClassCode` 读取 `cache_object` 表（SQL 为 `order by id`），`fillStaticInstances` 再用 `Collectors.groupingBy`（HashMap）组装 `buildingIdAndFloors`，楼层顺序完全丢失

## 需求

在 `SecurityDashboardQueryContext.fillStaticInstances` 的缓存路径（`cachedBuildings != null && cachedFloors != null` 分支）中补齐与 RWD 路径一致的排序：

1. `this.buildings`：按 `buildingSequenceId` 升序（对齐 `DtpRwdClientServiceImpl.listBuilding`）
2. `this.buildingIdAndFloors`：分组后每个建筑下楼层列表按 `floorSequenceId` 倒序，null 值排最后（对齐 `DtpRwdClientServiceImpl.listFloor`）

## 约束

- 只修改 `SecurityDashboardQueryContext.java`，不改动缓存表结构、不动 RWD 路径逻辑
- 排序 Comparator 与 `DtpRwdClientServiceImpl` 保持一致：`Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId, Comparator.nullsLast(Comparator.reverseOrder()))`
- 不引入新依赖

## 验收标准

1. READY 缓存路径下，dashboard 返回的建筑列表按 `buildingSequenceId` 升序
2. READY 缓存路径下，每个建筑下的楼层列表按 `floorSequenceId` 从高到低排列（与 RWD 路径行为一致）
3. 楼层 `floorSequenceId` 为 null 时排到最后，不抛 NPE
4. 非 READY 路径（RWD 查询）行为不变
5. 编译通过
