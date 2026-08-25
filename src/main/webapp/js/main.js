/**
 * Common JavaScript Utilities for Campus Recruitment Management System
 */

// Formats application status into colored HTML badge
function getStatusBadge(status) {
    if (!status) return '';
    const s = status.trim();
    let badgeClass = 'badge-applied';

    if (s === 'Selected') badgeClass = 'badge-selected';
    else if (s === 'Rejected') badgeClass = 'badge-rejected';
    else if (['Shortlisted', 'Aptitude Test', 'Interview'].includes(s)) badgeClass = 'badge-shortlisted';

    return `<span class="badge ${badgeClass}">${s}</span>`;
}

// Formats eligibility result badge
function getEligibilityBadge(isEligible, reasonSummary) {
    if (isEligible) {
        return `<span class="badge badge-eligible" title="You satisfy all criteria">Eligible</span>`;
    } else {
        return `<span class="badge badge-not-eligible" title="${reasonSummary || 'Not eligible'}">Not Eligible</span>`;
    }
}

// Displays alert messages inside container
function showAlert(containerId, message, type = 'danger') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `
        <div class="alert alert-${type}">
            <span>${message}</span>
        </div>
    `;
}

// Clears alert messages
function clearAlert(containerId) {
    const el = document.getElementById(containerId);
    if (el) el.innerHTML = '';
}

// Session Logout helper
async function logoutUser() {
    try {
        const res = await fetch('api/logout', { method: 'POST', headers: { 'X-Requested-With': 'XMLHttpRequest' } });
        const data = await res.json();
        window.location.href = data.redirectUrl || 'login.html';
    } catch (e) {
        window.location.href = 'login.html';
    }
}

// Format date helper
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}
