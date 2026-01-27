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
                "Generate a COMPLETE and DEEP learning curriculum for EXACTLY '%s'.\\n" +
                "This curriculum must be sufficient for someone to MASTER '%s' professionally.\\n\\n" +

                "Level: %s\\n" +
                
                "Learning Purpose: %s\\n\\n" +
                "• Generate topics ONLY if they are INTRINSIC to '%s' itself.\\n" +
                "CRITICAL SCOPE RULES:\\n" +
                "• Do NOT include adjacent tools, frameworks, or ecosystems unless they are             a CORE part of '%s'.\\n" +
                "• Do NOT broaden or generalize the topic. Stay strictly within '%s'.           \\n\\n" +

                "STRUCTURE RULES:\\n" +
                "• Use hierarchical paths with the format: Parent > Child > Grandchild\\n" +
                "• Use ' > ' as the ONLY delimiter\\n" +
                "• Level 1 MUST ALWAYS be exactly '%s'\\n" +
                "• Generate deeper levels ONLY when the concept logically requires it\\n" +
                "• Avoid shallow trees — depth is preferred over breadth where necessary\\n\\n" +

                "CONTENT DEPTH REQUIREMENTS:\\n" +
                "• Cover ALL core internal concepts required to master '%s'\\n" +
                "• Include architecture, internal mechanics, APIs, lifecycles, patterns, and edge cases\\n" +
                "• Include performance, scalability, limitations, and real-world usage aspects WHEN they are topic-specific\\n" +
                "• If a subtopic has meaningful internal components, break it into children\\n\\n" +

                "STRICT EXCLUSIONS:\\n" +
                "• NO generic headings: Introduction, Basics, Fundamentals, Overview, Getting Started\\n" +
                "• NO learning advice or non-technical sections\\n" +
                "• NO marketing or conceptual fluff\\n" +
                "• NO cross-topic teaching (e.g., Spring Boot when topic is Spring AI)\\n\\n" +

                "OUTPUT CONSTRAINTS:\\n" +
                "• Generate 10–16 total paths\\n" +
                "• Each path must be meaningful and non-overlapping\\n" +
                "• One path per line\\n" +
                "• No numbering, no bullets, no explanations\\n\\n" +

                "QUALITY CHECK (MANDATORY):\\n" +
                "Before outputting, verify that removing ANY line would reduce mastery of   '%s'.\\n\\n" +

                "Output ONLY the curriculum paths.",
                topicName, topicName, level, purpose,
                topicName, topicName, topicName, topicName, topicName
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
        
        // Check for SPECIFIC topics FIRST (before generic matches)
        // Spring AI - must come before generic "spring" check
        if (lower.contains("spring ai") || lower.contains("springai")) {
            return List.of(
                "Spring AI",
                "Spring AI > Chat Models",
                "Spring AI > Chat Models > OpenAI Integration",
                "Spring AI > Chat Models > Ollama Integration",
                "Spring AI > Embeddings",
                "Spring AI > Embeddings > Text Embeddings",
                "Spring AI > Vector Stores",
                "Spring AI > Vector Stores > PGVector",
                "Spring AI > Vector Stores > ChromaDB",
                "Spring AI > Prompt Templates",
                "Spring AI > Prompt Templates > Template Variables",
                "Spring AI > RAG (Retrieval Augmented Generation)",
                "Spring AI > RAG > Document Loaders",
                "Spring AI > Function Calling",
                "Spring AI > Output Parsers"
            );
        }
        
        // LangChain
        if (lower.contains("langchain")) {
            return List.of(
                "LangChain",
                "LangChain > LLM Integration",
                "LangChain > LLM Integration > OpenAI",
                "LangChain > Prompts",
                "LangChain > Prompts > Templates",
                "LangChain > Chains",
                "LangChain > Chains > Sequential Chains",
                "LangChain > Agents",
                "LangChain > Agents > Tools",
                "LangChain > Memory",
                "LangChain > Vector Stores",
                "LangChain > RAG"
            );
        }
        
        // Machine Learning / AI
        if (lower.contains("machine learning") || lower.contains("ml") || (lower.contains("ai") && !lower.contains("spring"))) {
            return List.of(
                "Machine Learning",
                "Machine Learning > Supervised Learning",
                "Machine Learning > Supervised Learning > Regression",
                "Machine Learning > Supervised Learning > Classification",
                "Machine Learning > Unsupervised Learning",
                "Machine Learning > Unsupervised Learning > Clustering",
                "Machine Learning > Neural Networks",
                "Machine Learning > Neural Networks > Deep Learning",
                "Machine Learning > Model Evaluation",
                "Machine Learning > Feature Engineering"
            );
        }
        
        // Deep Learning
        if (lower.contains("deep learning") || lower.contains("neural network")) {
            return List.of(
                "Deep Learning",
                "Deep Learning > Neural Network Basics",
                "Deep Learning > CNNs (Convolutional Neural Networks)",
                "Deep Learning > RNNs (Recurrent Neural Networks)",
                "Deep Learning > Transformers",
                "Deep Learning > Transformers > Attention Mechanism",
                "Deep Learning > GANs",
                "Deep Learning > Transfer Learning",
                "Deep Learning > PyTorch",
                "Deep Learning > TensorFlow"
            );
        }
        
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
        } else if (lower.contains("spring boot") || lower.contains("springboot")) {
            return List.of(
                "Spring Boot",
                "Spring Boot > Project Setup",
                "Spring Boot > Auto-Configuration",
                "Spring Boot > Dependency Injection",
                "Spring Boot > Dependency Injection > Beans",
                "Spring Boot > REST APIs",
                "Spring Boot > REST APIs > Controllers",
                "Spring Boot > Spring Data JPA",
                "Spring Boot > Spring Data JPA > Repositories",
                "Spring Boot > Security",
                "Spring Boot > Actuator"
            );
        } else if (lower.contains("spring") && !lower.contains("ai")) {
            // Generic Spring (not Spring AI)
            return List.of(
                "Spring Framework",
                "Spring Framework > IoC Container",
                "Spring Framework > Dependency Injection",
                "Spring Framework > Beans",
                "Spring Framework > AOP",
                "Spring Framework > Spring MVC",
                "Spring Framework > Spring MVC > Controllers",
                "Spring Framework > Spring Security",
                "Spring Framework > Spring Data"
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
