package me.learn.now.dto;

import java.time.LocalDateTime;

public class CertificateDTO {
    private Long id;
    private String certificateNumber;
    private String certificateUrl;
    private String userId;
    private Long courseId;
    private String courseName;
    private String userName;
    private LocalDateTime issuedAt;
    private LocalDateTime completionDate;

    public CertificateDTO() {}

    public CertificateDTO(Long id, String certificateNumber, String certificateUrl, String userId, 
                         Long courseId, String courseName, String userName, LocalDateTime issuedAt, 
                         LocalDateTime completionDate) {
        this.id = id;
        this.certificateNumber = certificateNumber;
        this.certificateUrl = certificateUrl;
        this.userId = userId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.userName = userName;
        this.issuedAt = issuedAt;
        this.completionDate = completionDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }
}
