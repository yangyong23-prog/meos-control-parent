package com.persagy.apccontrol.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcSysLog;
import com.persagy.apccontrol.basic.model.enumtype.AccSystemAlgorithmConfigEnum;
import com.persagy.apccontrol.basic.model.enumtype.CoolerRuleTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.SysResponseCode;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationDeleteDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationUpdateDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmStartCalculationDTO;
import com.persagy.apccontrol.basic.model.vo.ExpertExperienceDTO;
import com.persagy.apccontrol.basic.model.vo.cfg.ExpertRulesVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.resource.RcSysLogService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.system.SystemListDataService;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.model.vo.AIStrategicQuery;
import com.persagy.apccontrol.common.model.vo.AlgorithmCalculationVO;
import com.persagy.apccontrol.common.model.vo.StartAlgorithmCalculationDTO;
import com.persagy.apccontrol.common.outside.AlgorithmCalculationWebService;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.service.IAIStrategicService;
import com.persagy.apccontrol.vo.ExpertRulesDateCheckVO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.enumeration.error.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: AI策略生成设置service
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */
@Slf4j
@Service
public class AIStrategicServiceImpl implements IAIStrategicService {

    @Resource
    private RcBasicsConfigService basicsConfigService;
    @Resource
    private BizDtpRwdService bizDtpRwdService;
    @Resource
    private RcSysLogService sysLogService;
    @Resource
    private AlgorithmCalculationWebService algorithmCalculationWebService;
    @Resource
    private MidPermissionService midPermissionService;

    @Autowired
    private SystemListDataService systemListDataService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private DtpRwdClientService dtpRwdClientService;
    
    @Override
    public void addAIStrategicGenerate(ExpertRulesVO expertRulesVO) {
        // 参数验证
        expertRulesVO.check(getCooler(expertRulesVO.getProjectId(), expertRulesVO.getSystemId()));
        List<RcBasicsConfig> configList = getConfigList(expertRulesVO);
        ParameterCheck.listIsNotNull(configList, LocaleMessageUtil.getMessage("key_f66b6fbfe78f4f698204a8785ed88b59"));
        RcBasicsConfig basicsConfig = BeanUtil.copyProperties(expertRulesVO, RcBasicsConfig.class);
        basicsConfig.setType(BasicsConfigTypeEnum.EXPERT_RULES);
        basicsConfig.setResult(JSONObject.toJSONString(expertRulesVO));
        basicsConfig.setMemo(LocaleMessageUtil.getMessage("key_6cc5c1e16b074d669b45cdbd4f5a5d91"));
        saveBasicsConfig(expertRulesVO, basicsConfig);
    }

    /**
     * 报错配置并 发数同步消息
     *
     * @param expertRulesVO
     * @param basicsConfig
     */
    private void saveBasicsConfig(ExpertRulesVO expertRulesVO, RcBasicsConfig basicsConfig) {
        basicsConfigService.saveOrUpdateBasicsConfig(basicsConfig);
        // 若已注册算法，新增策略后调用算法修改
        algorithmCalculation(basicsConfig);

        //发数配置 消息同步给本地
        DataSynDTO dto = new DataSynDTO();
        dto.setTableName(DataSynRunEnum.RC_BASICS_CONFIG);
        dto.setProjectId(expertRulesVO.getProjectId());
        dto.setYsnObj(Collections.singletonList(JSONUtil.toJsonStr(basicsConfig)));
/*
        rabbitMessageSender.send(MqMessageConstant.APC_CONTROL_SYN_FANOUT, expertRulesVO.getProjectId(), JSONUtil.toJsonStr(Collections.singletonList(dto)));
*/
    }

    /**
     * 查询时间段内配置
     *
     * @param expertRulesVO
     * @return
     */
    private List<RcBasicsConfig> getConfigList(ExpertRulesVO expertRulesVO) {
        List<RcBasicsConfig> basicsConfigs = basicsConfigService.queryListConfig(expertRulesVO.getProjectId(), expertRulesVO.getSystemId(), BasicsConfigTypeEnum.EXPERT_RULES,
                expertRulesVO.getStartTime(), expertRulesVO.getEndTime());
        if (StringUtils.isNotEmpty(expertRulesVO.getConfigId())) {
            basicsConfigs = basicsConfigs.stream().filter(item -> !String.valueOf(item.getId()).equals(expertRulesVO.getConfigId())).collect(Collectors.toList());
        }
        return basicsConfigs;
    }

