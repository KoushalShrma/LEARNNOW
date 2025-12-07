package me.learn.now.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.learn.now.model.ProgressStatus;
import me.learn.now.model.UserProgress;
import me.learn.now.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController // ye controller user ke progress related cheeze handle karega
@RequestMapping("/api/users/{userId}/progress")
public class UserProgressController {

    @Autowired
    private UserProgressService ups; // service jahan main logic rakha hai

    @Autowired
    private ObjectMapper objectMapper;

    // List: user ka saara progress
    @GetMapping
    public ResponseEntity<List<UserProgress>> list(@PathVariable Long userId){
        return ResponseEntity.ok(ups.listByUser(userId));
    }

    // Create: naya progress row
    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long userId, @RequestBody(required = false) Map<String, Object> payload){
        Map<String, Object> body = payload != null ? payload : Map.of();
        try {
            if (body.containsKey("action")) {
                return ResponseEntity.ok().build();
            }
            UserProgress input = objectMapper.convertValue(body, UserProgress.class);
            return ResponseEntity.ok(ups.create(userId, input));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process progress request: " + e.getMessage());
        }
    }

    // Get one progress (ownership check inside service)
    @GetMapping("/{progressId}")
    public ResponseEntity<UserProgress> get(@PathVariable Long userId, @PathVariable Long progressId){
        Optional<UserProgress> p = ups.getForUser(userId, progressId);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Patch: status/seconds/lastSeenAt
    @PatchMapping("/{progressId}")
    public ResponseEntity<UserProgress> patch(@PathVariable Long userId, @PathVariable Long progressId, @RequestBody UserProgress delta){
        return ResponseEntity.ok(ups.patch(userId, progressId, delta));
    }

    // Delete progress
    @DeleteMapping("/{progressId}")
    public ResponseEntity<UserProgress> delete(@PathVariable Long userId, @PathVariable Long progressId){
        Optional<UserProgress> p = ups.delete(userId, progressId);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Helper: get by topic
    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<UserProgress> getByTopic(@PathVariable Long userId, @PathVariable Long topicId){
        Optional<UserProgress> p = ups.getByTopic(userId, topicId);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Helper: update only status by topic (accept via query or simple JSON)
    @PutMapping("/by-topic/{topicId}/status")
    public ResponseEntity<UserProgress> setStatus(@PathVariable Long userId, @PathVariable Long topicId, @RequestBody(required = false) Map<String, String> body, @RequestParam(name = "value", required = false) String value){
        String statusStr = value != null ? value : (body != null ? body.get("value") : null);
        if (statusStr == null) throw new IllegalArgumentException("status value required");
        ProgressStatus status = ProgressStatus.valueOf(statusStr);
        return ResponseEntity.ok(ups.setStatusByTopic(userId, topicId, status));
    }

    // Helper: update only watch seconds by topic (accept via query or simple JSON)
    @PutMapping("/by-topic/{topicId}/watch-seconds")
    public ResponseEntity<UserProgress> setWatchSeconds(@PathVariable Long userId, @PathVariable Long topicId, @RequestBody(required = false) Map<String, Integer> body, @RequestParam(name = "value", required = false) Integer value){
        Integer seconds = value != null ? value : (body != null ? body.get("value") : null);
        if (seconds == null) throw new IllegalArgumentException("seconds value required");
        return ResponseEntity.ok(ups.setWatchSecondsByTopic(userId, topicId, seconds));
    }
}
