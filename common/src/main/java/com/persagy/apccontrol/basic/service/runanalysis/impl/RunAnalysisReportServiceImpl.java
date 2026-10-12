package com.persagy.apccontrol.basic.service.runanalysis.impl;

import cn.hutool.core.date.DateUtil;
import com.persagy.apccontrol.basic.convert.RunAnalysisReportConvert;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportChapterVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportRemarkVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportVO;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisReportService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportChapterDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import com.persagy.apccontrol.basic.structure.runanalysis.service.RunAnalysisReportDomainService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportServiceImpl implements RunAnalysisReportService, LogComponent {

    private final RunAnalysisReportDomainService runAnalysisReportDomainService;

    private final RunAnalysisReportConvert runAnalysisReportConvert;
    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240802.0
     */
    @Override
    public String logTag() {
        return "运行分析报告";
    }

    @Override
    public Boolean build(RunAnalysisReportBuildParam param) {
        return runAnalysisReportDomainService.build(param);
    }

    @Override
    public Boolean buildAuto() {
        return runAnalysisReportDomainService.buildAuto();
    }

    @Override
    public List<RunAnalysisReportVO> getList(RunAnalysisReportListParam param) {
        List<RunAnalysisReport> reportList = runAnalysisReportDomainService.getList(param);
        
        if (reportList == null || reportList.isEmpty()) {
            return Collections.emptyList();
        }

        // 创建两个 Map，分别存储正在构建中的报告和所有报告
        Map<LocalDate, Boolean> buildingMap = new HashMap<>();
        Map<LocalDate, List<RunAnalysisReport>> allMap = new HashMap<>();
                
        // 获取当前时间，用于计算超时
        LocalDateTime now = DateUtils.now();
                
        for (RunAnalysisReport report : reportList) {
            if (report != null) {  // 添加空值检查
                LocalDate reportDate = report.getReportDate();
                if (reportDate != null) {  // 添加日期空值检查
                    // 根据状态分别归类
                    if (RunAnalysisReportStatusEnum.BUILDING.equals(report.getReportStatus())) {
                        // 检查是否超时（超过 1 小时）
                        if (isTimeout(report.getUpdateTime(), now)) {
                            // 如果超时，将状态改为失败
                            report.setReportStatus(RunAnalysisReportStatusEnum.FAILURE);
                        } else {
                            // 未超时，标记为正在构建中
                            buildingMap.put(reportDate, true);
                        }
                    }
                    allMap.computeIfAbsent(reportDate, k -> new ArrayList<>()).add(report);
                }
            }
        }
        
        List<RunAnalysisReport> groupList = new ArrayList<>();
        for (Map.Entry<LocalDate, List<RunAnalysisReport>> entry : allMap.entrySet()) {
            List<RunAnalysisReport> reports = entry.getValue();
            
            // 添加空值检查，确保列表不为空
            if (reports != null && !reports.isEmpty()) {
                RunAnalysisReport runAnalysisReport = reports.get(0);
                
                // 如果该日期的报告正在构建中，则更新状态
                if (buildingMap.containsKey(entry.getKey())) {
                    runAnalysisReport.setReportStatus(RunAnalysisReportStatusEnum.BUILDING);
                }
                groupList.add(runAnalysisReport);
            }
        }
        //将groupList 按照reportDate排序
        groupList.sort(Comparator.comparing(RunAnalysisReport::getReportDate).reversed());
        
        return runAnalysisReportConvert.toRunAnalysisReportVOList(groupList);
    }

    @Override
    public List<RunAnalysisReportChapterVO> getChapterTree(RunAnalysisReportQueryParam param) {
        List<RunAnalysisReportChapterDto> chapterList = runAnalysisReportDomainService.getChapterList(param);
        
        // 转换DTO为VO
        List<RunAnalysisReportChapterVO> chapterVOList = runAnalysisReportConvert.toRunAnalysisReportChapterVOList(chapterList);
        
        // 构建树形结构
        Map<String, RunAnalysisReportChapterVO> chapterMap = new HashMap<>();
        List<RunAnalysisReportChapterVO> rootChapters = new ArrayList<>();
        
        // 首先将所有章节放入map中，便于后续构建父子关系
        for (RunAnalysisReportChapterVO chapter : chapterVOList) {
            chapterMap.put(chapter.getCode(), chapter);
        }
        
        // 构建父子关系
        for (RunAnalysisReportChapterVO chapter : chapterVOList) {
            String parentCode = chapter.getParentCode();
            
            if (parentCode == null || parentCode.isEmpty()) {
                // 如果没有父级，则为根节点
                rootChapters.add(chapter);
            } else {
                // 如果有父级，则添加到父节点的children列表中
                RunAnalysisReportChapterVO parentChapter = chapterMap.get(parentCode);
                if (parentChapter != null) {
                    if (parentChapter.getChildren() == null) {
                        parentChapter.setChildren(new ArrayList<>());
                    }
                    parentChapter.getChildren().add(chapter);
                }
            }
        }

        //遍历rootChapters每一层级，按sort排序
        for (RunAnalysisReportChapterVO chapter : rootChapters) {
            sortChapterRecursively(chapter);
        }
        rootChapters.sort((a, b) -> a.getSort() - b.getSort());
        return rootChapters;
    }

    @Override
    public List<RunAnalysisReportVO> getDetail(List<Long> ids) {
        List<RunAnalysisReport> reportList = runAnalysisReportDomainService.getDetail(ids);
        return runAnalysisReportConvert.toRunAnalysisReportVOList(reportList);
    }

    @Override
    public Boolean saveRemark(List<RunAnalysisReportRemarkParam> param) {
        return runAnalysisReportDomainService.saveRemark(param);
    }

    @Override
    public List<RunAnalysisReportRemarkVO> listRemark(List<Long> reportIdList) {
        List<RunAnalysisReportRemark> remarkList = runAnalysisReportDomainService.getRemarkList(reportIdList);
        if (remarkList != null && !remarkList.isEmpty()) {
            return runAnalysisReportConvert.toRunAnalysisReportRemarkVOList(remarkList);
        }
        return Collections.emptyList();
    }

    @Override
    public void downloadReport(RunAnalysisReportDownloadParam param, HttpServletResponse response) throws Exception {
        runAnalysisReportDomainService.downloadReport(param,response);
    }

    /**
     * 递归排序章节及其子章节
     * @param chapter 当前章节
     */
    private void sortChapterRecursively(RunAnalysisReportChapterVO chapter) {
        if (chapter.getChildren() != null && !chapter.getChildren().isEmpty()) {
            // 对当前层级的子章节进行排序
            chapter.getChildren().sort((a, b) -> a.getSort() - b.getSort());
            
            // 递归排序每个子章节的下一层级
            for (RunAnalysisReportChapterVO child : chapter.getChildren()) {
                sortChapterRecursively(child);
            }
        }
    }

    /**
     * 检查报告构建是否超时（超过 1 小时）
     *
     * @param updateTime 更新时间
     * @param now 当前时间
     * @return 是否超时
     */
    private boolean isTimeout(LocalDateTime updateTime, LocalDateTime now) {
        if (updateTime == null) {
            return false;
        }
        long hoursBetween = ChronoUnit.HOURS.between(updateTime, now);
        return hoursBetween >= 1;
    }
}
