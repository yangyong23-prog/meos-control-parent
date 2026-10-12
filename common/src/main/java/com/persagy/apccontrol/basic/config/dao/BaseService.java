package com.persagy.apccontrol.basic.config.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.config.dao.check.CheckProvider;
import com.persagy.apccontrol.basic.exception.enumeration.MybatisServiceErrorEnum;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.framework.common.entity.PageParams;
import com.persagy.validate.utility.exception.ApplicationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MybatisService基类
 *
 * <p>该类为 [MybatisService] 基类, 对 [MybatisPlus] 自带的 {@link IService} 进行了进一步的封装,
 * 增强业务效果, 采用该类的方法以替代 {@link IService} 中的方法
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
public interface BaseService<T extends BaseAssignIdDO> extends IService<T>, CheckProvider {

    /**
     * JacksonTypeHandler
     */
    String JACKSON_MAPPING = "typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler";

    /**
     * 查询锁后缀
     */
    String QUERY_LOCK = " for update";

    // ------------------------- Ignore-Begin -------------------------

    /**
     * 忽略ID获取是否存在
     *
     * @param id ID
     * @return 是否存在 [true: 存在, false: 不存在]
     * @since v5.0.0
     */
    default boolean isExistsByIdIgnore(@Nullable Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        return this.countByIdValidate(id) != 0;
    }

