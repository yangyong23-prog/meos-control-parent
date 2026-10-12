package com.persagy.apccontrol.basic.service.parking;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.parking.ParkingSpaceStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingEquipStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingFloorParkingSpaceStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.parking.ParkingSpaceGridStatisticsVO;
import com.persagy.apccontrol.basic.service.BizComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum.WEGMCM;
import static com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum.WEGMGT;

public interface ParkingService extends BizComponent<ParkingService> {

    String isChargingRateOptional = "isChargingRateOptional";

    /**
     * 车辆总数统计 key（项目对象 carNum，仅项目级返回）
     *
     * @since v7.260901.0
     */
    String CAR_TOTAL = "CAR_TOTAL";

    List<String> PARKING_SPACE_TYPE_LIST = Arrays.asList("SFT-04.00.00", "SFT-04.01.00", "SFT-04.02.00"
            , "SFT-04.03.00", "SFT-04.04.00", "SFT-04.05.00", "SFT-04.06.00", "SFT-04.07.00");

    List<String> PARKING_EQUIP_TYPE_LIST = Arrays.asList(WEGMCM.value(), WEGMGT.value());


    /**
     * 停车场设备类所有设备类，并且带有部分过滤逻辑
     */
    ArrayList<String> PARKING_EQUIPMENT_CLASS_VALUE = Lists.newArrayList(
            DtpRwdClassCodeEnum.CFPKPS.value(),
            DtpRwdClassCodeEnum.SEGECP.value(),
            DtpRwdClassCodeEnum.WEGMGT.value(),
            DtpRwdClassCodeEnum.WEGMCM.value()
    );


    /**
     * 车位网格统计
     *
     * @param param
     * @param isConvert 对象是否转换，兼容网格图统计跟其他统计返回的数据格式问题
     */

    List<ParkingSpaceGridStatisticsVO> getParkingSpaceGridStatistics(ParkingSpaceStatisticsQueryParam param, Boolean isConvert);

    /**
     * 车位统计
     * <p>
     * PROJECT 级：空闲车位按实例化规则感知（CFPKPS parkingStatus 统计 / 项目对象 availableParkingSpaceNumber 兜底），
     * 并返回车辆总数 CAR_TOTAL（项目对象 carNum 有实例化规则时）；无规则不返字段，有规则无 IOT 值返 null
     *
     * @param param 查询参数（instanceType=PROJECT/FLOOR，instanceId 对应实例 ID）
     * @return 车位类型 → 数量（Integer，可为 null），PROJECT 级额外含 CAR_TOTAL（车辆总数）；
     * 无规则时对应 key 不返回
     */
    Map<String, Integer> getParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam param);


    List<ParkingFloorParkingSpaceStatisticsVO> getFloorParkingSpaceStatistics(ParkingSpaceStatisticsQueryParam param);

    List<ParkingEquipStatisticsVO> getParkingEquipStatistics(ParkingSpaceStatisticsQueryParam param);




}
