package com.app.citysparsh.dto;

import com.app.citysparsh.model.ComplaintCategory;
import com.app.citysparsh.model.ComplaintPriority;
import com.app.citysparsh.model.ComplaintStatus;
import lombok.Data;

@Data
public class ComplaintUpdateDto {
    private String title;           // for citizen
    private String description;     // for citizen
    private String address;         // for citizen
    private Double latitude;        // for citizen
    private Double longitude;       // for citizen
    private ComplaintStatus status;
    private Long assignedOfficerId;
    private String resolutionComment;
    private ComplaintPriority priority;
    private ComplaintCategory category;
}
