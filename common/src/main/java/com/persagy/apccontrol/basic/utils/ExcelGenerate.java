package com.persagy.apccontrol.basic.utils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;


import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Excel数据生成
 *
 * @author JiYinchuan
 * @since v4.3.0
 */
public class ExcelGenerate {

    /**
     * 默认空数据的值
     */
    public static final String DEFAULT_EMPTY_NUMBER_VALUE = "--";

    /**
     * 默认字体名称
     */
    private static final String DEFAULT_FONT_NAME = LocaleMessageUtil.getMessage("key_e0385499f3724c3dba2d53380214de55");

    /**
     * 默认字体大小
     */
    private static final short DEFAULT_FONT_SIZE = 11;

    /**
     * 默认水平是否居中
     */
    private static final boolean DEFAULT_HORIZONTAL_CENTER = true;

    /**
     * 默认垂直是否居中
     */
    private static final boolean DEFAULT_VERTICAL_CENTER = true;

    /**
     * 默认标题行高
     */
    private static final short DEFAULT_HEADER_ROW_HEIGHT = 20;

    /**
     * 默认内容行高
     */
    private static final short DEFAULT_BODY_ROW_HEIGHT = 20;

    /**
     * 获取默认Excel样式策略
     *
     * <ul>
     *     <li>表头背景色: 浅灰</li>
     *     <li>字体: 等线</li>
     *     <li>字体大小: 11</li>
     *     <li>加粗: 否</li>
     *     <li>居中: 是</li>
     *     <li>边框: 全边框</li>
     * </ul>
     *
     * @return Excel的样式
     * @since v4.5.0
     */
    public static HorizontalCellStyleStrategy getDefaultExcelCellStyle() {
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 头背景色
        headWriteCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 头字体大小
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName(DEFAULT_FONT_NAME);
        headWriteFont.setBold(false);
        headWriteFont.setFontHeightInPoints(DEFAULT_FONT_SIZE);
        if (DEFAULT_HORIZONTAL_CENTER) {
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
        if (DEFAULT_VERTICAL_CENTER) {
            headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        headWriteCellStyle.setWriteFont(headWriteFont);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);

        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteCellStyle.merge(headWriteCellStyle, contentWriteCellStyle);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 获取默认行高策略
     *
     * <p><b>Note: </b>包含标题的行高和内容的行高
     *
     * @return 默认行高策略
     * @since v4.5.0
     */
    public static SimpleRowHeightStyleStrategy getDefaultRowHeightStyle() {
        return new SimpleRowHeightStyleStrategy(DEFAULT_HEADER_ROW_HEIGHT, DEFAULT_BODY_ROW_HEIGHT);
    }

    /**
     * 格式化数字类型, 保留两位小数, 当值为空时返回 {@code --}
     *
     * @param value 数字对象
     * @return 格式化后的数据
     * @since v4.5.0
     */
    public static String formatExcelNumber(BigDecimal value) {
        return Objects.nonNull(value) ? value.setScale(2, RoundingMode.HALF_UP).toPlainString() : DEFAULT_EMPTY_NUMBER_VALUE;
    }

    /**
     * 格式化字符串,  当值为空时返回 {@code --}
     *
     * @param value 字符串数据
     * @return 格式化后的数据
     */
    public static String formatExcelStr(String value) {
        return Objects.nonNull(value) ? value : DEFAULT_EMPTY_NUMBER_VALUE;
    }
}
