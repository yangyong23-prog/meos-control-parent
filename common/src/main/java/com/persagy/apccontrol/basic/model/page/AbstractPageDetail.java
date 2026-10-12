package com.persagy.apccontrol.basic.model.page;

import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

/**
 * 分页参数抽象类, 定义了默认的分页参数
 *
 * <p>框架默认提供了实现类, 如不满足使用需要可以自行继承该类:
 * <ul>
 *     <li><b>{@link DefaultListPage}</b> - 默认分页对象, 其分页数据为 {@link java.util.List}</li>
 *     <li><b>{@link DefaultSetPage}</b> - 默认分页对象, 其分页数据为 {@link java.util.Set}</li>
 * </ul>
 *
 * @author JiYinchuan
 * @see Pageable
 * @see DefaultListPage
 * @see DefaultSetPage
 * @since 1.0.0
 */
@ToString
@EqualsAndHashCode
public abstract class AbstractPageDetail<T extends Collection<?>> implements Pageable<T>, Serializable {

    private static final long serialVersionUID = -7894974711113638539L;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 数据总条数
     */
    private Integer totalCount;

    /**
     * 构造器
     *
     * @since 1.0.0
     */
    protected AbstractPageDetail() {
        this.pageNo = DEFAULT_PAGE_NO;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    /**
     * 构造器
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @since 1.0.0
     */
    protected AbstractPageDetail(int pageNo, int pageSize) {
        this();
        setPageSize(pageSize);
        setPageNo(pageNo);
    }

    /**
     * 构造器
     *
     * @param pageNo     页码
     * @param pageSize   每页数量
     * @param totalCount 数据总条数
     * @since 1.0.0
     */
    protected AbstractPageDetail(int pageNo, int pageSize, int totalCount) {
        this();
        setTotalCount(totalCount);
        setPageSize(pageSize);
        setPageNo(pageNo);
    }

    /**
     * 构造器
     *
     * @param pageQuery 分页查询对象
     * @since 1.0.0
     */
    protected AbstractPageDetail(PageQuery pageQuery) {
        this();
        setPageSize(pageQuery.getPageSize());
        setPageNo(pageQuery.getPageNo());
    }

    /**
     * 构造器
     *
     * @param pageQuery  分页查询对象
     * @param totalCount 数据总条数
     * @since 1.0.0
     */
    protected AbstractPageDetail(PageQuery pageQuery, int totalCount) {
        this();
        setTotalCount(totalCount);
        setPageSize(pageQuery.getPageSize());
        setPageNo(pageQuery.getPageNo());
    }

    @Override
    public Integer getTotalCount() {
        return totalCount;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getPageNo() {
        return pageNo;
    }

    @Override
    public Integer getTotalPage() {
        if (Objects.isNull(totalCount)) {
            return null;
        }
        int totalPage = (totalCount / pageSize);
        if (totalPage == 0 || totalCount % pageSize != 0) {
            totalPage++;
        }
        return totalPage;
    }

    @Override
    public boolean isFirstPage() {
        return pageNo <= 1;
    }

    @Override
    public Boolean isLastPage() {
        Integer totalPage = getTotalPage();
        if (Objects.isNull(totalPage)) {
            return null;
        }
        return pageNo >= totalPage;
    }

    @Override
    public int getNextPage() {
        if (isLastPage()) {
            return pageNo;
        } else {
            return pageNo + 1;
        }
    }

    @Override
    public int getPrePage() {
        if (isFirstPage()) {
            return pageNo;
        } else {
            return pageNo - 1;
        }
    }

    /**
     * 设置数据总条数
     *
     * @param totalCount 数据总条数
     * @return 当前实例
     * @since 1.0.0
     */
    public AbstractPageDetail<T> setTotalCount(int totalCount) {
        this.totalCount = Math.max(totalCount, 0);
        adjustPageNo();
        return this;
    }

    /**
     * 设置每页数量
     *
     * @param pageSize 每页数量
     * @return 当前实例
     * @since 1.0.0
     */
    public AbstractPageDetail<T> setPageSize(int pageSize) {
        if (pageSize < 1) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else {
            this.pageSize = Math.min(pageSize, DEFAULT_MAX_PAGE_SIZE);
        }
        adjustPageNo();
        return this;
    }

    /**
     * 设置页码
     *
     * @param pageNo 分页页码
     * @return 当前实例
     * @since 1.0.0
     */
    public AbstractPageDetail<T> setPageNo(int pageNo) {
        this.pageNo = Math.max(pageNo, DEFAULT_PAGE_NO);
        adjustPageNo();
        return this;
    }

    /**
     * 调整页码，使不超过最大页数
     *
     * @since 1.0.0
     */
    private void adjustPageNo() {
        if (pageNo == 1) {
            return;
        }
        Integer totalPage = getTotalPage();
        if (Objects.isNull(totalPage)) {
            return;
        }
        if (pageNo > totalPage) {
            pageNo = totalPage;
        }
    }
}
