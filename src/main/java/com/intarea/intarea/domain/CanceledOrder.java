package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CanceledOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="material_Id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductName productName;

    private int orderQuantity;

    private String canceledText;

    private LocalDateTime orderDate;

    private LocalDateTime canceledDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;


}