    /**
     * 批量忽略ID获取是否存在
     *
     * @param ids IDs
     * @return 是否存在 [true: 存在, false: 不存在]
     * @since v5.0.0
     */
    default boolean isExistsByIdIgnore(@Nullable Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return false;
        }
        return this.countByIdValidate(ids) != 0;
    }

    /**
     * 忽略ID获取数量
     *
     * @param id ID
     * @return 数量
     * @since v5.0.0
     */
    default long countByIdIgnore(@Nullable Long id) {
        if (Objects.isNull(id)) {
            return -1;
        }
        return this.countByIdValidate(id);
    }

    /**
     * 忽略ID获取数量
     *
     * @param ids ID
     * @return 数量
     * @since v5.0.0
     */
    default long countByIdIgnore(@Nullable Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return -1;
        }
        return this.countByIdValidate(ids);
    }

    /**
     * 忽略ID并获取DO
     *
     * @param id ID
     * @return DO
     * @since v5.0.0
     */
    @Nullable
    default T getByIdIgnore(@Nullable Long id) {
        return this.getByIdIgnoreOpt(id).orElse(null);
    }

    /**
     * 忽略ID并获取DO
     *
     * @param id ID
     * @return DO
     * @since v5.0.0
     */
    default Optional<T> getByIdIgnoreOpt(@Nullable Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.getOne(Wrappers.<T>query().eq(BaseDO.FIELD_ID, id)));
    }

    /**
     * 忽略IDs并获取DO
     *
     * @param ids IDs
     * @return DOs
     * @since v5.0.0
     */
    default List<T> listByIdIgnore(@Nullable Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(ids);
    }

    /**
     * 忽略结果并获取DO
     *
     * @param wrapper wrapper
     * @return DOs
     * @since v5.0.0
     */
    @Nullable
    default List<T> listIgnore(Wrapper<T> wrapper) {
        List<T> result = this.list(wrapper);
        if (Objects.isNull(result) || result.isEmpty()) {
            return Collections.emptyList();
        }
        return result;
    }

    /**
     * 忽略PageRequest并获取Pageable
     * 默认根据 {@link BaseDO#getUpdateTime()} 进行排序
     *
     * @param pageParams pageParams
     * @return Pageable
     * @since v5.0.0
     */
    default IPage<T> pageIgnore(@Nullable PageParams<?> pageParams) {
        if (Objects.isNull(pageParams)) {
            return new Page<>();
        }
        return this.page(new Page<T>(pageParams.getCurrent(), pageParams.getSize())
                .addOrder(OrderItem.desc(BaseService.toUnderlineCase(BaseDO.FIELD_UPDATE_TIME))));
    }

    /**
     * 怒略实体类ID并保存
     *
     * @param entity 实体类
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default Boolean saveIgnore(@Nullable T entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        return this.save(BaseService.clearEntityDefaultParameterAndGet(entity,
                true, true, true));
    }

    /**
     * 批量怒略验证实体类ID并保存
     *
     * @param entities 实体对象集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveIgnore(@Nullable Collection<T> entities) {
        return this.saveIgnore(entities, 1000);
    }

    /**
     * 批量怒略验证实体类ID并保存
     *
     * @param entities  实体对象集合
     * @param batchSize 插入批次数量
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveIgnore(@Nullable Collection<T> entities, int batchSize) {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            return false;
        }
        entities.forEach(BaseAssignIdDO::clearSaveParameter);
        return this.saveBatch(entities, batchSize);
    }

    /**
     * 怒略验证实体类ID并更新
     *
     * @param entity 实体类
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdIgnore(@Nullable T entity) {
        if (Objects.isNull(entity)) {
            return false;
        }
        entity.clearUpdateParameter();
        return this.updateById(entity);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities 实体对象集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdIgnore(@Nullable Collection<T> entities) {
        return this.updateByIdIgnore(entities, 1000);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities  实体对象集合
     * @param batchSize 插入批次数量
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdIgnore(@Nullable Collection<T> entities, int batchSize) {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            return false;
        }
        entities.forEach(BaseAssignIdDO::clearUpdateParameter);
        return this.updateBatchById(entities, batchSize);
    }

    /**
     * 怒略实体类ID并删除
     *
     * @param id ID
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdIgnore(@Nullable Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        return this.removeById(id);
    }

    /**
     * 批量怒略实体类ID并删除
     *
     * @param ids ID集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdIgnore(@Nullable Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return false;
        }
        return this.removeByIds(ids);
    }

    // ------------------------- Ignore-End -------------------------

    // ------------------------- Validate-Begin -------------------------

    /**
     * 验证ID获取是否存在
     *
     * @param id ID
     * @return 是否存在 [true: 存在, false: 不存在]
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default boolean isExistsByIdValidate(Long id) throws ApplicationException {
        return this.isExistsByIdValidate(id, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证ID获取是否存在
     *
     * @param id           ID
     * @param errorUserTip 用户提示信息
     * @return 是否存在 [true: 存在, false: 不存在]
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default boolean isExistsByIdValidate(@Nullable Long id, String errorUserTip) throws ApplicationException {
        if (Objects.isNull(id)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.countByIdValidate(id) != 0;
    }

    /**
     * 批量验证ID获取是否存在
     *
     * @param ids IDs
     * @return 是否存在 [true: 存在, false: 不存在]
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default boolean isExistsByIdValidate(@Nullable Collection<Long> ids) throws ApplicationException {
        return this.isExistsByIdValidate(ids, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证ID获取是否存在
     *
     * @param ids          IDs
     * @param errorUserTip 用户提示信息
     * @return 是否存在 [true: 存在, false: 不存在]
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default boolean isExistsByIdValidate(@Nullable Collection<Long> ids, String errorUserTip) throws ApplicationException {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.countByIdValidate(ids) != 0;
    }

    /**
     * 验证ID获取数量
     *
     * @param id ID
     * @return 数量
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default long countByIdValidate(@Nullable Long id) throws ApplicationException {
        return this.countByIdValidate(id, null);
    }

    /**
     * 验证ID获取数量
     *
     * @param id           ID
     * @param errorUserTip 用户提示信息
     * @return 数量
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default long countByIdValidate(@Nullable Long id, @Nullable String errorUserTip) throws ApplicationException {
        if (Objects.isNull(id)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.count(Wrappers.<T>query().select(BaseDO.FIELD_ID).eq(BaseDO.FIELD_ID, id));
    }

    /**
     * 批量验证ID获取数量
     *
     * @param ids IDs
     * @return 数量
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default long countByIdValidate(@Nullable Collection<Long> ids) throws ApplicationException {
        return this.countByIdValidate(ids, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证ID获取数量
     *
     * @param ids          IDs
     * @param errorUserTip 用户提示信息
     * @return 数量
     * @throws ApplicationException ID为空
     * @since v5.0.0
     */
    default long countByIdValidate(@Nullable Collection<Long> ids, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.count(Wrappers.<T>query().select(BaseDO.FIELD_ID).in(BaseDO.FIELD_ID, ids));
    }

    /**
     * 验证ID并获取DO
     *
     * @param id ID
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default T getByIdValidate(@Nullable Long id) throws ApplicationException {
        return this.getByIdValidate(id, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证ID并获取DO
     *
     * @param id           ID
     * @param errorUserTip 用户提示信息
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default T getByIdValidate(@Nullable Long id, @Nullable String errorUserTip) throws ApplicationException {
        if (Objects.isNull(id)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        Optional<T> entity = Optional.ofNullable(this.getOne(Wrappers.<T>query().eq(BaseDO.FIELD_ID, id)));
        if (!entity.isPresent()) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip);
        }
        return entity.get();
    }

    /**
     * 验证IDs并获取DOs
     *
     * @param ids IDs
     * @return DOs
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default List<T> listByIdValidate(@Nullable Collection<Long> ids) throws ApplicationException {
        return this.listByIdValidate(ids, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证IDs并获取DOs
     *
     * @param wrapper wrapper
     * @return DOs
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default List<T> listByIdValidate(Wrapper<T> wrapper) throws ApplicationException {
        return this.listValidate(wrapper, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证IDs并获取DOs
     *
     * @param ids          IDs
     * @param errorUserTip 用户提示信息
     * @return DOs
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default List<T> listByIdValidate(@Nullable Collection<Long> ids, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        List<T> entities = this.list(Wrappers.<T>query().in(BaseDO.FIELD_ID, ids));
        if (Objects.isNull(entities) || entities.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_SET_IS_NULL, errorUserTip);
        }
        return entities;
    }

    /**
     * 验证IDs并获取DOs
     *
     * @param wrapper      wrapper
     * @param errorUserTip 用户提示信息
     * @return DOs
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default List<T> listValidate(Wrapper<T> wrapper, @Nullable String errorUserTip)
            throws ApplicationException {
        List<T> entities = this.list(wrapper);
        if (Objects.isNull(entities) || entities.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return entities;
    }

    /**
     * 验证PageRequest并获取Pageable
     * 默认根据 {@link BaseDO#getUpdateTime()} 进行排序
     *
     * @param pageParams pageParams
     * @return Pageable
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default IPage<T> pageValidate(@Nullable PageParams<?> pageParams) throws ApplicationException {
        return this.pageValidate(pageParams, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证PageRequest并获取Pageable
     * 默认根据 {@link BaseDO#getUpdateTime()} 进行排序
     *
     * @param pageParams   pageParams
     * @param errorUserTip 用户提示信息
     * @return Pageable
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default IPage<T> pageValidate(@Nullable PageParams<?> pageParams, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(pageParams)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.page(new Page<T>(pageParams.getCurrent(), pageParams.getSize())
                .addOrder(OrderItem.desc(BaseService.toUnderlineCase(BaseDO.FIELD_UPDATE_TIME))));
    }

    /**
     * 验证实体类ID并保存
     *
     * @param entity 实体类
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable T entity) throws ApplicationException {
        return this.saveValidate(entity, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证实体类ID并保存
     *
     * @param entity       实体类
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable T entity, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(entity)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.save(BaseService.clearEntityDefaultParameterAndGet(entity,
                true, true, true));
    }

    /**
     * 批量验证实体类ID并保存
     *
     * @param entities 实体对象集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable Collection<T> entities) throws ApplicationException {
        return this.saveValidate(entities, 1000, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证实体类ID并保存
     *
     * @param entities     实体对象集合
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable Collection<T> entities, @Nullable String errorUserTip)
            throws ApplicationException {
        return this.saveValidate(entities, 1000, errorUserTip);
    }

    /**
     * 批量验证实体类ID并保存
     *
     * @param entities  实体对象集合
     * @param batchSize 插入批次数量
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable Collection<T> entities, int batchSize) throws ApplicationException {
        return this.saveValidate(entities, batchSize, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证实体类ID并保存
     *
     * @param entities     实体对象集合
     * @param batchSize    插入批次数量
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveValidate(@Nullable Collection<T> entities, int batchSize, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        entities.forEach(BaseAssignIdDO::clearSaveParameter);
        return this.saveBatch(entities, batchSize);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entity 实体类
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable T entity) throws ApplicationException {
        if (Objects.isNull(entity)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL);
        }
        if (this.countByIdValidate(entity.getId()) == 0) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL);
        }
        return this.updateByIdValidate(entity, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entity       实体类
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable T entity, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(entity)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        if (this.countByIdValidate(entity.getId()) == 0) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip);
        }
        entity.clearUpdateParameter();
        return this.updateById(entity);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities 实体对象集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable Collection<T> entities) throws ApplicationException {
        return this.updateByIdValidate(entities, 1000);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities     实体对象集合
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable Collection<T> entities, @Nullable String errorUserTip)
            throws ApplicationException {
        return this.updateByIdValidate(entities, 1000, errorUserTip);
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities  实体对象集合
     * @param batchSize 插入批次数量
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable Collection<T> entities, int batchSize)
            throws ApplicationException {
        return this.updateByIdValidate(entities, batchSize, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证实体类ID并更新
     *
     * @param entities     实体对象集合
     * @param batchSize    插入批次数量
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException 参数为空|参数错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateByIdValidate(@Nullable Collection<T> entities, int batchSize, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        List<Long> ids = entities.stream().map(BaseAssignIdDO::getId).collect(Collectors.toList());
        if (this.countByIdValidate(ids) != entities.size()) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip);
        }
        entities.forEach(BaseAssignIdDO::clearUpdateParameter);
        return this.updateBatchById(entities, batchSize);
    }

    /**
     * 验证实体类ID并删除
     *
     * @param id ID
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdValidate(@Nullable Long id) throws ApplicationException {
        return this.removeByIdValidate(id, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 验证实体类ID并删除
     *
     * @param id           ID
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdValidate(@Nullable Long id, @Nullable String errorUserTip)
            throws ApplicationException {
        if (this.countByIdValidate(id) == 0) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip);
        }
        return this.removeById(id);
    }

    /**
     * 批量验证实体类ID并删除
     *
     * @param ids ID集合
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdValidate(@Nullable Collection<Long> ids) throws ApplicationException {
        return this.removeByIdValidate(ids, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 批量验证实体类ID并删除
     *
     * @param ids          ID集合
     * @param errorUserTip 用户提示信息
     * @return 操作是否成功 [null: 未执行, true: 成功, false: 失败]
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean removeByIdValidate(@Nullable Collection<Long> ids, @Nullable String errorUserTip)
            throws ApplicationException {
        if (Objects.isNull(ids) || this.countByIdValidate(ids) != ids.size()) {
            throw new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip);
        }
        return this.removeByIds(ids);
    }

    // ------------------------- Validate-End -------------------------

    // ------------------------- QueryLock-Begin -------------------------

    /**
     * 获取并锁定数据
     *
     * @param id ID
     * @return DO
     * @since v5.0.0
     */
    default T getAndLockIgnore(@Nullable Long id) {
        return this.getAndLockIgnoreOpt(id).orElse(null);
    }

    /**
     * 获取并锁定数据
     *
     * @param id ID
     * @return DO
     * @since v5.0.0
     */
    default Optional<T> getAndLockIgnoreOpt(@Nullable Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.getOne(Wrappers.<T>query().eq(BaseDO.FIELD_ID, id).last(QUERY_LOCK)));
    }

    /**
     * 获取并锁定数据
     *
     * @param id ID
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default T getAndLockValidate(@Nullable Long id) throws ApplicationException {
        return this.getAndLockValidate(id, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 获取并锁定数据
     *
     * @param id           ID
     * @param errorUserTip 用户提示信息
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default T getAndLockValidate(@Nullable Long id, @Nullable String errorUserTip) throws ApplicationException {
        if (Objects.isNull(id)) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.getAndLockIgnoreOpt(id).orElseThrow(() ->
                new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip));
    }

    /**
     * 获取并锁定数据
     *
     * @param ids ID集合
     * @return DO
     * @since v5.0.0
     */
    default Collection<T> getAndLockIgnore(@Nullable Collection<Long> ids) {
        return this.getAndLockIgnoreOpt(ids).orElse(Collections.emptyList());
    }

    /**
     * 获取并锁定数据
     *
     * @param ids ID集合
     * @return DO
     * @since v5.0.0
     */
    default Optional<Collection<T>> getAndLockIgnoreOpt(@Nullable Collection<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.list(Wrappers.<T>query().in(BaseDO.FIELD_ID, ids).last(QUERY_LOCK)));
    }

    /**
     * 获取并锁定数据
     *
     * @param ids ID集合
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default Collection<T> getAndLockValidate(@Nullable Collection<Long> ids) throws ApplicationException {
        return this.getAndLockValidate(ids, ApplicationException.DEFAULT_ERROR_USER_TIP.get());
    }

    /**
     * 获取并锁定数据
     *
     * @param ids          ID集合
     * @param errorUserTip 用户提示信息
     * @return DO
     * @throws ApplicationException ID为空|ID错误
     * @since v5.0.0
     */
    default Collection<T> getAndLockValidate(@Nullable Collection<Long> ids, @Nullable String errorUserTip) throws ApplicationException {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            throw new ApplicationException(MybatisServiceErrorEnum.ID_IS_NULL, errorUserTip);
        }
        return this.getAndLockIgnoreOpt(ids).orElseThrow(() ->
                new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, errorUserTip));
    }

    // ------------------------- QueryLock-End -------------------------

    /**
     * 解析实体ID列表
     *
     * @param entities 实体列表
     * @return 实体ID列表
     * @since v5.0.0
     */
    default Set<Long> analyzeIds(@Nullable Collection<T> entities) {
        if (Objects.isNull(entities) || entities.isEmpty()) {
            return new HashSet<>();
        }
        return entities.stream().map(BaseAssignIdDO::getId).collect(Collectors.toSet());
    }

    // ------------------------- Util-Begin -------------------------

    /**
     * 清除实体类默认参数值
     *
     * @param entity            实体类
     * @param isClearId         是否清除ID
     * @param isClearCreateTime 是否清除创建时间
     * @param isClearModifyTime 是否清除更新时间
     * @param <T>               实体类类型
     * @return 操作后的实体类
     * @since v5.0.0
     */
    static <T extends BaseAssignIdDO> T clearEntityDefaultParameterAndGet(
            T entity, boolean isClearId, boolean isClearCreateTime, boolean isClearModifyTime) {
        if (isClearId) {
            entity.setId(null);
        }
        if (isClearCreateTime) {
            entity.setCreateTime(null);
        }
        if (isClearModifyTime) {
            entity.setUpdateTime(null);
        }
        return entity;
    }

    /**
     * 简易驼峰转下划线
     *
     * @param value 驼峰字符串
     * @return 下划线字符串
     * @since v5.0.0
     */
    static String toUnderlineCase(@NonNull String value) {
        Assert.notNull(value, "UnderlineCase value must not be null");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
