# 【PRD-290902】移除部分末端管理的设备类 - 执行计划

## 执行顺序

### Step 1: AirTerminalService - 移除 CFCSEW
- 文件: `common/.../service/airterminal/AirTerminalService.java`
- 操作: 从 `ALL_EQUIPMENT_CLASS_CODES` 中删除 `DtpRwdClassCodeEnum.CFCSEW` 行

### Step 2: RunningMonitorAirTerminalStrategy - 移除 queryFFSCAFBySpace 调用
- 文件: `common/.../structure/common/strategy/impl/RunningMonitorAirTerminalStrategy.java`
- 操作: 删除 `queryAllEquipments()` 中的以下逻辑（lines 644-654）：
  ```java
  // 排风兼机械排烟风机
  if (CollectionUtils.isNotEmpty(baseData.sftSpaceIds)) {
      phase2Futures.add(this.runRwdQuery("查询排风兼机械排烟风机", () -> {
          try (PerformanceTracker.ParallelScope ignored = ffscafNode.openScope()) {
              this.queryFFSCAFBySpace(
                      param, baseData.sftSpaceIds, idAndData);
          }
      }));
  } else {
      ffscafNode.finish();
  }
  ```

### Step 3: RunningMonitorAirTerminalStrategy - 移除 ACVTSF ③ 动态归入
- 文件: 同上
- 操作: 删除 `queryACVTSFThreeWay()` 中的 ③ 动态归入逻辑（lines 867-883）

### Step 4: LocalCacheConstant - 移除 CFCSEW
- 文件: `common/.../constant/LocalCacheConstant.java`
- 操作: 从 `AIR_TERMINAL_OBJECT_CLASS` 集合中删除 `"CFCSEW"`
- 注意: ACVTSF 和 FFSCAF 保留不变（它们同时也是通风模块的设备类）

### 编译验证
```bash
cd d:\IdeaProjects\meos-control-parent
mvn compile -pl common -am -q
```

## 验证清单

### 后端验证
- [ ] 编译通过无错误
- [ ] RunningMonitorAirTerminalStrategy 的 queryAllEquipments 不再包含 FFSCAF 挑空查询
- [ ] RunningMonitorAirTerminalStrategy 的 queryACVTSFThreeWay 不再包含③动态归入
- [ ] AirTerminalService.ALL_EQUIPMENT_CLASS_CODES 不再包含 CFCSEW
- [ ] LocalCacheConstant.AIR_TERMINAL_OBJECT_CLASS 不再包含 CFCSEW

## 回滚方案
- 所有改动为代码级变更，通过 git revert 可完整回滚
- 涉及文件：
  1. `AirTerminalService.java` - 1处删除
  2. `RunningMonitorAirTerminalStrategy.java` - 2处删除
  3. `LocalCacheConstant.java` - 1处删除