# 室内温度与蓄冷蓄热时段六类适配 — 实施计划

## Step 1: DtpRwdClassCodeEnum 新增静态方法

- 文件：`common/.../model/enumtype/dtprwd/DtpRwdClassCodeEnum.java`
- 新增 `public static List<String> listAirConditionSystemClassCodes()`
- Javadoc（含 `@since v7.260902.0`），方法体：`Arrays.asList(COLD_SYSTEM.value(), HOT_SYSTEM.value(), COLD_HOT_SYSTEM.value(), ACAC_SYSTEM.value(), ACAH_SYSTEM.value(), ACAI_SYSTEM.value())`
- import `java.util.Arrays`、`java.util.List`（已在上次回滚中残留？当前文件头有无 import？若无则加）

## Step 2: queryIndoorTemp 系统枚举扩六类 + 去除屏蔽空间

- 文件：`common/.../service/runanalysis/impl/RunAnalysisServiceImpl.java`
- L591：`param.setClassCode(...)` 改为 `param.setClassCode(DtpRwdClassCodeEnum.listAirConditionSystemClassCodes())`
- L589 注释更新
- **不改**该方法内其余逻辑（Sy2Sp、tdb 规则、空调需求门槛、值计算）。

### Step 2.5: listHasConfigSpaceIds 过滤屏蔽空间

- 文件：`common/.../structure/demand/gateway/DemandConfigGatewayImpl.java`
- `listHasConfigSpaceIds` 中 `queryParam.setFilterShieldedSpaces(true)`（黑名单已有 demand 链路自动排除）
- 值计算确认：第 621-636 行已使用实际有 IOT 值的空间数（valueMap 自然过滤），无需额外改动。

## Step 3: HistoryStorageTaskHandler 系统枚举扩六类

- 文件：`local/meos-control-compute-edge/.../task/HistoryStorageTaskHandler.java`
- L58-L60：替换 `Arrays.asList(COLD_HOT_SYSTEM.value(), COLD_SYSTEM.value(), HOT_SYSTEM.value())` 为 `DtpRwdClassCodeEnum.listAirConditionSystemClassCodes()`
- 添加注释说明；确认 import 无残留（该文件用 `java.util.*` 通配，不影响）。

## Step 4: 编译验证

```bash
$env:JAVA_HOME="D:\Java\LibericaJDK-8"
mvn compile -T 1C -DskipTests -q
# EXIT=0
```

## Step 5: 验证与回归

- `git diff` 确认仅 3 个文件有变更
- 必要时与回滚前 `cdc80a9c9f` 中对应的 3 个片段进行文件级比对，验证有所回滚内容一致（无遗漏或多余改动）

## 提交（按 meos-commit-message 规范）

- 操作类型：`feat`
- 提交信息：`[7.260902.0](UYAI-1323)feat: 室内温度曲线与蓄冷蓄热时段系统范围扩至六类`

## 注意

- 本次不含 @since/this. 新增时需补齐（listAirConditionSystemClassCodes 方法、queryIndoorTemp 注释风格一致）。
- 与 `cdc80a9c9f` 中同位置改动对比验证，确保没有混入 statis 等其他改动。