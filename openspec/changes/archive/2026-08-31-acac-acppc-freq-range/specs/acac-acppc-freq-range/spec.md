## ADDED Requirements

### Requirement: ACAC侧ACPPC频率调节范围壳配置枚举定义
系统必须在 `ShellConfigItemEnum` 的 `group_alg_acac_freq` 分组下新增 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 枚举项，定义冷热共用水泵控制组（ACPPC）的频率调节范围参数元数据。

枚举字段值：
- scene: `"alg"`
- group: `"group_alg_acac_freq"`
- tab: `"空调冷源自动需求计算的条件输入"`
- code: `"alg_air_cooling_pump_acppc_freq_range"`
- name: `"频率调节范围"`
- unit: `"Hz"`
- defaultValues: `"40,50"`
- step: `1`（一位小数）
- min: `0`, max: `99`
- hint: `"范围（0-99）"`
- desc: `""`
- valueType: `null`
- valSource: `{"relation":{"classCode":["ACPPC"],"graphCode":"CombineSubset","relCode":"Sy2Eg","relDirection":"from"}}`
- editAuth: `null`

#### Scenario: 新枚举项插入位置正确
- **WHEN** 查询 `ShellConfigItemEnum` 枚举列表
- **THEN** `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 必须位于 `ALG_AIR_COOLING_PUMP_2_FREQ_RANGE`（二次泵组）之后、`ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE`（冷却水泵）之前

#### Scenario: 新项目首次加载回显默认值
- **WHEN** 新项目首次加载壳配置且未保存过 ACPPC 频率调节范围参数
- **THEN** 前端显示的默认值必须为 `"40,50"`

#### Scenario: valSource 关系查询发现 ACPPC 实例
- **WHEN** 查询 `group_alg_acac_freq` 分组的壳配置值
- **THEN** 系统必须通过 valSource 中的 relation（graphCode=CombineSubset, relCode=Sy2Eg, classCode=ACPPC, relDirection=from）从 ACAC 系统出发遍历发现冷热共用水泵控制组实例

### Requirement: ACPPC频率调节范围tab绑定注册
系统必须在 `ShellConfigItemExtendEnum` 中注册 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 的 tab 绑定，tabObjectName 为"冷热共用水泵控制组"。

#### Scenario: 前端查询壳配置时返回正确的三级分类名
- **WHEN** 前端查询 `alg_air_cooling_pump_acppc_freq_range` 参数的 tabObjectName
- **THEN** 返回值必须为"冷热共用水泵控制组"

#### Scenario: tab绑定插入位置正确
- **WHEN** 查询 `ShellConfigItemExtendEnum` 枚举列表
- **THEN** `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 必须位于 `ALG_COOLING_PUMP_2_FREQ_RANGE` 之后

### Requirement: ACPPC实例过滤可见性
`ShellConfigServiceImpl.getInstanceByValSource()` 必须将 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 加入 `GROUP_ALG_ACAC_FREQ` 分组的 else-if 分支，与 ACOPC/ACOTC 枚举在同一分支中直接 add，不检查 netUseType。

#### Scenario: ACPPC实例的配置项对前端可见
- **WHEN** 系统调用 `getInstanceByValSource()` 处理 `GROUP_ALG_ACAC_FREQ` 分组下 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 枚举
- **AND** 查询到 classCode 为 ACPPC 的实例
- **THEN** 该配置项必须被加入 `newShellConfigItems` 列表，前端可见

#### Scenario: 非ACPPC实例不匹配此配置项
- **WHEN** 系统调用 `getInstanceByValSource()` 处理 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 枚举
- **AND** 查询到的实例 classCode 不为 ACPPC
- **THEN** 该配置项不被加入列表（由外层 classCode 匹配 `continue` 跳过）

### Requirement: ACPPC频率调节范围BDTP推送
`InnerSchdule.saveFreqRangeConf()` 必须将 `ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE` 的 code 加入 `freqRangeCodes` 集合，使 5 分钟定时任务自动将值推送至 BDTP。

#### Scenario: ACPPC频率范围值非空时拆分推送
- **WHEN** 定时任务执行且 ACPPC 频率调节范围的 value 为 `"40,50"` 且 objectId 不为空
- **THEN** 生成两条推送记录：`{objectId: 控制组ID, paramCode: "vFDMinFreqCtrl", paramValue: "40", paramLabel: "频率调节范围"}` 和 `{objectId: 控制组ID, paramCode: "vFDMaxFreqCtrl", paramValue: "50", paramLabel: "频率调节范围"}`

#### Scenario: ACPPC频率范围值为空时推送空值
- **WHEN** 定时任务执行且 ACPPC 频率调节范围的 value 为空但 objectId 不为空
- **THEN** 生成两条推送记录：paramValue 均为 `""`
