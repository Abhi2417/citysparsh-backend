package com.app.citysparsh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @Enumerated(EnumType.STRING)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    private ComplaintCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id")
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id")
    private User assignedOfficer;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;

    @Column(columnDefinition = "TEXT")
    private String resolutionComment;

    // Stored file path or cloud URL (e.g. "uploads/complaints/uuid_filename.pdf")
    @Column(length = 1000)  // ← increase length for URL
    private String attachmentPath;

    // Original filename shown to users (e.g. "my-document.pdf")
    private String attachmentName;

    @Column
    private String address;        // human-readable address

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column
    private String detectedWardName;


}