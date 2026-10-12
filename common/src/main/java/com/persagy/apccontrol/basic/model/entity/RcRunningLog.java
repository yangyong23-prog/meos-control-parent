package com.persagy.apccontrol.basic.model.entity;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogContentDto;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "rc_running_log", autoResultMap = true)
public class RcRunningLog extends SynDTO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    private String systemId;
    /**
     * 对象id
     */
    private String objectId;

    /**
     * 对象名称
     */
    @Excel(name = "操作对象", orderNum = "4")
    private String objectName;
    /**
     * 对象类型
     */
    private String classCode;
    /**
     * 对象分类
     */
    private RunningLogObjectCategpryEnum objectCategory;

    /**
     * 一级分类
     */
    private RunningLogCategoryEnum firstLevelCategory;

    /**
     * 二级分类
     */
    private RunningLogCategoryEnum secondLevelCategory;

    /**
     * 三级分类
     */
    private RunningLogCategoryEnum thirdLevelCategory;

    @TableField(exist = false)
    @Excel(name = "日志分类", orderNum = "1")
    private String logCategory;

    /**
     * 日志标识: 1自动 / 2人工
     */
    private Integer type;

    /**
     * 日志标识1,人工/2:自动
     */
    @TableField(exist = false)
    @Excel(name = "日志标识", orderNum = "2")
    private String logType;
    /**
     * 关键字
     */
    private String keyWord;

    /**
     * 操作内容
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private RunningLogContentDto content;

    @Excel(name = "操作内容", orderNum = "5")
    @TableField(exist = false)
    private String contentDetail;

    /**
     * 操作详情
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONArray detail;

    /**
     * 生效开始时间
     */
    @Excel(name = "生效时间", orderNum = "6")
    @TableField(exist = false)
    private String effectTime;

    /**
     * 生效开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectStartTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectEndTime;

    /**
     * 调整原因
     */
    @Excel(name = "操作原因", orderNum = "7")
    private String reason;

    /**
     * 操作人
     */
    @Excel(name = "操作人", orderNum = "3")
    private String createUser;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Excel(name = "操作时间")
    @TableField(exist = false)
    private String logTime;

    /**
     * 同步时间
     */
    private LocalDateTime synTime;

    /**
     * 集团编码
     */
    @TableField(exist = false)
    private String groupCode;

    /**
     * 获取页面播报 处理后的显示 content 内容
     *
     * @param userName
     * @return
     */
    public String getPageBroadcastContent(String userName) {
        StringBuilder sb = new StringBuilder(createTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")) + " ");
        sb.append(userName + " " + content.getContentDesc());
        if (CollectionUtil.isNotEmpty(detail)) {
            sb.append("：");
            if (CollectionUtils.isNotEmpty(detail)) {
                List<RunningLogContentDto> details = JSONObject.parseArray(JSON.toJSONString(detail), RunningLogContentDto.class);
                for (RunningLogContentDto content : details) {
                    sb.append(content.getContentDesc());
                    if (!(details.indexOf(content) == details.size()-1))
                        sb.append("，");
                }
            }
        }
        if (StrUtil.isNotBlank(reason)) {
            sb.append(LocaleMessageUtil.getMessage("key_b6dc9dd6ebe14d4383aee3cdd2cfcfdc") + reason + "”");
        }
        return sb.toString();
    }

}
