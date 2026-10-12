# 技术设计：监测Dashboard排油烟需求时间

## 1. 方案总览

新增一个**纯查询接口**：前端传入"空间 + 空间关联的设备"，后端返回两个维度的合并需求时段。数据链路：

```text
前端（监测页面）
  └─ POST /kitchenRangeHood/queryDemandTime
       │  入参: spaces[ { id, relEquipment[ { id } ] } ]
       ▼
KitChenRangeHoodController（现有类 +1 方法）
       ▼
RunningMonitorKitChenRangeHoodStrategy#listDemandTime（策略类并入）
       ├─1. 提取 spaceIds；推导 equipmentId → Set<spaceId>
       ├─2. 查模板: KITCHEN_RANGE_HOOD + 当天 → queryAndGetTemplate()
       ├─3. 批量查空间配置: demandConfigGateway.listConfigsByInstances(SPACE→spaceIds, 含继承)
       ├─4. 组装 spaceId → exhaustTimeIntervals（finalConfig.globalConfig）
       ├─5. 需要时查日出日落（仅当存在 SUNRISE/SUNSET 时段）
       ├─6. 每空间: 解析+合并（DemandTimeIntervalValueObject.merge）→ 回填 space.demandTimes
       └─7. 每设备: 其空间时段汇总+合并 → 回填 relEquipment[].demandTimes
       ▼
出参: 原位回填 —— spaces[ { id, demandTimes[], relEquipment[ { id, demandTimes[] } ] } ]（space 与 equipment 同一 DTO）
```

后端**不查询任何设备-空间关系（Eq2Sp）**；设备与空间的关联完全来自入参。

## 2. 接口契约

### 2.1 接口定义

- 方法：`POST /kitchenRangeHood/queryDemandTime`
- 说明：查询排油烟风机（设备）与服务空间的需求时间（合并去重、按开始时间升序）

### 2.2 入参

```json
{
  "spaces": [
    {
      "id": "sp1",
      "relEquipment": [ { "id": "eq1" }, { "id": "eq2" } ]
    },
    {
      "id": "sp2",
      "relEquipment": [ { "id": "eq1" } ]
    }
  ]
}
```

- **space 与 equipment 共用同一 DTO**（`KitchenRangeHoodDemandTimeDTO`：`id`、`demandTimes`、`relEquipment`）；入参仅传 `id` 与 `relEquipment`（`demandTimes` 由后端回填）。
- `spaces`：空间与其关联设备（排油烟风机）列表；空间为纲。
- 同一设备可出现在多个空间下；同一空间重复传入按一次处理。
- 入参校验：`spaces` `@NotEmpty`、节点 `id` `@NotBlank`（`relEquipment` 级联 `@Valid`）——空入参/空 id 由 Controller 层 `@Valid` 注解校验拦截。

### 2.3 出参

```json
{
  "spaces": [
    {
      "id": "sp1",
      "demandTimes": [ { "beginTime": "07:30", "endTime": "12:00" } ],
      "relEquipment": [
        { "id": "eq1", "demandTimes": [ { "beginTime": "07:30", "endTime": "12:00" } ] },
        { "id": "eq2", "demandTimes": [] }
      ]
    }
  ]
}
```

- **原位回填**：需求时间（`demandTimes`）分别挂到 space 与 equipment（同一 DTO）对象下；响应与入参结构对称、顺序一致。
- 入参出现过的所有空间、设备均返回；无配置/无时段为 `[]`。
- 时段格式 `HH:mm`；结束点支持 `"24:00"`（表示当天结束/次日 0 点，LocalTime 无法表达，用字符串输出）。

## 3. 组件清单（文件级）

