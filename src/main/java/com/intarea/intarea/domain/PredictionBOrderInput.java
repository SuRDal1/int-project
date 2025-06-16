package com.intarea.intarea.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class PredictionBOrderInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Setter
    private double leftHighTemp;

    @Setter
    private double midHighTemp;

    @Setter
    private double rightHighTemp;

    @Setter
    @NotNull(message = "제품명을 입력해주세요.")
    private ProductName productName;

    @Setter
    @OneToOne
    @JoinColumn(name = "record_id")
    @JsonIgnore
    private PredictionBOrderRecord record;
}
