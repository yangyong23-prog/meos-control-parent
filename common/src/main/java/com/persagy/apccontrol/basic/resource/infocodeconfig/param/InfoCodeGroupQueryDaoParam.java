package com.persagy.apccontrol.basic.resource.infocodeconfig.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 信息点分组查询参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class InfoCodeGroupQueryDaoParam extends BaseRepositoryQueryParam<InfoCodeGroup> {

    /**
     * ID
     */
    private Set<Long> ids;

    /**
     * 分组编码
     */
    private Set<String> groupCodes;

    /**
     * 父级ID
     */
    private Set<Long> relParentIds;

}
