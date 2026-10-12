package com.persagy.apccontrol.basic.structure.runanalysis.util;

import cn.hutool.core.text.StrPool;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.persagy.apccontrol.basic.model.dto.runanalysis.download.*;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportDownloadParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 运行分析报告CSV导出工具类
 * 使用EasyExcel实现分批写入，避免性能问题
 *
 * @author: system
 * @create: 2026-03-18
 */
@Slf4j
public class RunAnalysisReportCsvExportUtil {



    /**
     * 导出CSV数据到HttpServletResponse（自定义批次大小）
     *
     * @param response   HttpServletResponse对象
     * @param param    下载参数
     * @param runAnalysisReportDownloadDTO   数据集合
     */
    public static void exportCsv(HttpServletResponse response, RunAnalysisReportDownloadParam param, RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO) throws IOException {
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        //utf-8 编码文件名
        String fileName = URLEncoder.encode(runAnalysisReportDownloadDTO.getProjectBasicInfo().getProjectName() + param.getReportDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "运行复盘日报", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=" + fileName + ".csv");

        ExcelWriter excelWriter = null;
        try {
            // 创建 Writer，不缓存所有数据
            excelWriter = EasyExcel.write(response.getOutputStream())
                    .excelType(ExcelTypeEnum.CSV)
                    .needHead(false)
                    .build();

            WriteSheet sheet = EasyExcel.writerSheet(fileName).build();

            // ========== 第1部分：项目基础信息（小数据量，直接写） ==========
            writeProjectInfo(excelWriter, sheet,runAnalysisReportDownloadDTO.getProjectBasicInfo());

            // ========== 第2部分：日汇总数据（小数据量，直接写） ==========
            writeDailySummaryData(excelWriter, sheet, runAnalysisReportDownloadDTO.getDailySummaryData());

            // ========== 第3部分：3.3.3 冷站日汇总数据） ==========
            writeCoolingStationDailySummary(excelWriter, sheet, runAnalysisReportDownloadDTO.getCoolingStationDailySummaryList());

            // ========== 第4部分：3.3.4 冷站参考相似日能耗数据） ==========
            writeCoolingStationSimilarDay(excelWriter, sheet, runAnalysisReportDownloadDTO.getCoolingStationSimilarDayList());

            // ========== 第5部分：3.3.5 逐15min冷站数据 ==========
            writeCoolingStation15MinData(excelWriter, sheet, runAnalysisReportDownloadDTO.getCoolingStation15MinDataList());

            // ========== 第 6 部分：3.3.6 逐 15min 冷站服务空间的温度状态 ==========
            writeServiceSpaceTempStatus(excelWriter, sheet, runAnalysisReportDownloadDTO.getServiceSpaceTempStatusList());
            
            // ========== 3.3.7 逐 15min 冷机数据 ==========
            writeChiller15MinData(excelWriter, sheet, runAnalysisReportDownloadDTO.getChiller15MinDataList());

            // ========== 3.3.8 逐 15min 冷却塔风机数据 ==========
            writeCoolingTowerFan15MinData(excelWriter, sheet, runAnalysisReportDownloadDTO.getCoolingTowerFan15MinDataList());

            // ========== 3.3.9 逐 15min 室外天气数据 ==========
            writeOutdoorWeatherData(excelWriter, sheet, runAnalysisReportDownloadDTO.getOutdoorWeatherDataList());



        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    public static void  writeProjectInfo(ExcelWriter writer, WriteSheet sheet, ProjectBasicInfoDTO projectBasicInfo) {
        List<List<Object>> rows = Lists.newArrayList();
        rows.add(createRow("项目基础信息"));
        rows.add(createRow("指标名称", "值"));
        rows.add(createRow("项目id", projectBasicInfo.getProjectId()));
        rows.add(createRow("项目名称", projectBasicInfo.getProjectName()));
        rows.add(createRow("省份", projectBasicInfo.getProvince()));
        rows.add(createRow("城市", projectBasicInfo.getCity()));
        rows.add(createRow("市内区域", projectBasicInfo.getUrbanZone()));
       //生成一个空行
        rows.add(createRow());

        writer.write(rows, sheet);
        // rows 可以立即被 GC 回收
    }

    public static List<Object> createRow(Object... values) {
        List<Object> row = Lists.newArrayList();
        for (Object v : values) {
            row.add(v == null ? "" : v);
        }
        return row;
    }

    /**
     * 格式化带单位的数值
     * 如果value为null，返回空字符串；否则返回数值+单位
     *
     * @param value 数值
     * @param unit  单位
     * @return 格式化后的字符串
     */
    private static String formatWithUnit(BigDecimal value, String unit) {
        if (value == null) {
            return "";
        }
        return value + unit;
    }

    /**
     * 写入日汇总数据到CSV
     *
     * @param writer       ExcelWriter对象
     * @param sheet        WriteSheet对象
     * @param dailySummary 日汇总数据DTO
     */
    public static void writeDailySummaryData(ExcelWriter writer, WriteSheet sheet, DailySummaryDataDTO dailySummary) {
        List<List<Object>> rows = Lists.newArrayList();
        // 标题行
        rows.add(createRow("日汇总数据"));
        // 表头行
        rows.add(createRow("指标名称", "值"));

        // 日期
        rows.add(createRow("日期", Optional.ofNullable(dailySummary.getDate()).orElse(String.valueOf(StrPool.C_SPACE))));
        // 营业时间段
        rows.add(createRow("营业时间段", Optional.ofNullable(dailySummary.getBusinessTimeRange()).orElse(String.valueOf(StrPool.C_SPACE))));

        // 全天数据
        rows.add(createRow("全天-室外平均温度", formatWithUnit(dailySummary.getAllDayAvgTemp(), "℃")));
        rows.add(createRow("全天-室外最高温度", formatWithUnit(dailySummary.getAllDayMaxTemp(), "℃")));
        rows.add(createRow("全天-室外最低温度", formatWithUnit(dailySummary.getAllDayMinTemp(), "℃")));
        rows.add(createRow("全天-室外平均湿度", formatWithUnit(dailySummary.getAllDayAvgHumidity(), "%RH")));
        rows.add(createRow("全天-室外最高湿度", formatWithUnit(dailySummary.getAllDayMaxHumidity(), "%RH")));
        rows.add(createRow("全天-室外最低湿度", formatWithUnit(dailySummary.getAllDayMinHumidity(), "%RH")));
        rows.add(createRow("全天-室外平均湿球温度", formatWithUnit(dailySummary.getAllDayAvgWetBulbTemp(), "℃")));
        rows.add(createRow("全天-室外平均焓值", formatWithUnit(dailySummary.getAllDayAvgEnthalpy(), "kJ/kg.dra")));

        // 营业时段内数据
        rows.add(createRow("营业时段内-室外平均温度", formatWithUnit(dailySummary.getBusinessHoursAvgTemp(), "℃")));
        rows.add(createRow("营业时段内-室外温度最高值", formatWithUnit(dailySummary.getBusinessHoursMaxTemp(), "℃")));
        rows.add(createRow("营业时段内-室外温度最低值", formatWithUnit(dailySummary.getBusinessHoursMinTemp(), "℃")));
        rows.add(createRow("营业时段内-室外平均湿度", formatWithUnit(dailySummary.getBusinessHoursAvgHumidity(), "%RH")));
        rows.add(createRow("营业时段内-室外湿度最高值", formatWithUnit(dailySummary.getBusinessHoursMaxHumidity(), "%RH")));
        rows.add(createRow("营业时段内-室外湿度最低值", formatWithUnit(dailySummary.getBusinessHoursMinHumidity(), "%RH")));
        rows.add(createRow("营业时段内-室外平均湿球温度", formatWithUnit(dailySummary.getBusinessHoursAvgWetBulbTemp(), "℃")));
        rows.add(createRow("营业时段内-室外平均焓值", formatWithUnit(dailySummary.getBusinessHoursAvgEnthalpy(), "kJ/kg.dra")));

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入冷站日汇总数据到CSV
     *
     * @param writer       ExcelWriter对象
     * @param sheet        WriteSheet对象
     * @param coolingStationDailySummaryList 冷站日汇总数据DTO
     */
    public static void writeCoolingStationDailySummary(ExcelWriter writer, WriteSheet sheet, List<CoolingStationDailySummaryDTO> coolingStationDailySummaryList) {
        List<List<Object>> rows = Lists.newArrayList();
        // 标题行
        rows.add(createRow("冷站日汇总数据"));
        // 表头行
        rows.add(createRow("系统名称", "温度满足率", "实际能耗", "参考能耗-基准年同日实际能耗", "参考能耗-基准年同日日期"));

        if(CollectionUtils.isEmpty(coolingStationDailySummaryList)){
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }
        coolingStationDailySummaryList.forEach(dailySummary -> {
            // 日期
            rows.add(createRow(
                    Optional.ofNullable(dailySummary.getSystemName()).orElse(String.valueOf(StrPool.C_SPACE)),
                    formatWithUnit((dailySummary.getTemperatureSatisfactionRate()), "%"),
                    formatWithUnit((dailySummary.getActualEnergyConsumption()), "kWh") ,
                    formatWithUnit((dailySummary.getReferenceEnergyConsumption()), "kWh") ,
                    Optional.ofNullable(dailySummary.getReferenceDate()).orElse(String.valueOf(StrPool.C_SPACE))
                )
            );
        });
        // 空行分隔
        rows.add(createRow());
        writer.write(rows, sheet);
    }

    /**
     * 写入冷站参考相似日能耗数据到CSV
     *
     * @param writer                          ExcelWriter对象
     * @param sheet                           WriteSheet对象
     * @param coolingStationSimilarDayList 冷站参考相似日能耗数据列表
     */
    public static void writeCoolingStationSimilarDay(ExcelWriter writer, WriteSheet sheet, List<CoolingStationSimilarDayDTO> coolingStationSimilarDayList) {
        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("冷站参考相似日能耗数据"));

        // 表头行
        rows.add(createRow("系统名称", "日期", "参考能耗值", "全天-室外平均温度", "全天-室外最高温度", "全天-室外平均湿度"));

        if (CollectionUtils.isEmpty(coolingStationSimilarDayList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        coolingStationSimilarDayList.forEach(similarDay -> {
            rows.add(createRow(
                    Optional.ofNullable(similarDay.getSystemName()).orElse(""),
                    Optional.ofNullable(similarDay.getDate()).orElse(""),
                    formatWithUnit(similarDay.getReferenceEnergyValue(), "kWh"),
                    formatWithUnit(similarDay.getAllDayAvgTemp(), "℃"),
                    formatWithUnit(similarDay.getAllDayMaxTemp(), "℃"),
                    formatWithUnit(similarDay.getAllDayAvgHumidity(), "%RH")
                    )
                );
        });

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入逐15min冷站数据到CSV
     *
     * @param writer ExcelWriter对象
     * @param sheet WriteSheet对象
     * @param coolingStation15MinDataList 逐15min冷站数据列表
     */
    public static void writeCoolingStation15MinData(
            ExcelWriter writer,
            WriteSheet sheet,
            List<CoolingStation15MinDataDTO> coolingStation15MinDataList) {

        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("逐15min冷站数据"));

        // 表头行
        rows.add(createRow(
                "时间点",
                "系统名称",
                "冷冻水供回水温差设定",
                "冷冻水供回水温差",
                "冷冻水一次侧供水干管温度壳配置下限值",
                "冷冻水一次侧供水干管温度",
                "冷冻水一次侧回水干管温度",
                "冷却塔出水温度设定",
                "冷机(冷却水)进水干管温度",
                "冷机(冷却水)出水干管温度",
                "冷却水进出水温差设定",
                "冷却水进出水温差",
                "免费冷开关设定",
                "供冷开关设定"
        ));

        if (CollectionUtils.isEmpty(coolingStation15MinDataList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        // 数据行
        for (CoolingStation15MinDataDTO data : coolingStation15MinDataList) {
            rows.add(createRow(
                    Optional.ofNullable(data.getTimePoint()).orElse(""),
                    Optional.ofNullable(data.getSystemName()).orElse(""),
                    formatWithUnit(data.getChillWaterDeltaTempSet(), "℃"),
                    formatWithUnit(data.getChillWaterDeltaTemp(), "℃"),
                    formatWithUnit(data.getPrimaryChillWaterSupplyTempLowerLimit(), "℃"),
                    formatWithUnit(data.getPrimaryChillWaterMainSupplyTemp(), "℃"),
                    formatWithUnit(data.getPrimaryChillWaterMainReturnTemp(), "℃"),
                    formatWithUnit(data.getCoolingTowerOutTempSet(), "℃"),
                    formatWithUnit(data.getCoolWaterMainReturnTemp(), "℃"),
                    formatWithUnit(data.getCoolWaterMainSupplyTemp(), "℃"),
                    formatWithUnit(data.getCoolWaterDeltaTempSet(), "℃"),
                    formatWithUnit(data.getCoolWaterDeltaTemp(), "℃"),
                    Optional.ofNullable(data.getFreeCoolingSwitchSet()).orElse(""),
                    Optional.ofNullable(data.getCoolingSupplySwitchSet()).orElse("")));
        }

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入逐15min冷站服务空间的温度状态到CSV
     *
     * @param writer                         ExcelWriter对象
     * @param sheet                          WriteSheet对象
     * @param serviceSpaceTempStatusList 逐15min冷站服务空间的温度状态列表
     */
    public static void writeServiceSpaceTempStatus(
            ExcelWriter writer,
            WriteSheet sheet,
            List<ServiceSpaceTempStatusDTO> serviceSpaceTempStatusList) {

        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("逐15min冷站服务空间的温度状态"));

        // 表头行
        rows.add(createRow(
                "时间点",
                "系统名称",
                "温度满足率",
                "过冷空间数量",
                "偏冷空间数量",
                "舒适空间数量",
                "偏热空间数量",
                "过热空间数量",
                "无数据空间数量",
                "总空间数量",
                "过冷空间数量占比",
                "偏冷空间数量占比",
                "舒适空间数量占比",
                "偏热空间数量占比",
                "过热空间数量占比",
                "无数据空间数量占比"
        ));

        if (CollectionUtils.isEmpty(serviceSpaceTempStatusList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        // 数据行
        for (ServiceSpaceTempStatusDTO data : serviceSpaceTempStatusList) {
            rows.add(createRow(
                    Optional.ofNullable(data.getTimePoint()).orElse(""),
                    Optional.ofNullable(data.getSystemName()).orElse(""),
                    formatWithUnit(data.getTemperatureSatisfactionRate(), "%"),
                    Optional.ofNullable(data.getTooColdSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getSlightlyColdSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getComfortableSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getSlightlyHotSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getTooHotSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getNoDataSpaceCount()).map(e -> e +"个").orElse(""),
                    Optional.ofNullable(data.getTotalSpaceCount()).map(e -> e +"个").orElse(""),
                    formatWithUnit(data.getTooColdSpacePercentage(), "%"),
                    formatWithUnit(data.getSlightlyColdSpacePercentage(), "%"),
                    formatWithUnit(data.getComfortableSpacePercentage(), "%"),
                    formatWithUnit(data.getSlightlyHotSpacePercentage(), "%"),
                    formatWithUnit(data.getTooHotSpacePercentage(), "%"),
                    formatWithUnit(data.getNoDataSpacePercentage(), "%")
            ));
        }

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入逐 15min 冷机数据到 CSV
     *
     * @param writer ExcelWriter 对象
     * @param sheet WriteSheet 对象
     * @param chiller15MinDataList 逐 15min 冷机数据列表
     */
    public static void writeChiller15MinData(
            ExcelWriter writer,
            WriteSheet sheet,
            List<Chiller15MinDataDTO> chiller15MinDataList) {

        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("逐 15min 冷机数据"));

        // 表头行
        rows.add(createRow(
                "时间点",
                "系统名称",
                "设备名称",
                "运行状态",
                "运行负载率",
                "设备大小机类型"
        ));

        if (CollectionUtils.isEmpty(chiller15MinDataList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        // 数据行
        for (Chiller15MinDataDTO data : chiller15MinDataList) {
            rows.add(createRow(
                    Optional.ofNullable(data.getTimePoint()).orElse(""),
                    Optional.ofNullable(data.getSystemName()).orElse(""),
                    Optional.ofNullable(data.getEquipmentName()).orElse(""),
                    Optional.ofNullable(data.getRunStatus()).orElse(""),
                    formatWithUnit(data.getIPLR(), "%"),
                    Optional.ofNullable(data.getEquipmentSizeType()).orElse("")));
        }

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入逐 15min 冷却塔风机数据到 CSV
     *
     * @param writer ExcelWriter 对象
     * @param sheet WriteSheet 对象
     * @param coolingTowerFan15MinDataList 逐 15min 冷却塔风机数据列表
     */
    public static void writeCoolingTowerFan15MinData(
            ExcelWriter writer,
            WriteSheet sheet,
            List<CoolingTowerFan15MinDataDTO> coolingTowerFan15MinDataList) {

        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("逐 15min 冷却塔风机数据"));

        // 表头行
        rows.add(createRow(
                "时间点",
                "系统名称",
                "设备名称",
                "运行状态",
                "运行频率",
                "运行频率设定",
                "运行档位",
                "运行档位设定"
        ));

        if (CollectionUtils.isEmpty(coolingTowerFan15MinDataList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        // 数据行
        for (CoolingTowerFan15MinDataDTO data : coolingTowerFan15MinDataList) {
            rows.add(createRow(
                    Optional.ofNullable(data.getTimePoint()).orElse(""),
                    Optional.ofNullable(data.getSystemName()).orElse(""),
                    Optional.ofNullable(data.getEquipmentName()).orElse(""),
                    Optional.ofNullable(data.getRunStatus()).orElse(""),
                    formatWithUnit(data.getFreq(), "Hz"),
                    formatWithUnit(data.getEquipFreqSet(), "Hz"),
                    Optional.ofNullable(data.getFanGear()).orElse(""),
                    Optional.ofNullable(data.getEquipGearSet()).orElse("")));
        }

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 写入逐 15min 室外天气数据到 CSV
     *
     * @param writer ExcelWriter 对象
     * @param sheet WriteSheet 对象
     * @param outdoorWeatherDataList 逐 15min 室外天气数据列表
     */
    public static void writeOutdoorWeatherData(
            ExcelWriter writer,
            WriteSheet sheet,
            List<OutdoorWeatherDataDTO> outdoorWeatherDataList) {

        List<List<Object>> rows = Lists.newArrayList();

        // 标题行
        rows.add(createRow("逐 15min 室外天气数据"));

        // 表头行
        rows.add(createRow(
                "时间点",
                "温度",
                "湿度",
                "湿球温度",
                "焓值"
        ));

        if (CollectionUtils.isEmpty(outdoorWeatherDataList)) {
            // 空行分隔
            rows.add(createRow());
            writer.write(rows, sheet);
            return;
        }

        // 数据行
        for (OutdoorWeatherDataDTO data : outdoorWeatherDataList) {
            rows.add(createRow(
                    Optional.ofNullable(data.getTimePoint()).orElse(""),
                    formatWithUnit(data.getTemperature(), "℃"),
                    formatWithUnit(data.getHumidity(), "%RH"),
                    formatWithUnit(data.getWetBulbTemp(), "℃"),
                    formatWithUnit(data.getEnthalpy(), "kJ/kg.dra")));
        }

        // 空行分隔
        rows.add(createRow());

        writer.write(rows, sheet);
    }

    /**
     * 构建 CSV 字符串（公共方法，供导出和构建报告复用）
     *
     * @param runAnalysisReportDownloadDTO 数据集合
     * @return CSV 字符串
     */
    public static String buildCsvString(RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO) {
        StringBuilder csvBuilder = new StringBuilder();

        // ========== 第 1 部分：项目基础信息 ==========
        buildProjectInfoCsv(csvBuilder, runAnalysisReportDownloadDTO.getProjectBasicInfo());

        // ========== 第 2 部分：日汇总数据 ==========
        buildDailySummaryDataCsv(csvBuilder, runAnalysisReportDownloadDTO.getDailySummaryData());

        // ========== 第 3 部分：冷站日汇总数据 ==========
        buildCoolingStationDailySummaryCsv(csvBuilder, runAnalysisReportDownloadDTO.getCoolingStationDailySummaryList());

        // ========== 第 4 部分：冷站参考相似日能耗数据 ==========
        buildCoolingStationSimilarDayCsv(csvBuilder, runAnalysisReportDownloadDTO.getCoolingStationSimilarDayList());

        // ========== 第 5 部分：逐 15min 冷站数据 ==========
        buildCoolingStation15MinDataCsv(csvBuilder, runAnalysisReportDownloadDTO.getCoolingStation15MinDataList());

        // ========== 第 6 部分：逐 15min 冷站服务空间的温度状态 ==========
        buildServiceSpaceTempStatusCsv(csvBuilder, runAnalysisReportDownloadDTO.getServiceSpaceTempStatusList());

        // ========== 第 7 部分：逐 15min 冷机数据 ==========
        buildChiller15MinDataCsv(csvBuilder, runAnalysisReportDownloadDTO.getChiller15MinDataList());

        // ========== 第 8 部分：逐 15min 冷却塔风机数据 ==========
        buildCoolingTowerFan15MinDataCsv(csvBuilder, runAnalysisReportDownloadDTO.getCoolingTowerFan15MinDataList());

        // ========== 第 9 部分：逐 15min 室外天气数据 ==========
        buildOutdoorWeatherDataCsv(csvBuilder, runAnalysisReportDownloadDTO.getOutdoorWeatherDataList());

        return csvBuilder.toString();
    }

    /**
     * 构建项目基础信息 CSV 字符串
     */
    private static void buildProjectInfoCsv(StringBuilder csvBuilder, ProjectBasicInfoDTO projectBasicInfo) {
        csvBuilder.append("项目基础信息\r\n");
        csvBuilder.append("指标名称，值\r\n");
        csvBuilder.append("项目 id，").append(Optional.ofNullable(projectBasicInfo.getProjectId()).orElse("")).append("\r\n");
        csvBuilder.append("项目名称，").append(Optional.ofNullable(projectBasicInfo.getProjectName()).orElse("")).append("\r\n");
        csvBuilder.append("省份，").append(Optional.ofNullable(projectBasicInfo.getProvince()).orElse("")).append("\r\n");
        csvBuilder.append("城市，").append(Optional.ofNullable(projectBasicInfo.getCity()).orElse("")).append("\r\n");
        csvBuilder.append("市内区域，").append(Optional.ofNullable(projectBasicInfo.getUrbanZone()).orElse("")).append("\r\n");
        csvBuilder.append("\r\n");
    }

    /**
     * 构建日汇总数据 CSV 字符串
     */
    private static void buildDailySummaryDataCsv(StringBuilder csvBuilder, DailySummaryDataDTO dailySummary) {
        csvBuilder.append("日汇总数据\r\n");
        csvBuilder.append("指标名称，值\r\n");
        csvBuilder.append("日期，").append(Optional.ofNullable(dailySummary.getDate()).orElse(String.valueOf(StrPool.C_SPACE))).append("\r\n");
        csvBuilder.append("营业时间段，").append(Optional.ofNullable(dailySummary.getBusinessTimeRange()).orElse(String.valueOf(StrPool.C_SPACE))).append("\r\n");
        csvBuilder.append("全天 - 室外平均温度，").append(formatWithUnit(dailySummary.getAllDayAvgTemp(), "℃")).append("\r\n");
        csvBuilder.append("全天 - 室外最高温度，").append(formatWithUnit(dailySummary.getAllDayMaxTemp(), "℃")).append("\r\n");
        csvBuilder.append("全天 - 室外最低温度，").append(formatWithUnit(dailySummary.getAllDayMinTemp(), "℃")).append("\r\n");
        csvBuilder.append("全天 - 室外平均湿度，").append(formatWithUnit(dailySummary.getAllDayAvgHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("全天 - 室外最高湿度，").append(formatWithUnit(dailySummary.getAllDayMaxHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("全天 - 室外最低湿度，").append(formatWithUnit(dailySummary.getAllDayMinHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("全天 - 室外平均湿球温度，").append(formatWithUnit(dailySummary.getAllDayAvgWetBulbTemp(), "℃")).append("\r\n");
        csvBuilder.append("全天 - 室外平均焓值，").append(formatWithUnit(dailySummary.getAllDayAvgEnthalpy(), "kJ/kg.dra")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外平均温度，").append(formatWithUnit(dailySummary.getBusinessHoursAvgTemp(), "℃")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外温度最高值，").append(formatWithUnit(dailySummary.getBusinessHoursMaxTemp(), "℃")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外温度最低值，").append(formatWithUnit(dailySummary.getBusinessHoursMinTemp(), "℃")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外平均湿度，").append(formatWithUnit(dailySummary.getBusinessHoursAvgHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外湿度最高值，").append(formatWithUnit(dailySummary.getBusinessHoursMaxHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外湿度最低值，").append(formatWithUnit(dailySummary.getBusinessHoursMinHumidity(), "%RH")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外平均湿球温度，").append(formatWithUnit(dailySummary.getBusinessHoursAvgWetBulbTemp(), "℃")).append("\r\n");
        csvBuilder.append("营业时段内 - 室外平均焓值，").append(formatWithUnit(dailySummary.getBusinessHoursAvgEnthalpy(), "kJ/kg.dra")).append("\r\n");
        csvBuilder.append("\r\n");
    }

    /**
     * 构建冷站日汇总数据 CSV 字符串
     */
    private static void buildCoolingStationDailySummaryCsv(StringBuilder csvBuilder, List<CoolingStationDailySummaryDTO> coolingStationDailySummaryList) {
        csvBuilder.append("冷站日汇总数据\r\n");
        csvBuilder.append("系统名称，温度满足率，实际能耗，参考能耗 - 基准年同日实际能耗，参考能耗 - 基准年同日日期\r\n");
        
        if (CollectionUtils.isEmpty(coolingStationDailySummaryList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (CoolingStationDailySummaryDTO dailySummary : coolingStationDailySummaryList) {
            csvBuilder.append(Optional.ofNullable(dailySummary.getSystemName()).orElse(""))
                    .append(",").append(formatWithUnit((dailySummary.getTemperatureSatisfactionRate()), "%"))
                    .append(",").append(formatWithUnit((dailySummary.getActualEnergyConsumption()), "kWh"))
                    .append(",").append(formatWithUnit((dailySummary.getReferenceEnergyConsumption()), "kWh"))
                    .append(",").append(Optional.ofNullable(dailySummary.getReferenceDate()).orElse(String.valueOf(StrPool.C_SPACE)))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建冷站参考相似日能耗数据 CSV 字符串
     */
    private static void buildCoolingStationSimilarDayCsv(StringBuilder csvBuilder, List<CoolingStationSimilarDayDTO> coolingStationSimilarDayList) {
        csvBuilder.append("冷站参考相似日能耗数据\r\n");
        csvBuilder.append("系统名称，日期，参考能耗值，全天 - 室外平均温度，全天 - 室外最高温度，全天 - 室外平均湿度\r\n");
        
        if (CollectionUtils.isEmpty(coolingStationSimilarDayList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (CoolingStationSimilarDayDTO similarDay : coolingStationSimilarDayList) {
            csvBuilder.append(Optional.ofNullable(similarDay.getSystemName()).orElse(""))
                    .append(",").append(Optional.ofNullable(similarDay.getDate()).orElse(""))
                    .append(",").append(formatWithUnit(similarDay.getReferenceEnergyValue(), "kWh"))
                    .append(",").append(formatWithUnit(similarDay.getAllDayAvgTemp(), "℃"))
                    .append(",").append(formatWithUnit(similarDay.getAllDayMaxTemp(), "℃"))
                    .append(",").append(formatWithUnit(similarDay.getAllDayAvgHumidity(), "%RH"))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建逐 15min 冷站数据 CSV 字符串
     */
    private static void buildCoolingStation15MinDataCsv(StringBuilder csvBuilder, List<CoolingStation15MinDataDTO> coolingStation15MinDataList) {
        csvBuilder.append("逐 15min 冷站数据\r\n");
        csvBuilder.append("时间点，系统名称，冷冻水供回水温差设定，冷冻水供回水温差，冷冻水一次侧供水干管温度壳配置下限值，冷冻水一次侧供水干管温度，冷冻水一次侧回水干管温度，冷却塔出水温度设定，冷机 (冷却水) 进水干管温度，冷机 (冷却水) 出水干管温度，冷却水进出水温差设定，冷却水进出水温差，免费冷开关设定，供冷开关设定\r\n");
        
        if (CollectionUtils.isEmpty(coolingStation15MinDataList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (CoolingStation15MinDataDTO data : coolingStation15MinDataList) {
            csvBuilder.append(Optional.ofNullable(data.getTimePoint()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSystemName()).orElse(""))
                    .append(",").append(formatWithUnit(data.getChillWaterDeltaTempSet(), "℃"))
                    .append(",").append(formatWithUnit(data.getChillWaterDeltaTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getPrimaryChillWaterSupplyTempLowerLimit(), "℃"))
                    .append(",").append(formatWithUnit(data.getPrimaryChillWaterMainSupplyTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getPrimaryChillWaterMainReturnTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getCoolingTowerOutTempSet(), "℃"))
                    .append(",").append(formatWithUnit(data.getCoolWaterMainReturnTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getCoolWaterMainSupplyTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getCoolWaterDeltaTempSet(), "℃"))
                    .append(",").append(formatWithUnit(data.getCoolWaterDeltaTemp(), "℃"))
                    .append(",").append(Optional.ofNullable(data.getFreeCoolingSwitchSet()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getCoolingSupplySwitchSet()).orElse(""))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建逐 15min 冷站服务空间的温度状态 CSV 字符串
     */
    private static void buildServiceSpaceTempStatusCsv(StringBuilder csvBuilder, List<ServiceSpaceTempStatusDTO> serviceSpaceTempStatusList) {
        csvBuilder.append("逐 15min 冷站服务空间的温度状态\r\n");
        csvBuilder.append("时间点，系统名称，温度满足率，过冷空间数量，偏冷空间数量，舒适空间数量，偏热空间数量，过热空间数量，无数据空间数量，总空间数量，过冷空间数量占比，偏冷空间数量占比，舒适空间数量占比，偏热空间数量占比，过热空间数量占比，无数据空间数量占比\r\n");
        
        if (CollectionUtils.isEmpty(serviceSpaceTempStatusList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (ServiceSpaceTempStatusDTO data : serviceSpaceTempStatusList) {
            csvBuilder.append(Optional.ofNullable(data.getTimePoint()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSystemName()).orElse(""))
                    .append(",").append(formatWithUnit(data.getTemperatureSatisfactionRate(), "%"))
                    .append(",").append(Optional.ofNullable(data.getTooColdSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSlightlyColdSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getComfortableSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSlightlyHotSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getTooHotSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getNoDataSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(Optional.ofNullable(data.getTotalSpaceCount()).map(e -> e + "个").orElse(""))
                    .append(",").append(formatWithUnit(data.getTooColdSpacePercentage(), "%"))
                    .append(",").append(formatWithUnit(data.getSlightlyColdSpacePercentage(), "%"))
                    .append(",").append(formatWithUnit(data.getComfortableSpacePercentage(), "%"))
                    .append(",").append(formatWithUnit(data.getSlightlyHotSpacePercentage(), "%"))
                    .append(",").append(formatWithUnit(data.getTooHotSpacePercentage(), "%"))
                    .append(",").append(formatWithUnit(data.getNoDataSpacePercentage(), "%"))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建逐 15min 冷机数据 CSV 字符串
     */
    private static void buildChiller15MinDataCsv(StringBuilder csvBuilder, List<Chiller15MinDataDTO> chiller15MinDataList) {
        csvBuilder.append("逐 15min 冷机数据\r\n");
        csvBuilder.append("时间点，系统名称，设备名称，运行状态，运行负载率，设备大小机类型\r\n");
        
        if (CollectionUtils.isEmpty(chiller15MinDataList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (Chiller15MinDataDTO data : chiller15MinDataList) {
            csvBuilder.append(Optional.ofNullable(data.getTimePoint()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSystemName()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getEquipmentName()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getRunStatus()).orElse(""))
                    .append(",").append(formatWithUnit(data.getIPLR(), "%"))
                    .append(",").append(Optional.ofNullable(data.getEquipmentSizeType()).orElse(""))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建逐 15min 冷却塔风机数据 CSV 字符串
     */
    private static void buildCoolingTowerFan15MinDataCsv(StringBuilder csvBuilder, List<CoolingTowerFan15MinDataDTO> coolingTowerFan15MinDataList) {
        csvBuilder.append("逐 15min 冷却塔风机数据\r\n");
        csvBuilder.append("时间点，系统名称，设备名称，运行状态，运行频率，运行频率设定，运行档位，运行档位设定\r\n");
        
        if (CollectionUtils.isEmpty(coolingTowerFan15MinDataList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (CoolingTowerFan15MinDataDTO data : coolingTowerFan15MinDataList) {
            csvBuilder.append(Optional.ofNullable(data.getTimePoint()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getSystemName()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getEquipmentName()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getRunStatus()).orElse(""))
                    .append(",").append(formatWithUnit(data.getFreq(), "Hz"))
                    .append(",").append(formatWithUnit(data.getEquipFreqSet(), "Hz"))
                    .append(",").append(Optional.ofNullable(data.getFanGear()).orElse(""))
                    .append(",").append(Optional.ofNullable(data.getEquipGearSet()).orElse(""))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }

    /**
     * 构建逐 15min 室外天气数据 CSV 字符串
     */
    private static void buildOutdoorWeatherDataCsv(StringBuilder csvBuilder, List<OutdoorWeatherDataDTO> outdoorWeatherDataList) {
        csvBuilder.append("逐 15min 室外天气数据\r\n");
        csvBuilder.append("时间点，温度，湿度，湿球温度，焓值\r\n");
        
        if (CollectionUtils.isEmpty(outdoorWeatherDataList)) {
            csvBuilder.append("\r\n");
            return;
        }
        
        for (OutdoorWeatherDataDTO data : outdoorWeatherDataList) {
            csvBuilder.append(Optional.ofNullable(data.getTimePoint()).orElse(""))
                    .append(",").append(formatWithUnit(data.getTemperature(), "℃"))
                    .append(",").append(formatWithUnit(data.getHumidity(), "%RH"))
                    .append(",").append(formatWithUnit(data.getWetBulbTemp(), "℃"))
                    .append(",").append(formatWithUnit(data.getEnthalpy(), "kJ/kg.dra"))
                    .append("\r\n");
        }
        csvBuilder.append("\r\n");
    }
}