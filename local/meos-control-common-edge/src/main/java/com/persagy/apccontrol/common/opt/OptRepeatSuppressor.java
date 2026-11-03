package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * opt 分支 B8（switch.opt.lightingSendDiff，默认关）：周期性下发的指令“相同不重发”。
 * <p>
 * 每次 {@link #filter} 视为该 scope 的一轮。同一对象的指令内容与上次下发相同，且距上次下发不到
 * {@code opt.lighting.resend-rounds}（默认 6 轮，5 分钟一轮即 30 分钟）时不再下发；内容变化（包括清除指令）立即下发；
 * 到达轮数强制重发一次，覆盖设备未执行到位的情况。进程内记录，重启后首轮全部下发。
 */
@Component
public class OptRepeatSuppressor {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .findAndRegisterModules();

    private final Map<String, AtomicLong> rounds = new ConcurrentHashMap<>();
    private final Map<String, Sent> sent = new ConcurrentHashMap<>();

    @Value("${opt.lighting.resend-rounds:6}")
    private int resendRounds = 6;

    void setResendRounds(int resendRounds) {
        this.resendRounds = resendRounds;
    }

    /**
     * @return 本轮需要下发的指令；开关关闭时原样返回
     */
    public <T> List<T> filter(String scope, List<T> orders, Function<T, String> objectIdOf) {
        if (!OptSwitch.on(OptSwitch.LIGHTING_SEND_DIFF) || orders == null || orders.isEmpty()) {
            return orders;
        }
        long round = rounds.computeIfAbsent(scope, k -> new AtomicLong()).incrementAndGet();
        List<T> result = new ArrayList<>(orders.size());
        for (T order : orders) {
            String id = objectIdOf.apply(order);
            String digest = digest(order);
            if (id == null || digest == null) {
                result.add(order);
                continue;
            }
            String key = scope + "|" + id;
            Sent last = sent.get(key);
            if (last != null && last.digest.equals(digest) && round - last.round < resendRounds) {
                OptMetrics.inc("lighting.sendSkipped");
                continue;
            }
            sent.put(key, new Sent(digest, round));
            result.add(order);
        }
        return result;
    }

    /**
     * 过滤后下发。开关关闭时原样调用 plainSender（与原逻辑一致）；打开时调用 checkedSender，
     * 其返回 false 或抛异常都视为下发失败，本次指令全部 {@link #forget}，下一轮重新下发。
     *
     * @return 开关打开时本次是否成功（无需下发也算成功）
     */
    public <T> boolean send(String scope, List<T> orders, Function<T, String> objectIdOf,
                            Consumer<List<T>> plainSender, Predicate<List<T>> checkedSender) {
        if (!OptSwitch.on(OptSwitch.LIGHTING_SEND_DIFF)) {
            if (orders != null && !orders.isEmpty()) {
                plainSender.accept(orders);
            }
            return true;
        }
        List<T> toSend = filter(scope, orders, objectIdOf);
        if (toSend == null || toSend.isEmpty()) {
            return true;
        }
        boolean ok;
        try {
            ok = checkedSender.test(toSend);
        } catch (RuntimeException e) {
            forget(scope, toSend, objectIdOf);
            OptMetrics.incAlways("lighting.sendFail");
            throw e;
        }
        if (!ok) {
            forget(scope, toSend, objectIdOf);
            OptMetrics.incAlways("lighting.sendFail");
        }
        return ok;
    }

    /** 下发失败时调用，使这些对象下一轮重新下发 */
    public <T> void forget(String scope, List<T> orders, Function<T, String> objectIdOf) {
        if (orders != null) {
            orders.stream().map(objectIdOf).filter(Objects::nonNull).forEach(id -> sent.remove(scope + "|" + id));
        }
    }

    private static String digest(Object order) {
        try {
            return MAPPER.writeValueAsString(order);
        } catch (Exception e) {
            return null;
        }
    }

    private static final class Sent {
        final String digest;
        final long round;

        Sent(String digest, long round) {
            this.digest = digest;
            this.round = round;
        }
    }
}
