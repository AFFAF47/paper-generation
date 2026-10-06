package com.example.exam.papergenerator.service;

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
    String bucketName;

    public void uploadNotes(String objectKey, String subject, String className, String chapter) {
        try {
            //1. Stream PDF directly to S3
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build());

            // 2. Read the PDF from the upload
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new InputStreamResource(s3Stream));

            // 3. Split the long PDF into small chunks (so the AI doesn't get overwhelmed)
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> documents = splitter.apply(pdfReader.get());

            // 4. Add Metadata & Namespace logic
            // We create a unique namespace like "physics-class1"
            String namespace = (subject + "-class" + className).toLowerCase();

            for (Document doc : documents) {
                doc.getMetadata().put("chapter", chapter.toLowerCase());
                doc.getMetadata().put("subject", subject.toLowerCase());
                doc.getMetadata().put("class", className.toLowerCase());
            }

            // 5. Send to Pinecone
            // Note: In Spring AI, we pass the namespace via PineconeVectorStore options
            vectorStore.add(documents);

            System.out.println("Successfully uploaded " + documents.size() + " chunks to namespace: " + namespace);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF: " + e.getMessage());
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