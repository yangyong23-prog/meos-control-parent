package com.persagy.apccontrol.basic.model.vo.infocode;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName 薛涛
 * @Description 信息点
 * @Author
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
public class InfoCodeObjectVO extends DtpRwdObjectDTO {
    /**
     * 模版类型ID
     */
    private Long templateTypeId;
    /**
     * 模版类型名称
     */
    private String templateTypeName;

    /**
     * 关联信息点配置(不做分组)
     */
    private List<InfoCodeConfigGroupInformationVO> infoCodeConfigGroupVOs;




    /**
     * @Author 薛涛
     * @Description 将分组的对象信息点打成平铺
     * @Date 13:43 2024/9/3
     * @Param [infoCodeObjectGroupVO]
     * @return com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectVO
     **/
    public static InfoCodeObjectVO of(InfoCodeObjectGroupVO infoCodeObjectGroupVO){
        InfoCodeObjectVO infoCodeObjectVO = new InfoCodeObjectVO();
        infoCodeObjectVO.setTemplateTypeId(infoCodeObjectGroupVO.getTemplateTypeId());
        infoCodeObjectVO.setTemplateTypeName(infoCodeObjectGroupVO.getTemplateTypeName());
        BeanUtils.copyProperties(infoCodeObjectGroupVO,infoCodeObjectVO);
        List<InfoCodeConfigGroupInformationVO> infoCodeConfigGroupVOs = new ArrayList<>();
        List<InfoCodeGroupVO> relInfoCodeGroupConfigs = infoCodeObjectGroupVO.getRelInfoCodeGroupConfigs();
        toLayFlatInfoCode(infoCodeConfigGroupVOs,relInfoCodeGroupConfigs);
        infoCodeObjectVO.setInfoCodeConfigGroupVOs(infoCodeConfigGroupVOs);
        return infoCodeObjectVO;

    }

    /**
     * @Author 薛涛
     * @Description 信息点平铺
     * @Date 13:54 2024/9/3
     * @Param [infoCodeConfigGroupVOs, relInfoCodeGroupConfigs]
     * @return void
     **/
    public static void toLayFlatInfoCode(List<InfoCodeConfigGroupInformationVO> infoCodeConfigGroupVOs,List<InfoCodeGroupVO> relInfoCodeGroupConfigs){
        for (InfoCodeGroupVO relInfoCodeGroupConfig : relInfoCodeGroupConfigs) {
            String groupCode = relInfoCodeGroupConfig.getGroupCode();
            String groupName = relInfoCodeGroupConfig.getGroupName();
            List<InfoCodeGroupVO> groupChildren = relInfoCodeGroupConfig.getGroupChildren();
            List<InfoCodeConfigVO> relInfoCodeConfigs = relInfoCodeGroupConfig.getRelInfoCodeConfigs();
            if(CollectionUtils.isNotEmpty(relInfoCodeConfigs)){
                for (InfoCodeConfigVO relInfoCodeConfig : relInfoCodeConfigs) {
                    InfoCodeConfigGroupInformationVO infoCodeConfigGroupInformationVO = new InfoCodeConfigGroupInformationVO();
                    infoCodeConfigGroupInformationVO.setGroupCode(groupCode);
                    infoCodeConfigGroupInformationVO.setGroupName(groupName);
                    BeanUtils.copyProperties(relInfoCodeConfig,infoCodeConfigGroupInformationVO);
                    infoCodeConfigGroupVOs.add(infoCodeConfigGroupInformationVO);
                }
            }
            if(CollectionUtils.isNotEmpty(groupChildren)){
                toLayFlatInfoCode(infoCodeConfigGroupVOs,groupChildren);
            }
        }
    }
    
}
