# 分类与对象查询

运行日志分类既用于日志筛选，也用于日志展示文案和设备列表。新版日志还会根据分类、对象和系统类别构建一二级分类。

## 分类构建

`RunningLogCategoryService#buildLogCategory` 用于构建新版日志的一二级分类。`RunningLog#buildSecondLevelCategory` 会执行具体的二级分类构建：

1. 如果 `firstLevelCategory` 为空，跳过。
2. 如果一级分类配置了 `codeRule`，通过 `RunningLogTemplateConfig#spelMapping` 计算二级分类。
3. 否则根据一级分类 code 和对象 `classCode` 调用 `NowRunningLogCategory#getSecondLevelCategoryByClassCode`。
4. 找到二级分类后写入 `secondLevelCategory`。

## 旧版分类

旧版日志使用 `RunningLogCategoryEnum`，包含一级、二级、三级分类。`RunningLogService#getLogCategoryList` 会调用 `RunningLogCategoryEnum#getCategoryList` 返回分类树。

`RunningLogService#listSecondLevelCategory` 根据一级分类 code 集合，读取旧版枚举的子分类并返回 JSON 列表。

## 新版分类

新版日志使用 `NowRunningLogCategory`。该枚举维护：

- 一级分类集合。
- code 到分类的映射。
- 父分类和系统类别的映射。
- 系统类别到分类的映射。
- 是否属于新版日志系统的判断。

`NowRunningLogService#getSecondLevelRunningLogCategory` 会结合请求一级分类、日志类型和数据库中实际存在的日志，返回有数据的二级分类。

## 分类对象查询策略

分类对象查询策略位于：

```text
common/src/main/java/com/persagy/apccontrol/basic/service/runninglog/category
```

核心类型：

| 类型 | 职责 |
| --- | --- |
| `RunningLogCategoryQueryObjFactory` | 注入所有分类对象查询策略，按对象 ID 和 classCode 汇总系统对象。 |
| `RunningLogCategoryQueryObjStrategy` | 分类对象查询策略接口。 |
| `CategoryQueryObjStrategyAbstract` | 分类对象查询抽象基类。 |
| `ACACQueryObjStrategy`、`ACAIQueryObjStrategy`、`ACCIQueryObjStrategy` | 空调相关分类对象查询。 |
| `KitChenRangeHoodQueryObjStrategy` | 厨房油烟机分类对象查询。 |
| `MdQueryObjStrategy` | MD 相关对象查询。 |
| `SecurityQueryObjStrategy` | 安防分类对象查询。 |
| `VentilationQueryObjStrategy` | 通风分类对象查询。 |

`RunningLogCategoryQueryObjFactory#queryObj` 会遍历所有策略，非空结果都会加入返回集合。新增分类对象查询时，应新增策略实现，而不是在工厂里写类别判断。

## 设备列表

`RunningLogService#equipmentList` 根据参数选择查询路径：

- 带新版一级分类时，走 `adaptiveNowEquipmentList`，从新版日志数据适配设备列表。
- 不带新版一级分类时，走 `RcRunningLogMapper#equipmentList` 查询旧版日志设备列表。

设备列表用于日志筛选，不负责设备主数据维护。

## 维护要点

- 分类是日志查询和展示的核心维度，新增分类要同步旧版枚举、新版枚举、对象查询策略和前端筛选。
- 新版二级分类可能由规则计算，也可能由 classCode 映射得到。
- 分类对象查询策略应保持可插拔，不要在 Controller 或 Service 主流程中硬编码类别分支。
- 设备列表要确认走旧版 mapper 还是新版适配逻辑。
