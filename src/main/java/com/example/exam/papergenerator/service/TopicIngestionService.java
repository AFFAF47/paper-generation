package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.IngestionRequest;
import com.example.exam.papergenerator.model.KnowledgeTopic;
import com.example.exam.papergenerator.model.SourceType;
import com.example.exam.papergenerator.respository.KnowledgeTopicRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicIngestionService {

    private final KnowledgeTopicRepository topicRepository;
    private final IngestionService ingestionService;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.ingestion-queue-url}")
    private String ingestionQueueUrl;

    @SneakyThrows
    public KnowledgeTopic ingestSource (IngestionRequest request) {

        SourceType sourceType = request.getSourceType() != null ? request.getSourceType() : SourceType.URL;

        String sourceIdentifier = SourceType.PDF.equals(request.getSourceType())
                ? request.getObjectKey() : request.getUrl();

        if (sourceIdentifier == null || sourceIdentifier.isBlank()) {
            throw new IllegalArgumentException("Source URL or S3 ObjectKey is required");
        }

        String normalizedTopicId = request.getTopicId().trim().toLowerCase();
        String chapter = (request.getChapter() != null && !request.getChapter().isBlank())
                ? request.getChapter().toUpperCase() : "GENERAL";

        KnowledgeTopic topic = topicRepository.findById(normalizedTopicId)
                .orElseGet(() -> KnowledgeTopic.builder()
                        .id(normalizedTopicId)
                        .name(request.getTopicName())
                        .totalChunks(0)
                        .build());

        if (SourceType.PDF.equals(sourceType)) {
            return handlePdfIngestion(topic, request, sourceIdentifier, chapter);
        } else {
            return handleUrlIngestion(topic, sourceIdentifier, chapter);
        }
    }

    private KnowledgeTopic handlePdfIngestion (KnowledgeTopic topic, IngestionRequest request,
                                               String objectKey, String chapter) {
        //Track the PDF source in MongoDB
        KnowledgeTopic.TopicSource pdfSource = KnowledgeTopic.TopicSource.builder()
                .url(objectKey)
                .chapter(chapter)
                .type(SourceType.PDF)
                .fileName(request.getFileName())
                .status(KnowledgeTopic.TopicStatus.PROCESSING)
                .chunkCount(0)
                .build();

        topic.getSources().add(pdfSource);
        topic.setStatus(KnowledgeTopic.VectorStatus.INDEXING);
        topic.setUpdatedAt(Instant.now());
        topicRepository.save(topic);

        try {
            int chunksCreated = ingestionService.uploadTopicPdf(objectKey, topic.getId(), chapter);

            pdfSource.setStatus(KnowledgeTopic.TopicStatus.DONE);
            pdfSource.setChunkCount(chunksCreated);
            topic.setTotalChunks(topic.getTotalChunks() + chunksCreated);
            reCalculateOverallTopicStatus(topic);
            topic.setUpdatedAt(Instant.now());

            return topicRepository.save(topic);
        } catch (Exception e) {
            pdfSource.setStatus(KnowledgeTopic.TopicStatus.FAILED);
            pdfSource.setError(e.getMessage());
            reCalculateOverallTopicStatus(topic);
            topic.setUpdatedAt(Instant.now());
            topicRepository.save(topic);
            throw new RuntimeException("PDF ingenstion failed: " + e.getMessage(), e);
        }
    }

    private KnowledgeTopic handleUrlIngestion (KnowledgeTopic topic, String url,
                                               String chapter) {

        KnowledgeTopic.TopicSource urlSource = KnowledgeTopic.TopicSource.builder()
                .url(url)
                .chapter(chapter)
                .type(SourceType.URL)
                .status(KnowledgeTopic.TopicStatus.PENDING)
                .chunkCount(0)
                .build();

        topic.getSources().add(urlSource);
        topic.setStatus(KnowledgeTopic.VectorStatus.INDEXING);
        topic.setUpdatedAt(Instant.now());
        topicRepository.save(topic);

        log.info("Topic is saved with Id: {}", topic.getId());

        try {
            Map<String, String> sqsPayload = new HashMap<>();
            sqsPayload.put("topicId", topic.getId());
            sqsPayload.put("chapter", chapter);
            sqsPayload.put("sourceType", SourceType.URL.name());
            sqsPayload.put("url", url);

            String messageBody = objectMapper.writeValueAsString(sqsPayload);

            log.info("Preparing SQS message body for topic Id: {}", topic.getId());

            sqsClient.sendMessage(SendMessageRequest.builder()
                            .queueUrl(ingestionQueueUrl)
                            .messageBody(messageBody)
                            .build());

            log.info("Dispatched ingestion task to SQS for topicId={} chapter='{}' source={}",
                    topic.getId(), chapter, url);

            return topic;

        } catch (Exception e) {
            log.error("Failed to dispatch message to SQS for topicId={}", topic.getId());
            urlSource.setStatus(KnowledgeTopic.TopicStatus.FAILED);
            urlSource.setError("SQS dispatch failed: " + e.getMessage());
            reCalculateOverallTopicStatus(topic);
            topic.setUpdatedAt(Instant.now());
            topicRepository.save(topic);
            throw new RuntimeException("Failed to queue URL ingestion task: " + e.getMessage(), e);
        }
    }

    private void reCalculateOverallTopicStatus (KnowledgeTopic topic) {
        boolean anyPendingOrProcessing = topic.getSources().stream()
                .anyMatch(s -> KnowledgeTopic.TopicStatus.PENDING.equals(s.getStatus())
                        || KnowledgeTopic.TopicStatus.PROCESSING.equals(s.getStatus()));
        boolean anyDone = topic.getSources().stream()
                .anyMatch(s -> KnowledgeTopic.TopicStatus.DONE.equals(s.getStatus()));

        if (anyPendingOrProcessing) {
            topic.setStatus(KnowledgeTopic.VectorStatus.INDEXING);
        } else if (anyDone) {
            topic.setStatus(KnowledgeTopic.VectorStatus.READY);
        } else {
            topic.setStatus(KnowledgeTopic.VectorStatus.FAILED);
        }
    }
}
