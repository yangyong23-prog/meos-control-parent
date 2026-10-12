## 1. InfoCodeTemplateTypeResource 事件发布控制

- [x] 1.1 在 `InfoCodeTemplateTypeResource.java` 接口中新增带 `publishEvent` 参数的 `add()` 和 `del()` 方法
- [x] 1.2 在 `InfoCodeTemplateTypeResourceImpl.java` 中实现 `add(config, publishEvent)` 和 `del(param, publishEvent)` 方法
- [x] 1.3 在 `InfoCodeImportService.java` 接口中同步修改 `del()` 方法签名
- [x] 1.4 在 `InfoCodeImportServiceImpl.java` 中实现 `del(param, publishEvent)` 方法
- [x] 1.5 在 `InfoCodeImportController.java` 中调用时传入 `publishEvent = true`

## 2. InfoCodeRepository 导入时不发布事件

- [x] 2.1 修改 `InfoCodeRepositoryImpl.addSheetConfig()` 方法
- [x] 2.2 调用 `infoCodeTemplateTypeResource.del()` 时传入 `publishEvent = false`
- [x] 2.3 调用 `infoCodeTemplateTypeResource.add()` 时传入 `publishEvent = false`

## 3. CacheInfoCodeTemplateRepository 新增 rebuildByParam 方法

- [x] 3.1 在 `CacheInfoCodeTemplateRepository.java` 接口中新增 `rebuildByParam(InfoCodeTemplateTypeQueryDaoParam param)`
  方法签名
- [x] 3.2 在 `CacheInfoCodeTemplateRepositoryImpl.java` 中实现 `rebuildByParam()` 方法
- [x] 3.3 实现查询符合条件的模版类型逻辑
- [x] 3.4 实现删除旧缓存并发布 RemoveEvent 逻辑
- [x] 3.5 实现新增新缓存并发布 SaveEvent 逻辑

## 4. AbstractCacheRwdInfoCodeConfigStrategy 重构

- [x] 4.1 重构 `buildCacheData()` 方法，调用 `rebuildByParam()` 替代原有逻辑
- [x] 4.2 确保重构后保持原有缓存构建功能不变

## 5. InfoCodeImportServiceImpl 触发缓存重建

- [x] 5.1 创建内部类 `CacheRebuildExecutor extends BaseTask`
- [x] 5.2 实现集团并行执行逻辑
- [x] 5.3 实现项目串行执行逻辑
- [x] 5.4 实现上下文设置 (`ContextUtil.setGroupCode/setProjectId`)
- [x] 5.5 实现项目级 try-catch 失败隔离逻辑
- [x] 5.6 实现失败项目收集和日志记录
- [x] 5.7 实现 `CompletableFuture.allOf().join()` 等待逻辑
- [x] 5.8 修改 `importTableSheet()` 方法，在导入完成后触发缓存重建
- [x] 5.9 扩展返回值结构，添加 `_cacheRebuildFailures` 字段

## 6. 测试验证

- [x] 6.1 编译验证所有修改文件无语法错误
- [ ] 6.2 验证导入接口正常工作（无缓存重建时的基本功能）
- [ ] 6.3 验证缓存重建正确触发事件链路
- [ ] 6.4 验证返回值格式正确（成功和失败场景）
