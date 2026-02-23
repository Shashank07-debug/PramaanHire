package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.entity.AiEvaluation;
import com.pramaanhire.pramaanhire.entity.Application;
import com.pramaanhire.pramaanhire.entity.Job;
import com.pramaanhire.pramaanhire.repository.ApplicationRepository;
import com.pramaanhire.pramaanhire.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public byte[] exportApplicationsToExcel(Long jobId, Long hrId) {
        log.info("Starting Excel export for Job ID: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getHr().getId().equals(hrId)) {
            throw new AccessDeniedException("You are not authorized to export applications for this job");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId, Pageable.unpaged()).getContent();
        log.info("Found {} applications to export", applications.size());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Applications");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "ID", "Candidate Name", "Email", "Status", "AI Score", 
                "AI Summary", "Strengths", "Weaknesses", "Submitted At", "Resume URL"
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Application app : applications) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(app.getId());
                row.createCell(1).setCellValue(app.getCandidate().getFullName());
                row.createCell(2).setCellValue(app.getCandidate().getEmail());
                row.createCell(3).setCellValue(app.getStatus().name());
                
                if (app.getAiScore() != null) {
                    row.createCell(4).setCellValue(app.getAiScore().doubleValue());
                } else {
                    row.createCell(4).setCellValue("N/A");
                }

                row.createCell(5).setCellValue(app.getAiSummary() != null ? app.getAiSummary() : "N/A");
                
                AiEvaluation eval = app.getAiEvaluation();
                row.createCell(6).setCellValue(eval != null && eval.getStrengths() != null ? eval.getStrengths() : "N/A");
                row.createCell(7).setCellValue(eval != null && eval.getWeaknesses() != null ? eval.getWeaknesses() : "N/A");
                
                row.createCell(8).setCellValue(app.getSubmittedAt().toString());
                row.createCell(9).setCellValue(app.getResumeUrl());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            byte[] bytes = out.toByteArray();
            log.info("Excel file generated successfully. Size: {} bytes", bytes.length);
            
            return bytes;
        } catch (IOException e) {
            log.error("Error generating Excel file", e);
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }
}
