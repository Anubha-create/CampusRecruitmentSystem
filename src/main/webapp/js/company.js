/**
 * Company Recruiter Dashboard & Candidate Pipeline JavaScript Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    loadCompanyProfile();
    loadCompanyJobDrives();
    loadCompanyApplicants();
});

let currentCompany = null;

async function loadCompanyProfile() {
    try {
        const res = await fetch('api/company?action=me');
        if (res.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        const data = await res.json();
        if (data.status === 'success') {
            currentCompany = data.company;
            const el = document.getElementById('companyHeaderName');
            if (el) el.textContent = data.company.companyName;
        }
    } catch (e) {
        console.error("Failed to load company profile:", e);
    }
}

// Post new job drive
async function createJobDrive(event) {
    event.preventDefault();
    clearAlert('jobDriveAlert');

    const payload = {
        action: 'create',
        jobRole: document.getElementById('jobRole').value,
        package: document.getElementById('packageAmount').value,
        minimumCgpa: parseFloat(document.getElementById('minimumCgpa').value),
        eligibleBranches: document.getElementById('eligibleBranches').value,
        maximumBacklogs: parseInt(document.getElementById('maximumBacklogs').value),
        requiredSkills: document.getElementById('requiredSkills').value,
        jobDescription: document.getElementById('jobDescription').value,
        applicationDeadline: document.getElementById('applicationDeadline').value
    };

    try {
        const res = await fetch('api/job-drive', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.status === 'success') {
            showAlert('jobDriveAlert', data.message, 'success');
            document.getElementById('createJobForm').reset();
            loadCompanyJobDrives();
        } else {
            showAlert('jobDriveAlert', data.message, 'danger');
        }
    } catch (e) {
        showAlert('jobDriveAlert', 'Failed to create job drive: ' + e.message, 'danger');
    }
}

// Load jobs created by company
async function loadCompanyJobDrives() {
    const container = document.getElementById('companyDrivesContainer');
    if (!container) return;

    try {
        const res = await fetch('api/job-drive?action=company');
        const data = await res.json();
        if (data.status === 'success') {
            renderCompanyDrives(data.jobDrives, container);
        }
    } catch (e) {
        container.innerHTML = `<div class="alert alert-danger">Failed to load posted job drives.</div>`;
    }
}

function renderCompanyDrives(drives, container) {
    if (!drives || drives.length === 0) {
        container.innerHTML = `<p class="text-muted">You have not posted any campus recruitment drives yet.</p>`;
        return;
    }

    container.innerHTML = drives.map(d => `
        <div class="card" style="margin-bottom:1rem;">
            <div class="card-header">
                <div>
                    <h3>${d.jobRole}</h3>
                    <div style="font-size:0.85rem; color:var(--text-muted);">Package: ${d.packageAmount} | Deadline: ${formatDate(d.applicationDeadline)}</div>
                </div>
                <div>
                    <button class="btn btn-danger btn-sm" onclick="deleteJobDrive(${d.jobId})">Delete Drive</button>
                </div>
            </div>
            <div style="font-size:0.9rem; color:var(--text-muted);">
                Criteria: CGPA >= ${d.minimumCgpa} | Branches: ${d.eligibleBranches} | Max Backlogs: ${d.maximumBacklogs}
            </div>
        </div>
    `).join('');
}

async function deleteJobDrive(jobId) {
    if (!confirm("Are you sure you want to delete this job drive? All related applications will be deleted.")) return;

    try {
        const res = await fetch('api/job-drive', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', jobId: jobId })
        });
        const data = await res.json();
        if (data.status === 'success') {
            alert(data.message);
            loadCompanyJobDrives();
            loadCompanyApplicants();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to delete drive: " + e.message);
    }
}

// Load applicants across company drives
async function loadCompanyApplicants() {
    const tableBody = document.getElementById('applicantsTableBody');
    if (!tableBody) return;

    try {
        const res = await fetch('api/application?action=company');
        const data = await res.json();
        if (data.status === 'success') {
            renderApplicantsTable(data.applications, tableBody);
        }
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load applicant pipeline.</td></tr>`;
    }
}

function renderApplicantsTable(apps, tableBody) {
    if (!apps || apps.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No student applications received yet.</td></tr>`;
        return;
    }

    tableBody.innerHTML = apps.map((a, idx) => `
        <tr>
            <td>${idx + 1}</td>
            <td><strong>${a.studentName}</strong><br><span style="font-size:0.8rem; color:var(--text-muted);">${a.studentEmail}</span></td>
            <td>${a.jobRole}</td>
            <td>${a.studentBranch}</td>
            <td>CGPA: <strong>${a.studentCgpa}</strong><br>Backlogs: ${a.studentBacklogs}</td>
            <td>${getStatusBadge(a.applicationStatus)}</td>
            <td>
                <select onchange="updateCandidateStatus(${a.applicationId}, this.value)" style="padding:0.3rem 0.5rem; font-size:0.85rem;">
                    <option value="" disabled selected>Change Status...</option>
                    <option value="Applied">Applied</option>
                    <option value="Shortlisted">Shortlisted</option>
                    <option value="Aptitude Test">Aptitude Test</option>
                    <option value="Interview">Interview</option>
                    <option value="Selected">Selected</option>
                    <option value="Rejected">Rejected</option>
                </select>
            </td>
        </tr>
    `).join('');
}

async function updateCandidateStatus(applicationId, newStatus) {
    if (!newStatus) return;

    try {
        const res = await fetch('api/application', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'updateStatus', applicationId: applicationId, status: newStatus })
        });
        const data = await res.json();
        if (data.status === 'success') {
            loadCompanyApplicants();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to update candidate status: " + e.message);
    }
}
