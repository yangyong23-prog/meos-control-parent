package com.persagy.apccontrol.basic.utils;

import com.persagy.apccontrol.basic.exception.ThreadLocalException;
import com.persagy.apccontrol.basic.exception.enumeration.ThreadLocalErrorEnum;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ThreadLocal操作工具类
 *
 * <ul>
 *     <li><b>setAndGet/setIfAbsentAndGet</b> - 设置值到源对象中</li>
 *     <li><b>get/getAndThrowIfAbsent</b> - 从源对象中获取值</li>
 *     <li><b>contains</b> - 源对象中是否包含某个值</li>
 *     <li><b>remove</b> - 从源对象中删除值</li>
 *     <li><b>clear</b> - 清空源对象中的所有值</li>
 *     <li><b>getInnerMap</b> - 从源对象中获取内部 {@link Map}</li>
 *     <li><b>Execute.eachFunction</b> - 遍历源对象函数式接口方法</li>
 * </ul>
 *
 * @author JiYinchuan
 * @see ThreadLocal
 * @see ConcurrentHashMap
 * @see Execute
 * @since v6.0.0
 */
public class ThreadLocalUtils {

    /**
     * 设置值到源对象中并获取设置的值
     *
     * @param threadLocal 源对象
     * @param key         键, 不能为空
     * @param value       值, 不能为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 设置的值
     * @since v6.0.0
     */
    public static <K, V> V setAndGet(final ThreadLocal<Map<K, V>> threadLocal,
                                     final K key, final V value) {
        return Execute.function(threadLocal, key, value, innerMap -> innerMap.put(key, value));
    }

    /**
     * 如果数据不存在于源对象中则设置值并返回设置的值
     *
     * @param threadLocal 源对象
     * @param key         键, 不能为空
     * @param value       值, 不能为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 设置的 [value] 值
     * @since v6.0.0
     */
    public static <K, V> V setIfAbsentAndGet(final ThreadLocal<Map<K, V>> threadLocal,
                                             final K key, final V value) {
        return Execute.function(threadLocal, key, value, innerMap -> {
            innerMap.putIfAbsent(key, value);
            return value;
        });
    }

    /**
     * 从源对象中获取值
     *
     * @param threadLocal 源对象
     * @param key         键, 不能为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 获取到的 [value] 值, 未获取到值时返回 [null]
     * @since v6.0.0
     */
    public static <K, V> V get(final ThreadLocal<Map<K, V>> threadLocal, final K key) {
        return Execute.function(threadLocal, key, innerMap -> innerMap.get(key));
    }

    /**
     * 从源对象中获取值, 如果数据不存在于源对象中则设置值并返回设置的值
     *
     * @param threadLocal 源对象
     * @param key         键, 不能为空
     * @param value       值, 不能为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 获取到的 [value] 值, 未获取到值时返回设置的新值
     * @since v6.0.0
     */
    public static <K, V> V getIfAbsentAndSet(final ThreadLocal<Map<K, V>> threadLocal,
                                             final K key, final V value) {
        return Execute.function(threadLocal, key, value, innerMap -> {
            if (innerMap.containsKey(key)) {
                return innerMap.get(key);
            } else {
                innerMap.put(key, value);
                return value;
            }
        });
    }

    /**
     * 从源对象中获取值, 如果数据不存在于源对象中则设置值并返回设置的值
     *
     * @param threadLocal   源对象
     * @param key           键, 不能为空
     * @param valueConsumer 值的函数式接口
     * @param <K>           键泛型
     * @param <V>           值泛型
     * @return 获取到的 [value] 值, 未获取到值时返回设置的新值
     * @since v6.0.0
     */
    public static <K, V> V getIfAbsentAndSet(final ThreadLocal<Map<K, V>> threadLocal,
                                             final K key, final Supplier<V> valueConsumer) {
        return Execute.function(threadLocal, key, innerMap -> {
            if (innerMap.containsKey(key)) {
                return innerMap.get(key);
            } else {
                V value = valueConsumer.get();
                innerMap.put(key, value);
                return value;
            }
        });
    }

    /**
     * 从源对象中获取值, 获取不到时抛出异常
     *
     * @param threadLocal 源对象
     * @param key         键, 不能为空
     * @param userTip     异常时的用户提示信息
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 获取到的 [value] 值
     * @throws ThreadLocalException 获取数据结果为空
     * @since v6.0.0
     */
    public static <K, V> V getAndThrowIfAbsent(final ThreadLocal<Map<K, V>> threadLocal,
                                               final K key, final String userTip)
            throws ThreadLocalException {
        return Execute.function(threadLocal, key, innerMap -> {
            if (innerMap.containsKey(key)) {
                return innerMap.get(key);
            } else {
                throw new ThreadLocalException(ThreadLocalErrorEnum.RESULT_EMPTY, userTip);
            }
        });
    }

