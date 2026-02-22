package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "api_endpoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String method; // e.g., GET, POST, DELETE

    @Column(nullable = false)
    private String urlPattern; // e.g., /api/academic/terms/**

    @Column(name = "module_name")
    private String moduleName; // e.g., Academics, Attendance

    @Column(name = "description")
    private String description; // e.g., Delete Exam Term

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "endpoint_role_mapping", joinColumns = @JoinColumn(name = "endpoint_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
