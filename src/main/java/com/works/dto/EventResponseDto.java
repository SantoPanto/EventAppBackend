package com.works.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

    private Integer eid; // Veritabanındaki ID (İstemcinin detay/güncelleme işlemleri için gereklidir)

    private String name;

    private LocalDate date;

    private LocalTime time;

    private String location;

    private String description;

    private String category;
}
