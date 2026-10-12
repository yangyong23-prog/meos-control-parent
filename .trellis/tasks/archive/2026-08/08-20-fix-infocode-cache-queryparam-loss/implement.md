# 执行计划：模板缓存坏快照自愈修复

## 实施清单（按序执行）

### 1. 修复读取侧（R1）

文件：[InfoCodeTemplateTypeModel.java](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/structure/infocodeconfig/model/InfoCodeTemplateTypeModel.java#L461-L467)（`parseFilterValueObject` 缓存读取分支）

- 在 `.map(cacheTemplate -> Optional.ofNullable(cacheTemplate.getDataJson())...)` 链中加入 `.filter(dataJson -> Objects.nonNull(dataJson.getQueryParam()))`。
- 不修改 supplier、`handleFilterObjectQueryParamWithSource`、redis 兜底分支。

### 2. 修复写入侧（R2）

文件：[CacheInfoCodeTemplate.java](file:///d:/IdeaProjects/meos-control-parent/common/src/main/java/com/persagy/apccontrol/basic/model/entity/cache/CacheInfoCodeTemplate.java#L75-L84)（`from` 方法）

- `parseFilterValueObject()` 结果先存局部变量；`queryParam` 为 null 或结果为 null 时写入 `dataJson = null`。
- 新增 `import java.util.Objects;`。

### 3. 单元测试

新建测试类（参考 [DebugToolServiceImplTest.java L41-56](file:///d:/IdeaProjects/meos-control-parent/common/src/test/java/com/persagy/apccontrol/basic/service/debugtool/impl/DebugToolServiceImplTest.java#L41-L56) 的 `StaticApplicationContext` + `SpringUtil` 模式）：

- `structure/infocodeconfig/model/InfoCodeTemplateTypeModelCacheReadTest`：
  - `@BeforeAll` 注册 beans：`objectMapper`、`dtpRwdConvert`、`dtpRwdClientService`、`abstractIotDataCacheServer`、`infoCodeGateway`、`redisCacheTemplate`、`cacheInfoCodeTemplateRepository`、`CacheObjectModelStatusService`（均为 mock，`objectMapper` 用真实实例），并 `new SpringUtil().setApplicationContext(...)`。
  - 用例1（AC1）：mock `getByKeyOpt` 返回 `dataJson.queryParam = null` 的行；`dtpRwdClientService.listObject` 返回含 id 的对象；调用 `parseFilterValueObject()`；断言返回对象 `queryParam` 非 null 且 `listObject` 被调用。
  - 用例2（AC2）：mock `getByKeyOpt` 返回 `dataJson.queryParam` 非 null 的行；调用 `parseFilterValueObject()`；断言返回对象即缓存对象且 `listObject` 未被调用。
  - 模型构造：`runningMonitorType = LIGHTING`、`templateType = TABLE_LIST`（`allowCache()` 为 true），`infoCodeFilter` 为含 `uniqueKey`、`queryParam`、`source` 的 JSON。
- `structure/cacherwd/repository/CacheInfoCodeTemplateFromTest` 或并入现有 [CacheInfoCodeTemplateRepositoryImplTest.java](file:///d:/IdeaProjects/meos-control-parent/common/src/test/java/com/persagy/apccontrol/basic/structure/cacherwd/repository/CacheInfoCodeTemplateRepositoryImplTest.java)：
  - `ContextUtil.setProjectId("project-1")` + mock `InfoCodeTemplateTypeModel`（`parseFilterValueObject()` 可 mock，`getTemplateTypeKey()` 等用真实值）。
  - 用例3（AC3）：`parseFilterValueObject()` 返回 `queryParam = null` 的对象 → 断言 `from()` 结果 `dataJson` 为 null。
  - 用例4（AC3）：返回 `queryParam` 非 null 的对象 → 断言 `dataJson` 原样保留。
  - 清理：`ContextUtil.remove()`。

### 4. 验证

- `mvn -pl common -am test -Dtest=InfoCodeTemplateTypeModelCacheReadTest,CacheInfoCodeTemplateRepositoryImplTest` 或 IDEA 运行对应测试类。
- `GetProblems` 检查两个修改文件无编译/告警问题。
- 走 `trellis-check`（或按 [.trellis/spec/backend/index.md](file:///d:/IdeaProjects/meos-control-parent/.trellis/spec/backend/index.md) 的 MUST FOLLOW 规则自查：无 SQL 拼接、无空 catch、日志走 LogComponent 等）。

### 5. 生产验收（AC4，部署后执行）

1. `UPDATE cache_info_code_template SET data_json = NULL WHERE custom_key = 'PUBLIC_AREA_LIGHTING-001' AND project_id = 'Pj3501040006';`
2. 触发模板重建，查 `data_json` 是否出现 `queryParam.id`（约 507 个 id）。
3. 再次重建，确认 `data_json` 保持正确（不再被坏快照覆盖）。

## 回滚点

- 步骤 1/2 完成前：随时可放弃，无任何影响。
- 步骤 1/2 完成后、测试前：两处改动互相独立，可分别 revert。
- 部署后回滚：git revert 即可；数据侧无需回滚（坏行不会因代码还原而恶化）。

## 前置检查（start 前）

- [x] prd.md 收敛完成（无遗留临时章节、无阻塞问题）
- [x] design.md / implement.md 就绪
- [ ] implement.jsonl / check.jsonl 含真实条目
- [ ] 用户批准最终规划摘要
