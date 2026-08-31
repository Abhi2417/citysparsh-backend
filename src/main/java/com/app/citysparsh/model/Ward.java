package com.app.citysparsh.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "wards")
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer wardNumber;
    private String  wardName;
    private String  zone;

    // Bounding box for GPS detection
    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;

}