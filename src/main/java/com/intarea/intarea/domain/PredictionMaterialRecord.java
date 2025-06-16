package com.intarea.intarea.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
public class PredictionMaterialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private double prediction;

    @Setter
    private LocalDateTime predictAt;

    @Lob   //LargeObject의 줄임말, DB에 텍스트 바이너리 대용량 필드를 저장하고 싶을 때 사용
    @Setter
    private String inputData;

    @Setter
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL)
    private List<PredictMaterialOrderInput> orderInputs;

}
