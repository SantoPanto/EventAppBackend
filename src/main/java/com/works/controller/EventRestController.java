package com.works.controller;

import com.works.dto.EventCreateRequestDto;
import com.works.dto.EventResponseDto;
import com.works.dto.EventUpdateRequestDto;
import com.works.entity.EventStatus;
import com.works.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
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

    // Tekli Etkinlik Ekleme (Oturum açan kullanıcı otomatik "Owner" olur)
    @PostMapping("/save")
    public ResponseEntity save(@Valid @RequestBody EventCreateRequestDto dto, HttpServletRequest request) {
        return eventService.save(dto, request);
    }

    // Toplu Etkinlik Ekleme (Testler ve dummy data yüklemek için)
    @PostMapping("/saveAll")
    public ResponseEntity saveAll(@Valid @RequestBody List<EventCreateRequestDto> dtos, HttpServletRequest request) {
        return eventService.saveAll(dtos, request);
    }

    // Sayfalı Listeleme (Yalnızca PUBLISHED olan, yani yayındaki etkinlikleri getirir)
    // Örn: GET /event/list?page=0
    @GetMapping("/list")
    public Page<EventResponseDto> list(@RequestParam(defaultValue = "0") int page) {
        return eventService.list(page);
    }

    // 🔍 4. Sayfalı ve Sıralamalı Arama (Yalnızca PUBLISHED olanlar içinde isim/açıklama arar)
    // Örn: GET /event/search?q=Java&page=0&daySort=asc
    @GetMapping("/search")
    public Page<EventResponseDto> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "asc") String daySort
    ) {
        return eventService.search(q, page, daySort);
    }

    //  Etkinlik Düzenleme ( Yalnızca etkinlik sahibi güncelleyebilir)
    @PutMapping("/update")
    public ResponseEntity update(@Valid @RequestBody EventUpdateRequestDto dto, HttpServletRequest request) {
        return eventService.update(dto, request);
    }

    // Etkinlik Silme ( Yalnızca etkinlik sahibi silebilir)
    @DeleteMapping("/delete/{eid}")
    public ResponseEntity delete(@PathVariable Integer eid, HttpServletRequest request) {
        return eventService.deleteOne(eid, request);
    }

    //  Etkinlik Durumu Güncelleme (Publish / Unpublish / Archive)
    //  Yalnızca etkinlik sahibi durumu değiştirebilir. Gelecekteki etkinlik arşivlenemez.
    // Örn: PUT /event/updateStatus/1?status=ARCHIVED veya UNPUBLISHED veya PUBLISHED
    @PutMapping("/updateStatus/{eid}")
    public ResponseEntity updateStatus(
            @PathVariable Integer eid,
            @RequestParam EventStatus status,
            HttpServletRequest request
    ) {
        return eventService.updateStatus(eid, status, request);
    }

    //  Sahibin Kendi Etkinlikleri (Giriş yapan kullanıcının oluşturduğu tüm durumdaki etkinlikler)
    // Katılım takibi ve yönetim ekranı için zorunlu gereksinimdir.
    @GetMapping("/my-events")
    public ResponseEntity getMyCreatedEvents(HttpServletRequest request) {
        return eventService.getMyCreatedEvents(request);
    }

    // Etkinlik Detayı (Get Detail)
    // Örn: GET http://localhost:8080/event/detail/3
    @GetMapping("/detail/{eid}")
    public ResponseEntity getDetail(@PathVariable Integer eid) {
        return eventService.getDetail(eid);
    }
}