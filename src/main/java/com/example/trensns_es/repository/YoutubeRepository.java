package com.example.trensns_es.repository;
import com.example.trensns_es.model.YoutubeModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface YoutubeRepository extends ElasticsearchRepository<YoutubeModel,String> {
}
