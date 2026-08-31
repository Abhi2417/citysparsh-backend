package com.app.citysparsh.service;

import com.app.citysparsh.dto.CreateOfficerDto;
import com.app.citysparsh.model.Role;
import com.app.citysparsh.model.User;
import com.app.citysparsh.model.Ward;
import com.app.citysparsh.repository.UserRepository;
import com.app.citysparsh.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private WardRepository wardRepo;

    @Override
    public User createOfficer(CreateOfficerDto dto) {
        // Check duplicate email
        Optional<User> existing = repo.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        User officer = new User();
        officer.setFirstName(dto.getFirstName());
        officer.setLastName(dto.getLastName());
        officer.setEmail(dto.getEmail());
        officer.setPassword(encoder.encode(dto.getPassword()));
        // if Role is enum type (previous examples used enum Role), set it accordingly:
        // officer.setRole(Role.OFFICER);
        // If your User.role is a String, use "OFFICER"
        // adjust below depending on your User.role type:
        try {
            // If your User.role type is enum Role
            officer.setRole(Role.OFFICER);
        } catch (ClassCastException e) {
            // fallback if role is String
            // officer.setRole("OFFICER");
        }
        officer.setDepartment(dto.getDepartment());  // ← set department
        // ── Ward assignment ───────────────────────────
        if (dto.getWardId() != null) {
            Ward ward = wardRepo.findById(dto.getWardId())
                    .orElseThrow(() -> new RuntimeException("Ward not found: " + dto.getWardId()));
            officer.setWard(ward);
            officer.setZone(ward.getZone()); // auto-set zone from ward
        } else if (dto.getZone() != null && !dto.getZone().isBlank()) {
            officer.setZone(dto.getZone()); // zone-wide coverage
        }

        return repo.save(officer);
    }
    @Override
    public List<User> getAllCitizens() {
        return repo.findByRole(Role.CITIZEN);
    }

    @Override
    public List<User> getAllOfficers() {
        return repo.findByRole(Role.OFFICER);
    }
}
