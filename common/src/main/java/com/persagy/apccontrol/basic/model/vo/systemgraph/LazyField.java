package com.persagy.apccontrol.basic.model.vo.systemgraph;

import org.springframework.lang.NonNull;

import java.util.function.Supplier;

/**
 * 字段延迟获取
 *
 * @author chenlong
 * @date 2022/1/22 11:09 上午
 */
public class LazyField<T> implements Supplier<T> {

    private volatile T t;
    private final Supplier<T> supplier;

    public LazyField(@NonNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyField<T> by(@NonNull Supplier<T> supplier) {
        return new LazyField<>(supplier);
    }

    @Override
    public T get() {
        if (t == null) {
            synchronized (this) {
                if (t == null) {
                    t = supplier.get();
//                    supplier = null;
                }
            }
        }
        return t;
    }

    /**
     * 清除数据，可以重新调用生成
     */
    public void clear() {
        synchronized (this) {
            t = null;
        }
    }
}
