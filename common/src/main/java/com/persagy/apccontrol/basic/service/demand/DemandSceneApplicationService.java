package com.persagy.apccontrol.basic.service.demand;

import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneCopyParam;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneSaveParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandAllSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;

import java.util.List;

/**
 * 需求场景查询服务
 *
 * @author wxf
 * @since v7.250801.0
 */
public interface DemandSceneApplicationService {
    /**
     * 查询场景数据
     *
     * @param templateId
     * @return
     */
    List<DemandSceneVO> listSceneConfig(Long templateId);

    /**
     * 查询类型下所有场景数据
     *
     * @param param
     * @return
     */
    List<DemandAllSceneVO> listAllScene(DemandTemplateBatchQueryParam param);

    /**
     * 批量保存场景数据，每次全量替换
     *
     * @param saveParams
     * @return
     */
    boolean editSceneConfigs(DemandSceneSaveParam saveParams);

    /**
     * @param fromSceneParam
     * @return boolean
     * @author wxf
     * @description 场景配置复制
     * @date 14:31 2025/6/20
     **/
    boolean copySceneConfig(DemandSceneCopyParam fromSceneParam);

    /**
     * 拷贝其余模板下的场景及其配置数据写入到场景和配置表中
     *
     * @param templateId
     * @param fromTemplateId
     * @return
     */
    boolean copySceneConfigByTemplate(Long templateId, Long fromTemplateId);

    /**
     * 根据模板ID，获取与此模板相同类型下，存在场景配置数据的其余模板列表
     *
     * @param templateId
     * @return
     */
    List<DemandTemplateVO> listExistsSceneConfigTemplate(Long templateId);


}
