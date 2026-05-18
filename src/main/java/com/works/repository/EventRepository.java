package com.works.repository;

import com.works.entity.Event;
import com.works.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    // 1. Sadece belirli bir durumda olan (örneğin PUBLISHED) etkinlikleri sayfalı getirmek için
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    // 2. Belirli bir durumda olan etkinlikler içinde isim VEYA açıklamaya göre kelime araması yapmak için
    Page<Event> findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            EventStatus status1, String name, EventStatus status2, String description, Pageable pageable);

    // 3. Etkinlik sahibinin kendi oluşturduğu etkinlikleri görmesi için (cid = Customer ID)
    List<Event> findByOwnerCid(Integer cid);
}