package com.app.citysparsh.dto;

import lombok.Data;

@Data
public class AssignOfficerRequestDto {
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
