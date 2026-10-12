package com.persagy.apccontrol.controller.logtrace;


import com.persagy.apccontrol.common.logtrace.dto.LogInfoAddDto;
import com.persagy.apccontrol.common.logtrace.dto.LogQuerySearchDto;
import com.persagy.apccontrol.common.logtrace.dto.LogQueryTemplateAndParamDto;
import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import com.persagy.apccontrol.common.logtrace.service.LogApplicationService;
import com.persagy.apccontrol.common.logtrace.service.LogNodeExtendService;
import com.persagy.apccontrol.common.logtrace.service.LogQueryDataService;
import com.persagy.apccontrol.common.logtrace.service.LogQueryTemplateGroupService;
import com.persagy.apccontrol.common.logtrace.service.LogQueryTemplateService;
import com.persagy.apccontrol.common.logtrace.vo.LogInfoExceptionVo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeExtendVo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeInfoVo;
import com.persagy.apccontrol.common.logtrace.vo.LogQueryTemplateRecordVo;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Tag(name = "业务生成日志")
@Validated
@RestController
@RequestMapping("/log-trace")
@Slf4j
public class LogTraceController {

    @Autowired
    private LogApplicationService logApplicationService;

    @Autowired
    private LogQueryTemplateGroupService groupQueryTemplateGroupService;

    @Autowired
    private LogQueryTemplateService logQueryTemplateService;

    @Autowired
    private LogNodeExtendService logNodeExtendService;
    @Autowired
    private LogQueryDataService logQueryDataService;
    private final static String apiVersion = "日志记录1.0";


    /**
     * 存储业务生成日志，并返回业务对下级对象在业务日志中不存在的数据节点信息
     *
     * @param addDto
     * @return
     */
    @ApiOperation(value = "日志存储接口", tags = apiVersion)
    @PostMapping("/save")
    public R<List<LogInfoExceptionVo>> saveLogInfo(@RequestBody LogInfoAddDto addDto) {
        //  String dd ="\"businessKey\": \"空调温度\",\"businessValue\": \"5度\",\"businessTime\": \"10:05\"";
        List<LogInfoExceptionVo> result = logApplicationService.saveLogInfo(addDto);
        return R.success(result);
    }

    @ApiOperation(value = "查询所有的顶节点", tags = apiVersion)
    @GetMapping("/top/all")
    public R<List<LogNodeInfoVo>> getAllTopNode() {
        return R.success(logApplicationService.queryAllTopNode());
    }

    @ApiOperation(value = "根据节点查询子树", tags = apiVersion)
    @GetMapping("/children/tree")
    public R<LogNodePo> getChildrenTreeByNodeId(@RequestParam(name = "nodeId") Long nodeId) {
        return R.success(logApplicationService.getTreeByTopNodeId(nodeId));
    }

    @ApiOperation(value = "查询日志模板的所有分组", tags = apiVersion)
    @GetMapping("/query/template/group")
    public R<Collection<LogGroupTemplateEntity>> getQueryModelGroupByApplication(@RequestParam(name = "application",required = false) String application) {
        Collection<LogGroupTemplateEntity> result = groupQueryTemplateGroupService.getAllQueryGroupByTemp(application);
        return R.success(result);
    }

    @ApiOperation(value = "查询日志模板", tags = apiVersion)
    @GetMapping("/query/template")
    public R<Collection<LogGroupTemplateEntity>> getQueryModelByGroupId(@RequestParam(name = "groupId") Long groupId, @RequestParam(name = "application") String application) {
        Collection<LogGroupTemplateEntity> result = logQueryTemplateService.getQueryTemplateByGroupIdAndApp(groupId, application);
        return R.success(result);
    }

    @ApiOperation(value = "根据模板查询日志数据", tags = apiVersion)
    @PostMapping("/query/template/data")
    public R<LogQueryTemplateRecordVo> getQueryTemplateDataById(@RequestBody LogQueryTemplateAndParamDto dto) {
        LogQueryTemplateRecordVo result =  logQueryDataService.getQueryTemplateDataById(dto);
        return R.success(result);
    }

    @ApiOperation(value = "根据模板查询搜索条件数据", tags = apiVersion)
    @PostMapping("/query/template/search/data")
    public R<List<LogNodeExtendVo>> getQuerySearchByTemplateId(@RequestBody LogQuerySearchDto searchDto )throws Exception {
       return R.success(logNodeExtendService.getQuerySearchByTemplateId(searchDto));
    }




}

