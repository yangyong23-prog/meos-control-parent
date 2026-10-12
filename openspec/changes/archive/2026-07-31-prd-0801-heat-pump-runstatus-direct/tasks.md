## 1. 核心改动

- [x] 1.1 去除边缘端 `ComputeIotDataHandler.java` L169 `updateACAHCInfoValue(source)` 调用，使 ACAHC/ACCCAH 的 runStatus 保持 BDTP 直采原始值
- [x] 1.2 确认 `updateACAHCInfoValue` 方法（L276-291）不再被任何代码调用，保留方法本身作为死代码（降低变更风险）
- [x] 1.3 确认 `checkComputeInfoCodeDataCondition`（L126-129）和 `computeInfoCodeData` 的其他逻辑不受影响，仍正常执行

## 2. 验证

- [x] 2.1 编译通过：边缘端 `meos-control-compute-edge` 模块无编译错误
- [x] 2.2 确认云端 `saas/` 模块无任何改动
- [x] 2.3 确认 `RunningMonitorServerEdge.getGroupObjectMessage` 和 `RunningMonitorServerCloud.getGroupObjectMessage` 未被修改（系统图/关系图路径保留，本迭代不处理）
