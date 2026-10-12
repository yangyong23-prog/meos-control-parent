# 执行计划：监测Dashboard排油烟需求时间

## 目标摘要

新增 `POST /kitchenRangeHood/queryDemandTime` 接口：前端传入 `spaces`（space 与 equipment 共用同一 DTO：`id`、`relEquipment` 关联设备），后端将合并后的需求时段**原位回填**到 space 与 equipment 对象下（`demandTimes`）；数据源为需求管理厨房排油烟需求配置（KITCHEN_RANGE_HOOD），合并规则=去重+重叠归并+按开始时间排序。

## 前置条件

- 分支：沿用当前工作分支（如需新拉分支，提交阶段确认）。
- 编译环境：JDK 8 —— `$env:JAVA_HOME='D:\Java\LibericaJDK-8'`（系统默认 JDK 与项目 Lombok 不兼容）。
- 交付前必须过**规范三件套**自查（`this.`/`super.`、`org.jspecify.annotations` 注解、`@since`/Javadoc）——上次任务的返工教训。

## 实现顺序

### Step 1 时段解析与合并（值对象内聚，无依赖）

- [x] 修改 `DemandTimeIntervalValueObject`（解析与合并行为内聚值对象，充血；解析结果复用既有模型承载，无新增载体类）
  - `toAbsoluteIntervals(@Nullable WeatherSunRiseAndSetDTO)` → `List<DemandTimeIntervalValueObject>`：`analyzeTimeInterval(TimeTypeEnum.MINUTE, sunRiseAndSet)` 解析 + 端点语义还原（开始/结束均为 ABSOLUTE 绝对时间，结束 00:00 表示 24:00），null/空/无效/脏数据跳过
  - `merge(Collection<DemandTimeIntervalValueObject>, @Nullable WeatherSunRiseAndSetDTO)` → `List<DemandTimeIntervalValueObject>`：集合级解析 + 归并
  - `mergeIntervals(Collection<DemandTimeIntervalValueObject>)` → `List<DemandTimeIntervalValueObject>`：排序扫描归并（`next.begin <= cur.end` 合并，相接视为连续），设备维度二次合并复用
- [x] 修改 `DemandTimeValueObject`：+静态工厂 `ofAbsoluteTime(LocalTime)`
- [x] 修改 `KitchenRangeHoodDemandTimeItemVO`：+静态工厂 `of(DemandTimeIntervalValueObject)`（格式化为 `HH:mm`，结束 00:00 输出 `24:00`）
  - 类/方法 Javadoc + `@since v7.261001.0`

### Step 2 解析与合并单测

- [x] 新建 `common/src/test/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/DemandTimeIntervalValueObjectTest.java`（入口 `DemandTimeIntervalValueObject#merge`）
  - 用例：单条；完全重复去重；重叠归并；相接合并（12:00 端点）；包含吸收；乱序按开始时间排序；跨天拆段（22:00-06:00 → 22:00-24:00 + 00:00-06:00）；结束点 00:00 → "24:00"；空/null 输入；日出日落类型缺数据跳过
  - 参照 `common/src/test` 下现有测试风格（JUnit 5 + 断言风格）

### Step 3 入参/出参模型

- [x] 新建 `common/.../model/dto/kitchen/KitchenRangeHoodDemandTimeDTO.java`（**space 与 equipment 共用**）
  - `String id`、`List<KitchenRangeHoodDemandTimeItemVO> demandTimes`（查询回填）、`List<KitchenRangeHoodDemandTimeDTO> relEquipment`（空间节点的关联设备，元素同类型、递归；设备节点为空）
- [x] 新建 `common/.../model/param/kitchen/KitchenRangeHoodDemandTimeQueryParam.java`
  - `List<KitchenRangeHoodDemandTimeDTO> spaces`
- [x] 新建 `common/.../model/vo/kitchen/KitchenRangeHoodDemandTimeVO.java`
  - `List<KitchenRangeHoodDemandTimeDTO> spaces`（与入参同构，`demandTimes` 已回填）
- [x] 新建 `common/.../model/vo/kitchen/KitchenRangeHoodDemandTimeItemVO.java`
  - `String beginTime`、`String endTime`（`HH:mm`）；+静态工厂 `of(DemandTimeIntervalValueObject)`（结束 00:00 输出 `24:00`）
- [x] `@Schema` + 校验注解（`@NotEmpty`/`@NotBlank`/`@Valid`）、Javadoc、`@since v7.261001.0`（字段级）；模型类仅 `@Data`（不加 `@NoArgsConstructor`/`@AllArgsConstructor`）

### Step 4 服务逻辑并入运行监测策略类

- [x] 修改 `common/.../structure/common/strategy/impl/RunningMonitorKitChenRangeHoodStrategy.java`
  - +方法 `listDemandTime(KitchenRangeHoodDemandTimeQueryParam)` → `KitchenRangeHoodDemandTimeVO`（服务逻辑并入策略类，不单独定义接口/实现）
  - 新增依赖注入：`DemandConfigGateway`、`WeatherClientService`（构造注入 `@RequiredArgsConstructor(onConstructor_ = @Autowired)`）
  - 方法命名与排序遵循 count/get/list/add/edit/其他/del 规范（`listDemandTime` 归入 list 区）；模型行为下沉（充血）：DTO 提供 `listRelEquipments()`/`fillDemandTimes(...)`/`fillEmptyDemandTimes()`，值对象提供 `isSunRiseAndSetTime()`，策略类不重复定义同类辅助
  - 流程（详见 design.md 第 5 节）：入参注解校验（Controller `@Valid`）→ 提取 spaceIds / 推导设备→空间 → 查模板（KITCHEN_RANGE_HOOD + 当天）→ 批量查空间配置（`listConfigsByInstances`，SPACE→spaceIds，含继承）→ 提取 `finalConfig.globalConfig.exhaustTimeIntervals` → （需要时）查日出日落 → 空间合并 → 设备合并 → 组装结果（所有入参 ID 都返回，空为 `[]`）
  - 日志：模板缺失 warn、日出日落缺失 warn（`logProjectInstance()`）

