package com.persagy.apccontrol.basic.opt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * opt 分支 B10：单次调用内的远程查询去重。
 * <p>
 * 只在一个方法调用（或一次请求）的生命周期内有效，随局部变量一起回收，不是跨请求的缓存。
 * 同一 key 只调用一次 loader，之后直接返回第一次的结果（含 null）。开关关闭时每次都调用 loader，行为与原代码一致。
 * 线程安全：并发首次访问同一 key 可能各调一次 loader，以先写入的为准。
 */
public final class OptCallMemo<K, V> {

    private static final Object NULL = new Object();

    private final String name;
    private final Map<K, Object> values;

    private OptCallMemo(String name, boolean enabled) {
        this.name = name;
        this.values = enabled ? new ConcurrentHashMap<>() : null;
    }

    /**
     * @param name   指标名后缀，命中计数为 {@code memo.{name}.hit}
     * @param switchName {@link OptSwitch} 开关名，创建时读取一次
     */
    public static <K, V> OptCallMemo<K, V> create(String name, String switchName) {
        return new OptCallMemo<>(name, OptSwitch.on(switchName));
    }

    public boolean enabled() {
        return values != null;
    }

    @SuppressWarnings("unchecked")
    public V get(K key, Function<? super K, ? extends V> loader) {
        if (values == null || key == null) {
            return loader.apply(key);
        }
        Object v = values.get(key);
        if (v != null) {
            OptMetrics.inc("memo." + name + ".hit");
            return v == NULL ? null : (V) v;
        }
        V loaded = loader.apply(key);
        Object prev = values.putIfAbsent(key, loaded == null ? NULL : loaded);
        if (prev != null) {
            return prev == NULL ? null : (V) prev;
        }
        return loaded;
    }
}
