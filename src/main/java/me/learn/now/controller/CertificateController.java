package me.learn.now.controller;

import me.learn.now.dto.CertificateDTO;
import me.learn.now.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * Generate certificate for a user's course completion
     * POST /api/certificates/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<CertificateDTO> generateCertificate(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Long courseId = Long.valueOf(request.get("courseId").toString());
            String userName = (String) request.get("userName");

            if (userId == null || courseId == null || userName == null) {
                return ResponseEntity.badRequest().build();
            }

            CertificateDTO certificate = certificateService.generateCertificate(userId, courseId, userName);
            return ResponseEntity.ok(certificate);

        } catch (Exception e) {
            System.err.println("[Certificate Controller] Error generating certificate: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all certificates for a user
     * GET /api/certificates/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CertificateDTO>> getUserCertificates(@PathVariable String userId) {
        try {
            List<CertificateDTO> certificates = certificateService.getUserCertificates(userId);
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            System.err.println("[Certificate Controller] Error fetching user certificates: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get certificate by certificate number (for verification)
     * GET /api/certificates/verify/{certificateNumber}
     */
    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<CertificateDTO> verifyCertificate(@PathVariable String certificateNumber) {
        return certificateService.getCertificateByCertificateNumber(certificateNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if user has certificate for a course
     * GET /api/certificates/check/{userId}/{courseId}
     */
    @GetMapping("/check/{userId}/{courseId}")
    public ResponseEntity<Map<String, Boolean>> checkCertificate(
            @PathVariable String userId,
            @PathVariable Long courseId) {
        boolean hasCertificate = certificateService.hasCertificate(userId, courseId);
        return ResponseEntity.ok(Map.of("hasCertificate", hasCertificate));
    }
}
