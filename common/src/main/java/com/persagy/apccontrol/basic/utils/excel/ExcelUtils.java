package com.persagy.apccontrol.basic.utils.excel;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Excel解析工具类
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);
    private static final String LOG_TAG = "ExcelUtils";

    /**
     * 解析行
     *
     * @param sheet    Sheet
     * @param rowIndex 行下标
     * @return 行
     * @since 5.241101.0
     */
    public static Row analyzeRow(@NonNull Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex);
    }

    /**
     * 解析行
     *
     * @param sheet         Sheet
     * @param beginRowIndex 开始读取行下标
     * @return 行
     * @since 5.240602.0
     */
    public static List<Row> analyzeRows(@NonNull Sheet sheet, int beginRowIndex) {
        List<Row> rows = new ArrayList<>();
        for (int rowIndex = beginRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (Objects.nonNull(row) && !ExcelUtils.isEmptyRow(row)) {
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * 解析字符串
     *
     * @param row       行
     * @param cellIndex 列下标
     * @return 字符串
     * @since 5.240602.0
     */
    public static String analyzeValueByString(@NonNull Row row, int cellIndex) {
        Cell cell = analyzeCell(row, cellIndex);
        if (Objects.isNull(cell)) {
            return null;
        }
        CellType cellType = cell.getCellType();
        String cellValue;
        switch (cellType) {
            case NUMERIC:
                cellValue = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case FORMULA:
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK:
                cellValue = "";
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case ERROR:
                throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                        String.format(LocaleMessageUtil.getMessage("key_8fec3dde64de48c38f34cd23a97f5333"), String.valueOf(cell.getErrorCellValue())));
            default:
                throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_1a12119b20404a7782eaeee51498aaea"));
        }
        return StringUtils.trimToNull(cellValue);
    }

    /**
     * 解析整数
     *
     * @param row       行
     * @param cellIndex 列下标
     * @return 整数
     * @since 5.240602.0
     */
    public static int analyzeValueByInteger(@NonNull Row row, int cellIndex) {
        Cell cell = analyzeCell(row, cellIndex);
        if (cell.getCellType() == CellType.STRING) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                    String.format(LocaleMessageUtil.getMessage("key_6b664b8db13f4fd1afc5c5c36b144cb1"),
                            row.getSheet().getSheetName(), row.getRowNum() + 1, cellIndex + 1));
        }
        return (int) cell.getNumericCellValue();
    }

    /**
     * 解析JsonNode
     *
     * @param row               行
     * @param cellIndex         列下标
     * @param exceptionCallback 解析异常回调
     * @return JsonNode
     * @since 5.240801.0
     */
    public static JsonNode analyzeValueByJsonNode(
            @NonNull Row row, int cellIndex,
            @NonNull Consumer<JsonProcessingException> exceptionCallback) {
        return analyzeValueByJsonNode(row, cellIndex, exceptionCallback, null, null);
    }

    /**
     * 解析JsonNode
     *
     * @param row               行
     * @param cellIndex         列下标
     * @param exceptionCallback 解析异常回调
     * @param validClass        校验实体类型
     * @param validCallback     校验失败回调
     * @param <T>               校验实体类型
     * @return JsonNode
     * @since 5.240801.0
     */
    public static <T> JsonNode analyzeValueByJsonNode(
            @NonNull Row row, int cellIndex,
            @NonNull Consumer<JsonProcessingException> exceptionCallback,
            @Nullable Class<T> validClass,
            @Nullable Consumer<JsonProcessingException> validCallback) {
        String tackValueLogicString = ExcelUtils.analyzeValueByString(row, cellIndex);
        if (StringUtils.isNotBlank(tackValueLogicString)) {
            ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
            try {
                // 替换不间断空格为标准空格
                tackValueLogicString = tackValueLogicString.replace('\u00a0', ' ');
                JsonNode jsonNode = objectMapper.readTree(tackValueLogicString);
                if (Objects.nonNull(validClass)) {
                    try {
                        objectMapper.treeToValue(jsonNode, validClass);
                    } catch (JsonProcessingException e) {
                        if (Objects.nonNull(validCallback)) {
                            validCallback.accept(e);
                        }
                    }
                }
                return jsonNode;
            } catch (JsonProcessingException e) {
                exceptionCallback.accept(e);
            }
        }
        return null;
    }

    /**
     * 获取列
     *
     * @param row       行
     * @param cellIndex 列下标
     * @return 列
     * @since 5.240602.0
     */
    public static Cell analyzeCell(@NonNull Row row, int cellIndex) {
        try {
            return row.getCell(cellIndex);
        } catch (NullPointerException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "文件列数量不足", Arrays.asList(
                    Pair.of("rowNum", row.getRowNum()),
                    Pair.of("cellIndex", cellIndex)
            ));
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_eb32108251344f5bbbe1baab26f6b4df"));
        }
    }

    /**
     * 是否为空行
     *
     * @param row 行
     * @return 是否为空行
     * @since 5.240602.0
     */
    public static boolean isEmptyRow(@NonNull Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (Objects.nonNull(cell) && StringUtils.isNotBlank(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 数据校验失败
     *
     * @param row          行
     * @param cellIndex    列索引
     * @param errorMessage 错误信息
     * @param target       目标源
     * @since 5.240801.0
     */
    public static void validFailedLog(
            @NonNull Row row, @NonNull int cellIndex,
            @NonNull String errorMessage, @Nullable List<String> target) {
        validFailedLog(row, cellIndex, errorMessage, null, target);
    }

    /**
     * 数据校验失败
     *
     * @param row          行
     * @param cellIndex    列索引
     * @param errorMessage 错误信息
     * @param e            异常
     * @param target       目标源
     * @since 5.240802.0
     */
    public static void validFailedLog(
            @NonNull Row row, @NonNull int cellIndex, @NonNull String errorMessage,
            @Nullable Throwable e, @Nullable List<String> target) {
        String sheetName = row.getSheet().getSheetName();
        int rowNum = row.getRowNum() + 1;
        LogExecutor.project().warn(LOGGER, LOG_TAG, errorMessage, e, Arrays.asList(
                Pair.of("sheetName", sheetName),
                Pair.of("rowNum", rowNum),
                Pair.of("cellIndex", cellIndex)
        ));
        if (Objects.nonNull(target)) {
            target.add(String.format(LocaleMessageUtil.getMessage("key_3fa68de3361444759d7c0ba6655db0c8"), errorMessage, sheetName, rowNum, cellIndex + 1));
        }
    }
}
