package com.persagy.apccontrol.basic.model.page;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 默认分页数据为 {@link List} 的实现
 *
 * @author JiYinchuan
 * @see AbstractPageDetail
 * @see Pageable
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DefaultListPage<T extends List<?>> extends AbstractPageDetail<T> implements Serializable {

    private static final long serialVersionUID = -5862802020459053683L;

    /**
     * 数据
     */
    private T records;

    // -------------------------------- Constructor-Default --------------------------------

    /**
     * 构造器
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @since 1.0.0
     */
    public DefaultListPage(int pageNo, int pageSize) {
        super(pageNo, pageSize);
    }

    /**
     * 构造器
     *
     * @param pageNo     页码
     * @param pageSize   每页数量
     * @param totalCount 数据总条数
     * @param records    分页数据
     * @since 1.0.0
     */
    public DefaultListPage(int pageNo, int pageSize, int totalCount, T records) {
        super(pageNo, pageSize, totalCount);
        this.setRecords(records);
    }

    // -------------------------------- Constructor-PageQuery --------------------------------

    /**
     * 构造器
     *
     * @param pageQuery 分页查询对象
     * @since 1.0.0
     */
    public DefaultListPage(PageQuery pageQuery) {
        super(pageQuery);
    }

    /**
     * 构造器
     *
     * @param pageQuery  分页查询对象
     * @param totalCount 数据总条数
     * @param records    分页数据
     * @since 1.0.0
     */
    public DefaultListPage(PageQuery pageQuery, int totalCount, T records) {
        super(pageQuery, totalCount);
        this.setRecords(records);
    }

    /**
     * 构造器
     *
     * @param jsonObject  分页查询对象
     * @param totalCount 数据总条数
     * @param records    分页数据
     * @since 1.0.0
     */
    public DefaultListPage(JSONObject jsonObject, int totalCount, T records) {
        super(new PageQuery(jsonObject.getInteger("page"), jsonObject.getInteger("size")), totalCount);
        this.setRecords(records);
    }

}
