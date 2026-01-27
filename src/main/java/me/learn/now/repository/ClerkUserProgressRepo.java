package me.learn.now.repository;

import me.learn.now.model.ClerkUserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClerkUserProgressRepo extends JpaRepository<ClerkUserProgress, Long> {
    
    Optional<ClerkUserProgress> findByClerkUserIdAndTopicId(String clerkUserId, Long topicId);
    
    List<ClerkUserProgress> findByClerkUserId(String clerkUserId);
    
    List<ClerkUserProgress> findByClerkUserIdAndStatus(String clerkUserId, me.learn.now.model.ProgressStatus status);
    
    boolean existsByClerkUserIdAndTopicId(String clerkUserId, Long topicId);
}
