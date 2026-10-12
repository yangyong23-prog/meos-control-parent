package com.persagy.apccontrol.basic.resource.infocodeconfig;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.IotMonitorConfig;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeTemplateTypeRemoveEvent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 信息点配置资源
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeConfigResource extends BaseService<InfoCodeConfig> {

    /**
     * 查询信息点配置
     *
     * @param param 查询参数
     * @return 信息点配置
     * @since 5.240602.0
     */
    List<InfoCodeConfig> list(@NonNull @Valid InfoCodeConfigQueryDaoParam param);

    /**
     * 根据分组编码查询无效的信息点配置
     *
     * @param groupCodes 分组编码
     * @return 无效的信息点配置
     * @since 5.240702.0
     */
    List<InfoCodeConfig> listInvalidByGroupCode(@NonNull Collection<String> groupCodes);

    /**
     * 删除配置
     *
     * @param relTemplateTypeIds 关联模模版类型ID
     * @return 操作是否成功
     * @since 5.240602.0
     */
    boolean del(@NonNull Collection<Long> relTemplateTypeIds);

    /**
     * 强制新增配置
     *
     * @param configs 配置
     * @return 新增后实体
     * @since 5.240602.0
     */
    List<InfoCodeConfig> addForce(@NonNull List<InfoCodeConfig> configs);

    /**
     * 编辑关联配置ID
     *
     * @param configId     配置ID
     * @param relConfigIds 关联配置ID
     * @since 5.240602.0
     */
    void editRelConfigId(@NonNull Long configId, @NonNull Set<Long> relConfigIds);

    /**
     * 根据ID更新信息点名称
     *
     * @param ids          ID
     * @param infoCodeName 信息点名称
     * @since 7.250901.0
     */
    void updateInfoCodeNameByIds(@NonNull Set<Long> ids, @NonNull String infoCodeName);

    /**
     * 根据ID更新信息点名称
     *
     * @param ids               ID
     * @param infoCodeName      信息点名称
     * @param infoCodeAliasName 信息点别名
     * @since 7.250902.0
     */
    void updateInfoCodeNameByIds(
            @NonNull Set<Long> ids,
            @Nullable String infoCodeName,
            @Nullable String infoCodeAliasName);

    @SuppressWarnings("unchecked")
    @TransactionalEventListener(value = InfoCodeTemplateTypeRemoveEvent.class)
    default void eventListener(InfoCodeTemplateTypeRemoveEvent event) {
        Collection<Long> toBeDelTemplateTypeIds = (Collection<Long>) event.getSource();
        // 删除关联的信息点配置
        del(toBeDelTemplateTypeIds);
    }

    List<IotMonitorConfig> listIotMonitorConfig();
}
