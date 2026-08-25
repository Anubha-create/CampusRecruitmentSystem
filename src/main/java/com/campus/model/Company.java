package com.campus.model;

/**
 * Company Entity class representing company profiles and recruiter accounts.
 */
public class Company {
    private int companyId;
    private int userId;
    private String companyName;
    private String companyDescription;

    // Joined User Email for convenience
    private String email;

    public Company() {}

    public Company(int companyId, int userId, String companyName, String companyDescription) {
        this.companyId = companyId;
        this.userId = userId;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Company{" +
                "companyId=" + companyId +
                ", userId=" + userId +
                ", companyName='" + companyName + '\'' +
                '}';
    }
}
