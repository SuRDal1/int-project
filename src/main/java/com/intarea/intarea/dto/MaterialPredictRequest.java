package com.intarea.intarea.dto;

import com.intarea.intarea.domain.PredictMaterialOrderInput;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data

public class MaterialPredictRequest {

    @Valid
    private List<PredictMaterialOrderInput> orders;

    private String predictType;
}
