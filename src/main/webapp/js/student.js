/**
 * Student Dashboard & Placement Portal JavaScript Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    loadStudentProfile();
    loadAvailableJobs();
    loadApplicationHistory();
});

let currentStudent = null;

// Fetch logged-in student profile
async function loadStudentProfile() {
    try {
        const res = await fetch('api/student?action=me');
        if (res.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        const data = await res.json();
        if (data.status === 'success') {
            currentStudent = data.student;
            renderProfileHeader(data.student);
            fillProfileForm(data.student);
        }
    } catch (e) {
        console.error("Failed to load student profile:", e);
    }
}

function renderProfileHeader(student) {
    const el = document.getElementById('studentHeaderName');
    if (el) el.textContent = student.name;
    const badge = document.getElementById('studentBranchBadge');
    if (badge) badge.textContent = `${student.branch} | CGPA: ${student.cgpa} | Backlogs: ${student.activeBacklogs}`;
}

function fillProfileForm(student) {
    const nameInput = document.getElementById('profName');
    const branchInput = document.getElementById('profBranch');
    const cgpaInput = document.getElementById('profCgpa');
    const gradInput = document.getElementById('profGradYear');
    const backlogsInput = document.getElementById('profBacklogs');
    const skillsInput = document.getElementById('profSkills');

    if (nameInput) nameInput.value = student.name || '';
    if (branchInput) branchInput.value = student.branch || '';
    if (cgpaInput) cgpaInput.value = student.cgpa || 0;
    if (gradInput) gradInput.value = student.graduationYear || 2026;
    if (backlogsInput) backlogsInput.value = student.activeBacklogs || 0;
    if (skillsInput) skillsInput.value = student.skills || '';
}

// Update profile submit
async function updateProfile(event) {
    event.preventDefault();
    clearAlert('profileAlert');

    const payload = {
        action: 'update',
        name: document.getElementById('profName').value,
        branch: document.getElementById('profBranch').value,
        cgpa: parseFloat(document.getElementById('profCgpa').value),
        graduationYear: parseInt(document.getElementById('profGradYear').value),
        activeBacklogs: parseInt(document.getElementById('profBacklogs').value),
        skills: document.getElementById('profSkills').value
    };

    try {
        const res = await fetch('api/student', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.status === 'success') {
            showAlert('profileAlert', data.message, 'success');
            loadStudentProfile();
            loadAvailableJobs(); // Refresh eligibility status on jobs
        } else {
            showAlert('profileAlert', data.message, 'danger');
        }
    } catch (e) {
        showAlert('profileAlert', 'Failed to update profile: ' + e.message, 'danger');
    }
}

// Load all available job drives and render eligibility checks
async function loadAvailableJobs() {
    const container = document.getElementById('jobListContainer');
    if (!container) return;

    try {
        const res = await fetch('api/job-drive');
        const data = await res.json();
        if (data.status === 'success') {
            renderJobList(data.jobDrives, container);
        }
    } catch (e) {
        container.innerHTML = `<div class="alert alert-danger">Failed to load job drives.</div>`;
    }
}

function renderJobList(jobs, container) {
    if (!jobs || jobs.length === 0) {
        container.innerHTML = `<div class="card"><p class="text-muted">No campus recruitment drives are currently active.</p></div>`;
        return;
    }

    container.innerHTML = jobs.map(j => {
        const eligibilityBadge = getEligibilityBadge(j.isEligible, j.eligibilityReason);
        const reasonText = !j.isEligible ? `<div class="text-danger" style="font-size:0.85rem; margin-top:0.3rem;">Reason: ${j.eligibilityReason}</div>` : '';
        
        let actionBtn = '';
        if (j.hasApplied) {
            actionBtn = `<span class="badge badge-applied">Already Applied</span>`;
        } else if (j.isEligible) {
            actionBtn = `<button class="btn btn-primary btn-sm" onclick="applyJob(${j.jobId})">Apply Now</button>`;
        } else {
            actionBtn = `<button class="btn btn-secondary btn-sm" disabled style="opacity:0.5; cursor:not-allowed;">Not Eligible</button>`;
        }

        return `
            <div class="card" style="margin-bottom:1rem;">
                <div class="card-header">
                    <div>
                        <h3 style="margin-bottom:0.2rem;">${j.jobRole}</h3>
                        <div style="color:var(--primary); font-weight:600;">${j.companyName}</div>
                    </div>
                    <div>${eligibilityBadge}</div>
                </div>
                <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap:1rem; margin-bottom:1rem; font-size:0.9rem; color:var(--text-muted);">
                    <div><strong>Package:</strong> ${j.packageAmount}</div>
                    <div><strong>Min CGPA:</strong> ${j.minimumCgpa}</div>
                    <div><strong>Eligible Branches:</strong> ${j.eligibleBranches}</div>
                    <div><strong>Max Backlogs:</strong> ${j.maximumBacklogs}</div>
                    <div><strong>Deadline:</strong> ${formatDate(j.applicationDeadline)}</div>
                </div>
                <p style="font-size:0.9rem; color:var(--text-main); margin-bottom:1rem;">${j.jobDescription}</p>
                ${reasonText}
                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:1rem; padding-top:0.75rem; border-top:1px solid var(--border-color);">
                    <div style="font-size:0.85rem; color:var(--text-muted);">Required Skills: <strong>${j.requiredSkills}</strong></div>
                    <div>${actionBtn}</div>
                </div>
            </div>
        `;
    }).join('');
}

// Apply for an eligible job drive
async function applyJob(jobId) {
    if (!confirm("Are you sure you want to submit your application for this job drive?")) return;

    try {
        const res = await fetch('api/application', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'apply', jobId: jobId })
        });
        const data = await res.json();
        if (data.status === 'success') {
            alert(data.message);
            loadAvailableJobs();
            loadApplicationHistory();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to submit application: " + e.message);
    }
}

// Load application history for student
async function loadApplicationHistory() {
    const tableBody = document.getElementById('historyTableBody');
    if (!tableBody) return;

    try {
        const res = await fetch('api/application?action=history');
        const data = await res.json();
        if (data.status === 'success') {
            renderHistoryTable(data.applications, tableBody);
        }
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="5" class="text-danger">Failed to load application history.</td></tr>`;
    }
}

function renderHistoryTable(apps, tableBody) {
    if (!apps || apps.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No job applications submitted yet.</td></tr>`;
        return;
    }

    tableBody.innerHTML = apps.map((a, index) => `
        <tr>
            <td>${index + 1}</td>
            <td><strong>${a.companyName}</strong></td>
            <td>${a.jobRole}</td>
            <td>${formatDate(a.applicationDate)}</td>
            <td>${getStatusBadge(a.applicationStatus)}</td>
        </tr>
    `).join('');
}
