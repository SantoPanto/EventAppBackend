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
    private Integer eid; // Customer'daki cid mantığına uygun olarak eid (Event ID)

    @Column(length = 150)
    private String name; // Etkinlik Adı

    private LocalDate date; // Tarih (Örn: 2026-05-22)

    private LocalTime time; // Saat (Örn: 14:30)

    @Column(length = 250)
    private String location; // Yer

    @Column(length = 1000)
    private String description; // Açıklama (Uzun olabileceği için 1000 karakter verdik)

    @Column(length = 100)
    private String category; // Kategori
}