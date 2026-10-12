package com.persagy.apccontrol.basic.config.dao.partition;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.TableNameParser;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.exception.enumeration.BusinessErrorEnum;
import com.persagy.apccontrol.basic.utils.ProjectUtils;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 分表策略抽象类
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Slf4j
public abstract class AbstractPartitionTableStrategy {

    private static final String LOG_TAG = "分表策略";

    /**
     * 默认分表符号
     */
    public static final String DEFAULT_PARTITION_TABLE_SYMBOL = "_";

    /**
     * 集团编码 -> [原始表名 -> 分表名称]
     */
    public static final Map<String, Map<String, Set<String>>>
            GROUP_AND_ORIGIN_TABLE_NAME_PARTITION_TABLE_NAMES = new ConcurrentHashMap<>();

    /**
     * MappedStatementId -> TableInfo
     */
    public static final Map<String, TableInfo> MAPPED_STATEMENT_ID_AND_TABLE_INFO = new ConcurrentHashMap<>();

    /**
     * 支持分表的MappedStatementId
     */
    public static final Set<String> SUPPORT_MAPPED_STATEMENT_ID = Collections.synchronizedSet(new HashSet<>());

    /**
     * 获取分表后缀
     *
     * @return 分表后缀
     * @since v6.0.0
     */
    public abstract String getPartitionTableSuffix();

    /**
     * 执行分表逻辑
     *
     * @param mapper   Mapper对象
     * @param executor 执行内容
     * @param <T>      执行结果类型
     * @param <M>      Mapper类型
     * @return 执行结果
     * @since v6.0.0
     */
    public synchronized <T, M extends BaseMapper<?>> T execute(M mapper, Function<M, T> executor) {
        Class<?> entityClass = getEntityClass(mapper.getClass());
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        String tableName = tableInfo.getTableName();
        Map<String, AbstractPartitionTableStrategy> partitionTableStrategies = LocalContextStorage.PARTITION_KEY.get();
        if (Objects.isNull(partitionTableStrategies)) {
            partitionTableStrategies = new ConcurrentHashMap<>(16);
        }
        partitionTableStrategies.put(tableName, this);
        LocalContextStorage.PARTITION_KEY.set(partitionTableStrategies);
        T result = executor.apply(mapper);
        partitionTableStrategies.remove(tableName);
        LocalContextStorage.PARTITION_KEY.set(partitionTableStrategies);
        return result;
    }

    /**
     * 获取分表分隔符
     *
     * @return 分表分隔符
     * @since v6.0.0
     */
    public String getPartitionTableSymbol() {
        return DEFAULT_PARTITION_TABLE_SYMBOL;
    }

