package com.persagy.apccontrol.opt;

import com.persagy.apccontrol.opt.OptSubscribePlan.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptSubscribePlanTest {

    private static Set<String> set(String... s) {
        return new HashSet<>(Arrays.asList(s));
    }

    @Test
    void firstSyncAddsEverything() {
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("ACCCCC", set("runStatus", "supplyTemp"));
        expected.put("WEFCDD", set());
        OptSubscribePlan plan = OptSubscribePlan.diff(expected, Collections.emptyList());
        Assertions.assertTrue(plan.getToDelete().isEmpty());
        Assertions.assertEquals(set("runStatus", "supplyTemp"), plan.getToAdd().get("ACCCCC"));
        Assertions.assertTrue(plan.getToAdd().get("WEFCDD").isEmpty(), "空集合 = 全部点");
        Assertions.assertEquals(3, plan.addedPointCount());
    }

    @Test
    void alreadyInSyncIsEmpty() {
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("A", set("x"));
        expected.put("B", set());
        List<Rule> existing = Arrays.asList(new Rule("A", "x"), new Rule("B", null));
        Assertions.assertTrue(OptSubscribePlan.diff(expected, existing).isEmpty());
    }

    @Test
    void switchingFromAllToPointsDeletesAllFirst() {
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("A", set("x", "y"));
        OptSubscribePlan plan = OptSubscribePlan.diff(expected, Collections.singletonList(new Rule("A", null)));
        Assertions.assertTrue(plan.getToDelete().get("A").isEmpty(), "删除“全部点”规则");
        Assertions.assertEquals(set("x", "y"), plan.getToAdd().get("A"));
    }

    @Test
    void pointChangesProduceMinimalDiff() {
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("A", set("x", "z"));
        OptSubscribePlan plan = OptSubscribePlan.diff(expected, Arrays.asList(new Rule("A", "x"), new Rule("A", "y")));
        Assertions.assertEquals(set("y"), plan.getToDelete().get("A"));
        Assertions.assertEquals(set("z"), plan.getToAdd().get("A"));
    }

    @Test
    void classesNoLongerNeededAreRemoved() {
        OptSubscribePlan plan = OptSubscribePlan.diff(new HashMap<>(),
                Arrays.asList(new Rule("A", "x"), new Rule("B", null)));
        Assertions.assertEquals(set("x"), plan.getToDelete().get("A"));
        Assertions.assertTrue(plan.getToDelete().get("B").isEmpty());
        Assertions.assertTrue(plan.getToAdd().isEmpty());
    }

    @Test
    void switchingFromPointsToAllOnlyAddsAll() {
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("A", set());
        OptSubscribePlan plan = OptSubscribePlan.diff(expected, Collections.singletonList(new Rule("A", "x")));
        Assertions.assertTrue(plan.getToDelete().isEmpty(), "Twin 加全部点时会自己删除具体点");
        Assertions.assertTrue(plan.getToAdd().get("A").isEmpty());
    }
}
