package com.example.kafka_es.service;

import com.example.kafka_es.model.SentimentModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kafka_es.model.CommentModel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaConsumerService {
    private final CommentService commentService;
    private final SentimentService sentimentService;
    private final ObjectMapper objectMapper;
    private final List<CommentModel> commentBuffer = new ArrayList<>();
    private final List<SentimentModel> sentimentBuffer = new ArrayList<>();
    private static final int BULK_SIZE = 10; // Bulk 저장 크기
    public KafkaConsumerService(CommentService commentService, SentimentService sentimentService, ObjectMapper objectMapper) {
        this.commentService = commentService;
        this.sentimentService=sentimentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "youtube_comment", groupId = "ES-group")
    public void consumeYoutube(String message) {
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
    @KafkaListener(topics = "sentiment", groupId = "ES-group")
    public void consumeSentiment(String message) {
        try {
            // JSON 메시지를 Tree 형태로 읽기
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode sentimentAnalysis = rootNode.get("sentiment_analysis");

            if (sentimentAnalysis != null) {
                // SentimentModel 객체 생성 및 데이터 매핑 중첩 구조이기 때문에 직접 매핑을 해준다.
                SentimentModel sentiment = new SentimentModel();
                sentiment.setSentiment(sentimentAnalysis.get("Sentiment").asText());
                sentiment.setPositive(sentimentAnalysis.get("SentimentScore").get("Positive").asDouble());
                sentiment.setNegative(sentimentAnalysis.get("SentimentScore").get("Negative").asDouble());
                sentiment.setNeutral(sentimentAnalysis.get("SentimentScore").get("Neutral").asDouble());
                sentiment.setMixed(sentimentAnalysis.get("SentimentScore").get("Mixed").asDouble());

                // 버퍼에 추가
                sentimentBuffer.add(sentiment);

                // 버퍼가 BULK_SIZE에 도달하면 Elasticsearch에 저장
                if (sentimentBuffer.size() >= BULK_SIZE) {
                    sentimentService.saveSentimentsToES(sentimentBuffer);
                    sentimentBuffer.clear();
                }

                System.out.println("Consumed sentiment: " + sentiment);
            } else {
                System.err.println("Message does not contain sentiment_analysis: " + message);
            }
        } catch (Exception e) {
            System.err.println("Failed to consume message: " + e.getMessage());
        }
    }

}