    @Override
    public List<ExpertRulesVO> queryAIStrategicGenerateList(AIStrategicQuery strategicQuery) {
        List<ExpertRulesVO> aiStrategicVOList = Lists.newArrayList();
        List<RcBasicsConfig> basicsConfigs = basicsConfigService.queryListConfig(strategicQuery.getProjectId(), BasicsConfigTypeEnum.EXPERT_RULES, strategicQuery.getSystemId());
        basicsConfigs.forEach(basicsConfig -> {
            ExpertRulesVO expertRulesVO = BeanUtil.toBean(basicsConfig, ExpertRulesVO.class).convertVo(basicsConfig);
            expertRulesVO.setConfigId(String.valueOf(basicsConfig.getId()));
            aiStrategicVOList.add(expertRulesVO);
        });
        return aiStrategicVOList;
    }

    @Override
    public ExpertRulesVO queryAIStrategicGenerate(String configId) {
        RcBasicsConfig basicsConfig = basicsConfigService.getById(configId);
        ExpertRulesVO strategicVO = BeanUtil.toBean(basicsConfig, ExpertRulesVO.class).convertVo(basicsConfig);
        strategicVO.setConfigId(configId);
        return strategicVO;
    }

    @Override
    public void editAIStrategicGenerate(ExpertRulesVO expertRulesVO) {
        // 参数验证
        expertRulesVO.check(getCooler(expertRulesVO.getProjectId(), expertRulesVO.getSystemId()));
        RcBasicsConfig basicsConfig = basicsConfigService.getById(expertRulesVO.getConfigId());
        ParameterCheck.isNull(basicsConfig, LocaleMessageUtil.getMessage("key_352fd3c1993144fd97304f2f86d61035"));
        List<RcBasicsConfig> configList = getConfigList(expertRulesVO);
        ParameterCheck.listIsNotNull(configList, LocaleMessageUtil.getMessage("key_f66b6fbfe78f4f698204a8785ed88b59"));
        RcBasicsConfig config = BeanUtil.copyProperties(expertRulesVO, RcBasicsConfig.class);
        config.setId(expertRulesVO.getConfigId());
        config.setType(BasicsConfigTypeEnum.EXPERT_RULES);
        config.setResult(JSONObject.toJSONString(expertRulesVO));
        config.setMemo(LocaleMessageUtil.getMessage("key_0ff69cbb56dd459ea6a5829ab8450405"));
        saveBasicsConfig(expertRulesVO, config);
    }

    @Override
    public void deleteAIStrategicGenerate(String configId) {
        RcBasicsConfig basicsConfig = basicsConfigService.getById(configId);
        ParameterCheck.isNull(basicsConfig, LocaleMessageUtil.getMessage("key_352fd3c1993144fd97304f2f86d61035"));
        basicsConfig.setMemo(LocaleMessageUtil.getMessage("key_c6652b85ecbe4dd6ba39de79ecdee66a"));
        basicsConfigService.deleteById(basicsConfig);
        algorithmCalculation(basicsConfig);
        //发数配置 消息同步给本地
        DataSynDTO dto = new DataSynDTO();
        dto.setTableName(DataSynRunEnum.RC_BASICS_CONFIG);
        dto.setProjectId(basicsConfig.getProjectId());
        dto.setYsnObj(Collections.singletonList(basicsConfig.getId()));
/*
        rabbitMessageSender.send(MqMessageConstant.APC_CONTROL_SYN_FANOUT, basicsConfig.getProjectId(), JSONUtil.toJsonStr(Collections.singletonList(dto)));
*/
    }
    
    /**
	 * Description: 
	 * @param projectId
	 * @param system void
	 * @author hengxi
	 * @since 2024年1月3日: 下午7:02:32
	 * Update By hengxi 2024年1月3日: 下午7:02:32
	 */
	 
