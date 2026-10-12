# 数据源与存储

需求管理模块的数据来自需求配置表、模板/日历/场景表、RWD 对象、知识标签、IOT 或外部统计数据，以及 SaaS/local 侧的数据库迁移。

## 资源层

资源层位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/resource/demand
```

主要资源：

| 资源 | 说明 |
| --- | --- |
| `DemandTemplateService` | 需求模板数据访问。 |
| `DemandTemplateCalendarService` | 模板日历数据访问。 |
| `DemandSceneService` | 场景数据访问。 |
| `DemandInstanceConfigService` | 实例配置数据访问。 |
| `DemandConfigService` | 需求配置数据访问。 |
| `DemandConfigGroupService` | 配置分组数据访问。 |
| `DemandBlacklistRuleService` | 黑名单规则数据访问。 |
| `DemandConfigLightingService` | 照明配置数据访问。 |
| `DemandConfigLightingSceneService` | 照明场景数据访问。 |
| `DemandConfigLightingResultService` | 照明计算结果数据访问。 |
| `DemandConfigAirconditioningCustomService` | 空调自定义配置数据访问。 |

DAO 参数位于 `resource/demand/param` 及其子目录。

## 仓储层

结构层仓储位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/structure/demand/repository
```

主要仓储：

| 仓储 | 说明 |
| --- | --- |
| `DemandTemplateRepository` | 模板查询和模板类型索引。 |
| `DemandSceneRepository` | 场景实体查询、保存和删除。 |
| `DemandConfigRepository` | 配置查询、保存、删除和变更事件发布。 |
| `DemandConfigGroupRepository` | 配置分组创建、重命名、复制和删除。 |
| `DemandInstanceRepository` | 实例配置查询、保存、删除。 |

仓储层负责把资源层实体转换成领域实体或聚合根所需的数据形态，Service 不应直接复刻仓储层查询逻辑。

## Mapper 与 XML

Mapper 位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/mapper/demand
common/src/main/resources/mapper
```

需求管理相关 XML 包括：

- `DemandTemplateMapper.xml`
- `DemandTemplateCalendarMapper.xml`
- `DemandSceneMapper.xml`
- `DemandInstanceConfigMapper.xml`
- `DemandConfigMapper.xml`
- `DemandConfigGroupMapper.xml`
- `DemandBlacklistRuleMapper.xml`
- `DemandConfigLightingMapper.xml`
- `DemandConfigLightingSceneMapper.xml`
- `DemandConfigLightingResultMapper.xml`
- `DemandConfigAirconditioningCustomMapper.xml`

新增 SQL 时应按现有 Mapper 位置组织，避免在循环中逐条查询。

## 数据库迁移

需求管理迁移脚本分布在 SaaS 和 local：

```text
saas/meos-control-server/src/main/resources/db/flyway/meos_control
local/meos-control-server-edge/src/main/resources/db/flyway/meos_control
```

相关脚本名称通常包含 `demand`，例如模板、实例组、照明需求、需求触发器和空调/通风历史分区等。

新增表或字段时，需要同时评估 SaaS 与 local 是否都需要迁移脚本。

## 外部数据源

| 数据源 | 使用场景 |
| --- | --- |
| `DtpRwdClientService` | 查询建筑、楼层、空间、对象名称、空间功能类型等 RWD 数据。 |
| `InstanceService` | 查询实例树、父子层级和实例继承链。 |
| 知识数据 | 空间功能类型、自定义标签和值对象中的知识数据源。 |
| IOT/统计数据 | 值选趋势、电量、碳排、当前温度等专项数据。 |
| 外部接口服务 | `/external/demand` 和 `/external/demand/config` 输出使用。 |

## 配置 JSON

`DemandConfig.configData` 以 `JsonNode` 存储。写入时通过 `DemandConfig#fillConfigData` 使用 `ObjectMapper` 转换；读取时由模板类型策略解析成 `DemandConfigValueObject<T>`。

维护规则：

- 不要在 Controller 或普通查询服务中直接假设 JSON 字段结构。
- 新增配置字段时，需要同步更新具体 `BaseDemandConfig` 子类、校验逻辑、默认值和策略解析。
- 字段级继承依赖 `BaseDemandConfig.cacheFieldNames()` 和属性描述符，字段命名和 getter/setter 必须稳定。

## 事件和消息

需求模块存在模板、场景、配置和实例配置变更事件。照明还存在实例组变更监听和计算任务。修改保存、删除、复制流程时，需要检查事件是否会影响：

- 场景清理。
- 配置分组清理。
- 照明计算结果。
- 实例组消息。
- 外部查询结果一致性。
