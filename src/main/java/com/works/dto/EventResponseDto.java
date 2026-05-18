package com.works.dto;

import com.works.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

    private Integer eid;
    private String name;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private String description;
    private String category;

    //Etkinlik Durumu
    private EventStatus status;

    // Etkinlik Sahibi Bilgileri (Flattening)
    // ModelMapper, Event entity'si içindeki "owner" nesnesine bakıp,
    // onun içindeki "cid", "name", "surname" alanlarını otomatik olarak buraya kopyalayacaktır.
    private Integer ownerCid;
    private String ownerName;
    private String ownerSurname;
}