package me.learn.now.service;

import me.learn.now.model.ProgressStatus;
import me.learn.now.model.Topic;
import me.learn.now.model.User;
import me.learn.now.model.UserProgress;
import me.learn.now.repository.TopicRepo;
import me.learn.now.repository.UserProgressRepo;
import me.learn.now.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserProgressService {
    @Autowired
    private UserProgressRepo upr; // progress table pe CRUD
    @Autowired
    private UserRepo ur;         // user ko attach karne ke liye
    @Autowired
    private TopicRepo tr;        // topic ko attach/check karne ke liye

    // Hinglish: ek user ka saara progress
    public List<UserProgress> listByUser(Long userId){
        return upr.findByUserId(userId);
    }

    // Hinglish: ensure ye progress isi user ka hai
    public Optional<UserProgress> getForUser(Long userId, Long progressId){
        Optional<UserProgress> up = upr.findById(progressId);
        return up.filter(p -> p.getUser() != null && p.getUser().getId().equals(userId));
    }

    // Hinglish: user attach + optional topic attach
    public UserProgress create(Long userId, UserProgress input){
        User u = ur.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        input.setUser(u);
        if (input.getTopic() != null && input.getTopic().getId() != null){
            Topic t = tr.findById(input.getTopic().getId()).orElseThrow(() -> new NoSuchElementException("Topic not found"));
            input.setTopic(t);
        }
        if (input.getCreateAt() == null) input.setCreateAt(LocalDateTime.now());
        input.setUpdateAt(LocalDateTime.now());
        return upr.save(input);
    }

    // Hinglish: progress update karne ke liye - status/seconds/lastSeenAt
    public UserProgress updateProgress(Long userId, Long progressId, UserProgress updates){
        UserProgress existing = upr.findById(progressId)
            .orElseThrow(() -> new NoSuchElementException("UserProgress not found"));

        // Hinglish: security check - sirf apna progress update kar sakta hai
        if (!existing.getUser().getId().equals(userId)) {
            throw new SecurityException("You can only update your own progress");
        }

        // Hinglish: fields update karte hai
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        if (updates.getSecondsWatched() != null) {
            existing.setSecondsWatched(updates.getSecondsWatched());
        }
        if (updates.getLastSeenAt() != null) {
            existing.setLastSeenAt(updates.getLastSeenAt());
        }

        existing.setUpdateAt(LocalDateTime.now());
        return upr.save(existing);
    }

    // Hinglish: user ka specific topic ke liye progress get karne ke liye
    public Optional<UserProgress> getUserTopicProgress(Long userId, Long topicId) {
        return upr.findByUserIdAndTopicId(userId, topicId);
    }

    // Hinglish: user ka specific topic ke liye progress create ya update karne ke liye
    public UserProgress createOrUpdateTopicProgress(Long userId, Long topicId, ProgressStatus status, Integer secondsWatched) {
        Optional<UserProgress> existingProgress = getUserTopicProgress(userId, topicId);

        UserProgress progress;
        if (existingProgress.isPresent()) {
            // Hinglish: existing progress update karte hai
            progress = existingProgress.get();
            progress.setStatus(status);
            if (secondsWatched != null) {
                progress.setSecondsWatched(secondsWatched);
            }
            progress.setLastSeenAt(LocalDateTime.now());
            progress.setUpdateAt(LocalDateTime.now());
        } else {
            // Hinglish: naya progress create karte hai
            User user = ur.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
            Topic topic = tr.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found"));

            progress = new UserProgress();
            progress.setUser(user);
            progress.setTopic(topic);
            progress.setStatus(status);
            progress.setSecondsWatched(secondsWatched != null ? secondsWatched : 0);
            progress.setLastSeenAt(LocalDateTime.now());
            progress.setCreateAt(LocalDateTime.now());
            progress.setUpdateAt(LocalDateTime.now());
        }

        return upr.save(progress);
    }

    // Hinglish: video watching ke time progress update karne ke liye
    public UserProgress updateVideoProgress(Long userId, Long topicId, Integer secondsWatched) {
        ProgressStatus status = ProgressStatus.IN_PROGRESS;

        // Hinglish: agar 90% se zyada dekha hai toh COMPLETED mark kar dete hai
        // Note: Real implementation me video ki total duration se compare karenge
        if (secondsWatched > 300) { // 5 minutes se zyada = completed (placeholder logic)
            status = ProgressStatus.COMPLETED;
        }

        return createOrUpdateTopicProgress(userId, topicId, status, secondsWatched);
    }

    // Hinglish: user ke completed topics count karne ke liye
    public long getCompletedTopicsCount(Long userId) {
        List<UserProgress> completedProgress = upr.findByUserIdAndStatus(userId, ProgressStatus.COMPLETED);
        return completedProgress.size();
    }

    // Hinglish: user ka overall learning progress percentage get karne ke liye
    public double getUserLearningProgressPercentage(Long userId) {
        List<UserProgress> allProgress = listByUser(userId);
        if (allProgress.isEmpty()) {
            return 0.0;
        }

        long completedCount = allProgress.stream()
            .mapToLong(up -> up.getStatus() == ProgressStatus.COMPLETED ? 1 : 0)
            .sum();

        return (double) completedCount / allProgress.size() * 100;
    }

    // Hinglish: delete progress entry
    public boolean deleteProgress(Long userId, Long progressId) {
        Optional<UserProgress> progress = getForUser(userId, progressId);
        if (progress.isPresent()) {
            upr.delete(progress.get());
            return true;
        }
        return false;
    }

    // Hinglish: patch method - update specific progress fields
    public UserProgress patch(Long userId, Long progressId, UserProgress delta) {
        UserProgress existing = upr.findById(progressId)
            .orElseThrow(() -> new NoSuchElementException("UserProgress not found"));

        // Hinglish: security check
        if (!existing.getUser().getId().equals(userId)) {
            throw new SecurityException("You can only update your own progress");
        }

        // Hinglish: selective field updates
        if (delta.getStatus() != null) existing.setStatus(delta.getStatus());
        if (delta.getSecondsWatched() != null) existing.setSecondsWatched(delta.getSecondsWatched());
        if (delta.getLastSeenAt() != null) existing.setLastSeenAt(delta.getLastSeenAt());

        existing.setUpdateAt(LocalDateTime.now());
        return upr.save(existing);
    }

    // Hinglish: progress delete karne ke liye
    public Optional<UserProgress> delete(Long userId, Long progressId) {
        Optional<UserProgress> progress = getForUser(userId, progressId);
        if (progress.isPresent()) {
            upr.delete(progress.get());
        }
        return progress;
    }

    // Hinglish: topic ke basis pe progress get karne ke liye
    public Optional<UserProgress> getByTopic(Long userId, Long topicId) {
        return upr.findByUserIdAndTopicId(userId, topicId);
    }

    // Hinglish: topic ke liye status set karne ke liye
    public UserProgress setStatusByTopic(Long userId, Long topicId, ProgressStatus status) {
        Optional<UserProgress> existing = getByTopic(userId, topicId);

        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            progress.setStatus(status);
            progress.setUpdateAt(LocalDateTime.now());
            return upr.save(progress);
        } else {
            // Hinglish: agar progress exist nahi karta toh naya banate hai
            User user = ur.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
            Topic topic = tr.findById(topicId).orElseThrow(() -> new NoSuchElementException("Topic not found"));

            UserProgress newProgress = new UserProgress();
            newProgress.setUser(user);
            newProgress.setTopic(topic);
            newProgress.setStatus(status);
            newProgress.setSecondsWatched(0);
            newProgress.setCreateAt(LocalDateTime.now());
            newProgress.setUpdateAt(LocalDateTime.now());

            return upr.save(newProgress);
        }
    }

    // Hinglish: watch seconds update karne ke liye
    public UserProgress setWatchSecondsByTopic(Long userId, Long topicId, Integer seconds) {
        Optional<UserProgress> existing = getByTopic(userId, topicId);

        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            progress.setSecondsWatched(seconds);
            progress.setUpdateAt(LocalDateTime.now());

            // Hinglish: agar progress IN_PROGRESS hai toh lastSeenAt update kar dete hai
            if (progress.getStatus() == ProgressStatus.IN_PROGRESS) {
                progress.setLastSeenAt(LocalDateTime.now());
            }

            return upr.save(progress);
        } else {
            // Hinglish: naya progress entry banate hai
            return setStatusByTopic(userId, topicId, ProgressStatus.IN_PROGRESS);
        }
    }

    // Hinglish: video complete hone pe progress update karne ke liye
    public UserProgress updateVideoCompletion(Long userId, Long topicId, int completedVideos, int totalVideos) {
        Optional<UserProgress> existing = getByTopic(userId, topicId);
        
        // Calculate progress percentage
        int progressPercentage = totalVideos > 0 ? (completedVideos * 100) / totalVideos : 0;
        
        // Determine status based on progress
        ProgressStatus status;
        if (progressPercentage >= 100) {
            status = ProgressStatus.COMPLETED;
        } else if (progressPercentage > 0) {
            status = ProgressStatus.IN_PROGRESS;
        } else {
            status = ProgressStatus.NOT_STARTED;
        }

        UserProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
        } else {
            progress = new UserProgress();
            User user = ur.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
            Topic topic = tr.findById(topicId).orElseThrow(() -> new NoSuchElementException("Topic not found"));
            progress.setUser(user);
            progress.setTopic(topic);
            progress.setCreateAt(LocalDateTime.now());
        }

        progress.setStatus(status);
        progress.setProgressPercentage(progressPercentage);
        progress.setLastSeenAt(LocalDateTime.now());
        progress.setUpdateAt(LocalDateTime.now());

        return upr.save(progress);
    }

    // Hinglish: course ka overall progress get karne ke liye with detailed stats
    public CourseProgressDTO getCourseProgress(Long userId, Long topicId) {
        Optional<UserProgress> progressOpt = getByTopic(userId, topicId);
        
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setTopicId(topicId);
        dto.setUserId(userId);
        
        if (progressOpt.isPresent()) {
            UserProgress progress = progressOpt.get();
            dto.setStatus(progress.getStatus().name());
            dto.setProgressPercentage(progress.getProgressPercentage());
            dto.setSecondsWatched(progress.getSecondsWatched() != null ? progress.getSecondsWatched() : 0);
            dto.setLastSeenAt(progress.getLastSeenAt());
        } else {
            dto.setStatus(ProgressStatus.NOT_STARTED.name());
            dto.setProgressPercentage(0);
            dto.setSecondsWatched(0);
            dto.setLastSeenAt(null);
        }
        
        return dto;
    }

    // Inner DTO class for course progress
    public static class CourseProgressDTO {
        private Long topicId;
        private Long userId;
        private String status;
        private int progressPercentage;
        private int secondsWatched;
        private LocalDateTime lastSeenAt;

        // Getters and setters
        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
        public int getSecondsWatched() { return secondsWatched; }
        public void setSecondsWatched(int secondsWatched) { this.secondsWatched = secondsWatched; }
        public LocalDateTime getLastSeenAt() { return lastSeenAt; }
        public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    }
}
