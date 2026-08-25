/**
 * Admin / Placement Officer Dashboard JavaScript Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    loadAdminStats();
    loadAdminStudents();
    loadAdminCompanies();
    filterApplications();
});

// Load Placement Dashboard KPI Cards
async function loadAdminStats() {
    try {
        const res = await fetch('api/admin?action=stats');
        if (res.status === 401 || res.status === 403) {
            window.location.href = 'login.html';
            return;
        }
        const data = await res.json();
        if (data.status === 'success') {
            const stats = data.stats;
            document.getElementById('statTotalStudents').textContent = stats.totalStudents || 0;
            document.getElementById('statTotalCompanies').textContent = stats.totalCompanies || 0;
            document.getElementById('statTotalJobDrives').textContent = stats.totalJobDrives || 0;
            document.getElementById('statTotalApplications').textContent = stats.totalApplications || 0;
            document.getElementById('statTotalSelected').textContent = stats.totalSelectedStudents || 0;
        }
    } catch (e) {
        console.error("Failed to load admin stats:", e);
    }
}

// Load and search students table
async function loadAdminStudents(query = '') {
    const tableBody = document.getElementById('adminStudentsTableBody');
    if (!tableBody) return;

    try {
        const res = await fetch(`api/student?action=search&query=${encodeURIComponent(query)}`);
        const data = await res.json();
        if (data.status === 'success') {
            renderAdminStudentsTable(data.students, tableBody);
        }
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load students.</td></tr>`;
    }
}

function renderAdminStudentsTable(students, tableBody) {
    if (!students || students.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No student records found.</td></tr>`;
        return;
    }

    tableBody.innerHTML = students.map((s, idx) => `
        <tr>
            <td>${idx + 1}</td>
            <td><strong>${s.name}</strong></td>
            <td>${s.email}</td>
            <td>${s.branch}</td>
            <td>${s.cgpa}</td>
            <td>${s.activeBacklogs}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteStudentByAdmin(${s.studentId})">Delete</button>
            </td>
        </tr>
    `).join('');
}

async function deleteStudentByAdmin(studentId) {
    if (!confirm("Are you sure you want to delete this student account? All application history will be removed.")) return;

    try {
        const res = await fetch('api/student', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', studentId: studentId })
        });
        const data = await res.json();
        if (data.status === 'success') {
            alert(data.message);
            loadAdminStudents();
            loadAdminStats();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to delete student: " + e.message);
    }
}

// Load companies
async function loadAdminCompanies() {
    const tableBody = document.getElementById('adminCompaniesTableBody');
    if (!tableBody) return;

    try {
        const res = await fetch('api/company?action=list');
        const data = await res.json();
        if (data.status === 'success') {
            renderAdminCompaniesTable(data.companies, tableBody);
        }
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="4" class="text-danger">Failed to load companies.</td></tr>`;
    }
}

function renderAdminCompaniesTable(companies, tableBody) {
    if (!companies || companies.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-muted);">No recruiter companies registered yet.</td></tr>`;
        return;
    }

    tableBody.innerHTML = companies.map((c, idx) => `
        <tr>
            <td>${idx + 1}</td>
            <td><strong>${c.companyName}</strong></td>
            <td>${c.email}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteCompanyByAdmin(${c.companyId})">Delete</button>
            </td>
        </tr>
    `).join('');
}

async function addCompanyByAdmin(event) {
    event.preventDefault();
    clearAlert('addCompanyAlert');

    const payload = {
        action: 'add',
        companyName: document.getElementById('adminCompName').value,
        email: document.getElementById('adminCompEmail').value,
        password: document.getElementById('adminCompPassword').value,
        companyDescription: document.getElementById('adminCompDesc').value
    };

    try {
        const res = await fetch('api/company', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.status === 'success') {
            showAlert('addCompanyAlert', data.message, 'success');
            document.getElementById('addCompanyForm').reset();
            loadAdminCompanies();
            loadAdminStats();
        } else {
            showAlert('addCompanyAlert', data.message, 'danger');
        }
    } catch (e) {
        showAlert('addCompanyAlert', 'Failed to add company: ' + e.message, 'danger');
    }
}

async function deleteCompanyByAdmin(companyId) {
    if (!confirm("Are you sure you want to delete this company? All company drives and applications will be deleted.")) return;

    try {
        const res = await fetch('api/company', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', companyId: companyId })
        });
        const data = await res.json();
        if (data.status === 'success') {
            alert(data.message);
            loadAdminCompanies();
            loadAdminStats();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to delete company: " + e.message);
    }
}

// Filter all applications across system
async function filterApplications() {
    const tableBody = document.getElementById('allApplicationsTableBody');
    if (!tableBody) return;

    const company = document.getElementById('filterCompany')?.value || '';
    const role = document.getElementById('filterRole')?.value || '';
    const student = document.getElementById('filterStudent')?.value || '';
    const status = document.getElementById('filterStatus')?.value || '';

    const queryParams = new URLSearchParams({
        action: 'applications',
        company: company,
        role: role,
        student: student,
        status: status
    });

    try {
        const res = await fetch(`api/admin?${queryParams.toString()}`);
        const data = await res.json();
        if (data.status === 'success') {
            renderAllApplicationsTable(data.applications, tableBody);
        }
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to filter applications.</td></tr>`;
    }
}

function renderAllApplicationsTable(apps, tableBody) {
    if (!apps || apps.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No application records match the search filter criteria.</td></tr>`;
        return;
    }

    tableBody.innerHTML = apps.map((a, idx) => `
        <tr>
            <td>${idx + 1}</td>
            <td><strong>${a.studentName}</strong><br><span style="font-size:0.8rem; color:var(--text-muted);">${a.studentBranch} | CGPA: ${a.studentCgpa}</span></td>
            <td><strong>${a.companyName}</strong></td>
            <td>${a.jobRole}</td>
            <td>${formatDate(a.applicationDate)}</td>
            <td>${getStatusBadge(a.applicationStatus)}</td>
            <td>
                <select onchange="updateApplicationStatusByAdmin(${a.applicationId}, this.value)" style="padding:0.3rem 0.5rem; font-size:0.85rem;">
                    <option value="" disabled selected>Update Status...</option>
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

async function updateApplicationStatusByAdmin(appId, newStatus) {
    try {
        const res = await fetch('api/application', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'updateStatus', applicationId: appId, status: newStatus })
        });
        const data = await res.json();
        if (data.status === 'success') {
            filterApplications();
            loadAdminStats();
        } else {
            alert(data.message);
        }
    } catch (e) {
        alert("Failed to update status: " + e.message);
    }
}
