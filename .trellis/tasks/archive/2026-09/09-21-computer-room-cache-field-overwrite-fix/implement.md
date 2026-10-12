# 实施计划：机房安防缓存字段并集修复

## 改动总览

| # | 文件 | 动作 |
|---|------|------|
| 1 | `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/projection/CacheObjectExtensionFieldConfig.java` | 新增 `listAllRwdQueryFields()` 全量登记字段并集方法 |
| 2 | `common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/strategy/impl/CacheRwdComputerRoomSecurityStrategy.java` | 设备查询字段由手写清单改为全量并集 + `relObjIds` |
| 3 | `common/src/test/java/com/persagy/apccontrol/basic/structure/cacherwd/projection/CacheObjectExtensionFieldConfigTest.java` | 新增：全量并集契约测试 |
| 4 | `common/src/test/java/com/persagy/apccontrol/basic/structure/cacherwd/strategy/impl/CacheRwdComputerRoomSecurityStrategyWriteBoundaryTest.java` | 新增：写入字段边界测试 |
| 5 | `.trellis/spec/backend/cache-object-model.md` | Phase 3 补充契约（无法枚举 classCode 的混合范围 → 全量并集 + 边界测试矩阵约定） |
| 6 | `common/src/test/java/com/persagy/apccontrol/basic/structure/cacherwd/strategy/impl/CacheRwdVentilationStrategyWriteBoundaryTest.java` | 新增（R6）：通风写入字段边界测试 |
| 7 | `common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModel.java` | 新增（R6）：模板查询返回跨类对象的告警防御 |
| 8 | `common/src/test/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModelCacheWriteFieldsTest.java` | 新增（R6）：模板写入字段边界测试 + 跨类判定用例 |

> 其余写入方（照明/通风/末端/末端白名单/安防/项目结构/模板重建）均已使用 `listRwdQueryFields(classCode)` 完整字段，本任务不改动。

## 步骤 1：配置类新增全量并集方法

位置：`CacheObjectExtensionFieldConfig.java`，紧跟在 `listRwdQueryFields(classCode)` 之后。

```java
/**
 * 获取无法枚举 classCode 的混合设备类写入所需的 RWD 查询字段全量并集。
 *
 * <p>用于按空间/楼层等范围查询"全量设备"且无法限定 classCode 的缓存写入入口。
 * 同一 cache_object 行会被多个缓存 Key 共享，字段子集写入会在 UPSERT 整列覆盖时
 * 丢失其他策略已写入的字段，因此此类入口必须请求全部已登记字段并集，保证覆盖幂等。</p>
 *
 * @return 基础字段、全部对象扩展字段、全部模型视图字段及通用字段的并集，深度不可变
 * @since v7.260802.3
 */
public static @NonNull Set<String> listAllRwdQueryFields() {
    Set<String> merged = new LinkedHashSet<>(CacheObjectExtensionFieldConfig.RWD_QUERY_BASE_FIELDS);
    CacheObjectExtensionFieldConfig.OBJECT_EXTENSION_FIELDS.values().forEach(merged::addAll);
    CacheObjectExtensionFieldConfig.MODEL_VIEW_FIELDS.values().forEach(merged::addAll);
    merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_OBJECT_FIELDS);
    merged.addAll(CacheObjectExtensionFieldConfig.UNIVERSAL_MODEL_VIEW_FIELDS);
    return Collections.unmodifiableSet(merged);
}
```

要点：
- 最后两行显式补 `UNIVERSAL_OBJECT_FIELDS`（major）与 `UNIVERSAL_MODEL_VIEW_FIELDS`（bimId/bimLocation），两份 Map 为空时也兜底成立；
- 该集合不含 `relObjIds` 等"调用方附加字段"，由调用方自行追加；
- 复用同类私有常量，不新增依赖。

## 步骤 2：机房策略改用全量并集

位置：`CacheRwdComputerRoomSecurityStrategy.buildNewCacheData` Step 3（约 203-212 行）。

整体替换原手写清单：

```java
// 机房按所在空间查询楼层全量设备且不限制 classCode：写入字段必须取全量登记并集。
// 同一 cache_object 行会被照明/通风/安防等策略以各自完整字段写入，字段子集会在
// UPSERT 整列覆盖时丢失其他策略字段（LTLTCC 曾只剩 major）；relObjIds 是关系快照
// 必需的调用方附加字段，并集不含，需单独追加。
Set<String> equipmentWithColumns = new LinkedHashSet<>(
        CacheObjectExtensionFieldConfig.listAllRwdQueryFields());
equipmentWithColumns.add(DtpRwdObjectDTO.Fields.relObjIds);
```

