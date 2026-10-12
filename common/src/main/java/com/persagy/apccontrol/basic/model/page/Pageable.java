package com.persagy.apccontrol.basic.model.page;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;

/**
 * 标准分页接口
 *
 * <p>该接口为业务标准分页接口, 已默认提供实现类如下:
 * <ul>
 *     <li><b>{@link AbstractPageDetail}</b> - 分页参数抽象类, 定义了分页相关的参数</li>
 *     <li><b>{@link DefaultListPage}</b> - 最终实现对象, 继承至 {@link AbstractPageDetail}, 其分页数据为 {@link java.util.List}</li>
 *     <li><b>{@link DefaultSetPage}</b> - 最终实现对象, 继承至 {@link AbstractPageDetail}, 其分页数据为 {@link java.util.Set}</li>
 * </ul>
 *
 * @author JiYinchuan
 * @see AbstractPageDetail
 * @see DefaultListPage
 * @see DefaultSetPage
 * @since 1.0.0
 */
public interface Pageable<T extends Collection<?>> {

    /**
     * 默认页码
     */
    int DEFAULT_PAGE_NO = 1;

    /**
     * 默认每页展示数量
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 默认最大每页数量
     */
    int DEFAULT_MAX_PAGE_SIZE = 1000;

    /**
     * 默认数据总条数
     */
    int DEFAULT_TOTAL_COUNT = 0;

    /**
     * 页码
     *
     * @return 页码
     * @since 1.0.0
     */
    int getPageNo();

    /**
     * 每页记录数
     *
     * @return 每页记录数
     * @since 1.0.0
     */
    int getPageSize();

    /**
     * 总记录数
     *
     * @return 总记录数
     * @since 1.0.0
     */
    Integer getTotalCount();

    /**
     * 总页数
     *
     * @return 总页数
     * @since 1.0.0
     */
    Integer getTotalPage();

    /**
     * 是否第一页
     *
     * @return 是否第一页
     * @since 1.0.0
     */
    boolean isFirstPage();

    /**
     * 是否最后一页
     *
     * @return 是否最后一页
     * @since 1.0.0
     */
    Boolean isLastPage();

    /**
     * 返回下页的页号
     *
     * @return 返回下页的页号
     * @since 1.0.0
     */
    int getNextPage();

    /**
     * 返回上页的页号
     *
     * @return 返回上页的页号
     * @since 1.0.0
     */
    int getPrePage();

    /**
     * 分页数据
     *
     * @return 分页数据
     * @since 1.0.0
     */
    T getRecords();

    /**
     * 返回数据库查询起始索引位置
     *
     * @return 数据库查询起始索引位置
     * @since 1.0.0
     */
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    default int getOffset() {
        return getPageNo() > 0 ? (getPageNo() - 1) * getPageSize() : 0;
    }
}
