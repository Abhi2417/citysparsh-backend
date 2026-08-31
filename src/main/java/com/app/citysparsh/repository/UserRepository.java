package com.app.citysparsh.repository;

import com.app.citysparsh.model.OfficerDepartment;
import com.app.citysparsh.model.Role;
import com.app.citysparsh.model.User;
import com.app.citysparsh.model.Ward;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.citizen.id = :userId")
    long countComplaintsByCitizenId(@Param("userId") Long userId);
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.assignedOfficer.id = :userId")
    long countComplaintsByOfficerId(@Param("userId") Long userId);

    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);


    long countByRole(Role role);

    Optional<User> findByResetToken(String resetToken);

    // Ward + department — primary search
    List<User> findByRoleAndDepartmentAndWard(
            Role role, OfficerDepartment dept, Ward ward);

    // Zone + department — fallback
    List<User> findByRoleAndDepartmentAndZone(
            Role role, OfficerDepartment dept, String zone);

    // Department only — last resort
    List<User> findByRoleAndDepartment(Role role, OfficerDepartment department);

    // Active workload — excludes resolved/rejected
    @Query("SELECT COUNT(c) FROM Complaint c " +
            "WHERE c.assignedOfficer.id = :officerId " +
            "AND c.status NOT IN ('RESOLVED','REJECTED')")
    long countActiveByOfficerId(@Param("officerId") Long officerId);

}
