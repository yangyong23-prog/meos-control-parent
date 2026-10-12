package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.enumtype.FunctionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WEBSOCKET推送配置
 *
 * @TableName websocket_config
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "WEBSOCKET推送配置")
@TableName(value = "websocket_config", autoResultMap = true)
public class WebsocketConfig {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId
    private String id;

    private FunctionTypeEnum functionType;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 设备类名称
     */
    private String className;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点名称
     */
    private String infoName;

    /**
     * 一级标签
     */
    private String firstTag;

    /**
     * 二级标签
     */
    private String secondTag;

    public static String getUniqueKey(String classCode, String infoCode) {
        return classCode + infoCode;
    }

}