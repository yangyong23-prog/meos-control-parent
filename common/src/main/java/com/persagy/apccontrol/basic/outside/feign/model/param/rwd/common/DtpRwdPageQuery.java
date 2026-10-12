package com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common;

import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 物理世界查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@Accessors(chain = true)
public class DtpRwdPageQuery {

    /**
     * 分页页码
     */
    private Integer page;

    /**
     * 分页大小
     */
    private Integer size;

    /**
     * 根据分页参数填充
     *
     * @param pageQuery 分页参数
     * @return 当前实例
     * @since 7.250201.0
     */
    public DtpRwdPageQuery fillByPageQuery(@Nullable PageQuery pageQuery) {
        if (Objects.isNull(pageQuery)) {
            return this;
        }
        setPage(pageQuery.getPageNo());
        setSize(pageQuery.getPageSize());
        return this;
    }

    /**
     * 转换分页参数
     *
     * @return 分页参数
     * @since 7.250201.0
     */
    public PageQuery toPageQuery() {
        return new PageQuery(getPage(), getSize());
    }
}
