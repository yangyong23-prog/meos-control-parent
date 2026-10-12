package com.persagy.apccontrol.basic.config.cache.provider.redis;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.crypto.digest.MD5;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.MapUtils;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.config.aspect.RequestDebugAspect;
import com.persagy.apccontrol.basic.config.cache.provider.CacheHashOperation;
import com.persagy.apccontrol.basic.config.cache.provider.CacheObjectOperation;
import com.persagy.apccontrol.basic.config.cache.provider.CacheProvider;
import com.persagy.apccontrol.basic.model.base.CacheDynamicKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * RedisService服务
 *
 * <p>该类为 {@code redis} 操作核心服务接口
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Slf4j
public class RedisCacheTemplate extends AbstractRedisCacheOperation<RedisTemplate<String, Object>> implements CacheProvider {

    /**
     * 默认模版Bean名称
     */
    public static final String APPLICATION_REDIS_CACHE_TEMPLATE_BEAN_NAME = "applicationRedisCacheTemplate";

    /**
     * 原始模版Bean名称
     */
    public static final String ORIGINAL_REDIS_CACHE_TEMPLATE_BEAN_NAME = "originalRedisCacheTemplate";

    private final CacheObjectOperation objectOperation = new RedisCacheObjectOperation(this);
    private final CacheHashOperation hashOperation = new RedisCacheHashOperation(this);
    
    /**
     * asyncRefreshTemplateByList 缓存锁前缀
     */
    public static final String BASE_CACHE_KEY_PREFIX =
            RedisKey.APC + CommonConst.COLON + "asyncRefreshTemplate" + CommonConst.COLON;
    @Resource
    private NacosSwitchConfig nacosSwitchConfig;
    @Resource
    private RedissonDistributedLockerService redissonDistributedLockerService;


    /**
     * 构造器
     *
     * @param redisTemplate RedisTemplate
     * @since v5.7.0
     */
    public RedisCacheTemplate(RedisTemplate<String, Object> redisTemplate) {
        super(() -> redisTemplate);
    }

    /**
     * 设置模版
     *
     * @param redisTemplate 模版
     * @return 新实例
     * @since v5.7.0
     */
    public RedisCacheTemplate template(@NonNull RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheTemplate(redisTemplate);
    }

    /**
     * 获取实际操作类
     *
     * @return 实际操作类
     * @since v5.7.0
     */
    public RedisTemplate<String, Object> delegate() {
        return opsDelegate();
    }

    /**
     * 对象操作类实体
     *
     * @return 对象操作类实体
     * @since v5.7.0
     */
    public CacheObjectOperation object() {
        return objectOperation;
    }

    /**
     * Hash操作类实体
     *
     * @return Hash操作类实体
     * @since v5.7.0
     */
    public CacheHashOperation hash() {
        return hashOperation;
    }

    @Override
    @NonNull
    public Set<String> keys(@NonNull String pattern) {
        return RedisCacheExpUtil.scanKeys(opsDelegate(), pattern);
    }

    @Override
    public boolean expire(@NonNull String key, long expireTime, @NonNull TimeUnit timeUnit) {
        if (expireTime <= 0) {
            return false;
        }
        return getBooleanValue(opsDelegate().expire(key, expireTime, timeUnit));
    }

    @Override
    public boolean expire(@NonNull String key, @NonNull Duration expireTime) {
        return getBooleanValue(opsDelegate().expire(key, expireTime));
    }

    @Override
    public long getExpire(@NonNull String key, @Nullable Supplier<Long> defaultNonExpire) {
        return getValue(opsDelegate().getExpire(key, TimeUnit.SECONDS),
                Objects.nonNull(defaultNonExpire) ? defaultNonExpire : () -> -1L);
    }

    @Override
    public boolean hasKey(@NonNull String key) {
        return getBooleanValue(opsDelegate().hasKey(key));
    }

    @Override
    public long del(@NonNull String... key) {
        long result = 0;
        if (key.length > 0) {
            if (key.length == 1) {
                result = getLongValue(opsDelegate().delete(key[0]));
            } else {
                Long deleteResult = opsDelegate().delete(Arrays.asList(key));
                result = getValue(deleteResult, () -> -1L);
            }
        }
        return result;
    }
    
    @Override
    public Long fuzzyMatchingDel(@NonNull String keyPattern) {
        Long result = 0L;
        // 1. 查找所有匹配的key
        Set<String> keys = keys(keyPattern + "*");
        
        // 2. 批量删除
        if (CollectionUtils.isNotEmpty(keys)) {
            result = opsDelegate().delete(keys);
        }
        return result;
    }
    
