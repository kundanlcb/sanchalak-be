package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public class TeacherResponse {
    private Long id;
    @JsonProperty("teacherID")
    private String teacherID;
    private String name;
    private String email;
    private String phone;
    private String mobileNumber;
    private String qualification;
    private String profileImage;
    private Set<Subject> specializations;
    private String createdAt;
    private String updatedAt;

    public TeacherResponse() {
    }

    public TeacherResponse(Teacher teacher) {
        this.id = teacher.getId();
        this.teacherID = "TCH-" + teacher.getId();
        this.name = teacher.getName();
        this.email = teacher.getEmail();
        this.phone = teacher.getMobileNumber();
        this.mobileNumber = teacher.getMobileNumber();
        this.qualification = teacher.getQualification();
        this.profileImage = teacher.getProfileImage();
        this.specializations = teacher.getSpecializations();
        this.createdAt = teacher.getCreatedAt() != null ? teacher.getCreatedAt().toString() : null;
        this.updatedAt = teacher.getUpdatedAt() != null ? teacher.getUpdatedAt().toString() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Subject> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(Set<Subject> specializations) {
        this.specializations = specializations;
    }
}
