package me.learn.now.controller;

import me.learn.now.dto.dashboard.ActivityDTO;
import me.learn.now.dto.dashboard.RecommendationDTO;
import me.learn.now.dto.dashboard.UserStatsDTO;
import me.learn.now.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Long userId) {
        UserStatsDTO stats = dashboardService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDTO>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ActivityDTO> activities = dashboardService.getUserActivity(userId, limit);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDTO>> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int limit) {
        List<RecommendationDTO> recommendations = dashboardService.getRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    // Endpoint to track when a user starts or resumes a course
    @PostMapping(value = "/progress", consumes = "application/vnd.learnnow.course-action+json")
    public ResponseEntity<?> trackCourseAction(
            @PathVariable Long userId,
            @RequestBody CourseActionRequest request) {
        try {
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to track course action: " + e.getMessage());
        }
    }

    // Endpoint to track when a user starts a challenge
    @PostMapping("/challenges/start")
    public ResponseEntity<?> startChallenge(
            @PathVariable Long userId,
            @RequestBody ChallengeStartRequest request) {
        try {
            // In a real implementation, this would set up a challenge for the user
            // For now, we'll just return success
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to start challenge: " + e.getMessage());
        }
    }

    public static class ChallengeStartRequest {
        private Long challengeId;

        public Long getChallengeId() {
            return challengeId;
        }

        public void setChallengeId(Long challengeId) {
            this.challengeId = challengeId;
        }
    }

    public static class CourseActionRequest {
        private Long topicId;
        private String action;

        public Long getTopicId() {
            return topicId;
        }

        public void setTopicId(Long topicId) {
            this.topicId = topicId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
