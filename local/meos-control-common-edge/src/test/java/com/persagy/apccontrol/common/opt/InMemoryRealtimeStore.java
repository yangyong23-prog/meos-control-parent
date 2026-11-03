package com.persagy.apccontrol.common.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 测试用内存存储 */
class InMemoryRealtimeStore implements OptRealtimeStore {

    final Map<String, String> data = new ConcurrentHashMap<>();
    final AtomicInteger gets = new AtomicInteger();
    final AtomicInteger sets = new AtomicInteger();

    @Override
    public List<String> mget(List<String> keys) {
        List<String> r = new ArrayList<>();
        for (String k : keys) {
            r.add(data.get(k));
        }
        return r;
    }

    @Override
    public String get(String key) {
        gets.incrementAndGet();
        return data.get(key);
    }

    @Override
    public void set(String key, String json) {
        sets.incrementAndGet();
        data.put(key, json);
    }
}
