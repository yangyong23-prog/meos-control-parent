# Redis 缓存使用规范

## 概述

本项目强制要求所有 Redis 缓存操作必须通过 `RedisCacheTemplate` 进行，以确保缓存操作的统一性、可维护性和可观测性。

## Requirement: 缓存操作接口

所有 Redis 缓存操作 SHALL 通过 `RedisCacheTemplate` 进行，禁止直接使用 `RedisTemplate`、`RedisCacheUtil` 或其他 Redis 操作类。

### Scenario: 依赖注入

- **WHEN** 类需要进行 Redis 缓存操作
- **THEN** 该类 SHALL 通过构造器注入或 `@Resource` 注入 `RedisCacheTemplate`
- **AND** 禁止通过 `SpringUtil.getBean()` 静态获取实例

### Scenario: 对象缓存操作

- **WHEN** 需要存储或读取对象缓存
- **THEN** 系统 SHALL 使用 `redisCacheTemplate.object().get/set()` 方法

### Scenario: Hash 缓存操作

- **WHEN** 需要存储或读取 Hash 结构缓存
- **THEN** 系统 SHALL 使用 `redisCacheTemplate.hash().get/set()` 方法

### Scenario: 缓存过期时间

- **WHEN** 设置缓存
- **THEN** 系统 SHALL 使用 `Duration` 或 `TimeUnit` 指定过期时间
- **AND** 过期时间 SHALL 根据业务需求合理设置

### Scenario: 缓存删除

- **WHEN** 需要删除缓存
- **THEN** 系统 SHALL 使用 `redisCacheTemplate.del()` 方法
- **AND** 对于模糊匹配删除，SHALL 使用 `redisCacheTemplate.fuzzyMatchingDel()` 方法

### Scenario: 热刷新缓存

- **WHEN** 需要实现缓存自动刷新
- **THEN** 系统 SHALL 使用 `redisCacheTemplate.asyncRefreshTemplate()` 方法
- **AND** 传递方法名称、参数和执行方法

## Requirement: Key 命名规范

### Scenario: Key 定义位置

- **WHEN** 定义 Redis Key
- **THEN** Key SHALL 定义在 `com.persagy.apccontrol.basic.model.base.CacheKey` 及其相关接口中
- **AND** 动态缓存 Key SHALL 定义在 `CacheKey.Dynamic` 或 `CacheDynamicKey` 中
- **AND** 持久化缓存 Key SHALL 定义在 `CacheKey.Persistence` 或 `CachePersistenceKey` 中
- **AND** 锁缓存 Key SHALL 定义在 `CacheKey.Lock` 或 `CacheLockKey` 中

### Scenario: Key 分隔符

- **WHEN** 构建 Key 字符串
- **THEN** 分隔符 SHALL 使用 `CacheKeyPrefix.SEPARATOR`（即 `::`）
- **AND** Key 格式 SHALL 为 `{模块}::{业务标识}`

### Scenario: Key 构建方法

- **WHEN** 需要动态构建 Key
- **THEN** 系统 SHALL 在相应的 Key 接口中添加静态方法或常量
- **AND** 禁止在业务代码中直接拼接 Key 字符串

## Requirement: 禁止的缓存方式

### Scenario: 禁止直接使用 RedisTemplate

- **WHEN** 需要 Redis 操作
- **THEN** 系统 SHALL NOT 直接使用 `redisTemplate.opsForValue()` 等原生方法

### Scenario: 禁止使用 RedisCacheUtil

- **WHEN** 需要 Redis 操作
- **THEN** 系统 SHALL NOT 使用 `RedisCacheUtil`（旧工具类）

### Scenario: 禁止静态获取实例

- **WHEN** 需要 Redis 操作
- **THEN** 系统 SHALL NOT 使用 `SpringUtil.getBean(RedisCacheTemplate.class)` 静态获取

## 示例代码

### 基本使用

```java
@Component
@RequiredArgsConstructor
public class MyCacheService {

    private final RedisCacheTemplate redisCacheTemplate;

    // 设置缓存
    public void setCache(String key, Object value) {
        redisCacheTemplate.object().set(key, value, Duration.ofHours(1));
    }

    // 获取缓存
    public Object getCache(String key) {
        return redisCacheTemplate.object().get(key);
    }

    // 删除缓存
    public void deleteCache(String key) {
        redisCacheTemplate.del(key);
    }

    // Hash 操作
    public void setHash(String key, String field, Object value) {
        redisCacheTemplate.hash().set(key, field, value);
    }

    public Object getHash(String key, String field) {
        return redisCacheTemplate.hash().get(key, field);
    }
}
```

### 热刷新缓存

```java
// 使用 asyncRefreshTemplate 实现缓存自动刷新
public List<MyData> getDataWithCache(String param) {
    return redisCacheTemplate.asyncRefreshTemplate(
            "getData",
            param,
            () -> myRepository.queryByParam(param)
    );
}
```

### Key 定义

```java
// 在 CacheDynamicKey.java 中定义动态缓存 Key
String MY_MODULE_KEY = "my_module" + CacheKeyPrefix.SEPARATOR + "my_data";

// 构建带参数的 Key
static String buildMyModuleKey(String id) {
    return MY_MODULE_KEY + CacheKeyPrefix.SEPARATOR + id;
}
```

## 相关文件

- `common/src/main/java/com/persagy/apccontrol/basic/config/cache/provider/redis/RedisCacheTemplate.java` - Redis 缓存模板
- `common/src/main/java/com/persagy/apccontrol/basic/config/cache/provider/redis/RedisCacheObjectOperation.java` - 对象操作
- `common/src/main/java/com/persagy/apccontrol/basic/config/cache/provider/redis/RedisCacheHashOperation.java` - Hash 操作
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/CacheKey.java` - Key 定义接口
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/CacheDynamicKey.java` - 动态缓存 Key 定义
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/CachePersistenceKey.java` - 持久化缓存 Key 定义
- `common/src/main/java/com/persagy/apccontrol/basic/model/base/CacheLockKey.java` - 锁缓存 Key 定义
