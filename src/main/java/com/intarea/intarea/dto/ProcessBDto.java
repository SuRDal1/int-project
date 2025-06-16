package com.intarea.intarea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessBDto {

    private String lotNumber;

    private String status;

    private double leftHighTemp;

    private double midHighTemp;

    private double rightHighTemp;

    private String productName;

}
