package com.works.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pid;

    // Etkinliğe katılan kullanıcı
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Katılınan etkinlik
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}