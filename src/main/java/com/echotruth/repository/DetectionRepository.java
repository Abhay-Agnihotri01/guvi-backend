package com.echotruth.repository;

import com.echotruth.model.Detection;
import com.echotruth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, String> {
    Page<Detection> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<Detection> findByUserAndCreatedAtBetween(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    Long countByUser(User user);
    
    List<Detection> findByUserAndLanguage(User user, String language);
}
