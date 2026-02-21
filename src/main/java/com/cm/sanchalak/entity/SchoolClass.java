package com.cm.sanchalak.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Adding schoolId for multi-tenancy
    @Column(name = "school_id")
    private UUID schoolId;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    @JsonProperty("className")
    private String name;

    @JsonProperty("classID")
    @Column(name = "class_id", length = 50, unique = true)
    private String classID; // Business ID

    private Integer grade;

    @Column(length = 10)
    private String section;

    @Column(length = 20)
    private String room;
}
