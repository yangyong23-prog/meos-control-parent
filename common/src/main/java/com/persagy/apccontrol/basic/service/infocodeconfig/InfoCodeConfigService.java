package com.persagy.apccontrol.basic.service.infocodeconfig;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.*;
import com.persagy.apccontrol.basic.model.vo.infocode.*;
import com.persagy.framework.common.entity.R;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface InfoCodeConfigService {

    /**
     * @Author 薛涛
     * @Description 查询-模版类型列表
     * @Date 18:38 2024/6/28
     * @Param [equipmentInfoCodeConfigQueryVO]
     * @return java.util.List<com.persagy.apccontrol.basic.model.equipment.ListTemplateTypesVO>
     **/
    List<InfoCodeTemplateVO> listTemplateTypes(@NonNull @Valid InfoCodeConfigTemplateTypeQueryParam param);

    /**
     * 模版类型对象存在规则的IOT信息点
     *
     * @param param 查询参数
     * @return 信息点定义模型
     * @since 5.240602.4
     */
    R<Collection<InfoCodeDefineModel>> listObjectIotInfoCodesWithRuleByTemplateType(
            @NonNull @Valid InfoCodeConfigIotWithRuleByTemplateTypeQueryParam param);

    /**
     * @Author 薛涛
     * @Description 查询-信息点配置-按模版类型
     * @Date 18:38 2024/6/28
     * @Param [equipmentInfoCodeConfigQueryVO]
     * @return java.util.List<com.persagy.apccontrol.basic.model.equipment.EquipmentInfoCodeObjectGroupVO>
     **/
    R<Collection<InfoCodeObjectGroupVO>> listInfoCodeConfigsByTemplateType(@NonNull @Valid InfoCodeConfigByTemplateIdQueryParam param);

    /**
     * 查询原始信息点配置列表
     *
     * @param param 查询参数
     * @return 原始信息点配置列表
     * @since 7.260401.0
     */
    List<InfoCodeConfigOriginVO> listOriginInfoCodeConfigs(@NonNull @Valid InfoCodeConfigsOriginQueryParam param);

    /**
     * @Author 薛涛
     * @Description 查询-信息点配置-按对象
     * @Date 18:38 2024/6/28
     * @Param [equipmentInfoCodeConfigQueryVO]
     * @return java.util.List<com.persagy.apccontrol.basic.model.equipment.EquipmentInfoCodeObjectGroupVO>
     **/
    List<InfoCodeObjectGroupVO> listInfoCodeConfigsByObject(@NonNull @Valid InfoCodeConfigByObjectQueryParam param);

    List<InfoCodeObjectGroupVO> listInfoCodeConfigsByObjectFilters(InfoCodeConfigObjectByFiltersQueryParam param);

    List<SpaceByBuildingIdGroupFloorVO> listSpaceByBuildingIdGroupFloor(SpaceByBuildingIdGroupFloorQueryParam param);

    List<BuildingAndSpaceGroupFloorVO> listBuildingAndSpaceByProjectIdGroupFloor(BuildingAndSpaceByProjectIdQueryParam param);

    /**
     * 获取全量classCode 用于孪生数据订阅
     * 
     * @param 
     * @return {@link Set<String>}
     * @author yuecaipu
     * @date 2024/9/18 17:26
     * @since v5.240801.5
     */
    Set<String> getClassCodesForTwinSubscribe();

    /**
     * @author wxf
     * @description 获取模板类型下的树结构
     * @date 14:01 2025/9/2
     * @param param
     * @return java.util.Set<com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeTemplateTreeVO>
    **/
    Set<InfoCodeTemplateTreeVO> treeTemplateType(@NonNull @Valid InfoCodeConfigTemplateTypeQueryParam param);

}
