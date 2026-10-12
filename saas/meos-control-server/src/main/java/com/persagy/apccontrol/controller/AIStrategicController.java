package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.model.vo.cfg.ExpertRulesVO;
import com.persagy.apccontrol.common.model.vo.*;
import com.persagy.apccontrol.service.IAIStrategicService;
import com.persagy.apccontrol.vo.ExpertRulesDateCheckVO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI策略生成设置
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Tag(name = "AI策略生成设置")
@Validated
@RestController
@RequestMapping("/aiStrategic")
@Slf4j
public class AIStrategicController {

    @Resource
    IAIStrategicService strategicService;

    /**
     * 添加策略
     * @param expertRulesVO
     * @return
     */
    @Operation(summary = "添加策略")
    @PostMapping("/addAIStrategicGenerate")
    public R<Boolean> addAIStrategicGenerate(@RequestBody @Valid ExpertRulesVO expertRulesVO) {
        strategicService.addAIStrategicGenerate(expertRulesVO);
        return R.success(true);
    }

    /**
     * 校验时间段是否已存在,true为存在，false为不存在
     *
     * @param rulesDateCheck
     * @return
     */
    @Operation(summary = "校验时间段是否已存在,true为存在，false为不存在")
    @PostMapping("/checkDate")
    public R<Boolean> checkDate(@RequestBody @Valid ExpertRulesDateCheckVO rulesDateCheck) {
        return R.success(strategicService.checkDate(rulesDateCheck));
    }

    /**
     * 获取冷机数量
     *
     * @param projectVO
     * @return
     */
    @Operation(summary = "获取冷机数量")
    @PostMapping("/getCooler")
    public R<Map<String, Integer>> getCooler(@RequestBody @Valid ProjectSystemVO projectVO) {
        return R.success(strategicService.getCooler(projectVO.getProjectId(), projectVO.getSystemId()));
    }

    /**
     * 查询ai策略列表
     * @param strategicQuery
     * @return
     */
    @Operation(summary = "查询ai策略列表")
    @PostMapping("/queryAIStrategicGenerateList")
    public R<List<ExpertRulesVO>> queryAIStrategicGenerateList(@RequestBody @Valid AIStrategicQuery strategicQuery){
        return R.success(strategicService.queryAIStrategicGenerateList(strategicQuery));
    }

    /**
     * 查询ai策略-根据ID查询
     * @param strategicBaseDTO
     * @return
     */
    @Operation(summary = "查询ai策略-根据ID查询")
    @PostMapping("/queryAIStrategicGenerate")
    public R<ExpertRulesVO> queryAIStrategicGenerate(@RequestBody @Valid AIStrategicBaseDTO strategicBaseDTO){
        return R.success(strategicService.queryAIStrategicGenerate(strategicBaseDTO.getConfigId()));
    }


    /**
     * 编辑ai策略
     * @param expertRulesVO
     * @return
     */
    @Operation(summary = "编辑ai策略")
    @PostMapping("/editAIStrategicGenerate")
    public R<Boolean> editAIStrategicGenerate(@RequestBody @Valid ExpertRulesVO expertRulesVO){
        strategicService.editAIStrategicGenerate(expertRulesVO);
        return R.success(true);
    }

    /**
     * 删除ai策略
     * @param strategicBaseDTO
     * @return
     */
    @Operation(summary = "删除ai策略-根据ID删除")
    @PostMapping("/deleteAIStrategicGenerate")
    public R<Boolean> deleteAIStrategicGenerate(@RequestBody @Valid AIStrategicBaseDTO strategicBaseDTO){
        strategicService.deleteAIStrategicGenerate(strategicBaseDTO.getConfigId());
        return R.success(true);
    }

    /**
     * 启动算法计算
     * @param dto
     * @return
     */
    @Operation(summary = "启动算法计算")
    @PostMapping("/startAlgorithmCalculation")
    public R<Boolean> startAlgorithmCalculation(@RequestBody @Valid StartAlgorithmCalculationDTO dto) {
        strategicService.startAlgorithmCalculation(dto);
        return R.success(true);
    }

    /**
     * 启动算法计算-查询上次操作记录
     * @param projectVO
     * @return
     */
    @Operation(summary = "查询上次操作记录")
    @PostMapping("/queryLastStartRecord")
    public R<AlgorithmCalculationVO> queryLastStartRecord(@RequestBody @Valid ProjectVO projectVO){
        return R.success(strategicService.queryLastStartRecord(projectVO.getProjectId()));
    }
}
