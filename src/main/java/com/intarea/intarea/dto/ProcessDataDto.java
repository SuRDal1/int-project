package com.intarea.intarea.dto;

import com.intarea.intarea.domain.ProcessA;
import com.intarea.intarea.domain.ProcessB;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProcessDataDto {
    private List<ProcessA> processAList;
    private List<ProcessB> processBList;
    //정상
    private long okProduct;
    //에러
    private long ngProduct;
    //총주문수
    private int totalOrder;

}
