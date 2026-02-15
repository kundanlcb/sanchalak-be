package com.cm.sanchalak.platform.auth;

import com.cm.sanchalak.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "platform_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class PlatformUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Size(max = 40)
    @Column(length = 40, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Email
    @Column(length = 40, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String password;

    @ElementCollection(targetClass = PlatformRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "platform_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<PlatformRole> roles = new HashSet<>();

    public PlatformUser() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<PlatformRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<PlatformRole> roles) {
        this.roles = roles;
    }
}