| # | 文件 | 类型 | 说明 |
| --- | --- | --- | --- |
| 1 | `common/.../controller/kitchen/KitChenRangeHoodController.java` | 修改 | +1 接口方法 `queryDemandTime` |
| 2 | `common/.../model/dto/kitchen/KitchenRangeHoodDemandTimeDTO.java` | 新建 | **space 与 equipment 共用**：`id`（`@NotBlank`）、`demandTimes`（查询回填）、`relEquipment`（`@Valid` 级联；空间节点的关联设备，元素同类型、递归）；充血方法：`listRelEquipments()`、`fillDemandTimes(...)`、`fillEmptyDemandTimes()` |
| 3 | `common/.../model/param/kitchen/KitchenRangeHoodDemandTimeQueryParam.java` | 新建 | 入参：`List<KitchenRangeHoodDemandTimeDTO> spaces`（`@NotEmpty` + `@Valid` 级联） |
| 4 | `common/.../model/vo/kitchen/KitchenRangeHoodDemandTimeVO.java` | 新建 | 出参：`List<KitchenRangeHoodDemandTimeDTO> spaces`（与入参同构、顺序一致，`demandTimes` 已回填） |
| 5 | `common/.../model/vo/kitchen/KitchenRangeHoodDemandTimeItemVO.java` | 新建 | 时段项：`beginTime`/`endTime`（`HH:mm` 字符串）；+静态工厂 `of(DemandTimeIntervalValueObject)`（结束 00:00 输出 24:00） |
| 6 | `common/.../structure/common/strategy/impl/RunningMonitorKitChenRangeHoodStrategy.java` | 修改 | +方法 `listDemandTime` 及少量私有辅助（服务逻辑并入策略类；新增依赖 `DemandConfigGateway`、`WeatherClientService`；方法命名/排序遵循 count/get/list/add/edit/其他/del 规范；组装行为经 DTO 充血方法完成） |
| 7 | `common/src/test/.../structure/demand/model/valueobject/DemandTimeIntervalValueObjectTest.java` | 新建 | 解析与合并算法单元测试 |
| 8 | `common/.../structure/demand/model/valueobject/DemandTimeIntervalValueObject.java` | 修改 | +`isSunRiseAndSetTime()`、+`toAbsoluteIntervals(...)`、+`merge(...)`、+`mergeIntervals(...)`（解析与归并行为内聚值对象，充血；解析结果复用既有模型承载，无新增载体类） |
| 9 | `common/.../structure/demand/model/valueobject/DemandTimeValueObject.java` | 修改 | +静态工厂 `ofAbsoluteTime(LocalTime)`（绝对时间值对象） |

所有新元素 `@since v7.261001.0`；遵循规范三件套（`this.`/`super.`、jspecify 注解、Javadoc）；模型行为（判断/取值/填充）下沉到 DTO 与值对象（充血模型），策略类不重复定义同类辅助。

## 4. 核心算法：时段解析与合并（值对象内聚）

### 4.1 解析（复用既有契约）

对每个 `DemandTimeIntervalValueObject` 调用 `analyzeTimeInterval(TimeTypeEnum.MINUTE, sunRiseAndSet)`：

- 返回 `null`：区间不完整 / 日出日落类型缺日出日落数据 → **跳过该时段**；
- 返回空列表：结束时间早于开始时间（非跨天无效区间）→ 跳过；
- 返回 1 段（不跨天）或 **2 段（跨天自动拆段：`[begin, 23:59]` + `[00:00, end-1min]`）**；
- 每段为前闭后闭的分钟粒度区间；⚠️ 端点语义不统一（实测）：普通区间端点为配置的真实时间（`07:30-12:00` → `Range[07:30, 12:00]`）；"结束于当天末"（结束为 0 点、跨天第一段）与跨天第二段的结束点被减 1 分钟（`23:59` / `end-1min`）——还原规则见 4.2 第 1 步。

### 4.2 合并（自有实现）

解析产物为**绝对时间区间**（开始/结束均为 `DemandTimeValueObject`，时间点为 `LocalTime`；结束点 `00:00` 表示 24:00，即当天结束）：

1. 端点还原：结束点到达当天末（`23:59`）→ `00:00`（表示 24:00）；跨天拆分第二段结束点（已被减 1 分钟）→ +1 补回；其余端点原样取用；
2. 全部区间按开始时间（`LocalTime` 自然序）升序排序；
3. 扫描归并：`next.begin <= cur.end`（`LocalTime` 比较；`cur.end` 为 `00:00` 时视为当天最晚）→ 合并（结束点取较晚者，`00:00` 视为最晚）；**相接视为连续**（如 `07:30-12:00` + `12:00-13:00` → `07:30-13:00`）；否则输出当前段、开启新段；
4. 输出转换：由 `KitchenRangeHoodDemandTimeItemVO#of(...)` 格式化为 `HH:mm`；结束点为 `00:00` → `"24:00"`。

示例：

| 输入 | 输出 |
| --- | --- |
| `07:30-12:00` + `07:30-12:00` | `07:30-12:00`（去重） |
| `07:30-12:00` + `11:00-13:00` | `07:30-13:00`（重叠归并） |
| `07:30-12:00` + `12:00-13:00` | `07:30-13:00`（相接连续） |
| `09:00-10:00` + `07:30-12:00` | `07:30-12:00`（乱序+包含吸收） |
| `22:00-06:00`（跨天） | `22:00-24:00` + `00:00-06:00`（按解析行为拆段） |

### 4.3 日出日落处理

- 仅当空间时段列表中存在 `SUNRISE/SUNSET` 类型的 begin/end 时，才查询 `weatherClientService.getSunRiseAndSetCache(projectId, today)`（一次，供全部空间复用）；
- 查询结果为 null 时记录 warn 日志，相关时段按解析行为跳过（`analyzeTimeInterval` 内部同样有 warn）。

### 4.4 设备维度二次合并

设备时间 = 其关联空间的**合并后时段**再次扁平化并合并（同一算法复用：绝对时间区间集合直接走 4.2 的排序扫描）。

