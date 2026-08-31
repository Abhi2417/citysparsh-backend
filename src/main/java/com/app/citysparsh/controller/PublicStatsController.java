package com.app.citysparsh.controller;

import com.app.citysparsh.model.ComplaintStatus;
import com.app.citysparsh.model.Role;
import com.app.citysparsh.repository.ComplaintRepository;
import com.app.citysparsh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicStatsController {

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long totalResolved   = complaintRepo.countByStatus(ComplaintStatus.RESOLVED);
        long activeOfficers  = userRepo.countByRole(Role.OFFICER);
        long totalCitizens   = userRepo.countByRole(Role.CITIZEN);
        long totalComplaints = complaintRepo.count();

        return Map.of(
                "totalResolved",   totalResolved,
                "activeOfficers",  activeOfficers,
                "totalCitizens",   totalCitizens,
                "totalComplaints", totalComplaints
        );
    }
}
