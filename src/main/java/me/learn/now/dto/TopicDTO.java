package me.learn.now.dto;

import me.learn.now.model.Topic;

import java.time.LocalDateTime;

public class TopicDTO {
    private Long id;
    private String name;
    private String description;
    private String purpose;
    private String language;
    private String level;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String thumbnail;
    private int estimatedDurationMinutes;
    private int enrolledUsers;
    private double rating;
    private int videoCount;

    public TopicDTO() {
    }

    public TopicDTO(Topic topic, int videoCount) {
        this.id = topic.getId();
        this.name = topic.getName();
        this.description = topic.getDescription();
        this.purpose = topic.getPurpose();
        this.language = topic.getLanguage();
        this.level = topic.getLevel();
        this.createAt = topic.getCreateAt();
        this.updateAt = topic.getUpdateAt();
        this.thumbnail = topic.getThumbnail();
        this.estimatedDurationMinutes = topic.getEstimatedDurationMinutes();
        this.enrolledUsers = topic.getEnrolledUsers();
        this.rating = topic.getRating();
        this.videoCount = videoCount;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public int getEnrolledUsers() {
        return enrolledUsers;
    }

    public void setEnrolledUsers(int enrolledUsers) {
        this.enrolledUsers = enrolledUsers;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }
}
