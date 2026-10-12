package com.persagy.apccontrol.basic.resource.demand;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 需求配置服务
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandConfigService extends BaseService<DemandConfig> {

    /**
     * 存在自定义配置实例的数量
     *
     * @param date       查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     * @param templateId 模版ID
     * @return 存在自定义配置实例的数量
     * @since 5.241201.0
     */
    int countExistsCustomizedInstance(@Nullable LocalDate date, @NonNull Long templateId);

    /**
     * 根据实例查询配置
     *
     * @param param 查询参数
     * @return 配置
     * @since 5.241102.0
     */
    List<DemandConfig> listByInstances(@NonNull @Valid DemandConfigByInstanceRepositoryBatchQueryParam param);

    /**
     * 编辑配置
     *
     * @param configIds 配置ID
     * @param configData 配置
     * @since 7.250202.0
     */
    void editConfig(@NonNull Collection<Long> configIds, @NonNull JsonNode configData);

}
