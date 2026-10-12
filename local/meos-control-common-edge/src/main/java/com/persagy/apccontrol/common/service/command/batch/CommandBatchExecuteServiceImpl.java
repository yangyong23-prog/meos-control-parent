package com.persagy.apccontrol.common.service.command.batch;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.hot.DtpRwdCentralHotInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.service.DtpRwdService;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.common.service.command.ManualAutoMaticSetService;
import com.persagy.apccontrol.common.service.command.batch.dto.CommandBatchExecuteDTO;
import com.persagy.apccontrol.common.service.command.batch.dto.CommandExecuteGroupDTO;
import com.persagy.apccontrol.common.util.SpecialInfoCodeUtil;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 指令下发服务
 * 1.下发自动需求指令
 * 2.下发手动指令
 *
 * @author yuyao
 */
@Service
@Slf4j
public class CommandBatchExecuteServiceImpl implements CommandBatchExecuteService {

    private static final String LOG_TAG = "指令下发";

    public static ExecutorService executor = Executors.newFixedThreadPool(15);
    @Resource
    private ProjectCfg projectCfg;

    @Resource
    private DtpRwdClientService dtpRwdClientService;

    @Resource
    private ManualAutoMaticSetService manualAutoMaticSetService;

    @Resource
    private IWSMessageService iwsMessageService;

    @Resource
    private DtpTwinClientService dtpTwinClientService;

    @Resource
    private DtpRwdService dtpRwdService;

    @Resource
    private EquipRecordService equipRecordService;
    @Autowired
    private AbstractCommonServer abstractCommonServer;


    @Override
    public void commandExecute(List<CommandBatchExecuteDTO> commandExecuteDTOList) {
        LocalDateTime now = LocalDateTime.now();

        //按照批次号进行分组
        Map<String, List<CommandBatchExecuteDTO>> map = new HashMap<>();
        commandExecuteDTOList.forEach(s -> {
            check(s);
            String key = s.getSystemId() + s.getBatchNumber();
            //兼容历史 无批次号 创建一个新的批次号
            if (StringUtils.isEmpty(key)) {
                key = s.getSystemId() + ChangeContent.buildBatchNumber(now);
            }
            if (map.containsKey(key)) {
                map.get(key).add(s);
            } else {
                List<CommandBatchExecuteDTO> list = new ArrayList<>();
                list.add(s);
                map.put(key, list);
            }
        });
        // 使用CountDownLatch来等待所有任务完成
        CountDownLatch latch = new CountDownLatch(map.size());
        map.forEach((k, v) -> {

            //记录下发的 ID
            List<Integer> equipRecordIds = new ArrayList<>();
            //多线程批量下发
            executor.execute(() -> {
                ContextUtil.setGroupCode(projectCfg.getGroupCode());
                ContextUtil.setTenant(projectCfg.getGroupCode());
                try {
                    // 这里填充一下当前的孪生值，为了记录日志--目前仅支持 最不利末端压差设定 点位
                    wirteCurrenTwinValue(v);

                    //自动模式判断
                    aiCommand(v);

                    //获取指令
                    List<DtpTwinAutoSendDTO> asList = getDtpTwinAutoSendDTO(v, equipRecordIds);
                    //下发指令
                    autoSend((new CommandExecuteGroupDTO(asList, v)));

                    //更新结果
                    updateEquip(v);
                } catch (Exception e) {
                    //批量更新执行状态
                    updateEquip(equipRecordIds, EquipRecord.getErrorExtendByString(LocaleMessageUtil.getMessage("key_dbe6283171f240f1bdeb6a571eca7909") + e.getMessage()));
                    log.error("批量指令下发异常", e);
                } finally {
                    // 每个任务完成时，计数减1
                    latch.countDown();
                }
            });
        });
        try {
            // 等待所有任务完成
            latch.await();
            // 在这里编写后续逻辑，这些逻辑会在所有多线程任务完成后执行
        } catch (InterruptedException e) {
            log.error("批量指令下发异常", e);
        }
        //页面推送
        executor.execute(this::pushMessageToWeb);
    }

