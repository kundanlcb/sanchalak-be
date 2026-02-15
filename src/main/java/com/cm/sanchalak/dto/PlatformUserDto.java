package com.cm.sanchalak.dto;

import com.cm.sanchalak.platform.auth.PlatformRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PlatformUserDto {
    private String id;
    private String email;
    private String name;
    private Set<PlatformRole> roles;
}
