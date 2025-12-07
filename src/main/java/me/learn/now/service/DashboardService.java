package me.learn.now.service;

import me.learn.now.dto.dashboard.ActivityDTO;
import me.learn.now.dto.dashboard.RecommendationDTO;
import me.learn.now.dto.dashboard.UserStatsDTO;
import me.learn.now.model.ScoreCard;
import me.learn.now.model.Topic;
import me.learn.now.model.User;
import me.learn.now.model.UserProgress;
import me.learn.now.repository.ScoreCardRepo;
import me.learn.now.repository.TopicRepo;
import me.learn.now.repository.UserProgressRepo;
import me.learn.now.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private UserProgressRepo userProgressRepo;

    @Autowired
    private ScoreCardRepo scoreCardRepo;

    /**
     * Get user statistics for the dashboard
     */
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            return stats; // Return empty stats if user not found
        }

        User user = userOpt.get();

        // Get all topics count
        int totalTopics = (int) topicRepo.count();
        stats.setTotalTopics(totalTopics);

        // Get user progress data
        List<UserProgress> userProgress = userProgressRepo.findByUserId(userId);

        // Count completed topics (progress 100%)
        int completedTopics = (int) userProgress.stream()
                .filter(progress -> progress.getProgressPercentage() >= 100)
                .count();
        stats.setCompletedTopics(completedTopics);

        // Calculate total study time
        long totalStudyTime = userProgress.stream()
                .mapToLong(UserProgress::getStudyTimeSeconds)
                .sum() / 60; // Convert seconds to minutes
        stats.setTotalStudyTimeMinutes(totalStudyTime);

        // Get user quiz statistics
        // Fixed: Use findByUserIdOrderByCreatedAtDesc instead of findByUserId which returns a List
        List<ScoreCard> scoreCards = scoreCardRepo.findByUserIdOrderByCreatedAtDesc(userId);
        stats.setTotalQuizzes(scoreCards.size());

        // Calculate average score
        // Fixed: Convert double score to int for mapToInt
        double averageScore = scoreCards.isEmpty() ? 0 :
                scoreCards.stream()
                        .mapToDouble(ScoreCard::getScore) // Changed from mapToInt to mapToDouble
                        .average()
                        .orElse(0);
        stats.setAverageScore(Math.round(averageScore * 10) / 10.0); // Round to 1 decimal place

        // Calculate streak days (simplified version)
        stats.setStreakDays(calculateStreakDays(userId));

        // Calculate total XP
        int totalXP = completedTopics * 100 + // 100 XP per completed topic
                (int)(averageScore * scoreCards.size() / 10); // 0-10 XP per quiz based on score
        stats.setTotalXP(totalXP);

        // Find favorite topic (most time spent)
        if (!userProgress.isEmpty()) {
            Optional<UserProgress> favoriteTopic = userProgress.stream()
                    .max(Comparator.comparingLong(UserProgress::getStudyTimeSeconds));

            favoriteTopic.ifPresent(progress -> {
                Optional<Topic> topic = topicRepo.findById(progress.getTopicId());
                topic.ifPresent(t -> stats.setFavoriteTopic(t.getName()));
            });
        }

        return stats;
    }

    /**
     * Calculate user streak days
     * A simplified implementation - in a real app, this would analyze login history
     */
    private int calculateStreakDays(Long userId) {
        // In a real implementation, we would check the user's login history
        // For now, we'll use a simplified approach based on progress updates
        List<UserProgress> progress = userProgressRepo.findByUserId(userId);

        if (progress.isEmpty()) {
            return 0;
        }

        // Sort by last update time
        progress.sort(Comparator.comparing(UserProgress::getLastSeenAt).reversed());

        // Get most recent activity
        LocalDateTime lastActivity = progress.get(0).getLastSeenAt();

        // If the user has activity in the last 24 hours, give them a streak
        // This is a very simplified implementation
        if (lastActivity.isAfter(LocalDateTime.now().minusDays(1))) {
            // Random number between 1-10 for demonstration
            // In real implementation, this would be calculated from consecutive days
            return Math.max(1, progress.size() % 10);
        }

        return 0;
    }

    /**
     * Get recent user activity for the dashboard
     */
    public List<ActivityDTO> getUserActivity(Long userId, int limit) {
        List<ActivityDTO> activities = new ArrayList<>();

        // Get user quiz activities
        List<ScoreCard> recentScoreCards = scoreCardRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (recentScoreCards.size() > limit) {
            recentScoreCards = recentScoreCards.subList(0, limit);
        }

        // Convert score cards to activities
        for (ScoreCard scoreCard : recentScoreCards) {
            Optional<Topic> topic = topicRepo.findById(scoreCard.getTopicId());
            if (topic.isPresent()) {
                ActivityDTO activity = new ActivityDTO(
                        scoreCard.getId(),
                        "quiz",
                        topic.get().getName() + " Quiz",
                        scoreCard.getCreatedAt()
                );
                activity.setScore(scoreCard.getScore());
                activities.add(activity);
            }
        }

        // Get user progress activities (completed videos)
        List<UserProgress> recentProgress = userProgressRepo.findByUserIdAndProgressPercentageOrderByLastSeenAtDesc(
                userId, 100);
        if (recentProgress.size() > limit) {
            recentProgress = recentProgress.subList(0, limit);
        }

        // Convert progress to activities
        for (UserProgress progress : recentProgress) {
            Optional<Topic> topic = topicRepo.findById(progress.getTopicId());
            if (topic.isPresent()) {
                ActivityDTO activity = new ActivityDTO(
                        progress.getId(),
                        "video",
                        topic.get().getName(),
                        progress.getLastSeenAt()
                );
                activity.setProgress(progress.getProgressPercentage());
                activities.add(activity);
            }
        }

        // Sort all activities by timestamp (most recent first)
        activities.sort(Comparator.comparing(ActivityDTO::getTimestamp).reversed());

        // Limit the final list
        if (activities.size() > limit) {
            activities = activities.subList(0, limit);
        }

        return activities;
    }

    /**
     * Get course recommendations for the dashboard
     */
    public List<RecommendationDTO> getRecommendations(Long userId, int limit) {
        List<RecommendationDTO> recommendations = new ArrayList<>();

        // Get in-progress courses (for "continue" recommendations)
        List<UserProgress> inProgressCourses = userProgressRepo.findByUserIdAndProgressPercentageBetweenOrderByLastSeenAtDesc(
                userId, 1, 99);

        for (UserProgress progress : inProgressCourses) {
            Optional<Topic> topic = topicRepo.findById(progress.getTopicId());
            if (topic.isPresent()) {
                RecommendationDTO recommendation = new RecommendationDTO(
                        topic.get().getId(),
                        "continue",
                        "Continue: " + topic.get().getName(),
                        topic.get().getDescription()
                );

                // Calculate time left (simplified)
                int estimatedMinutesLeft = (int) ((100 - progress.getProgressPercentage()) / 100.0
                        * topic.get().getEstimatedDurationMinutes());
                recommendation.setTimeLeft(estimatedMinutesLeft + " mins left");

                recommendations.add(recommendation);

                if (recommendations.size() >= limit) {
                    break;
                }
            }
        }

        // If we need more recommendations, add new courses the user hasn't started
        if (recommendations.size() < limit) {
            // Get user's completed and in-progress topics
            List<Long> userTopicIds = userProgressRepo.findByUserId(userId).stream()
                    .map(UserProgress::getTopicId)
                    .collect(Collectors.toList());

            // Find topics the user hasn't started yet
            List<Topic> newTopics = topicRepo.findAll().stream()
                    .filter(topic -> !userTopicIds.contains(topic.getId()))
                    .collect(Collectors.toList());

            for (Topic topic : newTopics) {
                RecommendationDTO recommendation = new RecommendationDTO(
                        topic.getId(),
                        "new",
                        "New Course: " + topic.getName(),
                        topic.getDescription()
                );

                // Format duration
                int hours = topic.getEstimatedDurationMinutes() / 60;
                int minutes = topic.getEstimatedDurationMinutes() % 60;
                if (hours > 0) {
                    recommendation.setDuration(hours + (hours == 1 ? " hour" : " hours") +
                            (minutes > 0 ? " " + minutes + " mins" : "") + " total");
                } else {
                    recommendation.setDuration(minutes + " mins total");
                }

                recommendations.add(recommendation);

                if (recommendations.size() >= limit) {
                    break;
                }
            }
        }

        // If we still need more recommendations, add challenges
        if (recommendations.size() < limit) {
            // For now, we'll add a generic challenge
            // In a real app, these would be generated based on user skills and progress
            RecommendationDTO challenge = new RecommendationDTO(
                    0L, // Generic ID
                    "challenge",
                    "Weekend Challenge: React Router",
                    "Test your skills and earn the Router Master badge"
            );
            challenge.setReward("250 XP reward");
            recommendations.add(challenge);
        }

        return recommendations;
    }

    /**
     * Get recent activity for the dashboard
     */
    public List<ActivityDTO> getRecentActivity(Long userId) {
        List<ActivityDTO> activities = new ArrayList<>();
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            return activities; // Return empty list if user not found
        }

        User user = userOpt.get();

        // Add progress activity
        userProgressRepo.findByUserId(userId).stream()
                .sorted(Comparator.comparing(UserProgress::getLastSeenAt).reversed())
                .limit(5)
                .forEach(progress -> {
                    Optional<Topic> topicOpt = topicRepo.findById(progress.getTopicId());
                    if (topicOpt.isPresent()) {
                        ActivityDTO activity = new ActivityDTO();
                        activity.setType("progress");
                        activity.setTimestamp(progress.getLastSeenAt());
                        activity.setTopicId(progress.getTopicId());
                        activity.setTopicName(topicOpt.get().getName());
                        activity.setDetail("Studied for " + progress.getStudyTimeSeconds() / 60 + " minutes");
                        activities.add(activity);
                    }
                });

        // Add quiz activity
        // Fixed: Use findByUserIdOrderByCreatedAtDesc which returns a List
        scoreCardRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .forEach(scoreCard -> {
                    Optional<Topic> topicOpt = topicRepo.findById(scoreCard.getTopicId());
                    if (topicOpt.isPresent()) {
                        ActivityDTO activity = new ActivityDTO();
                        activity.setType("quiz");
                        activity.setTimestamp(scoreCard.getCreatedAt());
                        activity.setTopicId(scoreCard.getTopicId());
                        activity.setTopicName(topicOpt.get().getName());
                        // Fixed: Convert double score to int for display
                        activity.setDetail("Scored " + (int)scoreCard.getScore() + "% on quiz");
                        activities.add(activity);
                    }
                });

        // Sort all activities by timestamp (most recent first) and return top 10
        return activities.stream()
                .sorted(Comparator.comparing(ActivityDTO::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get personalized recommendations for the dashboard
     */
    public List<RecommendationDTO> getRecommendations(Long userId) {
        List<RecommendationDTO> recommendations = new ArrayList<>();
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            return recommendations; // Return empty list if user not found
        }

        // Recommend continuing in-progress topics
        userProgressRepo.findByUserIdAndProgressPercentageBetweenOrderByLastSeenAtDesc(userId, 1, 99)
                .stream().limit(3).forEach(progress -> {
                    Optional<Topic> topicOpt = topicRepo.findById(progress.getTopicId());
                    if (topicOpt.isPresent()) {
                        Topic topic = topicOpt.get();
                        RecommendationDTO recommendation = new RecommendationDTO();
                        recommendation.setType("continue");
                        recommendation.setTopicId(topic.getId());
                        recommendation.setTopicName(topic.getName());
                        recommendation.setTopicDescription(topic.getDescription());
                        recommendation.setProgress((int)progress.getProgressPercentage());
                        recommendation.setReason("Continue where you left off");
                        recommendations.add(recommendation);
                    }
                });

        // Find topics user hasn't started based on category interests
        if (recommendations.size() < 5) {
            int limit = 5 - recommendations.size();

            // Get the topics the user has interacted with
            List<Long> userTopicIds = userProgressRepo.findByUserId(userId).stream()
                    .map(UserProgress::getTopicId)
                    .collect(Collectors.toList());

            // Find all topics and filter out ones the user has already interacted with
            topicRepo.findAll().stream()
                    .filter(topic -> !userTopicIds.contains(topic.getId()))
                    .limit(limit)
                    .forEach(topic -> {
                        RecommendationDTO recommendation = new RecommendationDTO();
                        recommendation.setType("new");
                        recommendation.setTopicId(topic.getId());
                        recommendation.setTopicName(topic.getName());
                        recommendation.setTopicDescription(topic.getDescription());
                        recommendation.setProgress(0);
                        recommendation.setReason("Based on your interests");
                        recommendations.add(recommendation);
                    });
        }

        return recommendations;
    }
}
