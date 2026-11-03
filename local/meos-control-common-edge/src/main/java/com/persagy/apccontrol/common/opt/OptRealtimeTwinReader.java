package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.common.opt.OptRealtimeCacheCore.Lookup;
import com.persagy.apccontrol.common.opt.OptRealtimeCacheCore.PointKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * B2 读路径：Twin on-moment 查询先读共享缓存（开关 {@code switch.opt.realtimeCacheRead}：off / verify / on）。
 *
 * <ul>
 *     <li>off（默认）：不介入。</li>
 *     <li>verify：仍以 Twin 结果为准返回，同时读缓存比对，计数 rt.verify.* 并限频打印不一致样例（“新路径读、旧路径校验”）。</li>
 *     <li>on：缓存命中且可用的点直接返回；负缓存命中的点返回当时 Twin 的原样记录；其余按 ≤200 个对象分片回源 Twin，
 *     有效结果回填缓存，仍无有效值的点写 60 秒负缓存。</li>
 * </ul>
 * 只处理“当前值”查询：指定了 twinTime / unit、信息点列表为空、或 rcRunStatus 的请求原样走 Twin。
 * 返回的 DtpTwinIotData 形状与 Twin 一致（dataFlag/dataStatus=1 表示有效），调用方的过滤逻辑不变。
 */
@Slf4j
@Component
public class OptRealtimeTwinReader {

    public static final String MODE_OFF = "off";
    public static final String MODE_VERIFY = "verify";
    public static final String MODE_ON = "on";

    static final int TWIN_OBJECT_BATCH = 200;
    private static final long SAMPLE_LOG_INTERVAL_MS = 60_000L;

    private final OptRealtimeCacheCore core;
    private final AtomicLong lastSampleLog = new AtomicLong();

    public OptRealtimeTwinReader(OptRealtimeCacheCore core) {
        this.core = core;
    }

    public static String mode() {
        return OptSwitch.value(OptSwitch.REALTIME_CACHE_READ, MODE_OFF);
    }

    /**
     * 是否由本类处理该请求。
     */
    public boolean handles(QueryTwinDataDTO dto) {
        String mode = mode();
        return (MODE_ON.equals(mode) || MODE_VERIFY.equals(mode))
                && dto != null && dto.getTwinTime() == null && dto.getUnit() == null
                && dto.getObjects() != null && !dto.getObjects().isEmpty();
    }

    /**
     * @param twin 原始 Twin 查询（不含前端过期提示等附加处理）
     */
    public List<DtpTwinIotData> query(QueryTwinDataDTO dto, Function<QueryTwinDataDTO, List<DtpTwinIotData>> twin) {
        Split split = split(dto);
        if (MODE_VERIFY.equals(mode())) {
            List<DtpTwinIotData> twinResult = twin.apply(dto);
            verify(split.points, twinResult);
            return twinResult;
        }
        return queryOn(split, twin);
    }

    // ------------------------------------------------------------------ on

    private List<DtpTwinIotData> queryOn(Split split, Function<QueryTwinDataDTO, List<DtpTwinIotData>> twin) {
        Lookup lookup = core.lookup(split.points);
        List<DtpTwinIotData> result = new ArrayList<>();
        for (Map.Entry<PointKey, OptRealtimeValue> hit : lookup.getHits().entrySet()) {
            result.add(toTwin(hit.getValue()));
        }
        for (Object neg : lookup.getNegatives().values()) {
            if (neg instanceof DtpTwinIotData) {
                result.add((DtpTwinIotData) neg);
            }
        }

        // 回源：未命中的点 + 不走缓存的对象
        Map<String, Set<String>> toTwin = new LinkedHashMap<>();
        for (PointKey miss : lookup.getMisses()) {
            toTwin.computeIfAbsent(miss.getObjectId(), k -> new LinkedHashSet<>()).add(miss.getInfoCode());
        }
        List<QueryTwinDataOnMomentDTO> twinObjects = new ArrayList<>(split.passthrough);
        for (Map.Entry<String, Set<String>> e : toTwin.entrySet()) {
            twinObjects.add(new QueryTwinDataOnMomentDTO(split.projectIdOf.get(e.getKey()), e.getKey(), e.getValue()));
        }
        if (twinObjects.isEmpty()) {
            return result;
        }

        List<DtpTwinIotData> fetched = new ArrayList<>();
        try {
            for (int from = 0; from < twinObjects.size(); from += TWIN_OBJECT_BATCH) {
                List<QueryTwinDataOnMomentDTO> part = twinObjects.subList(from, Math.min(twinObjects.size(), from + TWIN_OBJECT_BATCH));
                List<DtpTwinIotData> r = twin.apply(new QueryTwinDataDTO(new ArrayList<>(part)));
                OptMetrics.inc("rt.read.twinCall");
                if (r != null) {
                    fetched.addAll(r);
                }
            }
        } catch (RuntimeException e) {
            // 与原逻辑一致：回源失败整体抛出，不返回只含缓存命中的残缺结果
            OptMetrics.incAlways("rt.read.twinFail");
            throw e;
        }
        result.addAll(fetched);
        backfill(lookup.getMisses(), fetched);
        return result;
    }