    /**
     * 解析原始创建表SQL
     *
     * @param tableNames 表名称
     * @return 表名 -> 原始创建表SQL
     * @throws ApplicationException 解析原始创建表SQL失败
     * @since v6.0.0
     */
    @SneakyThrows
    public Map<String, String> parseInitTableSql(@NonNull Set<String> tableNames) throws ApplicationException {
        String initSqlResourceDirPath = getInitSqlResourceDir();
        Resource[] initTableSqlResources = ProjectUtils.getResources(initSqlResourceDirPath);
        if (ArrayUtils.isEmpty(initTableSqlResources)) {
            log.error("[{}] 读取分表初始化文件目录失败 [filePath: {}]", LOG_TAG, initSqlResourceDirPath);
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR, LocaleMessageUtil.getMessage("key_fad5788e87c441a3a5d2402998b5fb76"));
        }
        Map<String, String> originInitTableSqlMap = new HashMap<>(tableNames.size());
        for (Resource initTableSqlResource : initTableSqlResources) {
            if (Objects.isNull(initTableSqlResource)) {
                continue;
            }
            String fileBaseName = FilenameUtils.getBaseName(initTableSqlResource.getFilename());
            for (String tableName : tableNames) {
                if (StringUtils.equals(tableName, fileBaseName)) {
                    String initTableSqlString = IOUtils.toString(
                            initTableSqlResource.getInputStream(), StandardCharsets.UTF_8);
                    originInitTableSqlMap.put(tableName, initTableSqlString);
                }
            }
        }
        return originInitTableSqlMap;
    }

    /**
     * 获取初始化SQL脚本资源目录
     *
     * @return 初始化SQL脚本资源目录
     * @since v6.0.0
     */
    public String getInitSqlResourceDir() {
        return "/db/partition/*.sql";
    }

    /**
     * 获取初始化分表表名标识符
     *
     * @return 初始化分表表名标识符
     * @since v6.0.0
     */
    public String getInitPartitionTableNameIdentifier() {
        return "${tableName}";
    }

    /**
     * 是否不支持分表
     *
     * @param msId 映射ID
     * @return 是否不支持分表
     * @since v6.0.0
     */
    public static boolean isNotSupportPartitionTable(@NonNull String msId) {
        if (SUPPORT_MAPPED_STATEMENT_ID.contains(msId)) {
            return false;
        }
        Class<?> mapperClass = getMapperClass(msId);
        Class<?> tableEntityClass = getEntityClass(mapperClass);
        if (tableEntityClass.isAnnotationPresent(PartitionTable.class)) {
            SUPPORT_MAPPED_STATEMENT_ID.add(msId);
            return false;
        }
        return true;
    }

    /**
     * 解析实体类表信息
     *
     * @param msId 映射ID
     * @return 实体类表信息
     * @throws ApplicationException 未找到表信息
     * @since v6.0.0
     */
    public static TableInfo parseTableInfo(@NonNull String msId)
            throws ApplicationException {
        TableInfo tableInfo = MAPPED_STATEMENT_ID_AND_TABLE_INFO.get(msId);
        if (Objects.nonNull(tableInfo)) {
            return tableInfo;
        }
        Class<?> mapperClass = getMapperClass(msId);
        Class<?> tableEntityClass = getEntityClass(mapperClass);
        tableInfo = TableInfoHelper.getTableInfo(tableEntityClass);
        if (Objects.isNull(tableInfo)) {
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR, LocaleMessageUtil.getMessage("key_97ddea4cbb11462aad3c1a15a50b1495"));
        }
        MAPPED_STATEMENT_ID_AND_TABLE_INFO.put(msId, tableInfo);
        return tableInfo;
    }

    /**
     * 获取实体类Class
     *
     * @param mapperClass mapperClass
     * @return 实体类Class
     * @since v6.0.0
     */
    public static Class<?> getEntityClass(@NonNull Class<?> mapperClass) {
        Class<?> entityClass = ReflectionKit.getSuperClassGenericType(mapperClass, Mapper.class, 0);
        if (Objects.isNull(entityClass)) {
            log.error("[{}] 获取实体类失败 [mapperClass: {}]", LOG_TAG, mapperClass.getName());
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR,
                    LocaleMessageUtil.getMessage("key_147c07682d7146ccbe2934a064e712ff"));
        }
        return entityClass;
    }

    /**
     * 获取映射Class
     *
     * @param msId 映射ID
     * @return MapperClass
     * @throws ApplicationException 未找到MapperClass
     * @since v6.0.0
     */
    public static Class<?> getMapperClass(@NonNull String msId) {
        try {
            return Class.forName(StringUtils.substringBeforeLast(msId, "."));
        } catch (ClassNotFoundException e) {
            log.error("[{}] 获取Mapper接口失败 [id: {}]", LOG_TAG, msId, e);
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR, LocaleMessageUtil.getMessage("key_5b5233083b6942a5827d06c878fffa05"));
        }
    }

    /**
     * 是否存在分表
     *
     * @param groupCode       集团编码
     * @param originTableName 原始表名
     * @param realTableName   实际表名
     * @return 是否存在分表
     * @since v6.0.0
     */
    public static boolean isExistsPartitionTable(
            @NonNull String groupCode, @NonNull String originTableName, @NonNull String realTableName) {
        Map<String, Set<String>> originTableNameAndPartitionTableNames =
                GROUP_AND_ORIGIN_TABLE_NAME_PARTITION_TABLE_NAMES.get(groupCode);
        if (MapUtils.isNotEmpty(originTableNameAndPartitionTableNames)) {
            Set<String> partitionTableNames = originTableNameAndPartitionTableNames.get(originTableName);
            return CollectionUtils.isNotEmpty(partitionTableNames) && partitionTableNames.contains(realTableName);
        }
        return false;
    }

    /**
     * 解析表名称
     *
     * @param sql SQL
     * @return 表名称
     * @since v6.0.0
     */
    public static Set<String> analyzeTableName(@NonNull String sql) {
        TableNameParser parser = new TableNameParser(sql);
        List<TableNameParser.SqlToken> names = new ArrayList<>();
        parser.accept(names::add);
        Set<String> tableNames = new HashSet<>();
        for (TableNameParser.SqlToken name : names) {
            tableNames.add(name.getValue());
        }
        return tableNames;
    }

    /**
     * 解析查询数据库表SQL
     *
     * @param tenantDatabasePrefix 集团库前缀
     * @param groupCode            集团编码
     * @param realTableName        表名
     * @return 查询数据库表SQL
     * @since v6.0.0
     */
    public static String parseQueryTableSql(
            @NonNull String tenantDatabasePrefix,
            @NonNull String groupCode, @NonNull String realTableName) {
        String fullSchemaName = new StringJoiner("_", "'", "'")
                .add(groupCode).add(tenantDatabasePrefix).toString();
        return "select * from information_schema.TABLES"
                + " where lower(`TABLE_SCHEMA`) = " + "lower(" + fullSchemaName + ")"
                + " and `TABLE_NAME` = '" + realTableName + "';";
    }
}
