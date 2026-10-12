# Design: Cache Build Event Listener

## Architecture

```
业务服务
    │
    │ 发布 CacheRwdBuildTriggerEvent
    ▼
Spring ApplicationEventPublisher
    │
    │ 广播事件
    ▼
CacheRwdBuildEventListener
    │
    │ @Async 异步执行
    │ @EventListener
    ▼
CacheRwdDomainService.buildCache()
    │
    │ 分布式锁保护
    ▼
CacheRwdStrategy.buildCacheData()
```

## Event Design

### 事件类：CacheRwdBuildTriggerEvent

```java
package com.persagy.apccontrol.basic.structure.cacherwd.event;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

/**
 * 缓存构建触发事件
 */
@Getter
public class CacheRwdBuildTriggerEvent extends ApplicationEvent {

    /**
     * 运行监测类型，为空时构建所有类型缓存
     */
    private final @Nullable RunningMonitorTypeEnum runningMonitorType;

    public CacheRwdBuildTriggerEvent(@Nullable RunningMonitorTypeEnum runningMonitorType) {
        super(runningMonitorType);
        this.runningMonitorType = runningMonitorType;
    }
}
```

### 监听器：CacheRwdBuildEventListener

```java
package com.persagy.apccontrol.basic.structure.cacherwd.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 缓存构建事件监听器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheRwdBuildEventListener {

    private final CacheRwdDomainService cacheRwdDomainService;

    @Async
    @EventListener
    public void onCacheBuildTrigger(CacheRwdBuildTriggerEvent event) {
        log.info("收到缓存构建触发事件: runningMonitorType={}", event.getRunningMonitorType());
        cacheRwdDomainService.buildCache(event.getRunningMonitorType());
    }
}
```

## Usage Example

### 在业务服务中发布事件

```java
@Service
@RequiredArgsConstructor
public class SomeBusinessService {

    private final ApplicationEventPublisher eventPublisher;

    public void onBusinessDataChanged() {
        // 业务逻辑处理...
        
        // 发布缓存构建事件（异步执行，不等待结果）
        eventPublisher.publishEvent(new CacheRwdBuildTriggerEvent(null));
        
        // 或者指定特定类型
        // eventPublisher.publishEvent(new CacheRwdBuildTriggerEvent(RunningMonitorTypeEnum.AIR_TERMINAL));
    }
}
```

## Configuration

### 启用异步支持

在配置类上添加 `@EnableAsync` 注解（如果尚未启用）：

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    // 可选：配置异步线程池
}
```

## Implementation Details

### 1. 事件类位置

`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/event/`

### 2. 监听器位置

`common/src/main/java/com/persagy/apccontrol/basic/structure/cacherwd/event/`

### 3. 异步执行说明

- 使用 `@Async` 注解实现异步执行
- 事件发布后立即返回，不等待缓存构建完成
- 缓存构建过程由现有分布式锁保护，避免并发问题

### 4. 错误处理

- 监听器内部捕获异常，记录日志
- 不影响主业务流程
- 依赖 `CacheRwdDomainService.buildCache()` 内部的锁机制和错误处理
