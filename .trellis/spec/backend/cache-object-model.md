# Cache Object Model

## 1. Scope / Trigger

本规范适用于新增或修改缓存对象模型的表结构、实体、Mapper、仓储、云端重建和读取逻辑，
也覆盖末端白名单配置使用的全量管控空间候选接口。

当前基础结构由云端和边缘端各自的同版本迁移
`V7.260701.0.1__cache_object_model.sql` 创建，并由
`V7.260701.0.2__cache_object_base_fields.sql` 为 `cache_object` 补充独立基础列。
两端每个版本的脚本必须分别保持完全一致。云边同步登记、
数据传输、同步顺序和同步消费不属于本仓库交付范围。

## 2. Signatures

### Base Input ID

```java
public class BaseInputIdDO extends BaseDO {
    @TableId(value = "id", type = IdType.INPUT)
    protected String id;

    public @NonNull BaseDO clearSaveParameter();
}
```

`clearSaveParameter()` 必须保留调用方输入的字符串 ID，并清空 `createTime`、`createUser`、`updateTime`、`updateUser`。

### Database Tables

| 表 | 主键 | 主要业务字段 | 查询索引 |
| --- | --- | --- | --- |
| `cache_object` | `id varchar(64)` | `project_id`、`local_id`、`local_name`、`class_code`、`object_type`、`building_id`、`floor_id`、`monitoring_surveillance_status varchar(64)`、`object_data json` | `project_id`、项目 + 状态、建筑、楼层 |
| `cache_object_key_rel` | `id bigint` | `project_id`、`cache_key`、`object_id` | 项目 + 缓存键 |
| `cache_object_rel` | `id bigint` | `project_id`、正向对象、图、边、反向对象 | 正向关系、反向关系 |
| `cache_object_model_view` | `id varchar(64)` | `project_id`、视图公共列、`view_data json` | `project_id`、建筑、楼层 |
| `cache_template_object_detail` | `id bigint` | `project_id`、模板键、对象、信息点、`result_json` | 项目 + 模板键 + 对象 + 信息点 |
| `cache_rule` | `id bigint` | `project_id`、对象、信息点、规则类型 | 规则业务键 |
| `cache_rule_key_rel` | `id bigint` | `project_id`、缓存键、规则 | 项目 + 缓存键 + 规则 ID、项目 + 规则 ID + 缓存键 |

### Cloud/Edge Flyway Locations

```text
saas/meos-control-server/src/main/resources/db/flyway/meos_control/
    V7.260701.0.1__cache_object_model.sql
    V7.260701.0.2__cache_object_base_fields.sql
local/meos-control-server-edge/src/main/resources/db/flyway/meos_control/
    V7.260701.0.1__cache_object_model.sql
    V7.260701.0.2__cache_object_base_fields.sql
```

两端服务独立打包 Flyway 资源，因此同一数据库结构迁移必须在两个目录各保留一份，文件名和
内容完全一致。云边同步项目组负责表数据的同步接入，不负责替本项目发布边缘端 DDL。

### Cache Query Indexes

```sql
create index idx_custom_key
    on cache_info_code_template (project_id, custom_key);

create index idx_cache_key
    on cache_rule_key_rel (project_id, cache_key, rule_id);

create index idx_rule
    on cache_rule_key_rel (project_id, rule_id, cache_key);
```

模板元数据的 Key-only 查询不能依赖中间包含 `running_monitor_type` 的旧索引，否则只能使用
`project_id` 前缀扫描项目范围；新增 `idx_custom_key` 后删除冗余的旧 `idx_2`。规则-Key 关联的
两个方向均使用覆盖索引：按 Key 取得规则 ID 用 `idx_cache_key`，孤儿规则引用检查用 `idx_rule`。

### AIR_TERMINAL Whitelist Selection

```java
boolean isReady(@Nullable RunningMonitorTypeEnum runningMonitorType);

@NonNull Map<String, List<String>> mapObjectIdsByCacheKeys(
        @NonNull Collection<String> cacheKeys);

<T extends DtpRwdObjectDTO> @NonNull Map<String, List<T>> mapByCacheKeys(
        @NonNull Collection<String> cacheKeys,
        @NonNull Class<T> targetClass);
```

非空请求的结果必须包含每个请求缓存键；空列表表示该键没有对象关联，只有请求集合本身为空时
返回空 Map。对象-Key 查询信任写入校验和表字段约束，重复关联由 Set 自然去重，不在读取阶段再次
校验业务键完整性。

### Cache Key Replacement

```java
boolean replaceModelCacheKey(
        @NonNull String modelCacheKey,
        @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects);

boolean replaceModelCacheKey(
        @NonNull String modelCacheKey,
        @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
        @NonNull Collection<CacheWriteAction> writeActions);

boolean replaceModelCacheKey(
        @NonNull String modelCacheKey,
        @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
        @NonNull Collection<CacheWriteAction> writeActions,
        @NonNull BooleanSupplier afterReplaceAction);

boolean replaceModelCacheKeys(
        @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>>
                sourceObjectsByKey,
        @NonNull Collection<CacheWriteAction> writeActions);

boolean replaceModelCacheKeysAndRelations(
        @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>>
                sourceObjectsByKey,
        @NonNull Collection<CacheWriteAction> writeActions,
        @NonNull Map<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>>
                relationsByType,
        @NonNull BooleanSupplier afterReplaceAction);

boolean replaceObjectClassCodes(
        @NonNull Collection<String> classCodes,
        @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects);

void replaceObjectRelations(
        @NonNull DtpRwdRelationTypeEnum relationType,
        @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects);

@NonNull CacheWriteAction prepareObjectRelationsReplaceAction(
        @NonNull DtpRwdRelationTypeEnum relationType,
        @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects);

void removeCacheKeys(@NonNull Collection<String> cacheKeys);

boolean removeCacheKeys(
        @NonNull Collection<String> cacheKeys,
        @NonNull BooleanSupplier beforeRemoveAction);
```

`sourceObjects` 是调用方确定的当前新版 Key 完整对象快照；空集合仍进入短事务清理新版键关联。公共
写服务只负责使用统一转换器在事务前生成对象行和模型视图行，不根据 Key、运行监测类型或其他上下文
扩大字段或对象范围。通用关系使用独立入口：`relationType` 决定图谱和关系编码，每个
`relationObjects` 元素的 `id` 是关系主体，`relObjIds` 是该主体的完整目标集合；空对象集合表示
本次不维护该关系类型，单个对象的目标集合为 `null` 或空集合表示明确清理该主体范围。
同一次调用中的主体 ID 必须唯一，主体和目标 ID 均不得为空；重复主体无法表达哪一份是完整快照，
必须在删除旧范围前失败。调用方同时从正向和反向取得同一关系类型时，应先按主体合并目标集合，
再对该关系类型调用一次替换入口。
写服务不接收关系查询参数，也不持有 RWD 客户端。调用方必须在进入写服务前完成远程查询、关系方向
归一化及其业务校验；可复用已经查询出的完整关系对象，但不需要构造 `CacheObject`、
`CacheObjectModelView` 或 `CacheObjectRelReplaceScope`。写服务不提供一级类型完整重建生命周期接口，
也不保存跨 Key 上一版上下文。
该入口只维护新版表，不调用 `isSupported` 决定迁移范围；调用方负责选择新版或旧版写入路径。
末端、照明不得在该入口写入或删除旧版运行结果缓存。
带本地提交回调的重载用于把白名单模式配置与新版 Key 变更纳入同一短事务；回调不得发起
RWD/Feign 查询，返回 `false` 时必须显式回滚整个事务。
`replaceObjectClassCodes` 只用于无业务 Key 的公共对象完整快照：事务外投影，短事务内按 classCode
删除旧对象和模型视图后写入新快照；空快照仍必须清理旧范围。
多 Key 批量入口：

- `replaceModelCacheKeys`：多个 Key 在同一短事务内原子替换，Key 间不需要关系维护时使用；`sourceObjectsByKey` 的 Map 顺序即 Key 替换顺序
- `replaceModelCacheKeysAndRelations`：多个 Key 及其通用关系在同一短事务内原子替换，实现同类型完整缓存写入的物理原子性；事务外准备全部 Key 的对象投影和关系类型快照转换，事务内逐组 WRITE 对象、替换键关联、替换关系、执行扩展动作并调用回调
- `prepareObjectRelationsReplaceAction`：将已准备好的通用关系快照转换为本地写入动作，供 `CacheWriteAction` 插入到 `replaceModelCacheKeys` 的关系动作中，动作不开启独立事务

同一类型的保存阶段应优先使用批量入口，避免多个独立短事务之间缺少物理原子性保护。

### RWD Relation Query Scope

```java
@NonNull List<DtpRwdObjectDTO> listObject(
        @NonNull DtpRwdObjectQueryParam param);
```

调用方或事务外准备服务在需要查询新版对象模型和照明关系快照时，必须把当前 Key 的完整 `id`、
`objFrom`、`objTo` 范围一次交给 `DtpRwdClientService#listObject`，不得在业务服务中按固定数量重复
分片；已有包含图谱、关系编码、方向和空范围语义的完整关系快照应直接转换为关系替换范围，不得为了
公共写入接口再次查询。RWD 客户端包装层统一按
1000 条响应分页并并发取得后续页。包装层不会拆分请求条件；当前约定由物理世界接口接受项目级
完整 ID 集合。缓存构建直接复用既有 RWD 客户端行为，不新增专用 Feign 接口、响应完整性校验、
异常解包、重试或降级逻辑。

### AIR_TERMINAL Whitelist Atomic Save

```java
@NonNull Boolean saveShellConfigAndRemoveAirTerminalWhitelist(
        @NonNull SaveShellConfigParam param);
```

该入口必须原样使用通用 `/shellConfig/save` 的完整参数，并在事务开始前把项目上下文切换到
`param.projectId`。完整壳配置保存和末端白名单 Key 删除共用对象模型写服务的事务管理器；
Controller 不持有事务，方法结束后必须恢复原项目上下文。

### Write Concurrency Protection

```java
<T> T executeWithLock(
        @NonNull String lockKey,
        long waitSeconds,
        @NonNull Supplier<T> action,
        long @NonNull ... retryDelaysMillis);
```

缓存表 `cache_object_key_rel` / `cache_object_rel` 无唯一约束（云边同步规范禁止 `UNIQUE`），写入模式为**先删后插**（`DELETE` + `INSERT`）。RC 隔离级别下两个并发事务的 DELETE 对不存在行不加间隙锁，会导致两条事务都通过删除阶段并执行插入，产生重复记录。

强制规则：

- 所有"先删后插"写入必须在所属的构建/保存锁内执行，锁必须包住事务提交
- `CacheObjectModelWriteService` 对写入 `cache_object` / `cache_object_model_view` 的入口统一持有项目级保存锁；远程查询和对象投影仍在锁外完成
- 锁架构：
  - **触发锁**（`CacheLockKey.Trigger.cache()`）：DTX 发布、XXL-JOB 初始化/定时、调试接口，使用 `BaseEvent.executeWithLock` 抢占中断
  - **计算锁**（`CacheLockKey.CacheRwdBuild.cache(type)`）：手动缓存构建入口 `triggerBuildCache`，使用 `tryLock` 非阻塞，拿不到直接报错
  - **保存锁**（`CacheLockKey.CacheRebuild.byType(type)`）：模板缓存写入锁，按 `RunningMonitorType` 拆分粒度，Key 上的等锁时间统一为 30 分钟（`executeWithProjectLock` 取 `lockKey.getExpire().getSeconds()`，调用方不得各自定义等锁时间），覆盖百万级对象本地写入排队；锁内只允许本地替换，重建是轮询任务，等不到锁或并发变更时放弃本轮由下一轮自愈
  - **对象模型保存锁**（`CacheLockKey.CacheObjectModelWrite.cache()`）：按项目串行提交共享对象行，单次等待 120 秒，仅锁获取超时按 5 秒、10 秒退避重试，事务动作只执行一次
