package me.learn.now.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import me.learn.now.dto.youtube.YouTubeVideoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hinglish: YouTube Data API se videos search karne ke liye service class
 * Yaha hum YouTube API ko call kar ke learning videos dhoondte hai
 */
@Service
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.application-name}")
    private String applicationName;

    private YouTube youTube;

    /**
     * Hinglish: YouTube API client initialize karte hai
     * Yeh method application start hote time run hota hai
     */
    public void initializeYouTubeService() {
        try {
            youTube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null)
                .setApplicationName(applicationName)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("YouTube service initialize nahi ho payi: " + e.getMessage());
        }
    }

    /**
     * Hinglish: Keyword ke basis pe YouTube se educational videos search karte hai
     * @param keyword - kya search karna hai (java, python, etc.)
     * @param maxResults - kitni videos chahiye (default 10)
     * @return List of YouTube videos
     */
    public List<YouTubeVideoDto> searchEducationalVideos(String keyword, int maxResults) {
        try {
            // Hinglish: agar YouTube service initialize nahi hui hai toh pehle initialize karte hai
            if (youTube == null) {
                initializeYouTubeService();
            }

            // Hinglish: YouTube search query banate hai educational videos ke liye
            String searchQuery = keyword + " tutorial programming learn";

            YouTube.Search.List search = youTube.search().list(List.of("snippet"));
            search.setKey(apiKey);
            search.setQ(searchQuery);
            search.setType(List.of("video"));
            search.setMaxResults((long) maxResults);
            search.setOrder("relevance");
            search.setVideoDefinition("high");
            search.setVideoCategoryId("27"); // Education category

            // Hinglish: API call kar ke results laate hai
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            List<YouTubeVideoDto> videos = new ArrayList<>();

            // Hinglish: har search result ko process kar ke DTO banate hai
            for (SearchResult result : searchResults) {
                YouTubeVideoDto video = new YouTubeVideoDto();
                video.setVideoId(result.getId().getVideoId());
                video.setTitle(result.getSnippet().getTitle());
                video.setDescription(result.getSnippet().getDescription());
                video.setChannelTitle(result.getSnippet().getChannelTitle());
                video.setPublishedAt(result.getSnippet().getPublishedAt().toString());

                // Hinglish: thumbnail URL set karte hai
                if (result.getSnippet().getThumbnails() != null &&
                    result.getSnippet().getThumbnails().getMedium() != null) {
                    video.setThumbnailUrl(result.getSnippet().getThumbnails().getMedium().getUrl());
                }

                video.setVideoUrl("https://www.youtube.com/watch?v=" + video.getVideoId());
                videos.add(video);
            }

            // Hinglish: additional details ke liye video statistics bhi fetch kar sakte hai
            fetchVideoStatistics(videos);

            return videos;

        } catch (Exception e) {
            throw new RuntimeException("YouTube search mein error aaya: " + e.getMessage());
        }
    }

    /**
     * Hinglish: Video ki additional details like views, duration fetch karte hai
     */
    private void fetchVideoStatistics(List<YouTubeVideoDto> videos) {
        try {
            // Hinglish: video IDs ki list banate hai
            StringBuilder videoIds = new StringBuilder();
            for (int i = 0; i < videos.size(); i++) {
                if (i > 0) videoIds.append(",");
                videoIds.append(videos.get(i).getVideoId());
            }

            // Hinglish: video statistics API call karte hai
            YouTube.Videos.List videoRequest = youTube.videos().list(List.of("statistics", "contentDetails"));
            videoRequest.setKey(apiKey);
            videoRequest.setId(List.of(videoIds.toString()));

            VideoListResponse videoResponse = videoRequest.execute();

            // Hinglish: statistics ko videos mein set karte hai
            for (int i = 0; i < videos.size() && i < videoResponse.getItems().size(); i++) {
                YouTubeVideoDto video = videos.get(i);
                var youtubeVideo = videoResponse.getItems().get(i);

                if (youtubeVideo.getStatistics() != null) {
                    if (youtubeVideo.getStatistics().getViewCount() != null) {
                        video.setViewCount(youtubeVideo.getStatistics().getViewCount().longValue());
                    }
                }

                // Hinglish: duration parse karte hai (PT4M20S format se seconds mein)
                if (youtubeVideo.getContentDetails() != null &&
                    youtubeVideo.getContentDetails().getDuration() != null) {
                    String duration = youtubeVideo.getContentDetails().getDuration();
                    video.setDuration(parseDurationToSeconds(duration));
                }
            }
        } catch (Exception e) {
            // Hinglish: agar statistics fetch nahi ho payi toh error log karte hai but fail nahi karte
            System.err.println("Video statistics fetch karne mein error: " + e.getMessage());
        }
    }

    /**
     * Hinglish: YouTube duration format (PT4M20S) ko seconds mein convert karte hai
     */
    private Long parseDurationToSeconds(String duration) {
        try {
            Duration d = Duration.parse(duration);
            return d.getSeconds();
        } catch (Exception e) {
            return 0L; // Default value if parsing fails
        }
    }

    /**
     * Hinglish: Popular programming channels se videos search karte hai
     */
    public List<YouTubeVideoDto> getPopularProgrammingVideos(int maxResults) {
        String[] popularChannels = {
            "CodeWithHarry", "Apna College", "Programming with Mosh",
            "Java Brains", "Spring Developer", "Traversy Media"
        };

        // Hinglish: random channel select kar ke uski videos fetch karte hai
        String channelQuery = popularChannels[(int) (ThreadLocalRandom.current().nextDouble() * popularChannels.length)];
        return searchEducationalVideos(channelQuery, maxResults);
    }

    /**
     * Hinglish: Specific topic ke liye curated learning videos
     */
    public List<YouTubeVideoDto> getVideosByTopic(String topic, int maxResults) {
        String searchQuery = switch (topic.toLowerCase()) {
            case "java" -> "java programming tutorial for beginners";
            case "python" -> "python programming tutorial complete course";
            case "javascript" -> "javascript tutorial for beginners complete";
            case "react" -> "react js tutorial for beginners";
            case "spring" -> "spring boot tutorial java";
            case "angular" -> "angular tutorial for beginners";
            case "node" -> "node js tutorial for beginners";
            case "database" -> "sql database tutorial for beginners";
            default -> topic + " programming tutorial";
        };

        return searchEducationalVideos(searchQuery, maxResults);
    }
}
