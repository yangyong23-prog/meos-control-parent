package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class OptCandidatesTest {

    /** 原循环：对每个候选写一次结果，可跳转即停；返回最后一次写入的候选和 SQL 次数 */
    private static Object[] originalLoop(List<String> candidates, Set<String> jumpable) {
        String last = null;
        int queries = 0;
        for (String c : candidates) {
            queries++;
            last = c;
            if (jumpable.contains(c)) {
                break;
            }
        }
        return new Object[]{last, queries};
    }

    @Test
    void sameChoiceAsOriginalLoopOnRandomCases() {
        Random r = new Random(42);
        for (int round = 0; round < 500; round++) {
            List<String> candidates = new ArrayList<>();
            Set<String> jumpable = new HashSet<>();
            int n = r.nextInt(8);
            for (int i = 0; i < n; i++) {
                candidates.add("O" + i);
                if (r.nextInt(4) == 0) {
                    jumpable.add("O" + i);
                }
            }
            Object[] before = originalLoop(candidates, jumpable);
            Assertions.assertEquals(before[0], OptCandidates.firstOrLast(candidates, jumpable::contains),
                    "候选 " + candidates + " 可跳转 " + jumpable);
        }
    }

    @Test
    void noneUsableFallsBackToLast() {
        List<String> c = new ArrayList<>();
        c.add("a");
        c.add("b");
        Assertions.assertEquals("b", OptCandidates.firstOrLast(c, x -> false));
        Assertions.assertNull(OptCandidates.firstOrLast(Collections.<String>emptyList(), x -> true));
    }
}
