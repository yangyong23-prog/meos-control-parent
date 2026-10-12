package com.persagy.apccontrol.basic.model.entity;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.convert.InfoCodeConfigDataSourceConvert;
import com.persagy.apccontrol.basic.convert.InfoCodeConfigExtendInfoConvert;
import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdTagEnum;
import com.persagy.apccontrol.basic.model.enumtype.info.InfoTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信息点配置
 *
 * @TableName info_code_config
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "信息点配置")
@TableName(value = "info_code_config", autoResultMap = true)
public class RcInfoCodeCfg {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId
    private String id;

    private String groupCode;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点名称，或分组名称
     */
    private String name;

    /**
     * 信息点别名
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String aliasName;

    /**
     * 缩略名称，后端不处理，前端自行处理
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String abbreviationName;

    /**
     * 设备类
     */
    private String classCode;

    private String className;

    /**
     * 信息点合并编码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String mergeCode;

    /**
     * 信息点合并名称
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String mergeName;

    /**
     * 分组ID
     */
    private String groupInfo;

    /**
     * 单位名称
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    /**
     * 换算系数。页面展示的数值=采集数据*换算系数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal scale;

    /**
     * 对象类型
     */
    private String objType;

    /**
     * 数据类型，与中台交付不一致
     * GROUP：信息点分组数据
     * STRING：字符串数据
     * ENUM：枚举类型，包含中台的 BOOLEAN，此时dataSource有值
     */
    private InfoTypeEnum dataType;

    /**
     * 一级标签
     */
    private String firstTag;

    /**
     * 二级标签
     */
    private String secondTag;

    /**
     * 展示顺序
     */
    private Integer ordered;

    /**
     * 枚举类型，对应的数据源
     */
    @TableField(value = "`data_source`", typeHandler = InfoCodeConfigDataSourceConvert.class)
    private List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource;

    /**
     * 扩展信息-额外信息点计算表达式
     */
    //@TableField(typeHandler = FastjsonTypeHandler.class)
    @TableField(value = "`extend_info`", typeHandler = InfoCodeConfigExtendInfoConvert.class)
    private InfoCodeConfigExtendInfo extendInfo;

    /**
     * 扩展配置
     */
    @TableField(exist = false)
    private ExtendSetting extendSetting;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * iot 数据保留小数位，对应中台的 scale
     */
    @TableField(exist = false)
    private Integer iotDecimal;

    /**
     * 顶级分组ID
     */
    @TableField(exist = false)
    private String topGroupId;

    @TableField(exist = false)
    private String objectId;

    /**
     * 是否仅做显示，不允许控制
     * #{@link com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo#view}
     */
    @TableField(exist = false)
    private String view;

    /**
     * -1 或 null 手自动下均展示, 0-手动下展示，1-自动下展示
     * #{@link com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo#force}
     */
    @TableField(exist = false)
    private String force;

    /**
     * 参数类型：运行参数，设定参数 desc，兼容旧版前端界面信息点值的跳转功能
     * firstTag：运行参数，设定参数 -> parameterType: 运行参数
     */
    @TableField(exist = false)
    private String parameterType;

    // 信息点的值，不分是静态还是动态，均赋值
    @TableField(exist = false)
    private String dataValue;

    // 信息点的值对应的名称，枚举类型对应name，非枚举类型与dataValue同值
    @TableField(exist = false)
    private String dataValueName;

    /***上报时间 yyyyyMMddhhmmss */
    @TableField(exist = false)
    private String dataTime;
    
    /*** 时间常量 */
    @TableField(exist = false)
    private Integer dataIndate;

    /*** 0-联动控制，1-单点控制 */
    @TableField(exist = false)
    private String forceModel;

    /**
     * 强控点位集合
     */
    @TableField(exist = false)
    private JSONArray forceInfo;

    /***失效时间 yyyyyMMddhhmmss */
    @TableField(exist = false)
    private String expireTime;

    /**
     * 人工干预
     */
    @TableField(exist = false)
    private JSONObject changeContent;

    /**
     * 当前信息点的级联信息点-聚合展示使用
     */
    @TableField(exist = false)
    private List<RcInfoCodeCfg> linkCodes;

    /**
     * 系统ID
     */
    @TableField(exist = false)
    private String systemId;

    /**
     * 系统类型，兼容旧版的末端数据
     */
    @TableField(exist = false)
    private DtpRwdSystemClassCodeEnum systemType;

    /**
     * 运行参数重要等级
     */
    @TableField(exist = false)
    private String importanceLevel;


    /**
     * 原始数据类型
     */
    @TableField(exist = false)
    private String originDataType;

    /**
     * 类别 STATIC：静态信息点
     */
    @TableField(exist = false)
    private String category;
    /**
     * 值为1时替换图标
     */
    @TableField(exist = false)
    private Integer displayMode;

    /**
     * @param dataValue
     */
    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    /**
     * 是否为动态信息点参数
     *
     * @return 是否为动态信息点参数
     * @since 5.242502.0
     */
    public boolean isDynamicParameter() {
        return DtpRwdTagEnum.RUN_PARAM.value().equals(getParameterType());
    }

    /**
     * 是否为设定信息点参数
     *
     * @return 是否为设定信息点参数
     * @since 5.242502.0
     */
    public boolean isSettingParameter() {
        return DtpRwdTagEnum.SETTING_PARAM.value().equals(getFirstTag());
    }

    /**
     * 获取枚举类型的 name
     */
    public String getDataValueName() {
        return this.getDataValueName(dataValue);
    }

    /**
     * 获取枚举类型的 name
     */
    public String getDataValueName(String dataValue) {
        if (dataSource != null && StrUtil.isNotBlank(dataValue)) {
            try {
                for (DtpKnowledgeInfoCodeDataSourceDTO dataSource : dataSource) {
                    if (dataValue.equals(dataSource.getCode())) {
                        return dataSource.getName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dataValue;
    }

    /**
     * 获取枚举类型的 name
     */
    @Transient
    public Map<String, String> getDataSourceByMap() {
        Map<String, String> map = new HashMap<>();
        if (dataSource != null) {
            try {
                for (DtpKnowledgeInfoCodeDataSourceDTO dataSource : dataSource) {
                    map.put(dataSource.getCode(), dataSource.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 获取动态信息点名称
     *
     * @param defaultName 默认名称
     * @return 动态信息点名称
     * @since 5.240402.0
     */
    public String getDynamicInfoCodeName(String defaultName) {
        return StringUtils.defaultIfBlank(aliasName, StringUtils.defaultIfBlank(name, defaultName));
    }

    public static String getFirstGroupIdByName(String classCode, String firstGroupName) {
        return  !StrUtil.isAllNotBlank(classCode, firstGroupName) ? null : (classCode + MD5Utils.md5Hex(firstGroupName, StandardCharsets.UTF_8.name()));
    }
    
    public String getClassCodeAndInfoCode() {
        return classCode + infoCode;
    }

}