package com.campus.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * JobDrive Entity class representing placement drives and hiring criteria.
 */
public class JobDrive {
    private int jobId;
    private int companyId;
    private String jobRole;
    private String packageAmount;
    private double minimumCgpa;
    private String eligibleBranches;
    private int maximumBacklogs;
    private String requiredSkills;
    private String jobDescription;
    private Date applicationDeadline;
    private Timestamp createdAt;

    // Joined Company Details
    private String companyName;

    public JobDrive() {}

    public JobDrive(int jobId, int companyId, String jobRole, String packageAmount, double minimumCgpa,
                    String eligibleBranches, int maximumBacklogs, String requiredSkills,
                    String jobDescription, Date applicationDeadline, Timestamp createdAt) {
        this.jobId = jobId;
        this.companyId = companyId;
        this.jobRole = jobRole;
        this.packageAmount = packageAmount;
        this.minimumCgpa = minimumCgpa;
        this.eligibleBranches = eligibleBranches;
        this.maximumBacklogs = maximumBacklogs;
        this.requiredSkills = requiredSkills;
        this.jobDescription = jobDescription;
        this.applicationDeadline = applicationDeadline;
        this.createdAt = createdAt;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
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

    public double getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(double minimumCgpa) {
        this.minimumCgpa = minimumCgpa;
    }

    public String getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(String eligibleBranches) {
        this.eligibleBranches = eligibleBranches;
    }

    public int getMaximumBacklogs() {
        return maximumBacklogs;
    }

    public void setMaximumBacklogs(int maximumBacklogs) {
        this.maximumBacklogs = maximumBacklogs;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Date getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(Date applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "JobDrive{" +
                "jobId=" + jobId +
                ", companyId=" + companyId +
                ", jobRole='" + jobRole + '\'' +
                ", packageAmount='" + packageAmount + '\'' +
                ", minimumCgpa=" + minimumCgpa +
                '}';
    }
}
