package com.persagy.apccontrol.service;

import com.persagy.apccontrol.basic.model.vo.cfg.ExpertRulesVO;
import com.persagy.apccontrol.common.model.vo.AIStrategicQuery;
import com.persagy.apccontrol.common.model.vo.AlgorithmCalculationVO;
import com.persagy.apccontrol.common.model.vo.StartAlgorithmCalculationDTO;
import com.persagy.apccontrol.vo.ExpertRulesDateCheckVO;

import java.util.List;
import java.util.Map;

/**
 * @Description: AI策略生成设置service
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */
public interface IAIStrategicService {

    /**
     * 添加策略
     * @param strategicVO
     */
    void addAIStrategicGenerate(ExpertRulesVO strategicVO);

    /**
     * 查询ai策略列表
     * @param strategicQuery
     * @return
     */
    List<ExpertRulesVO> queryAIStrategicGenerateList(AIStrategicQuery strategicQuery);

    /**
     * 查询ai策略-根据ID查询
     * @param configId
     * @return
     */
    ExpertRulesVO queryAIStrategicGenerate(String configId);

    /**
     * 编辑ai策略
     * @param strategicVO
     */
    void editAIStrategicGenerate(ExpertRulesVO strategicVO);

    /**
     * 删除ai策略
     * @param configId
     */
    void deleteAIStrategicGenerate(String configId);

    /**
     * 启动计算
     */
    void startAlgorithmCalculation(StartAlgorithmCalculationDTO dto);

    /**
     * 启动算法计算-查询上次操作记录
     * @param projectId
     */
    AlgorithmCalculationVO queryLastStartRecord(String projectId);

    /**
     * 获取冷机数量
     *
     * @param projectId
     * @return
     */
    Map<String, Integer> getCooler(String projectId, String systemId);

    Boolean checkDate(ExpertRulesDateCheckVO rulesDateCheck);
}
