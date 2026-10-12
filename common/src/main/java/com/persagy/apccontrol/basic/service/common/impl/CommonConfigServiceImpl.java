package com.persagy.apccontrol.basic.service.common.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.mapper.common.CommonConfigMapper;
import com.persagy.apccontrol.basic.model.param.common.CommonConfigSaveParam;
import com.persagy.apccontrol.basic.model.po.common.CommonConfig;
import com.persagy.apccontrol.basic.model.vo.security.SecurityScreenConfigVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.enums.SystemGraphPipeLineConfigEnum;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置表 服务实现类
 *
 * @author yang
 * @since 2024-06-26
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommonConfigServiceImpl extends ServiceImpl<CommonConfigMapper, CommonConfig> implements CommonConfigService {


    /**
     * 根据配置获取
     *
     * @param tag 配置标签
     * @return 响应回data数据直接转为json
     */
    @Override
    public JSONArray getCommonConfigByTag(String tag) {
        CommonConfig commonConfig = getOne(new LambdaQueryWrapper<CommonConfig>().eq(CommonConfig::getTag, tag));
        if (commonConfig != null) {
            String config = commonConfig.getConfig();
            if (StringUtils.isNotBlank(config)) {
                return JSON.parseArray(config);
            }
        }
        return new JSONArray();
    }

    @Override
    public boolean saveConfig(CommonConfigSaveParam param) {
        try {
            String tag = param.getTag();
            CommonConfig commonConfig = getOne(new LambdaQueryWrapper<CommonConfig>().eq(CommonConfig::getTag, tag));
            if (commonConfig == null) {
                if (StringUtils.isBlank(param.getRemark())) {
                    throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_f95b02c766184ea08b9fd06915410063"), LocaleMessageUtil.getMessage("key_d442bb9592ac49ceafef21c239e52c87"));
                }
                commonConfig = new CommonConfig();
                commonConfig.setId(param.getId());
                commonConfig.setTag(tag);
                commonConfig.setConfig(param.getConfig());
                commonConfig.setRemark(param.getRemark());
                return save(commonConfig);
            }
            //更新配置
            String config = param.getConfig();
            commonConfig.setConfig(config);
            commonConfig.setUpdateTime(null);
            return updateById(commonConfig);
        } catch (Exception e) {
            log.error("保存公共配置失败错误信息{}", e);
        }
        return false;
    }

    private Boolean updateByTag(String tag, String config) {
        return lambdaUpdate().eq(CommonConfig::getTag, tag).set(CommonConfig::getConfig, config).update();
    }

    @Override
    public Boolean refreshCacheData() {
//        List<CommonConfig> commonConfigs = list();
//        if (CollectionUtils.isNotEmpty(commonConfigs)) {
//            commonConfigs.forEach(x -> redisCacheTemplate.object().set(getTagCacheKey(x.getTag()), x.getConfig(), 1, TimeUnit.DAYS));
//        }
        return true;
    }


    @Override
    public List<MenuConfigVo> getMenuConfig() {
        return getCommonConfigByTag("MENU_CONFIG").toJavaList(MenuConfigVo.class);
    }

    @Override
    public List<GraphConfigVo> getGraphConfig() {
        return getCommonConfigByTag("GRAPH_CONFIG").toJavaList(GraphConfigVo.class);
    }

    @Override
    public List<DiagramIconChangeConfigVo> getDiagramIconChangeConfig() {
        return getCommonConfigByTag("DIAGRAM_ICON_CHANGE_CONFIG").toJavaList(DiagramIconChangeConfigVo.class);
    }


    @Override
    public List<InteractiveConfigVo> getInteractiveConfig() {
        return getCommonConfigByTag("INTERACTIVE_CONFIG").toJavaList(InteractiveConfigVo.class);
    }


    @Override
    public List<GraphInformationVo> getGraphInformation() {
        return getCommonConfigByTag("GRAPH_INFORMATION").toJavaList(GraphInformationVo.class);
    }


    @Override
    public Map<String, List<GraphInformationVo>> getGraphInformationGroupByClassCode() {
        List<GraphInformationVo> graphInformation = getGraphInformation();
        return graphInformation.stream().collect(Collectors.groupingBy(GraphInformationVo::getClassCode));
    }

    /*------ 2024-03-07 24Q1T3-SP1 ------*/

    @Override
    public List<SystemGraphFilterStatusVO> getSystemGraphFilterStatus() {
        return getCommonConfigByTag("SYSTEM_GRAPH_FILTER_STATUS").toJavaList(SystemGraphFilterStatusVO.class);
    }

    @Override
    public List<ModelDiagramPrimitivesVo> getModelDiagramPrimitives() {
        return getCommonConfigByTag("MODEL_DIAGRAM_PRIMITIVES").toJavaList(ModelDiagramPrimitivesVo.class);
    }

    @Override
    public List<SystemRunParamVo> getSystemRunParam() {
        return getCommonConfigByTag("SYSTEM_RUN_PARAM").toJavaList(SystemRunParamVo.class);
    }

    @Override
    public List<ExceptionEventConfigVo> getExceptionEventConfig() {
        return getCommonConfigByTag("EXCEPTION_EVENT_CONFIG").toJavaList(ExceptionEventConfigVo.class);
    }

    @Override
    public List<OrderFailAlarmVo> getOrderFailAlarm() {
        return getCommonConfigByTag("ORDER_FAIL_ALARM").toJavaList(OrderFailAlarmVo.class);
    }

    @Override
    public List<SystemIconFilterConfigVo> getSystemIconFilterConfig() {
        return getCommonConfigByTag("SYSTEM_DIAGRAM_ICON_FILTER_CONFIG").toJavaList(SystemIconFilterConfigVo.class);
    }

    @Override
    public List<PipelineStyleVo> pipeLineStyleConfig() {
        return getCommonConfigByTag("PIPELINE_STYLE_CONFIG").toJavaList(PipelineStyleVo.class);
    }

    @Override
    public List<AreaStyleConfigVo> areaStyleConfigVos() {
        return getCommonConfigByTag("AREA_STYLE_CONFIG").toJavaList(AreaStyleConfigVo.class);
    }

    @Override
    public Map<Integer, MultiplePipeTextVo> multipleConfig() {
        List<MultiplePipeTextVo> multiplePipeTextVos = getCommonConfigByTag("MULTIPLE_PIPE_TEXT").toJavaList(MultiplePipeTextVo.class);
        return multiplePipeTextVos.stream().collect(Collectors.toMap(MultiplePipeTextVo::getMultiple, it -> it));
    }

    @Override
    public List<PipelineRelationConfigVo> pipelineRelationConfigVos() {
        return getCommonConfigByTag("PIPELINE_RELATION_COLOR_CONFIG").toJavaList(PipelineRelationConfigVo.class);
    }

    /**
     * 管线样式配置
     *
     * @return 管线样式配置
     */
    @Override
    public List<SecurityScreenConfigVO> getScreenConfigList() {
        return getCommonConfigByTag("SCREEN_CONFIG").toJavaList(SecurityScreenConfigVO.class);
    }

    /**
     * 获取运行分析配置列表
     *
     * @return {@link List }<{@link RunAnalysisConfigVO }>
     */
    @Override
    public Map<String, List<RunAnalysisConfigVO>> getRunAnalysisConfig() {
        JSONArray runAnalysisConfig = getCommonConfigByTag("RUN_ANALYSIS_CONFIG");
        HashMap<String, List<RunAnalysisConfigVO>> result = Maps.newHashMap();
        if (CollectionUtils.isEmpty(runAnalysisConfig)) {
            return result;
        }
        JSONObject jsonObject = runAnalysisConfig.getJSONObject(0);
        jsonObject.forEach((key, value) -> {
            List<RunAnalysisConfigVO> runAnalysisConfigVOList = JSONArray.parseArray(value.toString()).toJavaList(RunAnalysisConfigVO.class);
            result.put(key, runAnalysisConfigVOList);
        });
        return result;
    }

    @Override
    public String uploadSystemGraphPipelineStyle(Integer sheetNum, MultipartFile file) {
        if (Objects.nonNull(sheetNum)) {
            SystemGraphPipeLineConfigEnum systemGraphPipeLineConfigEnum = SystemGraphPipeLineConfigEnum.analyze(sheetNum);
            if (Objects.isNull(systemGraphPipeLineConfigEnum)) {
                return "sheet页数输入错误";
            }
            try {
                List<PipelineStyleConfig> pipelineStyleVos = handlePipelineStyleExcel(sheetNum, file, systemGraphPipeLineConfigEnum.getClazz(), systemGraphPipeLineConfigEnum.getHeaderNum());
                this.updateByTag(systemGraphPipeLineConfigEnum.getTag(), JSON.toJSONString(pipelineStyleVos));
                return "success";
            } catch (Exception e) {
                throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "import fail", e.getMessage());
            }

        }
        Map<Integer, List<PipelineStyleConfig>> pipelineStyleConfigMap = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            try {
                SystemGraphPipeLineConfigEnum systemGraphPipeLineConfigEnum = SystemGraphPipeLineConfigEnum.analyze(i);
                List<PipelineStyleConfig> configData = handlePipelineStyleExcel(i, file, systemGraphPipeLineConfigEnum.getClazz(), systemGraphPipeLineConfigEnum.getHeaderNum());
                pipelineStyleConfigMap.put(i, configData);
            } catch (Exception e) {
                errorMessage.append("第").append(i + 1).append("个sheet处理失败").append(e.getMessage()).append("\n");
            }
        }
        if (MapUtils.isNotEmpty(pipelineStyleConfigMap)) {
            pipelineStyleConfigMap.forEach((key, pipelineStyleConfigs) -> {
                SystemGraphPipeLineConfigEnum systemGraphPipeLineConfigEnum = SystemGraphPipeLineConfigEnum.analyze(key);
                this.updateByTag(systemGraphPipeLineConfigEnum.getTag(), JSON.toJSONString(pipelineStyleConfigs));
            });
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            return errorMessage.toString();
        }
        return "success";
    }

    private <T extends PipelineStyleConfig> List<T> handlePipelineStyleExcel(Integer sheetNum, MultipartFile file, Class<? extends PipelineStyleConfig> clazz, Integer headerNum) {
        List<T> list = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), clazz, new ReadListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    System.out.println(data + "rowNum:" + context.readSheetHolder().getRowIndex());
                    list.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 读取完成后可以做一些清理工作
                }
            }).sheet(sheetNum).headRowNumber(headerNum).doRead();
        } catch (Exception e) {
            throw new RuntimeException("读取Excel文件第" + (sheetNum + 1) + "个sheet时发生错误: " + e.getMessage(), e);
        }
        return list;
    }

}
