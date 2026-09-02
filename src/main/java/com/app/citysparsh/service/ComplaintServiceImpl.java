package com.app.citysparsh.service;

import com.app.citysparsh.dto.ComplaintCreateDto;
import com.app.citysparsh.dto.ComplaintResponseDto;
import com.app.citysparsh.dto.ComplaintUpdateDto;
import com.app.citysparsh.mapper.CategoryDepartmentMapper;
import com.app.citysparsh.model.*;
import com.app.citysparsh.repository.ComplaintRepository;
import com.app.citysparsh.repository.UserRepository;
import com.app.citysparsh.repository.WardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WardRepository wardRepo;

    @Autowired
    private AutoAssignService   autoAssignService;

    @Autowired
    private FileStorageService fileStorageService;

//    @Value("${app.upload.dir:uploads/complaints}")
//    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean isAdminUser(String email) {
        return userRepo.findByEmail(email)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    private ComplaintResponseDto toDto(Complaint c) {
        ComplaintResponseDto r = new ComplaintResponseDto();
        r.setId(c.getId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setStatus(c.getStatus() == null ? null : c.getStatus().name());
        r.setPriority(c.getPriority() == null ? null : c.getPriority().name());
        r.setCategory(c.getCategory() == null ? null : c.getCategory().name());
        if (c.getCitizen() != null) {
            r.setCitizenName(c.getCitizen().getFirstName() + " " + c.getCitizen().getLastName());
            r.setCitizenEmail(c.getCitizen().getEmail());
        }
        r.setCitizenEmail(c.getCitizen() != null ? c.getCitizen().getEmail() : null);

        if (c.getAssignedOfficer() != null) {
            r.setAssignedOfficerName(c.getAssignedOfficer().getFirstName() + " " + c.getAssignedOfficer().getLastName());
            r.setAssignedOfficerEmail(c.getAssignedOfficer().getEmail());
        }
        if (c.getAddress() != null)
            r.setAddress(c.getAddress());
        if (c.getLatitude() != null)
            r.setLatitude(c.getLatitude());
        if (c.getLongitude() != null)
            r.setLongitude(c.getLongitude());
        r.setAssignedOfficerEmail(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getEmail() : null);
        r.setResolutionComment(c.getResolutionComment());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        r.setResolvedAt(c.getResolvedAt());
        r.setAttachmentName(c.getAttachmentName());
        r.setAttachmentPath(c.getAttachmentPath());
        if(c.getWard() != null) {
            r.setWardId(c.getWard().getId());
            r.setWardName(c.getWard().getWardName());
            r.setZone(c.getWard().getZone());
        }
        r.setDetectedWardName(c.getDetectedWardName());

        return r;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File size exceeds the 10 MB limit.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported file type: " + contentType +
                            ". Allowed: PDF, DOC, DOCX, JPEG, PNG, GIF.");
        }
    }

