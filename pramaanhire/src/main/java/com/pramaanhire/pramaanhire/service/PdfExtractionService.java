package com.pramaanhire.pramaanhire.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class PdfExtractionService {

    public String extractTextFromPdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return extractTextFromStream(inputStream);
        } catch (IOException e) {
            log.error("Error extracting text from PDF MultipartFile", e);
            throw new RuntimeException("Failed to extract text from resume", e);
        }
    }

    public String extractTextFromResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return extractTextFromStream(inputStream);
        } catch (IOException e) {
            log.error("Error extracting text from PDF Resource", e);
            throw new RuntimeException("Failed to extract text from stored resume", e);
        }
    }

    private String extractTextFromStream(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                log.warn("PDF is encrypted, cannot extract text.");
                return "";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing PDF document", e);
        }
    }
}
