package me.learn.now.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    public GroqService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.groq.com/openai/v1").build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generate HIERARCHICAL multi-level subtopics for a given learning topic using Groq AI
     * @param topicName The main topic name
     * @param purpose Why the user wants to learn this
     * @param level Beginner, Intermediate, or Advanced
     * @return List of hierarchical subtopic paths (e.g., "React > Hooks > useState")
     */
    public List<String> generateSubtopics(String topicName, String purpose, String level) {
        try {
            String prompt = String.format(
                "Create hierarchical subtopics for '%s' (%s level, Purpose: %s).\\n" +
                "Format: 'Parent > Child > Grandchild' using ' > ' delimiter.\\n" +
                "Generate 2-3 levels. Example:\\n" +
                "React\\n" +
                "React > Components\\n" +
                "React > Hooks\\n" +
                "React > Hooks > useState\\n" +
                "React > Hooks > useEffect\\n" +
                "\\n" +
                "Rules:\\n" +
                "1. Level 1: Major topics (HTML, CSS, JavaScript, React, etc.)\\n" +
                "2. Level 2: Core concepts (React > Components, CSS > Flexbox)\\n" +
                "3. Level 3+: Specific topics (React > Hooks > useState)\\n" +
                "4. NO forbidden names: Introduction, Fundamentals, Basics, Overview, Best Practices\\n" +
                "5. Only technical concepts\\n" +
                "\\n" +
                "Output: One path per line, no numbering.",
                topicName, level, purpose
            );

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", "You are an expert curriculum designer who creates clear, structured learning paths."),
                    Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 500
            );

            String response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Parse the response
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Extract subtopics from the response
            List<String> subtopics = new ArrayList<>();
            String[] lines = content.split("\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                // Remove numbering, bullets, and other prefixes
                trimmed = trimmed.replaceAll("^[0-9]+\\.\\s*", "");
                trimmed = trimmed.replaceAll("^[-*•]\\s*", "");
                
                // Validate: reject forbidden subtopics
                if (!trimmed.isEmpty() && trimmed.length() > 3 && trimmed.length() < 100) {
                    if (!isForbiddenSubtopic(trimmed)) {
                        subtopics.add(trimmed);
                    } else {
                        System.out.println("[GroqService] Rejected forbidden subtopic: " + trimmed);
                    }
                }
            }

            System.out.println("[GroqService] Generated " + subtopics.size() + " valid subtopics for: " + topicName);
            return subtopics.isEmpty() ? getDefaultSubtopics(topicName) : subtopics;

        } catch (Exception e) {
            System.err.println("Error calling Groq API: " + e.getMessage());
            e.printStackTrace();
            // Return default subtopics as fallback
            return getDefaultSubtopics(topicName);
        }
    }

    /**
     * Fallback method to generate default HIERARCHICAL TECHNICAL subtopics
     * NEVER returns forbidden generic names
     * Returns multi-level paths using ' > ' delimiter
     */
    private List<String> getDefaultSubtopics(String topicName) {
        String lower = topicName.toLowerCase();
        
        // Topic-specific hierarchical technical subtopics
        if (lower.contains("web") || lower.contains("html") || lower.contains("frontend")) {
            return List.of(
                "HTML",
                "HTML > Basic Tags",
                "HTML > Forms",
                "CSS",
                "CSS > Selectors",
                "CSS > Flexbox",
                "JavaScript",
                "JavaScript > Variables",
                "JavaScript > Functions",
                "JavaScript > DOM Manipulation"
            );
        } else if (lower.contains("java") && !lower.contains("javascript")) {
            return List.of(
                "Java Syntax",
                "Java Syntax > Variables",
                "Java Syntax > Data Types",
                "OOP Concepts",
                "OOP Concepts > Classes & Objects",
                "OOP Concepts > Inheritance",
                "Collections Framework",
                "Collections Framework > ArrayList",
                "Collections Framework > HashMap"
            );
        } else if (lower.contains("python")) {
            return List.of(
                "Python Basics",
                "Python Basics > Variables",
                "Python Basics > Data Types",
                "Functions",
                "Functions > Parameters",
                "Functions > Lambda",
                "Data Structures",
                "Data Structures > Lists",
                "Data Structures > Dictionaries"
            );
        } else if (lower.contains("spring")) {
            return List.of(
                "Spring Boot Basics",
                "Spring Boot Basics > Project Setup",
                "Spring Boot Basics > Auto-Configuration",
                "Dependency Injection",
                "Dependency Injection > Beans",
                "REST APIs",
                "REST APIs > Controllers",
                "Spring Data JPA",
                "Spring Data JPA > Repositories"
            );
        } else if (lower.contains("react")) {
            return List.of(
                "React Components",
                "React Components > Functional Components",
                "React Components > JSX",
                "State Management",
                "State Management > useState",
                "Hooks",
                "Hooks > useEffect",
                "Hooks > Custom Hooks"
            );
        } else if (lower.contains("node")) {
            return List.of(
                "Node.js Basics",
                "Node.js Basics > Modules",
                "Node.js Basics > NPM",
                "Express.js",
                "Express.js > Routing",
                "Express.js > Middleware",
                "File System",
                "File System > Read & Write"
            );
        } else if (lower.contains("sql") || lower.contains("database")) {
            return List.of(
                "SQL Syntax",
                "SQL Syntax > SELECT",
                "SQL Syntax > INSERT",
                "JOINs",
                "JOINs > INNER JOIN",
                "JOINs > LEFT JOIN",
                "Aggregations",
                "Aggregations > COUNT",
                "Aggregations > SUM"
            );
        } else {
            // Generic hierarchical technical fallback
            return List.of(
                topicName + " Syntax",
                topicName + " Syntax > Variables",
                topicName + " Syntax > Operators",
                topicName + " Data Structures",
                topicName + " Data Structures > Arrays",
                topicName + " Functions",
                topicName + " Functions > Parameters",
                topicName + " Error Handling"
            );
        }
    }
    
    /**
     * Check if a subtopic name is forbidden (generic section heading)
     */
    private boolean isForbiddenSubtopic(String subtopic) {
        String lower = subtopic.toLowerCase();
        
        // Exact forbidden matches
        String[] forbidden = {
            "introduction",
            "core fundamentals",
            "key concepts and terminology",
            "key concepts",
            "practical applications",
            "hands-on examples",
            "best practices",
            "common pitfalls and solutions",
            "common pitfalls",
            "advanced techniques",
            "real-world projects",
            "resources and next steps",
            "getting started",
            "overview"
        };
        
        for (String forbidden_name : forbidden) {
            if (lower.equals(forbidden_name)) {
                return true;
            }
        }
        
        // Pattern-based forbidden matches
        if (lower.matches("^introduction to .*")) return true;
        if (lower.matches("^getting started with .*")) return true;
        if (lower.matches("^overview of .*")) return true;
        if (lower.matches(".*fundamentals$") && !lower.contains("javascript") && !lower.contains("programming")) return true;
        
        return false;
    }
}
