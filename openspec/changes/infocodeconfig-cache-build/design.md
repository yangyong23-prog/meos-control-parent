# Design: Infocodeconfig Cache Build

## Context

### 背景

信息点配置导入接口（`importTableSheet`）目前只完成公共库数据入库，不会触发相关缓存的重建。运维人员在导入新配置后，需要手动触发缓存构建或等待定时任务执行。

### 当前状态

```
┌─────────────────────────────────────────────────────────────────────┐
│  现有数据流                                                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  importTableSheet()                                                 │
│         │                                                           │
│         │ 调用 addSheetConfig()                                    │
│         ▼                                                           │
│  InfoCodeTemplateTypeResource.add()                                │
│         │                                                           │
│         │ 发布 InfoCodeTemplateTypeSaveEvent                       │
│         ▼                                                           │
│  CacheInfoCodeTemplateEventListener                                │
│         │                                                           │
│         │ ContextUtil.getProjectId() = null (公共库无项目上下文)   │
│         ▼                                                           │
│  cache_info_code_template 写入失败/数据异常                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 约束

1. **跨库操作**：导入接口操作公共库，缓存数据存储在集团库
2. **多租户**：需遍历所有集团和项目，每个集团库独立存储缓存数据
3. **事件链路**：复用现有事件链路，无需新增事件类型
4. **向后兼容**：保持现有缓存构建流程不变

## Goals / Non-Goals

**Goals:**

1. 导入接口完成后自动触发跨库缓存重建
2. 集团并行执行，项目串行执行
3. 同步等待所有缓存重建完成后再返回结果
4. 仅返回失败项目信息，全部成功返回 OK
5. 避免事件重复触发

**Non-Goals:**

1. 不修改现有事件链路结构
2. 不新增数据库表
3. 不影响其他调用 `InfoCodeTemplateTypeResource.add()/del()` 的功能

## Decisions

### Decision 1: 事件发布控制策略

**选择**: 方案 C - 条件参数化

**方案对比**:

| 方案    | 描述                        | 优点            | 缺点          |
|-------|---------------------------|---------------|-------------|
| A     | 新增 `addWithoutEvent()` 方法 | 清晰分离          | 方法数量增加      |
| B     | 事件监听器检查 projectId         | 无需修改 Resource | 逻辑分散，隐式行为   |
| **C** | **增加 `publishEvent` 参数**  | **灵活控制，接口简洁** | **需修改接口签名** |

**实现方式**:

```java
// InfoCodeTemplateTypeResource.java - 仅保留带 publishEvent 参数的方法
InfoCodeTemplateType add(@NonNull InfoCodeTemplateType config, boolean publishEvent);

boolean del(@NonNull InfoCodeTemplateTypeRemoveDaoParam param, boolean publishEvent);
```

// 调用方必须显式传入参数值
// 导入接口：publishEvent = false
// Controller 删除接口：publishEvent = true

**API 设计原则**: 接口签名保持简洁，避免不必要的重载和默认参数

### Decision 2: 缓存重建执行策略

**选择**: 集团并行 + 项目串行

**原因**:

- 集团间数据独立，可并行执行提高效率
- 项目串行执行避免数据库连接池耗尽
- 同步等待确保返回结果完整

**实现方式**: 内部类 `CacheRebuildExecutor extends BaseTask`，复用 BaseTask 框架

**参数传递**:

- 从 `importTableSheet` 收集所有成功导入的 `InfoCodeImportContext`
- 每个 context 包含：`runningMonitorType`、`runningMonitorSecondType`、`templateType`、`classCodes`
- 根据上下文构造 `InfoCodeTemplateTypeQueryDaoParam`，只重建相关模版类型

```java
private class CacheRebuildExecutor extends BaseTask {

    private final List<InfoCodeImportContext> contexts;
    private final Map<String, String> failedProjects = new ConcurrentHashMap<>();

    public CacheRebuildExecutor(List<InfoCodeImportContext> contexts) {
        this.contexts = contexts;
    }

