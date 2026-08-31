package com.app.citysparsh.dto;

import com.app.citysparsh.model.ComplaintCategory;
import com.app.citysparsh.model.ComplaintPriority;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ComplaintCreateDto {
    private String title;
    private String description;
    private ComplaintPriority priority;
    private ComplaintCategory category;
    private MultipartFile attachment;
    private String address;
    private Double latitude;
    private Double longitude;
}
