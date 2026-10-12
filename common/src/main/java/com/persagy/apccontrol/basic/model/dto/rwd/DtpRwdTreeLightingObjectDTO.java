package com.persagy.apccontrol.basic.model.dto.rwd;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 物理世界对象DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "物理世界对象DTO")
public class DtpRwdTreeLightingObjectDTO extends DtpRwdObjectDTO {


    @Schema(description = "建筑ID")
    private String buildingId;

    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "灯具类型列表")
    private Set<BaseEnumVO> lampFuncTypeList;

    @Schema(description = "功能类型列表")
    private Set<BaseEnumVO> funcTypeList;

    @Schema(description = "3D模型消费字段信息")
    private Map<String, Object> modelMap;

    @Schema(description = "对象类型")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 子节点列表
     */
    private List<DtpRwdTreeLightingObjectDTO> childrenList;


    /**
     * 用于递归遍历获取所有子节点id
     *
     * @return
     */
    public List<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        // 添加当前节点的id
        ids.add(this.getId());

        if (CollectionUtils.isNotEmpty(this.childrenList)) {
            for (DtpRwdTreeLightingObjectDTO child : this.childrenList) {
                // 递归添加子节点的所有id
                ids.addAll(child.getAllIds());
            }
        }
        return ids;
    }

    public void buildInstanceType() {
        if (StringUtils.isNotBlank(this.getClassCode())) {
            this.setInstanceType(DemandInstanceTypeEnum.create(BaseEnum.analyze(
                    this.getClassCode(), DtpRwdClassCodeEnum.class, false)));
        }
    }


    /**
     * 构建而外参数
     *
     * @param map 构建参数对象
     */
    public void buildExtraParameter(Map<String, Object> map) {
        //只有空间级别会消费以下数据
        if (!DemandInstanceTypeEnum.SPACE.equals(instanceType) || map == null) {
            return;
        }
        //添加空间模型需要用到字段 都是必要字段
        modelMap = new HashMap<>();
        modelMap.put("bimId", map.get("bimId"));
        modelMap.put("bimLocation", map.get("bimLocation"));
        modelMap.put("classCode", map.get("classCode"));
        modelMap.put("className", map.get("className"));
        modelMap.put("floorId", map.get("floorId"));
        modelMap.put("floorLocalId", map.get("floorLocalId"));
        modelMap.put("floorName", map.get("floorName"));
        modelMap.put("id", map.get("id"));
        modelMap.put("localName", map.get("localName"));
        modelMap.put("name", map.get("name"));
        modelMap.put("objType", map.get("objType"));

        if (map.get("outline") != null) {
            try {
                modelMap.put("outline", JSONArray.parseArray(map.get("outline").toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        modelMap.put("roomFuncType", map.get("roomFuncType"));
        modelMap.put("spaceId", map.get("spaceId"));
        //以下字段暂无
        modelMap.put("isChannelType", map.get("isChannelType"));
        modelMap.put("color", map.get("color"));
    }

}