- 当前线程已持有该类锁时直接执行（可重入），避免锁中套锁
- 新增缓存表时必须确认写入模式并决定锁保护方案

#### 模板重建锁边界（锁外准备 + 锁内短写）

模板重建与其他缓存策略一致：**RWD 远程查询与对象准备必须放在保存锁外**，锁内只允许本地 DB 替换动作。模板源查询与三方依赖可能耗时数小时，若包在锁内会导致长时间持锁并放大锁故障影响。

```java
// CacheInfoCodeTemplateRepositoryImpl.rebuildByParam 两段式：
// 锁外：listAllowCacheTemplateTypes（源查询）→ listPreviousKeys（快照）
//       → templateRebuildService.prepareTemplates（远程对象/关系准备）
// 锁内（CacheRebuild.byType，等锁 30 分钟）：listPreviousKeys（复核）→
//       校验与准备快照一致 → executePreparedTemplates + replaceByCacheKeys
```

锁内必须复核元数据键集合与锁外快照一致：准备期间发生并发模板新增/删除（`add`/`del` 事件）时快照过期，直接放弃本次写入（warn，不抛异常），避免误删并发新增的键或用旧快照覆盖新配置；并发入口自身已完成维护，下一轮全量重建自愈。全量范围快照必须提前到源查询之前，闭合“源查询定版 → 快照”之间的并发窗口（窄范围 `replacedKeys` 限定目标模板，并发新增键不在范围内，无漏检误删危害）。`del` 入口未持保存锁（已知边界），锁内写入期与 del 的竞态窗口已被锁外化收窄两个数量级，复核负责 prepare 期间的误删防护。

#### executeWithLock 释放可靠性

`RedissonDistributedLockerServiceImpl.executeWithLock` 的 finally 必须先清除中断标志再释放锁（中断标志存在时 Redisson 同步命令可能立即失败，导致锁残留僵尸锁），unlock 必须容错并记录 error；中断状态用局部变量显式记录并恢复——**日志调用会消费线程中断标志**，不能依赖 `Thread.interrupted()` 探测值判断是否恢复。持锁动作执行中被抢占 interrupt 时锁必须释放，动作异常场景不恢复中断标志（异常已传递意图，避免残留污染线程池后续任务）。

### Template Cache Key Identity

```java
void rebuildTemplates(
        @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels,
        @NonNull Collection<String> staleTemplateKeys);

void removeTemplateKeys(@NonNull Collection<String> templateKeys);
```

项目内模板缓存场景以 Key 作为唯一业务标识。对外重建和删除入口只接收目标模板或 Key，
不得再接收 `runningMonitorType` 参与范围匹配。删除前由服务按项目和 Key 查询
`cache_info_code_template`，一级运行监测类型只用于内部选择新版对象模型或现有 RWD 存储路径；
模板元数据必须在缓存删除完成后再删除，不能在类型未知时猜测存储路径或同时清理两套缓存。

### Template Snapshot Validity

`cache_info_code_template.data_json` 是构建时刻完成占位符替换的解析快照，不是配置原样拷贝。
本契约定义快照有效性判定、读取/写入两场景的复用边界，以及强制实时解析入口。

#### 1. Scope / Trigger

- 触发场景：修改 `parseFilterValueObject` 的解析/复用逻辑、新增其调用方、变更缓存重建链路
  （`CacheTemplateRebuildService.rebuildTemplates`、`CacheInfoCodeTemplate.from`、`rebuildByParam`/`add`）

#### 2. Signatures

```java
// 读取侧: 默认复用快照 (DB 行优先, Redis 兜底)
@Nullable InfoCodeFilterValueObject parseFilterValueObject();
@Nullable InfoCodeFilterValueObject parseFilterValueObject(
        @Nullable Consumer<DtpRwdObjectQueryParam> queryParamCallback);

// 写入/重建侧: 强制实时解析, 跳过 DB 与 Redis 快照复用
@Nullable InfoCodeFilterValueObject parseFilterValueObject(boolean forceRefresh);
@Nullable InfoCodeFilterValueObject parseFilterValueObject(
        @Nullable Consumer<DtpRwdObjectQueryParam> queryParamCallback,
        boolean forceRefresh);
```

#### 3. Contracts

- 快照有效性：`dataJson != null && dataJson.getQueryParam() != null`。
- 读取侧（`forceRefresh=false`）：先命中模型实例内存 `filterValueObject`；`allowCache()` 时读
  `cache_info_code_template` 行，有效快照直接复用；无效（`queryParam` 为 null）视为未命中，
  回退实时解析；表无行时回退 Redis `CACHE_KEY_TEMPLATE` 兜底。
- 写入侧（`forceRefresh=true`）：跳过 DB/Redis 快照复用，直接执行实时解析
  （JSON 反序列化 + source 占位符替换 + RWD 查询）；**本流程内已解析过则复用内存值**，
  同一模板实例在一次重建中只解析一次。
- 固定写入入口：`CacheTemplateRebuildService.rebuildTemplates`（对象缓存侧，prepareTemplate 前）
  与 `CacheInfoCodeTemplate.from`（模板元数据侧）。两个入口共享同一模型实例内存值。
- 解析结果 `queryParam` 为 null 时：`from` 必须写 `data_json = null`，由读取侧 `orElseGet(supplier)`
  实时解析兜底。

#### 4. Validation & Error Matrix

| 条件 | 行为 |
|------|------|
| `infoCodeFilterIsJson() == false` | 返回 null（非 JSON 过滤走 `mapInfoCodeQueryFilters` 简单匹配） |
| JSON 反序列化失败 | 记 error 日志，返回 null → `from` 写 `data_json = null` |
| 占位符解析为空导致 `queryParam == null` | 返回带 `queryParam=null` 的对象 → `from` 写 `data_json = null` |
| 读取侧：缓存行有效快照存在 | 直接复用，不触发 RWD 查询 |
| 读取侧：缓存行快照 `queryParam == null` | 视为未命中，回退实时解析（坏快照自愈） |
| 写入侧：`forceRefresh=true` 且内存已解析 | 复用内存值，不重复远程解析 |
| 写入侧：`forceRefresh=true` 且内存为空 | 实时解析并写入内存字段 |

#### 5. Good/Base/Bad Cases

- Good：重建时对象缓存与 `data_json` 均基于最新配置（同一内存解析值），且每模板仅解析一次。
- Base：运行时读取命中有效快照，零 RWD 查询，性能不受影响。
- Bad：重建复用旧快照——源配置已变更但 `data_json` 永久固化历史解析结果（本次修复前行为）。

#### 6. Tests Required

- `InfoCodeTemplateTypeModelCacheReadTest.shouldResolveLiveWhenForceRefreshEvenCacheValid`：
  有效旧快照存在时 `parseFilterValueObject(true)` 仍实时解析（断言 `assertNotSame` + 触发
  `dtpRwdClientService.listObject`）。
- `InfoCodeTemplateTypeModelCacheReadTest.shouldReuseCacheWhenQueryParamPresent`：
  读取侧有效快照直接复用（断言 `assertSame` + `listObject` 零调用）。
- `InfoCodeTemplateTypeModelCacheReadTest.shouldResolveLiveWhenCachedQueryParamMissing`：
  读取侧坏快照回退实时解析。
- `CacheInfoCodeTemplateFromTest` 三用例：`from` 基于强制解析结果，`queryParam` 缺失/解析失败
  写 `data_json = null`，有效结果原样保留。

#### 7. Wrong vs Correct

#### Wrong

```java
// 写入侧直接调用默认解析: 有效旧快照被原样复制, 源配置变更永不生效
InfoCodeFilterValueObject dataJson = templateTypeModel.parseFilterValueObject();
// 重建循环内未先强制解析: 对象缓存侧 toDtpRwdObjectQueryParam 仍命中 DB 旧快照,
// 与 dataJson 使用不同配置, 双写不一致
```

#### Correct

```java
// 重建循环内 (对象缓存写入前) 强制解析, 内存值供全流程复用
for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
    templateTypeModel.parseFilterValueObject(true);
    preparedWrites.add(this.prepareTemplate(templateTypeModel));
}
// 模板元数据侧同样强制解析; 同一实例命中内存新值, 不重复远程查询
InfoCodeFilterValueObject dataJson = templateTypeModel.parseFilterValueObject(true);
```

### JSON Storage Field Selection

```java
public static @NonNull Set<String> getObjectFields(@Nullable String classCode);

public static @NonNull Set<String> getModelViewFields(@Nullable String classCode);

public static @NonNull Set<String> listObjectQueryFields(@Nullable String classCode);

public static @NonNull Set<String> listRwdQueryFields(@Nullable String classCode);

public static @NonNull Set<String> listAllRwdQueryFields();
```

对象 JSON 和模型视图 JSON 各自维护完整的 classCode 字段 Map。`listRwdQueryFields` 只合并独立列
所需基础字段以及该 classCode 在两份 Map 中配置的字段；方法不接收 objType，也不按 objType 回补字段。
单 classCode 缓存范围直接使用该集合；混合 classCode 范围只合并各 classCode 集合后发起原范围查询，
不得先只查 `id/classCode` 再由公共写服务增加一次对象快照查询。

模型图字段只允许登记在模型视图字段 Map：所有设备类只能登记 `bimId`、`bimLocation`，`outline`
仅允许登记在 `SPACE`、`AC_SPACE`；`object_data` 不存储也不返回这些字段，需要方统一通过模型视图
字段读取 API 获取。`cache_object` 读取路径组装
请求字段必须使用 `listObjectQueryFields`（基础字段 + 对象扩展字段），禁止使用 `listRwdQueryFields`
或手写并集把模型视图字段带回对象主表读取；`listRwdQueryFields` 仅用于缓存写入快照的 RWD 查询入口。

无法枚举 classCode 的混合范围写入（如机房安防按楼层空间关系查询全量设备）必须使用
`listAllRwdQueryFields` 全量登记并集，再按需追加调用方附加字段（如 `relObjIds`）；禁止手写字段
子集清单——共享 `cache_object` 行的 `object_data` 由最后一次 UPSERT 整列覆盖，字段子集写入会
丢失其他策略已写入的字段。

写入字段完整性门禁：每个 `cache_object` 写入方必须提供写入字段边界测试，断言其 RWD 查询
字段清单覆盖对应 classCode 的 `listRwdQueryFields` 或 `listAllRwdQueryFields` 全量并集。
模板缓存写入（`queryTemplateObjectsForCacheWrite`）按模板 classCode 组装字段，若查询返回
非模板 classCode 对象必须告警暴露配置异常（字段可能未请求，整列覆盖存在丢字段风险）。

### Model View Field Read API

```java
@NonNull List<Map<String, Object>> listModelViewFieldsByObjectId(
        @NonNull CacheRwdModelViewFieldsQueryParam param);
```

该接口在请求一级类型 READY 时按对象 ID 批量读取 `cache_object_model_view.id` 和 `view_data`；
未 READY 时按同一批对象 ID 实时查询 RWD。每条结果把真实对象 ID 作为 `objectId`，并平铺按
classCode 配置的实际模型视图字段；部分未命中不影响其他对象，任何分支都不读取旧 `cacheRwd`。

### SECURITY Static Cache Scope

```java
public static final String SECURITY_LIST_EQUIPMENT = "SECURITY_listEquipment";
public static final String SECURITY_LIST_SPACE = "SECURITY_listSpace";

boolean replaceModelCacheKeys(
        @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>> sourceObjectsByKey,
        @NonNull Collection<CacheWriteAction> writeActions);
```

