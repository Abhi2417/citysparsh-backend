package com.app.citysparsh.controller;


import com.app.citysparsh.dto.*;
import com.app.citysparsh.model.ComplaintStatus;
import com.app.citysparsh.model.User;
import com.app.citysparsh.repository.UserRepository;
import com.app.citysparsh.service.ComplaintService;
import com.app.citysparsh.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/complaints")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    /**
     * Admin: list all complaints (paginated)
     * GET /admin/complaints?page=0&size=10
     */
    @GetMapping
    public Page<ComplaintResponseDto> listAllComplaints(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // We reuse the citizen/officer service methods by delegating to repository-level query;
        // If you prefer, add a specific service method e.g., getAllComplaints(pageable)
        // Here we assume ComplaintService has getComplaintsForOfficer/citizen and getComplaint; if not, add getAllComplaints.
        // For clarity, let's assume complaintService.getAllComplaints(pageable) exists.
        try {
            return complaintService.getAllComplaints(pageable);
        } catch (UnsupportedOperationException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "getAllComplaints not implemented in service");
        }
    }

    /**
     * Admin: get complaint details
     * GET /admin/complaints/{id}
     */
    @GetMapping("/{id}")
    public ComplaintResponseDto getComplaint(@PathVariable Long id) {
        return complaintService.getComplaint(id);
    }

    /**
     * Admin: assign an officer to a complaint
     * PUT /admin/complaints/{id}/assign/{officerId}
     */
    @PutMapping("/{id}/assign/{officerId}")
    public ComplaintResponseDto assignOfficer(@PathVariable Long id,
                                              @PathVariable Long officerId,
                                              @RequestBody(required = false) AssignOfficerRequestDto body,
                                              Authentication auth) {
        String adminEmail = auth.getName();

        // Build an update DTO that sets assignedOfficerId
        ComplaintUpdateDto dto = new ComplaintUpdateDto();
        dto.setAssignedOfficerId(officerId);
        // Optionally set status to IN_PROGRESS when assigned
        // dto.setStatus(ComplaintStatus.IN_PROGRESS);
        if (body != null && body.getStatus() != null && !body.getStatus().isBlank()) {
            dto.setStatus(ComplaintStatus.valueOf(body.getStatus()));
        }

        return complaintService.updateComplaint(id, dto, adminEmail);
    }

    /**
     * Admin: update status/resolution using generic update endpoint
     * PUT /admin/complaints/{id}
     * Body: ComplaintUpdateDto
     */
    @PutMapping("/{id}")
    public ComplaintResponseDto updateComplaint(@PathVariable Long id,
                                                @RequestBody ComplaintUpdateDto dto,
                                                Authentication auth) {
        String adminEmail = auth.getName();
        return complaintService.updateComplaint(id, dto, adminEmail);
    }

    /**
     * Admin creates an officer account.
     * POST /admin/create-officer
     * Body: CreateOfficerDto { name, email, password }
     */

    @PostMapping("/create-officer")
    public User createOfficer(@Valid @RequestBody CreateOfficerDto dto) {
        System.out.println(">>> DTO department: " + dto.getDepartment());
        try {
            return userService.createOfficer(dto);
        } catch (IllegalArgumentException ex) {
            // email already exists or invalid input
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create officer");
        }
    }

    @GetMapping("/citizens")
    public List<UserResponseDto> getAllCitizens() {
        return userService.getAllCitizens().stream()
                .map(u -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setId(u.getId());
                    dto.setFirstName(u.getFirstName());
                    dto.setLastName(u.getLastName());
                    dto.setEmail(u.getEmail());
                    dto.setRole(u.getRole().name());
                    dto.setComplaintCount(userRepo.countComplaintsByCitizenId(u.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/officers")
    public List<UserResponseDto> getAllOfficers() {
        return userService.getAllOfficers().stream()
                .map(u -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setId(u.getId());
                    dto.setFirstName(u.getFirstName());
                    dto.setLastName(u.getLastName());
                    dto.setEmail(u.getEmail());
                    dto.setRole(u.getRole().name());
                    dto.setDepartment(u.getDepartment() != null ? u.getDepartment().name() : null);
                    dto.setAssignedCount(userRepo.countComplaintsByOfficerId(u.getId()));
                    dto.setWardId(u.getWard() != null ? u.getWard().getId() : null);
                    dto.setWardName(u.getWard() != null ? u.getWard().getWardName() : null);
                    dto.setZone(u.getZone());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
