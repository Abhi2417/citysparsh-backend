package com.app.citysparsh.repository;

import com.app.citysparsh.model.Complaint;
import com.app.citysparsh.model.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {

    Page<Complaint> findByCitizenEmail(String email, Pageable pageable);
    Page<Complaint> findByAssignedOfficerEmail(String email, Pageable pageable);
    long countByStatus(ComplaintStatus status);
    List<Complaint> findByStatus(ComplaintStatus status);
}