`SECURITY` 的对象模型范围固定为以下十二类设备：`CFCSDR`、`CFCSFR`、`SPEGFG`、`SPVSCM`、
`WEGMCM`、`SPIACU`、`SPIAGD`、`SPIAID`、`SPIAMA`、`SPEGCU`、`SPVSVE`、`SPVSDR`。设备完整
快照写入 `SECURITY_LIST_EQUIPMENT`，项目全部空间写入 `SECURITY_LIST_SPACE`。建筑和楼层由独立
`CacheRwdProjectStructureStrategy` 维护，安防策略不得查询或写入；项目结构策略不建立缓存 Key，
只按 `class_code in (BUILDING, FLOOR)` 完整替换 `cache_object` 和
`cache_object_model_view`，不得使用运行监测类型前缀。两个安防 Key 的对象、
模型视图、Key 关联以及 `service_space`（`EQUIPMENT_SERVICE_SPACE`）和 `in_space`
（`EQUIPMENT_IN_SPACE`）关系动作必须在同一个短事务中提交。关系主体范围使用当前和上一版主体并集，
缺失主体提交空目标集合以清理删除或类别迁移后的旧关系；同一关系方向一次交给 RWD 客户端查询，
不得在业务层按 ID 分片。

安防设备和空间不查询、不持久化、不恢复 `name`，对外名称统一使用缓存独立基础列 `localName`；
安防静态扩展字段按 classCode 保存于对象 JSON；安防设备的 `bimId`、`bimLocation` 只能保存于
模型视图 JSON，设备不得保存 `outline`，空间 `outline` 仍按 `SPACE` 模型视图契约保存。安防构建查询
使用 `DtpRwdObjectQueryParam` 默认的 `valid = true` 范围。动态 IoT 值、孪生规则、状态统计和异常事件
不进入上述静态对象或模型视图缓存。

READY 后，`SecurityServiceImpl#listEquipment`、`listDeploymentSpace` 和 `querySecurityFunction`
只读取上述 Key、关系和模型视图，并把设备 `bimId`、`bimLocation` 或空间模型视图字段（含
`outline`）一次批量合并回对应响应；缓存空集合是
权威结果，不穿透 RWD，缓存仓储或转换异常直接上抛。未 READY 时三个接口完整保留原 RWD 路径，
公共 `getSecureAllEquipmentList(projectId, needRelation, queryParam)` 始终保持原 RWD 和调用方关系参数语义。
所有缓存仓储查询必须由当前项目上下文隔离。项目结构策略只在公共构建阶段执行：全量构建先执行
一次，显式构建本版支持的一级类型时也必须先执行一次；不得随着每个类型策略重复执行。公共结构
构建失败时，依赖新版对象模型的一级类型不得继续构建或提交 READY。SECURITY READY 只在公共
项目结构以及两个安防 Key、模型视图和关系均完整替换成功后提交。

`SecurityServiceImpl#dashboard` 每次请求只判断一次 SECURITY READY：READY 时使用固定字段集合从上述
安防空间/设备 Key 和 `class_code` 过滤读取项目建筑/楼层静态对象，并从两类关系组装 ARMING 静态范围；屏蔽和黑名单继续由本地需求
实例配置排除，动态 IoT 状态仍在查询期实时读取。未 READY 时完整调用原 RWD 实例填充路径，同一请求
不得混用缓存与 RWD 静态对象。缓存空集合是权威结果，不得穿透或改用旧路径。

### Cache Read Ordering

`cache_object` 主表查询固定 `order by id`，不保证任何业务顺序；`floorSequenceId`、
`buildingSequenceId` 等序号字段保存在对象 JSON 扩展字段中，SQL 无法直接排序。

用缓存路径替换 RWD 远程查询时必须显式补齐原有排序，否则发生行为回归（UYAI-1311：安防
Dashboard 楼层乱序）。原 RWD 客户端内部已按序号排序：`DtpRwdClientServiceImpl#listFloor`
按 `floorSequenceId` 倒序（楼层从高到低）、`#listBuilding` 按 `buildingSequenceId` 升序，
缓存路径必须对齐这两个 Comparator。

#### Wrong

```java
this.buildings = cachedBuildings.stream()
        .filter(building -> buildingIds.contains(building.getId()))
        .collect(Collectors.toCollection(ArrayList::new));
this.buildingIdAndFloors = cachedFloors.stream()
        .filter(floor -> floorIds.contains(floor.getId()))
        .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
```

#### Correct

```java
this.buildings = cachedBuildings.stream()
        .filter(building -> buildingIds.contains(building.getId()))
        .sorted(Comparator.comparing(DtpRwdBuildingDTO::getBuildingSequenceId,
                Comparator.nullsFirst(Comparator.naturalOrder())))
        .collect(Collectors.toCollection(ArrayList::new));
this.buildingIdAndFloors = cachedFloors.stream()
        .filter(floor -> floorIds.contains(floor.getId()))
        .sorted(Comparator.comparing(DtpRwdFloorDTO::getFloorSequenceId,
                Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
```

排序必须发生在业务调用方（按各自业务顺序要求），不得写入通用缓存查询仓储或 SQL；通用
`listByClassCode` / `listByCacheKey` 不承诺顺序。

### AIR_TERMINAL Control Space Candidate API

```text
POST /cacheRwd/listAirTerminalControlSpaces
```

```java
@NonNull List<CacheRwdAirTerminalControlSpaceVO> listAirTerminalControlSpaces();
```

接口没有请求体，返回当前项目可供白名单配置选择的末端全量管控空间。每项固定只包含
`id`、`localName`、`floorId`、`buildingId`，不返回对象扩展字段、模型视图字段或规则标记。

### Current Project Debug APIs

```text
POST /debug/cache/cacheObjectModel/init?runningMonitorType=AIR_TERMINAL
```

调试接口只使用 HTTP 请求头已经建立的当前集团和项目上下文，且一级运行监测类型必须属于
当前版本固定支持范围。接口直接调用单类型完整缓存构建入口，不进入 XXL-JOB 的集团、项目遍历，
也不复制 READY 提交逻辑。

### READY Commit

```java
void markReady(@NonNull RunningMonitorTypeEnum runningMonitorType);

Boolean buildCache(@Nullable RunningMonitorTypeEnum runningMonitorType);
```

`CacheRwdDomainService#buildCache(type)` 在该一级类型构建锁内执行全部匹配策略。只有策略完整执行
并返回成功后才调用 `markReady(type)`：先通过独立数据库事务幂等写入 READY，再刷新共享 Redis，
最后释放构建锁。显式 `AIR_TERMINAL`、`LIGHTING` 或 `SECURITY` 请求保留原异常；其他类型和
`type = null` 的全量构建维持失败返回值。手动初始化、调试初始化和 DTX/全量构建只要复用该领域
入口，就共享同一状态时序。

### READY Auto-Start Switch

```java
@Component
@ConditionalOnProperty(prefix = "project.cache.rwd", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class CacheObjectModelStatusRestoreTask {
}
```

`project.cache.rwd.enabled` 是云端 READY 启动恢复与边缘 READY 自动收敛共用的 Nacos 开关，
两端自动任务类都使用同一类级 `@ConditionalOnProperty`。缺省为 `true` 以保持既有
启动行为；显式为 `false` 时不注册云端 `CommandLineRunner` Bean，也不注册同时承载
边缘启动初始化和 30 秒调度的 Bean，因而不会访问权限、`ProjectCfg`、Redis 或状态数据库。
该开关只控制自动传播 Bean，不关闭手动初始化、Debug 初始化、DTX/全量构建或构建成功后的
READY 提交。Spring 条件在应用上下文启动时判定，Nacos 运行时刷新不会动态增删该 Bean；
云端和边缘端在开关值变更后都必须重启才生效。

### InfoCode Public Import Boundary

```java
@NonNull Set<Long> addSheetConfig(@NonNull InfoCodeImportContext context);

@NonNull Set<Long> delSheetConfig(
        @NonNull InfoCodeTemplateTypeRemoveDaoParam param);
```

信息点配置导入、分组导入、删除和名称导入都操作公共库。控制器必须在事务代理执行前清空
`ContextUtil.tenant`、`groupCode`、`projectId`。非事务服务入口再次清空作为直接调用保护；
带事务的服务入口仍以控制器的事务前清理为准。请求即使携带这些路由值也不得参与公共表 SQL。
模板替换或删除在公共库事务内返回旧模板 ID，事务完成后再用
无集团、项目过滤条件的项目遍历入口处理所有有效集团和项目。项目缓存失败记录项目级日志或返回
失败明细，不回滚已经提交的公共配置。

## 3. Contracts

