package com.works.controller;

import com.works.dto.EventCreateRequestDto;
import com.works.dto.EventResponseDto;
import com.works.dto.EventUpdateRequestDto;
import com.works.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventRestController {

    private final EventService eventService;

    // Etkinlik Ekleme
    @PostMapping("/save")
    public ResponseEntity save(@Valid @RequestBody EventCreateRequestDto dto) {
        return eventService.save(dto);
    }

    // ID ile Etkinlik Detayı Getirme
    @GetMapping("/detail/{eid}")
    public ResponseEntity getById(@PathVariable Integer eid) {
        return eventService.getById(eid);
    }

    // Toplu Etkinlik Ekleme
    @PostMapping("/saveAll")
    public ResponseEntity saveAll(@Valid @RequestBody List<EventCreateRequestDto> dtos) {
        return eventService.saveAll(dtos);
    }

    // ID ile Etkinlik Silme
    @DeleteMapping("/delete/{eid}")
    public ResponseEntity delete(@PathVariable Integer eid) {
        return eventService.deleteOne(eid);
    }

    // Sayfalı Listeleme (Örn: /event/list?page=0)
    @GetMapping("/list")
    public Page<EventResponseDto> list(@RequestParam(defaultValue = "0") int page) {
        return eventService.list(page);
    }

    // Arama Metodu (Örn: /event/search?q=konser&page=0&daySort=asc)
    @GetMapping("/search")
    public Page<EventResponseDto> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "asc") String daySort
    )
    {
        return eventService.search(q, page, daySort);
    }

    // Etkinlik Güncelleme
    @PutMapping("/update")
    public ResponseEntity update(@Valid @RequestBody EventUpdateRequestDto dto) {
        return eventService.update(dto);
    }
}