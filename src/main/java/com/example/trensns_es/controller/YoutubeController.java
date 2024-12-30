package com.example.trensns_es.controller;

import com.example.trensns_es.model.YoutubeModel;
import com.example.trensns_es.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/youtube")
public class YoutubeController {
    private final YoutubeService youtubeService;

    @PostMapping("/")
    public void save(@RequestBody YoutubeModel youtubeModel) {
        youtubeService.save(youtubeModel);
    }


    @PostMapping("/bulk")
    public void saveAll(@RequestBody List<YoutubeModel> youtubeModelList){
        youtubeService.saveAll(youtubeModelList);
    }

    @GetMapping("/{id}")
    public YoutubeModel findById(@PathVariable String id) {
        return youtubeService.findById(id);
    }

    @GetMapping
    public Iterable<YoutubeModel> findAll() {
        return youtubeService.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        youtubeService.deleteById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody YoutubeModel youtube) {
        youtubeService.update(youtube);
    }

}
