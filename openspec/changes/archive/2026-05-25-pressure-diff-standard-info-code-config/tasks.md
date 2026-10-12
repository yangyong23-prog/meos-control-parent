## 1. 压差品质映射逻辑

- [x] 1.1 在 `DemandConfigInfoCodeAnalyzeStrategy.mapSpaceIdAndDemandConfig()` 中新增 `spaceIdAndPressureDiffDemandConfigs` map，使用 `InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode()` 作为 key
- [x] 1.2 在遍历需求配置的循环中，增加压差品质判断逻辑：当 `isPressureDiffQualityEnabled` 为 true 且 `pressureDiffQuality` 不为 null 时，将品质值对象放入 map
- [x] 1.3 开关关闭时，不往 `spaceIdAndPressureDiffDemandConfigs` 中放数据，也不往 result map 中放入 `relativePressureDiffToOtherSpace` 键

## 2. 开关关闭时 aggregate 移除

- [x] 2.1 在 `DemandConfigInfoCodeAnalyzeStrategy.fillInfoCodeDataModel()` 中，当压差信息点的 `configByInfoCode` 为 null 时，调用 `aggregate.setInvalid(true)` 标记 aggregate 为无效
- [x] 2.2 验证 `InfoCodeAggregateLifeFilterAware.afterFillInfoCodeDataModel()` 对被标记为 invalid 的压差 aggregate 执行 `iterator.remove()`，确保开关关闭时连定义都不返回

## 3. 验证

- [x] 3.1 编译验证：确保修改后项目可正常编译
- [x] 3.2 逻辑自检：确认开关开启时压差信息点正常展示，开关关闭时完全移除
