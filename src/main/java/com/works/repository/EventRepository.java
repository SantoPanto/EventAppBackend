package com.works.repository;

import com.works.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {

    // Etkinlik adında veya açıklamasında geçen kelimeye göre arama yapar (Büyük/küçük harf duyarsız)
    Page<Event> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
}