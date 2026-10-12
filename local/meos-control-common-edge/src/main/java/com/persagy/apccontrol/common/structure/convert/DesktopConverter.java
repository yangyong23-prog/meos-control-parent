package com.persagy.apccontrol.common.structure.convert;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.common.model.vo.desktop.DesktopEquipmentDetailsVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 桌面端转换器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DesktopConverter {

    /**
     * 转换模型
     *
     * @param source                 数据源
     * @param infoCodeDetails        信息点详情
     * @param manualStatusExpireTime 手动状态过期时间
     * @param inSpace                所在空间
     * @return 模型
     * @since 24Q1 T3S2-5.18.0
     */
    @Mappings({
            @Mapping(target = "id", source = "source.id"),
            @Mapping(target = "name", source = "source.name"),
            @Mapping(target = "localId", source = "source.localId"),
            @Mapping(target = "localName", source = "source.localName"),
            @Mapping(target = "objType", source = "source.objType"),
            @Mapping(target = "classCode", source = "source.classCode"),
            @Mapping(target = "valid", source = "source.valid")
    })
    DesktopEquipmentDetailsVO toEquipmentDetailsVo(
            DtpRwdObjectDTO source,
            List<RcInfoCodeCfg> infoCodeDetails,
            String manualStatusExpireTime,
            DtpRwdObjectVO inSpace);

}
