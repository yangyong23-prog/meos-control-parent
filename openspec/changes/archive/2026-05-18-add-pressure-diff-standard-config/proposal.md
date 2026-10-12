## Why

当前空调运行标准的环境品质目标仅支持温度、CO₂ 和湿度三项参数的配置。随着业务对空间压差监测需求的增加，需要在环境品质目标中新增压差标准配置，并在黑名单 IOT 监测规则中支持按"未交付压差数据的空间"进行筛选，以完善环境品质管理体系。

## What Changes

- **全局配置新增压差开关**：在空调需求全局配置中新增 `isPressureDiffQualityEnabled` 开关，默认关闭，规则同现有湿度开关
- **新增压差品质配置模型**：新增压差品质值对象，支持配置压差下限值和上限值（范围 -1000~1000 Pa，一位小数），不按空调季设置
- **运行标准查看补充压差**：运行标准看板接口在返回温度、CO₂、湿度基础上，补充压差标准的显示（仅在开关开启时）
- **黑名单 IOT 监测新增压差维度**：黑名单"按 IOT 监测"规则中新增"未交付压差数据的空间"选项，信息点编码为 `relativePressureDiffToOtherSpace`
- **外围接口数据透传**：`queryStandard` 品质标准查询接口透传的数据中自然包含新增的压差配置字段
- **国际化翻译**：新增压差相关文案的国际化 key

## Capabilities

### New Capabilities

- `airconditioning-pressure-diff-quality`：空调环境品质目标中的压差标准配置能力，包含开关控制、上下限值录入、运行标准查看展示，以及黑名单 IOT 监测中压差维度的筛选

### Modified Capabilities

- （无现有 spec 需要修改，本次为纯新增能力）

## Impact

- **common 模块**：`DemandConfigAirconditioningEnvironmentQualityObject`、`DemandConfigAirconditioningValueObject` 等核心领域模型新增字段；运行标准相关 VO 新增压差字段；`InfoCodeEnum` 新增压差信息点编码
- **server/saas 模块**：`RunningStandardServiceImpl` 运行标准查询与映射逻辑补充压差数据；`DemandConfigAirConditioningAnalyzeStrategy` 保存分析策略补充压差开关清理逻辑与数值校验
- **前端**：运行标准查看页面、环境品质配置页面、黑名单配置页面需配合调整
- **外围服务**：`energy-quality-server` 品质服务侧需确认 `queryStandard` 响应中压差数据的展示处理