- 所有表必须包含 `create_time datetime(6)`、`create_user varchar(64)`、`update_time datetime(6)`、`update_user varchar(64)`。
- `cache_object.id` 和 `cache_object_model_view.id` 使用 RWD 对象 ID，不得重新生成。
- 其他五张表使用调用方生成的稳定 `bigint` ID，不使用 `auto_increment`。
- `object_data`、`view_data` 允许 SQL `NULL` 以兼容异常或历史脏数据；正常业务写入无扩展字段时保存 `{}`。
- `object_data` 与 `view_data` 分别使用独立的 `classCode -> 完整字段集合` 配置；所有 JSON 写入字段都必须在对应 classCode 中显式声明，不得根据 objType、运行监测类型、业务 Key 或查询场景隐式补充字段。
- 新版对象写入调用方负责把 classCode 完整字段加入原范围查询；公共写服务只消费调用方给出的完整 RWD 快照并通过统一转换器投影对象和模型视图，不查询 RWD，也不扩大任何投影字段。
- `local_id` 和 `local_name` 是 `cache_object` 的独立可空基础列；缓存快照查询必须把它们纳入公共 RWD 字段集，读取时仅在调用方请求对应字段时投影返回，不得再存入 `object_data`。
- classCode 完整字段并集只允许用于缓存写入快照入口；普通模板对象查询保持原调用方字段契约，不能因为缓存落盘需要而扩大旧业务响应。
- `monitoring_surveillance_status` 是可空单值字符串列；SQL `NULL` 表示 RWD 未返回，非空时保存 RWD 返回的单个状态 code，不保存展示名称或 JSON 数组。
- 同一个对象只保存一个监测监控状态；查询参数可以提供多个候选 code，Mapper 使用参数化 `IN` 条件匹配其中任意一个，不拼接用户输入。
- 状态筛选使用 `(project_id, monitoring_surveillance_status)` 组合索引；按对象 ID 的查询继续由主键索引处理。
- `result_json` 必须非空，并保存自包含的模板对象取值结果。
- `cache_template_object_detail` 每行保存当前 `(templateKey, 根对象 ID, infoCode)` 对应的完整有序 `List<InfoCodeRwdObjectDetailDTO>`；`[]` 是有效命中，不能与缺少数据库记录混为一谈。
- `result_json` 在实体和 Mapper 边界必须使用 `String`，不得直接使用 `JacksonTypeHandler` 映射泛型 List；读取统一通过 `TypeReference<List<InfoCodeRwdObjectDetailDTO>>` 恢复具体 DTO 类型。
- 模板对象明细写入前必须转换为独立 JSON 树，只沿任意深度 `relObjects` 删除 `relInfoCodeGroupConfigs`，并保证源 DTO 不被修改；不能只设置为 `null`，因为全局 Jackson 配置不保证忽略 null 字段。
- 模板对象明细精确批量查询必须使用完整 `(templateKey, objectId, infoCode)` 业务键组合；读取返回实际命中的业务键，缺行不影响其他结果，禁止拆成多个独立 IN 条件形成组合误命中。
- 单模板替换范围为 `(projectId, templateKey)`；同一 `(objectId, infoCode)` 的相同 JSON 可去重，不同 JSON 必须在删除旧范围前失败。
- 缓存对象模型各层的业务校验和范围冲突必须使用国际化 `BusinessException`；内部缓存数据问题使用 `SYSTEM_ERROR_B0001`，不得用 `IllegalArgumentException` 或 `ApplicationException` 代替业务异常。
- JSON 转换统一复用 `ObjectMapperUtils.writeValueAsStringFailSafe/readValueFailSafe`，Converter 不自行捕获或包装 Jackson 异常。fail-safe 返回值声明为 `@Nullable`；写入使用方必须在删除旧范围前把 `null` 按无效业务数据抛出 `BusinessException`，读取方不再重复校验写入阶段已经保证的完整性，也不因单条未命中把其他结果整批清空。
- `@NonNull` 参数由调用方保证，方法内部不重复判空；缓存读取集合及其元素同样由调用方保证，Resource Service 不重复去重或逐元素校验。写入集合元素、`@Nullable` 和 fail-safe 返回值仍按写入业务契约检查。
- 最多几千个参数的普通 `IN` 读取直接使用调用方集合执行一次 SQL，不为去重再复制查询参数；需要结果去重时由拥有该业务语义的调用方或结果组装层处理。
- 对象-Key 关联的必填字段、冲突和重复校验只在写入阶段执行；读取阶段按查询结果组装 Map，重复关联去重后正常返回，不因重复行把整个批次降级为空。
- 新版对象模型和照明关系查询由调用方按每个关系方向各调用一次 RWD；业务层不得按对象 ID 和关系 ID 嵌套分片。RWD 返回的 `relObjIds` 仍必须限制在当前请求关系范围内，避免写入未归属当前 Key 的关系；写服务只把调用方给出的关系枚举和关系主体对象转换为资源层替换范围。
- 新模型读取入口直接使用 `CacheObjectModelStatusService#isReady` 判断共享 Redis 是否 READY；模板入口在调用状态服务前先判断 `allowCache()`，状态服务不得依赖模板模型。
- `InfoCodeTemplateTypeEnum.allowCache()` 是模板缓存写入的许可矩阵，控制在 `CacheTemplateRebuildService` 中是否为新类型执行对象查询和扩展写入动作。**新增 `RunningMonitorTypeEnum` 到 `SUPPORTED_RUNNING_MONITOR_TYPES` 时，必须同步更新 `allowCache()` 方法**，否则该类型的模板对象缓存永不写入，DTX 发布和 XXL-JOB 初始化均无法生效。该矩阵按 `runningMonitorType` 分支，每个分支返回允许的 `templateType` 集合（如 SECURITY 仅允许 TABLE_LIST）。
- `isSupported` 只用于既有消费者在新版与历史链路之间选择迁移范围；既有历史消费者继续保持旧链路。本次新增的白名单和模型视图 API 属于新版能力，不得通过 `isSupported` 回退或写入旧表：白名单统一只维护新版 Key，模型视图未 READY 时按接口契约实时查询 RWD，READY 后读取新表。
- 独立模型视图字段接口不得新增按运行监测类型或查询场景维护的返回字段配置；查询只投影 `id`、`view_data`，最终字段仍由写入时的 classCode 配置决定，真实 `objectId` 必须覆盖历史 JSON 同名值。
- SQL、连接、删除、插入和事务异常不得伪装成缓存未命中；写入参数、转换和冲突校验必须在删除旧范围前完成，失败时保留上一版完整缓存且不得置为 `READY`。
- 显式按 `AIR_TERMINAL`、`LIGHTING` 或 `SECURITY` 构建时，策略、SQL 和事务异常必须保留原异常上抛给手动任务、调试接口或 DTX 事件监听器边界；历史系统的显式构建仍把异常折算为 `false`。未指定类型的 DTX 全量构建中，任一类型失败只把总结果置为 `false`，不得中止后续类型，也不得根据当前外层策略类型决定是否重新抛出。事件监听器记录异常或 `false`，不得把缓存重建故障同步抛回发布请求。
- 当前项目调试初始化接口允许当前版本固定支持范围内的一级类型（包括 `SECURITY`），不得调用 XXL-JOB `execute()` 或集团项目遍历入口；调试调用期间必须保留请求上下文，并让构建、锁、状态和 Redis 异常继续到控制器边界。
- 对象模型按单个新版缓存 Key 原子替换：先在事务外得到当前完整结果，再在短事务内删除该 Key 的新版键关联并写入本次结果。空结果只删除该新版 Key，不删除或写入旧版运行结果缓存，不反查一级类型对象模型上一版快照，也不扫描对象、模型视图或无所有权关系主表；已经不可达的无 Key 引用记录允许暂时保留。
- 照明关系由每个缓存 Key 独立处理，不枚举或合并其他照明 Key。替换时以当前 Key 新旧回路并集为删除范围：服务空间、楼层按 `object_id` 精确删除，控制模块、DDC 按 `rel_object_id` 精确删除，再写入当前 Key 的完整 RWD 关系结果；多个 Key 命中同一关系时允许重复执行。
- `local_id`、`local_name`、`building_id`、`floor_id` 允许为空；`class_code` 和 `object_type` 非空。
- `building_id` 和 `floor_id` 只承载单一归属语义：RWD 可能返回逗号分隔字符串或 JSON 数组两种多值结构，该列只写入第一个值（按首个英文逗号截断取第一段，再剥离首尾的数组括号、元素引号与空白装饰）；单值、空串等其他形态原样保存 RWD 返回值，字段缺失保存 SQL `NULL`。两个字段各自独立判断，规则对全部 classCode 生效，实现位于对象与模型视图两个转换器的统一读取方法。
- 不建立外键或业务组合唯一约束；业务去重和范围替换由服务事务保证。
- `cache_rule_key_rel` 只表达单个缓存 Key 的规则写入归属，必须同时保留 `(project_id, cache_key, rule_id)` 和 `(project_id, rule_id, cache_key)` 覆盖索引；前者避免按 Key 查询规则 ID 时回表，后者用于孤儿规则引用检查。
- 同一项目的 `cache_object` / `cache_object_model_view` 行可被不同缓存 Key 和不同运行监测类型共享；UPSERT 的主键冲突处理只保证行不重复，不能替代事务外的项目级保存互斥。
- 对象模型保存锁只保护四个会写对象主表或模型视图表的入口：`replaceModelCacheKey`、`replaceModelCacheKeys`、`replaceModelCacheKeysAndRelations`、`replaceObjectClassCodes`。调用方的 RWD 查询、对象投影和参数准备必须先完成，再获取锁并开启一次数据库事务。
- 锁服务仅在 `tryLock` 返回 `false` 时按调用方给出的退避序列重新获取；Redis 异常、线程中断、SQL/事务和锁内动作异常立即上抛。锁成功后动作只执行一次，事务提交或回滚完成后才释放锁。
- 规则业务读取直接按项目、对象 ID、信息点编码和云边规则类型查询 `cache_rule`，不得通过缓存 Key 或 `cache_rule_key_rel` 附加业务场景范围；关系表仅用于单 Key 替换、删除和孤儿清理。
- 规则表和关系表不存储或查询 `running_monitor_type`、`running_monitor_second_type`；规则读取接口不得接收缓存 Key 或这两个运行监测范围参数。
- 同一缓存键的远程计算必须在事务外完成。对象、模型视图、Key 关联及其模板明细、规则等扩展动作在对象入口的一个短事务内提交；通用关系按调用方显式调用独立关系入口，在另一个短事务内提交。规则业务键分配需要项目级跨实例锁覆盖到对应对象入口事务提交。
- 缓存资源层的对象、模型视图、关系、模板明细和规则批量写入统一通过 `CacheBatchExecutor` 按 1000 条分片调用 Mapper 多值 SQL；调用前必须完成主键、审计字段和业务校验，不使用 MyBatis-Plus `saveBatch` 代替显式批量 SQL。最多几千个参数的主键、对象 ID 和缓存 Key 普通 `IN` 读取使用一次 SQL，避免分片增加数据库往返；模板明细、规则完整业务键及关系范围等复合 `OR` 条件继续按 500 条控制 SQL 长度。
- `cache_info_code_template` 仓储拥有模板键元数据的独立业务范围；单个一级运行监测类型的全量模板重建可以在该仓储内比较上一版和当前模板键，并把明确失效键交给单 Key 删除入口。该范围替换不允许扩展为对象、模型视图或无所有权关系主表的孤儿扫描；带模板类型、对象类型或设备类筛选的窄范围不得退化为一级类型清理。
- 项目内模板缓存 Key 是场景唯一标识；`runningMonitorType` 只保存在模板元数据中供删除服务选择存储路径，不参与 Key 唯一性、对外删除签名或删除范围。`cache_info_code_template` 必须使用 `(project_id, custom_key)` 索引支持该访问路径；所有删除调用必须在对应模板元数据仍存在时执行，随后再按 Key 删除元数据。
- 信息点配置导入会关闭模板删除事件并先删除源模板。导入删除前必须用完全相同的运行监测类型、二级类型、模板类型和设备类范围查询旧模板 ID，再按这些 ID 精确删除缓存键；删除源模板后不能仅按新模板 ID 推导旧键。
- 信息点配置公共接口不得使用请求中的集团、项目或租户路由；公共库关联配置必须在同一公共事务中处理，旧模板 ID 只作为事务后的全集团、全项目缓存清理输入。名称变更至少清理覆盖所有项目的信息点 Redis 缓存，模板导入和删除还必须逐项目维护数据库缓存。
- READY 只由单类型完整构建成功边界写入，不再保留独立数量核验任务、报告、快照或 Debug 核验接口；策略调度必须合并每个 `buildCacheData(...)` 的返回值，任一策略返回 `false`、抛出异常或发生 SQL/事务异常时不得写入 READY，也不得记录该策略构建成功。
- READY 写入必须仍受该一级类型构建锁保护；数据库提交和 Redis 刷新不能移动到手动任务、Debug Controller 或 DTX 监听器中分别实现。
- 数据库 READY 提交成功但 Redis 刷新失败时，构建入口抛出国际化 `BusinessException`；数据库事实保留，后续服务启动恢复、边缘收敛或再次成功构建负责传播 READY。
- 历史数据库或 Redis 中的 `FAILED` 仍按未就绪读取以兼容旧值，但新构建流程不得再写入 `FAILED`，构建失败也不得把已有 READY 降级。
- 云端 READY 状态恢复必须遍历集团和项目上下文，仅对 Redis 缺失 Key 批量读取数据库并使用 SETNX 写入 READY；已有 Redis 值（包括测试手工值）不得覆盖。边缘端仍按固定支持类型每 30 秒只收敛缺失 Key。
- 云端 READY 启动恢复与边缘启动/定时收敛任务类必须统一使用 `@ConditionalOnProperty(prefix = "project.cache.rwd", name = "enabled", havingValue = "true", matchIfMissing = true)`；显式关闭时两个 Bean 都不注册，不得再依赖 `NacosSwitchConfig` 执行运行时判断，且不得影响手动构建和 READY 提交。开关变更后云端和边缘端都需重启生效。
- `WHITELIST_*` 末端白名单空间键属于 `AIR_TERMINAL` 新对象模型范围：配置保存必须先在内存中按本次白名单 ID 完成实时数据补全和校验，再通过通用接口只替换新版 Key，不能先覆盖旧表作为临时范围；切换全量或删除配置只清理新版键关联。
- 首版迁移完成或新版白名单配置保存后，`SPACE_RANGE_TERMINAL` 是末端全量/白名单模式的唯一持久化事实。保存自定义白名单时必须先完整替换新版 Key，再把壳配置切为 `WHITE_LIST`；切换全量或删除白名单时必须先把壳配置切为 `FULL`，再删除新版 Key。模式写入和对应的新版 Key 替换或删除必须使用同一个事务管理器，在同一短事务中整体提交；本地保存返回 `false` 或任一步骤抛出异常时必须整体回滚，同时保留原壳配置和原新版 Key。通用 `/shellConfig/save` 直接把该配置切为 `FULL` 时，必须先按原始 `SaveShellConfigParam` 完整持久化目标项目配置，再在同一短事务中只清理该项目的新版末端白名单 Key；不得用简化参数重复保存或覆盖同场景其他字段。直接保存 `WHITE_LIST` 只选择当前新版 Key，空 Key 是合法空白名单。查询配置和业务空间范围都以壳配置选择模式，不得根据 Key 是否命中反推模式。
- 首版升级迁移是唯一允许读取旧白名单结果的初始化例外：当数据库尚无 `AIR_TERMINAL READY` 且新版白名单 Key 为空时，必须先于壳配置判断读取一次旧对象表。旧白名单 Key 中存在有效对象 ID 即同时证明历史项目启用了白名单并给出迁移范围；此时不得被缺失壳配置返回的默认 `FULL` 阻断，而应按 ID 实时查询 RWD，并在同一短事务内替换新版 Key、把 `SPACE_RANGE_TERMINAL` 补写为 `WHITE_LIST`。
- 未命中首版迁移分支时，末端白名单完整构建才直接读取壳配置；无法确认空间范围模式时本次构建失败且不得写 READY。明确为 `FULL` 时以空目标替换白名单 Key，清理由历史入口或异常中断遗留的关联；明确为 `WHITE_LIST` 时使用新版 Key 当前配置 ID 重建。业务未 READY 分支不得使用旧表，数据库 READY 后也不得因 Redis 暂缺或新版 Key 为空恢复旧残留。
- `AirTerminalSpaceWithStatusQueryParam` 和 `RunningMonitorSpaceQueryParam` 均不得暴露 `useWhiteList` 等空间范围选择字段。末端空间业务查询只由 `SPACE_RANGE_TERMINAL` 决定白名单 Key 或项目空间 Key，并在策略中选择固定白名单入口或历史项目全量入口；`AIR_TERMINAL READY` 后白名单模式按白名单键读取新对象或模型视图，未 READY、状态缺失或 Redis 异常时读取新版白名单 Key 中的配置对象 ID，并按这些 ID 实时查询 RWD。选中新模型后返回新表实际查询到的数据，不得因部分缺失回退旧表或 RWD。缓存构建的 `useCache=false` 内部路径始终计算项目全量空间，不读取白名单范围。
- 自动判断白名单的消费者必须先读取壳配置决定使用白名单 Key 还是项目全量 Key，不得根据任一 Key 是否命中反推配置。壳配置选择白名单时，空关联就是空结果；选择全量时固定读取项目基础 Key。全量重建必须显式关闭业务缓存读取，从 RWD 计算最新空间。
- `/cacheRwd/listAirTerminalControlSpaces` 是白名单配置的全量候选接口，不属于自动判断白名单的业务消费者；当前壳配置即使是 `WHITE_LIST`，该接口也不得缩小为白名单 Key。`AIR_TERMINAL READY` 时按 `AIR_TERMINAL_listSpace` 查询对象表四个基础字段，未 READY 时通过末端运行监测策略实时计算同一全量范围。
- 末端全量候选接口使用固定 VO，并在 `CacheRwdServiceImpl` 中显式赋值四个字段；不得用 `BeanUtil` 反射复制，也不得直接返回包含更多属性的通用 RWD DTO。
- 云端和边缘端必须各自包含同版本、同内容的 Flyway 迁移；`.0.1` 创建七表，`.0.2` 在两端一致增加 `cache_object.local_id`，保证两端数据库结构一致；
  数据同步实现仍由云边同步项目组负责。

