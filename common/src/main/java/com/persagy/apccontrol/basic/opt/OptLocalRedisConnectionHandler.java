package com.persagy.apccontrol.basic.opt;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanCursor;
import org.springframework.data.redis.core.ScanIteration;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.persagy.apccontrol.basic.opt.OptLocalRedisStore.key;
import static com.persagy.apccontrol.basic.opt.OptLocalRedisStore.raw;

/**
 * 本地 Redis（switch.opt.localRedis）：{@link RedisConnection} 的动态代理实现。
 *
 * <p>在连接这一层替换，RedisTemplate / RedisCacheUtil / RedisCacheTemplate / {@code @Cacheable} 的 RedisCacheManager
 * 以及直接拿连接做回调的代码全部不用改：它们最终都调用这里的字节级命令。
 *
 * <p>路由（{@link OptLocalRedisRouter} 不为 null 时）：和 dtp-meos 共享的键（手自动、干预、运行模式、强制、控制值）
 * 原样转发到真实 Redis，写法与改造前逐字节一致；其余键全部落在 {@link OptLocalRedisStore}。
 * 多键命令按键拆分后合并结果；KEYS/SCAN 合并本地结果和真实 Redis 里属于共享前缀的键
 * （真实 Redis 里旧的私有键不会被带回来）。
 */
final class OptLocalRedisConnectionHandler implements InvocationHandler {

    private static final Object[] NO_ARGS = new Object[0];

    private final OptLocalRedisStore store;
    private final OptLocalRedisRouter router;
    private final RedisConnectionFactory realFactory;

    private volatile boolean closed;
    private RedisConnection real;

    private OptLocalRedisConnectionHandler(OptLocalRedisStore store, OptLocalRedisRouter router,
                                           RedisConnectionFactory realFactory) {
        this.store = store;
        this.router = router;
        this.realFactory = realFactory;
    }

