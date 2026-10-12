package com.persagy.apccontrol.basic.model.page.query;

import com.persagy.apccontrol.basic.model.page.Pageable;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 分页查询基类
 *
 * <p>该类为分页查询基类, 所有带分页参数的请求实体继承此类, 此类也可以单独进行使用
 * <p>已默认提供一个基础分页查询实体 {@link SimplePageQuery}
 *
 * @author JiYinchuan
 * @see Pageable
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class PageQuery implements Serializable {

    private static final long serialVersionUID = -8896344736429182949L;

    /**
     * 页码
     */
    protected Integer pageNo;

    /**
     * 每页展示数量
     */
    protected Integer pageSize;

    /**
     * 是否只统计数量, 默认 [false]
     */
    protected Boolean onlyCount;

    /**
     * 构造器
     *
     * @since 1.0.0
     */
    public PageQuery() {
    }

    /**
     * 构造器
     *
     * @param pageNo   页码
     * @param pageSize 每页展示数量
     * @since 1.0.0
     */
    public PageQuery(Integer pageNo, Integer pageSize) {
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
    }
}
