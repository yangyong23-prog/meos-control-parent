package com.persagy.apccontrol.common.saas.bizcheck.notify;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.service.bizcheck.BizCheckService;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.notify.BizCheckNotifier;
import com.persagy.apccontrol.common.saas.bizcheck.notify.render.NotifyTextRenderer;
import com.persagy.apccontrol.common.saas.bizcheck.notify.render.NotifyTextRendererRegistry;
import com.persagy.apccontrol.common.saas.bizcheck.notify.sender.DingTalkNotifySender;
import com.persagy.apccontrol.common.saas.bizcheck.notify.sender.NotifyReceiverResolver;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 钉钉通知编排实现
 * <p>
 * 完整编排流程：
 * ①检查 enabled 配置 → ②查询检查结果 → ③筛选异常项 → ④分派渲染 → ⑤组装消息 → ⑥解析收件人 → ⑦发送 → ⑧记录日志
 *
 * @since 7.260601.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DingTalkBizCheckNotifier implements BizCheckNotifier {

    private static final DateTimeFormatter END_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    /**
     * 按策略枚举声明顺序排列，确保消息中各行顺序一致
     */
    private static final List<BizCheckStrategyTypeEnum> STRATEGY_ORDER = Arrays.asList(
            BizCheckStrategyTypeEnum.WORK_CALENDAR,
            BizCheckStrategyTypeEnum.HVAC_OPERATION_STANDARD,
            BizCheckStrategyTypeEnum.UNIT_PRICE_CONFIG,
            BizCheckStrategyTypeEnum.SYSTEM_RELATIONSHIP_DIAGRAM
    );

    private final BizCheckNotifyProperties properties;
    private final BizCheckService bizCheckService;
    private final MidPermissionClientService midPermissionClientService;
    private final NotifyTextRendererRegistry rendererRegistry;
    private final NotifyReceiverResolver receiverResolver;
    private final DingTalkNotifySender sender;

    @Override
    public void notifyIfAbnormal(String bizCode) {
        // ① 检查 enabled 配置
        if (!properties.isEnabled()) {
            log.info("BizCheck通知：配置未启用，跳过发送. bizCode={}", bizCode);
            return;
        }

        // ② 查询检查结果
        RunningMonitorTypeEnum bizCodeEnum = RunningMonitorTypeEnum.analyze(bizCode);
        com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckQueryParam queryParam =
                new com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckQueryParam();
        queryParam.setBizCode(bizCodeEnum);
        List<BizCheckResultVO> results = bizCheckService.getCheckResults(queryParam);

        // ③ 筛选异常项：外层 checkStatus=REJECTED AND 内层 status=EXCEPTION
        List<BizCheckResultVO> abnormalResults = results.stream()
                .filter(r -> r.getCheckStatus() == BizCheckStatusEnum.REJECTED)
                .filter(this::hasInnerException)
                .collect(Collectors.toList());

        if (abnormalResults.isEmpty()) {
            return;
        }

        // ④ 分派到各 Renderer 渲染，按策略枚举声明顺序
        List<String> allLines = new ArrayList<>();
        Map<BizCheckStrategyTypeEnum, BizCheckResultVO> byType = abnormalResults.stream()
                .collect(Collectors.toMap(BizCheckResultVO::getCheckItemCode, r -> r, (a, b) -> a));

        for (BizCheckStrategyTypeEnum type : STRATEGY_ORDER) {
            BizCheckResultVO vo = byType.get(type);
            if (vo == null) {
                continue;
            }
            NotifyTextRenderer renderer = rendererRegistry.dispatch(type);
            if (renderer != null) {
                List<String> lines = renderer.render(vo);
                allLines.addAll(lines);
            }
        }

        if (allLines.isEmpty()) {
            return;
        }

        // ⑤ 异常总条数 + 组装消息模板
        int totalCount = allLines.size();
        String checkEndTime = abnormalResults.stream()
                .map(BizCheckResultVO::getCheckEndTime)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(t -> t.format(END_TIME_FORMATTER))
                .orElse("未知");

        String projectName = resolveProjectName();
        String bizCodeName = bizCodeEnum != null ? bizCodeEnum.getReasonPhrase() : bizCode;

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(projectName != null ? projectName : "").append("可用性检查异常通知】\n");
        sb.append("最近一次检查结束时间：").append(checkEndTime).append("\n");
        sb.append("业务类型：").append("供冷业务").append("\n");
        sb.append("异常总数：").append(totalCount).append("条").append("\n");
        sb.append("异常详情：\n");
        allLines.forEach(line -> sb.append(line).append("\n"));

        String messageContent = sb.toString().trim();
        //当messageContent长度大于指定长度就直接截断后面增加...
        if (messageContent.length() > properties.getTruncateLength()) {
            messageContent = messageContent.substring(0, properties.getTruncateLength()) + "...";
        }

        // ⑥ 解析收件人
        String projectId = ContextUtil.getProjectId();
        List<String> phones = receiverResolver.resolvePhoneNumbers(projectId);
        if (phones.isEmpty()) {
            log.warn("BizCheck通知：无收件人，跳过发送. bizCode={}", bizCode);
            return;
        }

        // ⑦ 发送
        sender.sendAll(phones, messageContent);

        // ⑧ 记录日志
        log.info("BizCheck通知发送完成. bizCode={}, 收件人数={}, 异常总条数={}", bizCode, phones.size(), totalCount);
    }

    /**
     * 解析当前项目名称
     */
    private String resolveProjectName() {
        try {
            ProjectDTO project = midPermissionClientService.getProject(false, false, ContextUtil.getProjectId());
            return project != null ? project.getProjectLocalName() : "";
        } catch (Exception e) {
            log.warn("BizCheck通知：查询项目名称失败，使用空字符串", e);
            return "";
        }
    }

    /**
     * 判断检查结果内部是否存在 EXCEPTION 状态
     */
    private boolean hasInnerException(BizCheckResultVO vo) {
        if (vo.getCheckResult() == null) {
            return false;
        }
        // 检查 JSON 中是否存在 "status":"EXCEPTION" 字段
        String json = vo.getCheckResult().toString();
        return json.contains("\"EXCEPTION\"");
    }
}
