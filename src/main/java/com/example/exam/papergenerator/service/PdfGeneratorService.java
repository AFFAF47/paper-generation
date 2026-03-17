package com.example.exam.papergenerator.service;

import com.example.exam.papergenerator.model.ExamRecord;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class PdfGeneratorService {

    public byte[] generateExamPdf(ExamRecord record) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50); // Added margins

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. ADD LOGO
            try {
                // Load image from resources
                ClassPathResource res = new ClassPathResource("static/images/school-logo.png");
                InputStream is = res.getInputStream();
                Image logo = Image.getInstance(is.readAllBytes());

                logo.scaleToFit(80, 80); // Resize logo
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found, skipping: " + e.getMessage());
            }

            // 2. Setup Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            // 3. Add Header Text
            Paragraph title = new Paragraph("OFFICIAL EXAM PAPER", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            document.add(title);

            Paragraph details = new Paragraph(
                    String.format("Subject: %s | Class: %s\nChapter: %s\nDate: %s\n\n",
                            record.getSubject(), record.getClassName(), record.getChapter(),
                            record.getCreatedAt().toLocalDate()),
                    subHeaderFont
            );
            details.setAlignment(Element.ALIGN_CENTER);
            document.add(details);

            // Line separator
            document.add(new Paragraph("______________________________________________________________________________\n\n"));

            // 4. ADD THE AI CONTENT WITH PAGE BREAK LOGIC
            // Split the content based on the marker we told the AI to use
            String[] sections = record.getContent().split("--- END OF PAPER ---");

            // Add the first section (The Question Paper)
            Paragraph questionPaper = new Paragraph(sections[0], bodyFont);
            document.add(questionPaper);

            // Check if an Answer Key exists and force it to a new page
            if (sections.length > 1) {
                document.newPage(); // This is the magic line that starts a fresh page

                Paragraph answerHeader = new Paragraph("ANSWER KEY / MEMORANDUM", headerFont);
                answerHeader.setAlignment(Element.ALIGN_CENTER);
                answerHeader.setSpacingAfter(20);
                document.add(answerHeader);

                Paragraph answerContent = new Paragraph(sections[1], bodyFont);
                document.add(answerContent);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}