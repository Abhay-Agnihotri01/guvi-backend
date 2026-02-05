package com.echotruth.repository;

import com.echotruth.model.ApiKey;
import com.echotruth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByUserAndIsActiveTrue(User user);
    Long countByUser(User user);
}
