package com.persagy.apccontrol.basic.opt;

import java.util.List;
import java.util.function.Predicate;

/**
 * opt 分支 B10：“逐个尝试候选，找到第一个可用的就停”的循环，改为先批量取齐数据再挑选。
 * <p>
 * 原循环对每个候选都写一次结果，找到可用的即停；找不到时最终留下的是最后一个候选的结果。
 * {@link #firstOrLast} 返回的就是原循环最后一次写入所对应的候选，调用方只需对它写一次结果。
 */
public final class OptCandidates {

    private OptCandidates() {
    }

    /** @return 第一个满足条件的候选；都不满足时返回最后一个；空列表返回 null */
    public static <T> T firstOrLast(List<T> candidates, Predicate<? super T> usable) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        for (T c : candidates) {
            if (usable.test(c)) {
                return c;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
