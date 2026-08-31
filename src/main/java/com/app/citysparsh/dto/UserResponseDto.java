package com.app.citysparsh.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String department;   // ← for officers
    private long complaintCount;  // ← for citizens
    private long assignedCount;   // for officers
    private Long  wardId;
    private String wardName;
    private String zone;
}
