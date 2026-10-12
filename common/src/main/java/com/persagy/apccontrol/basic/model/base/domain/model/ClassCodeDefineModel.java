package com.persagy.apccontrol.basic.model.base.domain.model;


import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 设备类定义模型
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@Accessors(chain = true)
public class ClassCodeDefineModel implements ValueObject {

    /**
     * 内容生产工具的版本
     */
    @NonNull
    private String version;

    /**
     * 编码, 全局唯一
     */
    @NonNull
    private String code;

    /**
     * 名称
     */
    @NonNull
    private String name;

    /**
     * 对象类型
     */
    @NonNull
    private String objType;

    /**
     * 对象英文名称
     */
    private String englishName;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 信息点编码定义
     */
    private List<InfoCodeDefineModel> infoCodeDefines;

    /**
     * 创建信息点编码定义模型
     *
     * @param response 信息点编码定义响应
     * @return 信息点编码定义模型
     * @since 7.260201.0
     */
    public static ClassCodeDefineModel from(DtpKnowledgeObjectClassCodeResponse response) {
        return SpringUtil.getBean(DtpKnowledgeConverter.class).toClassCodeDefineModel(response);
    }
}
