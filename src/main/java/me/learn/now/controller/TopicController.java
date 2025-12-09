package me.learn.now.controller;

import me.learn.now.dto.TopicDTO;
import me.learn.now.model.Topic;
import me.learn.now.model.Video;
import me.learn.now.model.Quiz;
import me.learn.now.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    @Autowired
    private TopicService ts;

    @PostMapping
    public ResponseEntity<Topic> addTopic(@RequestBody Topic topic){
        return ResponseEntity.ok(ts.addTopic(topic));
    }

    @GetMapping
    public ResponseEntity<List<TopicDTO>> getTopics(){
        return ResponseEntity.ok(ts.getTopics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> getTopicById(@PathVariable Long id){
        Optional<Topic> t = ts.getTopicById(id);
        return t.map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> changeTopic(@PathVariable Long id, @RequestBody Topic topic){
        return ResponseEntity.ok(ts.changeTopic(id, topic));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Topic> deleteTopic(@PathVariable Long id){
        Optional<Topic> t = ts.deleteTopic(id);
        return t.map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/videos")
    public ResponseEntity<List<Video>> getVideoForTopic(@PathVariable("id") Long id){
        return ResponseEntity.ok(ts.getVideoForTopic(id));
    }

    @GetMapping("/{id}/quizzes")
    public ResponseEntity<List<Quiz>> getQuizzesForTopic(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ts.getQuizzesForTopic(id));
    }

    @PatchMapping("/{id}/videos/reorder")
    public ResponseEntity<List<Video>> reorderTopicVideos(@PathVariable("id") Long id,
                                                          @RequestBody List<Long> orderedVideoIds) {
        return ResponseEntity.ok(ts.reorderVideos(id, orderedVideoIds));
    }
}
