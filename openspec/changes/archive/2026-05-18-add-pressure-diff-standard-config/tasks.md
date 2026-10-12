## 1. 领域模型扩展（common 模块）

- [x] 1.1 新增 `DemandConfigAirconditioningPressureDiffQualityValueObject` 压差品质值对象（含 `lowerLimitValue`、`upperLimitValue`）
- [x] 1.2 在 `DemandConfigAirconditioningEnvironmentQualityObject` 中新增 `isPressureDiffQualityEnabled` 和 `pressureDiffQuality` 字段
- [x] 1.3 在 `DemandConfigAirconditioningValueObject.fillDefaultConfig()` 中为项目级默认值补充压差开关（默认 false）
- [x] 1.4 在 `InfoCodeEnum` 中新增 `RELATIVE_PRESSURE_DIFF("relativePressureDiffToOtherSpace", ...)` 常量
- [x] 1.5 在 `AirconditioningRunningStandardObjectVo` 中新增 `pressureDiffQuality` 字段
- [x] 1.6 在 `SpecialDemandConfigObjectDetailVo` 中新增 `isPressureDiffQualityEnabled` 和 `pressureDiffQuality` 字段

## 2. 保存分析与校验逻辑（server 模块）

- [x] 2.1 在 `DemandConfigAirConditioningAnalyzeStrategy` 中新增压差开关关闭时的清理逻辑 `clearPressureDiffQualityConfig()`
- [x] 2.2 在保存全局配置时，当 `isPressureDiffQualityEnabled=false` 级联清空所有已配置对象的压差品质数据
- [x] 2.3 新增压差数值校验：范围 [-1000, 1000]、最多一位小数、下限 ≤ 上限
- [x] 2.4 校验失败时返回统一的国际化错误提示

## 3. 运行标准查看（server 模块）

- [x] 3.1 在 `RunningStandardServiceImpl.previewProjectGlobalConfig()` 中补充 `pressureDiffQuality` 的映射逻辑
- [x] 3.2 在 `RunningStandardServiceImpl.previewSpecialDemandConfig()` 中补充 `isPressureDiffQualityEnabled` 和 `pressureDiffQuality` 的映射逻辑
- [x] 3.3 确保开关关闭时压差字段为 null，不返回给前端

## 4. 国际化翻译

- [x] 4.1 新增压差开关名称的国际化 key（如 `key_xxx` = "压差参数配置"）
- [x] 4.2 新增压差下限值、上限值标签的国际化 key
- [x] 4.3 新增压差解释说明文案的国际化 key
- [x] 4.4 新增黑名单"未交付压差数据的空间"选项的国际化 key
- [x] 4.5 更新黑名单提示语的国际化 key（修改为交集逻辑描述）

## 5. 联调与验证

- [ ] 5.1 本地编译通过，确认新增字段的 Jackson 序列化/反序列化兼容性（当前环境无 Maven，需在有 Maven 环境后执行 `mvn compile` 验证）
- [ ] 5.2 验证开关开启时：可正常录入压差上下限，运行标准查看正常返回
- [ ] 5.3 验证开关关闭时：自动清空压差配置，运行标准查看不展示压差参数
- [ ] 5.4 验证黑名单中勾选"未交付压差数据的空间"后，无压差监测规则的空间正确纳入黑名单
- [ ] 5.5 与前端联调确认运行标准查看页面、环境品质配置页面、黑名单配置页面的展示与交互
- [ ] 5.6 确认 `energy-quality-server` 品质服务侧 `queryStandard` 透传数据包含压差字段

## 6. 归档与收尾

- [ ] 6.1 代码审查通过后合并分支
- [ ] 6.2 执行 OpenSpec 变更归档
