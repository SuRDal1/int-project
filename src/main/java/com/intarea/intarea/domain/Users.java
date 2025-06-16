package com.intarea.intarea.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    private String loginId;

    @Setter
    private String password;

    @Setter
    private String name;

    @Setter
    private String phone;

    @Setter
    private String email;

    @Setter
    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Setter
    @OneToMany(mappedBy = "users" ,cascade = CascadeType.ALL)
    private List<Material> materialList;

    @Setter
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Orders> ordersList;

    @Setter
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<CanceledOrder> canceledOrderList;

    @Setter
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<ProcessA> processAList;

    @Setter
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<ProcessB> processBList;

    @Setter
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Notices> noticesList;


    public void update(String password, String phone, String email, Grade grade, String name) {
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.grade = grade;
        this.name = name;
    }

}