    @Override
    public void dataUpdatingDelCache() {
        String rwdKey =
                CommonConst.APPLICATION_NAME + CacheKeyPrefix.SEPARATOR + CacheDynamicKey.DYNAMIC_KEY_PREFIX + "rwd";
        fuzzyMatchingDel(rwdKey);
        //把热刷新的缓存一并删除
        fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX);
    }
    
    /**
     * asyncRefreshTemplateByList 获取缓存key
     * @param methodName 方法名称
     * @param methodArgs 方法参数
     * @return 缓存key
     */
    private static String generateCacheKey(String methodName, Object methodArgs) {
        return BASE_CACHE_KEY_PREFIX + ContextUtil.getGroupCode() + CommonConst.COLON + ContextUtil.getProjectId() + CommonConst.COLON
                + methodName + CommonConst.COLON + (methodArgs != null ? MD5.create().digestHex(methodArgs.toString()) : "");
    }

    /**
     * asyncRefreshTemplateByList 获取缓存锁key
     *
     * @param methodName 方法名称
     * @param methodArgs 方法参数
     * @return 缓存锁key
     */
    private static String generateLockKey(String methodName, Object methodArgs) {
        return BASE_CACHE_KEY_PREFIX + methodName + (methodArgs != null ? MD5.create().digestHex(methodArgs.toString()) : "");
    }

    /**
     * 查询缓存模版方法
     * 传递一个待执行的方法 如果查询到数据就缓存,没有查询到就不缓存
     * 缓存刷新机制是每次触发进异步刷新(如果查询的为空 会把之前的缓存删除 保持数据一致性)
     * 缓存有效时间 3 小时
     * 缓存 key = BASE_CACHE_KEY_PREFIX + methodName + methodArgs
     * @param methodName   方法名称，缓存key 的标识
     * @param methodArgs   方法的参数，缓存key 的标识
     * @param methodToCall 要执行的方法，通过函数式接口传入具体的业务逻辑
     * @return 方法执行的结果，如果缓存中已有，则直接返回缓存中的值
     */
    public <T> T asyncRefreshTemplate(String methodName, Object methodArgs, MethodCall<T> methodToCall) {

        // 关闭缓存刷新
        if (!nacosSwitchConfig.getAsyncRefreshTemplateSwitch() || debug()) {
            try {
                return methodToCall.call();
            } catch (Exception e) {
                log.error("[缓存热刷新] 直接执行方法 {} 出现异常", methodName, e);
                throw new RuntimeException(LocaleMessageUtil.getMessage("key_cce5bcc8b7f34126b990e7d4d707fe67"), e);
            }
        }
        String cacheKey = generateCacheKey(methodName, methodArgs);
        String lockKey = generateLockKey(methodName, methodArgs);

        T cacheObject = object().get(cacheKey);
        
        // 判断缓存是否有数据 没有数据走方法执行
        if (isCacheObjectNull(cacheObject)) {
            return getT(methodName, methodToCall, lockKey, cacheKey);
        }
        //异步刷新缓存
        asynchronousRefresh(methodToCall, lockKey, cacheKey);
        return cacheObject;
    }
    
    /**
     * 查询方法数据
     */
    /**
     * 查询方法数据
     */
    private <T> T getT(String methodName, MethodCall<T> methodToCall, String lockKey, String cacheKey) {
        T cacheObject;
        // 先尝试获取锁，避免死锁设置超时时间 最长 1 秒
        boolean lock = redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, 1, 1);
        
        // 如果没有获取到锁，则进入循环检查
        if (!lock) {
            // 循环尝试获取锁，最多等待30秒
            for (int i = 0; i < 30; i++) {
                // 等待1秒后再次尝试获取锁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("[缓存热刷新] 等待获取锁失败，lockKey: {}", lockKey);
                    break;
                }
                
                // 尝试非阻塞获取锁
                lock = redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, 1, 1);
                if (lock) {
                    break;
                }
                
                // 获取锁失败，检查缓存中是否已有数据
                T cachedData = object().get(cacheKey);
                if (!isCacheObjectNull(cachedData)) {
                    return cachedData;
                }
            }
        }
        
        try {
            // 双重检查缓存，防止锁等待期间已有其他线程更新了缓存
            T doubleCheckCache = object().get(cacheKey);
            if (!isCacheObjectNull(doubleCheckCache)) {
                return doubleCheckCache;
            }
            //直接原始调用
            cacheObject = methodToCall.call();
        } catch (Exception e) {
            log.error("[缓存热刷新]执行方法 {} 出现异常", methodName, e);
            throw new RuntimeException(LocaleMessageUtil.getMessage("key_158a9027ac7e42b192f8c0c0658b1de0"), e);
        } finally {
            if (lock) {
                redissonDistributedLockerService.unlock(lockKey);
            }
        }
        object().set(cacheKey, cacheObject, Duration.ofHours(3));
        return cacheObject;
    }
    
    private boolean debug() {
        boolean debug = false;
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return debug;
            }
            HttpServletRequest request = attributes.getRequest();
            String level = request.getHeader(RequestDebugAspect.REQUEST_DEBUG_HEADER);
            debug = StringUtils.isNotBlank(level);
        } catch (Exception e) {
            log.warn("[缓存热刷新] 获取调试级别失败 跳过", e);
        }
        return debug;
    }
    
    /**
     * 异步刷新缓存
     *
     * @param methodToCall
     * @param lockKey
     * @param cacheKey
     * @param <T>
     */
    private <T> void asynchronousRefresh(MethodCall<T> methodToCall, String lockKey, String cacheKey) {
        //异步刷新数据
        BusinessThreadPoolExecutor.ttlExecutor().execute(() -> {
            //加锁 降级
            boolean tryLock = redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, 60, 60);
            //获取到锁 刷新缓存
            if (tryLock) {
                try {
                    // 添加降频逻辑：检查是否在10分钟内已经刷新过
                    String refreshTimeKey = cacheKey + ":lastRefresh";
                    String lastRefreshTimeString = object().get(refreshTimeKey);
                    long currentTime = System.currentTimeMillis();
                    if (lastRefreshTimeString != null) {
                        Long lastRefreshTime = new Long(lastRefreshTimeString);
                        // 如果距离上次刷新时间小于2分钟，则跳过刷新
                        if ((currentTime - lastRefreshTime) < TimeUnit.MINUTES.toMillis(2)) {
                            return; // 跳过刷新
                        }
                    }
                    
                    T object = methodToCall.call();
                    //如果查询到数据是空 那么删除缓存
                    if (isCacheObjectNull(object)) {
                        del(cacheKey);
                    } else {
                        //取到非空数据 更新缓存
                        object().set(cacheKey, object, Duration.ofHours(3));
                        // 记录刷新时间
                        object().set(refreshTimeKey, currentTime, Duration.ofMinutes(15));
                    }
                } catch (Exception e) {
                    log.error("[缓存热刷新]执行方法出现异常 lockQueryKey :{}", lockKey, e);
                } finally {
                    redissonDistributedLockerService.unlock(lockKey);
                }
            }
        });
    }
    
    /**
     * 判断对象是否是空 或者空集合
     *
     * @param cacheObject 对象
     * @return 是否为空
     */
    private boolean isCacheObjectNull(Object cacheObject) {
        if (cacheObject instanceof Collection<?>) {
            return CollectionUtils.isEmpty((Collection<?>) cacheObject);
        } else if (cacheObject instanceof Map<?, ?>) {
            return MapUtils.isEmpty((Map<?, ?>) cacheObject);
        } else {
            return cacheObject == null;
        }
    }


    /**
     * 定义函数式接口，用于表示要执行的方法
     * @param <T> 方法的返回类型
     */
    @FunctionalInterface
    public interface MethodCall<T> {
        T call() throws Exception;
    }

    /**
     * 保存数据，并确保只保留最新 10 条
     *
     * @param key   缓存key
     * @param data  查询的总数
     * @param total 查询的数据列表
     * @param date 过期时间
     * @param <V>
     */
    public <V> void saveLatestDate(String key, V data, int total, Date date) {
        // 将新数据添加到列表的左侧
        opsDelegate().opsForList().leftPush(key, data);
        // 修剪列表，只保留最新的 total 条数据
        opsDelegate().opsForList().trim(key, 0, total);

        // 设置列表的过期时间
        if (date != null) {
            opsDelegate().expireAt(key, date);
        }

    }

    /**
     * 查询最新的 total 条数据
     *
     * @param key   缓存key
     * @param total 查询的总数
     * @param <V>
     * @return 查询的数据列表
     */
    public <V> List<V> getLatestDate(String key, int total) {
        return (List<V>) opsDelegate().opsForList().range(key, 0, total);
    }



}
