package me.learn.now.controller;

import me.learn.now.dto.youtube.YouTubeVideoDto;
import me.learn.now.service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hinglish: YouTube API ke through learning videos search karne ke liye controller
 * Yaha hum students ko YouTube se educational content provide karte hai
 */
@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*") // Frontend ke liye CORS enable kar diya
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;

    /**
     * Hinglish: Keyword ke basis pe YouTube se videos search karte hai
     * Example: /api/youtube/search?query=java&max=5
     */
    @GetMapping("/search")
    public ResponseEntity<List<YouTubeVideoDto>> searchVideos(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int max) {

        try {
            // Hinglish: YouTube service se videos fetch karte hai
            List<YouTubeVideoDto> videos = youTubeService.searchEducationalVideos(query, max);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            // Hinglish: error handle karte hai agar YouTube API fail ho jaye
            throw new RuntimeException("YouTube videos fetch karne mein problem: " + e.getMessage());
        }
    }

    /**
     * Hinglish: Popular programming channels se trending videos laate hai
     * Example: /api/youtube/popular?max=8
     */
    @GetMapping("/popular")
    public ResponseEntity<List<YouTubeVideoDto>> getPopularVideos(
            @RequestParam(defaultValue = "8") int max) {

        try {
            List<YouTubeVideoDto> videos = youTubeService.getPopularProgrammingVideos(max);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            throw new RuntimeException("Popular videos fetch karne mein problem: " + e.getMessage());
        }
    }

    /**
     * Hinglish: Specific topic ke liye curated videos
     * Example: /api/youtube/topic/java?max=6
     */
    @GetMapping("/topic/{topic}")
    public ResponseEntity<List<YouTubeVideoDto>> getVideosByTopic(
            @PathVariable String topic,
            @RequestParam(defaultValue = "6") int max) {

        try {
            List<YouTubeVideoDto> videos = youTubeService.getVideosByTopic(topic, max);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            throw new RuntimeException("Topic videos fetch karne mein problem: " + e.getMessage());
        }
    }

    /**
     * Hinglish: YouTube API health check - API working hai ya nahi
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            return ResponseEntity.ok("YouTube API is working fine!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("YouTube API is not working: " + e.getMessage());
        }
    }

    /**
     * Search and return the BEST video for a topic based on quality scoring
     * Example: /api/youtube/best?query=javascript functions
     */
    @GetMapping("/best")
    public ResponseEntity<YouTubeVideoDto> searchBestVideo(@RequestParam String query) {
        try {
            YouTubeVideoDto bestVideo = youTubeService.searchBestVideo(query);
            if (bestVideo != null) {
                return ResponseEntity.ok(bestVideo);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Best video fetch karne mein problem: " + e.getMessage());
        }
    }
    
    /**
     * Search and return TOP 3 best videos for a subtopic
     * Example: /api/youtube/best-multiple?query=react hooks&max=3
     */
    @GetMapping("/best-multiple")
    public ResponseEntity<List<YouTubeVideoDto>> searchBestVideos(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int max) {
        try {
            List<YouTubeVideoDto> bestVideos = youTubeService.searchBestVideos(query, max);
            return ResponseEntity.ok(bestVideos);
        } catch (Exception e) {
            throw new RuntimeException("Best videos fetch karne mein problem: " + e.getMessage());
        }
    }
}
