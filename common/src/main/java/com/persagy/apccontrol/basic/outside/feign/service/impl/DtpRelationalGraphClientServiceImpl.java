package com.persagy.apccontrol.basic.outside.feign.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphJobParam;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.*;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRelationalGraphClient;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRelationalGraphClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系图引擎客户端服务实现
 *
 * @author MuAnFu
 * @date 2025-09-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpRelationalGraphClientServiceImpl implements DtpRelationalGraphClientService {

    private final DtpRelationalGraphClient dtpRelationalGraphClient;

    /**
     * 关系图（系统图实例生成）
     *
     * @param graphGenerateLoadDto
     * @return
     */
    @Override
    public GraphGenerateInstanceDto loadGraphGenerate(RequestSystemGraphDTO graphGenerateLoadDto) {
        return query(false, LocaleMessageUtil.getMessage("key_0587f47732404807ae7881da64931aae"), graphGenerateLoadDto,
                () -> dtpRelationalGraphClient.loadGraphGenerate(graphGenerateLoadDto), GraphGenerateInstanceDto::new);
    }

    /**
     * 模板查询
     *
     * @param filterDto
     */
    @Override
    public List<GraphTemplateSelectDto> graphTemplateSelect(GraphTemplateSelectFilterDto filterDto) {
        return query(false, LocaleMessageUtil.getMessage("key_d5109a7bc1bc4853864f3d401e003dc5"), filterDto,
                () -> dtpRelationalGraphClient.graphTemplateSelect(filterDto), ArrayList::new);
    }

    /**
     * 获取模板列表
     *
     * @param graphTemplatesDTO 图形模板dto
     * @return {@code GraphTemplateLegendSelectListDTO }
     * @since 7.250802.0
     */
    @Override
    public GraphTemplateLegendSelectListDTO getTemplates(GraphTemplatesDTO graphTemplatesDTO) {
        return query(false, LocaleMessageUtil.getMessage("key_58faec012cfe42a687cfe7b276079255"), graphTemplatesDTO,
                () -> dtpRelationalGraphClient.getTemplates(graphTemplatesDTO), GraphTemplateLegendSelectListDTO::new);
    }

    /**
     * 提交异步任务
     *
     * @param graphGenerateLoadDto
     * @return
     */
    @Override
    public GraphAsyncJobDTO asyncJobCommit(RequestSystemGraphDTO graphGenerateLoadDto) {
        return query(true, LocaleMessageUtil.getMessage("key_831d8491a4d14fa29a6af7a58b5c63ac"), graphGenerateLoadDto,
                () -> dtpRelationalGraphClient.asyncJobCommit(graphGenerateLoadDto), GraphAsyncJobDTO::new);
    }

    /**
     * 查询异步任务状态
     *
     * @param param
     * @return
     */
    @Override
    public GraphAsyncGenerateDTO asyncJobStatus(GraphJobParam param) {
        return query(false, LocaleMessageUtil.getMessage("key_7dabf4c25135422fba2844bce3bea7d9"), param,
                () -> dtpRelationalGraphClient.asyncJobStatus(param), GraphAsyncGenerateDTO::new);
    }

    /**
     * 查询异步任务结果
     *
     * @param param
     * @return
     */
    @Override
    public GraphGenerateInstanceDto asyncJobResult(GraphJobParam param) {
        return query(false, LocaleMessageUtil.getMessage("key_b00e7d58e7fe4addb724b1f5423e13e6"), param,
                () -> dtpRelationalGraphClient.asyncJobResult(param), GraphGenerateInstanceDto::new);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图引擎服务";
    }
}
