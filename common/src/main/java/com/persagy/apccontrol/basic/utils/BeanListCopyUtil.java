package com.persagy.apccontrol.basic.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2025/3/24 18:33
　　 * @description: List集合的拷贝
*/

public class BeanListCopyUtil {


    /**
     * 集合数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     * @return
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }


    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources:  数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调函数
     * @return
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanCopyUtilCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            BeanUtils.copyProperties(source, t);
            list.add(t);
            if (callBack != null) {
                // 回调
                callBack.callBack(source, t);
            }
        }
        return list;
    }

    @FunctionalInterface
    public interface BeanCopyUtilCallBack<S, T> {
        void callBack(S t, T s);
    }


    /**
     * 支持递归深度拷贝的工具方法
     *
     * @param sources 源数据列表
     * @param target  目标类构造器
     * @param childGetter 获取子节点的方法引用 (源对象 -> 子节点列表)
     * @param childSetter 设置子节点的方法引用 (目标对象, 子节点列表) -> void
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 拷贝后的列表
     */
    public static <S, T> List<T> deepCopyListProperties(
            List<S> sources,
            Supplier<T> target,
            Function<S, List<S>> childGetter,
            BiConsumer<T, List<T>> childSetter) {

        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            BeanUtils.copyProperties(source, t);
            list.add(t);

            // 递归处理子节点
            List<S> children = childGetter.apply(source);
            if (children != null && !children.isEmpty()) {
                List<T> copiedChildren = deepCopyListProperties(children, target, childGetter, childSetter);
                childSetter.accept(t, copiedChildren);
            }
        }
        return list;
    }


    /**
     * IPage数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     */
    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target) {
        return new Page<T>()
                .setSize(sources.getSize())
                .setCurrent(sources.getCurrent())
                .setTotal(sources.getTotal())
                .setPages(sources.getPages())
                .setRecords(copyListProperties(sources.getRecords(), target));
    }


    /**
     * IPage数据的拷贝，带回调
     *
     * @param sources:  数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调函数
     */
    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target, BeanCopyUtilCallBack<S, T> callBack) {
        return new Page<T>()
                .setSize(sources.getSize())
                .setCurrent(sources.getCurrent())
                .setTotal(sources.getTotal())
                .setPages(sources.getPages())
                .setRecords(copyListProperties(sources.getRecords(), target, callBack));
    }
}