//    private String saveFile(MultipartFile file) throws IOException {
//        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        // Sanitize original filename - strip any path components, keep only the name
//        String originalName = StringUtils.cleanPath(
//                Objects.requireNonNullElse(file.getOriginalFilename(), "file"));
//        originalName = Paths.get(originalName).getFileName().toString(); // strips any dir parts
//        originalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_"); // strip odd chars
//
//        String uniqueFilename = UUID.randomUUID() + "_" + originalName;
//        Path destination = uploadPath.resolve(uniqueFilename).normalize();
//
//        // Defense in depth: ensure resolved path is still inside uploadPath
//        if (!destination.startsWith(uploadPath)) {
//            throw new IOException("Invalid file path resolution.");
//        }
//
//        long bytesCopied = Files.copy(file.getInputStream(), destination,
//                StandardCopyOption.REPLACE_EXISTING);
//
//        // Verify the file was actually persisted and complete
//        if (!Files.exists(destination)) {
//            throw new IOException("File save failed: destination does not exist after copy.");
//        }
//        long savedSize = Files.size(destination);
//        if (savedSize != file.getSize() || bytesCopied != file.getSize()) {
//            Files.deleteIfExists(destination); // cleanup partial/corrupt file
//            throw new IOException("File save failed: size mismatch (expected "
//                    + file.getSize() + ", got " + savedSize + ").");
//        }
//
//        return uploadDir + "/" + uniqueFilename;
//    }

    // ── Service Methods ────────────────────────────────────────────────────────

    @Override
    public ComplaintResponseDto createComplaint(String citizenEmail, ComplaintCreateDto dto) {
        User citizen = userRepo.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("Citizen not found"));

        Complaint c = Complaint.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority() == null ? ComplaintPriority.MEDIUM : dto.getPriority())
                .status(ComplaintStatus.PENDING)
                .category(dto.getCategory())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .citizen(citizen)
                .createdAt(Instant.now())
                .build();



        // Handle optional file attachment
        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            Map<String, String> uploaded = null;
            try {
                uploaded = fileStorageService.uploadFile(dto.getAttachment());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            c.setAttachmentName(uploaded.get("name"));
            c.setAttachmentPath(uploaded.get("url"));  // ← Cloudinary URL
        }

        Ward detectedWard = null;
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            List<Ward> candidates = wardRepo.findByCoordinates(
                    dto.getLatitude(), dto.getLongitude());
            if (!candidates.isEmpty()) {
                detectedWard = candidates.get(0);
                System.out.println(">>> Ward detected: "
                        + detectedWard.getWardName()
                        + " (zone: " + detectedWard.getZone() + ")");
            }
        }
        c.setWard(detectedWard);
        c.setDetectedWardName(
                detectedWard != null ? detectedWard.getWardName() : null);

        Complaint saved = complaintRepo.save(c);

        // ── Auto-assign officer ────────────────────────────
        if (saved.getCategory() != null) {
            OfficerDepartment dept =
                    CategoryDepartmentMapper.getDepartmentFor(saved.getCategory());

            Optional<User> bestOfficer =
                    autoAssignService.findBestOfficer(dept, detectedWard);

            if (bestOfficer.isPresent()) {
                saved.setAssignedOfficer(bestOfficer.get());
                saved.setStatus(ComplaintStatus.IN_PROGRESS);
                saved = complaintRepo.save(saved);
                autoAssignService.notifyOfficer(bestOfficer.get(), saved);
            }
            // else → stays PENDING, appears in admin unassigned queue
        }

        complaintRepo.save(c);
        return toDto(c);
    }

    @Override
    public Page<ComplaintResponseDto> getComplaintsForCitizen(String citizenEmail, Pageable p) {
        return complaintRepo.findByCitizenEmail(citizenEmail, p).map(this::toDto);
    }

    @Override
    public Page<ComplaintResponseDto> getComplaintsForOfficer(String officerEmail, Pageable p) {
        return complaintRepo.findByAssignedOfficerEmail(officerEmail, p).map(this::toDto);
    }

    @Override
    public ComplaintResponseDto getComplaint(Long id) {
        Complaint c = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return toDto(c);
    }

    // In updateComplaint() — add department validation when assigning officer
    public ComplaintResponseDto updateComplaint(Long id, ComplaintUpdateDto dto, String performedByEmail) {

        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        String oldStatus = complaint.getStatus() != null
                ? complaint.getStatus().name() : "PENDING";

        // for citizen complaint update
        if (dto.getTitle() != null)
            complaint.setTitle(dto.getTitle());

        if (dto.getDescription() != null)
            complaint.setDescription(dto.getDescription());

        // ── DEPARTMENT VALIDATION when assigning officer ──────────
        if (dto.getAssignedOfficerId() != null) {
            User officer = userRepo.findById(dto.getAssignedOfficerId())
                    .orElseThrow(() -> new RuntimeException("Officer not found"));

            // Validate department matches category
            if (complaint.getCategory() != null && officer.getDepartment() != null) {
                boolean canHandle = CategoryDepartmentMapper.canHandle(
                        officer.getDepartment(),
                        complaint.getCategory()
                );
                if (!canHandle) {
                    throw new RuntimeException(
                            "Officer from " + officer.getDepartment() +
                                    " cannot handle " + complaint.getCategory() +
                                    " complaints. Required: " +
                                    CategoryDepartmentMapper.getDepartmentFor(complaint.getCategory())
                    );
                }
            }
            complaint.setAssignedOfficer(officer);

            // ── Send assignment email to officer ──────────  ← ADD FROM HERE
            String officerName  = officer.getFirstName() + " " + officer.getLastName();
            String citizenName  = complaint.getCitizen() != null
                    ? complaint.getCitizen().getFirstName() + " " + complaint.getCitizen().getLastName()
                    : null;

            emailService.sendAssignmentEmail(
                    officer.getEmail(),
                    officerName,
                    complaint.getTitle(),
                    complaint.getId(),
                    complaint.getPriority() != null ? complaint.getPriority().name() : "LOW",
                    complaint.getCategory() != null ? complaint.getCategory().name() : null,
                    citizenName,
                    complaint.getAddress()
            );
        }

        // Update other fields
        if (dto.getStatus() != null)
            complaint.setStatus(dto.getStatus());

        if (dto.getResolutionComment() != null)
            complaint.setResolutionComment(dto.getResolutionComment());

        if (dto.getPriority() != null)
            complaint.setPriority(dto.getPriority());

        if (dto.getCategory() != null)
            complaint.setCategory(dto.getCategory());
        if (dto.getAddress() != null)
            complaint.setAddress(dto.getAddress());
        if (dto.getLatitude() != null)
            complaint.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null)
            complaint.setLongitude(dto.getLongitude());

        // Set resolvedAt if resolved
        if (dto.getStatus() == ComplaintStatus.RESOLVED)
            complaint.setResolvedAt(Instant.now());

        complaint.setUpdatedAt(Instant.now());

        Complaint saved = complaintRepo.save(complaint);

         // ── Send email if status changed ──────────────
        if (dto.getStatus() != null && !dto.getStatus().name().equals(oldStatus)) {
            String citizenEmail = saved.getCitizen().getEmail();
            String citizenName  = saved.getCitizen().getFirstName() + " "
                    + saved.getCitizen().getLastName();

            emailService.sendStatusUpdateEmail(
                    citizenEmail,
                    citizenName,
                    saved.getTitle(),
                    saved.getId(),
                    oldStatus,
                    saved.getStatus().name(),
                    saved.getResolutionComment()
            );
        }

        return toDto(saved);
    }

    @Override
    public Page<ComplaintResponseDto> getAllComplaints(Pageable pageable) {
        return complaintRepo.findAll(pageable).map(this::toDto);
    }

    public void deleteComplaint(Long id, String email) {

        Complaint complaint = complaintRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Complaint not found"));

        if (!complaint.getCitizen().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied: This complaint is not yours.");
        }

        // Clean up the attached file from disk before deleting the record
        if (complaint.getAttachmentPath() != null) {
            try {
                Path filePath = Paths.get(complaint.getAttachmentPath()).toAbsolutePath().normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log but don't block deletion of the DB record over a file cleanup failure
                log.warn("Failed to delete attachment file for complaint {}: {}", id, e.getMessage());
            }
        }

        complaintRepo.delete(complaint);
    }
}