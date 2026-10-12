package com.persagy.apccontrol.basic.config.dao.handler;

import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LegendText;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * List类型处理器
 *
 * @author houyuanzhi
 * @date 2021/6/24 18:12
 */
@MappedJdbcTypes(JdbcType.LONGVARCHAR)  //数据库类型
@MappedTypes({List.class})          //java数据类型
@Slf4j
public class LegendTextListTypeHandler implements TypeHandler<List<LegendText>> {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, List<LegendText> attachments, JdbcType jdbcType) throws SQLException {
        if (CollectionUtils.isNotEmpty(attachments)) {
            String str = JSON.toJSONString(attachments);
            preparedStatement.setString(i, str);
        } else {
            preparedStatement.setString(i, null);
        }
    }

    @Override
    public List<LegendText> getResult(ResultSet resultSet, String s) throws SQLException {
        String str = resultSet.getString(s);
        if (StringUtils.isNotBlank(str)) {
            return JSON.parseArray(str, LegendText.class);
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<LegendText> getResult(ResultSet resultSet, int i) throws SQLException {
        String str = resultSet.getString(i);
        if (StringUtils.isNotBlank(str)) {
            return JSON.parseArray(str, LegendText.class);
        }
        return new ArrayList<>(0);
    }

    @Override
    public List<LegendText> getResult(CallableStatement callableStatement, int i) throws SQLException {
        if (StringUtils.isNotBlank(callableStatement.getString(i))) {
            String str = callableStatement.getString(i);
            return JSON.parseArray(str, LegendText.class);
        }
        return new ArrayList<>(0);
    }
}
