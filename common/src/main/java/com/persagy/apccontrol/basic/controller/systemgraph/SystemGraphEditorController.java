package com.persagy.apccontrol.basic.controller.systemgraph;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.systemgraph.*;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphAsyncJob;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.resource.SystemGraphAsyncJobService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/systemGraphEditor")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(value = "系统图编辑", tags = VersionConstant.V5_240602_0)
public class SystemGraphEditorController {

    private final SystemGraphEditorService systemGraphEditorService;
    private final SystemGraphAsyncJobService systemGraphAsyncJobService;
    private final SystemGraphService systemGraphService;

    @Operation(summary = "获取系统模板列表")
    @GetMapping(value = "/listSystemTemplate")
    public R<List<TemplateTreeVO>> listSystemTemplate() {
        return R.success(systemGraphEditorService.listSystemTemplate());
    }

    @Operation(summary = "初始化图纸")
    @PostMapping(value = "/initSystemGraph")
    public R<List<SystemGraphDuplicateVO>> initSystemGraph(@Validated @RequestBody RequestSystemGraphDTO dto) {
        SystemGraphAsyncJob job = systemGraphAsyncJobService.checkJob(dto.getGraphTypeCode(), dto.getReferenceObjectId());
        return R.success(systemGraphEditorService.initSystemGraph(dto, Objects.nonNull(job) ? job.getJobId() : null));
    }

    @Operation(summary = "获取副本列表")
    @PostMapping(value = "/listSystemGraphDuplicate")
    public R<List<SystemGraphDuplicateVO>> listSystemGraphDuplicate(@Validated @RequestBody ListSystemGraphDuplicateDTO dto) {
        SystemGraphAsyncJob job = systemGraphAsyncJobService.checkJob(dto.getGraphTypeCode(), dto.getSystemObjectId());
        if (Objects.nonNull(job)) {
            RequestSystemGraphDTO graphDTO = JSONObject.parseObject(job.getJobParam(), RequestSystemGraphDTO.class);
            systemGraphEditorService.initSystemGraph(graphDTO, job.getJobId());
        }
        return R.success(systemGraphEditorService.listSystemGraphDuplicate(dto));
    }

    @Operation(summary = "创建副本")
    @PostMapping(value = "/createSystemGraphDuplicate/{duplicateId}")
    public R<SystemGraphDuplicateVO> createSystemGraphDuplicate(@PathVariable("duplicateId") Long id) {
        return R.success(systemGraphEditorService.createSystemGraphDuplicate(id));
    }

    @Operation(summary = "重新计算管线")
    @PostMapping(value = "/recalculatePipeline/{duplicateId}")
    public R<SystemGraphDuplicateVO> recalculatePipeline(@PathVariable(value = "duplicateId") Long duplicateId, @Validated @RequestBody RequestSystemGraphDTO dto) {
        return R.success(systemGraphEditorService.recalculatePipeline(duplicateId, dto));
    }

    @Operation(summary = "获取图纸数据（包含系统图数据和细节数据）")
    @GetMapping(value = "/getSystemGraphData/{duplicateId}")
    public R<SystemGraphInstanceVo> getSystemGraphData(@PathVariable(value = "duplicateId") Long duplicateId) {
        return R.success(systemGraphEditorService.getSystemGraphData(duplicateId, true));
    }


    @Operation(summary = "更新图纸")
    @PostMapping(value = "/editSystemGraph/{duplicateId}")
    public R<Boolean> editSystemGraph(@PathVariable("duplicateId") Long duplicateId, @RequestBody List<UpdateGraphDTO> dto) {
        return R.success(systemGraphEditorService.editSystemGraph(duplicateId, dto));
    }

    @Operation(summary = "发布图纸")
    @PostMapping(value = "/publishSystemGraph/{duplicateId}")
    public R<Boolean> publishSystemGraph(@PathVariable("duplicateId") Long duplicateId) {
        return R.success(systemGraphEditorService.publishSystemGraph(duplicateId));
    }

    @Operation(summary = "删除副本")
    @PostMapping(value = "/deleteSystemGraphDuplicate/{duplicateId}")
    public R<Boolean> deleteSystemGraphDuplicate(@PathVariable("duplicateId") Long duplicateId) {
        return R.success(systemGraphEditorService.deleteSystemGraphCopy(duplicateId));
    }

    @Operation(summary = "下架图纸")
    @PostMapping(value = "/offlineSystemGraph/{duplicateId}")
    public R<Boolean> offlineSystemGraph(@PathVariable("duplicateId") Long duplicateId) {
        return R.success(systemGraphEditorService.offlineSystemGraph(duplicateId));
    }

    @Operation(summary = "保存或者更新系统图细节调整")
    @PostMapping(value = "/saveAndUpdateSystemGraphDetail")
    public R<SystemGraphDetailVO> saveAndUpdateSystemGraphDetail(@RequestBody SystemGraphDetailVO systemGraphDetailVO) {
        return R.success(systemGraphEditorService.saveAndUpdateSystemGraphDetail(systemGraphDetailVO));
    }

    @Operation(summary = "保存或者更新系统图细节调整批量")
    @PostMapping(value = "/saveAndUpdateSystemGraphDetailBatch")
    public R<List<SystemGraphDetailVO>> saveAndUpdateSystemGraphDetailBatch(@RequestBody List<SystemGraphDetailVO> systemGraphDetailVOs) {
        return R.success(systemGraphEditorService.saveAndUpdateSystemGraphDetailBatch(systemGraphDetailVOs));
    }


    @Operation(summary = "删除系统图细节调整")
    @PostMapping(value = "/deleteSystemGraphDetail")
    public R<Boolean> deleteSystemGraphDetail(@RequestBody SystemGraphDetailVO systemGraphDetailVO) {
        return R.success(systemGraphEditorService.deleteSystemGraphDetail(systemGraphDetailVO));
    }

    @Operation(summary = " 根据获取系统图细节调整id")
    @PostMapping(value = "/getSystemGraphDetailById")
    public R<List<SystemGraphDetailVO>> getSystemGraphDetailById(@RequestBody SystemGraphDetailVO systemGraphDetailVO) {
        return R.success(systemGraphEditorService.getSystemGraphDetailById(systemGraphDetailVO));
    }

    @Operation(summary = "图中设备已绑定的信息点全集")
    @PostMapping(value = "/listInfoCode")
    public R<List<ListInfoCodeVo>> listInfoCode(@RequestBody ListInfoCodeDTO listInfoCodeDTO) {
        listInfoCodeDTO.setProjectId(ContextUtil.getProjectId());
        return R.success(systemGraphEditorService.listInfoCode(listInfoCodeDTO));
    }

    @Operation(summary = "更新副本名称")
    @PostMapping(value = "/editDuplicateName")
    public R<Boolean> editDuplicateName(@Validated @RequestBody UpdateDuplicateNameVO updateDuplicateNameVO) {
        return R.success(systemGraphEditorService.editDuplicateName(updateDuplicateNameVO));
    }

    @Operation(summary = "更新模版名称")
    @PostMapping(value = "/updateTemplateName")
    public R<String> updateTemplateName(@Validated @RequestBody UpdateTemplateNameDTO updateTemplateNameDTO) {
        return R.success(systemGraphEditorService.updateTemplateName(updateTemplateNameDTO));
    }

    @Operation(summary = "复制细节数据")
    @PostMapping(value = "/copyDetailData")
    public R<Boolean> copyDetailData(@Validated @RequestBody CopyDetailDataParam param) {
        return R.success(systemGraphEditorService.copyDetailData(param));
    }

}
