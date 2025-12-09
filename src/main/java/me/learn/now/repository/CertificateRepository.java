package me.learn.now.repository;

import me.learn.now.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    Optional<Certificate> findByUserIdAndCourseId(String userId, Long courseId);
    
    List<Certificate> findByUserIdOrderByIssuedAtDesc(String userId);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    boolean existsByUserIdAndCourseId(String userId, Long courseId);
}
