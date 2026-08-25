package com.campus.model;

/**
 * Student Entity class extending student profile details.
 */
public class Student {
    private int studentId;
    private int userId;
    private String branch;
    private double cgpa;
    private int graduationYear;
    private int activeBacklogs;
    private String skills;

    // Joined User Details for convenient UI rendering
    private String name;
    private String email;

    public Student() {}

    public Student(int studentId, int userId, String branch, double cgpa, int graduationYear, int activeBacklogs, String skills) {
        this.studentId = studentId;
        this.userId = userId;
        this.branch = branch;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
        this.activeBacklogs = activeBacklogs;
        this.skills = skills;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }

    public int getActiveBacklogs() {
        return activeBacklogs;
    }

    public void setActiveBacklogs(int activeBacklogs) {
        this.activeBacklogs = activeBacklogs;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
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

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", branch='" + branch + '\'' +
                ", cgpa=" + cgpa +
                ", graduationYear=" + graduationYear +
                ", activeBacklogs=" + activeBacklogs +
                ", skills='" + skills + '\'' +
                '}';
    }
}
