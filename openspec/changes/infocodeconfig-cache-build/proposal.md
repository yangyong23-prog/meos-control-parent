# Proposal: Infocodeconfig Cache Build

## Why

信息点配置导入接口（`importTableSheet`）目前只完成数据入库，不会触发相关缓存的重建。运维人员在导入新配置后，需要手动触发缓存构建或等待定时任务执行，影响配置变更的生效时效性。

导入接口操作的是公共库，而缓存数据存储在集团库中，需要跨库触发缓存重建，确保配置变更后各集团/项目的缓存数据能够及时同步更新。

## What Changes

- **新增方法**：`CacheInfoCodeTemplateRepository.rebuildByParam(InfoCodeTemplateTypeQueryDaoParam param)`
    - 根据查询参数重建缓存，内部执行删除旧缓存 + 新增新缓存，触发事件链路

- **修改方法**：`InfoCodeTemplateTypeResource.add()` 和 `del()`
    - 增加 `publishEvent` 参数，控制是否发布事件
    - 保持向后兼容，默认发布事件

- **修改方法**：`InfoCodeRepository.addSheetConfig()`
    - 调用 `add()` 和 `del()` 时传入 `publishEvent = false`
    - 导入时只操作公共库数据，不触发缓存事件

- **重构方法**：`AbstractCacheRwdInfoCodeConfigStrategy.buildCacheData()`
    - 提取 `del + add` 逻辑到 `rebuildByParam` 方法
    - 保持原有缓存构建功能不变

- **新增功能**：`InfoCodeImportServiceImpl.importTableSheet()` 触发缓存重建
    - 使用内部类 `CacheRebuildExecutor extends BaseTask`
    - 集团并行执行，项目串行执行
    - 同步等待所有缓存重建完成后再返回
    - 返回失败项目信息，全部成功返回 OK

## Capabilities

### New Capabilities

- `infocode-import-cache-rebuild`: 信息点配置导入后自动触发跨库缓存重建

### Modified Capabilities

- `infocode-import`: 导入接口返回值扩展，增加缓存重建结果

## Impact

- **修改文件**:
    - `CacheInfoCodeTemplateRepository.java` - 新增 `rebuildByParam` 方法
    - `CacheInfoCodeTemplateRepositoryImpl.java` - 实现 `rebuildByParam` 方法
    - `InfoCodeTemplateTypeResource.java` - `add()` 和 `del()` 方法增加 `publishEvent` 参数
    - `InfoCodeTemplateTypeResourceImpl.java` - 实现事件发布条件判断
    - `InfoCodeRepositoryImpl.java` - `addSheetConfig()` 调用时传入 `publishEvent = false`
    - `AbstractCacheRwdInfoCodeConfigStrategy.java` - 重构 `buildCacheData` 方法
    - `InfoCodeImportServiceImpl.java` - 新增内部类 `CacheRebuildExecutor`，修改 `importTableSheet` 方法

- **影响范围**:
    - 信息点配置导入接口
    - 缓存构建流程（保持向后兼容）
    - 事件链路（复用现有事件，无需新增）

- **数据库**: 无新增表，使用现有 `cache_info_code_template`、`cache_rwd_object`、`cache_iot_rule` 表
