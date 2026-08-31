package com.app.citysparsh.controller;

import com.app.citysparsh.dto.ComplaintResponseDto;
import com.app.citysparsh.dto.ComplaintUpdateDto;
import com.app.citysparsh.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/officer/complaints")
public class OfficerComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Officer: get complaints assigned to this officer (paginated)
     * GET /officer/complaints?page=0&size=10
     */
    @GetMapping
    public Page<ComplaintResponseDto> getAssignedComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String officerEmail = auth.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return complaintService.getComplaintsForOfficer(officerEmail, pageable);
    }

    /**
     * Officer: get complaint details (must be assigned to this officer or accessible by officer)
     * GET /officer/complaints/{id}
     */
    @GetMapping("/{id}")
    public ComplaintResponseDto getComplaint(@PathVariable Long id, Authentication auth) {
        String officerEmail = auth.getName();
        ComplaintResponseDto dto = complaintService.getComplaint(id);

        // Security check: officer can see complaint only if assigned to them (or you may allow any officer)
        if (dto.getAssignedOfficerEmail() == null || !officerEmail.equals(dto.getAssignedOfficerEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: This complaint is not assigned to you.");
        }

        return dto;
    }

    /**
     * Officer: update complaint status (e.g., IN_PROGRESS, RESOLVED) or add resolution comments.
     * PUT /officer/complaints/{id}
     * Body: ComplaintUpdateDto (status, resolutionComment, optional assignedOfficerId ignored)
     */
    @PutMapping("/{id}")
    public ComplaintResponseDto updateComplaint(@PathVariable Long id,
                                                @RequestBody ComplaintUpdateDto dto,
                                                Authentication auth) {
        String officerEmail = auth.getName();

        // Optional: ensure complaint is assigned to this officer before allowing updates
        ComplaintResponseDto existing = complaintService.getComplaint(id);
        if (existing.getAssignedOfficerEmail() == null || !officerEmail.equals(existing.getAssignedOfficerEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: This complaint is not assigned to you.");
        }

        // only allow OFFICER to update status/resolution - performedBy is officerEmail
        return complaintService.updateComplaint(id, dto, officerEmail);
    }

    }