    private void wirteCurrenTwinValue(List<CommandBatchExecuteDTO> commandBatchExecuteDTOS) {
        if (CollectionUtils.isEmpty(commandBatchExecuteDTOS)) {
            return;
        }
        try {
            List<QueryTwinDataOnMomentDTO> objects = new ArrayList<>();
            for (CommandBatchExecuteDTO commandBatchExecuteDTO : commandBatchExecuteDTOS) {
                if (DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode().equals(commandBatchExecuteDTO.getInfoCode())) {
                    QueryTwinDataOnMomentDTO queryTwinDataOnMomentDTO = new QueryTwinDataOnMomentDTO();
                    queryTwinDataOnMomentDTO.setProjectId(projectCfg.getProjectId());
                    queryTwinDataOnMomentDTO.setObjectId(commandBatchExecuteDTO.getObjectId());
                    queryTwinDataOnMomentDTO.setInfoCodes(Sets.newHashSet(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode()));
                    objects.add(queryTwinDataOnMomentDTO);
                }
            }
            List<DtpTwinIotData> iotDataList = null;
            if (objects.size() > 0) {
                QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO();
                queryTwinDataDTO.setObjects(objects);
                iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, queryTwinDataDTO);
            }

            List<DtpTwinIotData> vaildIotDatas = DtpTwinIotData.getVaildIotDatas(iotDataList);
            if (CollectionUtils.isEmpty(vaildIotDatas)) {
                return;
            }
            Map<String, String> objectValueMap = vaildIotDatas.stream().collect(Collectors.toMap(DtpTwinIotData::getObjectId, DtpTwinIotData::getDataValue, (v1, v2) -> v2));
            for (CommandBatchExecuteDTO commandBatchExecuteDTO : commandBatchExecuteDTOS) {
                if (DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode().equals(commandBatchExecuteDTO.getInfoCode())) {
                    commandBatchExecuteDTO.setBeforeValue(objectValueMap.get(commandBatchExecuteDTO.getObjectId()));
                }
            }
        } catch (Exception e) {
            ControlLogUtil.error("实时孪生数据填充失败", e);
        }
    }

    @Override
    public void commandExecuteByEquipRecord(List<EquipRecord> equipRecordList) {

        if (CollectionUtils.isEmpty(equipRecordList)) {
            return;
        }

        //数据分组 存在批量标签和不存在批量标签的单独处理
        List<EquipRecord> pcl = new ArrayList<>();
        equipRecordList.forEach(i -> {
            if (i.getChangeContent() == null) {
                return;
            }
            if (i.getExecStatus() != -1) {
                return;
            }
            pcl.add(i);
        });

        // 获取一次热站的温压差绑点规则
        List<ClassCode> criteria = new ArrayList<>();
        criteria.add(new ClassCode(DtpRwdSystemClassCodeEnum.ACCH.value(), Sets.newHashSet(DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaTempSet.getValue(), DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaPressureSet.getValue())));
        Map<String, Set<String>> objectsExistRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(projectCfg.getGroupCode(), projectCfg.getProjectId(), criteria);

        //构建批量执行参数 区分人工还是自动,人工指令优先执行

        //人工指令
        List<CommandBatchExecuteDTO> artificialList = new ArrayList<>();
        List<Integer> artificialIds = new ArrayList<>();
        //自动指令
        List<CommandBatchExecuteDTO> aiExecuteDTOList = new ArrayList<>();
        List<Integer> aiEquipRecordIds = new ArrayList<>();
        for (EquipRecord er : pcl) {
            CommandBatchExecuteDTO dto = new CommandBatchExecuteDTO();
            dto.setGroupCode(projectCfg.getGroupCode());
            dto.setProjectId(projectCfg.getProjectId());
            dto.setType(er.getChangeContent().getSourceFrom());
            dto.setAi(er.getChangeContent().getAi());
            dto.setObjectId(er.getObjId());
            dto.setInfoCode(er.getChangeContent().getInfoCode());
            dto.setValue(er.getChangeContent().getInfoValue());
            //dto.setTime(LocalDateTime.parse(er.getChangeContent().getExecTime(), DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
            dto.setTime(LocalDateTime.now().withSecond(0).withNano(0));
            dto.setTimeDuration(er.getChangeContent().getTimeDuration());
            dto.setExtend(er.getExtend());
            dto.setEquipRecordId(er.getId());
            dto.setBatchNumber(er.getChangeContent().getBatchNumber());
            dto.setSystemCode(er.getSystemCode());
            dto.setSystemId(er.getSystemId());

            // 热站的时间表，需要实时判断温压差规则
            if (DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaTempSet.getValue().equals(dto.getInfoCode())
                    || DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaPressureSet.getValue().equals(dto.getInfoCode())) {
                Set<String> infoCodeRules = objectsExistRuleMap == null ? null : objectsExistRuleMap.get(dto.getSystemId());
                if (infoCodeRules == null || !infoCodeRules.contains(dto.getInfoCode())) {
                    // 直接修正数据库状态位0
                    er.setExecStatus(0);
                    er.addErrorExtend(LocaleMessageUtil.getMessage("key_95700f1109ad4a86ab00549e7d290f15"));
                    this.equipRecordService.updateById(er);
                    continue;
                }
            }

            if (!er.getChangeContent().getAi()) {
                artificialList.add(dto);
                artificialIds.add(er.getId());
            } else {
                aiExecuteDTOList.add(dto);
                aiEquipRecordIds.add(er.getId());
            }
        }
        //先执行人工的指令
        commandExecute(artificialList, artificialIds);
        //执行自动指令
        commandExecute(aiExecuteDTOList, aiEquipRecordIds);
    }

    /**
     * 批量执行更新指令
     *
     * @param commandExecuteDTOList
     * @param equipRecordIds
     */
    private void commandExecute(List<CommandBatchExecuteDTO> commandExecuteDTOList, List<Integer> equipRecordIds) {
        try {
            commandExecute(commandExecuteDTOList);
        } catch (Exception e) {
            //批量更新执行状态
            updateEquip(equipRecordIds, EquipRecord.getErrorExtendByString(LocaleMessageUtil.getMessage("key_cd920d841ebc4844b15a93950eb72c5d") + e.getMessage()));
            log.error("批量指令下发异常", e);
        }
    }

    /**
     * 更新执行记录
     *
     * @param commandExecuteDTOList
     */
    private void updateEquip(List<CommandBatchExecuteDTO> commandExecuteDTOList) {
        List<EquipRecord> gx = new ArrayList<>();
        //更新执行状态
        commandExecuteDTOList.forEach(dto -> {
            EquipRecord e = new EquipRecord();
            int st = dto.getErrorMessage() == null ? 1 : 0;
            e.setId(dto.getEquipRecordId());
            e.addErrorExtend(dto.getErrorMessage());
            e.setExecStatus(st);
            e.setModifiedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
            gx.add(e);
        });
        //批量对象更新
        equipRecordService.updateBatchById(gx);
    }

    /**
     * 更新失败信息
     *
     * @param equipRecordIds
     * @param message
     */
    private void updateEquip(List<Integer> equipRecordIds, String message) {
        equipRecordService.lambdaUpdate()
                .set(EquipRecord::getExecStatus, 0)
                .set(EquipRecord::getExtend, message)
                .set(EquipRecord::getModifiedTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)))
                .in(EquipRecord::getId, equipRecordIds).update();
    }

    /**
     * 需求指令下发 自动需求指令
     */
    public void aiCommand(List<CommandBatchExecuteDTO> dto) {

        //跳过人工需求判断
        if (!dto.get(0).getAi()) {
            return;
        }
        Map<String, List<CommandBatchExecuteDTO>> groupMap = CommandBatchExecuteDTO.getGroupDTOByInfoCode(dto);
        groupMap.forEach((k, v) -> {

            //判断是否有人工干预指令
            //大小机信息点特殊处理
            String infoCode = StrategyInfoCodeEnum.getChillerNumByInfoCode(k);
            //批量判断是否 存在人工干预指令
            Set<String> interveneList = manualAutoMaticSetService.getAllManualInterventionKeyByInfoCode(infoCode);
            //批量判断当前是否为自动状态
            Set<String> automaticList = manualAutoMaticSetService.getManualAutoMaticByInfoCode(infoCode);
            v.forEach(i -> {
                //人工干预判断
                String intervene = RedisKey.systemIntervene(i.getObjectId(), infoCode);
                if (interveneList.contains(intervene)) {
                    i.setErrorMessage(LocaleMessageUtil.getMessage("key_7eb28816b7a64fe680fe5208d2911c7e"));
                    return;
                }

                //自动模式判断 照明需求不做判断
                if (AutoMaticInfoCodeEnum.controlDemandSwitchState(k)) {
                    return;
                }
                //手自动判断
                String automatic = RedisKey.getAutomatic(i.getObjectId(), infoCode);
                if (!automaticList.contains(automatic)) {
                    i.setErrorMessage(LocaleMessageUtil.getMessage("key_cc8a554242c04875aa906edefc1c8a57"));

                }
            });
        });
    }

    /**
     * 获取下发指令
     * 特殊指令特殊处理 如大小机指令合并为一条进行发送
     *
     * @param commandExecuteDTOList
     */
    private List<DtpTwinAutoSendDTO> getDtpTwinAutoSendDTO(List<CommandBatchExecuteDTO> commandExecuteDTOList, List<Integer> equipRecordIds) {

        Set<String> ids = new HashSet<>();
        Set<String> infoCodes = new HashSet<>();

        commandExecuteDTOList.forEach(i -> {
            ids.add(i.getObjectId());
            infoCodes.add(i.getInfoCode());
        });
        //批量获取 已执行的数据
        List<EquipRecord> equipRecordList = equipRecordService.getEquipRecordByInfoCodeDesc(projectCfg.getGroupCode(), projectCfg.getProjectId(), new ArrayList<>(ids), new ArrayList<>(infoCodes));
        Map<String, EquipRecord> erMap = new HashMap<>();
        equipRecordList.forEach(i -> erMap.put(i.getObjId() + i.getInfoCode(), i));

        List<DtpTwinAutoSendDTO> tasList = new ArrayList<>();
        commandExecuteDTOList.forEach(dto -> {

            //解除干预不下发指令
            if (dto.getReleaseIntervention()) {
                return;
            }
            //dto 参数补充
            complementCommandExecuteDTO(dto);

            if (SourceFromEnum.AI_DEMAND.equals(dto.getType())) {
                //查询最新执行指令 看是否指令重复
                EquipRecord equipRecord = erMap.get(dto.getObjectId() + dto.getInfoCode());

                if (equipRecord != null && equipRecord.getChangeContent() != null) {
                    //指令不重复 执行指令 人工干预指令不判断
                    String val = equipRecord.getChangeContent().getInfoValue();

                    //指令不重复 执行指令
                    if (StringUtils.equals(String.valueOf(dto.getValue()), val)) {
                        dto.setErrorMessage(LocaleMessageUtil.getMessage("key_aded2cad79d94441bca9244397aa32ef"));
                    }
                    //如果是历史指令直接失败
                    if (equipRecord.getChangeContent().toExecTime().isAfter(dto.getTime())) {
                        dto.setErrorMessage(LocaleMessageUtil.getMessage("key_3204bbffc17d4a8c8a0bbae7f1c8cd00"));
                    }
                }
            }

            DtpTwinAutoSendDTO as = new DtpTwinAutoSendDTO(dto.getObjectId(), dto.getInfoCode(), String.valueOf(dto.getValue()));

            //------------ 特殊信息点处理 ------------
            //关机特殊处理
            boolean check = isShutdown(dto.getSystemCode(), dto.getInfoCode(), dto.getValue());
            //冷站 冷热源 关机特殊处理 如果冷站进行关机 下发大冷机和小冷机0的数据
            if (check) {
                //设置0大0下指令
                Map chillerNum = new HashMap<>();
                chillerNum.put(dto.getInfoCode(), dto.getValue());
                chillerNum.putAll(SpecialInfoCodeUtil.getChillerNum());
                as.setData(chillerNum);
            }

            //存在异常信息跳过指令
            if (dto.getExecStatus() == 0) {
                addEqid(equipRecordIds, dto);
                return;
            }
            addEqid(equipRecordIds, dto);
            tasList.add(as);
        });
        return tasList;
    }

    /**
     * 添加指令表 id
     *
     * @param equipRecordIds
     * @param dto
     */
    private void addEqid(List<Integer> equipRecordIds, CommandBatchExecuteDTO dto) {
        if (dto.getEquipRecordId() != null) {
            equipRecordIds.add(dto.getEquipRecordId());
        }
    }


    /**
     * 判断 是否是关机命令
     * 只做冷热源的判断
     *
     * @return
     */
    private boolean isShutdown(String systemCode, String infoCode, Object value) {
        boolean coldSys = StringUtils.equals(systemCode, DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()) ||
                StringUtils.equals(systemCode, DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue());

        return coldSys &&
                StringUtils.equals(infoCode, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()) &&
                StringUtils.equals(String.valueOf(value), "0");
    }

    /**
     * 补充缺失参数
     *
     * @param dto
     */
    private void complementCommandExecuteDTO(CommandBatchExecuteDTO dto) {

        //照明类需求暂不填充
        if (DemandTypeEnum.LIGHTING.getValue().equals(dto.getSystemId())) {
            return;
        }

        //填充系统编码
        DtpRwdObjectDTO objectDTO = dtpRwdClientService.getObject(true, true,
                dto.getProjectId(), new DtpRwdObjectQueryParam().setId(dto.getObjectId()));
        //如果是泵组 系统id特殊处理 转换为 冷站
        if (DtpRwdClassCodeEnum.ACCPC_GROUP.getValue().equals(objectDTO.getClassCode())) {
            dto.setSystemCode(DtpRwdClassCodeEnum.COLD_SYSTEM.getValue());
        } else {
            dto.setSystemCode(objectDTO.getClassCode());
        }

        //获取对象的系统id
        String syId = dto.getObjectId();
        //泵组单独处理
        if (StringUtils.equals(objectDTO.getClassCode(), DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())) {
            Map<String, String> gx = dtpRwdService.getSystemIdByFreezePumpId();
            syId = gx.get(dto.getObjectId());
        }
        dto.setSystemId(syId);
    }

    /**
     * 下发执行指令
     *
     * @param groupDTO 分组指令
     */
    private void autoSend(CommandExecuteGroupDTO groupDTO) {
        if (groupDTO.isCheckFail()) {
            return;
        }
        StopWatch stopWatch = new StopWatch("指令下发 耗时统计");
        stopWatch.start("指令下发");
        //调用执行接口
        LogExecutor.instance().info(log, LOG_TAG, "实际下发指令", Collections.singletonList(Pair.of("指令", groupDTO)));
        int st = 0;
        String eMsg = null;
        //相同批次的数据 打包执行
        try {
            //照明需求的指令走 批量指令下发
            if (DemandTypeEnum.LIGHTING.value().equals(groupDTO.getSystemId())) {
                dtpTwinClientService.autoSendBatch(groupDTO.getDtpTwinAutoSendDTOList());
            } else {
                //按照 设备 ID 进行分组
                Map<String, List<DtpTwinAutoSendDTO>> map = new HashMap<>();
                groupDTO.getDtpTwinAutoSendDTOList().forEach(s -> {
                    String key = s.getObjectId();
                    if (map.containsKey(key)) {
                        map.get(s.getObjectId()).add(s);
                    } else {
                        List<DtpTwinAutoSendDTO> list = new ArrayList<>();
                        list.add(s);
                        map.put(s.getObjectId(), list);
                    }
                });
                //构建分组指令
                map.forEach((k, v) -> {
                    if (CollectionUtils.isEmpty(v)) {
                        return;
                    }
                    DtpTwinAutoSendDTO mb = v.get(0);
                    v.forEach(i -> mb.setParameter(i.getData()));
                    //执行当前设备指令
                    dtpTwinClientService.autoSend(mb);
                });
            }
            st = 1;
        } catch (Exception e) {
            eMsg = JSONObject.toJSONString(e.getMessage());
            LogExecutor.instance().error(log, LOG_TAG, "指令下发失败", Collections.singletonList(Pair.of("指令", groupDTO)));
        }
        stopWatch.stop();
        LogExecutor.instance().info(log, LOG_TAG, "指令下发",
                Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        
        
    }
    
    
    public void check(CommandBatchExecuteDTO dto) {
        dto.check();
        //项目id 和集团编码没有传递情况 取边缘端的配置
        if (StringUtils.isBlank(dto.getProjectId())) {
            dto.setProjectId(projectCfg.getProjectId());
        }
        if (StringUtils.isBlank(dto.getGroupCode())) {
            dto.setGroupCode(projectCfg.getGroupCode());
        }

        //如果无执行时间 把时间设置为当前时间
        if (dto.getTime() == null) {
            dto.setTime(LocalDateTime.now());
        }

    }

    /**
     * 通知前端做页面数据刷新
     */
    @Override
    public void pushMessageToWeb() {
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        feedbackInfoMsgMqttVO.setWebType(WebSocketTypeEnum.SYSTEM_DATE_CHANGE);
        iwsMessageService.pushMessageWeb(feedbackInfoMsgMqttVO);
    }

}
