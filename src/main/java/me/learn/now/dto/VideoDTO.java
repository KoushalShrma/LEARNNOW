package me.learn.now.dto;

public class VideoDTO {
    private String youtubeId;
    private String title;
    private String channel;
    private Integer duration;
    private String language;
    private Integer position;
    private String subtopic;
    private String chaptersJson;
    private TopicReference topic;
    private QuizReference quiz;

    public static class TopicReference {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class QuizReference {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    // Getters and Setters
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

    public TopicReference getTopic() {
        return topic;
    }

    public void setTopic(TopicReference topic) {
        this.topic = topic;
    }

    public QuizReference getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizReference quiz) {
        this.quiz = quiz;
    }
}