## 5. 实现要点（RunningMonitorKitChenRangeHoodStrategy#listDemandTime）

1. **入参校验**：`@NotEmpty`（`spaces`）、`@NotBlank`（`id`，`relEquipment` 级联 `@Valid`）在 Controller 层拦截；方法内不重复判空。
2. **模板缺失**：`queryAndGetTemplate()` 为 null（项目无该类型模板）→ 所有 ID 返回 `[]` + warn 日志。
3. **配置查询**：
   ```java
   DemandConfigByInstanceBatchQueryParam param = new DemandConfigByInstanceBatchQueryParam();
   param.setDate(today);
   param.setTemplateId(template.getId());
   param.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, spaceIds));
   List<DemandConfigAggregate<DemandConfigKitchenRangeHoodValueObject>> aggregates = demandConfigGateway.listConfigsByInstances(param);
   ```
   - `queryInheritConfig` 默认 true（含字段级继承）；厨房排油烟需求无黑名单/屏蔽功能，`filterShieldedSpaces` 保持默认 false（不涉及该过滤）；
   - ⚠️ 必须走 `listConfigsByInstances`（实时配置），**不可**走 `listConfigResultsByInstances`（预计算结果表——KITCHEN_RANGE_HOOD 未接入预计算）。
4. **配置提取**：`aggregate.getFinalConfig().getGlobalConfig()`（判空）→ `DemandConfigKitchenRangeHoodValueObject#getExhaustTimeIntervals()`。
5. **结果组装**：需求时间经 DTO 充血方法 `fillDemandTimes(...)` 原位回填到传入的 DTO（space 节点与其 `relEquipment` 下的设备节点）的 `demandTimes`（各节点独立实例）；所有入参空间、设备均返回（无时段 `[]`），顺序与入参一致。

## 6. 关键决策与权衡

| 决策 | 结论 | 理由 / 备选 |
| --- | --- | --- |
| 设备-空间关系来源 | 前端入参（不查 Eq2Sp） | 用户与前端确认；避免与页面范围漂移 |
| 配置数据通道 | 实时 `listConfigsByInstances` | 该类型未接入预计算，结果表无数据；含继承能力 |
| 时段合并实现 | 新写合并行为（内聚 `DemandTimeIntervalValueObject#merge` / `#mergeIntervals`；解析产物复用 `DemandTimeValueObject`/`DemandTimeIntervalValueObject` 承载，结束点 00:00 表示 24:00；取消静态工具类与专用载体类） | 备选 `CalculateComputeUtil#mergeBusinessTime`：输出为平铺 `LocalTime` 无结构、无 `24:00` 表达、含 5 分钟容差（会误合并小间隔）、不处理日出日落；备选 `AbstractLightingService#mergeAndSplitTimeRanges`：面向多来源优先级合并，语义不符 |
| 跨天时段 | 按解析行为拆段展示（`22:00-24:00` + `00:00-06:00`） | 与既有系统（营业时间解析）行为一致；不做"跨天还原"避免与"独立两段"混淆 |
| 黑名单/屏蔽空间 | 不涉及 | 厨房排油烟需求无黑名单和屏蔽功能（用户确认，10-09）；查询参数保持默认值 |
| 出参时间格式 | `String "HH:mm"`（支持 `"24:00"`） | 直接可展示；避免 `LocalTime` 序列化格式与 `24:00` 表达问题 |

## 7. 边界与异常

- 空入参 / 空 `id` → 注解校验拦截（不进入方法体）；
- 空间无配置 / 无 `exhaustTimeIntervals` / 空数组 → `[]`；
- 无效区间（结束早于开始且非跨天）→ 跳过；
- 日出日落数据缺失 → 对应时段跳过（warn）；
- 大量空间/设备：一次批量配置查询 + 内存合并，无 N+1 查询。

## 8. 影响面与回滚

- **纯增量**：1 个 Controller 方法 + 4 个新模型类 + 1 个测试（另：`DemandTimeIntervalValueObject` 新增判断与解析合并方法、`DemandTimeValueObject` 新增工厂方法、i18n zh/en 各 +2 校验 key）；不修改任何既有逻辑、无枚举接线、无缓存、无 DB 变更。
- 影响范围：仅新增接口；现有接口与页面零影响。
- 回滚：还原本次提交即可（无数据/配置副作用）。

## 9. 测试策略

- **单元测试（核心）**：`DemandTimeIntervalValueObjectTest` —— 去重、重叠、相接、包含吸收、乱序排序、跨天拆段、日出日落类型（无数据跳过）、空/null 输入、`24:00` 输出；
- **编译验证**：`mvn -pl common -am compile`（JDK 8）；
- **单测运行**：`mvn -pl common test -Dtest=DemandTimeIntervalValueObjectTest`；
- **联调（AC6，需环境）**：前端传入真实空间/设备，核对风机卡片与空间浮层展示。
