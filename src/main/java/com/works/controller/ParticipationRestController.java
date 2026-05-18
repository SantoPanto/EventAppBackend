package com.works.controller;

import com.works.service.ParticipationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/participation")
@RequiredArgsConstructor
public class ParticipationRestController {

    private final ParticipationService participationService;

    // Etkinliğe Katılma Uç Noktası
    // Örn: POST http://localhost:8080/participation/join/5
    @PostMapping("/join/{eventId}")
    public ResponseEntity joinEvent(@PathVariable Integer eventId, HttpServletRequest request) {
        return participationService.joinEvent(eventId, request);
    }

    // Etkinliğe Katılanları Listeleme Uç Noktası (Sadece Etkinlik Sahibi İçin)
    // Örn: GET http://localhost:8080/participation/list/5
    @GetMapping("/list/{eventId}")
    public ResponseEntity getParticipants(@PathVariable Integer eventId, HttpServletRequest request) {
        return participationService.getParticipants(eventId, request);
    }
}