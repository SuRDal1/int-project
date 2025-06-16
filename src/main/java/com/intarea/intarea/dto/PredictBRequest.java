package com.intarea.intarea.dto;

import com.intarea.intarea.domain.PredictionBOrderInput;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data

public class PredictBRequest {

    @Valid
    private List<PredictionBOrderInput> orders;

}
