# 设计：需求管理新增厨房排油烟

## 1. 总体方案

新增独立需求大类 `KITCHEN_RANGE_HOOD`，沿用需求管理既有扩展模式，共 4 类改动 + 枚举接线：

1. 枚举接线：`DemandTypeEnum`、`DemandTemplateTypeEnum` 各新增枚举项并补齐映射 switch；
2. 新大类策略：`DemandKitchenRangeHoodStrategyImpl`，空间查询委托运行监控既有实现；
3. 新配置值对象：`DemandConfigKitchenRangeHoodValueObject`（仅排油烟需求时间）；
4. 新配置解析策略：`DemandConfigKitchenRangeHoodAnalyzeStrategy`。

不接入预计算；不改运行监控、数据库与前端。

## 2. 组件与契约

### 2.1 DemandTypeEnum（需求大类枚举）

文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/enumeration/DemandTypeEnum.java`

新增枚举项（`ordered=5`）：

```java
KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a",
        "kitchenRangeHoodDemand", DemandKitchenRangeHoodStrategyImpl.BEAN_NAME, 5);
```

补齐 2 处 switch 映射：

- `toDemandTemplateTypes()`：`case KITCHEN_RANGE_HOOD -> Lists.newArrayList(DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD)`
- `toRunningMonitorTypeEnum()`：`case KITCHEN_RANGE_HOOD -> RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD`

约束：`demandStrategy()` 经 `getBeanName()` 从 Spring 取新策略；`getReasonPhrase()` 经 i18n key 取"厨房油烟机"文案。

### 2.2 DemandTemplateTypeEnum（模板类型枚举）

文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/enumeration/DemandTemplateTypeEnum.java`

新增枚举项（放在通风需求区段之后、新增大类区段）：

```java
KITCHEN_RANGE_HOOD("KITCHEN_RANGE_HOOD", "key_69d5825940c14669b2fd0621f24a8f7a",
        DemandConfigKitchenRangeHoodAnalyzeStrategy.BEAN_NAME);
```

补齐 2 处 switch 映射：

- `toDemandTypeEnum()`：`case KITCHEN_RANGE_HOOD -> DemandTypeEnum.KITCHEN_RANGE_HOOD`
- `toRunningMonitorSecondTypeEnum()`：`case KITCHEN_RANGE_HOOD -> RunningMonitorSecondTypeEnum.KITCHEN_RANGE_HOOD`（该二级枚举值已存在，`RunningMonitorSecondTypeEnum.java#L146`）

### 2.3 新大类策略 DemandKitchenRangeHoodStrategyImpl

新文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/kitchen/DemandKitchenRangeHoodStrategyImpl.java`

与 `DemandVentilationStrategyImpl` 同构（委托模式）：

```java
@Service(DemandKitchenRangeHoodStrategyImpl.BEAN_NAME)
@Validated
public class DemandKitchenRangeHoodStrategyImpl
        extends AbstractDemandStrategyProcessor
        implements PriorityOrdered {

    public static final String BEAN_NAME = "demandKitchenRangeHoodStrategyImpl";

    @Override
    public List<DtpRwdSpaceDTO> listDemandSpace(@NonNull DemandSpacesQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = templateType.toRunningMonitorSecondTypeEnum();
        RunningMonitorSpaceQueryParam spaceQueryParam = param.toRunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        return runningMonitorSecondType.toRunningMonitorTypeEnum().getRunningMonitorStrategy()
                .listSpaces(spaceQueryParam);
    }

    @Override
    public DemandTypeEnum demandType() {
        return DemandTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public String logTag() {
        return "需求管理-厨房排油烟需求策略";
    }
}
```

- 继承 `AbstractDemandStrategyProcessor`：自动获得 `getTemplateAndCheckThrow`、`listTemplateTypes`（有需求空间才返回）、`listDemandSpaceByInstance`、实例自定义标签（基类空实现）等。
- 空间查询复用 `RunningMonitorKitChenRangeHoodStrategy#listSpaces`（ACKLSF + fanPos=1 → 服务空间关系 → needExaustSmoke=1），无重复查询逻辑。
- 命名拼写说明：新代码使用规范拼写 `Kitchen`（与 saas 层 `KitchenRangeHoodServiceImpl` 一致）；common 层历史类名 `RunningMonitorKitChenRangeHoodStrategy` 保留不动。

### 2.4 配置值对象 DemandConfigKitchenRangeHoodValueObject

新文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/kitchen/DemandConfigKitchenRangeHoodValueObject.java`

```java
@Data
@FieldNameConstants
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigKitchenRangeHoodValueObject extends BaseDemandConfig implements ValueObject {

    /** 排油烟需求时间：在设置时段内自动运行排油烟风机 */
    private List<DemandTimeIntervalValueObject> exhaustTimeIntervals;

    @JsonSetter(Fields.exhaustTimeIntervals)
    public void deserializeExhaustTimeIntervals(Collection<Map<String, Object>> exhaustTimeIntervals) {
        this.exhaustTimeIntervals = readValueFromMap(exhaustTimeIntervals, DemandTimeIntervalValueObject.class);
    }
}
```

- 直接继承 `BaseDemandConfig`（不继承通风基类）：仅保留需求时间字段，无送风/反馈调节字段。
- 字段名 `exhaustTimeIntervals` 与通风同名，前端可复用时间区间配置组件。
- 不覆写 `fillDefaultConfig`：无默认值（默认空配置）。
- `readValueFromMap` 由 `BaseDemandConfig` 提供（`BaseDemandConfig.java#L97-L101`）。

### 2.5 配置解析策略 DemandConfigKitchenRangeHoodAnalyzeStrategy

新文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/config/kitchen/DemandConfigKitchenRangeHoodAnalyzeStrategy.java`

与 `DemandConfigVentilationToiletAnalyzeStrategy` 同构：

```java
@Component(DemandConfigKitchenRangeHoodAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor = @Autowired)
public class DemandConfigKitchenRangeHoodAnalyzeStrategy
        extends AbstractDemandConfigAnalyzeStrategy<DemandConfigKitchenRangeHoodValueObject> {

    public static final String BEAN_NAME = "demandConfigKitchenRangeHoodAnalyzeStrategy";

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public Supplier<DemandConfigKitchenRangeHoodValueObject> emptyConfig() {
        return DemandConfigKitchenRangeHoodValueObject::new;
    }

    @Override
    public Supplier<DemandConfigKitchenRangeHoodValueObject> defaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        return emptyConfig();
    }

    @Override
    protected TypeReference<DemandConfigValueObject<DemandConfigKitchenRangeHoodValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigKitchenRangeHoodValueObject>>() {};
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-厨房排油烟";
    }
}
```

- `defaultConfig` 对所有实例层级返回空配置（无默认字段；实现时核对 `DemandConfigAnalyzeStrategy` 接口默认方法契约，若 PROJECT 级有非空约束则返回空对象实例）。
- 不新增 `DemandKitchenRangeHoodProperties`（无默认值来源，YAGNI）。

## 3. 数据流

| 链路 | 路径 | 说明 |
| --- | --- | --- |
| 需求类型列表 | `/demand/template/listTemplateType` → `DemandCalendarQueryServiceImpl#listTemplateType` → 新策略 `listTemplateTypes()` → `listDemandSpace()` | 有需求空间才返回新大类 |
| 需求空间树 | `DemandQueryServiceImpl` → 新策略 `listDemandSpace` → `RunningMonitorKitChenRangeHoodStrategy#listSpaces` | 黑名单/屏蔽由查询服务层统一后置过滤 |
| 配置读写 | 通用配置网关 → `DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD.demandConfigAnalyzeStrategy()` → 新解析策略 | 保存/查询/继承/复制/清除全走既有框架 |
| 预计算 | `DemandConfigResultRefreshRepositoryImpl#supportsTemplateType` | 返回 false，入口自动跳过（预期行为，非缺陷） |