    public Map<String, String> execute() {
        if (CollectionUtils.isEmpty(contexts)) {
            return failedProjects;
        }
        try {
            // 复用 BaseTask.asyncHandlerGroup()，集团并行执行
            asyncHandlerGroup(null, group -> {
                // 获取集团下所有项目
                List<ProjectDTO> projects = midPermissionClientService
                        .listProjectOfValid(false, false, group.getGroupCode(), null);

                if (CollectionUtils.isEmpty(projects)) {
                    return;
                }
                // 项目串行执行
                for (ProjectDTO project : projects) {
                    executeProject(group.getGroupCode(), project);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return failedProjects;
    }

    private void executeProject(String groupCode, ProjectDTO project) {
        try {
            ContextUtil.setProjectId(project.getProjectId());
            // 根据导入上下文构造查询参数，逐个重建
            for (InfoCodeImportContext context : contexts) {
                InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
                queryDaoParam.setRunningMonitorType(context.getRunningMonitorType());
                queryDaoParam.setRunningMonitorSecondType(context.getRunningMonitorSecondType());
                queryDaoParam.setTemplateType(context.getTemplateType());
                // 设置设备类编码列表
                Set<String> classCodes = context.getSheetContexts().stream()
                        .map(InfoCodeSheetImportContext::getClassCode)
                        .collect(Collectors.toSet());
                queryDaoParam.setClassCodes(classCodes);
                cacheInfoCodeTemplateRepository.rebuildByParam(queryDaoParam);
            }
            // 使用 LogComponent 接口的 logProjectInstance() 记录日志
            this.logProjectInstance(project.getProjectId()).info("项目缓存重建成功",
                    Pair.of("projectId", project.getProjectId()));
        } catch (Exception e) {
            String failureKey = groupCode + ":" + project.getProjectId();
            failedProjects.put(failureKey, e.getMessage());
            // 使用 LogComponent 接口的 logProjectInstance() 记录错误日志
            this.logProjectInstance(project.getProjectId()).error("项目缓存重建失败", e);
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-缓存重建执行器";
    }
}
```

**框架复用原则**:

- 继承 `BaseTask` 复用集团/项目遍历框架
- 使用 `asyncHandlerGroup()` 实现集团并行执行
- 内部项目串行执行，避免连接池耗尽
- 通过 `LogComponent.logProjectInstance()` 统一日志输出

### Decision 3: 返回值结构

**选择**: 扩展现有 Map 结构，仅返回失败项目

**格式**:

```json
// 全部成功
{
  "file1.xlsx": [
    "OK"
  ],
  "file2.xlsx": [
    "OK"
  ]
}

// 部分失败
{
  "file1.xlsx": [
    "OK"
  ],
  "file2.xlsx": [
    "OK"
  ],
  "_cacheRebuildFailures": {
    "group1:project1": "缓存构建失败: xxx",
    "group2:project3": "连接超时"
  }
}
```

### Decision 4: rebuildByParam 方法设计

**接口签名**:

```java
void rebuildByParam(@NonNull InfoCodeTemplateTypeQueryDaoParam param);
```

**实现逻辑**:

```java

@Override
public void rebuildByParam(@NonNull InfoCodeTemplateTypeQueryDaoParam param) {
    // 1. 查询符合条件的模版类型
    List<InfoCodeTemplateTypeModel> templateTypeModels =
            infoCodeGateway.listTemplateTypes(param);
    templateTypeModels.removeIf(m -> !m.allowCache());

    if (CollectionUtils.isEmpty(templateTypeModels)) {
        return;
    }

    Set<Long> templateTypeIds = templateTypeModels.stream()
            .map(InfoCodeTemplateTypeModel::getTemplateTypeId)
            .collect(Collectors.toSet());

    // 2. 删除旧缓存（发布 RemoveEvent）
    this.del(templateTypeIds);

    // 3. 新增新缓存（发布 SaveEvent）
    templateTypeIds.forEach(this::add);
}
```

## Risks / Trade-offs

### Risk 1: 大规模项目遍历超时

**风险**: 集团/项目数量多时，接口响应时间可能超过 HTTP 超时限制

**缓解措施**:

- 考虑后续版本引入异步机制（先返回，后异步执行）
- 当前版本记录详细日志，便于排查问题
- 建议运维人员在项目数量少的时段执行导入

### Risk 2: 部分项目缓存重建失败

**风险**: 某些项目因网络、数据库等问题导致缓存构建失败

**缓解措施**:

- 项目级失败隔离，不影响其他项目
- 返回值中明确标识失败项目
- 保留详细错误日志

### Risk 3: 并发控制

**风险**: 多集团并行执行可能对数据库造成压力

**缓解措施**:

- 使用现有线程池 `BusinessThreadPoolExecutor.ttlExecutor()`
- 线程池大小已在系统中配置，不会无限扩展

## Migration Plan

### 部署步骤

1. 部署代码变更
2. 验证导入接口正常工作
3. 验证缓存数据正确写入

### 回滚策略

1. 代码回滚即可，无数据迁移
2. 旧版本导入接口不触发缓存重建，保持原有行为

### 验证方法

1. 执行导入接口
2. 检查返回值中的缓存重建结果
3. 查询各集团库的缓存表确认数据正确
