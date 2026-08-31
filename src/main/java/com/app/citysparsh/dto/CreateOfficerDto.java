package com.app.citysparsh.dto;

import com.app.citysparsh.model.OfficerDepartment;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOfficerDto {


    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private OfficerDepartment department;

    private Long   wardId;  // specific ward
    private String zone;    // or whole zone coverage
}
