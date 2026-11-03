package com.persagy.apccontrol.common.opt;

import java.util.List;

/**
 * B2：实时值缓存的存储抽象（键值均为 UTF-8 字符串，值为 JSON）。生产实现见 {@link OptRedisRealtimeStore}。
 */
public interface OptRealtimeStore {

    /**
     * @return 与 keys 等长、同序；不存在的位置为 null
     */
    List<String> mget(List<String> keys);

    String get(String key);

    void set(String key, String json);
}
