package com.persagy.apccontrol.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphAsyncGenerateDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphAsyncJob;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphTemplateStatus;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumJobStatus;
import com.persagy.apccontrol.basic.model.message.mq.DtpRelationalGraphMessage;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.outside.feign.client.MidPermissionClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.GroupQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.GroupResponse;
import com.persagy.apccontrol.basic.resource.SystemGraphAsyncJobService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphTemplateStatusService;
import com.persagy.apccontrol.basic.utils.Obj2JSONObject;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.rabbitmq.handler.AbstractRabbitHandlerTemplate;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CommonConst.APPLICATION_NAME + ".dtp-relational-graph-server-client"),
        exchange = @Exchange(value = "dtp-relational-graph-server", type = ExchangeTypes.FANOUT),
        key = "dtp-relational-graph-server"))
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DtpRelationalGraphMessageHandler extends AbstractRabbitHandlerTemplate implements LogComponent {

    private final SystemGraphTemplateStatusService systemGraphTemplateStatusService;
    private final MidPermissionClient midPermissionClient;
    private final CommonConfigService commonConfigService;
    private final SystemGraphService systemGraphService;
    private final SystemGraphEditorService systemGraphEditorService;
    private final SystemGraphAsyncJobService systemGraphAsyncJobService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    protected void handler(String message, Channel channel, Message vo) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        logInstance().info("收到MQ消息", Pair.of("message", message));
        // 先只处理2次，用while可能会出现死循环漏洞，当前情况转2次就够了
        Object object = objectMapper.readValue(message, Object.class);
        if (object instanceof String) {
            object = objectMapper.readValue((String) object, Object.class);
        }
        if (!(object instanceof Map<?, ?>)) {
            logInstance().error("message格式错误，最终无法转换为map", Pair.of("message", message));
            return;
        }
        Map<?, ?> messageObjectMap = (Map<?, ?>) object;
        Object messageType = messageObjectMap.get("messageType");
        if (Objects.isNull(messageType) || !(messageType instanceof String)) {
            return;
        }
        String messageTypeStr = messageType.toString();
        switch (messageTypeStr) {
            case "asyncGenerate":
                GraphAsyncGenerateDTO dto = Obj2JSONObject.obj2Class(messageObjectMap, GraphAsyncGenerateDTO.class);
                if (StringUtils.isBlank(dto.getGroupCode()) || StringUtils.isBlank(dto.getProjectId())) {
                    logInstance().error("groupCode或projectId为空", Pair.of("message", message));
                    return;
                }
                ContextUtil.setTenant(dto.getGroupCode());
                ContextUtil.setGroupCode(dto.getGroupCode());
                ContextUtil.setProjectId(dto.getProjectId());
                handleAsyncGenerate(dto);
                break;
            case "templateChange":
                // 模板变更功能已弃用，改为每次获取模板的时候动态处理
//                handelTemplateChange(messageObjectMap);
                break;
            default:
                logInstance().error("messageType功能未实现", Pair.of("messageType", messageTypeStr));
        }

    }

    /**
     * 处理异步生成
     *
     * @param dto 异步生成消息
     */
    private void handleAsyncGenerate(@NonNull GraphAsyncGenerateDTO dto) {
        String jobId = dto.getJobId();
        SystemGraphAsyncJob job = systemGraphAsyncJobService.getByJobId(jobId);
        if (Objects.isNull(job)) {
            return;
        }
        if (dto.isResultSuccess()) {
            systemGraphAsyncJobService.updateByJobId(jobId, dto.getResultMessage(), EnumJobStatus.SUCCESS);
            RequestSystemGraphDTO graphGenerateLoadDto = JSONObject.parseObject(job.getJobParam(), RequestSystemGraphDTO.class);
            systemGraphEditorService.initSystemGraph(graphGenerateLoadDto, jobId);
        } else {
            systemGraphAsyncJobService.updateByJobId(dto.getJobId(), dto.getResultMessage(), EnumJobStatus.FAIL);
        }


    }

    /**
     * 处理模板变更
     *
     * @param messageObjectMap 模板变更消息
     */
    private void handelTemplateChange(Map<?, ?> messageObjectMap) {
        DtpRelationalGraphMessage messageObject = JSON.parseObject(JSON.toJSONString(messageObjectMap), DtpRelationalGraphMessage.class);
        List<DtpRelationalGraphMessage.ClassCodeMessage> classCodes = messageObject.getClassCodes();
        if (CollectionUtils.isEmpty(classCodes)) {
            return;
        }
        GroupQueryParam param = new GroupQueryParam();
        param.setStatus(1);
        CustomerResponse<List<GroupResponse>> listCustomerResponse = midPermissionClient.listGroup(param);
        List<GroupResponse> listGroupCode = CustomerResponse.handleResult(listCustomerResponse);
        for (DtpRelationalGraphMessage.ClassCodeMessage data : classCodes) {
            List<DtpRelationalGraphMessage.GraphTypeMessage> graphTypes = data.getGraphTypes();
            if (CollectionUtils.isEmpty(graphTypes)) {
                continue;
            }
            for (DtpRelationalGraphMessage.GraphTypeMessage graphType : graphTypes) {
                List<DtpRelationalGraphMessage.TemplateMessage> templates = graphType.getTemplates();
                if (CollectionUtils.isEmpty(templates)) {
                    continue;
                }
                for (DtpRelationalGraphMessage.TemplateMessage template : templates) {
                    String change = template.getChange();
                    if (!"update".equals(change)) {
                        continue;
                    }
                    // 获取配置
                    List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
                    // 获取系统图的图名
                    String graphTypeCode = graphType.getGraphTypeCode();
                    String graphName = systemGraphService.getGraphName(data.getClassCode(), graphTypeCode, graphConfig, null);
                    // 判断图是否可编辑
                    GraphConfigVo graphConfigVo = graphConfig.stream().filter(e -> e.getMapTitle().equals(graphName)).findFirst().orElse(null);
                    boolean isNotEdit = graphConfigVo != null && graphConfigVo.getNotEdit() == 1;
                    String oriId = template.getOriId();
                    // 不可编辑的图走自动发布更新逻辑，可编辑的图走更新状态逻辑
                    if (isNotEdit) {
                        for (GroupResponse group : listGroupCode) {
                            handleProjectTemplateInit(group.getGroupCode(), oriId, template.getId(), graphTypeCode);
                        }
                    } else {
                        for (GroupResponse group : listGroupCode) {
                            String groupCode = group.getGroupCode();
                            handleProjectTemplateStatus(groupCode, oriId);
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理项目模板状态
     *
     * @param groupCode 组码
     * @param oriId     模板oriId
     */
    public void handleProjectTemplateStatus(String groupCode, String oriId) {
        //先设置集团编码和项目
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        try {
            List<SystemGraphTemplateStatus> systemGraphTemplateStatuses = systemGraphTemplateStatusService.listTemplateStatusByTemplateId(oriId, null);
            if (CollectionUtils.isEmpty(systemGraphTemplateStatuses)) {
                return;
            }
            //批量更新
//            systemGraphTemplateStatusService.updateTemplateStatusByTemplateId(oriId, true, null);
        } catch (Exception e) {
            log.error("租户{}更新失败", groupCode, e);
        }
    }

    /**
     * 处理项目模板状态
     *
     * @param groupCode         组码
     * @param templateOriId     模板oriId
     * @param referenceObjectId 对象Id
     * @param graphTypeCode     系统图编码
     */
    public void handleProjectTemplateInit(String groupCode, String templateOriId,
                                          String referenceObjectId, String graphTypeCode) {
        //先设置集团编码和项目
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        try {
            RequestSystemGraphDTO systemGraphDTO = new RequestSystemGraphDTO();
            systemGraphDTO.setReferenceObjectId(referenceObjectId);
//            systemGraphDTO.setTemplateOriId(templateOriId);
            systemGraphDTO.setGraphTypeCode(graphTypeCode);
            systemGraphEditorService.initSystemGraph(systemGraphDTO, null);
        } catch (Exception e) {
            log.error("租户{}更新失败", groupCode, e);
        }
    }

    @Override
    public String logTag() {
        return "关系图MQ消息处理类";
    }
}
