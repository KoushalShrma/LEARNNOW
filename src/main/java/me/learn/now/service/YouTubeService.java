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
            // Add "tutorial" and "course" to ensure educational content
            String searchQuery = keyword + " tutorial course explained";

            YouTube.Search.List search = youTube.search().list(List.of("snippet"));
            search.setKey(apiKey);
            search.setQ(searchQuery);
            search.setType(List.of("video"));
            search.setMaxResults((long) Math.min(maxResults * 5, 50)); // Fetch 5x more for filtering, max 50
            search.setOrder("relevance");
            search.setVideoDefinition("high");
            search.setVideoCategoryId("27"); // Education category
            search.setVideoEmbeddable("true"); // Only embeddable videos
            search.setVideoSyndicated("true"); // Only videos that can be played outside YouTube
            search.setSafeSearch("strict"); // Filter out inappropriate content
            search.setRelevanceLanguage("en"); // English content

            // Hinglish: API call kar ke results laate hai
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            System.out.println("[YouTube Search] Query: " + searchQuery + ", Found " + (searchResults != null ? searchResults.size() : 0) + " results");

            List<YouTubeVideoDto> videos = new ArrayList<>();
            int skippedCount = 0;

            // Hinglish: har search result ko process kar ke DTO banate hai
            for (SearchResult result : searchResults) {
                String title = result.getSnippet().getTitle().toLowerCase();
                String description = result.getSnippet().getDescription().toLowerCase();
                
                // Skip roadmap, career, and motivational videos
                if (title.contains("roadmap") || title.contains("career path") || 
                    title.contains("how to become") || title.contains("should you learn") ||
                    title.contains("vs ") && title.contains("which") ||
                    title.contains("top 10") || title.contains("best 10") ||
                    title.contains("in 2024") || title.contains("in 2025")) {
                    skippedCount++;
                    System.out.println("[YouTube Search] Skipped roadmap/career: " + result.getSnippet().getTitle());
                    continue;
                }
                
                // Skip music, songs, and shorts
                if (title.contains(" song") || title.contains(" music ") || 
                    title.contains(" lyrics") || title.contains("#shorts")) {
                    skippedCount++;
                    System.out.println("[YouTube Search] Skipped music/shorts: " + result.getSnippet().getTitle());
                    continue;
                }
                
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

            System.out.println("[YouTube Search] Returning " + videos.size() + " videos after filtering (" + skippedCount + " skipped)");
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

    /**
     * Calculate quality score for a video based on multiple factors
     * Score range: 0-100
     */
    public double calculateVideoQualityScore(YouTubeVideoDto video) {
        double score = 0.0;
        
        // 1. View Count Score (0-25 points)
        // Videos with 10K-1M views get best score (avoid both too low and viral non-educational)
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        if (views >= 10000 && views <= 1000000) {
            score += 25;
        } else if (views >= 5000 && views < 10000) {
            score += 20;
        } else if (views >= 1000000 && views <= 5000000) {
            score += 20;
        } else if (views >= 1000 && views < 5000) {
            score += 15;
        } else if (views > 5000000) {
            score += 10; // Too viral, might not be best educational content
        }
        
        // 2. Duration Score (0-25 points)
        // Educational videos should be 10-60 minutes
        Long duration = video.getDuration() != null ? video.getDuration() : 0L;
        if (duration >= 600 && duration <= 3600) { // 10-60 minutes
            score += 25;
        } else if (duration >= 3600 && duration <= 7200) { // 60-120 minutes
            score += 20;
        } else if (duration >= 300 && duration < 600) { // 5-10 minutes
            score += 15;
        } else if (duration > 7200) { // > 2 hours
            score += 10;
        } else if (duration < 300 && duration > 0) { // < 5 minutes
            score += 5; // Too short for quality education
        }
        
        // 3. Title Quality Score (0-20 points)
        String title = video.getTitle().toLowerCase();
        
        // Penalize roadmap/career/motivational videos
        if (title.contains("roadmap") || title.contains("career") || 
            title.contains("should you") || title.contains("top 10")) {
            score -= 20; // Heavy penalty
        }
        
        // Reward technical tutorial content
        if (title.contains("complete") || title.contains("full course") || title.contains("comprehensive")) {
            score += 10;
        }
        if (title.contains("tutorial") || title.contains("explained") || title.contains("guide")) {
            score += 5;
        }
        if (title.contains("beginner") || title.contains("basics") || title.contains("fundamentals")) {
            score += 5;
        }
        
        // Bonus for hands-on/project-based content
        if (title.contains("project") || title.contains("build") || title.contains("create")) {
            score += 3;
        }
        
        // 4. Channel Credibility (0-15 points)
        String channel = video.getChannelTitle().toLowerCase();
        String[] highQualityChannels = {
            "traversy media", "programming with mosh", "freecodecamp",
            "the net ninja", "academind", "codevolution",
            "codewithharry", "apna college", "chai aur code",
            "hitesh choudhary", "telusko", "java brains"
        };
        
        for (String qualityChannel : highQualityChannels) {
            if (channel.contains(qualityChannel)) {
                score += 15;
                break;
            }
        }
        
        // 5. Description Quality (0-15 points)
        String description = video.getDescription() != null ? video.getDescription().toLowerCase() : "";
        if (description.length() > 200) { // Detailed description
            score += 5;
        }
        if (description.contains("github") || description.contains("source code")) {
            score += 5;
        }
        if (description.contains("timestamps") || description.contains("chapters")) {
            score += 5;
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * Select the best video from a list based on quality scoring
     */
    public YouTubeVideoDto selectBestVideo(List<YouTubeVideoDto> videos) {
        if (videos == null || videos.isEmpty()) {
            return null;
        }
        
        YouTubeVideoDto bestVideo = videos.get(0);
        double bestScore = calculateVideoQualityScore(bestVideo);
        
        for (YouTubeVideoDto video : videos) {
            double score = calculateVideoQualityScore(video);
            if (score > bestScore) {
                bestScore = score;
                bestVideo = video;
            }
        }
        
        System.out.println("[YouTube Selection] Best video: " + bestVideo.getTitle() + 
                         " (Score: " + String.format("%.1f", bestScore) + "/100)");
        return bestVideo;
    }

    /**
     * Search and select the best educational video for a topic
     */
    public YouTubeVideoDto searchBestVideo(String keyword) {
        List<YouTubeVideoDto> candidates = searchEducationalVideos(keyword, 10);
        return selectBestVideo(candidates);
    }
    
    /**
     * Search and select TOP 3 best videos for a subtopic with language filtering
     * Returns 1-3 videos based on quality and diversity
     * @param keyword - search keyword
     * @param maxResults - max videos to return (1-3)
     * @param language - language preference: English, Hindi, Spanish, French, German, Multi
     */
    public List<YouTubeVideoDto> searchBestVideos(String keyword, int maxResults, String language) {
        List<YouTubeVideoDto> candidates = searchEducationalVideosByLanguage(keyword, Math.max(maxResults * 5, 15), language);
        
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Score all candidates
        List<VideoScore> scoredVideos = new ArrayList<>();
        for (YouTubeVideoDto video : candidates) {
            double score = calculateVideoQualityScore(video);
            // Boost score for language-matching videos
            if (isVideoInLanguage(video, language)) {
                score += 15; // Language match bonus
            }
            scoredVideos.add(new VideoScore(video, score));
        }
        
        // Sort by score descending
        scoredVideos.sort((a, b) -> Double.compare(b.score, a.score));
        
        // Take top videos (max 3)
        int count = Math.min(3, Math.min(maxResults, scoredVideos.size()));
        List<YouTubeVideoDto> bestVideos = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            bestVideos.add(scoredVideos.get(i).video);
            System.out.println(String.format("[YouTube Selection] #%d: %s (Score: %.1f/100, Lang: %s)", 
                i + 1, scoredVideos.get(i).video.getTitle(), scoredVideos.get(i).score, language));
        }
        
        return bestVideos;
    }
    
    /**
     * Search educational videos with language filtering
     * Supports: English, Hindi, Spanish, French, German, Multi (mixed)
     */
    public List<YouTubeVideoDto> searchEducationalVideosByLanguage(String keyword, int maxResults, String language) {
        try {
            if (youTube == null) {
                initializeYouTubeService();
            }

            String searchQuery;
            String relevanceLanguage;
            
            // Build language-specific search query
            switch (language.toLowerCase()) {
                case "hindi":
                    searchQuery = keyword + " tutorial in hindi हिंदी";
                    relevanceLanguage = "hi";
                    break;
                case "spanish":
                    searchQuery = keyword + " tutorial en español";
                    relevanceLanguage = "es";
                    break;
                case "french":
                    searchQuery = keyword + " tutoriel en français";
                    relevanceLanguage = "fr";
                    break;
                case "german":
                    searchQuery = keyword + " tutorial auf deutsch";
                    relevanceLanguage = "de";
                    break;
                case "multi":
                    // Multi-language: search without language filter, get mixed results
                    searchQuery = keyword + " tutorial course";
                    relevanceLanguage = null; // No language filter
                    break;
                case "english":
                default:
                    searchQuery = keyword + " tutorial course explained";
                    relevanceLanguage = "en";
                    break;
            }

            YouTube.Search.List search = youTube.search().list(List.of("snippet"));
            search.setKey(apiKey);
            search.setQ(searchQuery);
            search.setType(List.of("video"));
            search.setMaxResults((long) Math.min(maxResults, 50));
            search.setOrder("relevance");
            search.setVideoDefinition("high");
            search.setVideoCategoryId("27"); // Education category
            search.setVideoEmbeddable("true");
            search.setVideoSyndicated("true");
            search.setSafeSearch("strict");
            
            // Set relevance language if not Multi
            if (relevanceLanguage != null) {
                search.setRelevanceLanguage(relevanceLanguage);
            }

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            System.out.println("[YouTube Search] Query: " + searchQuery + ", Language: " + language + 
                             ", Found " + (searchResults != null ? searchResults.size() : 0) + " results");

            List<YouTubeVideoDto> videos = new ArrayList<>();

            for (SearchResult result : searchResults) {
                String title = result.getSnippet().getTitle().toLowerCase();
                
                // Skip roadmap, career, and motivational videos
                if (title.contains("roadmap") || title.contains("career path") || 
                    title.contains("how to become") || title.contains("should you learn") ||
                    title.contains("vs ") && title.contains("which") ||
                    title.contains("top 10") || title.contains("best 10")) {
                    continue;
                }
                
                // Skip music, songs, and shorts
                if (title.contains(" song") || title.contains(" music ") || 
                    title.contains(" lyrics") || title.contains("#shorts")) {
                    continue;
                }
                
                // Language validation for specific language requests (not Multi)
                if (!language.equalsIgnoreCase("multi") && !isVideoMatchingLanguage(result, language)) {
                    continue;
                }
                
                YouTubeVideoDto video = new YouTubeVideoDto();
                video.setVideoId(result.getId().getVideoId());
                video.setTitle(result.getSnippet().getTitle());
                video.setDescription(result.getSnippet().getDescription());
                video.setChannelTitle(result.getSnippet().getChannelTitle());
                video.setPublishedAt(result.getSnippet().getPublishedAt().toString());

                if (result.getSnippet().getThumbnails() != null &&
                    result.getSnippet().getThumbnails().getMedium() != null) {
                    video.setThumbnailUrl(result.getSnippet().getThumbnails().getMedium().getUrl());
                }

                video.setVideoUrl("https://www.youtube.com/watch?v=" + video.getVideoId());
                videos.add(video);
            }

            fetchVideoStatistics(videos);
            return videos;

        } catch (Exception e) {
            System.err.println("Error searching videos by language: " + e.getMessage());
            // Fallback to default search
            return searchEducationalVideos(keyword, maxResults);
        }
    }
    
    /**
     * Check if video matches the requested language based on title/channel
     */
    private boolean isVideoMatchingLanguage(SearchResult result, String language) {
        String title = result.getSnippet().getTitle().toLowerCase();
        String channel = result.getSnippet().getChannelTitle().toLowerCase();
        String description = result.getSnippet().getDescription().toLowerCase();
        
        switch (language.toLowerCase()) {
            case "hindi":
                // Check for Hindi indicators
                return title.contains("hindi") || title.contains("हिंदी") ||
                       channel.contains("hindi") || channel.contains("apna college") ||
                       channel.contains("codewithharry") || channel.contains("chai aur code") ||
                       channel.contains("hitesh") || channel.contains("telusko") ||
                       description.contains("hindi") || description.contains("हिंदी");
            case "spanish":
                return title.contains("español") || title.contains("spanish") ||
                       description.contains("español");
            case "french":
                return title.contains("français") || title.contains("french") ||
                       description.contains("français");
            case "german":
                return title.contains("deutsch") || title.contains("german") ||
                       description.contains("deutsch");
            case "english":
                // English: exclude non-English indicators
                return !title.contains("hindi") && !title.contains("हिंदी") &&
                       !title.contains("español") && !title.contains("français") &&
                       !title.contains("deutsch");
            default:
                return true; // Multi or unknown - accept all
        }
    }
    
    /**
     * Check if a video DTO matches the language
     */
    private boolean isVideoInLanguage(YouTubeVideoDto video, String language) {
        String title = video.getTitle().toLowerCase();
        String channel = video.getChannelTitle().toLowerCase();
        
        switch (language.toLowerCase()) {
            case "hindi":
                return title.contains("hindi") || title.contains("हिंदी") ||
                       channel.contains("hindi") || channel.contains("apna college") ||
                       channel.contains("codewithharry") || channel.contains("chai");
            case "spanish":
                return title.contains("español") || title.contains("spanish");
            case "french":
                return title.contains("français") || title.contains("french");
            case "german":
                return title.contains("deutsch") || title.contains("german");
            case "english":
                return !title.contains("hindi") && !title.contains("español") &&
                       !title.contains("français") && !title.contains("deutsch");
            default:
                return true;
        }
    }
    
    /**
     * Helper class to pair videos with their scores
     */
    private static class VideoScore {
        YouTubeVideoDto video;
        double score;
        
        VideoScore(YouTubeVideoDto video, double score) {
            this.video = video;
            this.score = score;
        }
    }
}

