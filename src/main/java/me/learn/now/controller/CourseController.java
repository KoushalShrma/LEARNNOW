package me.learn.now.controller;

import me.learn.now.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private GroqService groqService;

    /**
     * Generate AI-powered subtopics for a course
     * POST /api/courses/generate-subtopics
     */
    @PostMapping("/generate-subtopics")
    public ResponseEntity<SubtopicsResponse> generateSubtopics(@RequestBody SubtopicsRequest request) {
        try {
            List<String> subtopics = groqService.generateSubtopics(
                request.getTopicName(),
                request.getPurpose(),
                request.getLevel()
            );

            return ResponseEntity.ok(new SubtopicsResponse(subtopics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SubtopicsResponse(List.of("Error generating subtopics: " + e.getMessage())));
        }
    }

    // DTOs
    public static class SubtopicsRequest {
        private String topicName;
        private String purpose;
        private String level;

        public String getTopicName() {
            return topicName;
        }

        public void setTopicName(String topicName) {
            this.topicName = topicName;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }

    public static class SubtopicsResponse {
        private List<String> subtopics;

        public SubtopicsResponse(List<String> subtopics) {
            this.subtopics = subtopics;
        }

        public List<String> getSubtopics() {
            return subtopics;
        }

        public void setSubtopics(List<String> subtopics) {
            this.subtopics = subtopics;
        }
    }
}
