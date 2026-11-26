package com.persagy.apccontrol.basic.opt;

import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

/**
 * 本地 Redis（switch.opt.localRedis）：进程内的键值存储，语义对齐 Redis 单机版的常用命令。
 *
 * <p>设计要点：
 * <ul>
 *   <li>与 Redis 一样“一条命令一个原子操作”：所有方法 {@code synchronized}，命令之间串行，
 *       incr / setNX / mSet 等不需要额外加锁。单条命令都是微秒级，串行化不会成为瓶颈（Redis 本身也是单线程）。</li>
 *   <li>键和值都保存序列化后的字节（键用 ISO-8859-1 映射成 String，字节与字符一一对应），
 *       和走 Redis 时一样每次读取都会反序列化出新对象，调用方修改返回值不会污染缓存。</li>
 *   <li>过期：访问时惰性判断 + {@link #sweepExpired()} 定期清理。</li>
 *   <li>只覆盖 String / Hash / List / Set 四种类型；ZSet、Stream、发布订阅、Lua 等未实现的命令抛
 *       {@link InvalidDataAccessApiUsageException}，与连接异常一样走调用方原有的异常处理。</li>
 * </ul>
 *
 * <p>数据只在内存里，重启即清空。边缘盒子上 meos-control 放在 Redis 里的私有数据都是可重建的
 * （信息点配置缓存会在启动时重新加载，实时值/统计会被新数据覆盖），因此不做持久化。
 */
public class OptLocalRedisStore {

    /** 键的字节与 String 互转使用的字符集：ISO-8859-1 下字节与字符一一对应，不会丢信息 */
    static final java.nio.charset.Charset KEY_CHARSET = StandardCharsets.ISO_8859_1;

    private static final int PATTERN_CACHE_LIMIT = 512;

    private final Map<String, Entry> data = new HashMap<>();
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public OptLocalRedisStore(LongSupplier clock) {
        this.clock = clock;
    }

    /** 一个键的值和过期时间（毫秒时间戳，0 表示永不过期） */
    private static final class Entry {
        private final Object value;
        private long expireAt;

        private Entry(Object value) {
            this.value = value;
        }
    }

    /** Redis TYPE 命令的结果 */
    public enum Type { NONE, STRING, HASH, LIST, SET }

    // ----------------------------------------------------------------------------------------------- 工具

    static String key(byte[] raw) {
        return new String(raw, KEY_CHARSET);
    }

    static byte[] raw(String key) {
        return key.getBytes(KEY_CHARSET);
    }

    private Entry live(String key) {
        Entry e = data.get(key);
        if (e != null && e.expireAt > 0 && e.expireAt <= clock.getAsLong()) {
            data.remove(key);
            return null;
        }
        return e;
    }

    private static InvalidDataAccessApiUsageException wrongType(String key) {
        return new InvalidDataAccessApiUsageException(
                "WRONGTYPE Operation against a key holding the wrong kind of value: " + key);
    }

    private byte[] stringValue(String key) {
        Entry e = live(key);
        if (e == null) {
            return null;
        }
        if (!(e.value instanceof byte[])) {
            throw wrongType(key);
        }
        return (byte[]) e.value;
    }

    @SuppressWarnings("unchecked")
    private <T> T typed(String key, Class<?> type, boolean create, java.util.function.Supplier<Object> factory) {
        Entry e = live(key);
        if (e == null) {
            if (!create) {
                return null;
            }
            e = new Entry(factory.get());
            data.put(key, e);
        }
        if (!type.isInstance(e.value)) {
            throw wrongType(key);
        }
        return (T) e.value;
    }

    private Map<String, byte[]> hash(String key, boolean create) {
        return typed(key, LinkedHashMap.class, create, LinkedHashMap::new);
    }

    private LinkedList<byte[]> list(String key, boolean create) {
        return typed(key, LinkedList.class, create, LinkedList::new);
    }

    private Set<String> set(String key, boolean create) {
        return typed(key, LinkedHashSet.class, create, LinkedHashSet::new);
    }