覆盖性核对（逐项确认原清单全部被并集包含）：
- `listSimpleWithColumns` {id, localName, objType, classCode} → base 字段；
- `realTimeStreamProtocol` → SPVSCM/WEGMCM 等对象扩展字段；
- `monitoringSurveillanceStatus` → base 字段；
- `major` → UNIVERSAL_OBJECT_FIELDS；
- `bimId/bimLocation` → UNIVERSAL_MODEL_VIEW_FIELDS + 各类模型视图字段；
- `floorId/buildingId` → base 字段；
- `localId`（并集新增）无害：读取端按请求字段交集恢复，不改任何对外契约。

import 清理：`InfoCodeEnum` 仍被 `buildAndWriteIotRules` 使用、`DtpRwdEquipmentDTO` 仍用于设备列表类型，均保留；确认无新增未使用 import（编译门禁）。

## 步骤 3：测试

### 3.1 配置契约测试（新增 `CacheObjectExtensionFieldConfigTest`）

用例 `shouldExposeAllRegisteredFieldsInUnion()`：
- 期望集合 = base 字段（测试内独立手工列出）+ 遍历 `getObjectExtensionFields().values()` + `getModelViewFields().values()` + `getUniversalObjectFields()` + `getUniversalModelViewFields()` 的平铺并集；
- 断言 `listAllRwdQueryFields()` 与期望集合**完全相等**（防漏、防多）；
- 追加断言显式包含 `major`、`bimId`、`bimLocation` 与 LTLTCC 的 `serviceAreaType/funcType/lampFuncType`，防登记变动时静默失效。

### 3.2 机房写入边界测试（新增 `CacheRwdComputerRoomSecurityStrategyWriteBoundaryTest`）

参照 `CacheRwdLightingStrategyWriteBoundaryTest` 结构：
- `@BeforeAll` / `@AfterAll`：`StaticApplicationContext` 注册 `dtpRwdClientService` / `abstractIotDataCacheServer` / `dtpRwdConvert` 三个 Mock 到 Hutool `SpringUtil`（`BaseObjectQueryParam` 构造函数读取），测试后恢复原上下文；
- 构造策略：Mock `RunningMonitorComputerRoomSecurityStrategy`、`DtpRwdClientService`、`CacheObjectModelWriteService`、`CacheObjectModelStatusService`、`AbstractIotDataCacheServer`、`CacheRuleRepository`、`CacheIotRuleRepository`，注入 `new CacheRwdComputerRoomSecurityStrategy(...)`；
- 行为打桩：
  - `listSpacesComputerRoomSecurityCacheSerial(any, anyInt)` → 1 个机房空间（含 floorId/buildingId/roomFuncType）；
  - `dtpRwdClientService.listSpace(any)` → 1 个空间 DTO（单页 < 1000 即终止分页循环）；
  - `listEquipmentsForCacheBuildParallel(any, anyCollection, anyInt)` → 1 个 LTLTCC 设备 DTO；
  - `replaceModelCacheKeysAndRelations(any, any, any, any)` → true；
  - `mapObjectIdAndExistsRuleInfoCodes(any, any)` → 空 Map；`cacheIotRuleType()` → 任一现有枚举值（实现时确认枚举名）；`replaceRules` 为 void 无需打桩；
- 断言（`ArgumentCaptor<Collection<String>>` 捕获 `listEquipmentsForCacheBuildParallel` 第二个入参）：
  - `withColumns.containsAll(CacheObjectExtensionFieldConfig.listAllRwdQueryFields())`；
  - `withColumns` 包含 `serviceAreaType`、`funcType`、`lampFuncType`、`major`、`relObjIds`；
  - `strategy.buildCacheData(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY)` 返回 `true` 且 `markReady` 被调用。

### 3.3 既有测试回归

确认 `SecurityCacheFieldContractTest`、各 `*WriteBoundaryTest`、`CacheObjectRecordConverterTest` 等不受并集超集影响（它们断言白名单子集/投影行为，预期不破坏）。

## 步骤 4：写入方边界测试矩阵补齐（R6 · 方案 A）

### 4.1 通风写入字段边界测试（新增 `CacheRwdVentilationStrategyWriteBoundaryTest`）

- 构造 `new CacheRwdVentilationStrategy(mock(RunningMonitorVentilationStrategy), mock(DtpRwdClientService), mock(CacheObjectModelWriteService))`；
- 打桩：`dtpRwdClientService.listSpace(any())` → 1 个空间（space-1）；`listEquipmentsForCacheBuild(any(), any())` → 1 个 ACVTSF 设备（relObjIds 含 space-1）；`replaceModelCacheKeysAndRelations(any, any, any, any)` → true；
- 断言（ArgumentCaptor）：
  - `listSpace` 的 queryParam.withColumns ⊇ `listRwdQueryFields(SPACE)` + name；
  - `listEquipmentsForCacheBuild` 第二个入参 ⊇ FFSCMF/FFSCAF/ACVTSF/CFCSWN/CFCSEW 五类 `listRwdQueryFields` 并集 + name + relObjIds；
  - `buildCacheData(VENTILATION)` 返回 true。

