# Tasks: Cache Build Event Listener

## Implementation Tasks

- [x] 创建事件类 `CacheRwdBuildTriggerEvent`
  - 位置：`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/event/CacheRwdBuildTriggerEvent.java`
  - 继承 `ApplicationEvent`
  - 包含 `runningMonitorType` 字段

- [x] 创建事件监听器 `CacheRwdBuildEventListener`
  - 位置：`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/event/CacheRwdBuildEventListener.java`
  - 使用 `@Component` 注解
  - 使用 `@EventListener` 注解监听事件
  - 使用 `@Async` 注解实现异步执行
  - 注入 `CacheRwdDomainService` 调用 `buildCache()` 方法

- [x] 确认异步配置
  - 检查项目是否已启用 `@EnableAsync`
  - 如未启用，在配置类中添加 `@EnableAsync` 注解

- [x] 编写单元测试
  - 测试事件发布和监听流程
  - 验证异步执行不阻塞主线程

## Verification

- [ ] 启动应用，确认无编译错误
- [ ] 发布事件后，验证缓存构建被异步触发
- [ ] 验证分布式锁正常工作，避免并发构建