    /** 容器类型（hash/list/set）被清空后与 Redis 一样删除整个键 */
    private void dropIfEmpty(String key, Object container) {
        if (container instanceof Map && ((Map<?, ?>) container).isEmpty()
                || container instanceof Collection && ((Collection<?>) container).isEmpty()) {
            data.remove(key);
        }
    }

    private static long parseLong(byte[] value, String key) {
        try {
            return Long.parseLong(new String(value, StandardCharsets.UTF_8).trim());
        } catch (NumberFormatException ex) {
            throw new InvalidDataAccessApiUsageException("ERR value is not an integer or out of range: " + key, ex);
        }
    }

    private static double parseDouble(byte[] value, String key) {
        try {
            return Double.parseDouble(new String(value, StandardCharsets.UTF_8).trim());
        } catch (NumberFormatException ex) {
            throw new InvalidDataAccessApiUsageException("ERR value is not a valid float: " + key, ex);
        }
    }

    private static byte[] number(long v) {
        return Long.toString(v).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] number(double v) {
        String s = (v == Math.rint(v) && !Double.isInfinite(v)) ? Long.toString((long) v) : Double.toString(v);
        return s.getBytes(StandardCharsets.UTF_8);
    }

    // ----------------------------------------------------------------------------------------------- 键

    public synchronized boolean exists(String key) {
        return live(key) != null;
    }

    public synchronized long del(Collection<String> keys) {
        long n = 0;
        for (String k : keys) {
            if (live(k) != null) {
                data.remove(k);
                n++;
            }
        }
        return n;
    }

    public synchronized Type type(String key) {
        Entry e = live(key);
        if (e == null) {
            return Type.NONE;
        }
        if (e.value instanceof byte[]) {
            return Type.STRING;
        }
        if (e.value instanceof Map) {
            return Type.HASH;
        }
        if (e.value instanceof List) {
            return Type.LIST;
        }
        return Type.SET;
    }

    /** 设置过期时间（毫秒时长）；ttlMillis &lt;= 0 时与 Redis 一样立即删除 */
    public synchronized boolean pExpire(String key, long ttlMillis) {
        Entry e = live(key);
        if (e == null) {
            return false;
        }
        if (ttlMillis <= 0) {
            data.remove(key);
        } else {
            e.expireAt = clock.getAsLong() + ttlMillis;
        }
        return true;
    }

    public synchronized boolean pExpireAt(String key, long unixMillis) {
        return pExpire(key, unixMillis - clock.getAsLong());
    }

    public synchronized boolean persist(String key) {
        Entry e = live(key);
        if (e == null || e.expireAt == 0) {
            return false;
        }
        e.expireAt = 0;
        return true;
    }

    /** 剩余毫秒数：-2 不存在，-1 永不过期 */
    public synchronized long pTtl(String key) {
        Entry e = live(key);
        if (e == null) {
            return -2;
        }
        if (e.expireAt == 0) {
            return -1;
        }
        return Math.max(e.expireAt - clock.getAsLong(), 0);
    }

    public synchronized void rename(String from, String to) {
        Entry e = live(from);
        if (e == null) {
            throw new InvalidDataAccessApiUsageException("ERR no such key: " + from);
        }
        data.remove(from);
        data.put(to, e);
    }

    /** KEYS / SCAN：按 Redis glob 规则匹配，返回快照 */
    public synchronized List<String> keys(String globPattern) {
        Pattern p = compile(globPattern);
        long now = clock.getAsLong();
        List<String> out = new ArrayList<>();
        for (Iterator<Map.Entry<String, Entry>> it = data.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Entry> me = it.next();
            Entry e = me.getValue();
            if (e.expireAt > 0 && e.expireAt <= now) {
                it.remove();
                continue;
            }
            if (p.matcher(me.getKey()).matches()) {
                out.add(me.getKey());
            }
        }
        return out;
    }

    public synchronized long dbSize() {
        sweepExpired();
        return data.size();
    }

    public synchronized void flush() {
        data.clear();
    }

    /** 清理已过期的键，返回清理数量 */
    public synchronized int sweepExpired() {
        long now = clock.getAsLong();
        int n = 0;
        for (Iterator<Entry> it = data.values().iterator(); it.hasNext(); ) {
            Entry e = it.next();
            if (e.expireAt > 0 && e.expireAt <= now) {
                it.remove();
                n++;
            }
        }
        return n;
    }

