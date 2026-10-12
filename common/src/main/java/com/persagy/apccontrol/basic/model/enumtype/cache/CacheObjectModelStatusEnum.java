package com.persagy.apccontrol.basic.model.enumtype.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 缓存对象模型的共享启用状态。
 *
 * <p>{@link #READY} 表示对应一级运行监测类型已经完成一次完整缓存构建；其他值或缺失值
 * 均由读取方解释为未就绪。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Getter
@AllArgsConstructor
public enum CacheObjectModelStatusEnum {

    /**
     * 缓存完整并允许新模型读取。
     *
     * @since v7.260701.0
     */
    READY("READY"),

    /**
     * 历史未就绪状态，仅用于兼容数据库或 Redis 中已有值；新流程不再写入。
     *
     * @since v7.260701.0
     */
    FAILED("FAILED");

    /**
     * 持久化和 Redis 中使用的状态编码。
     *
     * @since v7.260701.0
     */
    private final @NonNull String code;

    /**
     * 按状态编码解析状态；未知编码按未就绪处理，不向业务请求抛出状态异常。
     *
     * @param code 状态编码
     * @return 状态，编码缺失或未知时返回 {@code null}
     * @since v7.260701.0
     */
    public static @Nullable CacheObjectModelStatusEnum parse(@Nullable String code) {
        if (code == null) {
            return null;
        }
        for (CacheObjectModelStatusEnum status : CacheObjectModelStatusEnum.values()) {
            if (StringUtils.equals(status.code, code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取状态编码。
     *
     * @return 状态编码
     * @since v7.260701.0
     */
    @Override
    public @NonNull String toString() {
        return this.code;
    }
}
