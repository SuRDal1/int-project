package com.intarea.intarea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
@AllArgsConstructor
public class ProcessViewDto {
    private LocalDateTime dateTime;
    private String productName;
    private String processType; // "A" or "B"
    private int total;
    private int ok;
    private int ng;

    }