    // ----------------------------------------------------------------------------------------------- String

    public synchronized byte[] get(String key) {
        return stringValue(key);
    }

    public synchronized List<byte[]> mGet(List<String> keys) {
        List<byte[]> out = new ArrayList<>(keys.size());
        for (String k : keys) {
            Entry e = live(k);
            // 与 Redis 一样：MGET 遇到非字符串类型返回 null 而不是报错
            out.add(e != null && e.value instanceof byte[] ? (byte[]) e.value : null);
        }
        return out;
    }

    /**
     * SET 的完整形式。
     *
     * @param ttlMillis  过期毫秒，&lt;= 0 表示不过期
     * @param keepTtl    覆盖时保留原过期时间（SET ... KEEPTTL）
     * @param onlyAbsent NX；onlyPresent XX；两者都 false 为无条件覆盖
     * @return 是否写入
     */
    public synchronized boolean set(String key, byte[] value, long ttlMillis, boolean keepTtl,
                                    boolean onlyAbsent, boolean onlyPresent) {
        Entry old = live(key);
        if (onlyAbsent && old != null || onlyPresent && old == null) {
            return false;
        }
        Entry e = new Entry(value);
        if (ttlMillis > 0) {
            e.expireAt = clock.getAsLong() + ttlMillis;
        } else if (keepTtl && old != null) {
            e.expireAt = old.expireAt;
        }
        data.put(key, e);
        return true;
    }

    public synchronized byte[] getSet(String key, byte[] value) {
        byte[] old = stringValue(key);
        data.put(key, new Entry(value));
        return old;
    }

    public synchronized boolean mSet(Map<String, byte[]> values, boolean onlyIfNoneExists) {
        if (onlyIfNoneExists) {
            for (String k : values.keySet()) {
                if (live(k) != null) {
                    return false;
                }
            }
        }
        values.forEach((k, v) -> data.put(k, new Entry(v)));
        return true;
    }

    public synchronized long incrBy(String key, long delta) {
        Entry e = live(key);
        byte[] cur = stringValue(key);
        long v = (cur == null ? 0 : parseLong(cur, key)) + delta;
        Entry n = new Entry(number(v));
        n.expireAt = e == null ? 0 : e.expireAt;
        data.put(key, n);
        return v;
    }

    public synchronized double incrByFloat(String key, double delta) {
        Entry e = live(key);
        byte[] cur = stringValue(key);
        double v = (cur == null ? 0 : parseDouble(cur, key)) + delta;
        Entry n = new Entry(number(v));
        n.expireAt = e == null ? 0 : e.expireAt;
        data.put(key, n);
        return v;
    }

    public synchronized long append(String key, byte[] value) {
        Entry e = live(key);
        byte[] cur = stringValue(key);
        byte[] merged = cur == null ? value : concat(cur, value);
        Entry n = new Entry(merged);
        n.expireAt = e == null ? 0 : e.expireAt;
        data.put(key, n);
        return merged.length;
    }

