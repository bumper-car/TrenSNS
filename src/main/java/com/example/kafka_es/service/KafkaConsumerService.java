package com.example.kafka_es.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kafka_es.model.CommentModel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaConsumerService {
    private final CommentService commentService;
    private final ObjectMapper objectMapper;
    private final List<CommentModel> commentBuffer = new ArrayList<>();
    private static final int BULK_SIZE = 10; // Bulk 저장 크기
    public KafkaConsumerService(CommentService commentService, ObjectMapper objectMapper) {
        this.commentService = commentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "youtube_comment", groupId = "comment-group")
    public void consume(String message) {
        try {

            // JSON 메시지를 Comment 객체로 변환
            CommentModel comment = objectMapper.readValue(message, CommentModel.class);

            // 버퍼에 댓글 추가
            commentBuffer.add(comment);

            // 버퍼가 BULK_SIZE에 도달하면 Elasticsearch에 저장
            if (commentBuffer.size() >= BULK_SIZE) {
                commentService.saveCommentsToES(commentBuffer);
                commentBuffer.clear(); // 버퍼 초기화
            }

            System.out.println("Consumed comment: " + comment);
        } catch (Exception e) {
            System.err.println("Failed to consume message: " + e.getMessage());
        }
    }
}
