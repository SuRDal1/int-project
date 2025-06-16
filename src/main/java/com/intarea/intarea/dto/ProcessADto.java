package com.intarea.intarea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessADto {
    private String lotNumber;

    private String status;

    private double temp;

    private double press;

    private String productName;
}
