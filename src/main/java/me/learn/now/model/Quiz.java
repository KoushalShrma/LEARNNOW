package me.learn.now.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Quiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "qId")
	private Long id;

	@Column(name = "qSubTopic")
	private String subTopic;

	// CHANGED: Use a strong enum type with @Enumerated for safety and readability
	// @Enumerated(EnumType.STRING) → Persist enum by name (EASY/MEDIUM/HARD) instead of ordinal
	@Enumerated(EnumType.STRING)
	@Column(name = "qDifficulty")
	private Difficulty difficulty;

	@Column(name = "qLanguage")
	private String language;

	@Column(name = "qPurpose")
	private String purpose;

	@Column(name = "qIsActive")
	private boolean isActive;

	@Column(name = "qCreateAt")
	private LocalDateTime createAt;

	@Column(name = "qUpdateAt")
	private LocalDateTime updateAt;

	// No change: Quiz belongs to a Topic via FK tId on the owning side
	// @ManyToOne @JoinColumn(name = "tId") → Many-to-One relation (Quiz → Topic) using foreign key column tId
	@ManyToOne
	@JoinColumn(name = "tId")
	@JsonIgnore // Prevent infinite recursion when serializing to JSON
	private Topic topic;

	// Hinglish: Quiz ke questions JSON format me store kar rahe hai for simplicity
	@Column(name = "qQuestionsJson", columnDefinition = "TEXT")
	private String questionsJson;

	@PrePersist
	protected void onCreate() {
		// Hinglish: jab bhi naya quiz create ho, timestamp set kar dete hai
		createAt = LocalDateTime.now();
		updateAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		// Hinglish: har update pe timestamp refresh kar dete hai
		updateAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubTopic() {
		return subTopic;
	}

	public void setSubTopic(String subTopic) {
		this.subTopic = subTopic;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
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

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public String getQuestionsJson() {
		return questionsJson;
	}

	public void setQuestionsJson(String questionsJson) {
		this.questionsJson = questionsJson;
	}
}
