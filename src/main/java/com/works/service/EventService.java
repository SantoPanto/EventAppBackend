package com.works.service;

import com.works.dto.CustomerResponseDto;
import com.works.dto.EventCreateRequestDto;
import com.works.dto.EventResponseDto;
import com.works.dto.EventUpdateRequestDto;
import com.works.entity.Customer;
import com.works.entity.Event;
import com.works.entity.EventStatus;
import com.works.repository.CustomerRepository;
import com.works.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private final CustomerRepository customerRepository; // Oturum açan kullanıcıyı doğrulamak için ekledik
    private final ModelMapper model;

    //  YARDIMCI METOT: O an Session'da login olan kullanıcıyı bulur
    private Customer getLoggedInCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object sessionUser = session.getAttribute("customer");
        if (sessionUser == null) return null;

        // Session'daki veriyi güvenli bir şekilde DTO'ya map edip ID'sini alıyoruz
        CustomerResponseDto cachedDto = model.map(sessionUser, CustomerResponseDto.class);
        return customerRepository.findById(cachedDto.getCid()).orElse(null);
    }

    //  Etkinlik Kaydetme (Create)
    public ResponseEntity save(EventCreateRequestDto eventCreateRequestDto, HttpServletRequest request) {
        // İş Kuralı: Geçmiş saat kontrolü
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (eventCreateRequestDto.getDate().isEqual(today) && eventCreateRequestDto.getTime().isBefore(now)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Bugünün tarihini seçtiyseniz, geçmiş bir saat giremezsiniz!"));
        }

        // Giriş yapan kullanıcıyı bul
        Customer loggedInCustomer = getLoggedInCustomer(request);
        if (loggedInCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Oturum bulunamadı!"));
        }

        Event event = model.map(eventCreateRequestDto, Event.class);
        event.setOwner(loggedInCustomer); // Etkinliğin sahibi o anki kullanıcı yapıldı
        event.setStatus(EventStatus.PUBLISHED); // Yeni etkinlik varsayılan olarak PUBLISHED (Yayında) başlar

        eventRepository.save(event);

        EventResponseDto responseDto = model.map(event, EventResponseDto.class);
        return ResponseEntity.ok().body(responseDto);
    }

    //  Toplu Etkinlik Kaydetme (Save All) - Testler için
    public ResponseEntity saveAll(List<EventCreateRequestDto> eventCreateRequestDtos, HttpServletRequest request) {
        Customer loggedInCustomer = getLoggedInCustomer(request);
        if (loggedInCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Oturum bulunamadı!"));
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Event> eventList = eventCreateRequestDtos.stream().map(dto -> {
            Event event = model.map(dto, Event.class);
            event.setOwner(loggedInCustomer);
            event.setStatus(EventStatus.PUBLISHED);
            return event;
        }).collect(Collectors.toList());

        List<Event> savedEvents = eventRepository.saveAll(eventList);
        List<EventResponseDto> responseDtos = savedEvents.stream()
                .map(event -> model.map(event, EventResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(responseDtos);
    }

    // 📄 Sayfalı Listeleme (Sadece PUBLISHED durumundaki açık etkinlikleri getirir)
    public Page<EventResponseDto> list(int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "eid");
        Pageable pageable = PageRequest.of(page, 10, sort);

        // Ziyaretçiler ve diğer kullanıcılar sadece YAYINDA olanları görebilir
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
                .map(event -> model.map(event, EventResponseDto.class));
    }

    // 🔍 Sayfalı ve Sıralamalı Arama (Yalnızca PUBLISHED olanlar içinde arar)
    public Page<EventResponseDto> search(String q, int page, String daySort) {
        Sort sort = daySort.equalsIgnoreCase("asc") ? Sort.by("date").ascending() : Sort.by("date").descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        return eventRepository.findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                        EventStatus.PUBLISHED, q, EventStatus.PUBLISHED, q, pageable)
                .map(event -> model.map(event, EventResponseDto.class));
    }

    // 📝 Etkinlik Güncelleme (Update - Sadece Sahibi Düzenleyebilir)
    public ResponseEntity update(EventUpdateRequestDto eventUpdateRequestDto, HttpServletRequest request) {
        Optional<Event> optionalEvent = eventRepository.findById(eventUpdateRequestDto.getEid());
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Etkinlik bulunamadı!"));
        }

        Event event = optionalEvent.get();
        Customer loggedInCustomer = getLoggedInCustomer(request);

        // 🛡️ YETKİ KONTROLÜ: İstek atan kişi bu etkinliğin sahibi mi?
        if (loggedInCustomer == null || !event.getOwner().getCid().equals(loggedInCustomer.getCid())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Yetkisiz işlem! Sadece kendi etkinliğinizi düzenleyebilirsiniz."));
        }

        // İş Kuralı: Zaman kontrolü
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (eventUpdateRequestDto.getDate().isEqual(today) && eventUpdateRequestDto.getTime().isBefore(now)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Geçmiş bir saat girilemez!"));
        }

        // Güncelleme işlemi
        model.map(eventUpdateRequestDto, event);
        event.setOwner(loggedInCustomer); // İlişkiyi koru
        eventRepository.save(event);

        return ResponseEntity.ok().body(Map.of("success", true, "message", "Event updated successfully."));
    }

    // ❌ Etkinlik Silme (Delete - Sadece Sahibi Silebilir)
    public ResponseEntity deleteOne(Integer eid, HttpServletRequest request) {
        Optional<Event> optionalEvent = eventRepository.findById(eid);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Etkinlik bulunamadı!"));
        }

        Event event = optionalEvent.get();
        Customer loggedInCustomer = getLoggedInCustomer(request);

        // 🛡️ YETKİ KONTROLÜ
        if (loggedInCustomer == null || !event.getOwner().getCid().equals(loggedInCustomer.getCid())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Yetkisiz işlem! Sadece kendi etkinliğinizi silebilirsiniz."));
        }

        eventRepository.deleteById(eid);
        return ResponseEntity.ok().body(Map.of("success", true, "message", "Event deleted successfully."));
    }

    // ⚙️ ENUM STATÜ GÜNCELLEME (Publish, Unpublish, Archive işlemlerini tek elden çözer)
    public ResponseEntity updateStatus(Integer eid, EventStatus newStatus, HttpServletRequest request) {
        Optional<Event> optionalEvent = eventRepository.findById(eid);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Etkinlik bulunamadı!"));
        }

        Event event = optionalEvent.get();
        Customer loggedInCustomer = getLoggedInCustomer(request);

        // 🛡️ YETKİ KONTROLÜ
        if (loggedInCustomer == null || !event.getOwner().getCid().equals(loggedInCustomer.getCid())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Yetkisiz işlem! Bu durum değişikliğini sadece etkinlik sahibi yapabilir."));
        }

        // İş Kuralı: Süresi geçmiş etkinliklerin arşivlenmesi kontrolü
        if (newStatus == EventStatus.ARCHIVED && event.getDate().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Gelecekteki bir etkinlik henüz süresi geçmediği için arşivlenemez!"));
        }

        event.setStatus(newStatus);
        eventRepository.save(event);
        return ResponseEntity.ok().body(Map.of("success", true, "message", "Etkinlik durumu güncellendi: " + newStatus.name()));
    }

    // 👤 SAHİBİN KENDİ ETKİNLİKLERİ: Giriş yapan kullanıcının oluşturduğu tüm etkinlikler
    public ResponseEntity getMyCreatedEvents(HttpServletRequest request) {
        Customer loggedInCustomer = getLoggedInCustomer(request);
        if (loggedInCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Lütfen önce giriş yapınız."));
        }

        List<Event> myEvents = eventRepository.findByOwnerCid(loggedInCustomer.getCid());
        List<EventResponseDto> dtoList = myEvents.stream()
                .map(event -> model.map(event, EventResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(dtoList);
    }

    // Etkinlik Detayı (Get Detail by ID)
    public ResponseEntity getDetail(Integer eid) {
        // ID'ye göre etkinliği veritabanından sorguluyoruz
        Optional<Event> optionalEvent = eventRepository.findById(eid);

        if (optionalEvent.isPresent()) {
            // Eğer etkinlik varsa, DTO nesnesine dönüştürüp 200 OK ile döndürüyoruz
            EventResponseDto responseDto = model.map(optionalEvent.get(), EventResponseDto.class);
            return ResponseEntity.ok().body(responseDto);
        } else {
            // Eğer o ID ile bir etkinlik yoksa, projenin standart hata formatında 404 dönüyoruz
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Etkinlik bulunamadı! ID: " + eid
            ));
        }
    }
}