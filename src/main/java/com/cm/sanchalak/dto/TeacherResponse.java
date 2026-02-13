package com.cm.sanchalak.dto;

import com.cm.sanchalak.entity.Subject;
import com.cm.sanchalak.entity.Teacher;
import java.util.Set;

public class TeacherResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String qualification;
    private String profileImage;
    private Set<Subject> specializations;
    
    public TeacherResponse() {}
    
    public TeacherResponse(Teacher teacher) {
        this.id = teacher.getId();
        this.name = teacher.getName();
        this.email = teacher.getEmail();
        this.phone = teacher.getPhone();
        this.qualification = teacher.getQualification();
        this.profileImage = teacher.getProfileImage();
        this.specializations = teacher.getSpecializations();
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

    public Set<Subject> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(Set<Subject> specializations) {
        this.specializations = specializations;
    }
}
