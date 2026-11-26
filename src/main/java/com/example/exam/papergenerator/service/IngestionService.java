package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.KnowledgeTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.InputStreamResource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;
    private final S3Client s3Client;

    @Value("${app.bucket-name:exam-paper-generator-bucket-558260070804-ap-south-1-an}")
    private String bucketName;

    public int uploadTopicPdf(String objectKey, String topicId, String chapter) {
        try {
            // 1. Stream PDF directly from S3
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build());

            // 2. Read the PDF
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new InputStreamResource(s3Stream));

            // 3. Split into chunks
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> documents = splitter.apply(pdfReader.get());

            // 4. Stamp Workflow A metadata (aligned with Python URL worker)
            String normalizedTopicId = topicId.trim().toLowerCase();
            String normalizedChapter = (chapter != null && !chapter.isBlank())
                    ? chapter
                    : "GENERAL";

            for (Document doc : documents) {
                doc.getMetadata().put("topicId", normalizedTopicId);
                doc.getMetadata().put("chapter", normalizedChapter);
                doc.getMetadata().put("sourceUrl", objectKey);
            }

            // 5. Upsert to Pinecone
            vectorStore.add(documents);
            log.info("Uploaded {} PDF chunks for topicId='{}', chapter='{}'",
                    documents.size(), normalizedTopicId, normalizedChapter);

            return documents.size();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF: " + e.getMessage(), e);
        } finally {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build());
                log.info("Purged temporary S3 file: {}", objectKey);
            } catch (Exception e) {
                log.warn("Failed to delete S3 file {}, lifecycle rule will catch it: {}", objectKey, e.getMessage());
            }
        }
    }
}