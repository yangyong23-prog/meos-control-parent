package com.persagy.apccontrol.basic.controller.systemgraph;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.SystemGraphType;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.*;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.ConsumptionSystemGraphService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author houyuanzhi
 * @date 2023/6/25 14:59
 */
@RestController
@RequestMapping("/system/graph")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(value = "系统图", tags = VersionConstant.V5_240602_0)
public class SystemGraphController implements LogComponent {
    private final ConsumptionSystemGraphService graphGenerateInstance;
    private final CommonConfigService commonConfigService;
    private final SystemGraphService systemGraphService;


    @Operation(summary = "图标滤镜参数")
    @GetMapping("/icon/filter/config")
    public List<SystemIconFilterConfigVo> iconFilterConfigVoList() {
        return commonConfigService.getSystemIconFilterConfig();
    }

    @Operation(summary = "管线样式配置")
    @GetMapping("/pipeline/style/config")
    public List<PipelineStyleVo> pipelineStyleConfigVos() {

        return commonConfigService.pipeLineStyleConfig();
    }

    @Operation(summary = "区域样式配置")
    @GetMapping("/area/style/config")
    public List<AreaStyleConfigVo> areaStyleConfigVos() {
        return commonConfigService.areaStyleConfigVos();
    }


    @Operation(summary = "查询管道和文字多倍尺寸")
    @GetMapping("/multiple/config")
    public Map<Integer, MultiplePipeTextVo> multipleConfig() {
        return commonConfigService.multipleConfig();
    }


    @Operation(summary = "查询系统实例所有关系图列表")
    @GetMapping("/relation/graph/{systemObjectId}")
    public List<SystemGraph> getRelationGraphId(@ApiParam(value = "systemObjectId", required = true) @PathVariable("systemObjectId") String systemObjectId,
                                                @ApiParam(value = "passBy") @RequestParam(defaultValue = "true", required = false) boolean passBy
    ) {
        return graphGenerateInstance.getRelationGraphId(systemObjectId, passBy);
    }

    @Operation(summary = "系统图实例查询(id)")
    @GetMapping("/instance/{id}")
    public SystemGraphInstanceVo selectSystemGraphInstanceById(@ApiParam(value = "id", required = true) @PathVariable("id") Long id) {
        return graphGenerateInstance.querySystemGraphById(id);
    }


    @Operation(summary = "查询联动模型数据")
    @GetMapping("/model/{id}")
    public SystemGraphModelVo modelInstanceList(@ApiParam(value = "id", required = true) @PathVariable("id") Long id) {
        return graphGenerateInstance.modelInstanceList(id);
    }

    @Operation(summary = "查询系统实例是否有系统图")
    @GetMapping("/has/{systemId}")
    public SystemGraphType getSystemType(@ApiParam(value = "systemId", required = true) @PathVariable("systemId") String systemId) {
        return graphGenerateInstance.getGraphType(systemId);
    }

    @Operation(summary = "菜单配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/menu/config")
    public R<List<TreeNodeVO>> getMenuConfig(@ApiParam(value = "projectId") @RequestHeader(value = "projectId") String projectId,
                                             @ApiParam(value = "menuCode") @RequestParam(defaultValue = "all", required = false) String menuCode,
                                             @ApiParam(value = "productModule") @RequestParam(defaultValue = "状态感知", required = false) String productModule) {
        try {
            String groupCode = ContextUtil.getGroupCode();
            List<TreeNodeVO> menuConfig = graphGenerateInstance.getMenuConfig(projectId, menuCode, productModule, groupCode);
            return R.success(menuConfig);
        } catch (Exception e) {
            logProjectInstance().error("首页数据异常", e);
            return R.success(new ArrayList<>());
        }
    }

    @Operation(summary = "图配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/graph/config")
    public R<List<GraphConfigVo>> getGraphConfig() {
        return R.success(commonConfigService.getGraphConfig());
    }

    @Operation(summary = "关系图信息", tags = VersionConstant.V5_240602_0)
    @GetMapping("/graph/info/config")
    public R<List<GraphInformationVo>> getGraphInformation() {
        return R.success(commonConfigService.getGraphInformation());
    }

    @Operation(summary = "关系图图标更换配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/diagram/icon/config")
    public R<List<DiagramIconChangeConfigVo>> getDiagramIconChangeConfig() {
        return R.success(commonConfigService.getDiagramIconChangeConfig());
    }

    @Operation(summary = "交互配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/interactive/config")
    public R<List<InteractiveConfigVo>> getInteractiveConfig() {
        return R.success(commonConfigService.getInteractiveConfig());
    }

    @Operation(summary = "系统图实例查询(systemObjectId,classCode)", tags = VersionConstant.V5_240602_0)
    @GetMapping("/instance/select")
    public R<SystemGraphVO> selectSystemGraphInstance(@ApiParam(value = "systemObjectId") @RequestParam String systemObjectId,
                                                      @ApiParam(value = "classCode") @RequestParam String classCode,
                                                      @ApiParam(value = "graphTypeCode") @RequestParam(defaultValue = "供电关系图", required = false) String graphTypeCode) {
        SystemGraph result = systemGraphService.getSystemGraph(systemObjectId, classCode, graphTypeCode);
        if (result == null || result.getSystemGraphDuplicateId() == null) {
            return R.success(null);
        }
        return R.success(SystemGraphVO.toVO(result));
    }

    @Operation(summary = "系统图实例查询(systemObjectId,classCode)", tags = VersionConstant.V5_240602_0)
    @GetMapping("/instance/list/select")
    public R<List<SystemGraphVO>> selectSystemGraphInstanceList(@ApiParam(value = "systemObjectId") @RequestParam String systemObjectId,
                                                                @ApiParam(value = "classCode") @RequestParam String classCode) {
        List<SystemGraph> result = systemGraphService.listSystemGraph(systemObjectId, classCode);
        if (CollectionUtils.isEmpty(result)) {
            return R.success(Collections.emptyList());
        }
        return R.success(SystemGraphVO.toVOList(result));
    }

    @Operation(summary = "模型图图元", tags = VersionConstant.V5_240602_0)
    @GetMapping("/primitives/config")
    public R<List<ModelDiagramPrimitivesVo>> getModelDiagramPrimitives() {
        return R.success(commonConfigService.getModelDiagramPrimitives());
    }

    @Operation(summary = "系统运行参数", tags = VersionConstant.V5_240602_0)
    @GetMapping("/system/run/config")
    public R<List<SystemRunParamVo>> getSystemRunParam() {
        return R.success(commonConfigService.getSystemRunParam());
    }

    @Operation(summary = "异常事件配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/exception/event/config")
    public R<List<ExceptionEventConfigVo>> getExceptionEventConfig() {
        return R.success(commonConfigService.getExceptionEventConfig());
    }

    @Operation(summary = "控反不一致配置", tags = VersionConstant.V5_240602_0)
    @GetMapping("/orderFailAlarm/config")
    public R<List<OrderFailAlarmVo>> getOrderFailAlarm() {
        return R.success(commonConfigService.getOrderFailAlarm());
    }


    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图controller";
    }
}
