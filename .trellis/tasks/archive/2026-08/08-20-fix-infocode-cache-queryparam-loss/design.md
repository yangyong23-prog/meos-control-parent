# 设计：模板缓存坏快照自愈修复

## 现状链路

```
object_info_code_template_type.info_code_filter
        │  反序列化为 InfoCodeFilterValueObject（queryParam + source）
        ▼
parseFilterValueObject()                      [InfoCodeTemplateTypeModel.java L424-477]
        │  ┌─ 读取分支：getByKeyOpt 命中且 dataJson 非空 → 直接复用（缺陷点1）
        │  └─ 实时解析：filterValueObjectSupplier → handleFilterObjectQueryParamWithSource
        │       └─ 任一 source 解析为空 → 主 queryParam.replacePlaceholder 返回 null
        │          → setQueryParam(null) 后照常返回对象（污染源）
        ▼
CacheInfoCodeTemplate.from()                  [CacheInfoCodeTemplate.java L75-84]
        │  无条件 setDataJson(parseFilterValueObject())（缺陷点2）
        ▼
insertBatch / replaceByCacheKeys → cache_info_code_template.data_json
```

- 缺陷点1：读取时 `dataJson` 非空即复用，即使其 `queryParam` 为 null；实时解析（`orElseGet(filterValueObjectSupplier)`）只在 `dataJson` 为 null 时执行。坏行由此获得"不死性"：重建把旧坏 JSON 读出来原样写回。
- 缺陷点2：写入时无条件固化解析结果，包括 `queryParam = null` 的坏快照。

## 修复方案

### 修复点1：读取侧自愈（R1）

[InfoCodeTemplateTypeModel.java L462-465](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModel.java#L462-L465) 的 Optional 链增加过滤：

```java
this.filterValueObject = cacheInfoCodeTemplateRepository.getByKeyOpt(templateTypeKey)
        .map(cacheTemplate -> Optional.ofNullable(cacheTemplate.getDataJson())
                .filter(dataJson -> Objects.nonNull(dataJson.getQueryParam()))
                .orElseGet(filterValueObjectSupplier))
        .orElseGet(filterValueObjectSupplier);
```

- 缓存行 `dataJson` 的 `queryParam` 为 null → 视为未命中 → 实时解析。
- 解析成功 → 返回正确对象（运行时立即恢复正确匹配）；DB 行在下次重建时被正确内容覆盖。
- 解析仍失败（数据确实未就绪）→ 返回 `queryParam = null` 的对象，运行时行为与现状一致（模板不匹配任何对象、查询返回空），无回退到 classCode 全量查询的风险（R3）。
- `java.util.Objects` 已有导入（`import java.util.*`），无需新增 import。

### 修复点2：写入侧不固化坏快照（R2）

[CacheInfoCodeTemplate.java L75-84](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/model/entity/cache/CacheInfoCodeTemplate.java#L75-L84) 的 `from` 改为条件写入：

```java
InfoCodeFilterValueObject dataJson = templateTypeModel.parseFilterValueObject();
cacheInfoCodeTemplate.setDataJson(Objects.nonNull(dataJson) && Objects.nonNull(dataJson.getQueryParam())
        ? dataJson : null);
```

- 写入 null → 后续读取走 `orElseGet(filterValueObjectSupplier)` 实时解析，数据就绪后自然恢复（自愈闭环）。
- 需新增 `import java.util.Objects;`。

### 数据流（修复后）

| 场景 | 读取行为 | 重建行为 |
|---|---|---|
| 缓存行正常（queryParam 非 null） | 直接复用，无 RWD 查询 | 复用正确快照 |
| 缓存行坏（queryParam null）且数据已就绪 | 实时解析 → 正确结果（DB 行待重建纠正） | 实时解析 → 写入正确快照 |
| 缓存行坏且数据未就绪 | 实时解析 → queryParam null → 返回空（语义同现状） | 写入 dataJson = null（不再固化坏快照） |
| 配置本身无主 queryParam（合法场景） | 每次实时解析（仅 JSON 反序列化，无 RWD 查询，因为 queryParam 为 null 时跳过 `handleFilterObjectQueryParamWithSource`） | 写入 dataJson = null |

## 兼容性与权衡

- `replacePlaceholder` 的 null 返回语义不改：它是"占位符解析为 null 即整体无效"的既有契约，改动影响面大，本任务不触碰。
- 性能：只有 queryParam 为 null 的缓存行才走实时解析路径；正常行零额外开销。最坏情况（坏行 + 数据未就绪）每次读取多 2 次 RWD 查询，与"读取时实时解析"的既有兜底语义一致，且仅限异常状态。
- 运行时语义不变：`toDtpRwdObjectQueryParam()` 在解析失败时依旧返回 null → `queryTemplateObjects()` 返回空集合 → `match()` 不匹配任何对象（R3）。

## 回滚

- 两处均为小范围局部改动（一处 Optional 链加 filter、一处条件赋值），git revert 即回滚，无数据迁移、无接口契约变化。
- 如需生产回滚：还原代码部署后，将目标行 `data_json` 重新写回旧值即可（坏行本身不依赖代码版本）。
