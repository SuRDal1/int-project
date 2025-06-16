package com.intarea.intarea.dto;

import com.intarea.intarea.domain.Company;
import com.intarea.intarea.domain.MaterialName;
import com.intarea.intarea.domain.OrderMaterial;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialForm {

    private Long id;

    @NotNull(message = "원재료를 선택해주세요.")
    private MaterialName materialName;

    @NotNull(message = "수량을 입력해주세요.")
    @Min(value = 1, message = "1개 이상의 수량을 입력해주세요.")
    private int quantity;

    @NotNull(message = "거래처 입력은 필수입니다.")
    private Company company;

    private List<OrderMaterial> orderMaterialList;

    private String exhaustionDate;


}
