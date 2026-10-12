package com.persagy.apccontrol.basic.resource.infocodeconfig;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeGroupQueryDaoParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 设备信息的分组配置资源
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeGroupResource extends BaseService<InfoCodeGroup> {

    /**
     * 查询分组配置
     *
     * @param param 查询参数
     * @return 分组配置
     * @since 5.240602.0
     */
    List<InfoCodeGroup> list(@NonNull @Valid InfoCodeGroupQueryDaoParam param);

    /**
     * 删除分组配置
     *
     * @param groupCodes 分组编码
     * @return 操作是否成功
     * @since 5.240602.0
     */
    boolean del(@NonNull Collection<String> groupCodes);

    /**
     * 强制新增分组配置
     *
     * @param configs 分组配置
     * @return 新增后实体
     * @since 5.240602.0
     */
    List<InfoCodeGroup> addForce(@NonNull List<InfoCodeGroup> configs);

}
