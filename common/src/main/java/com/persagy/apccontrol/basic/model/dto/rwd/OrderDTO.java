package com.persagy.apccontrol.basic.model.dto.rwd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class OrderDTO {
    private String column;
    private Boolean asc;
}
