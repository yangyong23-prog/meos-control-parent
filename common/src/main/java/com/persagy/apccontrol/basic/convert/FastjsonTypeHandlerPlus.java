package com.persagy.apccontrol.basic.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FastjsonTypeHandlerPlus<T> extends BaseTypeHandler<Object> {

    private Type type;

    /**
     * 构造方法
     */
    public FastjsonTypeHandlerPlus() {
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        this.type = resolvableType.as(FastjsonTypeHandlerPlus.class).getGeneric().getType();
    }

 
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }
 
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return convertToEntityAttribute(value);
    }
 
 
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertToEntityAttribute(rs.getString(columnIndex));
    }
 
 
    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return convertToEntityAttribute(value);
    }
 
 
    public Object convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            if (List.class.isAssignableFrom(type.getClass())) {
                return Collections.emptyList();
            } else if (Set.class.isAssignableFrom(type.getClass())) {
                return Collections.emptySet();
            } else if (Map.class.isAssignableFrom(type.getClass())) {
                return Collections.emptyMap();
            } else {
                return null;
            }
        }
        return toObject(dbData, type);
    }


    public static <T> T toObject(String json, Type type) {
        Assert.hasText(json, "[Assertion failed] - this json must have text; it must not be null, empty, or blank");
        Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
        return JSONObject.parseObject(json, type);
    }

}