package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphJobParam;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.*;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;

import java.util.List;

public interface DtpRelationalGraphClientService extends BaseFeignClientService {


    /**
     * 关系图（系统图实例生成）
     *
     * @param graphGenerateLoadDto
     * @return
     */
    GraphGenerateInstanceDto loadGraphGenerate(RequestSystemGraphDTO graphGenerateLoadDto);

    /**
     * 模板列表
     */
    List<GraphTemplateSelectDto> graphTemplateSelect(GraphTemplateSelectFilterDto filterDto);


    GraphTemplateLegendSelectListDTO getTemplates(GraphTemplatesDTO graphTemplatesDTO);

    /**
     * 提交异步任务
     * @param graphGenerateLoadDto
     * @return
     */
    GraphAsyncJobDTO asyncJobCommit(RequestSystemGraphDTO graphGenerateLoadDto);

    /**
     * 查询异步任务状态
     * @param param
     * @return
     */
    GraphAsyncGenerateDTO asyncJobStatus(GraphJobParam param);

    /**
     * 查询异步任务结果
     * @param param
     * @return
     */
    GraphGenerateInstanceDto asyncJobResult(GraphJobParam param);

}
