package com.cm.sanchalak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * User profile DTO for mobile API
 */
public class UserProfileDto {

    @JsonProperty("userId")
    private UUID userId;
    private String email;
    private String mobileNumber;
    private String name;
    private String role;

    // Student-specific fields (populated only for ROLE_STUDENT)
    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentID")
    private String studentID;

    private String className;
    private Integer rollNo;
    private Integer rollNumber;

    // Parent-specific fields (populated only for ROLE_PARENT)
    @JsonProperty("parentId")
    private Long parentId;
    @JsonProperty("parentID")
    private String parentID;

    public UserProfileDto() {
    }

    public UserProfileDto(UUID userId, String email, String mobileNumber, String name, String role,
            Long studentId, String studentID, String className, Integer rollNo, Integer rollNumber,
            Long parentId, String parentID) {
        this.userId = userId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.name = name;
        this.role = role;
        this.studentId = studentId;
        this.studentID = studentID;
        this.className = className;
        this.rollNo = rollNo;
        this.rollNumber = rollNumber;
        this.parentId = parentId;
        this.parentID = parentID;
    }

    public static UserProfileDtoBuilder builder() {
        return new UserProfileDtoBuilder();
    }

    // Getters
    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getClassName() {
        return className;
    }

    public Integer getRollNo() {
        return rollNo;
    }

    public Integer getRollNumber() {
        return rollNumber;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentID() {
        return parentID;
    }

    // Setters
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setRollNo(Integer rollNo) {
        this.rollNo = rollNo;
    }

    public void setRollNumber(Integer rollNumber) {
        this.rollNumber = rollNumber;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public static class UserProfileDtoBuilder {
        private UUID userId;
        private String email;
        private String mobileNumber;
        private String name;
        private String role;
        private Long studentId;
        private String studentID;
        private String className;
        private Integer rollNo;
        private Integer rollNumber;
        private Long parentId;
        private String parentID;

        UserProfileDtoBuilder() {
        }

        public UserProfileDtoBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public UserProfileDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserProfileDtoBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public UserProfileDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserProfileDtoBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserProfileDtoBuilder studentId(Long studentId) {
            this.studentId = studentId;
            return this;
        }

        public UserProfileDtoBuilder studentID(String studentID) {
            this.studentID = studentID;
            return this;
        }

        public UserProfileDtoBuilder className(String className) {
            this.className = className;
            return this;
        }

        public UserProfileDtoBuilder rollNo(Integer rollNo) {
            this.rollNo = rollNo;
            return this;
        }

        public UserProfileDtoBuilder rollNumber(Integer rollNumber) {
            this.rollNumber = rollNumber;
            return this;
        }

        public UserProfileDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public UserProfileDtoBuilder parentID(String parentID) {
            this.parentID = parentID;
            return this;
        }

        public UserProfileDto build() {
            return new UserProfileDto(userId, email, mobileNumber, name, role, studentId, studentID, className, rollNo,
                    rollNumber, parentId, parentID);
        }
    }
}
