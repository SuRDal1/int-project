package com.intarea.intarea.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class PredictMaterialOrderInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotEmpty(message = "날짜를 입력해주세요.")
    private String date;

    @Setter
    @NotNull(message = "수량을 입력해주세요.")
    private Integer qty;

    @Setter
    @NotNull(message = "원재료를 입력해주세요.")
    private MaterialName materialName;

    @Setter
    @ManyToOne
    @JoinColumn(name = "record_id")
    private PredictionMaterialRecord record;
}
