package com.works.service;

import com.works.dto.EventCreateRequestDto;
import com.works.dto.EventResponseDto;
import com.works.dto.EventUpdateRequestDto;
import com.works.entity.Event;
import com.works.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper model;

    // Etkinlik Kaydetme (Create)
    @CacheEvict(cacheNames = "productListCache", allEntries = true)
    public ResponseEntity save(EventCreateRequestDto eventCreateRequestDto) {

        // --- YENİ EKLENEN İŞ KURALI (BUSINESS RULE) ---
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Eğer tarih bugünse VE saat şu andan önceyse kaydetmeye izin verme!
        if (eventCreateRequestDto.getDate().isEqual(today) && eventCreateRequestDto.getTime().isBefore(now)) {
            Map<String, Object> hm = Map.of(
                    "success", false,
                    "message", "Bugünün tarihini seçtiyseniz, geçmiş bir saat giremezsiniz!"
            );
            return ResponseEntity.badRequest().body(hm);
        }
        // ----------------------------------------------

        Event event = model.map(eventCreateRequestDto, Event.class);
        eventRepository.save(event);
        EventResponseDto responseDto = model.map(event, EventResponseDto.class);
        return ResponseEntity.ok().body(responseDto);
    }

    // Toplu Etkinlik Kaydetme (Save All)
    @CacheEvict(cacheNames = "productListCache", allEntries = true)
    public ResponseEntity saveAll(List<EventCreateRequestDto> eventCreateRequestDtos) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Listedeki herhangi bir etkinlik bu kuralı ihlal ediyorsa tüm işlemi reddet
        for (EventCreateRequestDto dto : eventCreateRequestDtos) {
            if (dto.getDate().isEqual(today) && dto.getTime().isBefore(now)) {
                Map<String, Object> hm = Map.of(
                        "success", false,
                        "message", "Toplu kayıt başarısız: '" + dto.getName() + "' etkinliği için bugünün geçmiş bir saatini giremezsiniz!"
                );
                return ResponseEntity.badRequest().body(hm);
            }
        }

        // ... mevcut saveAll kodlarınızın geri kalanı ...
        // DTO listesini Entity listesine çeviriyoruz
        List<Event> eventList = eventCreateRequestDtos.stream()
                .map(dto -> model.map(dto, Event.class))
                .collect(Collectors.toList());

        // Veritabanına toplu kayıt işlemi
        List<Event> savedEvents = eventRepository.saveAll(eventList);

        // Kaydedilen verileri güvenli Response DTO listesine çeviriyoruz
        List<EventResponseDto> responseDtos = savedEvents.stream()
                .map(event -> model.map(event, EventResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(responseDtos);
    }

    // Tüm Etkinlikleri Listeleme (List)
    @CacheEvict(cacheNames = "productListCache", allEntries = true)
    public Page<EventResponseDto> list(int page) {
        // Varsayılan olarak en yeni etkinlikten en eskiye sıralıyoruz (eid'ye göre DESC)
        Sort sort = Sort.by(Sort.Direction.DESC, "eid");
        Pageable pageable = PageRequest.of(page, 10, sort);

        return eventRepository.findAll(pageable)
                .map(event -> model.map(event, EventResponseDto.class));
    }

    // Sayfalı ve Sıralamalı Arama
    public Page<EventResponseDto> search(String q, int page, String daySort) {
        // daySort parametresine göre tarih sıralamasını (asc/desc) belirliyoruz
        Sort sort = daySort.equalsIgnoreCase("asc") ?
                Sort.by("date").ascending() :
                Sort.by("date").descending();

        Pageable pageable = PageRequest.of(page, 10, sort);

        return eventRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable)
                .map(event -> model.map(event, EventResponseDto.class));
    }

    // ID ile Etkinlik Bulma (Detail)
    public ResponseEntity getById(Integer eid) {
        return eventRepository.findById(eid)
                .map(event -> ResponseEntity.ok().body(model.map(event, EventResponseDto.class)))
                .orElse(ResponseEntity.badRequest().build());
    }

    // ID ile Etkinlik Silme (Delete)
    @CacheEvict(cacheNames = "eventListCache", allEntries = true)
    public ResponseEntity deleteOne(Integer eid) {
        Optional<Event> optionalEvent = eventRepository.findById(eid);
        if(optionalEvent.isPresent()) {
            eventRepository.deleteById(eid);
            Map<String, Object> hm = Map.of("success", true, "message", "Event deleted successfully.");
            return ResponseEntity.ok().body(hm);
        } else {
            Map<String, Object> hm = Map.of("success", false, "message", "Event not found id: " + eid);
            return ResponseEntity.status(404).body(hm);
        }
    }

    // Etkinlik Güncelleme (Update)
    @CacheEvict(cacheNames = "eventListCache", allEntries = true)
    public ResponseEntity update(EventUpdateRequestDto eventUpdateRequestDto) {
        // 1. Veritabanında bu ID'ye sahip bir etkinlik var mı?
        Optional<Event> optionalEvent = eventRepository.findById(eventUpdateRequestDto.getEid());

        if (optionalEvent.isPresent()) {

            // --- GÜVENLİK KURALI: Geçmiş saat kontrolü ---
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            if (eventUpdateRequestDto.getDate().isEqual(today) && eventUpdateRequestDto.getTime().isBefore(now)) {
                Map<String, Object> hm = Map.of("success", false, "message", "Bugünün tarihini seçtiyseniz, geçmiş bir saat giremezsiniz!");
                return ResponseEntity.badRequest().body(hm);
            }
            // ----------------------------------------------

            // DTO'yu Entity'ye çevirip veritabanında üzerine yazıyoruz
            Event event = model.map(eventUpdateRequestDto, Event.class);
            eventRepository.save(event);

            Map<String, Object> hm = Map.of("success", true, "message", "Event updated successfully.");
            return ResponseEntity.ok().body(hm);

        } else {
            // Kayıt bulunamazsa 404 dön
            Map<String, Object> hm = Map.of("success", false, "message", "Event not found id: " + eventUpdateRequestDto.getEid());
            return ResponseEntity.status(404).body(hm);
        }
    }
}