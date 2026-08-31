package com.app.citysparsh.controller;

import com.app.citysparsh.model.Ward;
import com.app.citysparsh.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wards")
@CrossOrigin("*")
public class WardController {

    @Autowired
    private WardRepository wardRepo;

    // Public — citizen map needs this
    @GetMapping
    public List<Ward> getAll() {
        return wardRepo.findAll();
    }

    @GetMapping("/zone/{zone}")
    public List<Ward> getByZone(@PathVariable String zone) {
        return wardRepo.findByZone(zone);
    }

    // Detect ward from GPS — called when citizen drops pin
    @GetMapping("/detect")
    public ResponseEntity<?> detect(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        List<Ward> found = wardRepo.findByCoordinates(lat, lng);
        if (found.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of("wardName", "Unknown", "detected", false));
        }
        Ward w = found.get(0);
        return ResponseEntity.ok(Map.of(
                "detected",   true,
                "wardId",     w.getId(),
                "wardName",   w.getWardName(),
                "wardNumber", w.getWardNumber(),
                "zone",       w.getZone() != null ? w.getZone() : ""
        ));
    }

    // Admin only — CRUD for wards
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Ward create(@RequestBody Ward ward) {
        return wardRepo.save(ward);
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Ward update(@PathVariable Long id, @RequestBody Ward dto) {
        Ward w = wardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ward not found"));
        w.setWardName(dto.getWardName());
        w.setZone(dto.getZone());
        w.setMinLat(dto.getMinLat());
        w.setMaxLat(dto.getMaxLat());
        w.setMinLng(dto.getMinLng());
        w.setMaxLng(dto.getMaxLng());
        return wardRepo.save(w);
    }
}
