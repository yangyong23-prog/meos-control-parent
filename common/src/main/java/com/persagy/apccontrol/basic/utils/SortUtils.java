package com.persagy.apccontrol.basic.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class SortUtils {

    // 标准排序顺序
    public enum SortOrder {
        ASC, DESC
    }

    // 自定义顺序策略
    public enum MissingOrderPolicy {
        HEAD,  // 不在顺序列表中的元素放在前面
        TAIL   // 不在顺序列表中的元素放在后面（默认）
    }

    /**
     * 按多个比较器顺序排序（原地修改）
     * @param list 待排序列表
     * @param comparators 比较器列表，按优先级顺序排列
     * @param <T> 列表元素类型
     */
    @SafeVarargs
    public static <T> void sortByMultipleComparators(List<T> list, Comparator<T>... comparators) {
        if (CollectionUtils.isEmpty(list) || ArrayUtils.isEmpty(comparators)) {
            return;
        }

        // 组合所有比较器
        Comparator<T> combinedComparator = comparators[0];
        for (int i = 1; i < comparators.length; i++) {
            combinedComparator = combinedComparator.thenComparing(comparators[i]);
        }

        list.sort(combinedComparator);
    }

    /**
     * 排序字段信息类
     */
    public static class SortField<T, U> {
        @NotNull
        final Function<? super T, ? extends U> keyExtractor;
        final List<U> customOrder;
        final SortOrder sortOrder;

        public SortField(Function<? super T, ? extends U> keyExtractor, @Nullable List<U> customOrder) {
            this(keyExtractor, customOrder, SortOrder.ASC);
        }

        public SortField(@NotNull Function<? super T, ? extends U> keyExtractor,
                         @Nullable List<U> customOrder,
                         @Nullable SortOrder sortOrder) {
            this.keyExtractor = keyExtractor;
            this.customOrder = customOrder;
            this.sortOrder = sortOrder;
        }
    }

    /**
     * 按多个字段的自定义顺序排序（原地修改）
     * 示例用法：
     * sortByMultipleCustomOrders(list,
     *     new SortField<>(Person::getLastName, Arrays.asList("Smith", "Johnson", "Williams"), SortOrder.ASC),
     *     new SortField<>(Person::getFirstName, Arrays.asList("John", "Jane", "Bob"), SortOrder.DESC)
     * );
     *
     * @param list 待排序列表
     * @param sortFields 排序字段信息数组
     * @param <T> 列表元素类型
     */
    @SafeVarargs
    public static <T> void sortByMultipleCustomOrders(
            List<T> list,
            SortField<T, ?>... sortFields) {
        sortByMultipleCustomOrders(list, MissingOrderPolicy.TAIL, sortFields);
    }

    /**
     * 按多个字段的自定义顺序排序（原地修改）
     * 示例用法：
     * sortByMultipleCustomOrders(list, 
     *     MissingOrderPolicy.TAIL,
     *     new SortField<>(Person::getLastName, Arrays.asList("Smith", "Johnson", "Williams"), SortOrder.ASC),
     *     new SortField<>(Person::getFirstName, Arrays.asList("John", "Jane", "Bob"), SortOrder.DESC)
     * );
     *
     * @param list 待排序列表
     * @param policy 未出现在顺序列表中的元素处理策略,默认放后面
     * @param sortFields 排序字段信息数组
     * @param <T> 列表元素类型
     */
    @SafeVarargs
    public static <T> void sortByMultipleCustomOrders(
            List<T> list,
            MissingOrderPolicy policy,
            SortField<T, ?>... sortFields) {

        if (CollectionUtils.isEmpty(list) || ArrayUtils.isEmpty(sortFields)) {
            return;
        }

        // 创建组合比较器
        Comparator<T> combinedComparator = createCustomOrderComparator(
                sortFields[0].keyExtractor,
                sortFields[0].customOrder,
                policy,
                sortFields[0].sortOrder);

        for (int i = 1; i < sortFields.length; i++) {
            combinedComparator = combinedComparator.thenComparing(
                    createCustomOrderComparator(
                            sortFields[i].keyExtractor,
                            sortFields[i].customOrder,
                            policy,
                            sortFields[i].sortOrder));
        }

        list.sort(combinedComparator);
    }

    /**
     * 创建自定义顺序比较器的辅助方法
     */
    private static <T> Comparator<T> createCustomOrderComparator(
            Function<? super T, ?> keyExtractor,
            List<?> customOrder,
            MissingOrderPolicy policy,
            SortOrder sortOrder) {

        if (customOrder == null || customOrder.isEmpty()) {
            // 如果没有自定义顺序，则根据升序/降序返回相应比较器
            return (o1, o2) -> {
                Object val1 = keyExtractor.apply(o1);
                Object val2 = keyExtractor.apply(o2);

                // 空值处理：空值保持原有顺序（视为相等）
                if (val1 == null && val2 == null) return 0;
                if (val1 == null) return 0;  // 空值保持原有顺序
                if (val2 == null) return 0;  // 空值保持原有顺序

                // 非空值比较
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    int result = ((Comparable) val1).compareTo(val2);
                    return sortOrder == SortOrder.ASC ? result : -result;
                }
                return 0; // 无法比较时保持原有顺序
            };
        }

        // 创建顺序映射：值 -> 位置索引
        Map<Object, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < customOrder.size(); i++) {
            orderMap.put(customOrder.get(i), i);
        }

        return (o1, o2) -> {
            Object val1 = keyExtractor.apply(o1);
            Object val2 = keyExtractor.apply(o2);

            // 空值处理：空值保持原有顺序（视为相等）
            if (val1 == null && val2 == null) return 0;
            if (val1 == null) return 0;  // 空值保持原有顺序
            if (val2 == null) return 0;  // 空值保持原有顺序

            boolean inOrder1 = orderMap.containsKey(val1);
            boolean inOrder2 = orderMap.containsKey(val2);

            // 两个值都在自定义顺序中
            if (inOrder1 && inOrder2) {
                int result = Integer.compare(orderMap.get(val1), orderMap.get(val2));
                return sortOrder == SortOrder.ASC ? result : -result;
            }

            // 两个值都不在自定义顺序中
            if (!inOrder1 && !inOrder2) {
                return 0; // 保持原始相对顺序
            }

            // 一个在一个不在：根据策略处理
            int result = (policy == MissingOrderPolicy.TAIL) ?
                    (inOrder1 ? -1 : 1) : // 在列表中的排前面
                    (inOrder1 ? 1 : -1);  // 不在列表中的排前面

            return sortOrder == SortOrder.ASC ? result : -result;
        };
    }

    /**
     * 按自定义顺序排序（原地修改）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param customOrder 自定义顺序列表
     * @param policy 未出现在顺序列表中的元素处理策略
     */
    public static <T, U> void sortByCustomOrder(
            List<T> list,
            Function<? super T, ? extends U> keyExtractor,
            List<U> customOrder,
            MissingOrderPolicy policy) {
        sortByCustomOrder(list, keyExtractor, customOrder, policy, SortOrder.ASC);
    }

    /**
     * 按自定义顺序排序（原地修改）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param customOrder 自定义顺序列表
     * @param policy 未出现在顺序列表中的元素处理策略
     * @param sortOrder 排序顺序（升序或降序）
     */
    public static <T, U> void sortByCustomOrder(
            List<T> list,
            Function<? super T, ? extends U> keyExtractor,
            List<U> customOrder,
            MissingOrderPolicy policy,
            SortOrder sortOrder) {

        if (list == null || list.isEmpty()) return;

        if (customOrder == null || customOrder.isEmpty()) {
            // 没有自定义顺序，按自然顺序排序
            Comparator<T> comparator = (o1, o2) -> {
                U val1 = keyExtractor.apply(o1);
                U val2 = keyExtractor.apply(o2);

                // 空值处理：空值保持原有顺序（视为相等）
                if (val1 == null && val2 == null) return 0;
                if (val1 == null) return 0;
                if (val2 == null) return 0;

                // 非空值比较
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    int result = ((Comparable) val1).compareTo(val2);
                    return sortOrder == SortOrder.ASC ? result : -result;
                }
                return 0; // 无法比较时保持原有顺序
            };
            list.sort(comparator);
            return;
        }

        // 创建顺序映射：值 -> 位置索引
        Map<U, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < customOrder.size(); i++) {
            orderMap.put(customOrder.get(i), i);
        }

        list.sort((o1, o2) -> {
            U val1 = keyExtractor.apply(o1);
            U val2 = keyExtractor.apply(o2);

            // 空值处理：空值保持原有顺序（视为相等）
            if (val1 == null && val2 == null) return 0;
            if (val1 == null) return 0;
            if (val2 == null) return 0;

            boolean inOrder1 = orderMap.containsKey(val1);
            boolean inOrder2 = orderMap.containsKey(val2);

            // 两个值都在自定义顺序中
            if (inOrder1 && inOrder2) {
                int result = Integer.compare(orderMap.get(val1), orderMap.get(val2));
                return sortOrder == SortOrder.ASC ? result : -result;
            }

            // 两个值都不在自定义顺序中
            if (!inOrder1 && !inOrder2) {
                return 0; // 保持原始相对顺序
            }

            // 一个在一个不在：根据策略处理
            int result = (policy == MissingOrderPolicy.TAIL) ?
                    (inOrder1 ? -1 : 1) : // 在列表中的排前面
                    (inOrder1 ? 1 : -1);  // 不在列表中的排前面

            return sortOrder == SortOrder.ASC ? result : -result;
        });
    }

    /**
     * 按自定义顺序排序（返回新列表）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param customOrder 自定义顺序列表
     * @param policy 未出现在顺序列表中的元素处理策略
     * @return 排序后的新列表
     */
    @NotNull
    public static <T, U> List<T> sortedByCustomOrder(
            List<T> list,
            Function<? super T, ? extends U> keyExtractor,
            List<U> customOrder,
            MissingOrderPolicy policy) {
        return sortedByCustomOrder(list, keyExtractor, customOrder, policy, SortOrder.ASC);
    }

    /**
     * 按自定义顺序排序（返回新列表）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param customOrder 自定义顺序列表
     * @param policy 未出现在顺序列表中的元素处理策略
     * @param sortOrder 排序顺序（升序或降序）
     * @return 排序后的新列表
     */
    @NotNull
    public static <T, U> List<T> sortedByCustomOrder(
            List<T> list,
            Function<? super T, ? extends U> keyExtractor,
            List<U> customOrder,
            MissingOrderPolicy policy,
            SortOrder sortOrder) {

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<T> newList = new ArrayList<>(list);
        sortByCustomOrder(newList, keyExtractor, customOrder, policy, sortOrder);
        return newList;
    }

    /**
     * 标准排序（原地修改）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param order 排序顺序
     */
    public static <T, U extends Comparable<? super U>> void sort(
            List<T> list,
            Function<? super T, ? extends U> keyExtractor,
            SortOrder order) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Comparator<T> comparator = Comparator.comparing(keyExtractor);
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);
    }

    /**
     * 标准排序（返回新列表）
     * @param list 待排序列表
     * @param keyExtractor 字段提取函数
     * @param order 排序顺序
     * @return 排序后的新列表
     */
    @NotNull
    public static <T, U extends Comparable<? super U>> List<T> sorted(
            Collection<T> list,
            Function<? super T, ? extends U> keyExtractor,
            SortOrder order) {

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<T> newList = new ArrayList<>(list);
        sort(newList, keyExtractor, order);
        return newList;
    }
}