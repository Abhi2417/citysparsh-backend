package com.app.citysparsh.controller;

import com.app.citysparsh.mapper.CategoryDepartmentMapper;
import com.app.citysparsh.model.ComplaintCategory;
import com.app.citysparsh.model.OfficerDepartment;
import com.app.citysparsh.model.Role;
import com.app.citysparsh.model.User;
import com.app.citysparsh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meta")
@CrossOrigin("*")
public class MetaController {

    @Autowired
    private UserRepository userRepo;

    // GET /meta/categories — returns all complaint categories
    @GetMapping("/categories")
    public List<String> getCategories() {
        return Arrays.stream(ComplaintCategory.values())
                .map(Enum::name)
                .toList();
    }

    // GET /meta/departments — returns all officer departments
    @GetMapping("/departments")
    public List<String> getDepartments() {
        return Arrays.stream(OfficerDepartment.values())
                .map(Enum::name)
                .toList();
    }

    // GET /meta/category-department-map
    // Returns which department handles which category
    // Useful for frontend to show only valid officers
    @GetMapping("/category-department-map")
    public Map<String, String> getCategoryDepartmentMap() {
        Map<String, String> result = new java.util.HashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            result.put(cat.name(), CategoryDepartmentMapper.getDepartmentFor(cat).name());
        }
        return result;
    }

    // GET /meta/officers-by-category/{category}
    // Admin uses this to get valid officers for a complaint
    @GetMapping("/officers-by-category/{category}")
    public List<Map<String, Object>> getOfficersByCategory(
            @PathVariable ComplaintCategory category) {

        OfficerDepartment required = CategoryDepartmentMapper.getDepartmentFor(category);

        // Fetch officers of required department
        List<User> deptOfficers = userRepo.findByRoleAndDepartment(Role.OFFICER, required);

        // Fetch GENERAL department officers (they can handle anything)
        // Only fetch GENERAL separately if required is not already GENERAL
        List<User> generalOfficers = required == OfficerDepartment.GENERAL
                ? List.of()   // already fetched above, avoid duplicates
                : userRepo.findByRoleAndDepartment(Role.OFFICER, OfficerDepartment.GENERAL);

        // Merge both lists
        List<User> allEligible = new java.util.ArrayList<>();
        allEligible.addAll(deptOfficers);
        allEligible.addAll(generalOfficers);

        // Map to response
        return allEligible.stream()
                .map(u -> Map.of(
                        "id",         (Object) u.getId(),
                        "name",      u.getFirstName() + " " + u.getLastName(),
                        "email",      u.getEmail(),
                        "department", u.getDepartment() != null
                                ? u.getDepartment().name()
                                : "GENERAL"
                ))
                .toList();
    }
}
