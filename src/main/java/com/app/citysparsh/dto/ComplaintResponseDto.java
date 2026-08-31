package com.app.citysparsh.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ComplaintResponseDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private String citizenName;
    private String citizenEmail;
    private String assignedOfficerName;
    private String assignedOfficerEmail;
    private String resolutionComment;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private String attachmentName;
    private String attachmentPath;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long  wardId;
    private String wardName;
    private String zone;
    private String detectedWardName;
}
