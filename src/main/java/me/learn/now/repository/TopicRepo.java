package me.learn.now.repository;

import me.learn.now.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.List;

// CHANGED: Extend JpaRepository to get CRUD methods for Topic
// extends JpaRepository<Topic, Long> → Spring Data repository with Long primary key
public interface TopicRepo extends JpaRepository<Topic, Long> {
    
    // Get video count for each topic using a native SQL query to avoid N+1 problem
    @Query(value = "SELECT t_id as topicId, COUNT(*) as count FROM video GROUP BY t_id", nativeQuery = true)
    List<Object[]> findVideoCountsByTopic();
}
