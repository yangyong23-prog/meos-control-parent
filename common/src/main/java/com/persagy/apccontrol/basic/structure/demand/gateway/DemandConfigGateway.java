package com.persagy.apccontrol.basic.structure.demand.gateway;

import com.persagy.apccontrol.basic.model.base.domain.Gateway;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandReferenceParentSpaceInFloorQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.*;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandCustomizedInstanceDTO;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 需求配置网关
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandConfigGateway<T extends BaseDemandConfig>
        extends Gateway<DemandConfigIdentifier, DemandConfigAggregate<T>> {

    /**
     * 查询是否存在自定义配置实例
     *
     * @param param 查询参数
     * @return 是否存在自定义配置实例
     * @since 5.241201.0
     */
    boolean isExistsCustomizedInstance(@NonNull @Valid BaseDemandTemplateParam param);

    /**
     * 检查分组名称是否存在
     *
     * @param param 检查参数
     * @return 分组名称是否存在
     * @since 7.250202.0
     */
    boolean checkConfigGroupNameExists(@NonNull @Valid DemandConfigGroupNameCheckParam param);

    /**
     * 根据日期查询模版
     *
     * @param param 查询参数
     * @return 模版
     * @see BaseDemandTemplateParam#queryAndGetTemplate()
     * @since 5.241201.0
     */
    Optional<DemandTemplate> getByTemplate(@NonNull @Valid BaseDemandTemplateParam param);

    /**
     * 根据实例查询配置
     *
     * @param param 查询参数
     * @return 配置
     * @since 5.240902.0
     */
    Optional<DemandConfigAggregate<T>> getConfigByInstance(@NonNull @Valid DemandConfigByInstanceQueryParam param);

    /**
     * 查询存在需求配置的空间ID
     *
     * @param param          查询参数
     * @param filterSpaceIds 过滤空间ID, 为空时查询所有空间
     * @return 存在需求配置的空间ID
     * @since 5.241201.0
     */
    Set<String> listHasConfigSpaceIds(
            @NonNull @Valid BaseDemandTemplateParam param,
            @Nullable Set<String> filterSpaceIds);

    /**
     * 查询原始配置
     *
     * @param param 查询参数
     * @return 原始配置
     * @since 7.250801.0
     */
    List<DemandConfigAggregate<T>> listOriginConfigs(@NonNull @Valid DemandConfigByInstanceRepositoryBatchQueryParam param);

    /**
     * 根据实例查询可持久化的稳定配置。
     *
     * <p>执行模板、场景、配置、继承和需求类型稳定生命周期逻辑，但不补充营业日历空调季、
     * DTX/RWD运行态或本地化名称等查询期动态内容，供需求配置结果预计算使用。</p>
     *
     * @param param 查询参数
     * @return 稳定配置聚合根
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigAggregate<T>> listStableConfigsByInstances(
            @NonNull @Valid DemandConfigByInstanceBatchQueryParam param);

    /**
     * 根据实例查询配置
     *
     * @param param 查询参数
     * @return 配置
     * @since 5.240902.0
     */
    @NonNull List<DemandConfigAggregate<T>> listConfigsByInstances(
            @NonNull @Valid DemandConfigByInstanceBatchQueryParam param);

    /**
     * 根据实例查询预计算需求配置结果。
     *
     * <p>该方法封装模板解析、结果查询、实例标签补齐和查询期动态配置补全，
     * 供需求配置之外的业务模块通过稳定边界安全调用。</p>
     *
     * @param param 查询参数
     * @return 需求配置结果
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResultVO<T>> listConfigResultsByInstances(
            @NonNull @Valid DemandConfigResultBatchQueryParam param);

    /**
     * 查询存在自定义配置实例
     *
     * @param param 查询参数
     * @return 存在自定义配置实例
     * @since 5.241102.0
     */
    List<DemandCustomizedInstanceDTO> listCustomizedInstances(@NonNull @Valid DemandCustomizedInstanceQueryParam param);

    /**
     * 查询引用上级的空间ID
     *
     * @param param 查询参数
     * @return 引用上级的空间ID
     * @since 5.241102.0
     */
    List<DtpRwdSpaceDTO> listReferenceParentSpaces(@NonNull DemandReferenceParentSpaceInFloorQueryParam param);

    /**
     * 新增或修改配置
     *
     * @param param 新增或修改参数
     * @return 分组ID
     * @since 5.240902.0
     */
    Long addOrUpdateConfig(@NonNull @Valid DemandConfigSaveOrUpdateParam<T> param);

    /**
     * 复制配置
     *
     * @param param 复制参数, 左侧为源, 右侧为目标
     * @since 7.250801.0
     */
    void copyConfig(@NonNull @Valid Pair<DemandConfigCopyPairParam, DemandConfigCopyPairParam> param);

    /**
     * 取消引用配置
     *
     * @param param 取消引用参数
     * @since 5.240902.0
     */
    void cancelReference(@NonNull @Valid DemandConfigReferenceCancelParam param);

    /**
     * 清除配置
     *
     * @param param 清除参数
     * @since 5.240902.0
     */
    void clearConfig(@NonNull @Valid DemandConfigClearParam param);

}
