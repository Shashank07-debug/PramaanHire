package com.pramaanhire.pramaanhire.service;

import com.pramaanhire.pramaanhire.dto.RejectionEmailDto;
import com.pramaanhire.pramaanhire.entity.Application;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendSubmissionEmail(String toEmail, String candidateName, String jobTitle) {
        String subject = "Application Received: " + jobTitle;
        String content = buildSubmissionEmailContent(candidateName, jobTitle);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendShortlistedEmail(String toEmail, String candidateName, String jobTitle) {
        String subject = "Good News! You've been Shortlisted for " + jobTitle;
        String content = buildShortlistedEmailContent(candidateName, jobTitle);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendHiredEmail(String toEmail, String candidateName, String jobTitle) {
        String subject = "Congratulations! Offer for " + jobTitle;
        String content = buildHiredEmailContent(candidateName, jobTitle);
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendRejectionEmail(RejectionEmailDto emailDto) {
        String subject = "Update on your application for " + emailDto.getJobTitle();
        String content = buildRejectionEmailContent(emailDto);
        sendEmail(emailDto.getCandidateEmail(), subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to " + to, e);
        }
    }

    private String buildSubmissionEmailContent(String name, String jobTitle) {
        return "<html><body>" +
                "<h2>Application Received</h2>" +
                "<p>Dear " + name + ",</p>" +
                "<p>Thank you for applying for the <strong>" + jobTitle + "</strong> position at PramaanHire.</p>" +
                "<p>We have successfully received your application and resume. Our team (and our AI assistant) will review your profile shortly.</p>" +
                "<p>You can track your application status in your dashboard.</p>" +
                "<br><p>Best Regards,<br>PramaanHire Team</p>" +
                "</body></html>";
    }

    private String buildShortlistedEmailContent(String name, String jobTitle) {
        return "<html><body>" +
                "<h2>You've been Shortlisted! 🎉</h2>" +
                "<p>Dear " + name + ",</p>" +
                "<p>We are excited to inform you that your application for <strong>" + jobTitle + "</strong> has been shortlisted!</p>" +
                "<p>Your profile stood out to us, and we would like to move forward with the next steps in the hiring process.</p>" +
                "<p><strong>Shortly, we will contact you with details regarding the interview schedule.</strong></p>" +
                "<br><p>Best Regards,<br>PramaanHire Team</p>" +
                "</body></html>";
    }

    private String buildHiredEmailContent(String name, String jobTitle) {
        return "<html><body>" +
                "<h2>Congratulations! You're Hired! 🥂</h2>" +
                "<p>Dear " + name + ",</p>" +
                "<p>We are thrilled to offer you the position of <strong>" + jobTitle + "</strong> at PramaanHire!</p>" +
                "<p>Your skills and experience are a perfect match for our team, and we can't wait to have you on board.</p>" +
                "<p>Our HR team will send the official offer letter and onboarding details shortly.</p>" +
                "<br><p>Welcome to the team!</p>" +
                "<p>Best Regards,<br>PramaanHire Team</p>" +
                "</body></html>";
    }

    private String buildRejectionEmailContent(RejectionEmailDto dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Application Update</h2>");
        sb.append("<p>Dear ").append(dto.getCandidateName()).append(",</p>");
        sb.append("<p>Thank you for your interest in the <strong>").append(dto.getJobTitle()).append("</strong> position.</p>");
        sb.append("<p>After careful review, we have decided not to proceed with your application at this time.</p>");

        if (dto.getStrengths() != null || dto.getWeaknesses() != null || dto.getImprovementTips() != null) {
            sb.append("<h3>AI-Powered Feedback</h3>");
            sb.append("<p>To help you in your future job search, here is some personalized feedback based on your application:</p>");
            
            if (dto.getStrengths() != null) {
                sb.append("<h4>✅ Strengths</h4>");
                sb.append("<p>").append(dto.getStrengths()).append("</p>");
            }
            
            if (dto.getWeaknesses() != null) {
                sb.append("<h4>⚠️ Areas for Improvement</h4>");
                sb.append("<p>").append(dto.getWeaknesses()).append("</p>");
            }
            
            if (dto.getImprovementTips() != null) {
                sb.append("<h4>💡 Tips for Growth</h4>");
                sb.append("<p>").append(dto.getImprovementTips()).append("</p>");
            }
        }

        sb.append("<br><p>We wish you the best in your career journey.</p>");
        sb.append("<p>Best Regards,<br>PramaanHire Team</p>");
        sb.append("</body></html>");
        
        return sb.toString();
    }
}
