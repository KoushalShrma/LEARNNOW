package me.learn.now.controller;

import me.learn.now.model.ProgressStatus;
import me.learn.now.model.Topic;
import me.learn.now.model.Video;
import me.learn.now.repository.TopicRepo;
import me.learn.now.repository.VideoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progress Controller for Clerk users (string IDs)
 * Uses in-memory storage for progress tracking to minimize API calls
 * Progress is stored per-session and persisted to localStorage on frontend
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    @Autowired
    private TopicRepo topicRepo;
    
    @Autowired
    private VideoRepo videoRepo;

    // In-memory progress storage: userId -> topicId -> progress data
    private static final Map<String, Map<Long, CourseProgress>> userProgressCache = new ConcurrentHashMap<>();

    /**
     * Get progress for a specific course
     * GET /api/progress/{userId}/course/{topicId}
     */
    @GetMapping("/{userId}/course/{topicId}")
    public ResponseEntity<CourseProgress> getCourseProgress(
            @PathVariable String userId,
            @PathVariable Long topicId) {
        
        CourseProgress progress = getOrCreateProgress(userId, topicId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Get all progress for a user
     * GET /api/progress/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<Long, CourseProgress>> getAllUserProgress(@PathVariable String userId) {
        Map<Long, CourseProgress> userProgress = userProgressCache.getOrDefault(userId, new HashMap<>());
        return ResponseEntity.ok(userProgress);
    }

    /**
     * Update video completion
     * POST /api/progress/{userId}/course/{topicId}/video/{videoIndex}
     */
    @PostMapping("/{userId}/course/{topicId}/video/{videoIndex}")
    public ResponseEntity<CourseProgress> markVideoComplete(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @PathVariable int videoIndex) {
        
        CourseProgress progress = getOrCreateProgress(userId, topicId);
        
        // Mark video as completed
        if (!progress.completedVideos.contains(videoIndex)) {
            progress.completedVideos.add(videoIndex);
        }
        
        // Get total videos for this topic
        int totalVideos = videoRepo.findByTopicIdOrderByPositionAsc(topicId).size();
        if (totalVideos == 0) totalVideos = 1; // Avoid division by zero
        
        // Update progress percentage
        progress.progressPercentage = (progress.completedVideos.size() * 100) / totalVideos;
        progress.currentVideoIndex = videoIndex + 1;
        progress.totalVideos = totalVideos;
        progress.lastUpdated = System.currentTimeMillis();
        
        // Update status
        if (progress.progressPercentage >= 100) {
            progress.status = "COMPLETED";
        } else if (progress.progressPercentage > 0) {
            progress.status = "IN_PROGRESS";
        }
        
        // Save to cache
        saveProgress(userId, topicId, progress);
        
        System.out.println("[Progress] User " + userId + " completed video " + videoIndex + 
                         " for course " + topicId + " - Progress: " + progress.progressPercentage + "%");
        
        return ResponseEntity.ok(progress);
    }

    /**
     * Set current video index (when user navigates)
     * PUT /api/progress/{userId}/course/{topicId}/current-video
     */
    @PutMapping("/{userId}/course/{topicId}/current-video")
    public ResponseEntity<CourseProgress> setCurrentVideo(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @RequestBody Map<String, Integer> body) {
        
        Integer videoIndex = body.get("videoIndex");
        if (videoIndex == null) {
            return ResponseEntity.badRequest().build();
        }
        
        CourseProgress progress = getOrCreateProgress(userId, topicId);
        progress.currentVideoIndex = videoIndex;
        progress.lastUpdated = System.currentTimeMillis();
        
        // Start progress if first interaction
        if (progress.status.equals("NOT_STARTED")) {
            progress.status = "IN_PROGRESS";
        }
        
        saveProgress(userId, topicId, progress);
        return ResponseEntity.ok(progress);
    }

    /**
     * Add watch time
     * POST /api/progress/{userId}/course/{topicId}/watch-time
     */
    @PostMapping("/{userId}/course/{topicId}/watch-time")
    public ResponseEntity<CourseProgress> addWatchTime(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @RequestBody Map<String, Integer> body) {
        
        Integer seconds = body.get("seconds");
        if (seconds == null || seconds < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        CourseProgress progress = getOrCreateProgress(userId, topicId);
        progress.totalWatchTimeSeconds += seconds;
        progress.lastUpdated = System.currentTimeMillis();
        
        // Start progress if first interaction
        if (progress.status.equals("NOT_STARTED")) {
            progress.status = "IN_PROGRESS";
        }
        
        saveProgress(userId, topicId, progress);
        return ResponseEntity.ok(progress);
    }

    /**
     * Bulk sync progress from frontend localStorage
     * POST /api/progress/{userId}/sync
     */
    @PostMapping("/{userId}/sync")
    public ResponseEntity<Map<String, Object>> syncProgress(
            @PathVariable String userId,
            @RequestBody Map<Long, CourseProgress> progressData) {
        
        userProgressCache.put(userId, new ConcurrentHashMap<>(progressData));
        
        Map<String, Object> response = new HashMap<>();
        response.put("synced", true);
        response.put("coursesCount", progressData.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics
     * GET /api/progress/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStats> getUserStats(@PathVariable String userId) {
        Map<Long, CourseProgress> userProgress = userProgressCache.getOrDefault(userId, new HashMap<>());
        
        UserStats stats = new UserStats();
        stats.totalCourses = userProgress.size();
        stats.completedCourses = (int) userProgress.values().stream()
                .filter(p -> "COMPLETED".equals(p.status))
                .count();
        stats.inProgressCourses = (int) userProgress.values().stream()
                .filter(p -> "IN_PROGRESS".equals(p.status))
                .count();
        stats.totalWatchTimeMinutes = userProgress.values().stream()
                .mapToInt(p -> p.totalWatchTimeSeconds / 60)
                .sum();
        stats.averageProgress = userProgress.isEmpty() ? 0 :
                userProgress.values().stream()
                        .mapToInt(p -> p.progressPercentage)
                        .sum() / userProgress.size();
        
        return ResponseEntity.ok(stats);
    }

    // Helper methods
    private CourseProgress getOrCreateProgress(String userId, Long topicId) {
        Map<Long, CourseProgress> userCourses = userProgressCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        
        return userCourses.computeIfAbsent(topicId, k -> {
            CourseProgress newProgress = new CourseProgress();
            newProgress.topicId = topicId;
            newProgress.userId = userId;
            newProgress.status = "NOT_STARTED";
            newProgress.progressPercentage = 0;
            newProgress.currentVideoIndex = 0;
            newProgress.completedVideos = new HashSet<>();
            newProgress.totalWatchTimeSeconds = 0;
            newProgress.lastUpdated = System.currentTimeMillis();
            
            // Get total videos
            newProgress.totalVideos = videoRepo.findByTopicIdOrderByPositionAsc(topicId).size();
            
            return newProgress;
        });
    }

    private void saveProgress(String userId, Long topicId, CourseProgress progress) {
        Map<Long, CourseProgress> userCourses = userProgressCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userCourses.put(topicId, progress);
    }

    // DTOs
    public static class CourseProgress {
        public Long topicId;
        public String userId;
        public String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
        public int progressPercentage;
        public int currentVideoIndex;
        public int totalVideos;
        public Set<Integer> completedVideos = new HashSet<>();
        public int totalWatchTimeSeconds;
        public long lastUpdated;
    }

    public static class UserStats {
        public int totalCourses;
        public int completedCourses;
        public int inProgressCourses;
        public int totalWatchTimeMinutes;
        public int averageProgress;
    }
}
