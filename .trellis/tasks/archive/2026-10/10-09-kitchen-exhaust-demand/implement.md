# 执行计划：需求管理新增厨房排油烟

## 实现顺序（按依赖关系）

### Step 1 配置值对象（无依赖）

- [x] 新建 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/kitchen/DemandConfigKitchenRangeHoodValueObject.java`
  - 字段 `List<DemandTimeIntervalValueObject> exhaustTimeIntervals` + `@JsonSetter` 反序列化（参照 `DemandConfigVentilationDomeValueObject` 的写法）
  - 类注释遵循模块 Javadoc 风格（`@author JiYinchuan`、`@since v7.261001.0`）

### Step 2 解析策略（依赖 Step 1）

- [x] 新建 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/config/kitchen/DemandConfigKitchenRangeHoodAnalyzeStrategy.java`
  - `@Component(BEAN_NAME)`、`templateType()`、`inheritConfigType()=FIELD`、`emptyConfig()`、`defaultConfig()`、`typeReference()`、`logTag()`
  - 实现前核对 `DemandConfigAnalyzeStrategy` 接口的 default 方法契约（`defaultConfig` 对 PROJECT 的语义）

### Step 3 大类策略（依赖接线中的命名引用，可独立编译）

- [x] 新建 `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/kitchen/DemandKitchenRangeHoodStrategyImpl.java`
  - 与 `DemandVentilationStrategyImpl` 同构；`listDemandSpace` 委托 `RunningMonitorSecondTypeEnum.KITCHEN_RANGE_HOOD` → `RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD` → `RunningMonitorKitChenRangeHoodStrategy#listSpaces`

### Step 4 枚举接线（依赖 Step 2/3 的类引用）

- [x] `DemandTypeEnum.java`：
  - 新增枚举项 `KITCHEN_RANGE_HOOD(..., "kitchenRangeHoodDemand", DemandKitchenRangeHoodStrategyImpl.BEAN_NAME, 5)`
  - `toDemandTemplateTypes()` 补 case
  - `toRunningMonitorTypeEnum()` 补 case → `RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD`
  - 类 Javadoc 若枚举清单需同步（检查类注释是否列出枚举）
- [x] `DemandTemplateTypeEnum.java`：
  - 新增枚举项 `KITCHEN_RANGE_HOOD(..., DemandConfigKitchenRangeHoodAnalyzeStrategy.BEAN_NAME)`
  - `toDemandTypeEnum()` 补 case
  - `toRunningMonitorSecondTypeEnum()` 补 case

### Step 5 全库影响面核对（编译前后各一次）

- [x] 搜索核对其他散落 switch/分支：
  - `rg "DemandTemplateTypeEnum\.(VENTILATION|AIRCONDITIONING)" --glob "*.java"` 检查是否有按类型枚举的穷举判断需补新值
  - `rg "switch\s*\((demandType|templateType|secondType)" --glob "*.java"` 逐个人工检查
  - 检查 `local/`、`saas/` 模块中是否存在按需求类型/模板类型的分支逻辑
- [x] 确认 `DemandConfigResultRefreshRepositoryImpl` 无需改动（supportsTemplateType=false 预期行为）

### Step 6 编译验证（主会话串行执行）

```bash
# 快速验证 common 模块
mvn -q -pl common -am compile -DskipTests

# 最终验证（common + saas + local 相关模块）
mvn -q compile -DskipTests
```

（实际命令以仓库根 pom 的模块结构为准；若全量编译过长，至少覆盖 `common` 与 `saas/meos-control-common`、`saas/meos-control-server`、`saas/meos-control-compute`。）

### Step 7 移交检查

- [x] `mvn compile` 通过、无新增告警错误
- [x] 搜索核对项全部闭环
- [x] 汇报时列出：新增文件清单、修改文件清单、DO NOT 项（未接预计算、未改运行监控）

## 验证命令

| 项 | 命令/方式 |
| --- | --- |
| 编译 | `mvn -q -pl common -am compile -DskipTests` |
| 全量编译 | `mvn -q compile -DskipTests` |
| 影响面搜索 | `rg` 按 "DemandTemplateTypeEnum"、"DemandTypeEnum" 全库扫 |
| 运行验证（超出本计划） | 由用户/后续联调阶段执行：接口返回、空间树口径、配置读写 |

## 风险文件与回滚点

| 文件 | 风险 | 说明 |
| --- | --- | --- |
| `DemandTypeEnum.java` | 中 | 全工程引用最广的枚举；改动仅增量（新枚举+case），不做重构 |
| `DemandTemplateTypeEnum.java` | 中 | 同上；注意 `toRunningMonitorSecondTypeEnum` default-throw |
| `RunningMonitorKitChenRangeHoodStrategy.java` | 低 | 只读依赖，不改动 |
| 新增 3 个文件 | 低 | 纯新增 |

回滚点：全部改动为一个提交，revert 即恢复；回滚后无脏数据（配置数据未落库前无副作用）。

## 执行记录（2026-10-09，主会话直接执行）

- Step 1-4 全部完成：新增 4 个类（配置值对象、解析策略、策略接口、策略实现），`DemandTypeEnum` / `DemandTemplateTypeEnum` 接线完成（含 4 处 switch case）。
- Step 5 影响面核对完成：散落 switch 均有 default 兜底或属专用链路（通风/照明/SSE/边缘手自动），无需补分支；所有 `DemandSpacesQueryParam` 调用均通过构造器传非空 templateType，无 NPE 路径。
- Step 6 编译验证通过：`mvn -q -pl common -am compile -DskipTests` 与全量 `mvn compile -DskipTests` 均 BUILD SUCCESS（须使用 JDK 8：`D:\Java\LibericaJDK-8`，否则触发 lombok TypeTag 错误）。
- 附加：同步模块设计文档 `docs/design/demand-management/overview.md` 与 `strategy-and-business-types.md`。
- 未做（按 Out of Scope）：预计算接入、运行监控改动、前端、DB 变更。
- 待联调运行验证：AC1-AC6（接口返回、空间口径、配置读写、继承、黑名单/屏蔽、预计算跳过）。

## 完成后汇报口径

- 交付文件：新增 3 个类 + 修改 2 个枚举
- 验收基线：编译通过；接口/空间/配置链路的运行验证列入联调清单（见 prd AC1-AC7）
- 明确未做：预计算接入、运行监控改动、前端、DB 变更
