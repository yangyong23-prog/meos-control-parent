package com.persagy.apccontrol.basic.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JackSonTypeHandlerPlus<T> extends BaseTypeHandler<Object> {

    private final Type type;
    private final ObjectMapper objectMapper;

    public JackSonTypeHandlerPlus() {
        this.objectMapper = new ObjectMapper();
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        this.type = resolvableType.as(JackSonTypeHandlerPlus.class).getGeneric().getType();
    }
    
    public JackSonTypeHandlerPlus(Type type) {
        this.objectMapper = new ObjectMapper();
        this.type = type;
    }
    
    public JackSonTypeHandlerPlus(Type type, ObjectMapper objectMapper) {
        this.type = type;
        this.objectMapper = objectMapper;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error occurred when serializing parameter to JSON", e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return convertToEntityAttribute(value);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return convertToEntityAttribute(value);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return convertToEntityAttribute(value);
    }
    
    private Object convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (List.class.isAssignableFrom(clazz)) {
                    return Collections.emptyList();
                } else if (Set.class.isAssignableFrom(clazz)) {
                    return Collections.emptySet();
                } else if (Map.class.isAssignableFrom(clazz)) {
                    return Collections.emptyMap();
                }
            }
            return null;
        }
        
        try {
            if (type != null) {
                return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructType(type));
            } else {
                return objectMapper.readValue(dbData, Object.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred when deserializing JSON to object", e);
        }
    }
}