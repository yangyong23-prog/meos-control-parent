package com.persagy.apccontrol.basic.outside.feign.model.response.midpermission;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目数据响应
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectResponse extends BaseResponse {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 项目本地编码
     */
    @JsonAlias("projectLocalID")
    private String projectLocalId;

    /**
     * 项目名称
     */
    @JsonAlias("projName")
    private String projectName;

    /**
     * 项目本地名称
     */
    private String projectLocalName;

    /**
     * 开业时间, {@code yyyyMMdd}
     */
    private String businessOpeningTime;

    /**
     * 数据质量标签分类code
     */
    private String dataQualityLabelClassificationCode;

    /**
     * 数据质量标签分类名称
     */
    private String dataQualityLabelClassificationName;

    /**
     * 地图展示方式code
     */
    private String mapDisplayModeCode;

    /**
     * 地图展示方式名称
     */
    private String mapDisplayModeName;

    /**
     * 竣工日期, {@code yyyyMMdd}
     */
    private String beCompletedDate;

    /**
     * 商业层数
     */
    private String businessFloorNum;

    /**
     * 办公层数
     */
    private String officeFloorNum;

    /**
     * BIM模型中编码
     */
    @JsonAlias("BIMID")
    private String bimId;

    /**
     * 省编码
     */
    @JsonAlias("province")
    private String provinceCode;

    /**
     * 市编码
     */
    @JsonAlias("city")
    private String cityCode;

    /**
     * 区域编码
     */
    @JsonAlias("partition")
    private String partitionCode;

    /**
     * 区域名称
     */
    private String partitionName;

    /**
     * 地理位置名称
     */
    private String geographicalPosition;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 海拔
     */
    private String altitude;

    /**
     * 气候区
     */
    @JsonAlias("climateZone")
    private String climateZoneCode;

    /**
     * 气候区名称
     */
    private String climateZoneName;

    /**
     * 项目功能类型编码
     */
    @JsonAlias("functionType")
    private String functionTypeCode;

    /**
     * 项目功能类型名称
     */
    private String functionTypeName;

    /**
     * 管理分区编码
     */
    @JsonAlias("groupManageZone")
    private String groupManageZoneCode;

    /**
     * 管理分区编码名称
     */
    private String groupManageZoneName;

    /**
     * 项目介绍
     */
    private String introduction;

    /**
     * 项目总面积
     */
    private BigDecimal totalArea;

    /**
     * 地下车库面积
     */
    private BigDecimal undergroundGarageArea;

    /**
     * 地上面积
     */
    @JsonAlias("abovegroundArea")
    private BigDecimal aboveGroundArea;

    /**
     * 地下面积
     */
    @JsonAlias("undergroundArea")
    private BigDecimal underGroundArea;

    /**
     * 占地面积
     */
    private BigDecimal coveredArea;

    /**
     * 空调面积
     */
    private BigDecimal airConditioningArea;

    /**
     * 公区面积
     */
    private BigDecimal publicArea;

    /**
     * 租区面积
     */
    private BigDecimal rentedArea;

    /**
     * 办公面积
     */
    private BigDecimal officeArea;

    /**
     * 商业面积
     */
    private BigDecimal commercialArea;

    /**
     * 餐饮面积
     */
    private BigDecimal diningArea;

    /**
     * 超市面积
     */
    private BigDecimal supermarketArea;

    /**
     * 项目状态
     *
     * <ul>
     *     <li><b>1</b> - 启用</li>
     *     <li><b>0</b> - 停用</li>
     * </ul>
     */
    private String state;

    /**
     * 项目在线状态
     *
     * <ul>
     *     <li><b>1</b> - 上线<li>
     *     <li><b>0</b> - 未上线</li>
     * </ul>
     */
    private String liveStatus;

    /**
     * 项目在线状态, {@code yyyyMMdd}
     */
    private String liveTime;

    /**
     * 城市发展水平名称
     */
    @JsonAlias("urbanDevpLevName")
    private String urbanDevelopmentLev;

    /**
     * 项目图片
     */
    private List<String> pictures;

    private String provinceName;

    private String cityName;

}