	public static List<AccSystemAlgorithmConfigEnum> getAlgorithmConfig(String projectId, DtpRwdObjectDTO system) {
		String infoCode = AccSystemAlgorithmConfigEnum.getInfoCode(system.getClassCode());
		List<AccSystemAlgorithmConfigEnum> algorithmConfig = null;
		Object infoCodeValue = system.getUndefinedProperties() == null? null: system.getUndefinedProperties().get(infoCode);
		if(infoCodeValue == null) {
			log.error(projectId + " id: " + system.getId() + " infocode: " + infoCode + " infoCodeValue: " + infoCodeValue + " 未取到值,应该交付配置");
			algorithmConfig = AccSystemAlgorithmConfigEnum.getAlgorithmDefaultConfig(system.getClassCode());
		}else {
			if(infoCodeValue instanceof List) {
				List<String> lstInfoCodeValue = (List<String>)infoCodeValue;
				if(lstInfoCodeValue.size()>0) {
					algorithmConfig = AccSystemAlgorithmConfigEnum.getAlgorithmConfig(system.getClassCode(), infoCode, lstInfoCodeValue.get(0));
				}else {
					log.error(projectId + " id: " + system.getId() + " infocode: " + infoCode + " infoCodeValue: " + infoCodeValue + " 值为空数组");
				}
			}else if(infoCodeValue instanceof String){
				algorithmConfig = AccSystemAlgorithmConfigEnum.getAlgorithmConfig(system.getClassCode(), infoCode, String.valueOf(infoCodeValue));
			}else {
				log.error(projectId + " id: " + system.getId() + " infocode: " + infoCode + " infoCodeValue: " + infoCodeValue + " 非List类型也非字符串类型" + infoCodeValue.getClass().toGenericString());
			}
		}
		log.info(projectId + " id: " + system.getId() + " config: " + JSON.toJSON(algorithmConfig));
		return algorithmConfig;
	}


    @Override
    public void startAlgorithmCalculation(StartAlgorithmCalculationDTO dto) {
        String projectId = ContextUtil.getProjectId();
        //查询项目下的系统
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(dto.getSystemIdList());

        List<DtpRwdObjectDTO> lstCentralSystem = dtpRwdClientService.listCentralSystem(false, false, projectId, queryParam);
        for (DtpRwdObjectDTO system : lstCentralSystem) {
            List<AccSystemAlgorithmConfigEnum> lstAlgorithmConfig = getAlgorithmConfig(projectId, system);
            for (AccSystemAlgorithmConfigEnum algorithmConfig : lstAlgorithmConfig) {
            	AlgorithmStartCalculationDTO algorithmStartCalculationDTO = new AlgorithmStartCalculationDTO();
                try {
                	// 注册算法,算法注册
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    algorithmStartCalculationDTO.setProjectId(projectId);
                    algorithmStartCalculationDTO.setGroupCode(ContextUtil.getGroupCode());
                    algorithmStartCalculationDTO.setSystemId(system.getId());
                    ProjectDTO projectDTO = midPermissionService.queryProjectDTOByProjectId(projectId);
                    algorithmStartCalculationDTO.setCityCode(projectDTO.getCityCode());
                    algorithmStartCalculationDTO.setMode(algorithmConfig.getMode());
                    algorithmStartCalculationDTO.setHeating_source(algorithmConfig.getHeatingSource());
                    log.info(JSON.toJSONString(algorithmStartCalculationDTO));
                    DmpResult<String> result = algorithmCalculationWebService.logonAlgorithmCalculation(algorithmStartCalculationDTO);
                    if(!result.getResult().equals("success")) {
                    	log.error("算法启动失败 systemID:{}, dto:{}", system.getId(),JSON.toJSONString(algorithmStartCalculationDTO));
                    }
                } catch (Exception e) {
                    log.error("算法启动失败 systemID:{}, dto:{}, e：{}", system.getId(),JSON.toJSONString(algorithmStartCalculationDTO), ExceptionUtil.getStackTrace(e));
                    throw new ApplicationException(ErrorCodeConstants.SYSTEM_ERROR_B0001,
                            LocaleMessageUtil.getMessage("key_start_using_error"));
                }
			}
        }
    }

