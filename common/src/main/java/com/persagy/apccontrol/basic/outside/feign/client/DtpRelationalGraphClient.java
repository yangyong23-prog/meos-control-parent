package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphJobParam;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.*;
import com.persagy.apccontrol.basic.outside.feign.decoder.SystemGraphFeignConfig;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * dtp关系图客户端
 *
 * @author MAF
 */
@FeignClient(name = "${persagy.serviceName.admGraph:dtp-relational-graph-server}", contextId = "dtp-relational-graph-server",
        configuration = SystemGraphFeignConfig.class, url = "${inner-url.bdtp.dtp-relational-graph.service:}")
@RefreshScope
public interface DtpRelationalGraphClient {


    /**
     * 关系图（系统图实例生成）
     *
     * @param graphGenerateLoadDto
     * @return
     */
    @PostMapping(value = "/graph_generate/load", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphGenerateInstanceDto> loadGraphGenerate(@RequestBody RequestSystemGraphDTO graphGenerateLoadDto);


    /**
     * 查询图例数据
     *
     * @param graphLegendSelectDto
     * @return
     */
    @PostMapping(value = "/graph_legend/select", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<List<GraphLegendDto>> graphLegendSelect(@RequestBody GraphLegendSelectDto graphLegendSelectDto);

    /**
     * 设备类型树
     *
     * @return
     */
    @GetMapping(value = "/graph_common/typeTree", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<List<GraphTypeTreeDto>> graphCommonTree();

    /**
     * 模板列表
     */
    @PostMapping(value = "/graph_template/select", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<List<GraphTemplateSelectDto>> graphTemplateSelect(@RequestBody GraphTemplateSelectFilterDto filterDto);

    /**
     * 小附件类型
     */
    @GetMapping(value = "/graph_common/attachTypeList", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<List<AttachTypeNodeDto>> attachTypeList();

    /**
     * 查询模板详情
     */
    @PostMapping(value = "/graph_template/loadTemplate", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphTemplateDto> loadTemplate(@RequestBody GraphTemplateLegendSelectDto graphTemplateLegendSelectDto);

    /**
     * 查询基准对象的模板
     */
    @PostMapping(value = "/graph_generate/getTemplate", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphTemplateLegendSelectDto> getTemplate(@RequestBody GraphTemplateFilterDto graphTemplateFilterDto);

    @PostMapping(value = "/graph_generate/getTemplates", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphTemplateLegendSelectListDTO> getTemplates(@RequestBody GraphTemplatesDTO graphTemplatesDTO);

    /**
     * 提交异步任务
     * @param graphGenerateLoadDto
     * @return
     */
    @PostMapping(value = "/graph_generate/asyncJobCommit", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphAsyncJobDTO> asyncJobCommit(@RequestBody RequestSystemGraphDTO graphGenerateLoadDto);

    /**
     * 查询异步任务状态
     * @param param
     * @return
     */
    @PostMapping(value = "/graph_generate/asyncJobStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphAsyncGenerateDTO> asyncJobStatus(@RequestBody GraphJobParam param);

    /**
     * 查询异步任务结果
     * @param param
     * @return
             */
    @PostMapping(value = "/graph_generate/asyncJobResult", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<GraphGenerateInstanceDto> asyncJobResult(@RequestBody GraphJobParam param);


}
