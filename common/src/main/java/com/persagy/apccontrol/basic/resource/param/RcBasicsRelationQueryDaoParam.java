package com.persagy.apccontrol.basic.resource.param;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

/**
 * 基础配置表关联查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Builder
public class RcBasicsRelationQueryDaoParam {

    /**
     * 配置类型
     */
    private BasicsConfigTypeEnum type;

    /**
     * 被关联ID
     */
    private String objFrom;

    /**
     * 被关联ID
     */
    private Collection<String> objFroms;

    /**
     * 关联对象ID
     */
    private String objTo;

    /**
     * 关联对象ID
     */
    private Collection<String> objTos;

}
