package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="orders_id")
    private Long id;

    // 제품명
    @Setter
    @Enumerated(EnumType.STRING)
    private ProductName productName;

    // 주문량
    @Setter
    private int orderQuantity;

    // 주문날짜
    @Setter
    private LocalDateTime orderDate;

    // 공정 예정일
    @Setter
    private LocalDate processDate;

    // 현재 상태
    @Setter
    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    // 발주처
    @Setter
    @Enumerated(EnumType.STRING)
    private OrderCompany orderCompany;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;


    @Setter
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<ProcessA> processAList;

    @Setter
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<ProcessB> processBList;


    @Builder.Default
    @OneToMany(mappedBy = "orders" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMaterial> orderMaterialList = new ArrayList<>();

    public void addJobOrderMaterial(OrderMaterial orderMaterial) {
        this.orderMaterialList.add(orderMaterial);
        orderMaterial.setOrders(this);
    }

    public void update(String productName, int orderQuantity, OrderCompany orderCompany, LocalDate processDate) {
        this.productName = ProductName.valueOf(productName);
        this.orderQuantity = orderQuantity;
        this.orderCompany = orderCompany;
        this.processDate = processDate;
    }

}
