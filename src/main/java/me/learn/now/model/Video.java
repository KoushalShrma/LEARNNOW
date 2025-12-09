package me.learn.now.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Video {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String youtubeId;
	private String title;
	private String channel;
	private Integer duration; // in seconds
	private String language;
	private Integer position; // position in the topic
	private String subtopic; // subtopic/section name for grouping videos
	private String chaptersJson; // JSON representation of chapters
	private LocalDateTime createAt;
	private LocalDateTime updateAt;

	// No change: Video is the owning side with FK tId
	// @ManyToOne @JoinColumn(name = "tId") → Many-to-One relation (Video → Topic) using foreign key column tId
	@ManyToOne
	@JoinColumn(name = "tId")
	@JsonIgnore // Prevent infinite recursion when serializing to JSON
	private Topic topic;

	// CHANGED: Remove CascadeType.ALL to avoid deleting Quiz when Video is deleted; keep association optional
	// @OneToOne(optional = true) → One-to-One relation (Video ↔ Quiz), quiz is optional and not cascaded
	@OneToOne(optional = true)
	@JoinColumn(name = "qId", referencedColumnName = "qId")
	private Quiz quiz;

	@PrePersist
	protected void onCreate() {
		// Hinglish: naya video add hone pe timestamps set kar dete hai
		createAt = LocalDateTime.now();
		updateAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		// Hinglish: video update hone pe sirf update timestamp change karte hai
		updateAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getYoutubeId() {
		return youtubeId;
	}

	public void setYoutubeId(String youtubeId) {
		this.youtubeId = youtubeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getSubtopic() {
		return subtopic;
	}

	public void setSubtopic(String subtopic) {
		this.subtopic = subtopic;
	}

	public String getChaptersJson() {
		return chaptersJson;
	}

	public void setChaptersJson(String chaptersJson) {
		this.chaptersJson = chaptersJson;
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

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}
}
