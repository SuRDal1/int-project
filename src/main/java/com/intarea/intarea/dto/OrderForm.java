package com.intarea.intarea.dto;

import com.intarea.intarea.domain.OrderCompany;
import com.intarea.intarea.domain.ProductName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderForm {

    private Long id;

    @NotNull(message = "제품을 선택하세요.")
    private ProductName productName;

    @NotNull(message = "수량을 입력해주세요.")
    @Min(value = 1, message = "1개 이상의 수량을 입력해주세요.")
    private int orderQuantity;

    @NotNull(message = "공정예정일을 입력해주세요")
    private LocalDate processDate;

    @NotNull(message = "주문처를 선택해주세요.")
    private OrderCompany orderCompany;

}
