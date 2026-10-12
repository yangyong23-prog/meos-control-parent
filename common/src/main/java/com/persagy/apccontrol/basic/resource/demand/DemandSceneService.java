package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * 需求场景服务
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandSceneService extends BaseService<DemandScene> {

    /**
     * 查询需求场景
     *
     * @param param 查询参数
     * @return 需求场景
     * @since 5.241102.0
     */
    Optional<DemandScene> getOpt(@NonNull DemandSceneQueryDaoParam param);

    /**
     * 查询需求场景
     *
     * @param param 查询参数
     * @return 需求场景
     * @since 5.241102.0
     */
    List<DemandScene> list(@NonNull DemandSceneQueryDaoParam param);

}
