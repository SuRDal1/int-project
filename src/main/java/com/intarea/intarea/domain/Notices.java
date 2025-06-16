package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String title;

    @Setter
    private String content;

    private LocalDateTime createdAt;

    @Setter
    private LocalDateTime modifiedAt;

    @Setter
    @Column(nullable = false)
    private int views = 0;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users author;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

