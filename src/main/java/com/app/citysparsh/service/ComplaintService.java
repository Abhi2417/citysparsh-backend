package com.app.citysparsh.service;

import com.app.citysparsh.dto.ComplaintCreateDto;
import com.app.citysparsh.dto.ComplaintResponseDto;
import com.app.citysparsh.dto.ComplaintUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {
    ComplaintResponseDto createComplaint(String citizenEmail, ComplaintCreateDto dto);
    Page<ComplaintResponseDto> getComplaintsForCitizen(String citizenEmail, Pageable p);
    Page<ComplaintResponseDto> getComplaintsForOfficer(String officerEmail, Pageable p);
    ComplaintResponseDto getComplaint(Long id);
    ComplaintResponseDto updateComplaint(Long id, ComplaintUpdateDto dto, String performedByEmail);
    Page<ComplaintResponseDto> getAllComplaints(Pageable pageable);
     void deleteComplaint(Long id, String citizenEmail);

}
