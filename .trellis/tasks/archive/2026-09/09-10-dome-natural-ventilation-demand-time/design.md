# 设计：穹顶配置新增「自然通风需求时间」

## 1. 范围与边界

- 已确认范围：PRD-260902 第 5.4 章，仅穹顶配置字段「自然通风需求时间」的后端全链路贯通。
- 明确不做：5.2.3 窗类设备关系纳管、执行端（执行对象/消防优先级）控制逻辑、前端时间段组件行为、服务端时间校验。
- 兼容目标：原有四类通风场景、字段级机制、历史配置行为不变。

## 2. 关键决策

### D1 字段落位

- 落位 `DemandConfigVentilationDomeValueObject`（穹顶专属 VO），**不改**基类 `BaseDemandConfigVentilationValueObject`，避免停车场/机房/卫生间 JSON 契约被动变化（PRD 5.4.1 全量字段表仅穹顶含该字段）。
- 属性名 / JSON 契约名：`naturalVentilationTimeIntervals`，类型 `List<DemandTimeIntervalValueObject>`（复用时间字段组件语义）。命名对齐现有 `exhaustTimeIntervals` / `deliverTimeIntervals`。

### D2 反序列化模式：镜像基类 `@JsonSetter` 模式（选定）

- 原因：保存入口 `DemandConfigAnalyzeStrategy#analyzeConfig` 用 Jackson 将 JSON 反序列化为强类型 VO，**未落到 VO 的字段会被静默丢弃**；基类同名字段使用 `@JsonSetter` + `readValueFromMap`（Map 形态再转换）模式并经生产验证（历史提交 d59bf26a51、7ad6f17473 专门修复过此处解析赋值问题）。
- 备选方案（未选）：纯字段依赖 Lombok setter 绑定（空调/安防 VO 采用），语义更直白（null 保持 null），但与本模板兄弟字段行为不一致，无必要引入差异。
- 实现要点：
  - 类上补 `@FieldNameConstants`（生成 `Fields.naturalVentilationTimeIntervals` 常量，与基类用法对齐）。
  - 已知语义：`readValueFromMap` 对 null/空输入返回**非 null 空 List**——属性缺失时 setter 不被调用（保持 null）；显式传 `null`/`[]` 时落为空集合。此为排风/送风既有行为，本任务沿用不改，未配置字段应省略不传（前端约定）。

```java
/** 自然通风需求时间 */
private List<DemandTimeIntervalValueObject> naturalVentilationTimeIntervals;

@JsonSetter(Fields.naturalVentilationTimeIntervals)
public void deserializeNaturalVentilationTimeIntervals(
        Collection<Map<String, Object>> naturalVentilationTimeIntervals) {
    this.naturalVentilationTimeIntervals = readValueFromMap(
            naturalVentilationTimeIntervals, DemandTimeIntervalValueObject.class);
}
```

### D3 自动生效链路（零额外代码，以验证为主）

| 链路 | 机制 | 代码改动 |
| --- | --- | --- |
| 保存 | `analyzeConfig` 强类型反序列化（必须有 VO 字段） | D2 |
| 查询回显 | `config` / `finalConfig` 由 VO 序列化输出（项目/建筑/楼层/空间一致） | 无 |
| 字段级向上引用 | `DemandConfigAggregateInitByInstanceContext` + `handleFinalConfig()` 反射逐字段合并 | 无 |
| 已配置/特殊配置 | `DemandConfigAggregate#isCustomizedConfig()` → `anyFieldNotEmpty()`（反射） | 无 |
| 一键清除 | 按实例整体清配置 | 无 |
| 复制 | 配置 JSON 整体复制 | 无 |
| 预计算结果 | 通风已接入 `demand_config_result`（`DemandTypeEnum.VENTILATION`），稳定 JSON 自动携带 | 无 |
| 跨零点拆分 | `DemandTimeIntervalValueObject#analyzeTimeInterval`（既有两段 Range 语义） | 无 |

### D4 默认值与兼容

- 项目默认「无」：不修改 `DemandVentilationDefaultSettingProperties`，不加入 `fillDefaultConfig`。
- 老数据：JSON 无该字段 → null → 无需求；无 DB 迁移（`config_data` / `final_config_json` 均为 JSON 列）。
- 不新增服务端时间校验（与排风/送风现状一致；段数上限/重叠校验由前端组件承担）。

## 3. 契约与影响面

- JSON 形态（`DemandTimeIntervalValueObject` 既有结构，不新造格式）：

```json
"naturalVentilationTimeIntervals": [
  { "beginTime": { "timeType": "ABSOLUTE", "absoluteTime": "08:00:00" },
    "endTime":   { "timeType": "ABSOLUTE", "absoluteTime": "12:00:00" },
    "acrossDay": null }
]
```

- 影响面：仅穹顶模板配置 JSON 增加一个可选属性；接口签名、库表结构、其他模板均不变。

## 4. 测试设计

- 新增 `DemandConfigVentilationDomeValueObjectTest`（JUnit 5，无 Spring 上下文）：
  1. 完整 JSON 反序列化（触发 `@JsonSetter` Map 路径）：字段赋值正确、时段顺序保持；
  2. 序列化 round-trip：输出 JSON 含该字段且与输入等价；
  3. 边界：属性缺失 → null；显式 `null` / `[]` → 非 null 空集合（文档化既有语义）；
  4. 状态判断：仅设置该字段时 `anyFieldNotEmpty()` 为 true；全字段为空时为 false；
  5. 回归：既有字段（`tdbTarget` / `participateFeedbackAdjust`）解析不受影响。
- 测试基座注意：`readValueFromMap` 经由 `SpringUtil.getBean(ObjectMapper.class)` 获取 mapper（`BaseDemandConfig.objectMapper` 静态字段从未赋值），需按既有测试模式注册 `StaticApplicationContext` 单例（参考 `CacheInfoCodeTemplateRepositoryImplTest#setUpSpringUtil` 与 `DemandConfigInfoCodeAnalyzeStrategyTest`）；测试 mapper 需含 `JavaTimeModule`（对齐 Spring Boot 默认，LocalTime 按 `HH:mm:ss` 字符串）。
- 可选增强：`DemandConfigVentilationDomeAnalyzeStrategyTest` 走真实 `analyzeConfig` 路径断言字段保留（防「未知字段静默丢弃」回归）。

## 5. 回滚

- 纯增量字段。回滚 = 还原提交；已写入的多余 JSON 属性在回滚后被静默忽略，无数据破坏。
