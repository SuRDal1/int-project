package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="material_Id")
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    private MaterialName materialName;

    @Setter
    @Enumerated(EnumType.STRING)
    private Company company;

    @Setter
    private int quantity;

    @Setter
    private LocalDateTime dateTime;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    @Setter
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<OrderMaterial> orderMaterialList;


    // 재료 업뎃용 메서드?
    public void update(String materialName, int quantity, Company company) {
        this.materialName= MaterialName.valueOf(materialName);
        this.quantity = quantity;
        this.company = company;
    }

}
