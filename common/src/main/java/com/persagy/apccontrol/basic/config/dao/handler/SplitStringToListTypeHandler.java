package com.persagy.apccontrol.basic.config.dao.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.lang.Nullable;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串分割转集合类型转换器
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public class SplitStringToListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.join(",", parameter));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return resolveResult(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return resolveResult(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return resolveResult(cs.getString(columnIndex));
    }

    /**
     * 解析结果
     *
     * @param dbResult 数据库中的值
     * @return 结果值
     * @since v5.0.0
     */
    private List<String> resolveResult(@Nullable String dbResult) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isNotBlank(dbResult)) {
            result.addAll(Arrays.asList(dbResult.split(",")));
        }
        return result;
    }
}