### Step 5 Controller 接口

- [x] 修改 `common/.../controller/kitchen/KitChenRangeHoodController.java`（调用 `RunningMonitorKitChenRangeHoodStrategy#listDemandTime`）
  - `@Operation(summary = "查询-需求时间(风机与服务空间)")`
  - `@PostMapping("/queryDemandTime")`，`R<KitchenRangeHoodDemandTimeVO>` 返回

### Step 6 编译与单测（主会话串行执行）

- [x] `$env:JAVA_HOME = 'D:\Java\LibericaJDK-8'; mvn -pl common -am compile -DskipTests` → BUILD SUCCESS
- [x] `mvn -pl common test -Dtest=DemandTimeIntervalValueObjectTest` → 用例全过（必要时加 `-am` 或调整测试命令）
- [x] 人工核对合并结果与 design.md 第 4.2 节示例表一致

### Step 7 质量与规范核验（交付前必做）

- [x] 规范三件套逐项自查：`this.`/`super.` 前缀；`org.jspecify.annotations.NonNull/Nullable`（禁用 spring/lang）；新类/方法/字段/常量 `@since v7.261001.0` + Javadoc
- [x] 影响面核对：少量增量（新建 5 文件：4 模型+单测；修改：Controller、`RunningMonitorKitChenRangeHoodStrategy`（+`listDemandTime` 及依赖）、`DemandTimeIntervalValueObject`（+判断与解析合并方法）、`DemandTimeValueObject`（+工厂）、i18n zh/en 各 +2 校验 key）；未接预计算（`SUPPORTED_DEMAND_TYPES` 不变）、无既有 switch 需补
- [x] 清理：删除独立 Service 接口/实现（`KitchenRangeHoodDemandTimeService(+Impl)`）
- [x] （IDE 语言服务不可用时以 mvn 编译为准）

### Step 8 文档评估

- [x] `docs/design/` 下无"排油烟监测"功能域模块文档（现有模块：demand-management 等均不匹配本接口）→ 本期不新增文档，避免错建归属；后续该功能域独立沉淀文档时再补

## 验证命令

```powershell
$env:JAVA_HOME = 'D:\Java\LibericaJDK-8'
mvn -pl common -am compile -DskipTests
mvn -pl common test -Dtest=DemandTimeIntervalValueObjectTest
```

## DO NOT

- 不查 Eq2Sp / 不调 `listSpaceServiceByEquipmentId`（设备-空间关系来自入参）
- 其他运行监控策略零改动（仅 `RunningMonitorKitChenRangeHoodStrategy` 增量：+`listDemandTime` 及依赖字段）
- 不接预计算：`SUPPORTED_DEMAND_TYPES` 不变；不用 `listConfigResultsByInstances`（结果表无该类型数据）
- 不引入黑名单/屏蔽过滤逻辑（厨房排油烟需求无此功能；`filterShieldedSpaces` 保持默认 false）
- 不改前端、不改现有接口行为；新增 i18n 校验 key 仅 `key_validation_*` 两条（zh/en 同步）
- 不做数据库结构变更

## 执行记录

- 日期：2026-10-10
- 变更：新建 5 个文件（`KitchenRangeHoodDemandTimeDTO/QueryParam/VO/ItemVO`、单测），修改 `KitChenRangeHoodController`（+1 接口方法）、`DemandTimeIntervalValueObject`（解析与归并行为内聚）、`DemandTimeValueObject`（+绝对时间工厂）与 `RunningMonitorKitChenRangeHoodStrategy`（服务逻辑并入：+`listDemandTime` 及依赖字段）
- 编译：`mvn -pl common -am compile -DskipTests` → BUILD SUCCESS（无新增告警）
- 单测：`mvn -pl common test -Dtest=DemandTimeIntervalValueObjectTest` → 12/12 通过
- 实现要点：`analyzeTimeInterval` 端点语义不统一，值对象内做端点还原（23:59→00:00(24:00)、跨天二段 +1）——见 design.md 4.1/4.2
- 规范核验：三件套达标（jspecify 注解、`this.` 前缀、Javadoc/`@since v7.261001.0`）
- 交互修订（10-10）：服务逻辑并入 `RunningMonitorKitChenRangeHoodStrategy#listDemandTime`（原独立 Service 已删）；入参改注解校验（`@NotEmpty`/`@NotBlank`/`@Valid`），移除方法内判空；模型类移除 `@NoArgsConstructor`/`@AllArgsConstructor`；校验 message 用语义化 key（`key_validation_*`，zh/en 同步新增）；日志调用改 `super.logProjectInstance()`；充血重构（模型行为下沉，策略删 4 个同类辅助；合并工具类与专用载体类取消，解析与归并行为内聚 `DemandTimeIntervalValueObject`，解析产物复用 `DemandTimeValueObject`/`DemandTimeIntervalValueObject` 承载）
- 全量复查（10-10）：修复 `logTag()` 返回 null（改回 `LOG_TAG`，与家族一致，新增 warn 日志 tag 生效）；清理"分钟区间/1440"类过时词汇（DTO Javadoc、测试用例名与 Javadoc、design 4.4/8）；`clean` 全量编译 + 单测 12/12 通过