## 4. Validation & Error Matrix

| 条件 | 处理 |
| --- | --- |
| 输入型字符串 ID 已设置 | 保存参数清理后必须保持原值 |
| 对象或模型视图无扩展字段 | 正常业务写入 `{}`，不得以 SQL `NULL` 表示正常空对象 |
| 单 classCode / 混合 classCode 缓存范围查询 | 分别使用该 classCode 完整字段 / 全部目标 classCode 字段并集；公共写服务统一投影且 RWD 调用次数为零 |
| classCode 未配置模型视图字段 | `view_data` 写入 `{}`，不得根据 objType 推断设备或空间字段 |
| 监测监控状态未返回 / 返回单个 code | 独立列分别保存 SQL `NULL` / 单值字符串，不得写入 `object_data` |
| RWD 返回多值结构的 `building_id` / `floor_id`（逗号分隔字符串或 JSON 数组） | 对应独立列只保存第一个值；单值与空串原样保存，字段缺失保存 SQL `NULL`，两字段独立判断 |
| 读取到扩展 JSON 为 SQL `NULL` | 按异常或历史脏数据容错，不覆盖公共列 |
| 模型视图字段接口请求部分对象不存在 | 返回其他实际命中对象，不要求完整命中，不回退旧缓存 |
| 模型视图字段接口对应一级类型未 READY | 按请求对象 ID 实时查询 RWD，不访问模型视图表或旧 `cacheRwd` |
| 末端全量候选接口 READY | 只查询 `AIR_TERMINAL_listSpace` 及四个基础字段，不读取白名单 Key |
| 末端全量候选接口未 READY、状态缺失或 Redis 异常 | 实时计算项目全量管控空间，只请求四个响应字段 |
| 末端全量候选接口查询缓存或 RWD 失败 | 保留底层异常向上抛出，不返回伪造空结果 |
| 新版缓存关系范围包含数千个对象或关系 ID | 每个关系方向只调用一次 `listObject`，由 RWD 客户端分页响应；不得在业务层形成分片笛卡尔调用 |
| RWD 拒绝项目级完整 ID 请求或分页调用失败 | 沿用既有 RWD 客户端行为；缓存构建不增加专用校验、异常转换、重试或兜底 |
| 模板对象结果 JSON 为空 | 拒绝写入，不生成不完整明细 |
| 模板对象结果为 `[]` | 作为存在且完整的空结果保存和返回，不按缓存未命中处理 |
| 写入前 fail-safe JSON 结果为 `null` | 使用方抛出国际化 `BusinessException`，删除前失败 |
| 读取时缺少对象、关系、明细或规则记录 | 返回实际查询到的结果，不整批清空、不回退旧链路 |
| 对象-Key 关联查询存在重复行 | 由 Set 去重并返回查询结果，不重复执行写入校验 |
| 模板对象明细精确查询缺少任一业务键 | 返回其他已命中的复合业务键结果，缺失键由调用方按空结果处理 |
| `AIR_TERMINAL READY` 且壳配置选择白名单、白名单键关联非空 | 使用白名单新键读取对象或模型视图，不访问旧对象表 |
| `AIR_TERMINAL READY` 且壳配置选择白名单、白名单键为空关联 | 返回空空间，不读取项目基础键 |
| `AIR_TERMINAL READY` 且壳配置选择全量 | 固定读取项目基础键，不查询白名单键决定范围 |
| `AIR_TERMINAL` 未 READY、状态缺失或 Redis 异常 | 从新版白名单 Key 取得配置 ID 后实时查询 RWD，不读取旧白名单对象快照 |
| 数据库无 `AIR_TERMINAL READY`、新版白名单 Key 为空、旧白名单 Key 有有效对象 ID | 先按旧 ID 迁移并原子补写 `WHITE_LIST`；不得读取默认 `FULL` 后清空迁移范围 |
| 数据库无 `AIR_TERMINAL READY`、新版白名单 Key 为空且旧白名单 Key 无有效对象 ID | 不执行迁移，继续按当前壳配置执行新版日常构建 |
| 白名单模式保存、Key 替换或 Key 删除返回 `false` / 抛出异常 | 使用同一事务管理器整体回滚壳配置和新版 Key 变更；策略调度返回失败且不写 READY |
| 同一模板对象明细业务键出现不同 JSON | 抛出国际化 `BusinessException`，删除旧模板范围前失败并保留上一版 |
| 一级类型全量模板查询为空 | 由拥有 `cache_info_code_template` 的模板仓储清理该范围上一版模板键；不得扩展为对象、模型视图或无所有权关系主表清理 |
| 带设备类等筛选的模板查询为空 | 不按一级类型扩大清理；导入流程按删除前捕获的旧模板 ID 精确清理 |
| 信息点配置请求携带集团编码、项目 ID 或租户值 | 入口在公共事务开始前清空这些路由值；公共表 SQL 不使用请求范围 |
| 信息点模板导入或删除完成 | 使用删除前捕获的旧模板 ID 遍历所有有效集团、项目清理缓存；不得只处理请求上下文项目 |
| 某个项目缓存清理或重建失败 | 记录该集团、项目失败并继续后续项目；公共配置不回滚，接口不因缓存异常显示业务失败 |
| 公共配置 Redis 清理失败 | 记录错误并保留已提交的公共配置，不因缓存异常向前端返回业务失败 |
| 按模板 Key 删除缓存 | 在元数据删除前按项目和 Key 查询类型并选择唯一存储路径；不要求调用方传类型，不同时删除新旧缓存 |
| 模板元数据已先于缓存删除 | 不猜测类型、不执行双路径兜底；这是调用顺序错误，删除流程必须调整为先缓存后元数据 |
| 模板元数据按 Key 查询但缺少 `(project_id, custom_key)` 索引 | 结构检查失败；不得依赖运行监测类型位于中间列的旧索引扫描项目范围 |
| 规则关联按 Key 查询规则 ID | 使用 `(project_id, cache_key, rule_id)` 覆盖索引；反向引用检查继续使用 `(project_id, rule_id, cache_key)` |
| SQL、连接、删除或插入失败 | 保留原异常并向上抛出，写事务回滚 |
| 对象模型保存锁前两次 `tryLock` 返回 `false`、第三次成功 | 分别等待 5 秒、10 秒后重试；只开启并执行一次数据库事务 |
| 对象模型保存锁三次均未获取 | 抛出系统业务异常；Mapper 和事务动作零调用 |
| Redis 获取锁异常或锁内 SQL/事务动作异常 | 原异常立即上抛，不进入锁超时重试 |
| 锁获取或退避期间线程中断 | 恢复线程中断标记并立即失败，不继续重试 |
| 手动初始化末端/照明时策略或事务失败 | 释放一级类型构建锁，并把原始运行时异常上抛给任务 |
| 手动初始化的单类型构建返回 `false` | 记录类型失败并继续后续类型；项目结束时抛出国际化 `BusinessException`，不得把该项目记为成功 |
| 显式构建未接入新模型的历史系统失败 | 记录异常并返回 `false`，不改变历史调用方异常边界 |
| 未指定类型的 DTX 全量构建中单个类型失败 | 记录失败、继续后续类型并最终返回 `false`；不得按外层策略类型决定是否中止 |
| 单类型完整构建成功 | 构建锁内提交数据库 READY、刷新共享 Redis，然后释放锁 |
| 单类型构建返回 `false` 或抛出异常 | 不写 READY；已有 READY 不降级 |
| 手动初始化项目执行结束 | 成功和失败分别记录一条项目终态日志，包含 `groupCode`、`projectId`、一级类型和耗时；失败日志额外包含异常类型与原因 |
| 数据库 READY 提交失败 | 不刷新 Redis，保留数据库异常并在释放构建锁后向上抛出 |
| 数据库 READY 已提交但 Redis 刷新失败 | 抛出国际化 `BusinessException`；保留数据库 READY，等待恢复任务或下次成功构建传播 |
| `project.cache.rwd.enabled` 缺失或为 `true` | 注册云端启动恢复及边缘启动、30 秒收敛 Bean，保持既有行为 |
| `project.cache.rwd.enabled = false` 且云端启动 | 不注册云端恢复 Bean，因而没有权限服务、状态数据库或 Redis 访问 |
| `project.cache.rwd.enabled = false` 且边缘启动 | 不注册边缘收敛 Bean，启动初始化和 30 秒调度都不存在；手动构建仍可执行 |
| 运行中修改 `project.cache.rwd.enabled` | 当前上下文的 Bean 注册状态不变；云端和边缘端均需重启才生效 |
| DTX 发布触发的缓存重建失败 | 事件监听器记录 error；不把异常同步抛回 DTX 发布请求；返回 `false` 时记录 warn |
| 当前照明缓存 Key 的完整结果为空 | 删除新版键关联，并按该 Key 上一版回路 ID 清理四类照明关系；不查询其他照明 Key |
| 删除一个规则缓存键 | 先按 `rule_id` 检查其他引用，只删除无引用规则 |
| 迁移包含外键、业务唯一约束或自增主键 | 结构检查失败，不允许进入后续持久化任务 |
| 云端或边缘端缺少同版本迁移，或两份脚本内容不一致 | 结构交付检查失败，不允许进入发布 |

