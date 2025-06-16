package com.intarea.intarea.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class PredictionAOrderInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Setter
    private double temp;

    @Setter
    private double press;

    @Setter
    @NotNull(message = "제품명을 입력해주세요.")
    private ProductName productName;

    @Setter
    @OneToOne
    @JoinColumn(name = "record_id")
    @JsonIgnore
    private PredictionAOrderRecord record;
}