    private void backfill(List<PointKey> misses, List<DtpTwinIotData> fetched) {
        Map<PointKey, DtpTwinIotData> byPoint = new HashMap<>();
        for (DtpTwinIotData d : fetched) {
            byPoint.put(new PointKey(d.getObjectId(), d.getInfoCode()), d);
        }
        for (PointKey miss : misses) {
            DtpTwinIotData d = byPoint.get(miss);
            if (isValid(d)) {
                core.write(fromTwin(d));
            } else {
                core.markNegative(miss, d);
            }
        }
    }

    // ------------------------------------------------------------------ verify

    private void verify(List<PointKey> points, List<DtpTwinIotData> twinResult) {
        try {
            Map<PointKey, String> twinValues = new HashMap<>();
            if (twinResult != null) {
                for (DtpTwinIotData d : twinResult) {
                    if (isValid(d)) {
                        twinValues.put(new PointKey(d.getObjectId(), d.getInfoCode()), d.getDataValue());
                    }
                }
            }
            Lookup lookup = core.lookup(points);
            for (PointKey p : points) {
                OptRealtimeValue cached = lookup.getHits().get(p);
                String twinValue = twinValues.get(p);
                String cacheValue = cached == null ? null : cached.getDataValue();
                Outcome outcome = compare(twinValue, cacheValue);
                OptMetrics.incAlways("rt.verify." + outcome.name());
                if (outcome == Outcome.mismatch) {
                    sampleLog(p, twinValue, cached);
                }
            }
        } catch (Exception e) {
            OptMetrics.incAlways("rt.verify.error");
            log.warn("[opt] 实时值双读校验异常: {}", e.toString());
        }
    }

    enum Outcome { match, bothEmpty, cacheMiss, mismatch }

    static Outcome compare(String twinValue, String cacheValue) {
        if (twinValue == null && cacheValue == null) {
            return Outcome.bothEmpty;
        }
        if (cacheValue == null) {
            return Outcome.cacheMiss;
        }
        if (twinValue == null) {
            return Outcome.mismatch;
        }
        return sameValue(twinValue, cacheValue) ? Outcome.match : Outcome.mismatch;
    }

    static boolean sameValue(String a, String b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        try {
            return new BigDecimal(a.trim()).compareTo(new BigDecimal(b.trim())) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sampleLog(PointKey p, String twinValue, OptRealtimeValue cached) {
        long now = System.currentTimeMillis();
        long last = lastSampleLog.get();
        if (now - last >= SAMPLE_LOG_INTERVAL_MS && lastSampleLog.compareAndSet(last, now)) {
            log.warn("[opt] 实时值双读不一致 {} twin={} cache={}", p, twinValue, cached);
        }
    }

    // ------------------------------------------------------------------ 转换

    static boolean isValid(DtpTwinIotData d) {
        return d != null && d.getDataFlag() != null && d.getDataFlag() == 1
                && d.getDataStatus() != null && d.getDataStatus() == 1;
    }

    static DtpTwinIotData toTwin(OptRealtimeValue v) {
        DtpTwinIotData d = new DtpTwinIotData();
        d.setObjectId(v.getObjectId());
        d.setClassCode(v.getClassCode());
        d.setInfoCode(v.getInfoCode());
        d.setDataTime(v.getDataTime());
        d.setDataValue(v.getDataValue());
        // DtpTwinIotData.getVaildIotDatas 对 dataIndate 做 Integer.valueOf，空值用 "0"（不过期）
        d.setDataIndate(v.getDataIndate() == null ? "0" : String.valueOf(v.getDataIndate()));
        d.setDataFlag(1);
        d.setDataStatus(1);
        return d;
    }

    static OptRealtimeValue fromTwin(DtpTwinIotData d) {
        OptRealtimeValue v = new OptRealtimeValue();
        v.setObjectId(d.getObjectId());
        v.setClassCode(d.getClassCode());
        v.setInfoCode(d.getInfoCode());
        v.setDataValue(d.getDataValue());
        v.setDataTime(d.getDataTime());
        try {
            v.setDataIndate(d.getDataIndate() == null ? null : Integer.valueOf(d.getDataIndate().trim()));
        } catch (NumberFormatException e) {
            v.setDataIndate(null);
        }
        v.setDataStatus(d.getDataStatus());
        v.setSource(OptRealtimeValue.SOURCE_TWIN);
        return v;
    }

    // ------------------------------------------------------------------ 请求拆分

    static final class Split {
        final List<PointKey> points = new ArrayList<>();
        final List<QueryTwinDataOnMomentDTO> passthrough = new ArrayList<>();
        final Map<String, String> projectIdOf = new HashMap<>();
    }

    static Split split(QueryTwinDataDTO dto) {
        Split split = new Split();
        for (QueryTwinDataOnMomentDTO o : dto.getObjects()) {
            if (o == null || o.getObjectId() == null) {
                continue;
            }
            Collection<String> infoCodes = o.getInfoCodes();
            if (infoCodes == null || infoCodes.isEmpty()) {
                split.passthrough.add(o);
                continue;
            }
            Set<String> passthroughCodes = new LinkedHashSet<>();
            for (String infoCode : infoCodes) {
                if (infoCode == null || InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCode)) {
                    passthroughCodes.add(infoCode);
                } else {
                    split.points.add(new PointKey(o.getObjectId(), infoCode));
                }
            }
            if (!passthroughCodes.isEmpty()) {
                split.passthrough.add(new QueryTwinDataOnMomentDTO(o.getProjectId(), o.getObjectId(), passthroughCodes));
            }
            split.projectIdOf.putIfAbsent(o.getObjectId(), o.getProjectId());
        }
        return split;
    }
}