## 5. Good / Base / Bad Cases

- Good：对象 ID 原样作为 `cache_object.id`，`local_id`、`local_name` 等公共字段进入独立列，扩展字段进入 JSON。
- Good：不同运行监测类型保存同一项目的共享对象时，只串行化本地事务；远程查询和投影仍可并行。
- Base：保存锁前两次超时、第三次成功时，只执行一次 Mapper 和事务动作。
- Bad：重试整个 `TransactionTemplate` 或 `CannotAcquireLockException`，造成已开始的数据库写入被重复执行。
- Good：调用方把完整 RWD 对象快照交给对象入口，把关系枚举和已归一化的关系主体对象交给独立关系入口；写服务统一投影对象与模型视图，并在内部转换关系资源参数。
- Good：云端和边缘端安装包都携带同版本、同内容的七表 Flyway，数据同步只负责传输表数据。
- Good：每个 classCode 在对象 Map 和模型视图 Map 中分别显式列出其完整 JSON 字段，写入时只取配置与 RWD 实际返回字段的交集。
- Good：照明、白名单和模板在原范围查询中请求单 classCode 完整字段；末端混合设备查询先计算目标设备类字段并集，再复用同一查询参数。
- Base：对象没有 `local_id`、`local_name`、建筑或楼层时，公共可空列保存 `NULL`，主记录仍可写入。
- Base：classCode 未配置时公共独立列仍正常写入，对象或模型视图 JSON 保存 `{}`。
- Base：模板对象明细存在记录且 `result_json = []` 时属于有效命中。
- Base：模板明细缺少某个复合业务键时仅该聚合使用空结果，其他聚合继续使用已查询记录。
- Good：末端 READY 且白名单关联非空时只读取白名单新键，旧表读取次数为零。
- Good：`/airTerminal/listSpaceEquipment` 不接收白名单范围开关，服务按 `SPACE_RANGE_TERMINAL` 统一选择空间 Key；缓存构建始终读取项目完整空间。
- Good：`/cacheRwd/listAirTerminalControlSpaces` 在当前模式为 `WHITE_LIST` 时仍返回项目全量候选范围，响应对象只有四个固定字段。
- Base：项目全量空间 Key 为空或实时计算无空间时返回空列表。
- Bad：候选接口复用当前白名单 Key，导致已经进入白名单模式后无法选择新的空间。
- Good：保存自定义白名单时先在事务外完成 RWD 查询和投影，再在同一短事务内替换新版 Key 并保存 `WHITE_LIST`。
- Good：首版升级时旧白名单 Key 有有效 ID，即使壳配置尚未持久化且查询默认值为 `FULL`，仍先迁移旧 ID 并原子补写 `WHITE_LIST`。
- Base：壳配置本地保存返回 `false` 时事务整体回滚，原壳配置和原新版 Key 均保持不变。
- Base：数据库未 READY 且新旧白名单 Key 都为空时没有迁移输入，继续按壳配置执行新版日常构建。
- Good：手动初始化能感知末端/照明写入故障，DTX 发布请求只留下可诊断日志且不被缓存故障打断。
- Base：手动任务未取得类型构建锁而收到 `false` 时只记录未完成，不把该类型记录为执行成功。
- Good：DTX 全量构建中的末端或照明失败不会阻止其他运行监测系统继续刷新，最终结果统一为 `false`。
- Good：末端或照明全部策略成功后，在同一构建锁内写数据库 READY、刷新 Redis，再释放锁。
- Good：末端和照明关系快照各关系方向只调用一次 RWD，客户端按 1000 条响应分页并发汇总。
- Base：RWD 返回的某个对象没有当前请求范围内的关系 ID 时保留对象空关系结果，不补查、不重试分片。
- Good：`project.cache.rwd.enabled = false` 时云端和边缘自动任务 Bean 都不注册，手动初始化仍复用领域构建入口。
- Base：Nacos 未配置 `project.cache.rwd.enabled` 时由 `matchIfMissing = true` 保持原自动恢复行为。
- Good：调用方只传失效模板 Key，删除服务从尚未删除的模板元数据取得类型并只清理该类型对应的存储路径。
- Good：信息点配置请求即使携带集团和项目头，公共表仍只在公共 schema 执行，缓存关联随后覆盖全部有效集团和项目。
- Base：单个项目缓存维护失败时公共配置已经生效，失败项目被记录，其他集团项目继续处理。
- Good：模板 Key-only 查询和规则关联双向查询分别命中对应联合索引，不回表扫描不需要的关联字段。
- Base：历史数据库存在 `FAILED` 时读取仍保持旧链路；下一次完整构建成功后覆盖为 READY。
- Good：末端空间从末端 Key 移除后，该 Key 查询不再返回该空间；对象、视图和无 Key 归属的关系记录可以保留。
- Base：末端 READY 且壳配置选择白名单、白名单键为空关联时返回空空间，不回退项目基础键。
- Base：对象-Key 关联存在重复行时按对象 ID 去重，仍返回当前查询到的关联。
- Bad：READY 后先读旧白名单表判断是否配置；旧表与新关联不同步时会选择错误范围。
- Bad：把 HTTP 请求中的白名单开关直接传入 `RunningMonitorSpaceQueryParam`，使前端可绕过已保存的空间范围配置。
- Bad：首版升级先读取缺失壳配置的默认 `FULL` 并清空新版 Key，导致旧白名单 ID 永久错过迁移。
- Bad：先独立提交新版白名单 Key，再在第二个事务保存模式；后一步失败会造成接口报错但部分数据已经生效。
- Bad：给 Controller 或包含 RWD/Feign 查询的完整白名单方法直接增加长事务。
- Bad：读取对象-Key 关联时重复执行写入校验，并因一条重复关联把整个批次返回为空。
- Bad：为共享对象重新生成 bigint ID，导致对象表与模型视图、关系表无法使用同一对象标识。
- Bad：根据 objType 自动为模型视图补充字段，或给设备类登记 `outline`，导致 classCode 字段契约失效。
- Bad：调用方只查询 `id/classCode`，再让公共写服务按同一批 ID 查询完整对象；这会给每个非空 Key 增加一次远程往返。
- Bad：只为规则关联建立缓存键索引，删除缓存键后按 `rule_id` 全表扫描引用。
- Bad：把模板明细的 `result_json` 声明为泛型 List 并交给 JacksonTypeHandler，读取后元素退化为 `LinkedHashMap`。
- Bad：分别使用模板键、对象 ID、信息点编码三个 IN 条件，返回调用方未请求的笛卡尔组合。
- Bad：单类型重建仅检查缓存键引用后调用关系级联删除，误删其他类型仍在使用的共享关系。
- Bad：把 `runningMonitorType` 作为第二个业务 Key 传入删除入口，或类型未知时同时删除新版和现有 RWD 缓存。
- Bad：继续用 `(project_id, running_monitor_type, custom_key)` 承担 Key-only 查询，或只给规则关联建立 `(project_id, cache_key)` 非覆盖索引。
- Bad：从同步 `@EventListener` 直接抛出缓存写入异常，导致 DTX 发布页面显示失败。
- Bad：用信息点配置请求中的 `ContextUtil.projectId` 删除一次缓存，导致其他项目继续使用旧模板。
- Bad：全量循环根据当前策略声明的类型决定是否重新抛出异常，导致构建顺序改变最终行为或提前跳过后续系统。
- Bad：领域构建返回 `false` 后仍打印类型执行成功，导致任务日志与 READY 事实不一致。
- Bad：手动初始化收到 `false` 后仍正常返回，导致 XXL-JOB 和集团日志把实际失败项目记为成功。
- Bad：策略返回 `false` 后调度层仍保持 `allSuccess=true`，导致本地事务已回滚但一级类型被误写 READY。
- Bad：在 Debug Controller 或 XXL-JOB Handler 中单独写 READY，导致 DTX 构建和其他完整构建漏掉状态切换。
- Bad：把开关字段注入 `NacosSwitchConfig` 并在 `run()`、`init()` 或 `scheduledConverge()` 中运行时判断；这会继续注册自动任务 Bean，也会造成错误的热刷新预期。
- Bad：因为云边数据同步由其他项目组负责，就只在云端增加 Flyway；边缘应用切到新模型后会查询不存在的表。
- Bad：在缓存写服务中同时按对象 ID 和关系 ID 做双层固定分片，把一次关系查询放大为多次串行 RWD 请求。
- Bad：把关系查询参数交给缓存写服务，让写服务再次访问 RWD 并隐式决定关系实际写入内容。

## 6. Tests Required

