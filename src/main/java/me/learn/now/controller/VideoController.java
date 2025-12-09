package me.learn.now.controller;

import me.learn.now.dto.VideoDTO;
import me.learn.now.model.Video;
import me.learn.now.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // ye REST controller hai, JSON in/out karega
@RequestMapping("/api/videos")
public class VideoController {
    @Autowired
    private VideoService vs; // service layer jahan main logic rakhenge

    // Create video using DTO
    @PostMapping
    public ResponseEntity<Video> add(@RequestBody VideoDTO dto){
        return ResponseEntity.ok(vs.addFromDTO(dto));
    }

    // Get one video by id
    @GetMapping("/{id}")
    public ResponseEntity<Video> get(@PathVariable Long id){
        Optional<Video> v = vs.get(id);
        return v.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // List all videos (basic)
    @GetMapping
    public ResponseEntity<List<Video>> list(){
        return ResponseEntity.ok(vs.list());
    }

    // Update a video
    @PutMapping("/{id}")
    public ResponseEntity<Video> update(@PathVariable Long id, @RequestBody Video v){
        return ResponseEntity.ok(vs.update(id, v));
    }

    // Delete a video
    @DeleteMapping("/{id}")
    public ResponseEntity<Video> delete(@PathVariable Long id){
        Optional<Video> v = vs.delete(id);
        return v.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
