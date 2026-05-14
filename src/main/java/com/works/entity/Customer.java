package com.works.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cid;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String surname;

    @Column(unique = true, length = 200)
    private String email;

    @Column(unique = true, length = 15)
    private String phone;

    private boolean enabled;

    @Column(length = 1000)
    private String password;

}