package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.persagy.apccontrol.basic.config.dao.handler.LegendAnchorListTypeHandler;
import com.persagy.apccontrol.basic.config.dao.handler.LegendTextListTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;

import java.io.Serializable;
import java.util.List;

import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendAnchor;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendText;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import lombok.Data;

/**
 * @TableName graph_legend
 */
@TableName(value = "graph_legend",autoResultMap = true)
@Data
public class GraphLegend extends BaseAssignIdDO implements Serializable {

    /**
     * 图例id
     */
    @TableField(value = "legend_id")
    private String legendId;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private LegendSourceStatus status;

    /**
     * 图例对应图片
     */
    @TableField(value = "file_url")
    private String fileUrl;

    @TableField(value = "pic_url")
    private String picUrl;

    /**
     * 对象类code
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 图例code
     */
    @TableField(value = "legend_code")
    private String legendCode;

    /**
     * 对象类型
     */
    @TableField(value = "obj_type")
    private String objType;

    /**
     * 图例名称
     */
    @TableField(value = "legend_name")
    private String legendName;

    /**
     * 是否是小附件，0不是，1是
     */
    @TableField(value = "attach_type")
    private Boolean attachType;

    /**
     * 图例版本
     */
    @TableField(value = "legend_version")
    private Integer legendVersion;

    /**
     * 是否可用
     */
    @TableField(value = "valid")
    private Integer valid;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 文本框
     */
    @TableField(value = "texts", typeHandler = LegendTextListTypeHandler.class)
    private List<LegendText> texts;

    /**
     * 尺寸
     */
    @TableField(value = "size", typeHandler = FastjsonTypeHandler.class)
    private CoordinateDto size = new CoordinateDto();

    /**
     * 锚点
     */
    @TableField(value = "anchors", typeHandler = LegendAnchorListTypeHandler.class)
    private List<LegendAnchor> anchors;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public LegendSourceStatus checkLegendVersion(Integer newLegendVersion) {
        if (legendVersion == null) {
            legendVersion = 0;
        }
        if (newLegendVersion == null) {
            return LegendSourceStatus.DELETED;
        } else if (!legendVersion.equals(newLegendVersion)) {
            return LegendSourceStatus.UPDATED;
        } else {
            return LegendSourceStatus.DEFAULT;
        }
    }
}