    /**
     * 调用算法专家经验修改或删除
     * @param basicsConfig
     */
    private void algorithmCalculation(RcBasicsConfig basicsConfig) {
        RcSysLog log = sysLogService.queryLog(basicsConfig.getProjectId(), LogTypeEnum.DEMAND_CHANGE, OperateTypeEnum.EXPERT_RULES, "success");
        // 若已成功注册算法：删除策略后,删除剩余调用算法修改，无剩余调用算法删除
        if (Objects.nonNull(log)) {
            RcBasicsConfig query = new RcBasicsConfig();
            query.setProjectId(basicsConfig.getProjectId());
            query.setSystemId(basicsConfig.getSystemId());
            query.setType(BasicsConfigTypeEnum.EXPERT_RULES);
            List<RcBasicsConfig> basicsConfigs = basicsConfigService.queryListAfterNow(query);
            if (CollectionUtils.isNotEmpty(basicsConfigs)) {
                AlgorithmCalculationUpdateDTO algorithmCalculationUpdateDTO = new AlgorithmCalculationUpdateDTO();
                algorithmCalculationUpdateDTO.setProjectId(basicsConfig.getProjectId());
                algorithmCalculationUpdateDTO.setSystemId(basicsConfig.getSystemId());
                List<ExpertExperienceDTO> expertExperience = Lists.newArrayList();
                basicsConfigs.forEach(config -> {
                    ExpertExperienceDTO experienceDTO = BeanUtil.toBean(config, ExpertExperienceDTO.class).convertDto(config);
                    expertExperience.add(experienceDTO);
                });
                algorithmCalculationUpdateDTO.setExpertExperience(expertExperience);
                algorithmCalculationUpdateDTO.setTempRule();
                algorithmCalculationWebService.updateAlgorithmCalculation(algorithmCalculationUpdateDTO);
            }else {
                AlgorithmCalculationDeleteDTO algorithmCalculationDeleteDTO = new AlgorithmCalculationDeleteDTO();
                algorithmCalculationDeleteDTO.setGroupCode(ContextUtil.getGroupCode());
                algorithmCalculationDeleteDTO.setProjectId(basicsConfig.getProjectId());
                algorithmCalculationDeleteDTO.setSystemId(basicsConfig.getSystemId());
                algorithmCalculationWebService.deleteAlgorithmCalculation(algorithmCalculationDeleteDTO);
            }
        }
    }

    @Override
    public AlgorithmCalculationVO queryLastStartRecord(String projectId) {
        AlgorithmCalculationVO algorithmCalculationVO = new AlgorithmCalculationVO();
        // 查询最新一条操作记录
        RcSysLog log = sysLogService.queryLog(projectId, LogTypeEnum.DEMAND_CHANGE, OperateTypeEnum.EXPERT_RULES,null);
        if (Objects.nonNull(log)){
            //algorithmCalculationVO.setStatus(log.getResultInfo());
            algorithmCalculationVO.setCreateTime(log.getCreateTime());
            algorithmCalculationVO.setCreateUser(log.getCreateUser());
        }
        return algorithmCalculationVO;
    }

    @Override
    public Map<String, Integer> getCooler(String projectId, String systemId) {
        ParameterCheck.isNull(ContextUtil.getGroupCode(), LocaleMessageUtil.getMessage("key_c0659325aa57425499811b4a531f4315"));
        // key: 大/小冷机  value: 数量
        Map<String, Integer> resultMap = new HashMap<>();
        try {
            // 查询系统下冷机分组
            AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), projectId, systemId);
            if (acccacGroup == null) {
                resultMap.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), 0);
                resultMap.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), 0);
                return resultMap;
            }
            resultMap.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), org.apache.commons.collections4.CollectionUtils.isNotEmpty(acccacGroup.getLarge()) ? acccacGroup.getLarge().size() : 0);
            resultMap.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), org.apache.commons.collections4.CollectionUtils.isNotEmpty(acccacGroup.getSmall()) ? acccacGroup.getSmall().size() : 0);
        } catch (Exception e) {
            log.error("设备信息获取失败", e);
            throw new BusinessException(SysResponseCode.FAIL_K0001.getCode(), LocaleMessageUtil.getMessage("key_aaf91f4ecfda4fa6890f21f61f96bae4"));
        }
        return resultMap;
    }

    @Override
    public Boolean checkDate(ExpertRulesDateCheckVO rulesDateCheck) {
        ExpertRulesVO rulesVO = BeanUtil.copyProperties(rulesDateCheck, ExpertRulesVO.class);
        List<RcBasicsConfig> configList = getConfigList(rulesVO);
        return CollectionUtils.isNotEmpty(configList);
    }
}