- `BaseInputIdDOTest`：断言输入 ID 保留，四个审计字段清空，返回当前实例。
- DDL 契约检查：断言七张表、七个主键、设计中的普通索引全部存在。
- DDL 禁止项检查：断言不存在外键、业务唯一约束和 `auto_increment`。
- 空值检查：断言对象 `local_id`、`local_name` 和视图 `local_name` 可空，`result_json` 非空。
- 基础列往返检查：断言 RWD 返回的 `localId`、`localName` 分别写入独立列，按字段请求从新表恢复同值，且 `object_data` 不重复保存这些字段。
- 状态列检查：断言对象表状态列为可空 `varchar(64)`，且对象扩展字段配置不包含 `monitoringSurveillanceStatus`。
- 状态读取检查：实体以单值字符串接收状态 code，RWD 未返回时保存 SQL `NULL`，DTO 重建时恢复为同名单值动态属性。
- 多值归属列检查：断言 RWD 返回逗号分隔字符串或 JSON 数组的 `buildingId`/`floorId` 时对应独立列写入第一个值（含单元素数组），单值原样保留，缺失为 SQL `NULL`，两个字段互不影响（`CacheObjectRecordConverterTest`、`CacheObjectModelViewRecordConverterTest`）。
- 字段配置检查：断言对象与模型视图使用两份独立的 classCode 完整字段 Map，未知 classCode 生成 `{}`，任何 objType 都不能扩大配置字段范围。
- 快照查询边界检查：断言单 classCode 入口使用对应完整字段、末端混合设备入口使用目标 classCode 字段并集；关系准备发生在写服务调用前，写服务没有 RWD 客户端依赖，对象与模型视图仍由同一组公共转换器投影。
- 调用方写入边界检查：断言对象快照直接决定 Key 对象范围；关系入口只接收关系枚举和对象集合，空对象集合表示不维护关系，单个对象的 `relObjIds` 为空仍被转换并提交以清理旧数据，并且调用方已查询的完整关系对象无需再次请求 RWD。
- 对象模型保存锁检查：断言四个对象/模型视图写入口使用同一项目级锁，投影先于锁获取，事务开始和提交/回滚均位于锁动作内；仅锁获取超时按 5 秒、10 秒退避重试，成功后事务动作只执行一次，Redis、线程中断和事务动作异常不重试。
- 关系完整范围检查：断言空主体、空目标和重复主体均在事务开始前失败；同一关系类型的正反向查询结果先按主体合并目标集合，再只提交一次完整替换。
- 模板明细转换检查：断言有序 DTO List 与任意深度 `relObjects` 可往返，所有层级均不存在 `relInfoCodeGroupConfigs`，源 DTO 保持不变。
- 模板明细命中检查：断言 `[]` 为有效命中，缺行不影响其他复合业务键结果，精确业务键查询不产生组合误命中。
- 异常边界检查：断言写入业务校验使用国际化 `BusinessException`，Converter 复用 fail-safe 且不自行捕获 Jackson 异常；读取不重复执行写入完整性校验，SQL/连接异常继续抛出。
- 模板范围替换检查：断言同键相同 JSON 去重、不同 JSON 删除前失败，空目标集合只清理当前模板键。
- 模板重建范围检查：断言单一级类型全量空目标会清理旧键，带设备类的窄范围空目标不访问旧键查询或清理；导入删除源模板前按旧模板 ID 调用缓存删除。
- 信息点公共库边界检查：携带集团、项目和租户上下文调用各导入/删除入口，断言公共 SQL 路由不使用这些值；模板导入和删除按删除前 ID 遍历全部有效集团项目，单项目失败不阻断后续项目。
- 模板 Key 身份检查：断言对外重建和删除只按 Key 定位范围，类型从项目内模板元数据取得且仅用于存储路由；缓存删除发生在模板元数据删除之前，不存在类型未知双路径兜底。
- 查询索引检查：断言模板元数据存在 `(project_id, custom_key)` 且旧 `idx_2` 已删除；规则关联存在两个列顺序相反的覆盖索引，并与 Mapper 的 Key 查询及孤儿引用检查一致。
- 对象-Key 关联读取检查：断言非空请求包含每个请求 Key、无关联时 value 为空列表、重复关联自动去重且不整批降级。
- 末端白名单 READY 路由检查：分别断言壳配置选择白名单和全量；请求参数不声明范围开关，白名单关联为空时返回空空间，只有配置选择全量时才读取项目基础键，READY 分支不得调用旧缓存。
- 末端全量候选接口检查：断言 READY 时只读 `AIR_TERMINAL_listSpace`，未 READY 时调用末端全量实时策略；两种模式均不读取当前白名单范围，且序列化字段严格为 `id`、`localName`、`floorId`、`buildingId`。
- 末端白名单复合事务检查：断言 CUSTOM 的 Key 替换和 `WHITE_LIST`、FULL/delete 的 `FULL` 和 Key 删除、通用壳配置完整保存和目标项目 Key 删除分别共用一个短事务；本地保存返回 `false` 或任一 SQL 抛出异常时两部分全部回滚。
- 末端白名单首版迁移检查：数据库未 READY 且新版 Key 为空时，旧白名单 Key 有有效 ID 即先于壳配置判断迁移，并原子补写 `WHITE_LIST`；旧行为空时才按当前壳配置处理；数据库 READY 后不得查询旧表。
- 末端白名单事务范围检查：断言 RWD/Feign 查询和投影发生在事务开始前，Controller 不声明事务，通用壳配置入口在事务前切换目标项目并在结束后恢复原上下文。
- 两端迁移检查：断言云端和边缘端都存在同版本脚本，且文件内容完全一致。
- 复合事务检查：断言同键对象、关系、明细和规则任一 SQL 失败时全部回滚，事务提交前项目级规则分配锁不释放。
- 重建异常边界检查：断言显式末端/照明初始化保留并上抛原始写入异常，DTX 事件监听器记录异常但不向发布方传播，普通 `false` 结果有告警。
- 全量异常隔离检查：断言 `buildCache(null)` 中末端、照明或历史系统任一失败后仍执行后续类型并返回 `false`；显式历史类型失败仍返回 `false`，显式末端、照明失败仍抛出原异常；手动任务收到 `false` 时继续后续类型并最终以国际化 `BusinessException` 标记当前项目失败。
- 项目终态日志检查：断言每个实际执行项目只产生一条缓存初始化终态日志，成功和失败都包含集团、项目、一级类型和耗时，失败额外包含可检索原因且不重复打印异常堆栈。
- 空 Key 替换检查：断言空结果只删除新版键关联，不访问旧版运行结果缓存，不查询一级类型对象模型上一版，也不扫描对象或模型视图主表；照明扩展只按当前 Key 上一版回路 ID 分方向删除关系，不枚举其他照明 Key。
- RWD 关系查询检查：传入超过 1000 个对象或关系 ID，断言每个方向只调用一次 `listObject`、查询参数包含完整范围、客户端多页结果完整聚合，并且写入关系只包含当前请求范围内的 `relObjIds`。
- READY 成功边界检查：断言单类型全部策略成功后，在构建锁释放前依次提交数据库 READY 和刷新 Redis；手动初始化、调试初始化和 DTX 构建均复用同一领域入口。
- READY 失败边界检查：断言任一策略返回 `false` 时调度结果为失败、不打印策略成功且从不调用 `markReady`；策略异常、SQL/事务异常同样不得写 READY，已有 READY 不因后续重建失败而降级。
- READY 传播异常检查：断言 groupCode/projectId 在数据库写入前校验；数据库提交失败不写 Redis，Redis 失败保留数据库 READY 并抛出国际化 `BusinessException`。
- READY 自动开关检查：反射断言两端任务类的 `prefix`、`name`、`havingValue`、`matchIfMissing` 完整契约，并使用轻量 Spring 上下文断言 `project.cache.rwd.enabled = false` 时云端、边缘任务 Bean 不注册；同时保留两端缺失 Key 批量恢复的原行为测试。
- 有可用隔离数据库时执行真实 Flyway 迁移，验证 MySQL JSON、索引长度和存储过程调用。

## 7. Wrong vs Correct

### AIR_TERMINAL Control Space Candidates

#### Wrong

```java
String key = useWhitelist
        ? CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE
        : CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE;
return BeanUtil.copyToList(cacheObjectReadService.listByCacheKey(key), DtpRwdSpaceDTO.class);
```

候选接口若服从当前白名单模式，用户只能看到已经选择的空间，无法扩展配置；返回通用 DTO 或
反射复制还会使接口字段随其他模型变化而扩大。

#### Correct

```java
List<DtpRwdSpaceDTO> spaces = cacheObjectReadService.listByCacheKey(
        CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE,
        DtpRwdSpaceDTO.class,
        AIR_TERMINAL_CONTROL_SPACE_FIELDS);
CacheRwdAirTerminalControlSpaceVO item = new CacheRwdAirTerminalControlSpaceVO();
item.setId(space.getId());
item.setLocalName(space.getLocalName());
item.setFloorId(space.getFloorId());
item.setBuildingId(space.getBuildingId());
```

READY 和未 READY 都使用项目全量管控空间语义；固定 VO 与显式赋值共同限制对外字段。

### InfoCode Public Context

#### Wrong

```java
infoCodeTemplateTypeResource.del(param, true);
cacheInfoCodeTemplateRepository.del(templateIds);
```

公共配置请求没有可信的集团和项目范围。直接发布项目缓存事件或读取当前 `ContextUtil`，最多只会
修改一个误传项目，其他项目仍保留旧缓存；事务开始前未清租户时还可能把公共表 SQL 路由到集团库。

#### Correct

```java
clearPublicContext();
Set<Long> oldTemplateIds = infoCodeRepository.delSheetConfig(param);
handlerProject(null, null, project ->
        cacheInfoCodeTemplateRepository.del(oldTemplateIds));
```

公共表增删先在无租户上下文中提交，并返回删除前捕获的模板 ID；随后显式无过滤遍历所有有效集团
和项目，在每个项目上下文中维护其缓存。项目异常只记录并继续，不把缓存故障伪装成公共配置失败。

### Cache Rebuild Scope

#### Wrong

```java
this.beginCompleteRebuild(runningMonitorType);
for (CacheRwdStrategy strategy : strategies) {
    strategy.buildCacheData(runningMonitorType);
}
this.completeRebuild(runningMonitorType);
```

跨 Key 保存上一版快照并在最后计算差集，会把普通重建扩大为全局对象清理；关系没有 Key 归属时
还需要额外推断共享范围，代码复杂且容易误删其他系统仍在使用的数据。

#### Correct

```java
List<DtpRwdObjectDTO> relationObjects =
        this.queryAndNormalizeRelationObjects(objectIds, relationType);
this.cacheObjectModelWriteService.replaceModelCacheKey(
        modelCacheKey, sourceObjects, writeActions);
this.cacheObjectModelWriteService.replaceObjectRelations(
        relationType, relationObjects);
```

每个 Key 的调用方先在事务外得到完整对象和关系结果。对象入口统一投影并替换 Key，关系入口按调用方
给出的关系类型和主体快照独立替换；每种关系类型分别调用一次。空对象结果只删除该 Key；已经不可达的
对象、模型视图和通用关系可以保留，因为业务读取先由 Key 关联限定直接对象范围。照明关系扩展
只按当前 Key 的新旧回路范围精确删除并重建，不读取其他 Key 的对象范围。

### Object Model Save Lock

#### Wrong

```java
retryTemplate.execute(context ->
        transactionTemplate.execute(status -> writeObjectRows()));
```

数据库事务或 Mapper 异常也会触发整段重试；UPSERT 幂等不能保证扩展动作和事务回调可重复执行。

#### Correct

```java
locker.executeWithLock(lockKey, 120,
        () -> transactionTemplate.execute(status -> writeObjectRows()),
        5000L, 10000L);
```

只有 `tryLock` 返回 `false` 才重新获取锁；拿到项目锁后数据库事务只执行一次，并在提交或回滚后释放锁。

### Cache Snapshot Query Fields

#### Wrong

```java
queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
List<DtpRwdObjectDTO> objectRefs = rwdService.listObject(queryParam);
List<DtpRwdObjectDTO> snapshots = rwdService.listObject(
        completeSnapshotQuery(objectRefs));
cacheObjectModelWriteService.replaceModelCacheKey(key, snapshots);
```

先查引用再补全会使每个非空缓存 Key 多一次 RWD 往返；照明多个独立 Key 还会重复查询同一类字段。

#### Correct

```java
Set<String> fields = new LinkedHashSet<>();
for (DtpRwdClassCodeEnum classCode : targetClassCodes) {
    fields.addAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(classCode.value()));
}
queryParam.addWithColumn(fields.toArray(new String[0]));
List<DtpRwdObjectDTO> snapshots = rwdService.listObject(queryParam);
List<DtpRwdObjectDTO> relationObjects = this.reuseOrQueryRelations(snapshots);
cacheObjectModelWriteService.replaceModelCacheKey(key, snapshots, actions);
cacheObjectModelWriteService.replaceObjectRelations(relationType, relationObjects);
```

单 classCode 入口的 `targetClassCodes` 只有一个元素；混合入口只在本地合并字段集合。调用方在事务外
准备或复用关系对象；公共写服务统一投影对象与模型视图，但不触发关系 RWD 查询。

### Whitelist Mode And Key Atomicity

#### Wrong

```java
cacheObjectModelWriteService.replaceModelCacheKey(whitelistKey, objects);
shellConfigService.save(groupCode, whiteListConfig);
```

两个调用各自提交时，第二步返回 `false` 或抛出异常会留下已经生效的新 Key；反向顺序则可能让
业务先切到尚未准备完成的白名单范围。把整个方法标为事务又会把前置 RWD 查询变成长事务。

#### Correct

```java
cacheObjectModelWriteService.replaceModelCacheKey(
        whitelistKey, sourceObjects, Collections.emptyList(),
        () -> shellConfigService.save(groupCode, whiteListConfig));
```

调用方先在事务外准备完整对象，再把纯本地壳配置写入作为替换后回调交给写服务；
`false` 显式标记回滚，异常继续传播，使模式和 Key 只可能一起提交或一起保留上一版。

### First Whitelist Migration Order

#### Wrong

```java
if (!this.isWhitelistSpaceRangeConfigured()) {
    return this.replaceWhitelist(key, Collections.emptyList());
}
this.migrateLegacyWhitelist(key);
```

历史项目没有独立壳配置时会得到默认 `FULL`。先判断壳配置会在读取旧白名单事实前直接清空新版
Key；随后写入 READY 后，旧对象 ID 永久失去迁移机会。

