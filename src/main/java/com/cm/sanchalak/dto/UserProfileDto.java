package com.cm.sanchalak.dto;

import java.util.UUID;

/**
 * User profile DTO for mobile API
 */
public class UserProfileDto {
    
    private UUID userId;
    private String email;
    private String mobileNumber;
    private String name;
    private String role;
    
    // Student-specific fields (populated only for ROLE_STUDENT)
    private Long studentId;
    private String className;
    private Integer rollNo;
    
    // Parent-specific fields (populated only for ROLE_PARENT)
    private Long parentId;

    public UserProfileDto() {}

    public UserProfileDto(UUID userId, String email, String mobileNumber, String name, String role, Long studentId, String className, Integer rollNo, Long parentId) {
        this.userId = userId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.name = name;
        this.role = role;
        this.studentId = studentId;
        this.className = className;
        this.rollNo = rollNo;
        this.parentId = parentId;
    }

    public static UserProfileDtoBuilder builder() {
        return new UserProfileDtoBuilder();
    }

    // Getters
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public Long getStudentId() { return studentId; }
    public String getClassName() { return className; }
    public Integer getRollNo() { return rollNo; }
    public Long getParentId() { return parentId; }

    // Setters
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setClassName(String className) { this.className = className; }
    public void setRollNo(Integer rollNo) { this.rollNo = rollNo; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public static class UserProfileDtoBuilder {
        private UUID userId;
        private String email;
        private String mobileNumber;
        private String name;
        private String role;
        private Long studentId;
        private String className;
        private Integer rollNo;
        private Long parentId;

        UserProfileDtoBuilder() {}

        public UserProfileDtoBuilder userId(UUID userId) { this.userId = userId; return this; }
        public UserProfileDtoBuilder email(String email) { this.email = email; return this; }
        public UserProfileDtoBuilder mobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; return this; }
        public UserProfileDtoBuilder name(String name) { this.name = name; return this; }
        public UserProfileDtoBuilder role(String role) { this.role = role; return this; }
        public UserProfileDtoBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public UserProfileDtoBuilder className(String className) { this.className = className; return this; }
        public UserProfileDtoBuilder rollNo(Integer rollNo) { this.rollNo = rollNo; return this; }
        public UserProfileDtoBuilder parentId(Long parentId) { this.parentId = parentId; return this; }

        public UserProfileDto build() {
            return new UserProfileDto(userId, email, mobileNumber, name, role, studentId, className, rollNo, parentId);
        }
    }
}