    /**
     * 新建一个连接代理。
     *
     * @param router      共享键路由，null 表示全部本地
     * @param realFactory 真实 Redis 连接工厂，router 为 null 时可为 null
     */
    static RedisConnection newConnection(OptLocalRedisStore store, OptLocalRedisRouter router,
                                         RedisConnectionFactory realFactory) {
        return (RedisConnection) Proxy.newProxyInstance(OptLocalRedisConnectionHandler.class.getClassLoader(),
                new Class<?>[]{RedisConnection.class},
                new OptLocalRedisConnectionHandler(store, router, router == null ? null : realFactory));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object[] a = args == null ? NO_ARGS : args;
        switch (name) {
            case "equals":
                return proxy == a[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            case "toString":
                return "OptLocalRedisConnection" + (router == null ? "[local]" : "[local+shared]");
            case "close":
                closeQuietly();
                return null;
            case "isClosed":
                return closed;
            case "getNativeConnection":
                return store;
            case "isQueueing":
            case "isPipelined":
                return false;
            case "keyCommands":
            case "stringCommands":
            case "listCommands":
            case "setCommands":
            case "zSetCommands":
            case "hashCommands":
            case "geoCommands":
            case "hyperLogLogCommands":
            case "scriptingCommands":
            case "serverCommands":
            case "streamCommands":
                return proxy;
            default:
                break;
        }
        if (router != null) {
            Object[] routed = route(method, name, a);
            if (routed != null) {
                return routed[0];
            }
        }
        return local(name, a);
    }

    // ------------------------------------------------------------------------------------------- 路由到真实 Redis

    private RedisConnection real() {
        if (real == null) {
            real = realFactory.getConnection();
        }
        return real;
    }

    private void closeQuietly() {
        closed = true;
        if (real != null) {
            try {
                real.close();
            } catch (RuntimeException ignored) {
                // 关闭真实连接失败不影响本地连接的关闭
            } finally {
                real = null;
            }
        }
    }

    private Object invokeReal(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(real(), args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 判断并执行需要转发到真实 Redis 的命令。
     *
     * @return null 表示完全由本地处理；否则数组第一个元素是命令结果
     */
    private Object[] route(Method method, String name, Object[] a) throws Throwable {
        switch (name) {
            case "del":
            case "unlink":
            case "touch":
                if (a.length == 1 && a[0] instanceof byte[][]) {
                    return splitCount(name, (byte[][]) a[0]);
                }
                break;
            case "exists":
                if (a.length == 1 && a[0] instanceof byte[][]) {
                    return splitCount(name, (byte[][]) a[0]);
                }
                break;
            case "mGet":
                return splitMGet((byte[][]) a[0]);
            case "mSet":
            case "mSetNX":
                return splitMSet(name, castMap(a[0]));
            case "keys":
                return new Object[]{mergeKeys((byte[]) a[0])};
            case "scan":
                return new Object[]{cursor(new ArrayList<>(toRawList(mergeKeyList(scanPattern(a)))))};
            case "rename":
            case "renameNX": {
                boolean from = router.isShared(key((byte[]) a[0]));
                boolean to = router.isShared(key((byte[]) a[1]));
                if (from != to) {
                    throw new InvalidDataAccessApiUsageException("本地 Redis 不支持跨本地/共享键改名: " + key((byte[]) a[0]));
                }
                return from ? new Object[]{invokeReal(method, a)} : null;
            }
            case "select":
            case "ping":
            case "echo":
            case "info":
            case "dbSize":
            case "flushDb":
            case "flushAll":
            case "time":
                return null;
            default:
                break;
        }
        if (a.length > 0 && a[0] instanceof byte[] && router.isShared(key((byte[]) a[0]))) {
            return new Object[]{invokeReal(method, a)};
        }
        return null;
    }

    private Object[] splitCount(String name, byte[][] keys) {
        List<byte[]> shared = new ArrayList<>();
        List<String> local = new ArrayList<>();
        for (byte[] k : keys) {
            if (router.isShared(key(k))) {
                shared.add(k);
            } else {
                local.add(key(k));
            }
        }
        long n = localCount(name, local);
        if (!shared.isEmpty()) {
            byte[][] s = shared.toArray(new byte[0][]);
            Long r;
            switch (name) {
                case "unlink":
                    r = real().unlink(s);
                    break;
                case "touch":
                    r = real().touch(s);
                    break;
                case "exists":
                    r = real().exists(s);
                    break;
                default:
                    r = real().del(s);
                    break;
            }
            n += r == null ? 0 : r;
        }
        return new Object[]{n};
    }

    private long localCount(String name, List<String> keys) {
        if (keys.isEmpty()) {
            return 0;
        }
        if ("exists".equals(name) || "touch".equals(name)) {
            long n = 0;
            for (String k : keys) {
                if (store.exists(k)) {
                    n++;
                }
            }
            return n;
        }
        return store.del(keys);
    }

    private Object[] splitMGet(byte[][] keys) {
        List<byte[]> out = new ArrayList<>(Arrays.asList(new byte[keys.length][]));
        List<Integer> sharedIdx = new ArrayList<>();
        List<Integer> localIdx = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            (router.isShared(key(keys[i])) ? sharedIdx : localIdx).add(i);
        }
        if (!localIdx.isEmpty()) {
            List<String> lk = new ArrayList<>(localIdx.size());
            localIdx.forEach(i -> lk.add(key(keys[i])));
            List<byte[]> lv = store.mGet(lk);
            for (int j = 0; j < localIdx.size(); j++) {
                out.set(localIdx.get(j), lv.get(j));
            }
        }
        if (!sharedIdx.isEmpty()) {
            byte[][] sk = new byte[sharedIdx.size()][];
            for (int j = 0; j < sk.length; j++) {
                sk[j] = keys[sharedIdx.get(j)];
            }
            List<byte[]> sv = real().mGet(sk);
            for (int j = 0; j < sk.length; j++) {
                out.set(sharedIdx.get(j), sv == null ? null : sv.get(j));
            }
        }
        return new Object[]{out};
    }

    private Object[] splitMSet(String name, Map<byte[], byte[]> values) {
        Map<byte[], byte[]> shared = new LinkedHashMap<>();
        Map<String, byte[]> local = new LinkedHashMap<>();
        values.forEach((k, v) -> {
            if (router.isShared(key(k))) {
                shared.put(k, v);
            } else {
                local.put(key(k), v);
            }
        });
        boolean nx = "mSetNX".equals(name);
        boolean ok = local.isEmpty() || store.mSet(local, nx);
        if (!shared.isEmpty()) {
            Boolean r = nx ? real().mSetNX(shared) : real().mSet(shared);
            ok = ok && Boolean.TRUE.equals(r);
        }
        return new Object[]{ok};
    }

    private Set<byte[]> mergeKeys(byte[] pattern) {
        return new LinkedHashSet<>(toRawList(mergeKeyList(key(pattern))));
    }

    private Collection<String> mergeKeyList(String pattern) {
        Set<String> out = new LinkedHashSet<>(store.keys(pattern));
        if (router.mayMatchShared(pattern)) {
            // 用 SCAN 而不是 KEYS，避免阻塞与 dtp-meos 共用的 Redis
            ScanOptions options = ScanOptions.scanOptions()
                    .match(new String(raw(pattern), StandardCharsets.UTF_8)).count(1000).build();
            try (Cursor<byte[]> remote = real().scan(options)) {
                while (remote.hasNext()) {
                    String k = key(remote.next());
                    if (router.isShared(k)) {
                        out.add(k);
                    }
                }
            } catch (java.io.IOException e) {
                throw new InvalidDataAccessApiUsageException("关闭 Redis SCAN 游标失败", e);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------- 本地实现

    private Object local(String name, Object[] a) {
        switch (name) {
            // ---------------- key
            case "exists":
                if (a[0] instanceof byte[][]) {
                    return localCount("exists", keyList((byte[][]) a[0]));
                }
                return store.exists(k(a[0]));
            case "del":
            case "unlink":
                return store.del(keyList((byte[][]) a[0]));
            case "touch":
                return localCount("touch", keyList((byte[][]) a[0]));
            case "type":
                return dataType(store.type(k(a[0])));
            case "keys":
                return new LinkedHashSet<>(toRawList(store.keys(k(a[0]))));
            case "scan":
                return cursor(new ArrayList<>(toRawList(store.keys(scanPattern(a)))));
            case "expire":
                return store.pExpire(k(a[0]), TimeUnit.SECONDS.toMillis((Long) a[1]));
            case "pExpire":
                return store.pExpire(k(a[0]), (Long) a[1]);
            case "expireAt":
                return store.pExpireAt(k(a[0]), TimeUnit.SECONDS.toMillis((Long) a[1]));
            case "pExpireAt":
                return store.pExpireAt(k(a[0]), (Long) a[1]);
            case "persist":
                return store.persist(k(a[0]));
            case "ttl":
                return ttl(k(a[0]), a.length > 1 ? (TimeUnit) a[1] : TimeUnit.SECONDS);
            case "pTtl":
                return ttl(k(a[0]), a.length > 1 ? (TimeUnit) a[1] : TimeUnit.MILLISECONDS);
            case "rename":
                store.rename(k(a[0]), k(a[1]));
                return null;
            case "renameNX":
                if (store.exists(k(a[1]))) {
                    return false;
                }
                store.rename(k(a[0]), k(a[1]));
                return true;
            // ---------------- string
            case "get":
                return store.get(k(a[0]));
            case "getSet":
                return store.getSet(k(a[0]), (byte[]) a[1]);
            case "mGet":
                return store.mGet(keyList((byte[][]) a[0]));
            case "set":
                return set(a);
            case "setNX":
                return store.set(k(a[0]), (byte[]) a[1], 0, false, true, false);
            case "setEx":
                return store.set(k(a[0]), (byte[]) a[2], TimeUnit.SECONDS.toMillis((Long) a[1]), false, false, false);
            case "pSetEx":
                return store.set(k(a[0]), (byte[]) a[2], (Long) a[1], false, false, false);
            case "mSet":
                return store.mSet(stringKeyMap(castMap(a[0])), false);
            case "mSetNX":
                return store.mSet(stringKeyMap(castMap(a[0])), true);
            case "incr":
                return store.incrBy(k(a[0]), 1);
            case "decr":
                return store.incrBy(k(a[0]), -1);
            case "incrBy":
                if (a[1] instanceof Double) {
                    return store.incrByFloat(k(a[0]), (Double) a[1]);
                }
                return store.incrBy(k(a[0]), (Long) a[1]);
            case "decrBy":
                return store.incrBy(k(a[0]), -((Long) a[1]));
            case "append":
                return store.append(k(a[0]), (byte[]) a[1]);
            case "strLen":
                return store.strLen(k(a[0]));
            // ---------------- hash
            case "hSet":
                return store.hSet(k(a[0]), k(a[1]), (byte[]) a[2], false);
            case "hSetNX":
                return store.hSet(k(a[0]), k(a[1]), (byte[]) a[2], true);
            case "hGet":
                return store.hGet(k(a[0]), k(a[1]));
            case "hMGet":
                return store.hMGet(k(a[0]), keyList((byte[][]) a[1]));
            case "hMSet":
                store.hMSet(k(a[0]), stringKeyMap(castMap(a[1])));
                return null;
            case "hGetAll":
                return rawKeyMap(store.hGetAll(k(a[0])));
            case "hKeys":
                return new LinkedHashSet<>(toRawList(store.hGetAll(k(a[0])).keySet()));
            case "hVals":
                return new ArrayList<>(store.hGetAll(k(a[0])).values());
            case "hDel":
                return store.hDel(k(a[0]), keyList((byte[][]) a[1]));
            case "hExists":
                return store.hExists(k(a[0]), k(a[1]));
            case "hLen":
                return store.hLen(k(a[0]));
            case "hStrLen":
                return store.hStrLen(k(a[0]), k(a[1]));
            case "hIncrBy":
                if (a[2] instanceof Double) {
                    return store.hIncrByFloat(k(a[0]), k(a[1]), (Double) a[2]);
                }
                return store.hIncrBy(k(a[0]), k(a[1]), (Long) a[2]);
            case "hScan":
                return hashCursor(store.hGetAll(k(a[0])), (ScanOptions) a[1]);
            // ---------------- list
            case "lPush":
                return store.push(k(a[0]), Arrays.asList((byte[][]) a[1]), true, false);
            case "rPush":
                return store.push(k(a[0]), Arrays.asList((byte[][]) a[1]), false, false);
            case "lPushX":
                return store.push(k(a[0]), listOf(a[1]), true, true);
            case "rPushX":
                return store.push(k(a[0]), listOf(a[1]), false, true);
            case "lLen":
                return store.lLen(k(a[0]));
            case "lRange":
                return store.lRange(k(a[0]), (Long) a[1], (Long) a[2]);
            case "lTrim":
                store.lTrim(k(a[0]), (Long) a[1], (Long) a[2]);
                return null;
            case "lIndex":
                return store.lIndex(k(a[0]), (Long) a[1]);
            case "lSet":
                store.lSet(k(a[0]), (Long) a[1], (byte[]) a[2]);
                return null;
            case "lRem":
                return store.lRem(k(a[0]), (Long) a[1], (byte[]) a[2]);
            case "lPop":
                if (a.length == 1) {
                    return store.pop(k(a[0]), true);
                }
                break;
            case "rPop":
                if (a.length == 1) {
                    return store.pop(k(a[0]), false);
                }
                break;
            // ---------------- set
            case "sAdd":
                return store.sAdd(k(a[0]), keyList((byte[][]) a[1]));
            case "sRem":
                return store.sRem(k(a[0]), keyList((byte[][]) a[1]));
            case "sMembers":
                return new LinkedHashSet<>(toRawList(store.sMembers(k(a[0]))));
            case "sIsMember":
                return store.sIsMember(k(a[0]), k(a[1]));
            case "sCard":
                return store.sCard(k(a[0]));
            case "sPop":
                if (a.length == 1) {
                    String m = store.sPop(k(a[0]));
                    return m == null ? null : raw(m);
                }
                break;
            case "sScan":
                return cursor(new ArrayList<>(toRawList(filter(store.sMembers(k(a[0])), (ScanOptions) a[1]))));
            // ---------------- connection / server
            case "select":
                return null;
            case "ping":
                return "PONG";
            case "echo":
                return a[0];
            case "info":
                return info();
            case "dbSize":
                return store.dbSize();
            case "flushDb":
            case "flushAll":
                store.flush();
                return null;
            case "time":
                return System.currentTimeMillis();
            default:
                break;
        }
        throw new InvalidDataAccessApiUsageException("本地 Redis（switch.opt.localRedis）未实现命令: " + name
                + "/" + a.length);
    }

    private Object set(Object[] a) {
        String k = k(a[0]);
        byte[] v = (byte[]) a[1];
        if (a.length == 2) {
            store.set(k, v, 0, false, false, false);
            return true;
        }
        Expiration exp = (Expiration) a[2];
        RedisStringCommands.SetOption opt = (RedisStringCommands.SetOption) a[3];
        long ttl = exp == null || exp.isPersistent() || exp.isKeepTtl() ? 0 : exp.getExpirationTimeInMilliseconds();
        boolean keepTtl = exp != null && exp.isKeepTtl();
        boolean nx = opt == RedisStringCommands.SetOption.SET_IF_ABSENT;
        boolean xx = opt == RedisStringCommands.SetOption.SET_IF_PRESENT;
        return store.set(k, v, ttl, keepTtl, nx, xx);
    }

    private Long ttl(String k, TimeUnit unit) {
        long ms = store.pTtl(k);
        if (ms < 0) {
            return ms;
        }
        // Redis 的 TTL 是向下取整的秒数；这里与 Lettuce 换算保持一致
        return unit.convert(ms, TimeUnit.MILLISECONDS);
    }

    private static DataType dataType(OptLocalRedisStore.Type type) {
        switch (type) {
            case STRING:
                return DataType.STRING;
            case HASH:
                return DataType.HASH;
            case LIST:
                return DataType.LIST;
            case SET:
                return DataType.SET;
            default:
                return DataType.NONE;
        }
    }

    private Properties info() {
        Properties p = new Properties();
        p.setProperty("redis_version", "opt-local");
        p.setProperty("redis_mode", router == null ? "local" : "local+shared");
        p.setProperty("db0", "keys=" + store.dbSize());
        return p;
    }

    // ------------------------------------------------------------------------------------------- 转换工具

    private static String k(Object rawKey) {
        return key((byte[]) rawKey);
    }

    private static List<String> keyList(byte[][] keys) {
        List<String> out = new ArrayList<>(keys.length);
        for (byte[] k : keys) {
            out.add(key(k));
        }
        return out;
    }

    private static List<byte[]> toRawList(Collection<String> keys) {
        List<byte[]> out = new ArrayList<>(keys.size());
        for (String k : keys) {
            out.add(raw(k));
        }
        return out;
    }

    private static List<byte[]> listOf(Object value) {
        List<byte[]> l = new ArrayList<>(1);
        l.add((byte[]) value);
        return l;
    }

    @SuppressWarnings("unchecked")
    private static Map<byte[], byte[]> castMap(Object o) {
        return (Map<byte[], byte[]>) o;
    }

    private static Map<String, byte[]> stringKeyMap(Map<byte[], byte[]> m) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(key(k), v));
        return out;
    }

    private static Map<byte[], byte[]> rawKeyMap(Map<String, byte[]> m) {
        Map<byte[], byte[]> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(raw(k), v));
        return out;
    }

    private static String scanPattern(Object[] a) {
        ScanOptions options = a.length > 0 && a[0] instanceof ScanOptions ? (ScanOptions) a[0] : null;
        return patternOf(options);
    }

    private static String patternOf(ScanOptions options) {
        String p = options == null ? null : options.getPattern();
        // ScanOptions 里的 pattern 是 String，Lettuce 发送时按 UTF-8 编码；这里换成与键一致的字节映射
        return p == null ? "*" : new String(p.getBytes(StandardCharsets.UTF_8), OptLocalRedisStore.KEY_CHARSET);
    }

    private Collection<String> filter(Collection<String> members, ScanOptions options) {
        java.util.regex.Pattern p = store.compile(patternOf(options));
        List<String> out = new ArrayList<>();
        for (String m : members) {
            if (p.matcher(m).matches()) {
                out.add(m);
            }
        }
        return out;
    }

    private Cursor<Map.Entry<byte[], byte[]>> hashCursor(Map<String, byte[]> h, ScanOptions options) {
        java.util.regex.Pattern p = store.compile(patternOf(options));
        List<Map.Entry<byte[], byte[]>> items = new ArrayList<>();
        h.forEach((f, v) -> {
            if (p.matcher(f).matches()) {
                items.add(new AbstractMap.SimpleImmutableEntry<>(raw(f), v));
            }
        });
        return cursor(items);
    }

    /** 一次性返回全部结果的游标（本地数据量小，没有必要分批） */
    private static <T> Cursor<T> cursor(List<T> items) {
        return new ScanCursor<T>(ScanOptions.NONE) {
            @Override
            protected ScanIteration<T> doScan(long cursorId, ScanOptions options) {
                return new ScanIteration<>(0, items);
            }
        }.open();
    }
}
