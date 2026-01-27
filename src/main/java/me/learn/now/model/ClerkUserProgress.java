package me.learn.now.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clerk_user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"clerkUserId", "topicId"})
})
public class ClerkUserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clerkUserId;

    @Column(nullable = false)
    private Long topicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(nullable = false)
    private int progressPercentage = 0;

    @Column(nullable = false)
    private int currentVideoIndex = 0;

    @Column(nullable = false)
    private int totalVideos = 0;

    // Store completed video indices as comma-separated string (e.g., "0,1,2,5,7")
    @Column(length = 2000)
    private String completedVideosStr = "";

    private int totalWatchTimeSeconds = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClerkUserId() {
        return clerkUserId;
    }

    public void setClerkUserId(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public int getCurrentVideoIndex() {
        return currentVideoIndex;
    }

    public void setCurrentVideoIndex(int currentVideoIndex) {
        this.currentVideoIndex = currentVideoIndex;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public String getCompletedVideosStr() {
        return completedVideosStr;
    }

    public void setCompletedVideosStr(String completedVideosStr) {
        this.completedVideosStr = completedVideosStr;
    }

    public int getTotalWatchTimeSeconds() {
        return totalWatchTimeSeconds;
    }

    public void setTotalWatchTimeSeconds(int totalWatchTimeSeconds) {
        this.totalWatchTimeSeconds = totalWatchTimeSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods for completed videos
    public java.util.Set<Integer> getCompletedVideos() {
        java.util.Set<Integer> result = new java.util.HashSet<>();
        if (completedVideosStr != null && !completedVideosStr.isEmpty()) {
            for (String s : completedVideosStr.split(",")) {
                try {
                    result.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    public void setCompletedVideos(java.util.Set<Integer> videos) {
        if (videos == null || videos.isEmpty()) {
            this.completedVideosStr = "";
        } else {
            this.completedVideosStr = videos.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        }
    }

    public void addCompletedVideo(int videoIndex) {
        java.util.Set<Integer> completed = getCompletedVideos();
        completed.add(videoIndex);
        setCompletedVideos(completed);
    }
}
