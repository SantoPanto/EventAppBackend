package com.works.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequestDto {

    @NotNull(message = "Güncellenecek etkinliğin ID'si (eid) boş olamaz!")
    private Integer eid;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 150)
    private String name;

    @NotNull
    @FutureOrPresent(message = "Etkinlik tarihi geçmiş bir tarih olamaz")
    private LocalDate date;

    @NotNull
    private LocalTime time;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 250)
    private String location;

    @NotNull
    @NotEmpty
    @Size(min = 10, max = 1000)
    private String description;

    @NotNull
    @NotEmpty
    @Size(min = 2, max = 100)
    private String category;
}