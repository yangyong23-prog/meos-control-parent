package com.persagy.apccontrol.basic.model.entity.intervention;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TimeSlotEntity {

    public TimeSlotEntity(Long interventionConfigId, DemandInstanceTypeEnum instanceType, LocalDateTime beginDateTime, LocalDateTime endDateTime) {
        this.configId = interventionConfigId;
        this.instanceType = instanceType;
        this.beginDateTime = beginDateTime;
        this.endDateTime = endDateTime;
    }

    private Long configId;
    private String configName;
    private DemandInstanceTypeEnum instanceType;

    private LocalDateTime beginDateTime;

    private LocalDateTime endDateTime;

    public Range<LocalDateTime> asRange() {
        return Range.closed(beginDateTime, endDateTime);
    }

    public static List<TimeSlotEntity> calculateNonOverlappingPartsWithExisting(List<TimeSlotEntity> existingRanges, TimeSlotEntity newRange) {
        Range<LocalDateTime> newRangeClosed = newRange.asRange();
        List<TimeSlotEntity> nonOverlappingRanges = new ArrayList<>();

        // Create a RangeSet with all existing ranges
        RangeSet<LocalDateTime> rangeSet = TreeRangeSet.create();
        existingRanges.forEach(range -> rangeSet.add(range.asRange()));

        // Remove all existing ranges from the new range to get non-overlapping parts
        RangeSet<LocalDateTime> nonOverlappingRangeSet = TreeRangeSet.create();
        nonOverlappingRangeSet.add(newRangeClosed);
        nonOverlappingRangeSet.removeAll(rangeSet);

        // Convert the non-overlapping parts back to TimeRange objects
        for (Range<LocalDateTime> range : nonOverlappingRangeSet.asRanges()) {
            nonOverlappingRanges.add(new TimeSlotEntity(newRange.getConfigId(),newRange.getInstanceType(),range.lowerEndpoint(), range.upperEndpoint()));
        }
        return nonOverlappingRanges;
    }

}