### 4.2 模板写入字段边界测试（新增 `InfoCodeTemplateTypeModelCacheWriteFieldsTest`）

- 参照 `InfoCodeTemplateTypeModelCacheReadTest` 的静态字段替换模式：用反射把 `dtpRwdClientService`、`CACHE_OBJECT_MODEL_STATUS_SERVICE`（`isSupported`→true）等静态字段替换为 Mock，测试后恢复；
- 构造一个可缓存的模板模型（classCode=LTLTCC、runningMonitorType/templateType 满足 `allowCache()`），mock `dtpRwdClientService.listObject(any())` 返回该 classCode 对象；
- 断言（ArgumentCaptor 捕获 listObject 的 queryParam）：
  - `withColumns` ⊇ `listRwdQueryFields(LTLTCC)`（含 serviceAreaType/funcType/lampFuncType/major 与 bimId/bimLocation）；
  - 用例二（跨类返回）：listObject 返回 classCode ≠ 模板 classCode 的对象，验证查询仍按模板 classCode 组装字段、且跨类判定方法识别该对象（见步骤 5）。

## 步骤 5：模板跨类对象告警防御（R6 · R1）

位置：`InfoCodeTemplateTypeModel.queryTemplateObjects(boolean cacheWrite)` 的 `objectModelCacheWrite` 分支，查询返回后：

```java
if (objectModelCacheWrite) {
    // 防御：字段按模板 classCode 组装；返回其他类对象时其白名单字段可能未请求，暴露配置异常
    List<DtpRwdObjectDTO> crossClassObjects = objects.stream()
            .filter(object -> !StringUtils.equals(this.classCode, object.getClassCode()))
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(crossClassObjects)) {
        LogExecutor.project().warn(LOGGER, LOG_TAG, "模板缓存写入返回非模板classCode对象",
                Lists.newArrayList(
                        Pair.of("templateTypeKey", this.getTemplateTypeKey()),
                        Pair.of("templateClassCode", this.classCode),
                        Pair.of("crossObjectCount", crossClassObjects.size()),
                        Pair.of("sample", crossClassObjects.stream().limit(5)
                                .map(o -> o.getId() + ":" + o.getClassCode()).collect(Collectors.toList()))));
    }
}
```

- 告警按"模板 × 构建批次"聚合为一条（count + 前 5 个样例），避免刷日志；
- `this.classCode` 为空时同样告警（模板未配 classCode 属于异常配置）；
- 判定逻辑如可提取为包级静态方法（如 `static List<DtpRwdObjectDTO> listCrossClassObjects(String classCode, Collection<DtpRwdObjectDTO> objects)`）则一并提取，供测试直接断言。

## 验证命令

```bash
# 聚焦验证（实现阶段快速反馈）
mvn -pl common test -Dtest="CacheObjectExtensionFieldConfigTest,CacheRwdComputerRoomSecurityStrategyWriteBoundaryTest,CacheRwdVentilationStrategyWriteBoundaryTest,InfoCodeTemplateTypeModelCacheWriteFieldsTest" -DfailIfNoTests=false

# 全量回归（合并前）
mvn -pl common test
```

## 规范同步（Phase 3 / step 3.3）

`.trellis/spec/backend/cache-object-model.md` 的 "JSON Storage Field Selection" 段（现约 325-334 行）追加：
- 无法枚举 classCode 的混合范围写入（如机房安防按空间查全量设备）必须使用 `listAllRwdQueryFields()` 全量登记并集，再按需追加调用方附加字段（如 `relObjIds`）；禁止手写字段子集清单——共享 `cache_object` 行的 `object_data` 由最后一次 UPSERT 整列覆盖，字段子集写入会丢失其他策略字段；
- 方法签名小节补充 `listAllRwdQueryFields()` 声明；
- 补充"写入方字段完整性门禁"约定：每个 cache_object 写入方必须提供"写入字段边界测试"，断言其 RWD 查询字段清单覆盖对应 classCode 的 `listRwdQueryFields` 或 `listAllRwdQueryFields` 全量并集；模板缓存写入若返回非模板 classCode 对象，必须告警暴露配置异常。

## 回滚点

- 单一逻辑提交（配置类 + 策略 + 测试 + spec 一并提交）；若线上出现回归，revert 该提交恢复手写清单（仅在确认并集导致新问题时使用，回滚会重新引入原缺陷）。

## Review Gates

1. 用户 review `prd.md` + `implement.md` → `task.py start` 激活任务；
2. 实现完成 → 聚焦测试 + 全量回归；
3. 质量检查按"覆盖性核对"逐项确认字段无遗漏；
4. spec 更新后提交（commit message 按 meos-commit-message 四段式规范）。
