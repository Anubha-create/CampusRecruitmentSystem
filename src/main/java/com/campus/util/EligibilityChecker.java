package com.campus.util;

import com.campus.model.JobDrive;
import com.campus.model.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AUTOMATIC ELIGIBILITY ENGINE
 * Evaluates student profiles against job drive criteria using OOP principles.
 */
public class EligibilityChecker {

    /**
     * Inner Result DTO class representing eligibility check evaluation output.
     */
    public static class EligibilityResult {
        private final boolean eligible;
        private final List<String> reasons;

        public EligibilityResult(boolean eligible, List<String> reasons) {
            this.eligible = eligible;
            this.reasons = reasons;
        }

        public boolean isEligible() {
            return eligible;
        }

        public List<String> getReasons() {
            return reasons;
        }

        public String getReasonSummary() {
            if (eligible) {
                return "Eligible for this job drive.";
            } else {
                return "Not Eligible - " + String.join("; ", reasons);
            }
        }
    }

    /**
     * Core method to evaluate eligibility of a student for a specific job drive.
     * @param student Student entity
     * @param jobDrive JobDrive entity
     * @return EligibilityResult object containing status and reasons
     */
    public static EligibilityResult checkEligibility(Student student, JobDrive jobDrive) {
        if (student == null || jobDrive == null) {
            return new EligibilityResult(false, List.of("Invalid student or job details provided."));
        }

        List<String> failureReasons = new ArrayList<>();

        // 1. CGPA Criterion Check
        if (student.getCgpa() < jobDrive.getMinimumCgpa()) {
            failureReasons.add(String.format("Minimum CGPA requirement not met (Required: %.2f, Your CGPA: %.2f)",
                    jobDrive.getMinimumCgpa(), student.getCgpa()));
        }

        // 2. Active Backlogs Criterion Check
        if (student.getActiveBacklogs() > jobDrive.getMaximumBacklogs()) {
            failureReasons.add(String.format("Active backlogs limit exceeded (Maximum allowed: %d, Your backlogs: %d)",
                    jobDrive.getMaximumBacklogs(), student.getActiveBacklogs()));
        }

        // 3. Eligible Branches Criterion Check
        String studentBranch = student.getBranch() != null ? student.getBranch().trim().toUpperCase() : "";
        String allowedBranchesRaw = jobDrive.getEligibleBranches() != null ? jobDrive.getEligibleBranches() : "";

        List<String> allowedBranches = Arrays.stream(allowedBranchesRaw.split("[,;\\s]+"))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(b -> !b.isEmpty())
                .toList();

        boolean branchEligible = allowedBranches.isEmpty() || 
                allowedBranches.contains("ALL") || 
                allowedBranches.contains("ANY") || 
                allowedBranches.contains(studentBranch);

        if (!branchEligible) {
            failureReasons.add(String.format("Branch '%s' is not eligible (Eligible branches: %s)",
                    student.getBranch(), jobDrive.getEligibleBranches()));
        }

        boolean isEligible = failureReasons.isEmpty();
        return new EligibilityResult(isEligible, failureReasons);
    }
}
