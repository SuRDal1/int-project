package com.intarea.intarea.dto;

import com.intarea.intarea.domain.PredictionAOrderInput;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data

public class PredictARequest {

    @Valid
    private List<PredictionAOrderInput> orders;

}
