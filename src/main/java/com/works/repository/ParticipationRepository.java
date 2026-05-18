package com.works.repository;

import com.works.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Integer> {
    // Belirli bir etkinliğe katılanların listesi
    List<Participation> findByEventEid(Integer eid);

    // Bir kullanıcının bir etkinliğe mükerrer (çift) katılmasını engellemek için kontrol sorgusu
    boolean existsByCustomerCidAndEventEid(Integer cid, Integer eid);
}