## 4. 对外契约（供前端/外部对接）

- `listTemplateType` 响应新增组：

```json
{
  "demandType": "KITCHEN_RANGE_HOOD",
  "productCode": "kitchenRangeHoodDemand",
  "templateTypes": [{ "value": "KITCHEN_RANGE_HOOD", "reasonPhrase": "厨房油烟机" }]
}
```

- 配置 JSON 结构（`demand_config.configData`）：

```json
{
  "exhaustTimeIntervals": [
    { "beginTime": { "...": "..." }, "endTime": { "...": "..." }, "acrossDay": false }
  ]
}
```

（时间值对象结构复用 `DemandTimeValueObject`/`DemandTimeIntervalValueObject` 既有契约。）

## 5. 兼容性、影响面与验证锚点

- 所有涉及新枚举的 switch 均补齐 case（`toDemandTemplateTypes`、`toRunningMonitorTypeEnum`、`toDemandTypeEnum`、`toRunningMonitorSecondTypeEnum`），避免 default-throw 异常。
- 已知影响面排查点（实现后全库搜索核对）：
  - `switch (demandType)` / `switch (templateType)` 其他散落分支（含 saas、local 模块）；
  - `DemandTypeEnum.values()` 遍历消费方（如 `DemandCalendarQueryServiceImpl#listTemplateType` 自动包含新大类，符合预期）；
  - 预计算入口（`supportsTemplateType=false` 自动跳过，需确认无强依赖）；
  - 运行日志/操作日志枚举（无需新增，配置保存走通用模板类型字段）。
- 无 DDL；无 Nacos 配置新增；无 i18n 资源新增。
- 编译验证：`common` 模块 + 依赖模块（saas、local）。

## 6. 权衡与备选

| 决策点 | 选择 | 备选 | 理由 |
| --- | --- | --- | --- |
| 空间查询实现 | 委托 `RunningMonitorKitChenRangeHoodStrategy` | 在需求策略内复制查询逻辑 | 单一口径；后续口径调整自动同步 |
| 配置模型 | 新建独立值对象，直接继承 `BaseDemandConfig` | 继承 `BaseDemandConfigVentilationValueObject` | 后者带送风/反馈调节字段，与 D2 决策不符 |
| 枚举拼写 | 新代码 `KitchenRangeHood` | 跟随 common 层 `KitChen` 历史拼写 | 规范拼写；与 saas 层一致；枚举值字符串不受影响 |
| 预计算 | 不接入 | `SUPPORTED_DEMAND_TYPES` 加一行 | 用户决策；后续接入成本仅一行 |

## 7. 回滚

纯新增代码 + 枚举补 case。回滚方式：revert 本次提交；无数据迁移、无存量数据影响（新枚举未被引用前无副作用）。
