//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.persagy.apccontrol.basic.outside.feign.client;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.*;
import com.persagy.framework.common.entity.R;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author yuyao
 * 物理世界 字典定义
 */
@FeignClient(name = "dtp-knowledge-server", url = "${inner-url.bdtp.dtp-knowledge.service:}")
@RefreshScope
public interface DtpKnowledgeServerClient {

    /**
     * 查询码表数据
     *
     * <p><a href="https://dtp-knowledge-server.apifox.cn/262811706e0">点击查看接口文档</a></p>
     *
     * @param typeCode 知识库编码
     * @return 码表数据
     * @since 7.250902.0
     */
    @GetMapping("/api/knowledge/base")
    CustomerResponse<List<DtpKnowledgeTypeResponse>> listTypeDefines(@RequestParam String typeCode);

    /**
     * 查询信息点编码定义
     *
     * @param typeCode 设备类编码
     * @param infoCode 信息点编码
     * @return 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    @GetMapping("/api/type/info/listInfo")
    CustomerResponse<List<DtpKnowledgeObjectInfoCodeResponse>> listInfoCode(
            @RequestParam String typeCode, @RequestParam String infoCode);

    /**
     * 查询信息点编码定义
     *
     * @param classCodes 类型编码
     * @return 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    @PostMapping("/api/type/info/listInfoByTypes")
    CustomerResponse<List<Map<String, List<DtpKnowledgeObjectInfoCodeResponse>>>> listInfoCode(
            @RequestBody Collection<String> classCodes);

    /**
     * 查询设备类编码定义
     *
     * @param classCodes 类型编码
     * @return 设备类编码定义
     * @since 5.240502.0
     */
    @PostMapping("/api/type/listTypeByCode")
    CustomerResponse<List<DtpKnowledgeObjectClassCodeResponse>> listClassCode(
            @RequestBody Collection<String> classCodes);

    /**
     * 查询对象类型定义
     *
     * @param codes 编码
     */
    @Deprecated
    @PostMapping({"/api/type/listTypeByCode"})
    R<List<JSONObject>> listTypeByCode(@RequestBody List<String> codes);

    @GetMapping({"/api/type/listTypeByType"})
    CustomerResponse<List<DtpKnowledgeObjectInfoCodeResponse>> listObjectClassByType(@RequestParam DtpRwdObjectTypeEnum objType);

    /**
     * 根据标签查询设备类编码
     *
     * @param param 查询参数
     * @return 设备类编码
     * @since 7.250201.0
     */
    @PostMapping({"/api/type/listTypeByTag"})
    CustomerResponse<List<DtpKnowledgeClassCodeByTagResponse>> listClassCodesByTag(
            @RequestBody DtpKnowledgeClassCodeByTagQueryParam param);

    /**
     * 查询设备类编码标签
     *
     * @param param 查询参数
     * @return 设备类编码定义
     * @since 7.250902.0
     */
    @PostMapping("/api/proj/tagObjList")
    CustomerResponse<List<DtpKnowledgeClassCodeTagResponse>> listClassCodeTags(
            @RequestBody DtpKnowledgeClassCodeTagQueryParam param);

    /**
     * 根据标签查询信息点
     *
     * @param param 类型编码
     * @return 信息点编码定义
     * @since 7.251202.0
     */
    @PostMapping("/api/type/info/listInfoByTag")
    CustomerResponse<Map<String, List<DtpKnowledgeInfoCodeTagResponse>>> listInfoByTag(
            @RequestBody DtpKnowledgeInfoCodeByTagQueryParam param);

    /**
     * 字典响应数据合并
     *
     * @param classCodes 对象类编码
     * @return 合并后的数据结构
     */
    default Map<String, List<DtpKnowledgeObjectInfoCodeResponse>> listInfoByClassCodes(List<String> classCodes) {
        CustomerResponse<List<Map<String, List<DtpKnowledgeObjectInfoCodeResponse>>>> listCustomerResponse = listInfoCode(classCodes);
        if (listCustomerResponse != null
                && "success".equals(listCustomerResponse.getResult())
                && CollectionUtil.isNotEmpty(listCustomerResponse.getContent())) {
            List<Map<String, List<DtpKnowledgeObjectInfoCodeResponse>>> content = listCustomerResponse.getContent();


            return content.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        }
        return new HashMap<>();
    }

}
