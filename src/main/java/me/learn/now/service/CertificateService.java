package me.learn.now.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.learn.now.dto.CertificateDTO;
import me.learn.now.model.Certificate;
import me.learn.now.repository.CertificateRepository;
import me.learn.now.model.Topic;
import me.learn.now.repository.TopicRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final TopicRepo topicRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${certificate.api.url:https://api.craftmypdf.com}")
    private String certificateApiUrl;

    @Value("${certificate.api.key:d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==}")
    private String certificateApiKey;

    @Value("${certificate.template.id:YOUR_TEMPLATE_ID}")
    private String templateId;

    public CertificateService(CertificateRepository certificateRepository,
                            TopicRepo topicRepository,
                            WebClient.Builder webClientBuilder,
                            ObjectMapper objectMapper) {
        this.certificateRepository = certificateRepository;
        this.topicRepository = topicRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generate or retrieve existing certificate for a user's course completion
     */
    public CertificateDTO generateCertificate(String userId, Long courseId, String userName) {
        // Check if certificate already exists
        Optional<Certificate> existing = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) {
            System.out.println("[Certificate] Certificate already exists for user: " + userId + ", course: " + courseId);
            return mapToDTO(existing.get());
        }

        // Fetch course details
        Topic topic = topicRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        // Generate unique certificate number
        String certificateNumber = generateUniqueCertificateNumber(courseId, topic.getName());

        // Determine course color theme
        Map<String, String> colorTheme = getCourseColorTheme(topic.getName());

        // Generate certificate via external API
        String certificateUrl = callCertificateAPI(
            userName,
            topic.getName(),
            certificateNumber,
            colorTheme
        );

        // Save certificate to database
        Certificate certificate = new Certificate();
        certificate.setCertificateNumber(certificateNumber);
        certificate.setCertificateUrl(certificateUrl);
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setCourseName(topic.getName());
        certificate.setUserName(userName);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setCompletionDate(LocalDateTime.now());

        Certificate saved = certificateRepository.save(certificate);
        System.out.println("[Certificate] Generated certificate: " + certificateNumber + " for user: " + userId);

        return mapToDTO(saved);
    }

    /**
     * Generate unique certificate number
     * Format: LN-{YEAR}-{COURSE_ABBR}-{RANDOM_6}
     * Example: LN-2025-WEBDEV-A3F8E2
     */
    private String generateUniqueCertificateNumber(Long courseId, String courseName) {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String courseAbbr = generateCourseAbbreviation(courseName);
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        return String.format("LN-%s-%s-%s", year, courseAbbr, randomPart);
    }

    /**
     * Generate course abbreviation from course name
     */
    private String generateCourseAbbreviation(String courseName) {
        String cleaned = courseName.toUpperCase()
            .replaceAll("[^A-Z0-9\\s]", "")
            .trim();
        
        String[] words = cleaned.split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(6, words[0].length()));
        }
        
        // Take first letter of each word, max 6 letters
        StringBuilder abbr = new StringBuilder();
        for (String word : words) {
            if (abbr.length() >= 6) break;
            if (word.length() > 0) {
                abbr.append(word.charAt(0));
            }
        }
        
        return abbr.toString();
    }

    /**
     * Determine color theme based on course name
     */
    private Map<String, String> getCourseColorTheme(String courseName) {
        String lower = courseName.toLowerCase();
        Map<String, String> theme = new HashMap<>();

        if (lower.contains("web") || lower.contains("html") || lower.contains("css") || lower.contains("frontend")) {
            theme.put("primaryColor", "#6366f1"); // Indigo
            theme.put("secondaryColor", "#8b5cf6"); // Purple
            theme.put("themeName", "web-dev");
        } else if (lower.contains("java") || lower.contains("spring")) {
            theme.put("primaryColor", "#10b981"); // Green
            theme.put("secondaryColor", "#14b8a6"); // Teal
            theme.put("themeName", "java");
        } else if (lower.contains("python") || lower.contains("data") || lower.contains("ml")) {
            theme.put("primaryColor", "#8b5cf6"); // Purple
            theme.put("secondaryColor", "#f59e0b"); // Orange
            theme.put("themeName", "data-science");
        } else if (lower.contains("react") || lower.contains("node") || lower.contains("javascript")) {
            theme.put("primaryColor", "#06b6d4"); // Cyan
            theme.put("secondaryColor", "#3b82f6"); // Blue
            theme.put("themeName", "javascript");
        } else if (lower.contains("cloud") || lower.contains("aws") || lower.contains("azure")) {
            theme.put("primaryColor", "#0ea5e9"); // Sky blue
            theme.put("secondaryColor", "#6366f1"); // Indigo
            theme.put("themeName", "cloud");
        } else {
            // Default theme
            theme.put("primaryColor", "#6366f1"); // Indigo
            theme.put("secondaryColor", "#8b5cf6"); // Purple
            theme.put("themeName", "default");
        }

        return theme;
    }

    /**
     * Call CraftMyPDF API to generate certificate
     */
    private String callCertificateAPI(String userName, String courseName, 
                                     String certificateNumber, Map<String, String> colorTheme) {
        try {
            String completionDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            
            // Sanitize username for file naming (remove spaces and special chars)
            String sanitizedUserName = userName.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();

            // Build CraftMyPDF API request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("template_id", templateId);
            payload.put("export_type", "json"); // Returns JSON with file URL on CDN
            payload.put("expiration", 525600); // 1 year in minutes (365 * 24 * 60)
            payload.put("output_file", certificateNumber + ".pdf");
            payload.put("cloud_storage", 1); // Upload to CraftMyPDF CDN
            
            // Certificate data matching CraftMyPDF template fields
            Map<String, String> data = new HashMap<>();
            data.put("title", "of Achievement");  // Certificate type
            data.put("desc", "has successfully completed the " + courseName + " course. This achievement demonstrates exceptional dedication and commitment to learning.");  // Description with course name
            data.put("recipient", userName);  // Logged-in user's name
            data.put("date", completionDate);  // Real-time completion date
            data.put("signature", "Koushal Sharma");  // CEO & Founder signature
            data.put("signer_name", "CEO & Founder, LearnNow");  // Signature title
            payload.put("data", data);

            System.out.println("[CraftMyPDF] Calling API to generate certificate: " + certificateNumber);
            System.out.println("[CraftMyPDF] Template ID: " + templateId);
            System.out.println("[CraftMyPDF] Payload: " + objectMapper.writeValueAsString(payload));

            // Call CraftMyPDF API
            String response = webClient.post()
                .uri(certificateApiUrl + "/v1/create")
                .header("X-API-KEY", certificateApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .map(errorBody -> {
                            System.err.println("[CraftMyPDF] API Error Response: " + errorBody);
                            return new RuntimeException("CraftMyPDF API Error: " + errorBody);
                        })
                )
                .bodyToMono(String.class)
                .block();

            System.out.println("[CraftMyPDF] API Response: " + response);

            // Parse response
            JsonNode responseNode = objectMapper.readTree(response);
            String status = responseNode.path("status").asText();
            
            if ("success".equals(status)) {
                String certificateUrl = responseNode.path("file").asText();
                System.out.println("[CraftMyPDF] Certificate generated successfully: " + certificateUrl);
                return certificateUrl;
            } else {
                throw new RuntimeException("CraftMyPDF API returned error: " + response);
            }

        } catch (Exception e) {
            System.err.println("[CraftMyPDF] Error generating certificate: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to mock URL if API fails
            String mockUrl = String.format(
                "https://certificates.learnnow.com/%s/%s.pdf",
                certificateNumber,
                userName.replaceAll("\\s+", "-").toLowerCase()
            );
            
            System.out.println("[CraftMyPDF] Fallback to mock URL: " + mockUrl);
            return mockUrl;
        }
    }

    /**
     * Get all certificates for a user
     */
    public List<CertificateDTO> getUserCertificates(String userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get certificate by certificate number
     */
    public Optional<CertificateDTO> getCertificateByCertificateNumber(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber)
            .map(this::mapToDTO);
    }

    /**
     * Check if user has certificate for course
     */
    public boolean hasCertificate(String userId, Long courseId) {
        return certificateRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Map Certificate entity to DTO
     */
    private CertificateDTO mapToDTO(Certificate certificate) {
        return new CertificateDTO(
            certificate.getId(),
            certificate.getCertificateNumber(),
            certificate.getCertificateUrl(),
            certificate.getUserId(),
            certificate.getCourseId(),
            certificate.getCourseName(),
            certificate.getUserName(),
            certificate.getIssuedAt(),
            certificate.getCompletionDate()
        );
    }
}
