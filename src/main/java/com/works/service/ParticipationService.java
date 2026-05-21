package com.works.service;

import com.works.dto.CustomerResponseDto;
import com.works.entity.Customer;
import com.works.entity.Event;
import com.works.entity.EventStatus;
import com.works.entity.Participation;
import com.works.repository.CustomerRepository;
import com.works.repository.EventRepository;
import com.works.repository.ParticipationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper model;

    // YARDIMCI METOT: O an Session'da login olan kullanıcıyı bulur
    private Customer getLoggedInCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object sessionUser = session.getAttribute("customer");
        if (sessionUser == null) return null;

        // Session'daki DTO'yu okuyup ID'sine göre gerçek Customer nesnesini veritabanından çekiyoruz
        CustomerResponseDto cachedDto = model.map(sessionUser, CustomerResponseDto.class);
        return customerRepository.findById(cachedDto.getCid()).orElse(null);
    }

    // 1. Etkinliğe Katılma (Join Event)
    public ResponseEntity joinEvent(Integer eventId, HttpServletRequest request) {
        Customer loggedInCustomer = getLoggedInCustomer(request);
        if (loggedInCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Lütfen önce giriş yapınız."));
        }

        // Etkinlik gerçekten var mı?
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Etkinlik bulunamadı!"));
        }

        Event event = optionalEvent.get();

        //  Sadece YAYINDA olan etkinliklere katılınabilir
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Sadece yayında olan etkinliklere katılabilirsiniz."));
        }

        //  Kullanıcı kendi etkinliğine katılamaz
        if (event.getOwner().getCid().equals(loggedInCustomer.getCid())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kendi oluşturduğunuz etkinliğe katılamazsınız!"));
        }

        //  Daha önce katılmış mı?
        boolean isAlreadyJoined = participationRepository.existsByCustomerCidAndEventEid(loggedInCustomer.getCid(), eventId);
        if (isAlreadyJoined) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Bu etkinliğe zaten katıldınız."));
        }

        // Tüm kuralları geçtiyse katılımı sağla ve kaydet
        Participation participation = new Participation();
        participation.setCustomer(loggedInCustomer);
        participation.setEvent(event);
        participationRepository.save(participation);

        return ResponseEntity.ok().body(Map.of("success", true, "message", "Etkinliğe başarıyla katıldınız!"));
    }

    //  Katılımcıları Listeleme (Sadece Etkinlik Sahibi Görebilir)
    public ResponseEntity getParticipants(Integer eventId, HttpServletRequest request) {
        Customer loggedInCustomer = getLoggedInCustomer(request);
        if (loggedInCustomer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Lütfen önce giriş yapınız."));
        }

        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Etkinlik bulunamadı!"));
        }

        Event event = optionalEvent.get();

        //  YETKİ KONTROLÜ: İstek atan kişi bu etkinliğin sahibi mi?
        if (!event.getOwner().getCid().equals(loggedInCustomer.getCid())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Yetkisiz işlem! Sadece etkinliği oluşturan kişi katılımcı listesini görebilir."));
        }

        // Katılımcıları veritabanından çekiyoruz
        List<Participation> participations = participationRepository.findByEventEid(eventId);

        // Şifre vb. bilgileri gizlemek için Entity nesnelerini DTO nesnelerine çeviriyoruz
        List<CustomerResponseDto> participantDtos = participations.stream()
                .map(p -> model.map(p.getCustomer(), CustomerResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(participantDtos);
    }

    // Katılımcı Sayısını Getir (Herkese Açık)
    public ResponseEntity<?> getParticipantCount(Integer eventId) {
        // Zaten var olan findByEventEid metodunu kullanarak sayıyı alıyoruz
        List<Participation> list = participationRepository.findByEventEid(eventId);
        return ResponseEntity.ok().body(Map.of("success", true, "count", list.size()));
    }
}