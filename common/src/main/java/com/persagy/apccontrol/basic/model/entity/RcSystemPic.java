package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.ClassStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统图表
 *
 * @TableName rc_system_pic
 */

@TableName(value = "rc_system_pic")
@Data
@EqualsAndHashCode(callSuper = true)
public class RcSystemPic extends SynDTO {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId
    private String id;

    private String groupCode;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 对象ID，指的是这系统图是提供给谁查询的
     */
    private String objectId;

    private String name;

    /**
     * 图类型编码
     */
    private String type;

    /**
     * 宽
     */
    private Double width;

    /**
     * 高
     */
    private Double height;

    /**
     * 图片base64字符串
     */
    private String pictureCode;

    /** 结果集是否进行了压缩 true-是 */
    private Boolean compressed;

    /**
     * 设备类
     */
    private String classCode;

    /**
     * 设备状态
     */
    private ClassStatusEnum classStatus;

    /**
     * 偏差量，表示设备管道的连接位置
     */
    private String deviation;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 更新人
     */
    private String updateUser;
    /**
     * 同步时间
     */
    private Date synTime;

}