    /**
     * 判断源对象是否包括某个值
     *
     * @param threadLocal 源对象
     * @param key         键, 可为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 源对象中是否包含 [key] 对应的值
     * @see #getInnerMap(ThreadLocal)
     * @since v6.0.0
     */
    public static <K, V> boolean contains(final ThreadLocal<Map<K, V>> threadLocal, K key) {
        return getInnerMap(threadLocal).containsKey(key);
    }

    /**
     * 删除源对象中的值
     *
     * @param threadLocal 源对象
     * @param key         键, 可为空
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 删除的 [value] 值
     * @see #getInnerMap(ThreadLocal)
     * @since v6.0.0
     */
    public static <K, V> V remove(final ThreadLocal<Map<K, V>> threadLocal, K key) {
        return getInnerMap(threadLocal).remove(key);
    }

    /**
     * 清空源对象中的所有值
     *
     * @param threadLocal 源对象
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @see #getInnerMap(ThreadLocal)
     * @since v6.0.0
     */
    public static <K, V> void removeAll(final ThreadLocal<Map<K, V>> threadLocal) {
        getInnerMap(threadLocal).clear();
    }

    /**
     * 获取源对象中的 {@link Map} 对象
     *
     * @param threadLocal 源对象
     * @param <K>         键泛型
     * @param <V>         值泛型
     * @return 源对象中的 {@link Map} 对象
     * @see ConcurrentHashMap
     * @since v6.0.0
     */
    public static <K, V> Map<K, V> getInnerMap(final ThreadLocal<Map<K, V>> threadLocal) {
        Map<K, V> innerMap = threadLocal.get();
        if (Objects.isNull(innerMap)) {
            innerMap = new ConcurrentHashMap<>(16);
            threadLocal.set(innerMap);
        }
        return innerMap;
    }

    /**
     * 执行类
     *
     * <p><b>Warning:</b> {@link ConcurrentHashMap} 类中键与值均不能为 {@code null}
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    public static class Execute {

        /**
         * 函数式接口方法
         *
         * @param threadLocal 源对象
         * @param key         键
         * @param value       值
         * @param function    函数式接口
         * @param <K>         键泛型
         * @param <V>         值泛型
         * @return 参数中的 [value] 值
         * @throws ThreadLocalException 设置的键为空|设置的值为空
         * @see #getInnerMap(ThreadLocal)
         * @since v6.0.0
         */
        public static <K, V> V function(final ThreadLocal<Map<K, V>> threadLocal,
                                        final K key, final V value,
                                        final Function<Map<K, V>, V> function)
                throws ThreadLocalException {
            if (Objects.isNull(key)) {
                throw new ThreadLocalException(ThreadLocalErrorEnum.KEY_EMPTY);
            }
            if (Objects.isNull(value)) {
                throw new ThreadLocalException(ThreadLocalErrorEnum.VALUE_EMPTY);
            }
            return function.apply(getInnerMap(threadLocal));
        }

        /**
         * 函数式接口方法
         *
         * @param threadLocal 源对象
         * @param key         键
         * @param function    函数式接口
         * @param <K>         键泛型
         * @param <V>         值泛型
         * @return 获取到的 [value] 值
         * @throws ThreadLocalException 设置的键为空
         * @see #getInnerMap(ThreadLocal)
         * @since v6.0.0
         */
        public static <K, V> V function(final ThreadLocal<Map<K, V>> threadLocal,
                                        final K key, final Function<Map<K, V>, V> function)
                throws ThreadLocalException {
            if (Objects.isNull(key)) {
                throw new ThreadLocalException(ThreadLocalErrorEnum.KEY_EMPTY);
            }
            return function.apply(getInnerMap(threadLocal));
        }

        /**
         * 遍历键值对函数式键值对方法
         *
         * @param threadLocal 源对象
         * @param function    函数式接口
         * @param <K>         键泛型
         * @param <V>         值泛型
         * @see #getInnerMap(ThreadLocal)
         * @since v6.0.0
         */
        public static <K, V> void eachFunction(final ThreadLocal<Map<K, V>> threadLocal,
                                               final BiConsumer<K, V> function)
                throws ThreadLocalException {
            Set<Map.Entry<K, V>> entries = getInnerMap(threadLocal).entrySet();
            for (Map.Entry<K, V> entry : entries) {
                function.accept(entry.getKey(), entry.getValue());
            }
        }
    }
}
