package com.campus.model;

import java.sql.Timestamp;

/**
 * Application Entity class representing student applications for job drives.
 */
public class Application {
    private int applicationId;
    private int studentId;
    private int jobId;
    private Timestamp applicationDate;
    private String applicationStatus; // Applied, Shortlisted, Aptitude Test, Interview, Selected, Rejected

    // Joined Details for display
    private String studentName;
    private String studentEmail;
    private String studentBranch;
    private double studentCgpa;
    private int studentBacklogs;

    private String companyName;
    private String jobRole;
    private String packageAmount;

    public Application() {}

    public Application(int applicationId, int studentId, int jobId, Timestamp applicationDate, String applicationStatus) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.applicationStatus = applicationStatus;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public Timestamp getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Timestamp applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentBranch() {
        return studentBranch;
    }

    public void setStudentBranch(String studentBranch) {
        this.studentBranch = studentBranch;
    }

    public double getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(double studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public int getStudentBacklogs() {
        return studentBacklogs;
    }

    public void setStudentBacklogs(int studentBacklogs) {
        this.studentBacklogs = studentBacklogs;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getPackageAmount() {
        return packageAmount;
    }

    public void setPackageAmount(String packageAmount) {
        this.packageAmount = packageAmount;
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationId=" + applicationId +
                ", studentId=" + studentId +
                ", jobId=" + jobId +
                ", applicationStatus='" + applicationStatus + '\'' +
                '}';
    }
}
