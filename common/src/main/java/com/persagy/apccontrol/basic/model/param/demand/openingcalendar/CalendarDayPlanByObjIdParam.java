package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CalendarDayPlanByObjIdParam {

    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objId;
    @NotNull(message = "{key_c9e5b3bfdc0345828c98d7c6f0514bbd}")
    private LocalDate date;
}
