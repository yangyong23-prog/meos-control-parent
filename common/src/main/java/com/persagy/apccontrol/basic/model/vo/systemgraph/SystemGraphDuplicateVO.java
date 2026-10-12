package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumGraphType;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class SystemGraphDuplicateVO {

    @Schema(description = "id")
    private long id;

    /**
     * 设备类code
     */
    @Schema(description = "设备类code")
    private String classCode;

    /**
     * 模版id
     */
    @Schema(description = "模版id")
    private String templateId;

    /**
     * 系统实例id
     */
    @Schema(description = "系统实例id")
    private String systemObjectId;

    /**
     * 关系图类
     */
    @Schema(description = "关系图类")
    private String graphTypeCode;

    /**
     * 系统图数据id
     */
    @Schema(description = "系统图数据id")
    private Long systemGraphDataId;

    /**
     * 系统图细节调整id
     */
    @Schema(description = "关系图细节调整id")
    private Long systemGraphDetailId;

    /**
     * 关系图图名称
     */
    @Schema(description = "关系图图名称")
    private String name;

    @Schema(description = "是否不可编辑")
    private Boolean isNotEdit;

    @Schema(description = "顺序")
    private Integer order;

    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private String projectId;

    /**
     * 生成id
     */
    private String generateId;

    /**
     * 状态：未发布，已发布，已下架
     */
    @Schema(description = "状态：未发布，已发布，已下架")
    private EnumSystemGraphStatus status;

    /**
     * 0、原图，1、副本
     */
    @Schema(description = "0、原图，1、副本")
    private EnumGraphType graphType;

    @Schema(description = "对象类型")
    private String objType;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public static SystemGraphDuplicateVO toVO(SystemGraphDuplicate po) {
        SystemGraphDuplicateVO vo = new SystemGraphDuplicateVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }
}
