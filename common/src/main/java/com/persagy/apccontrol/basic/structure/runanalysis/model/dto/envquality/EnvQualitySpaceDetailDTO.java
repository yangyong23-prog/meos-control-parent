package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.SpacesAnalysis;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import lombok.Data;
import org.apache.commons.lang3.Range;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 环境品质-空间温度 DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class EnvQualitySpaceDetailDTO {

    /**
     * 空间ID
     */
    private String id;

    /**
     * 空间名称
     */
    private String localName;

    /**
     * 楼层ID
     */
    private String floorId;

    /**
     * 时间标签
     */
    private List<TimeTagPO> timeTag;

    /**
     * 营业时间
     */
    private List<Range<LocalTime>> businessTime;

    /**
     * 服务系统
     */
    private List<DtpRwdObjectDTO> serviceSystems;

    /**
     * 温度品质配置
     */
    private DemandConfigAirconditioningTdbQualityValueObject tdbQuality;

    /**
     * 温度孪生控制目标
     */
    private Map<LocalDateTime, String> dateTime2TempTwinControlAim;

    /**
     * 实际温度
     */
    private Map<LocalDateTime, String> dateTime2Tdb;

    /**
     * 关联类型 -> 对象数据
     */
    private Map<String, List<EnvQualitySpaceRelObjectDTO>> relClassCode2Detail;

    /**
     * 异常记录
     */
    private List<ExceptionRecordDTO> exceptionRecords;

    @JsonSetter(SpacesAnalysis.Fields.businessTime)
    public void deserializeBusinessTime(
            LinkedHashSet<String> businessTime) {
        this.businessTime = RangeUtils.instanceByArray(businessTime,
                item -> LocalTime.parse(item, MidCalendarClientService.OPENING_TIME_FORMATTER),
                ArrayList::new);
    }

    @JsonGetter(SpacesAnalysis.Fields.businessTime)
    public List<String> serializeBusinessTime() {
        return RangeUtils.toString(this.businessTime,
                item -> item.format(MidCalendarClientService.OPENING_TIME_FORMATTER), ArrayList::new);
    }
}
