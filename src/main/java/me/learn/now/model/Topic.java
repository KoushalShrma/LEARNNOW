package me.learn.now.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@JsonIgnoreProperties({"videos", "quizzes", "user", "userProgress"}) // Prevent infinite recursion and N+1 query issues
public class Topic {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	
	// Clerk user ID who created this topic (string format from Clerk)
	@Column(name = "created_by_user_id")
	private String createdByUserId;

	// Hinglish: har topic ka ek owner/creator user hota hai
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "u_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private User user;

	// CHANGED: Use mappedBy instead of @JoinColumn on OneToMany; FK lives on Video table as tId
	// @OneToMany(mappedBy = "topic") → One-to-Many relation (Topic → Videos) using the owning side on Video
	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true) // mappedBy fix + cascade cleanup
	private List<Video> videos = new ArrayList<>(); // initialize to avoid NPEs

	// No change to mapping name, just clarifying it's inverse side
	// @OneToMany(mappedBy = "topic") → One-to-Many relation (Topic → Quizzes) inverse side; Quiz owns FK
	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true) // cascade cleanup for quizzes
	private List<Quiz> quizzes = new ArrayList<>();

	// User progress tracking for this topic
	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true) // cascade cleanup for user progress
	private List<UserProgress> userProgress = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		// Hinglish: naya topic create hone pe timestamps set kar dete hai
		createAt = LocalDateTime.now();
		updateAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		// Hinglish: topic update hone pe sirf update timestamp change karte hai
		updateAt = LocalDateTime.now();
	}

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

	public List<Video> getVideos() {
		return videos;
	}

	public void setVideos(List<Video> videos) {
		this.videos = videos;
	}

	public List<Quiz> getQuizzes() {
		return quizzes;
	}

	public void setQuizzes(List<Quiz> quizzes) {
		this.quizzes = quizzes;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	// Utility methods

	public int getVideoCount() {
		return videos != null ? videos.size() : 0;
	}

	public String getEstimatedDuration() {
		int hours = estimatedDurationMinutes / 60;
		int minutes = estimatedDurationMinutes % 60;

		if (hours > 0) {
			return hours + (hours == 1 ? " hour" : " hours") +
				(minutes > 0 ? " " + minutes + " mins" : "");
		} else {
			return minutes + " mins";
		}
	}
}
