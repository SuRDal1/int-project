package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProcessB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String lotNumber;


    // 상열 (좌) 온도
    @Setter
    private double leftHighTemp;

    // 상열 (중) 온도
    @Setter
    private double midHighTemp;

    // 상열 (우) 온도
    @Setter
    private double rightHighTemp;

    @Setter
    private int step;

    @Setter
    private LocalDateTime processDateTime;

    @Setter
    private boolean pass;

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "orders_id")
    private Orders orders;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;


}
