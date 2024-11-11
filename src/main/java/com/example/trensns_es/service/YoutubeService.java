package com.example.trensns_es.service;
import com.example.trensns_es.model.YoutubeModel;
import com.example.trensns_es.repository.YoutubeRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@AllArgsConstructor
public class YoutubeService {

    @Autowired
    private final YoutubeRepository youtubeRepository;

    public void save(@RequestBody YoutubeModel youtubeModel){
        youtubeRepository.save(youtubeModel);
    }

    public YoutubeModel findById(@PathVariable String id){
        return youtubeRepository.findById(id).orElse(null);
    }

    public Iterable<YoutubeModel> findAll(){
        return youtubeRepository.findAll();
    }

    public void deleteById(@PathVariable String id){
        youtubeRepository.deleteById(id);
    }

    public void update(@RequestBody YoutubeModel youtube){
        youtubeRepository.save(youtube);
    }

    public void saveAll(@RequestBody List<YoutubeModel> youtubeModelList) { youtubeRepository.saveAll(youtubeModelList);}

}

