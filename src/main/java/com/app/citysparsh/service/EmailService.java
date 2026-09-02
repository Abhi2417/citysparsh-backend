package com.app.citysparsh.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.app-name}")
    private String appName;

    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);  // true = HTML
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println(">>> Email send failed");
            e.printStackTrace();
        }
    }

    // ── Citizen: status changed ───────────────────
    public void sendStatusUpdateEmail(
            String citizenEmail,
            String citizenName,
            String complaintTitle,
            Long   complaintId,
            String oldStatus,
            String newStatus,
            String resolutionComment) {

        String subject = "[" + appName + "] Your complaint status updated — #" + complaintId;

        String statusColor = switch (newStatus.toUpperCase()) {
            case "RESOLVED"    -> "#166534";
            case "IN_PROGRESS" -> "#1e40af";
            case "REJECTED"    -> "#991b1b";
            default            -> "#92400e";
        };

        String statusBg = switch (newStatus.toUpperCase()) {
            case "RESOLVED"    -> "#dcfce7";
            case "IN_PROGRESS" -> "#dbeafe";
            case "REJECTED"    -> "#fee2e2";
            default            -> "#fef3c7";
        };

        String formattedStatus = switch (newStatus.toUpperCase()) {
            case "RESOLVED"    -> "Resolved";
            case "IN_PROGRESS" -> "In Progress";
            case "REJECTED"    -> "Rejected";
            default            -> "Pending";
        };

        String resolutionSection = (resolutionComment != null && !resolutionComment.isBlank())
                ? """
              <div style="margin-top:16px;padding:14px 16px;background:#f0fdf4;border-left:4px solid #22c55e;border-radius:6px;">
                <p style="margin:0 0 6px;font-size:12px;font-weight:600;color:#166534;text-transform:uppercase;letter-spacing:0.05em;">Resolution Note</p>
                <p style="margin:0;font-size:14px;color:#14532d;">""" + resolutionComment + """
                </p>
              </div>
              """
                : "";

        String html = """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f8f9fc;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                
                <!-- Header -->
                <div style="background:#0f172a;padding:28px 32px;">
                  <h1 style="margin:0;font-size:22px;color:#ffffff;font-weight:700;">CitySparsh</h1>
                  <p style="margin:4px 0 0;font-size:13px;color:#475569;">Civic Complaint Management</p>
                </div>
                
                <!-- Body -->
                <div style="padding:32px;">
                  <p style="margin:0 0 8px;font-size:15px;color:#0f172a;">Hi <strong>%s</strong>,</p>
                  <p style="margin:0 0 24px;font-size:14px;color:#64748b;line-height:1.6;">
                    Your complaint status has been updated.
                  </p>
                  
                  <!-- Complaint card -->
                  <div style="border:1px solid #e5e9f2;border-radius:10px;padding:18px;margin-bottom:20px;">
                    <p style="margin:0 0 4px;font-size:11px;font-weight:600;color:#94a3b8;text-transform:uppercase;letter-spacing:0.08em;">Complaint #%d</p>
                    <p style="margin:0 0 16px;font-size:16px;font-weight:600;color:#0f172a;">%s</p>
                    
                    <!-- Status change -->
                    <div style="display:flex;align-items:center;gap:12px;">
                      <span style="padding:4px 12px;background:#f1f5f9;border-radius:20px;font-size:12px;font-weight:600;color:#64748b;">%s</span>
                      <span style="color:#94a3b8;font-size:16px;">→</span>
                      <span style="padding:4px 12px;background:%s;border-radius:20px;font-size:12px;font-weight:600;color:%s;">%s</span>
                    </div>
                    %s
                  </div>
                  
                  <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.6;">
                    Log in to <a href="https://citysparsh.vercel.app" style="color:#1a56db;">CitySparsh</a> to view full details.
                  </p>
                </div>
                
                <!-- Footer -->
                <div style="padding:20px 32px;background:#f8f9fc;border-top:1px solid #e5e9f2;">
                  <p style="margin:0;font-size:12px;color:#94a3b8;">This is an automated email from CitySparsh. Please do not reply.</p>
                </div>
                
              </div>
            </body>
            </html>
            """.formatted(
                citizenName, complaintId, complaintTitle,
                oldStatus, statusBg, statusColor, formattedStatus,
                resolutionSection
        );

        sendEmail(citizenEmail, subject, html);
    }

    // ── Officer: complaint assigned ───────────────
    public void sendAssignmentEmail(
            String officerEmail,
            String officerName,
            String complaintTitle,
            Long   complaintId,
            String priority,
            String category,
            String citizenName,
            String address) {

        String subject = "[" + appName + "] New complaint assigned to you — #" + complaintId;

        String priorityColor = switch (priority.toUpperCase()) {
            case "HIGH"   -> "#991b1b";
            case "MEDIUM" -> "#854d0e";
            default       -> "#166534";
        };

        String priorityBg = switch (priority.toUpperCase()) {
            case "HIGH"   -> "#fee2e2";
            case "MEDIUM" -> "#fefce8";
            default       -> "#dcfce7";
        };

        String addressSection = (address != null && !address.isBlank())
                ? """
              <div style="margin-top:12px;display:flex;align-items:flex-start;gap:8px;">
                <span style="font-size:16px;">📍</span>
                <p style="margin:0;font-size:13px;color:#475569;">%s</p>
              </div>
              """.formatted(address)
                : "";

        String html = """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f8f9fc;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                
                <!-- Header -->
                <div style="background:#0f172a;padding:28px 32px;">
                  <h1 style="margin:0;font-size:22px;color:#ffffff;font-weight:700;">CitySparsh</h1>
                  <p style="margin:4px 0 0;font-size:13px;color:#475569;">Officer Portal</p>
                </div>
                
                <!-- Body -->
                <div style="padding:32px;">
                  <p style="margin:0 0 8px;font-size:15px;color:#0f172a;">Hi <strong>%s</strong>,</p>
                  <p style="margin:0 0 24px;font-size:14px;color:#64748b;line-height:1.6;">
                    A new complaint has been assigned to you. Please review and take action.
                  </p>
                  
                  <!-- Complaint card -->
                  <div style="border:1px solid #e5e9f2;border-radius:10px;padding:18px;margin-bottom:20px;">
                    <p style="margin:0 0 4px;font-size:11px;font-weight:600;color:#94a3b8;text-transform:uppercase;letter-spacing:0.08em;">Complaint #%d</p>
                    <p style="margin:0 0 14px;font-size:16px;font-weight:600;color:#0f172a;">%s</p>
                    
                    <!-- Meta -->
                    <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:12px;">
                      <span style="padding:4px 12px;background:%s;border-radius:20px;font-size:12px;font-weight:600;color:%s;">%s</span>
                      <span style="padding:4px 12px;background:#f1f5f9;border-radius:20px;font-size:12px;font-weight:600;color:#475569;">%s</span>
                    </div>
                    
                    <div style="display:flex;align-items:center;gap:8px;">
                      <span style="font-size:14px;">👤</span>
                      <p style="margin:0;font-size:13px;color:#475569;">Citizen: <strong>%s</strong></p>
                    </div>
                    %s
                  </div>
                  
                  <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.6;">
                    Log in to <a href="https://citysparsh.vercel.app" style="color:#1a56db;">CitySparsh Officer Portal</a> to manage this case.
                  </p>
                </div>
                
                <!-- Footer -->
                <div style="padding:20px 32px;background:#f8f9fc;border-top:1px solid #e5e9f2;">
                  <p style="margin:0;font-size:12px;color:#94a3b8;">This is an automated email from CitySparsh. Please do not reply.</p>
                </div>
                
              </div>
            </body>
            </html>
            """.formatted(
                officerName, complaintId, complaintTitle,
                priorityBg, priorityColor, priority,
                category != null ? category : "General",
                citizenName != null ? citizenName : "—",
                addressSection
        );

        sendEmail(officerEmail, subject, html);
    }

    public void sendPasswordResetEmail(String to, String firstName, String token) {
        String resetLink = "https://citysparsh.vercel.app/reset-password?token=" + token;
        String subject   = "[CitySparsh] Reset your password";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="margin:0;padding:0;background:#f8f9fc;font-family:'Segoe UI',Arial,sans-serif;">
          <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
            <div style="background:#0f172a;padding:28px 32px;">
              <h1 style="margin:0;font-size:22px;color:#fff;font-weight:700;">CitySparsh</h1>
              <p style="margin:4px 0 0;font-size:13px;color:#475569;">Password Reset Request</p>
            </div>
            <div style="padding:32px;">
              <p style="margin:0 0 8px;font-size:15px;color:#0f172a;">Hi <strong>%s</strong>,</p>
              <p style="margin:0 0 24px;font-size:14px;color:#64748b;line-height:1.6;">
                We received a request to reset your CitySparsh password.
                Click the button below to set a new password.
              </p>
              <a href="%s"
                 style="display:inline-block;padding:13px 28px;background:#1a56db;color:#fff;
                        text-decoration:none;border-radius:9px;font-size:14px;font-weight:600;">
                Reset Password
              </a>
              <p style="margin:20px 0 0;font-size:12px;color:#94a3b8;line-height:1.6;">
                This link expires in <strong>1 hour</strong>.
                If you didn't request this, you can safely ignore this email.
              </p>
            </div>
            <div style="padding:20px 32px;background:#f8f9fc;border-top:1px solid #e5e9f2;">
              <p style="margin:0;font-size:12px;color:#94a3b8;">Do not reply to this email.</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(firstName, resetLink);

        sendEmail(to, subject, html);
    }
}
