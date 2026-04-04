package com.example.exam.papergenerator.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

@Service
public class IngestionService {

    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void uploadNotes(MultipartFile file, String subject, String className, String chapter) {
        try {
            // 1. Read the PDF from the upload
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new InputStreamResource(file.getInputStream()));

            // 2. Split the long PDF into small chunks (so the AI doesn't get overwhelmed)
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> documents = splitter.apply(pdfReader.get());

            // 3. Add Metadata & Namespace logic
            // We create a unique namespace like "physics-class1"
            String namespace = (subject + "-class" + className).toLowerCase();

            for (Document doc : documents) {
                doc.getMetadata().put("chapter", chapter.toLowerCase());
                doc.getMetadata().put("subject", subject.toLowerCase());
                doc.getMetadata().put("class", className.toLowerCase());
            }

            // 4. Send to Pinecone
            // Note: In Spring AI, we pass the namespace via PineconeVectorStore options
            vectorStore.add(documents);

            System.out.println("Successfully uploaded " + documents.size() + " chunks to namespace: " + namespace);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF: " + e.getMessage());
        }
    }
}