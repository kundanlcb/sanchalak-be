package com.cm.sanchalak.dto;

import java.util.List;

public class ReportCardDto {
    private String studentName;
    private String className;
    private List<TermReport> terms;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<TermReport> getTerms() { return terms; }
    public void setTerms(List<TermReport> terms) { this.terms = terms; }

    public static class TermReport {
        private String termName;
        private List<SubjectReport> subjects;

        public String getTermName() { return termName; }
        public void setTermName(String termName) { this.termName = termName; }
        public List<SubjectReport> getSubjects() { return subjects; }
        public void setSubjects(List<SubjectReport> subjects) { this.subjects = subjects; }
    }

    public static class SubjectReport {
        private String subjectName;
        private Double marksObtained;
        private Integer maxMarks;

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public Double getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
        public Integer getMaxMarks() { return maxMarks; }
        public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    }
}
