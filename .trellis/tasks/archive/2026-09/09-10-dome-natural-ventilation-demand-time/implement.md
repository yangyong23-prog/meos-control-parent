# 实施计划：穹顶配置新增「自然通风需求时间」

前置参考：同目录 `prd.md`（需求与验收）、`design.md`（技术设计与决策）。

## 前置条件

- 分支：需求管理迭代分支（当前为 `feat_v7.260802.2_需求管理`）；开始前确认分支与工作区状态符合预期。
- 环境：JDK 8 + Maven（仓库无 mvnw，直接用 `mvn`）；Windows PowerShell；文件读写注意 UTF-8。
- 代码约定：成员访问使用 `this.` 前缀；方法与注释风格对齐目标文件现有写法。

## 实施步骤

### S1 生产代码：穹顶 VO 增量字段

文件：`common/src/main/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/ventilation/DemandConfigVentilationDomeValueObject.java`

1. 类注解增加 `@FieldNameConstants`。
2. 新增字段（置于 `tdbTarget` 之后，属性名即 JSON 契约名 `naturalVentilationTimeIntervals`）：
   - `private List<DemandTimeIntervalValueObject> naturalVentilationTimeIntervals;`
   - Javadoc 文案（对齐 PRD 5.4.1 定义列）：`自然通风需求时间` + `<p>在设置时段内, 穹顶空间存在通过普通窗或排烟窗进行自然通风的需求`。
3. 新增反序列化方法，**逐行镜像**基类 `deserializeExhaustTimeIntervals` 写法（含方法名风格，不加自定义 javadoc，与基类保持最小差异）：
   - 注解 `@JsonSetter(Fields.naturalVentilationTimeIntervals)`；
   - 参数 `Collection<Map<String, Object>> naturalVentilationTimeIntervals`；
   - 方法体 `this.naturalVentilationTimeIntervals = readValueFromMap(naturalVentilationTimeIntervals, DemandTimeIntervalValueObject.class);`。
4. 补 import：`com.fasterxml.jackson.annotation.JsonSetter`、`com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject`、`lombok.experimental.FieldNameConstants`、`java.util.Collection`、`java.util.List`、`java.util.Map`。
5. 明确不改动：基类 VO、其余场景 VO、`fillDefaultConfig`（默认「无」）、`DemandVentilationDefaultSettingProperties`。

### S2 单元测试

新增文件：`common/src/test/java/com/persagy/apccontrol/basic/structure/demand/model/valueobject/ventilation/DemandConfigVentilationDomeValueObjectTest.java`

- 参考风格：`DemandConfigAirConditioningAnalyzeStrategyTest`（JUnit 5 + Mockito，无 Spring 上下文）；测试类名/包名与目标类对应。
- SpringUtil 基座（`readValueFromMap` 依赖 `SpringUtil.getBean(ObjectMapper.class)`）：
  - 参照 `common/src/test/java/com/persagy/apccontrol/basic/structure/cacherwd/repository/CacheInfoCodeTemplateRepositoryImplTest.java#setUpSpringUtil`：保存原 `ApplicationContext`，注册 `StaticApplicationContext` 并注册单例 `ObjectMapper`，`@AfterEach` 恢复。
- 测试 ObjectMapper：`JsonMapper.builder().addModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()`（对齐 Spring Boot 默认，LocalTime 为 `HH:mm:ss` 字符串）。
- 用例清单（对应 design.md §4 测试设计 1~5）：
  1. 完整 JSON（含 `naturalVentilationTimeIntervals` 两段）反序列化 → 字段赋值、顺序保持；
  2. 序列化 round-trip → 输出 JSON 含该字段且数据等价；
  3. 边界：属性缺失 → null；显式 `null` / `[]` → 非 null 空集合；
  4. `anyFieldNotEmpty()`：仅设置该字段为 true；全空 VO 为 false；
  5. 既有字段回归：`tdbTarget` / `participateFeedbackAdjust` 解析不受影响。

### S3 验证命令

```bash
mvn -pl common -am -DskipTests compile
mvn -pl common test -Dtest=DemandConfigVentilationDomeValueObjectTest
```

- 如 `-pl common` 因跨模块依赖失败，改用 `mvn -pl common -am test -Dtest=...` 或 IDEA Maven 面板执行；
- 可选全量回归：`mvn -pl common test`（视时间预算）。

### S4 真实链路联调（需环境，建议用户侧执行）

在测试环境以穹顶通风模板人工验证：保存含「自然通风需求时间」的空间配置 → 配置查询接口回显（含顺序）→ 检查 `demand_config_result.final_config_json` 携带该字段、且空间仅配置该字段时 `is_customized_config` 为真。

## 提交前自检清单

- [ ] `grep naturalVentilationTimeIntervals` 确认仅穹顶 VO + 测试引用，无遗漏的字段白名单/序列化清单（设计调研结论：无此类清单）。
- [ ] 确认未改动基类、默认配置类、其余场景 VO。
- [ ] 测试通过；字段 javadoc 完整。
- [ ] spec 影响评估：`demand-config-result-precompute.md` 无需变更（字段走既有 JSON 管道）；如复查有新发现，移交 finish 阶段（3.3）更新。

## 风险与回滚点

- 风险 1（高）：JSON 契约名与前端约定不一致 → 保存时字段被静默丢弃。缓解：契约名 `naturalVentilationTimeIntervals` 已写入 PRD/设计；联调时如前端另有约定，仅需单点改名。
- 风险 2（低）：复制粘贴错赋值目标（历史上有先例 7ad6f17473）。缓解：S2 用例 1 直接覆盖该字段赋值。
- 回滚点：单提交 revert；无数据/结构变更，已有多余 JSON 属性回滚后被静默忽略。

## 启动前检查（1.4 激活门槛）

- [ ] 用户已批准 planning summary（PRD/design/implement）。
- [ ] `implement.jsonl` / `check.jsonl` 已含真实条目。
- [ ] 经用户批准后由主会话执行 `python ./.trellis/scripts/task.py start`，再进入 Phase 2。
