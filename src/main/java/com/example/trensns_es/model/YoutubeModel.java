package com.example.trensns_es.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName="youtube")
public class YoutubeModel {

    @Id
    private String id;

    @Field(type = FieldType.Text,name="channel_title")
    private String channel_title;

    @Field(type = FieldType.Text,name="published_at")
    private String published_at;

    @Field(type = FieldType.Text,name="description")
    private String description;

    @Field(type = FieldType.Integer,name="like_count")
    private Integer like_count;

    @Field(type = FieldType.Integer,name="comments_count")
    private Integer comments_count;

    @Field(type = FieldType.Long,name="view_count")
    private Long view_count;

    @Field(type = FieldType.Text,name="comments")
    private List<String> comments;
}
