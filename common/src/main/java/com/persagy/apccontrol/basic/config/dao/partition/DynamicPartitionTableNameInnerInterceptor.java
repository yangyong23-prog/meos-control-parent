package com.persagy.apccontrol.basic.config.dao.partition;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.exception.enumeration.BusinessErrorEnum;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.database.properties.DatabaseProperties;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 动态分表拦截器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Slf4j
@Component
public class DynamicPartitionTableNameInnerInterceptor extends DynamicTableNameInnerInterceptor {

    @Autowired
    @Lazy
    private DatabaseProperties databaseProperties;
    @Autowired
    @Lazy
    private JdbcTemplate jdbcTemplate;

    /**
     * 构造器
     *
     * @since v6.0.0
     */
    public DynamicPartitionTableNameInnerInterceptor() {
        super.setTableNameHandler(DynamicPartitionTableNameHandler.INSTANCE);
    }

    @Override
    public boolean willDoQuery(
            Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        String mapperStatementId = ms.getId();
        if (AbstractPartitionTableStrategy.isNotSupportPartitionTable(mapperStatementId)) {
            return true;
        }
        TableInfo tableInfo = AbstractPartitionTableStrategy.parseTableInfo(mapperStatementId);
        String entityTableName = tableInfo.getTableName();
        String groupCode = ContextUtil.getTenant().toLowerCase();
        Map<String, AbstractPartitionTableStrategy> partitionTableStrategies =
                analyzePartitionTableStrategies(true);
        AbstractPartitionTableStrategy partitionTableStrategy =
                Objects.requireNonNull(partitionTableStrategies).get(entityTableName);
        String partitionTableSymbol = partitionTableStrategy.getPartitionTableSymbol();
        String partitionTableNameSuffix = partitionTableStrategy.getPartitionTableSuffix();
        String realTableName = entityTableName + partitionTableSymbol + partitionTableNameSuffix;
        if (AbstractPartitionTableStrategy.isExistsPartitionTable(groupCode, entityTableName, realTableName)) {
            return true;
        }

        String tenantDatabasePrefix = databaseProperties.getTenantDatabasePrefix();
        String queryTableSql = AbstractPartitionTableStrategy.parseQueryTableSql(
                tenantDatabasePrefix, groupCode, realTableName);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(queryTableSql);
        if (CollectionUtils.isNotEmpty(result)) {
            MapExecutor.dynamicPut(AbstractPartitionTableStrategy.GROUP_AND_ORIGIN_TABLE_NAME_PARTITION_TABLE_NAMES,
                    groupCode, entityTableName, realTableName, HashMap::new, HashSet::new);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SneakyThrows
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (AbstractPartitionTableStrategy.isNotSupportPartitionTable(ms.getId())) {
            return;
        }
        Map<String, AbstractPartitionTableStrategy> partitionTableStrategies =
                Objects.requireNonNull(analyzePartitionTableStrategies(true));

        BoundSql boundSql = ms.getBoundSql(parameter);
        Set<String> tableNames = AbstractPartitionTableStrategy.analyzeTableName(boundSql.getSql());
        for (String tableName : tableNames) {
            AbstractPartitionTableStrategy partitionTableStrategy = partitionTableStrategies.get(tableName);
            if (Objects.isNull(partitionTableStrategy)) {
                throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR,
                        String.format(LocaleMessageUtil.getMessage("key_4fb3c05a43344b76aab87f297c890ecc"), tableName));
            }

            Map<String, String> initTableSql = partitionTableStrategy.parseInitTableSql(tableNames);
            if (MapUtils.isEmpty(initTableSql)) {
                throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR,
                        String.format(LocaleMessageUtil.getMessage("key_a98ea8bafa5549b58f662dedafa26bc9"), partitionTableStrategy.getClass().getName()));
            }
            String originInitTableSql = initTableSql.get(tableName);
            if (StringUtils.isBlank(originInitTableSql)) {
                throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR,
                        String.format(LocaleMessageUtil.getMessage("key_f21b39e0f330497c9c4bbfcf95f0ee65"), tableName));
            }

            String groupCode = ContextUtil.getTenant().toLowerCase();
            String realTableName = tableName
                    + partitionTableStrategy.getPartitionTableSymbol()
                    + partitionTableStrategy.getPartitionTableSuffix();
            if (AbstractPartitionTableStrategy.isExistsPartitionTable(groupCode, tableName, realTableName)) {
                break;
            }

            String tenantDatabasePrefix = databaseProperties.getTenantDatabasePrefix();
            String fullSchemaName = new StringJoiner("_", "`", "`")
                    .add(ContextUtil.getTenant()).add(tenantDatabasePrefix).toString();
            String realTableNameWithSchema = fullSchemaName + "." + "`" + realTableName + "`";

            String executeSql = originInitTableSql.replace(
                    partitionTableStrategy.getInitPartitionTableNameIdentifier(), realTableNameWithSchema);
            jdbcTemplate.update(executeSql);
            MapExecutor.dynamicPut(AbstractPartitionTableStrategy.GROUP_AND_ORIGIN_TABLE_NAME_PARTITION_TABLE_NAMES,
                    groupCode, tableName, realTableName, HashMap::new, HashSet::new);
        }
    }

    /**
     * 解析分表策略
     *
     * @param isThrowException 是否抛出异常
     * @return 分表策略
     * @since v6.0.0
     */
    public static Map<String, AbstractPartitionTableStrategy> analyzePartitionTableStrategies(boolean isThrowException) {
        Map<String, AbstractPartitionTableStrategy> partitionTableStrategy = LocalContextStorage.PARTITION_KEY.get();
        if (Objects.nonNull(partitionTableStrategy)) {
            return partitionTableStrategy;
        }
        if (isThrowException) {
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_621d500a1bfd40818ecf64db6ab90584"));
        } else {
            return null;
        }
    }

    /**
     * 动态表名处理
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    static class DynamicPartitionTableNameHandler implements TableNameHandler {

        public static final DynamicPartitionTableNameHandler INSTANCE = new DynamicPartitionTableNameHandler();

        @Override
        public String dynamicTableName(String sql, String tableName) {
            Map<String, AbstractPartitionTableStrategy> partitionTableStrategies =
                    analyzePartitionTableStrategies(false);
            String onlyTableName = tableName.contains(".") ? tableName.split("\\.")[1] : tableName;
            if (MapUtils.isEmpty(partitionTableStrategies) || !partitionTableStrategies.containsKey(onlyTableName)) {
                return tableName;
            }
            AbstractPartitionTableStrategy partitionTableStrategy = partitionTableStrategies.get(onlyTableName);
            String partitionTableSymbol = partitionTableStrategy.getPartitionTableSymbol();
            String partitionTableNameSuffix = partitionTableStrategy.getPartitionTableSuffix();
            return tableName + partitionTableSymbol + partitionTableNameSuffix;
        }
    }
}
