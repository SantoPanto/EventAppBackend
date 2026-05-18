package com.works.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eid;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(length = 250, nullable = false)
    private String location;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(length = 100, nullable = false)
    private String category;

    // Etkinliği oluşturan kullanıcı ilişkisi (Sahibi)
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Customer owner;

    // Etkinliğin mevcut durumu (Varsayılan olarak YAYINDA başlar)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.PUBLISHED;
}