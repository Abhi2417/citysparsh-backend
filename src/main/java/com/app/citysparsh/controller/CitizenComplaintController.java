package com.app.citysparsh.controller;

import com.app.citysparsh.dto.ComplaintCreateDto;
import com.app.citysparsh.dto.ComplaintResponseDto;
import com.app.citysparsh.dto.ComplaintUpdateDto;
import com.app.citysparsh.model.ComplaintCategory;
import com.app.citysparsh.model.ComplaintPriority;
import com.app.citysparsh.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/citizen/complaints")
@CrossOrigin("*")
public class CitizenComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Citizen creates a new complaint
     * POST /citizen/complaints
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponseDto> createComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("priority") ComplaintPriority priority,
            @RequestParam("category") ComplaintCategory category,
            @RequestParam("address")  String address,
            @RequestParam(value = "latitude",   required = false) Double latitude,
            @RequestParam(value = "longitude",  required = false) Double longitude,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            Authentication auth) {

        ComplaintCreateDto dto = new ComplaintCreateDto();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setPriority(priority);
        dto.setCategory(category);
        dto.setAddress(address);
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setAttachment(attachment);

        String citizenEmail = auth.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.createComplaint(citizenEmail, dto));
    }
    /**
     * Citizen views all their complaints (paginated)
     * GET /citizen/complaints?page=0&size=10
     */
    @GetMapping
    public Page<ComplaintResponseDto> getMyComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String email = auth.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return complaintService.getComplaintsForCitizen(email, pageable);
    }

    /**
     * Citizen views a specific complaint (only their own)
     * GET /citizen/complaints/{id}
     */
    @GetMapping("/{id}")
    public ComplaintResponseDto getComplaintById(@PathVariable Long id,
                                                 Authentication auth) {
        String email = auth.getName();

        ComplaintResponseDto response = complaintService.getComplaint(id);

        // Security check: citizen can only access own complaint
        if (!email.equals(response.getCitizenEmail())) {
            throw new RuntimeException("Access denied: This complaint is not yours.");
        }

        return response;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id, Authentication auth) {
        complaintService.deleteComplaint(id, auth.getName());
        return ResponseEntity.noContent().build(); // 204, empty body
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComplaintResponseDto> updateComplaint(
            @PathVariable Long id,
            @RequestBody ComplaintUpdateDto dto,
            Authentication auth) {
        return ResponseEntity.ok(
                complaintService.updateComplaint(id, dto, auth.getName())
        );
    }

}
