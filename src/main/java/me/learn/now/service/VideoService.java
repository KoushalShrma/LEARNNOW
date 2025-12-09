package me.learn.now.service;

import me.learn.now.dto.VideoDTO;
import me.learn.now.model.Topic;
import me.learn.now.model.Video;
import me.learn.now.repository.TopicRepo;
import me.learn.now.repository.VideoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoService {
    @Autowired
    private VideoRepo vr; // yahi se DB calls jayengi
    
    @Autowired
    private TopicRepo tr; // topic fetch karne ke liye

    // Create from DTO
    public Video addFromDTO(VideoDTO dto) {
        Video v = new Video();
        v.setYoutubeId(dto.getYoutubeId());
        v.setTitle(dto.getTitle());
        v.setChannel(dto.getChannel());
        v.setDuration(dto.getDuration());
        v.setLanguage(dto.getLanguage());
        v.setPosition(dto.getPosition());
        v.setSubtopic(dto.getSubtopic());
        v.setChaptersJson(dto.getChaptersJson());
        
        // Set topic if provided
        if (dto.getTopic() != null && dto.getTopic().getId() != null) {
            Topic topic = tr.findById(dto.getTopic().getId())
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + dto.getTopic().getId()));
            v.setTopic(topic);
        }
        
        return vr.save(v);
    }

    // Create
    public Video add(Video v){
        return vr.save(v);
    }

    // Read one
    public Optional<Video> get(Long id){
        return vr.findById(id);
    }

    // Read all
    public List<Video> list(){
        return vr.findAll();
    }

    // Update
    public Video update(Long id, Video input){
        Video v = vr.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        // jo fields zaroori lagti hain woh update kar rahe
        v.setTitle(input.getTitle());
        v.setChannel(input.getChannel());
        v.setYoutubeId(input.getYoutubeId());
        v.setDuration(input.getDuration());
        v.setLanguage(input.getLanguage());
        v.setPosition(input.getPosition());
        v.setChaptersJson(input.getChaptersJson());
        v.setTopic(input.getTopic());
        v.setQuiz(input.getQuiz());
        return vr.save(v);
    }

    // Delete
    public Optional<Video> delete(Long id){
        Optional<Video> v = vr.findById(id);
        v.ifPresent(vr::delete);
        return v;
    }
}
