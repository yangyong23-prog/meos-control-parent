package com.persagy.apccontrol.basic.service.common;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.param.common.CommonConfigSaveParam;
import com.persagy.apccontrol.basic.model.po.common.CommonConfig;
import com.persagy.apccontrol.basic.model.vo.security.SecurityScreenConfigVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 配置表 服务类
 *
 * @author yang
 * @since 2024-06-26
 */
public interface CommonConfigService extends IService<CommonConfig> {

    /**
     * 根据配置获取
     *
     * @param tag 配置标签
     * @return 响应回data数据直接转为json
     */
    JSONArray getCommonConfigByTag(String tag);

    /**
     * 保存配置
     *
     * @param param 标签+配置
     * @return 保存结果
     */
    boolean saveConfig(CommonConfigSaveParam param);

    /**
     * 刷新缓存
     *
     * @return 缓存是否刷新成功
     */
    Boolean refreshCacheData();

    /**
     * 总览配置
     *
     * @return 总览配置
     */
    List<MenuConfigVo> getMenuConfig();

    /**
     * 图配置
     *
     * @return 图配置
     */
    List<GraphConfigVo> getGraphConfig();

    /**
     * 关系图图标更换配置
     *
     * @return 关系图图标更换配置
     */
    List<DiagramIconChangeConfigVo> getDiagramIconChangeConfig();

    /**
     * 交互配置
     *
     * @return 交互配置
     */
    List<InteractiveConfigVo> getInteractiveConfig();

    /**
     * 关系图信息
     *
     * @return 关系图信息配置
     */
    List<GraphInformationVo> getGraphInformation();

    /**
     * 关系图信息按设备类编码分组
     *
     * @return 关系图信息按设备类编码分组配置
     */
    Map<String, List<GraphInformationVo>> getGraphInformationGroupByClassCode();

    List<SystemGraphFilterStatusVO> getSystemGraphFilterStatus();

    /**
     * 模型图图元
     *
     * @return 模型图图元
     */
    List<ModelDiagramPrimitivesVo> getModelDiagramPrimitives();

    /**
     * 系统运行参数
     *
     * @return 系统运行参数
     */
    List<SystemRunParamVo> getSystemRunParam();

    /**
     * 异常事件配置
     *
     * @return 异常事件配置
     */
    List<ExceptionEventConfigVo> getExceptionEventConfig();

    /**
     * 控反不一致配置
     *
     * @return 控反不一致配置
     */
    List<OrderFailAlarmVo> getOrderFailAlarm();

    /**
     * 图标滤镜参数
     *
     * @return 图标滤镜参数
     */
    List<SystemIconFilterConfigVo> getSystemIconFilterConfig();

    /**
     * 管线样式配置
     *
     * @return 管线样式配置
     */
    List<PipelineStyleVo> pipeLineStyleConfig();

    /**
     * 区域样式配置
     *
     * @return 区域样式配置
     */
    List<AreaStyleConfigVo> areaStyleConfigVos();

    /**
     * 查询管道和文字多倍尺寸
     *
     * @return 查询管道和文字多倍尺寸
     */
    Map<Integer, MultiplePipeTextVo> multipleConfig();

    List<PipelineRelationConfigVo> pipelineRelationConfigVos();

    /**
     * 管线样式配置
     *
     * @return 管线样式配置
     */
    List<SecurityScreenConfigVO> getScreenConfigList();

    Map<String, List<RunAnalysisConfigVO>> getRunAnalysisConfig();

    String uploadSystemGraphPipelineStyle(Integer sheetNum, MultipartFile file);
}
