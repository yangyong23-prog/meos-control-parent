package com.persagy.apccontrol.basic.resource.infocodeconfig;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 信息点模版类型配置资源
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeTemplateTypeResource extends BaseService<InfoCodeTemplateType> {

    /**
     * 根据ID查询模版类型
     *
     * @param id ID
     * @return 模版类型
     * @since 5.240602.0
     */
    Optional<InfoCodeTemplateType> get(@NonNull Long id);

    /**
     * 查询模版类型
     *
     * @param param 查询参数
     * @return 模版类型
     * @since 5.2420602.0
     */
    List<InfoCodeTemplateType> list(@NonNull @Valid InfoCodeTemplateTypeQueryDaoParam param);

    /**
     * 删除配置
     *
     * @param param        删除参数
     * @param publishEvent 是否发布事件
     * @return 操作是否成功
     * @since 7.250401.0
     */
    boolean del(@NonNull InfoCodeTemplateTypeRemoveDaoParam param, boolean publishEvent);

    /**
     * 新增配置
     *
     * @param config       配置
     * @param publishEvent 是否发布事件
     * @return 新增后实体
     * @since 7.250401.0
     */
    InfoCodeTemplateType add(@NonNull InfoCodeTemplateType config, boolean publishEvent);

}
