package com.persagy.apccontrol.common.resource;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 干预配置操作服务
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
public interface InterventionConfigOperationResourceService extends BaseService<InterventionConfig> {

    /**
     * 编辑结束时间
     *
     * @param id      ID
     * @param endTime 结束时间
     * @return 操作是否成功
     * @since 5.240702.0
     */
    boolean editEndTime(@NonNull Long id, @NonNull LocalDateTime endTime);

    /**
     * 删除干预配置
     *
     * @param id ID
     * @return 操作是否成功
     * @since 5.240702.0
     */
    boolean del(@NonNull Long id);

    List<Long> addInterventionConfig(InterventionAddEntity entity);

    void deleteInterventionConfigByInstanceIds(Collection<String> instanceIds);

}
