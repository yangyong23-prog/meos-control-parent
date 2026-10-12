package com.persagy.apccontrol.basic.service.duty.strategy;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.handler.duty.SecurityEquipmentTypeHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityCameraTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentTypeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.SecurityCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.SecurityChildCardDataVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.persagy.apccontrol.basic.service.security.SecurityService.*;


/**
 * 安防首页卡片
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/24 15:42
 * @since 7.250402.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SecurityHomePageCardDataHandler extends AbstractHomePageCardDataHandler<SecurityCardDataVO> {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    private static final Triple<String, String, SecurityCameraTypeEnum> DEFAULT_KEY =
            Triple.of("UNDEFINED", "UNDEFINED_CONFIG", SecurityCameraTypeEnum.UNKNOWN);
    private static final ArrayList<String> SECURITY_INFO_CODES = Lists.newArrayList(
            InfoCodeEnum.ONLINE_STATUS.getCode(),
            InfoCodeEnum.RUN_STATUS.getCode(),
            InfoCodeEnum.ARMING_STATUS.value(),
            InfoCodeEnum.DEPLOY_WITHDRAWALARM_STATUS.getCode()
    );

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.SECURITY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SecurityCardDataVO execute(HomePageParam param) {

        // 1. 获取设备统计数据
        Map<String, List<DtpRwdEquipmentDTO>> equipmentStatisticsMap = getEquipmentStatistics();

        SecurityCardDataVO securityCardData = new SecurityCardDataVO();
        if (equipmentStatisticsMap.isEmpty()) {
            return securityCardData;
        }
        // 全量设备id
        Set<String> equipmentIds = extractEquipmentIds(equipmentStatisticsMap);
        Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> enumAndObjectMap = initEquipmentTypeMap();
        Map<String, Set<String>> idAndInfoCodeRuleMap = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(convertToIdMapSafely(equipmentStatisticsMap), SECURITY_INFO_CODES);
        Set<String> rules = idAndInfoCodeRuleMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        // 3. 并行处理信息点和异常事件
        Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap = processInParallel(equipmentIds, idAndInfoCodeRuleMap, securityCardData);

        // 4.根据设备类，进行信息点数据合并
        equipmentStatisticsMap.forEach((classCode, equips) -> {
            // 根据设备类型分发处理逻辑
            if (DtpRwdClassCodeEnum.SPVSCM.value().equals(classCode)) {
                handlerCameraTypeAndHasArmingFunc(equips, infoCodeEnumMap, enumAndObjectMap, rules);
            } else {
                final Set<String> equipIds = Collections.unmodifiableSet(
                        equips.stream()
                                .map(DtpRwdEquipmentDTO::getId)
                                .collect(Collectors.toSet())
                );
                if (DOOR_EQUIPMENT_CLASS_STRING.contains(classCode)) {
                    // 处理门设备类
                    handleEquipment(equipIds, infoCodeEnumMap, enumAndObjectMap, SecurityEquipmentTypeEnum.DOOR, rules);
                } else if (DtpRwdClassCodeEnum.SPIACU.value().equals(classCode)) {
                    // 处理入侵防盗设备
                    handleEquipment(equipIds, infoCodeEnumMap, enumAndObjectMap, SecurityEquipmentTypeEnum.BURGLAR_ALARM_DEVICE, rules);
                } else if (DtpRwdClassCodeEnum.WEGMCM.value().equals(classCode)) {
                    // 处理摄像头
                    handleEquipment(equipIds, infoCodeEnumMap, enumAndObjectMap, SecurityEquipmentTypeEnum.CAMERA, rules);
                }
            }
        });

        // 5. 过滤并设置结果
        securityCardData.setListSecurityChildCard(filterEmptyData(enumAndObjectMap));

        return securityCardData;
    }


    /**
     * 提取设备ID集合
     *
     * @param equipmentMap 设备分类集合
     * @return 设备ID集合
     */
    private Set<String> extractEquipmentIds(Map<String, List<DtpRwdEquipmentDTO>> equipmentMap) {
        return equipmentMap.values().stream()
                .flatMap(List::stream)
                .map(DtpRwdEquipmentDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 初始化设备类型映射
     *
     * @return 设备类型映射
     */
    private Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> initEquipmentTypeMap() {
        Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> map = new LinkedHashMap<>();
        Arrays.stream(SecurityEquipmentTypeEnum.values()).forEach(type -> {
            SecurityChildCardDataVO vo = new SecurityChildCardDataVO();
            vo.setName(type.getReasonPhrase());
            vo.setNameCode(type);
            map.put(type, vo);
        });
        return map;
    }


    /**
     * 并行处理信息点和异常事件
     *
     * @param equipmentIds         设备id
     * @param idAndInfoCodeRuleMap id和信息点规则集合
     * @param securityCardData     安防首页卡片数据
     * @return 信息点枚举和设备ID集合
     */
    private Map<SecurityEquipmentStatusEnum, Set<String>> processInParallel(
            Set<String> equipmentIds,
            Map<String, Set<String>> idAndInfoCodeRuleMap,
            SecurityCardDataVO securityCardData) {

        CompletableFuture<Map<SecurityEquipmentStatusEnum, Set<String>>> infoCodeFuture = CompletableFuture.supplyAsync(
                () -> mapInfoCodeEnumAndEquipmentId(equipmentIds, idAndInfoCodeRuleMap),
                BusinessThreadPoolExecutor.ttlExecutor()
        );

        CompletableFuture<Void> exceptionFuture = CompletableFuture.runAsync(
                () -> {
                    CardTagVO cardTag = new CardTagVO();
                    cardTag.setErrorEventTotal(getExceptionEventCountByObjIds(equipmentIds));
                    securityCardData.setCardTag(cardTag);
                },
                BusinessThreadPoolExecutor.ttlExecutor()
        );

        return CompletableFuture.allOf(infoCodeFuture, exceptionFuture)
                .thenApply(v -> infoCodeFuture.join())
                .join();
    }


    /**
     * 过滤空数据
     *
     * @param enumAndObjectMap 枚举卡片数据集合
     * @return 枚举卡片数据集合
     */
    private Collection<SecurityChildCardDataVO> filterEmptyData(
            Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> enumAndObjectMap) {

        return enumAndObjectMap.values().stream()
                .filter(vo -> {
                    Integer equipmentTotal = vo.getEquipmentTotal();
                    Integer runStatusTotal = vo.getRunStatusTotal();
                    Integer armingStatusTotal = vo.getArmingStatusTotal();
                    Integer onlineStatusTotal = vo.getOnlineStatusTotal();

                    // 判断是否四个值不全为null且不全为0
                    return !(Stream.of(equipmentTotal, runStatusTotal, armingStatusTotal, onlineStatusTotal)
                            .allMatch(total -> total == null || total == 0));
                })
                .collect(Collectors.toList());
    }

    /**
     * 将分类的map集合转为信息点规则查询的入参
     *
     * @param equipmentMap 分类的map集合
     * @return 信息点规则查询的入参
     */
    public Map<String, Set<String>> convertToIdMapSafely(
            Map<String, List<DtpRwdEquipmentDTO>> equipmentMap) {

        if (equipmentMap == null) {
            return Collections.emptyMap();
        }

        return equipmentMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Optional.of(entry.getValue())
                                .map(this::extractEquipmentIds)
                                .orElse(Collections.emptySet()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 从设备列表中提取ID集合
     *
     * @param equipmentList 设备DTO列表
     * @return 设备ID集合
     */
    private Set<String> extractEquipmentIds(List<DtpRwdEquipmentDTO> equipmentList) {
        return equipmentList.stream()
                .map(DtpRwdEquipmentDTO::getId) // 提取ID
                .filter(Objects::nonNull) // 过滤掉null值
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 保持原始顺序
    }

    /**
     * 将设备ID集合和信息点规则集合映射为信息点枚举和设备ID集合
     *
     * @param equipmentIds         设备ID集合
     * @param idAndInfoCodeRuleMap 设备ID和信息点规则集合
     * @return 信息点枚举和设备ID集合
     */
    @SuppressWarnings("unchecked")
    public Map<SecurityEquipmentStatusEnum, Set<String>> mapInfoCodeEnumAndEquipmentId(Set<String> equipmentIds,
                                                                                       Map<String, Set<String>> idAndInfoCodeRuleMap) {
        // 信息点数据
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(equipmentIds, SECURITY_INFO_CODES);
        return new LinkedHashMap<>(infoCodeValues.stream()
                .filter(vo -> Objects.nonNull(vo.getInfoCodeValue()) && "1".equals(vo.getInfoCodeValue()))
                .collect(
                        Collectors.groupingBy(vo ->
                                        mapToStatusEnum(
                                                vo.getInfoCode(),
                                                idAndInfoCodeRuleMap.getOrDefault(vo.getObjectId(), SetUtils.emptySet())
                                        ),
                                Collectors.mapping(
                                        InfoCodeValueVO::getObjectId,
                                        Collectors.toSet()
                                )
                        )
                ));
    }

    /**
     * 查询指定空间下指定的设备类统计
     *
     * @return key 是按照给定设备类统计，value 物理世界对象列表
     */
    public Map<String, List<DtpRwdEquipmentDTO>> getEquipmentStatistics() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCode(Arrays.asList(
                                DtpRwdClassCodeEnum.CFCSDR.value(),
                                DtpRwdClassCodeEnum.CFCSFR.value(),
                                DtpRwdClassCodeEnum.SPEGFG.value(),
                                DtpRwdClassCodeEnum.SPIACU.value()
                        )
                )
                .filterMonitoringSurveillanceStatus()
                .addWithColumn(InfoCodeEnum.HAS_ARMING_FUNC.value(), InfoCodeEnum.CAMERA_TYPE.value())
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        
        //摄像头的设备单独查询 需要过滤存在 实时流协议（realTimeStreamProtocol）静态信息点的数据
        DtpRwdObjectQueryParam cameraQueryParam = new DtpRwdObjectQueryParam();
        cameraQueryParam.setClassCode(Arrays.asList(
                        DtpRwdClassCodeEnum.SPVSCM.value(), DtpRwdClassCodeEnum.WEGMCM.value()
                )
        );
        cameraQueryParam.addWithColumn(HAS_ARMING_FUNC_CODE, CAMERA_TYPE_CODE,
                InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode(),InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
        cameraQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        
        List<DtpRwdEquipmentDTO> listEquipment = Collections.synchronizedList(new ArrayList<>());
        
        //并发查询
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(
                () -> listEquipment.addAll(dtpRwdClientService.listEquipment(queryParam)),
                BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(
                () -> {
                    List<DtpRwdEquipmentDTO> camera = dtpRwdClientService.listEquipment(cameraQueryParam);
                    //车库摄像头过滤掉没有实时流协议的数据
                    camera.removeIf(dtpRwdEquipmentDTO -> {
                        String realTimeStreamProtocol =
                                dtpRwdEquipmentDTO.analyzeProperties(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.value());
                        return StringUtils.equals(DtpRwdClassCodeEnum.WEGMCM.value(),
                                dtpRwdEquipmentDTO.getClassCode()) && StringUtils.isEmpty(realTimeStreamProtocol);
                    });
                    if (CollectionUtils.isNotEmpty(camera)) {
                        listEquipment.addAll(camera);
                    }
                },
                BusinessThreadPoolExecutor.ttlExecutor()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Map<String, List<DtpRwdEquipmentDTO>> equipmentStatisticsMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(listEquipment)) {
            equipmentStatisticsMap = listEquipment.parallelStream()
                    .collect(Collectors.groupingByConcurrent(
                            DtpRwdEquipmentDTO::getClassCode,
                            ConcurrentHashMap::new,
                            Collectors.toList()
                    ));
        }
        return equipmentStatisticsMap;
    }

    /**
     * 根据信息点值和规则判断枚举
     *
     * @param infoCode         信息点值
     * @param hasInfoCodeRules 对象的有实例化规则的集合
     * @return 状态枚举
     */
    private SecurityEquipmentStatusEnum mapToStatusEnum(String infoCode, Set<String> hasInfoCodeRules) {
        // 信息点有规则则判断枚举
        if (hasInfoCodeRules.contains(infoCode)) {
            // 根据不同的infoCode进行状态映射
            switch (infoCode) {
                // 布防状态/部署撤防报警状态："1"表示布防(AMAZING)，"0"表示撤防(DISAMAZING)
                case "armingStatus":
                case "deployWithdrawAlarmStatus":
                    return SecurityEquipmentStatusEnum.AMAZING;
                // 运行状态："1"表示开启(OPEN)，"0"表示关闭(CLOSE)
                case "runStatus":
                    return SecurityEquipmentStatusEnum.OPEN;
                // 在线状态："1"表示在线(ONLINE)，"0"表示离线(OFFLINE)
                case "onlineStatus":
                    return SecurityEquipmentStatusEnum.ONLINE;
            }
        }
        // 不会存在这种情况
        return SecurityEquipmentStatusEnum.EMPTY;
    }

    /**
     * 处理摄像头类型和布防功能相关的设备统计信息
     *
     * @param equips           <枚举设备分类,设备信息>
     * @param infoCodeEnumMap  设备状态枚举与对应设备ID集合的映射
     * @param enumAndObjectMap 设备类型枚举与统计VO对象的映射
     */
    public void handlerCameraTypeAndHasArmingFunc(List<DtpRwdEquipmentDTO> equips,
                                                  Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                                  Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> enumAndObjectMap,
                                                  Set<String> rules) {

        // 定义摄像头类型与枚举的映射关系（可扩展）
        // 三元组结构：<设备属性值, 设备属性代码, 对应的摄像头类型枚举>
        List<Triple<String, String, SecurityCameraTypeEnum>> cameraTypeConfigs = Arrays.asList(
                Triple.of("1", InfoCodeEnum.CAMERA_TYPE.value(), SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_1),
                Triple.of("2", InfoCodeEnum.CAMERA_TYPE.value(), SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_2),
                Triple.of("3", InfoCodeEnum.CAMERA_TYPE.value(), SecurityCameraTypeEnum.SPVSCM_CAMERATYPE_3)
        );

        // 定义布防功能与枚举的映射关系（可扩展）
        List<Triple<String, String, SecurityCameraTypeEnum>> hasArmingFuncConfig = Collections.singletonList(
                Triple.of("1", InfoCodeEnum.HAS_ARMING_FUNC.value(), SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1)
        );

        // 将设备按照配置规则分组  结果映射：<配置规则三元组, 匹配该规则的设备ID集合>
        Map<Triple<String, String, SecurityCameraTypeEnum>, Set<String>> grouped =
                Stream.of(cameraTypeConfigs, hasArmingFuncConfig)
                        .flatMap(configs -> equips.parallelStream()
                                .collect(Collectors.groupingBy(
                                        equipment -> findMatchingConfig(configs, equipment), // 根据配置规则匹配设备
                                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet()) // 收集匹配设备的ID
                                )).entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (existing, replacement) -> { // 合并相同key的value集合
                                    existing.addAll(replacement);
                                    return existing;
                                }
                        ));

        // 处理分组后的结果
        grouped.forEach((config, equipIds) -> {
            // 只处理有效分组（配置不为空且设备ID集合不为空）
            if (isValidGroup(config, equipIds)) {
                // 设备大类
                SecurityEquipmentTypeEnum equipmentType = determineEquipmentType(config.getRight());
                // 更新类型统计信息
                handleEquipment(equipIds, infoCodeEnumMap, enumAndObjectMap, equipmentType, rules);
            }
        });
    }

    /**
     * 处理设备
     *
     * @param equipIds         设备id
     * @param infoCodeEnumMap  设备状态枚举与对应设备ID集合的映射
     * @param enumAndObjectMap 设备类型枚举与统计VO对象的映射
     * @param equipmentType    设备类型
     */
    private void handleEquipment(Set<String> equipIds,
                                 Map<SecurityEquipmentStatusEnum, Set<String>> infoCodeEnumMap,
                                 Map<SecurityEquipmentTypeEnum, SecurityChildCardDataVO> enumAndObjectMap,
                                 SecurityEquipmentTypeEnum equipmentType,
                                 Set<String> rules) {

        SecurityChildCardDataVO vo = enumAndObjectMap.get(equipmentType);
        SecurityEquipmentTypeHandler typeHandler = SecurityEquipmentTypeHandler.from(equipmentType);

        typeHandler.getHandlers().forEach(statusHandler -> {
            Set<String> statusCodes = infoCodeEnumMap.getOrDefault(
                    statusHandler.getStatusType(), Collections.emptySet());
            long count = equipIds.stream().filter(statusCodes::contains).count();
            statusHandler.handle(vo, count, rules);
        });

        vo.setEquipmentTotal(equipIds.size());
    }

    /**
     * 从配置列表中查找与设备匹配的配置项
     *
     * @param configs   配置列表，每个配置项是一个三元组，包含：
     *                  - 左值：匹配值
     *                  - 中值：设备属性名
     *                  - 右值：摄像头类型枚举
     * @param equipment 设备DTO对象
     * @return 匹配到的配置项三元组，若未找到则返回默认值DEFAULT_KEY
     */
    private Triple<String, String, SecurityCameraTypeEnum> findMatchingConfig(
            List<Triple<String, String, SecurityCameraTypeEnum>> configs,
            DtpRwdObjectDTO equipment) {
        for (Triple<String, String, SecurityCameraTypeEnum> config : configs) {
            String expectedValue = config.getLeft();
            String actualValue = equipment.analyzeProperties(config.getMiddle());
            
            //如果SPVSCM是 SPVSCM_HASARMINGFUNC_1 枚举需要过滤realTimeStreamProtocol!=null
            // 其它的需要过滤monitoringSurveillanceStatus=2/3
            if (expectedValue.equals(actualValue)) {
                if (config.getRight() == SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1 ){
                    String status = equipment.analyzeProperties(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode());
                    if ("2".equals(status) || "3".equals(status)) {
                        return config;
                    }
                }else {
                    if (StringUtils.isNotEmpty(equipment.analyzeProperties(InfoCodeEnum.REAL_TIME_STREAM_PROTOCOL.getCode()))) {
                        return config;
                    }
                }
            }
        }
        return DEFAULT_KEY;
    }

    /**
     * 校验安全摄像头配置分组是否有效
     *
     * @param config   三元组配置对象，包含摄像头分组信息。其中第三个元素为摄像头类型枚举
     * @param equipIds 设备ID集合，表示该分组下的设备集合
     * @return boolean 返回分组是否有效：
     * 1. 配置对象非空
     * 2. 设备集合非空
     * 3. 摄像头类型不是UNKNOWN
     */
    private boolean isValidGroup(Triple<?, ?, SecurityCameraTypeEnum> config, Set<String> equipIds) {
        return config != null
                && !equipIds.isEmpty()
                && config.getRight() != SecurityCameraTypeEnum.UNKNOWN;
    }

    /**
     * 根据摄像头类型确定对应的安全设备类型
     *
     * @param type 摄像头类型枚举值，参考SecurityCameraTypeEnum
     * @return SecurityEquipmentTypeEnum 返回对应的安全设备类型枚举值：
     * - 如果是带布防功能的摄像头(SPVSCM_HASARMINGFUNC_1)，返回入侵防盗设备(BURGLAR_ALARM_DEVICE)
     * - 其他所有摄像头类型都返回摄像头类型(CAMERA)
     */
    private SecurityEquipmentTypeEnum determineEquipmentType(SecurityCameraTypeEnum type) {
        return type == SecurityCameraTypeEnum.SPVSCM_HASARMINGFUNC_1
                ? SecurityEquipmentTypeEnum.BURGLAR_ALARM_DEVICE
                : SecurityEquipmentTypeEnum.CAMERA;
    }

}
