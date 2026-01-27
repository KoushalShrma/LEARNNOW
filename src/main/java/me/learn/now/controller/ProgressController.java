package me.learn.now.controller;

import me.learn.now.model.ClerkUserProgress;
import me.learn.now.model.ProgressStatus;
import me.learn.now.repository.ClerkUserProgressRepo;
import me.learn.now.repository.VideoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Progress Controller for Clerk users (string IDs)
 * Uses database-backed storage for persistent progress tracking
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    @Autowired
    private ClerkUserProgressRepo progressRepo;
    
    @Autowired
    private VideoRepo videoRepo;

    /**
     * Get progress for a specific course
     * GET /api/progress/{userId}/course/{topicId}
     */
    @GetMapping("/{userId}/course/{topicId}")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @PathVariable String userId,
            @PathVariable Long topicId) {
        
        ClerkUserProgress progress = getOrCreateProgress(userId, topicId);
        return ResponseEntity.ok(toDTO(progress));
    }

    /**
     * Get all progress for a user
     * GET /api/progress/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<Long, CourseProgressDTO>> getAllUserProgress(@PathVariable String userId) {
        List<ClerkUserProgress> userProgressList = progressRepo.findByClerkUserId(userId);
        
        Map<Long, CourseProgressDTO> result = userProgressList.stream()
                .collect(Collectors.toMap(
                        ClerkUserProgress::getTopicId,
                        this::toDTO
                ));
        
        return ResponseEntity.ok(result);
    }

    /**
     * Update video completion
     * POST /api/progress/{userId}/course/{topicId}/video/{videoIndex}
     */
    @PostMapping("/{userId}/course/{topicId}/video/{videoIndex}")
    public ResponseEntity<CourseProgressDTO> markVideoComplete(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @PathVariable int videoIndex) {
        
        ClerkUserProgress progress = getOrCreateProgress(userId, topicId);
        
        // Add completed video
        progress.addCompletedVideo(videoIndex);
        
        // Get total videos for this topic
        int totalVideos = videoRepo.findByTopicIdOrderByPositionAsc(topicId).size();
        if (totalVideos == 0) totalVideos = 1; // Avoid division by zero
        
        // Update progress
        int completedCount = progress.getCompletedVideos().size();
        progress.setProgressPercentage((completedCount * 100) / totalVideos);
        progress.setCurrentVideoIndex(videoIndex + 1);
        progress.setTotalVideos(totalVideos);
        
        // Update status
        if (progress.getProgressPercentage() >= 100) {
            progress.setStatus(ProgressStatus.COMPLETED);
        } else if (progress.getProgressPercentage() > 0) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }
        
        // Save to database
        progress = progressRepo.save(progress);
        
        System.out.println("[Progress] User " + userId + " completed video " + videoIndex + 
                         " for course " + topicId + " - Progress: " + progress.getProgressPercentage() + 
                         "% (" + completedCount + "/" + totalVideos + " videos)");
        
        return ResponseEntity.ok(toDTO(progress));
    }

    /**
     * Set current video index (when user navigates)
     * PUT /api/progress/{userId}/course/{topicId}/current-video
     */
    @PutMapping("/{userId}/course/{topicId}/current-video")
    public ResponseEntity<CourseProgressDTO> setCurrentVideo(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @RequestBody Map<String, Integer> body) {
        
        Integer videoIndex = body.get("videoIndex");
        if (videoIndex == null) {
            return ResponseEntity.badRequest().build();
        }
        
        ClerkUserProgress progress = getOrCreateProgress(userId, topicId);
        progress.setCurrentVideoIndex(videoIndex);
        
        // Start progress if first interaction
        if (progress.getStatus() == ProgressStatus.NOT_STARTED) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }
        
        progress = progressRepo.save(progress);
        return ResponseEntity.ok(toDTO(progress));
    }

    /**
     * Add watch time
     * POST /api/progress/{userId}/course/{topicId}/watch-time
     */
    @PostMapping("/{userId}/course/{topicId}/watch-time")
    public ResponseEntity<CourseProgressDTO> addWatchTime(
            @PathVariable String userId,
            @PathVariable Long topicId,
            @RequestBody Map<String, Integer> body) {
        
        Integer seconds = body.get("seconds");
        if (seconds == null || seconds < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        ClerkUserProgress progress = getOrCreateProgress(userId, topicId);
        progress.setTotalWatchTimeSeconds(progress.getTotalWatchTimeSeconds() + seconds);
        
        // Start progress if first interaction
        if (progress.getStatus() == ProgressStatus.NOT_STARTED) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }
        
        progress = progressRepo.save(progress);
        return ResponseEntity.ok(toDTO(progress));
    }

    /**
     * Bulk sync progress from frontend localStorage
     * POST /api/progress/{userId}/sync
     */
    @PostMapping("/{userId}/sync")
    public ResponseEntity<Map<String, Object>> syncProgress(
            @PathVariable String userId,
            @RequestBody Map<Long, CourseProgressDTO> progressData) {
        
        int syncedCount = 0;
        for (Map.Entry<Long, CourseProgressDTO> entry : progressData.entrySet()) {
            Long topicId = entry.getKey();
            CourseProgressDTO dto = entry.getValue();
            
            ClerkUserProgress progress = getOrCreateProgress(userId, topicId);
            
            // Only update if frontend has more progress
            if (dto.completedVideos != null && dto.completedVideos.size() > progress.getCompletedVideos().size()) {
                progress.setCompletedVideos(new HashSet<>(dto.completedVideos));
                progress.setProgressPercentage(dto.progressPercentage);
                progress.setCurrentVideoIndex(dto.currentVideoIndex);
                progress.setTotalVideos(dto.totalVideos);
                
                if (dto.progressPercentage >= 100) {
                    progress.setStatus(ProgressStatus.COMPLETED);
                } else if (dto.progressPercentage > 0) {
                    progress.setStatus(ProgressStatus.IN_PROGRESS);
                }
                
                progressRepo.save(progress);
                syncedCount++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("synced", true);
        response.put("coursesCount", progressData.size());
        response.put("updatedCount", syncedCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics
     * GET /api/progress/{userId}/stats
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStats> getUserStats(@PathVariable String userId) {
        List<ClerkUserProgress> userProgressList = progressRepo.findByClerkUserId(userId);
        
        UserStats stats = new UserStats();
        stats.totalCourses = userProgressList.size();
        stats.completedCourses = (int) userProgressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();
        stats.inProgressCourses = (int) userProgressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.IN_PROGRESS)
                .count();
        stats.totalWatchTimeMinutes = userProgressList.stream()
                .mapToInt(p -> p.getTotalWatchTimeSeconds() / 60)
                .sum();
        stats.averageProgress = userProgressList.isEmpty() ? 0 :
                userProgressList.stream()
                        .mapToInt(ClerkUserProgress::getProgressPercentage)
                        .sum() / userProgressList.size();
        
        return ResponseEntity.ok(stats);
    }

    // Helper methods
    private ClerkUserProgress getOrCreateProgress(String userId, Long topicId) {
        return progressRepo.findByClerkUserIdAndTopicId(userId, topicId)
                .orElseGet(() -> {
                    ClerkUserProgress newProgress = new ClerkUserProgress();
                    newProgress.setClerkUserId(userId);
                    newProgress.setTopicId(topicId);
                    newProgress.setStatus(ProgressStatus.NOT_STARTED);
                    newProgress.setProgressPercentage(0);
                    newProgress.setCurrentVideoIndex(0);
                    newProgress.setTotalVideos(videoRepo.findByTopicIdOrderByPositionAsc(topicId).size());
                    return progressRepo.save(newProgress);
                });
    }

    private CourseProgressDTO toDTO(ClerkUserProgress progress) {
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.topicId = progress.getTopicId();
        dto.userId = progress.getClerkUserId();
        dto.status = progress.getStatus().name();
        dto.progressPercentage = progress.getProgressPercentage();
        dto.currentVideoIndex = progress.getCurrentVideoIndex();
        dto.totalVideos = progress.getTotalVideos();
        dto.completedVideos = progress.getCompletedVideos();
        dto.totalWatchTimeSeconds = progress.getTotalWatchTimeSeconds();
        dto.lastUpdated = progress.getUpdatedAt() != null ? 
                progress.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 
                System.currentTimeMillis();
        return dto;
    }

    // DTOs
    public static class CourseProgressDTO {
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
