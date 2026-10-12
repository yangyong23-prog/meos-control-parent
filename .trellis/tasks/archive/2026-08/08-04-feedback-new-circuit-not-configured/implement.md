# 执行计划：反馈调节空间新增回路置未配置

## 改动清单

### Step 1: 枚举继承分支改造

**文件**: `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/enumeration/lighting/DemandLightingCircuitSettingModeEnum.java`

- `getSettingMode()` 中 `FEEDBACK_ADJUSTMENT → FEEDBACK_ADJUSTMENT` 分支改为返回 `NOT_CONFIGURED`，注释说明"反馈调节不继承：回路级条件下沉后，新增回路无回路级条件，须人为补充"。
- 同步更新方法 Javadoc 行为说明。

### Step 2: configBuild 反馈调节清空重建分支删除

**文件**: `common/src/main/java/com/persagy/apccontrol/basic/structure/demand/strategy/lighting/DemandLightingStrategyImpl.java`

- 删除 `configBuild()` 中 FEEDBACK_ADJUSTMENT 特殊分支（清空 circuitSettings + break），使反馈调节功能类型走通用筛选逻辑。
- 清理因此失去用途的 `settingMode` 局部变量。
- 检查该分支删除后无残留未使用 import。

## 验证

1. 编译：`mvn -pl common -am compile -q -DskipTests`（或 IDE 增量编译，确认无错误）。
2. 静态走查对照验收标准：
   - AC1/AC2：审阅 `configBuild()` 修改后的通用路径——已保存回路保留条件、新增回路 NOT_CONFIGURED。
   - AC3：确认 `getSettingMode()` 其余分支（ALL_OPEN/ALL_CLOSE/PARTIAL_OPEN/NOT_CONFIGURED）未触碰。
   - AC4：确认 `putCircuitIdResults()` 对 NOT_CONFIGURED 取消配置来源的既有逻辑不变。
3. 如 common 模块存在针对 `getSettingMode` 的既有单元测试，运行并确认通过。

## Review Gates

- 改动仅限上述 2 个文件；`merge()`、`DemandConfigLightingHandler` 两处调用点不改代码，仅行为随枚举变化。
- `git diff` 复核：无无关格式化变更。

## Rollback

- `git revert` 本任务提交即可恢复原继承行为，无数据结构/存量数据变更。
