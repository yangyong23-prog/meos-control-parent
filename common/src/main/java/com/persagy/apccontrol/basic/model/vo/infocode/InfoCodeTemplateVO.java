package com.persagy.apccontrol.basic.model.vo.infocode;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @ClassName 薛涛
 * @Description 模版类型列表实体
 * @Author
 * @Date 2024/6/28 18:17
 * @Version 1.0
 */
@Data
public class InfoCodeTemplateVO {

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 设备类编码名称
     */
    private String classCodeName;

    /**
     * 信息点过滤条件
     */
    private Map<String, Object> infoCodeFilter;

    /**
     * 信息点定义
     */
    private List<InfoCodeDefineModel> infoCodeDefines;

    /**
     * 模版类型ID
     */
    private Long templateTypeId;

    /**
     * 模版类型Key
     */
    private String templateTypeKey;

    /**
     * 模版类型名称
     */
    private String templateTypeName;

    /**
     * 总对象数量
     */
    private Integer totalObjectCount;

    /**
     * 序列
     */
    private Integer sequence;

    /**
     * 模版下对象
     */
    private List<DtpRwdObjectDTO> objects;

    /**
     * 模版类型列表实体
     *
     * @param templateTypeModel 模版类型模型
     * @return 模版类型列表实体
     * @since 7.250901.0
     */
    public static InfoCodeTemplateVO of(@NonNull InfoCodeTemplateTypeModel templateTypeModel) {
        InfoCodeTemplateVO infoCodeTemplateVO = new InfoCodeTemplateVO();
        infoCodeTemplateVO.setClassCode(templateTypeModel.getClassCode());
        infoCodeTemplateVO.setClassCodeName(templateTypeModel.getClassCodeName());
        infoCodeTemplateVO.setInfoCodeFilter(templateTypeModel.getInfoCodeFilterData());
        infoCodeTemplateVO.setInfoCodeDefines(templateTypeModel.getInfoCodeDefines());
        infoCodeTemplateVO.setTemplateTypeId(templateTypeModel.getTemplateTypeId());
        infoCodeTemplateVO.setTemplateTypeKey(templateTypeModel.getTemplateTypeKey());
        infoCodeTemplateVO.setTemplateTypeName(templateTypeModel.getTemplateTypeName());
        if (CollectionUtils.isNotEmpty(templateTypeModel.getObjects())) {
            infoCodeTemplateVO.setObjects(templateTypeModel.getObjects());
            infoCodeTemplateVO.setTotalObjectCount(templateTypeModel.getObjects().size());
        }
        infoCodeTemplateVO.setSequence(templateTypeModel.getSequence());
        return infoCodeTemplateVO;
    }
}
