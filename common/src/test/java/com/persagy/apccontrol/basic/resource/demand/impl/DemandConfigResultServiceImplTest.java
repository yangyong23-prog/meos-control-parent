package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.mapper.demand.DemandConfigResultMapper;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Demand-config result service partition tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigResultServiceImplTest {

    private static final String PROJECT_ID = "project-id";
    private static final Long TEMPLATE_ID = 101L;
    private static final Long SCENE_ID = 202L;
    private static final LocalDate HISTORY_DATE = LocalDate.of(2026, 1, 15);

    private DemandConfigResultMapper mapper;
    private TestDemandConfigResultServiceImpl service;

    /**
     * Initialize the mocked mapper and current-project context.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId(PROJECT_ID);
        this.mapper = Mockito.mock(DemandConfigResultMapper.class);
        this.service = new TestDemandConfigResultServiceImpl();
        this.service.setMapper(this.mapper);
        Mockito.when(this.mapper.list(
                        Mockito.eq(PROJECT_ID),
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class),
                        Mockito.nullable(String[].class)))
                .thenReturn(Collections.emptyList());
    }

    /**
     * Clear the current-project context after each test.
     *
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.remove();
    }

    /**
     * Verify large explicit instance lists are partitioned into at most 500 IDs per query.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPartitionLargeExplicitInstanceIds() {
        Set<String> spaceIds = IntStream.range(0, 501)
                .mapToObj(index -> "space-" + index)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        DemandConfigResultRepositoryQueryParam param = new DemandConfigResultRepositoryQueryParam();
        param.setDate(HISTORY_DATE);
        param.setRelTemplateId(TEMPLATE_ID);
        param.setRelSceneId(SCENE_ID);
        param.setIsShielded(Boolean.FALSE);
        param.setDemandInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setDemandInstanceIds(spaceIds);

        Assertions.assertTrue(this.service.list(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> paramCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        ArgumentCaptor<String[]> columnsCaptor = ArgumentCaptor.forClass(String[].class);
        Mockito.verify(this.mapper, Mockito.times(2)).list(
                Mockito.eq(PROJECT_ID),
                paramCaptor.capture(),
                columnsCaptor.capture());
        List<DemandConfigResultRepositoryQueryParam> partitionParams = paramCaptor.getAllValues();
        List<Set<String>> spacePartitions = new ArrayList<>();
        for (DemandConfigResultRepositoryQueryParam partitionParam : partitionParams) {
            Assertions.assertEquals(HISTORY_DATE, partitionParam.getDate());
            Assertions.assertEquals(TEMPLATE_ID, partitionParam.getRelTemplateId());
            Assertions.assertEquals(SCENE_ID, partitionParam.getRelSceneId());
            Assertions.assertEquals(Boolean.FALSE, partitionParam.getIsShielded());
            Assertions.assertEquals(
                    DemandInstanceTypeEnum.SPACE,
                    partitionParam.getDemandInstanceType());
            spacePartitions.add(new LinkedHashSet<>(partitionParam.getDemandInstanceIds()));
        }
        Assertions.assertEquals(2, spacePartitions.size());
        Assertions.assertTrue(spacePartitions.stream().allMatch(partition -> partition.size() <= 500));
        Set<String> partitionedSpaceIds = spacePartitions.stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Assertions.assertEquals(spaceIds, partitionedSpaceIds);
        for (String[] columns : columnsCaptor.getAllValues()) {
            Assertions.assertNull(columns);
        }
    }

    /**
     * Test service exposing mapper injection without starting Spring.
     *
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private static final class TestDemandConfigResultServiceImpl extends DemandConfigResultServiceImpl {

        /**
         * Set the mocked mapper.
         *
         * @param mapper mocked mapper
         * @since v7.260701.0
         */
        private void setMapper(@NonNull DemandConfigResultMapper mapper) {
            this.baseMapper = mapper;
        }
    }
}
