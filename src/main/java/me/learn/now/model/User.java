package me.learn.now.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "uId")
	private Long id;

	@Column(name = "uName", unique = true, nullable = false)
	private String name;

	@Column(name = "uEmail", unique = true, nullable = false)
	private String email;

	@Column(name = "uPass", nullable = false)
	private String password;

	// CHANGED: Fix mapping - use collection and correct mappedBy value
	// @OneToMany(mappedBy = "user") → One-to-Many relation (User → UserProgress) inverse side; FK owned by UserProgress.user
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserProgress> userProgress = new ArrayList<>();

	private String role = "USER"; // Hinglish: default role set kar diya

	@Column(name = "uPreferredLanguage")
	private String preferredLanguage = "en"; // Hinglish: default language English

	@Column(name = "uCreateAt")
	private LocalDateTime createAt;

	@Column(name = "uUpdateAt")
	private LocalDateTime updateAt;

	@PrePersist
	protected void onCreate() {
		// Hinglish: naya user register hone pe timestamps set kar dete hai
		createAt = LocalDateTime.now();
		updateAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		// Hinglish: user update hone pe sirf update timestamp change karte hai
		updateAt = LocalDateTime.now();
	}

	// Getters and Setters
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<UserProgress> getUserProgress() {
		return userProgress;
	}

	public void setUserProgress(List<UserProgress> userProgress) {
		this.userProgress = userProgress;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
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
}
