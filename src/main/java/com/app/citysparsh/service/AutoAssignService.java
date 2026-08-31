package com.app.citysparsh.service;

import com.app.citysparsh.model.*;
import com.app.citysparsh.repository.UserRepository;
import com.app.citysparsh.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AutoAssignService {

    @Autowired private UserRepository  userRepo;
    @Autowired private WardRepository  wardRepo;
    @Autowired private EmailService    emailService;

    /**
     * Auto-assign logic:
     * Step 1 — ward + department   (most specific)
     * Step 2 — zone + department   (zone fallback)
     * Step 3 — department only     (city-wide fallback)
     * Step 4 — GENERAL officers    (last resort)
     * Returns empty → admin queue
     */
    public Optional<User> findBestOfficer(
            OfficerDepartment department,
            Ward ward) {

        // Step 1: Ward + Department
        if (ward != null) {
            List<User> wardOfficers = userRepo
                    .findByRoleAndDepartmentAndWard(
                            Role.OFFICER, department, ward);
            Optional<User> best = pickLowestWorkload(wardOfficers);
            if (best.isPresent()) {
                System.out.println(">>> [AutoAssign] Ward match: "
                        + ward.getWardName()
                        + " | Officer: " + best.get().getEmail());
                return best;
            }

            // Step 2: Zone + Department
            if (ward.getZone() != null) {
                List<User> zoneOfficers = userRepo
                        .findByRoleAndDepartmentAndZone(
                                Role.OFFICER, department, ward.getZone());
                best = pickLowestWorkload(zoneOfficers);
                if (best.isPresent()) {
                    System.out.println(">>> [AutoAssign] Zone fallback: "
                            + ward.getZone()
                            + " | Officer: " + best.get().getEmail());
                    return best;
                }
            }
        }

        // Step 3: Department only (any ward, any zone)
        List<User> deptOfficers = userRepo
                .findByRoleAndDepartment(Role.OFFICER, department);
        Optional<User> best = pickLowestWorkload(deptOfficers);
        if (best.isPresent()) {
            System.out.println(">>> [AutoAssign] Dept-only fallback"
                    + " | Officer: " + best.get().getEmail());
            return best;
        }

        // Step 4: GENERAL department officers
        List<User> generalOfficers = userRepo
                .findByRoleAndDepartment(
                        Role.OFFICER, OfficerDepartment.GENERAL);
        best = pickLowestWorkload(generalOfficers);
        if (best.isPresent()) {
            System.out.println(">>> [AutoAssign] GENERAL fallback"
                    + " | Officer: " + best.get().getEmail());
        } else {
            System.out.println(">>> [AutoAssign] No officer found"
                    + " — complaint goes to admin queue");
        }
        return best;
    }

    private Optional<User> pickLowestWorkload(List<User> officers) {
        if (officers == null || officers.isEmpty())
            return Optional.empty();
        return officers.stream()
                .min(Comparator.comparingLong(o ->
                        userRepo.countActiveByOfficerId(o.getId())));
    }

    public void notifyOfficer(User officer, Complaint complaint) {
        String officerName = officer.getFirstName()
                + " " + officer.getLastName();
        String citizenName = null;
        if (complaint.getCitizen() != null) {
            citizenName = complaint.getCitizen().getFirstName()
                    + " " + complaint.getCitizen().getLastName();
        }
        emailService.sendAssignmentEmail(
                officer.getEmail(),
                officerName,
                complaint.getTitle(),
                complaint.getId(),
                complaint.getPriority() != null
                        ? complaint.getPriority().name() : "LOW",
                complaint.getCategory() != null
                        ? complaint.getCategory().name() : null,
                citizenName,
                complaint.getAddress()
        );
    }
}