#### Correct

```java
List<String> objectIds = this.cacheObjectRepository.listObjectIdsByCacheKey(key);
if (CollectionUtils.isEmpty(objectIds) && this.shouldMigrateLegacyWhitelist()) {
    List<String> legacyObjectIds = this.cacheRwdDomainService
            .getByCustomIndexKey(key).stream()
            .map(CacheRwdObject::getObjectId)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(legacyObjectIds)) {
        return this.replaceWhitelist(
                key, legacyObjectIds, this::saveWhitelistSpaceRangeConfig);
    }
}
if (!this.isWhitelistSpaceRangeConfigured()) {
    return this.replaceWhitelist(key, Collections.emptyList());
}
return this.replaceWhitelist(key, objectIds);
```

数据库未 READY 且新版 Key 为空时，旧白名单行是首版迁移前的模式和范围事实。有效旧 ID 必须先
迁移，并与 `WHITE_LIST` 在一个短事务提交；只有没有迁移输入时才按当前壳配置进入新版日常逻辑。

### Template Key Routing

#### Wrong

```java
removeTemplateKeys(templateKeys, runningMonitorType);
removeTemplateKeys(unknownTypeKeys, AIR_TERMINAL);
removeTemplateKeys(unknownTypeKeys, LIGHTING);
```

调用方把类型作为第二个业务标识，或者类型未知时同时尝试多条存储路径，会扩大删除范围并让同一
Key 出现多套身份。

#### Correct

```java
removeTemplateKeys(templateKeys);
```

服务在模板元数据删除前按当前项目和 Key 查询一级类型，类型只决定执行哪条存储路径；缓存删除
完成后再按相同 Key 删除模板元数据。

### Cache Query Index Prefixes

#### Wrong

```sql
create index idx_2
    on cache_info_code_template (project_id, running_monitor_type, custom_key);
create index idx_cache_key
    on cache_rule_key_rel (project_id, cache_key);
```

第一个索引不能直接支持不传运行监测类型的 Key-only 查询；第二个索引查询 `rule_id` 时需要回表。

#### Correct

```sql
create index idx_custom_key
    on cache_info_code_template (project_id, custom_key);
create index idx_cache_key
    on cache_rule_key_rel (project_id, cache_key, rule_id);
```

索引前导列与 Mapper 的固定条件一致，规则关联查询所需字段也包含在索引中；没有实际查询条件的
设备类和对象类型不提前建立推测性索引。

### Cache Rebuild Event Boundary

#### Wrong

```java
@EventListener
public void onCacheBuildTrigger(CacheRwdBuildTriggerEvent event) {
    this.cacheRwdDomainService.buildCache(event.getRunningMonitorType());
}
```

同步事件会把缓存 SQL 故障抛回 DTX 发布请求，造成发布页面失败。

#### Correct

```java
@EventListener
public void onCacheBuildTrigger(CacheRwdBuildTriggerEvent event) {
    try {
        Boolean success = this.cacheRwdDomainService.buildCache(event.getRunningMonitorType());
        if (!Boolean.TRUE.equals(success)) {
            log.warn("Cache rebuild was not fully successful: {}", event.getRunningMonitorType());
        }
    } catch (RuntimeException exception) {
        log.error("Cache rebuild failed: {}", event.getRunningMonitorType(), exception);
    }
}
```

显式手动任务仍从领域服务收到原始异常；同步 DTX 事件在最终边界记录诊断信息并保护发布请求。

### READY Auto-Start Switch

#### Wrong

```java
public void run(String... args) {
    if (!Boolean.TRUE.equals(this.nacosSwitchConfig.getCacheRwdEnabled())) {
        return;
    }
    // 恢复 READY
}
```

运行时判断仍会创建 `CommandLineRunner`、`SystemInitService` 和 `@Scheduled` 承载 Bean，
也会让调用方误以为 `@RefreshScope` 刷新可以动态增删自动任务。

#### Correct

```java
@Component
@ConditionalOnProperty(prefix = "project.cache.rwd", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class CacheObjectModelStatusConvergenceTask {
}
```

云端恢复任务使用同样的类级条件。显式关闭时两个 Bean 都不进入应用上下文；
手动构建链路没有依赖该条件。由于条件只在上下文启动时计算，两端配置变更后都需重启。

### Full Rebuild Exception Boundary

#### Wrong

```java
if (statusService.isSupported(strategy.runningMonitorType())) {
    throw exception;
}
```

全量构建的外层策略不一定是实际失败策略，通用策略还会返回 `null`；异常是否传播会因此依赖
Spring Bean 遍历顺序，并可能提前终止后续系统。

#### Correct

```java
if (statusService.isSupported(requestedRunningMonitorType)) {
    throw exception;
}
allSuccess = false;
```

只有显式请求末端或照明时上抛原异常。请求类型为空或属于历史系统时记录失败并继续，保持全量
构建隔离和历史调用契约。

### Model View Storage Fields

#### Wrong

```java
Set<String> fields = new LinkedHashSet<>(MODEL_VIEW_BASE_FIELDS.get(objType));
fields.addAll(MODEL_VIEW_EXTENSION_FIELDS.get(classCode));
```

该写法让 objType 和 classCode 同时决定写入字段，新增或排查某个设备类时需要维护两个配置来源。

#### Correct

```java
Set<String> fields = MODEL_VIEW_FIELDS.getOrDefault(classCode, ImmutableSet.of());
```

每个 classCode 显式维护完整字段集合；未配置时写入空 JSON，不再根据 objType 推断字段。

### AIR_TERMINAL Whitelist READY Routing

#### Wrong

```java
List<CacheRwdObject> legacyWhitelist = this.cacheRwdDomainService
        .getByCustomIndexKey(whitelistKey);
if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL)) {
    return this.readNewModel(legacyWhitelist.isEmpty() ? projectKey : whitelistKey);
}
```

该写法在 READY 后仍依赖旧表决定读取范围，旧表与新关联状态不一致时会误选白名单或项目全量。

#### Correct

```java
boolean useWhitelist = this.isWhitelistSpaceRangeConfigured();
String selectedKey = useWhitelist ? whitelistKey : projectKey;
List<String> selectedIds = this.cacheObjectRepository
        .mapObjectIdsByCacheKey(selectedKey);
if (this.cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL)) {
    return this.readNewModel(selectedKey, selectedIds);
}
return useWhitelist
        ? this.querySpacesFromRwd(selectedIds)
        : this.queryProjectSpacesFromRwd();
```

壳配置先确定范围 Key；新关联表只表达该 Key 的对象范围。白名单 Key 为空时保持空结果，未 READY 分支按其中的配置 ID 实时查询 RWD，不读取旧表。

### AIR_TERMINAL Space Range Authority

#### Wrong

```java
spaceQueryParam.setUseWhiteList(true);
```

空间范围是持久化业务配置，不是前端筛选条件。请求值会让同一项目在相同配置下返回白名单或项目全量两种
互相矛盾的结果，并可能使缓存读取范围与初始化范围不一致。

#### Correct

```java
boolean useWhitelistSpaceRange = useCache
        && this.airTerminalQuerySpace.isWhitelistSpaceRangeConfigured();
return useWhitelistSpaceRange
        ? this.runningMonitorAirTerminalStrategy.listWhitelistSpaces(spaceQueryParam)
        : this.runningMonitorAirTerminalStrategy.listSpaces(spaceQueryParam);
```

仅 `useCache=true` 的业务查询读取 `SPACE_RANGE_TERMINAL`；`useCache=false` 的缓存构建明确保留
项目全量范围。两个空间查询参数均不持有范围控制字段。

### Wrong

```sql
create table cache_rule_key_rel (
    id bigint auto_increment primary key,
    rule_id bigint not null,
    unique key uk_rule (rule_id),
    foreign key (rule_id) references cache_rule(id)
);
```

这会破坏调用方稳定 ID、共享规则多键引用以及云边同步边界。

### Correct

```sql
create table if not exists cache_rule_key_rel (
    id bigint not null primary key,
    project_id varchar(50) not null,
    cache_key varchar(255) not null,
    rule_id bigint not null,
    create_time datetime(6) not null,
    create_user varchar(64) null,
    update_time datetime(6) not null,
    update_user varchar(64) null
);
create index idx_cache_key on cache_rule_key_rel (project_id, cache_key);
create index idx_rule on cache_rule_key_rel (project_id, rule_id, cache_key);
```

### Template Detail JSON Mapping

#### Wrong

```java
@TableField(typeHandler = JacksonTypeHandler.class)
private List<InfoCodeRwdObjectDetailDTO> resultJson;
```

当前 MyBatis-Plus TypeHandler 只持有原始 `List.class`，无法恢复泛型元素类型，读取结果会退化为 `List<LinkedHashMap>`。

#### Correct

```java
private String resultJson;

private static final TypeReference<List<InfoCodeRwdObjectDetailDTO>> RESULT_TYPE =
        new TypeReference<List<InfoCodeRwdObjectDetailDTO>>() {
        };
```

实体和 Mapper 只负责 JSON 字符串，独立转换器使用项目 ObjectMapper 与固定 TypeReference 完成类型安全的序列化和反序列化。

### Cache Read Exception Boundary

#### Wrong

```java
try {
    List<CacheTemplateObjectDetail> details = this.detailService.listByBusinessKeys(param);
    return this.convert(details);
} catch (Exception exception) {
    return Collections.emptyMap();
}
```

该写法会把数据库不可用伪装成普通缓存未命中，也无法区分读取降级和基础设施故障。

#### Correct

```java
List<CacheTemplateObjectDetail> details = this.detailService.listByBusinessKeys(param);
Map<BusinessKey, Result> result = new LinkedHashMap<>();
for (Detail detail : details) {
    result.put(BusinessKey.of(detail), this.converter.toResult(detail));
}
return result;
```

查询先完成并按实际返回行组装结果，不检查请求键是否全部命中；写入路径在删除前检查转换结果并通过 `BusinessException` 阻止无效替换。

### RWD Relation Query Batching

#### Wrong

```java
for (List<String> objectIds : Lists.partition(allObjectIds, 500)) {
    for (List<String> relationIds : Lists.partition(allRelationIds, 500)) {
        result.addAll(this.dtpRwdClientService.listObject(
                relationQuery(objectIds, relationIds)));
    }
}
```

业务层双重分片会把一次完整关系查询放大成分片数量的笛卡尔积；每次调用仍会进入客户端分页，
增加串行远程往返和重复结果合并。

#### Correct

```java
DtpRwdObjectQueryParam query = relationQuery(allObjectIds, allRelationIds);
List<DtpRwdObjectDTO> result = this.dtpRwdClientService.listObject(query);
```

每个关系方向一次提交完整范围，由客户端统一处理响应分页和并发。若物理世界以后限制请求条件数量，
只在 RWD 客户端层集中拆分，并按对象 ID 合并完整关系集合。

### Cloud/Edge Flyway Placement

#### Wrong

```text
saas/meos-control-server/.../V7.260701.0.1__cache_object_model.sql
saas/meos-control-server/.../V7.260701.0.2__cache_object_base_fields.sql
```

只发布云端迁移会把“数据同步归属”误当成“边缘表结构归属”。同步可以把数据写到边缘，但不能
保证边缘应用安装包具备本项目新增表的 DDL。

#### Correct

```text
saas/meos-control-server/.../V7.260701.0.1__cache_object_model.sql
saas/meos-control-server/.../V7.260701.0.2__cache_object_base_fields.sql
local/meos-control-server-edge/.../V7.260701.0.1__cache_object_model.sql
local/meos-control-server-edge/.../V7.260701.0.2__cache_object_base_fields.sql
```

两份迁移保持同名、同版本、同内容；本项目维护两端结构，云边同步项目组继续负责数据同步登记、
传输顺序和完成状态。