    public synchronized long strLen(String key) {
        byte[] v = stringValue(key);
        return v == null ? 0 : v.length;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    // ----------------------------------------------------------------------------------------------- Hash

    /** @return true 表示新增字段，false 表示覆盖已有字段（与 HSET 返回值一致） */
    public synchronized boolean hSet(String key, String field, byte[] value, boolean onlyAbsent) {
        Map<String, byte[]> h = hash(key, true);
        if (onlyAbsent && h.containsKey(field)) {
            return false;
        }
        return h.put(field, value) == null;
    }

    public synchronized void hMSet(String key, Map<String, byte[]> values) {
        if (!values.isEmpty()) {
            hash(key, true).putAll(values);
        }
    }

    public synchronized byte[] hGet(String key, String field) {
        Map<String, byte[]> h = hash(key, false);
        return h == null ? null : h.get(field);
    }

    public synchronized List<byte[]> hMGet(String key, List<String> fields) {
        Map<String, byte[]> h = hash(key, false);
        List<byte[]> out = new ArrayList<>(fields.size());
        for (String f : fields) {
            out.add(h == null ? null : h.get(f));
        }
        return out;
    }

    public synchronized Map<String, byte[]> hGetAll(String key) {
        Map<String, byte[]> h = hash(key, false);
        return h == null ? new LinkedHashMap<>() : new LinkedHashMap<>(h);
    }

    public synchronized long hDel(String key, List<String> fields) {
        Map<String, byte[]> h = hash(key, false);
        if (h == null) {
            return 0;
        }
        long n = 0;
        for (String f : fields) {
            if (h.remove(f) != null) {
                n++;
            }
        }
        dropIfEmpty(key, h);
        return n;
    }

    public synchronized boolean hExists(String key, String field) {
        Map<String, byte[]> h = hash(key, false);
        return h != null && h.containsKey(field);
    }

    public synchronized long hLen(String key) {
        Map<String, byte[]> h = hash(key, false);
        return h == null ? 0 : h.size();
    }

    public synchronized long hStrLen(String key, String field) {
        byte[] v = hGet(key, field);
        return v == null ? 0 : v.length;
    }

    public synchronized long hIncrBy(String key, String field, long delta) {
        Map<String, byte[]> h = hash(key, true);
        byte[] cur = h.get(field);
        long v = (cur == null ? 0 : parseLong(cur, key)) + delta;
        h.put(field, number(v));
        return v;
    }

    public synchronized double hIncrByFloat(String key, String field, double delta) {
        Map<String, byte[]> h = hash(key, true);
        byte[] cur = h.get(field);
        double v = (cur == null ? 0 : parseDouble(cur, key)) + delta;
        h.put(field, number(v));
        return v;
    }

    // ----------------------------------------------------------------------------------------------- List

    public synchronized long push(String key, List<byte[]> values, boolean left, boolean onlyIfExists) {
        LinkedList<byte[]> l = list(key, !onlyIfExists);
        if (l == null) {
            return 0;
        }
        for (byte[] v : values) {
            if (left) {
                l.addFirst(v);
            } else {
                l.addLast(v);
            }
        }
        return l.size();
    }

    public synchronized long lLen(String key) {
        LinkedList<byte[]> l = list(key, false);
        return l == null ? 0 : l.size();
    }

    /** 把 Redis 的下标（支持负数）换算成 [from, to]，越界时返回 null */
    private static int[] range(int size, long start, long stop) {
        long s = start < 0 ? size + start : start;
        long e = stop < 0 ? size + stop : stop;
        s = Math.max(s, 0);
        e = Math.min(e, size - 1L);
        if (s > e || s >= size) {
            return null;
        }
        return new int[]{(int) s, (int) e};
    }

    public synchronized List<byte[]> lRange(String key, long start, long stop) {
        LinkedList<byte[]> l = list(key, false);
        if (l == null) {
            return new ArrayList<>();
        }
        int[] r = range(l.size(), start, stop);
        return r == null ? new ArrayList<>() : new ArrayList<>(l.subList(r[0], r[1] + 1));
    }

    public synchronized void lTrim(String key, long start, long stop) {
        LinkedList<byte[]> l = list(key, false);
        if (l == null) {
            return;
        }
        int[] r = range(l.size(), start, stop);
        if (r == null) {
            data.remove(key);
            return;
        }
        LinkedList<byte[]> kept = new LinkedList<>(l.subList(r[0], r[1] + 1));
        l.clear();
        l.addAll(kept);
    }

    public synchronized byte[] lIndex(String key, long index) {
        LinkedList<byte[]> l = list(key, false);
        if (l == null) {
            return null;
        }
        long i = index < 0 ? l.size() + index : index;
        return i < 0 || i >= l.size() ? null : l.get((int) i);
    }

    public synchronized void lSet(String key, long index, byte[] value) {
        LinkedList<byte[]> l = list(key, false);
        long i = l == null ? -1 : (index < 0 ? l.size() + index : index);
        if (l == null || i < 0 || i >= l.size()) {
            throw new InvalidDataAccessApiUsageException("ERR index out of range: " + key);
        }
        l.set((int) i, value);
    }

    public synchronized byte[] pop(String key, boolean left) {
        LinkedList<byte[]> l = list(key, false);
        if (l == null || l.isEmpty()) {
            return null;
        }
        byte[] v = left ? l.removeFirst() : l.removeLast();
        dropIfEmpty(key, l);
        return v;
    }

    public synchronized long lRem(String key, long count, byte[] value) {
        LinkedList<byte[]> l = list(key, false);
        if (l == null) {
            return 0;
        }
        long removed = 0;
        long limit = count == 0 ? Long.MAX_VALUE : Math.abs(count);
        Iterator<byte[]> it = count < 0 ? l.descendingIterator() : l.iterator();
        while (it.hasNext() && removed < limit) {
            if (Arrays.equals(it.next(), value)) {
                it.remove();
                removed++;
            }
        }
        dropIfEmpty(key, l);
        return removed;
    }

    // ----------------------------------------------------------------------------------------------- Set（成员以 ISO-8859-1 String 保存）

    public synchronized long sAdd(String key, List<String> members) {
        Set<String> s = set(key, true);
        long n = 0;
        for (String m : members) {
            if (s.add(m)) {
                n++;
            }
        }
        return n;
    }

    public synchronized long sRem(String key, List<String> members) {
        Set<String> s = set(key, false);
        if (s == null) {
            return 0;
        }
        long n = 0;
        for (String m : members) {
            if (s.remove(m)) {
                n++;
            }
        }
        dropIfEmpty(key, s);
        return n;
    }

    public synchronized Set<String> sMembers(String key) {
        Set<String> s = set(key, false);
        return s == null ? new LinkedHashSet<>() : new LinkedHashSet<>(s);
    }

    public synchronized boolean sIsMember(String key, String member) {
        Set<String> s = set(key, false);
        return s != null && s.contains(member);
    }

    public synchronized long sCard(String key) {
        Set<String> s = set(key, false);
        return s == null ? 0 : s.size();
    }

    public synchronized String sPop(String key) {
        Set<String> s = set(key, false);
        if (s == null || s.isEmpty()) {
            return null;
        }
        Iterator<String> it = s.iterator();
        String m = it.next();
        it.remove();
        dropIfEmpty(key, s);
        return m;
    }

    // ----------------------------------------------------------------------------------------------- glob

    /** Redis glob（* ? [abc] [^a] [a-z] \x）转成 Java 正则 */
    Pattern compile(String glob) {
        Pattern cached = patternCache.get(glob);
        if (cached != null) {
            return cached;
        }
        Pattern p = Pattern.compile(globToRegex(glob), Pattern.DOTALL);
        if (patternCache.size() >= PATTERN_CACHE_LIMIT) {
            patternCache.clear();
        }
        patternCache.put(glob, p);
        return p;
    }

    static String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder(glob.length() + 8);
        int i = 0;
        int n = glob.length();
        while (i < n) {
            char c = glob.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append('.');
                    break;
                case '\\':
                    if (i + 1 < n) {
                        i++;
                        sb.append(Pattern.quote(String.valueOf(glob.charAt(i))));
                    } else {
                        sb.append("\\\\");
                    }
                    break;
                case '[': {
                    int close = glob.indexOf(']', i + 1);
                    if (close < 0) {
                        sb.append("\\[");
                        break;
                    }
                    String body = glob.substring(i + 1, close);
                    StringBuilder cls = new StringBuilder("[");
                    int j = 0;
                    if (body.startsWith("^")) {
                        cls.append('^');
                        j = 1;
                    }
                    for (; j < body.length(); j++) {
                        char b = body.charAt(j);
                        if (b == '-' && j > 0 && j < body.length() - 1) {
                            cls.append('-');
                        } else if (b == '\\' && j + 1 < body.length()) {
                            j++;
                            cls.append('\\').append(body.charAt(j));
                        } else if (Character.isLetterOrDigit(b)) {
                            cls.append(b);
                        } else {
                            cls.append('\\').append(b);
                        }
                    }
                    cls.append(']');
                    sb.append(cls);
                    i = close;
                    break;
                }
                default:
                    if (Character.isLetterOrDigit(c) || c == ':' || c == '_') {
                        sb.append(c);
                    } else {
                        sb.append(Pattern.quote(String.valueOf(c)));
                    }
            }
            i++;
        }
        return sb.toString();
    }
}
