package me.learn.now.controller;

import me.learn.now.dto.dashboard.ActivityDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.learn.now.dto.dashboard.RecommendationDTO;
import me.learn.now.dto.dashboard.UserStatsDTO;
import me.learn.now.model.UserProgress;
import me.learn.now.service.DashboardService;
import me.learn.now.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private ObjectMapper objectMapper;

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

    // Endpoint to track course actions or create progress (single mapping to avoid collisions)
    @PostMapping("/progress")
    public ResponseEntity<?> trackOrCreateProgress(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> body = payload != null ? payload : Map.of();
        try {
            boolean hasCourseActionData = body.containsKey("action") || body.containsKey("topicId");
            boolean onlyActionFields = body.keySet().stream().allMatch(key -> Set.of("action", "topicId").contains(key));
            if (hasCourseActionData && onlyActionFields) {
                return ResponseEntity.ok().build();
            }
            UserProgress input = objectMapper.convertValue(body, UserProgress.class);
            return ResponseEntity.ok(userProgressService.create(userId, input));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process progress request: " + e.getMessage());
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
}
