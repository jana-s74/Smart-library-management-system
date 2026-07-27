/**
 * 📚 LibraAI Application JavaScript Engine
 * Connects frontend UI to Java HTTP Web Backend
 */

// Application Global State
const state = {
    currentUser: null,
    activeTab: 'dashboard',
    books: [],
    students: [],
    borrowHistory: [],
    catalogView: 'grid',
    selectedDetailBook: null,
    attendanceLogs: [],
    attendanceCurrentlyInside: 0,
    attendanceTodayVisits: 0,
    // Issue modal multi-step state
    issueSelectedStudentId: null,
    issuePendingBookId: null,
    issuePendingLoanDays: null,
    studentDeptFilter: 'ALL'
};

// API Base URL
const API_BASE = window.location.origin.startsWith('file:') ? 'http://localhost:8080' : '';

document.addEventListener("DOMContentLoaded", function () {
    console.log("🚀 Initializing LibraAI Web Application...");
    const savedUser = localStorage.getItem("libraai_user");
    if (savedUser) {
        try {
            state.currentUser = JSON.parse(savedUser);
            showDashboard();
        } catch (e) {
            localStorage.removeItem("libraai_user");
            showLanding();
        }
    } else {
        showLanding();
    }

    // Auto-fill email dynamically as Name and Register Number are entered
    const regCode = document.getElementById("regCode");
    const regFullName = document.getElementById("regFullName");
    const regEmail = document.getElementById("regEmail");
    if (regCode && regFullName && regEmail) {
        const updateEmail = () => {
            const code = regCode.value.trim();
            const name = regFullName.value.trim();
            if (code.length >= 4 && name.length > 0) {
                const last4 = code.substring(code.length - 4);
                const cleanName = name.toLowerCase().replace(/[^a-z]/g, "");
                regEmail.value = `${cleanName}${last4}.ai24@gmail.com`;
            }
        };
        regCode.addEventListener("input", updateEmail);
        regFullName.addEventListener("input", updateEmail);
    }
});

function showLanding() {
    document.getElementById('landingPage') && document.getElementById('landingPage').classList.remove('hidden');
    document.getElementById('dashboardPage') && document.getElementById('dashboardPage').classList.add('hidden');
}

function showDashboard() {
    document.getElementById('landingPage') && document.getElementById('landingPage').classList.add('hidden');
    document.getElementById('dashboardPage') && document.getElementById('dashboardPage').classList.remove('hidden');
    updateUserUI();
    checkHealth();
    fetchStats();
    fetchCatalogBooks();
    fetchBorrowHistory();
    fetchStudents();
    fetchOverdueData();
    setTimeout(renderAnalyticsCanvasChart, 300);
}

// ==================== API FETCHERS ====================

async function checkHealth() {
    try {
        const res = await fetch(`${API_BASE}/api/health`);
        if (res.ok) {
            document.getElementById("serverStatusBadge").innerHTML = `
                <span class="status-dot online"></span>
                <span>Backend Connected (JDK HttpServer)</span>
            `;
        }
    } catch (err) {
        document.getElementById("serverStatusBadge").innerHTML = `
            <span class="status-dot offline" style="background:#EF4444;box-shadow:0 0 8px #EF4444;"></span>
            <span style="color:#FCA5A5">Backend Offline</span>
        `;
    }
}

async function fetchStats() {
    try {
        const res = await fetch(`${API_BASE}/api/stats`);
        if (res.ok) {
            const data = await res.json();
            document.getElementById("statTotalTitles").innerText = data.totalBookTitles || 0;
            document.getElementById("statTotalCopiesSub").innerText = `${data.totalBookCopies || 0} Copies Total`;
            document.getElementById("statAvailableCopies").innerText = data.availableCopies || 0;
            document.getElementById("statActiveBorrows").innerText = data.activeBorrows || 0;
            document.getElementById("statTotalStudents").innerText = data.totalStudents || 0;
        }
    } catch (err) {
        console.error("Failed to fetch stats:", err);
    }
}

async function fetchCatalogBooks() {
    try {
        const sort = document.getElementById("sortSelect") ? document.getElementById("sortSelect").value : 'title';
        const search = document.getElementById("catalogSearchInput") ? document.getElementById("catalogSearchInput").value.trim() : '';
        
        let url = `${API_BASE}/api/books?sort=${encodeURIComponent(sort)}`;
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }

        const res = await fetch(url);
        if (res.ok) {
            state.books = await res.json();
            renderPopularBooks();
            renderCatalogBooks();
            populateIssueSelects();
        }
    } catch (err) {
        console.error("Failed to fetch books:", err);
    }
}

async function fetchBorrowHistory() {
    try {
        const res = await fetch(`${API_BASE}/api/borrow/history`);
        if (res.ok) {
            state.borrowHistory = await res.json();
            renderBorrowHistoryTable();
        }
    } catch (err) {
        console.error("Failed to fetch borrow history:", err);
    }
}

async function fetchStudents() {
    try {
        const res = await fetch(`${API_BASE}/api/students`);
        if (res.ok) {
            state.students = await res.json();
            renderStudentsTable();
            populateIssueSelects();
        }
    } catch (err) {
        console.error("Failed to fetch students:", err);
    }
}

async function fetchOverdueData() {
    const panel = document.getElementById('overdueAlertBody');
    if (!panel) return;
    try {
        const res = await fetch(`${API_BASE}/api/borrow/overdue`);
        if (!res.ok) throw new Error('API error');
        const data = await res.json();
        const count = data.overdueCount || 0;
        const students = data.overdueStudents || [];
        if (count === 0) {
            panel.innerHTML = `<div style="text-align:center;padding:2rem;">
                <div style="font-size:2.5rem;">✅</div>
                <p style="color:#10B981;font-weight:600;margin-top:0.5rem;">All books returned!</p>
                <p style="color:#6B7280;font-size:0.85rem;">No overdue students.</p>
            </div>`;
            document.getElementById('overdueAlertPanel') && (document.getElementById('overdueAlertPanel').style.borderColor = 'rgba(16,185,129,0.3)');
        } else {
            document.getElementById('overdueAlertPanel') && (document.getElementById('overdueAlertPanel').style.borderColor = 'rgba(239,68,68,0.5)');
            panel.innerHTML = `
                <div style="padding:0.5rem 1rem;background:rgba(239,68,68,0.1);border-radius:8px;margin-bottom:0.75rem;">
                    <strong style="color:#EF4444;font-size:1.1rem;">⚠️ ${count} overdue loan${count > 1 ? 's' : ''}</strong>
                    <p style="color:#FCA5A5;font-size:0.8rem;margin:0;">Students who have not returned books</p>
                </div>
                ${students.map(s => `
                    <div style="padding:0.75rem 1rem;border-bottom:1px solid rgba(255,255,255,0.06);">
                        <div style="display:flex;justify-content:space-between;align-items:center;">
                            <div>
                                <div style="font-weight:600;color:#F1F5F9;">${escapeHtml(s.studentName)}</div>
                                <div style="font-size:0.78rem;color:#94A3B8;">Reg: ${escapeHtml(s.studentCode)}</div>
                                <div style="font-size:0.8rem;color:#CBD5E1;margin-top:2px;">📖 ${escapeHtml(s.bookTitle)}</div>
                            </div>
                            <div style="text-align:right;">
                                <div style="color:#EF4444;font-weight:700;font-size:0.9rem;">${s.daysOverdue} day${s.daysOverdue !== 1 ? 's' : ''}</div>
                                <div style="color:#FCA5A5;font-size:0.78rem;">overdue</div>
                                ${s.fineAmount > 0 ? `<div style="color:#F59E0B;font-size:0.78rem;font-weight:600;">₹${s.fineAmount.toFixed(2)} fine</div>` : ''}
                            </div>
                        </div>
                    </div>
                `).join('')}
            `;
        }
    } catch (err) {
        panel.innerHTML = `<div style="text-align:center;padding:1rem;color:#EF4444;">Unable to load overdue data.</div>`;
    }
}

function getBookCoverUrl(book) {
    if (book.coverImagePath && book.coverImagePath.trim().length > 0) {
        return book.coverImagePath;
    }
    if (book.isbn && book.isbn.trim()) {
        const cleanIsbn = book.isbn.replace(/[^0-9]/g, '');
        if (cleanIsbn.length === 10 || cleanIsbn.length === 13) {
            return `https://covers.openlibrary.org/b/isbn/${cleanIsbn}-M.jpg`;
        }
    }
    return null;
}

function renderBookCoverHTML(book) {
    const coverUrl = getBookCoverUrl(book);
    const titleEscaped = escapeHtml(book.title);
    const authorEscaped = escapeHtml(book.author);
    
    let hash = 0;
    for (let i = 0; i < book.title.length; i++) {
        hash = book.title.charCodeAt(i) + ((hash << 5) - hash);
    }
    const hue1 = Math.abs(hash % 360);
    const hue2 = (hue1 + 60) % 360;
    const gradient = `linear-gradient(135deg, hsl(${hue1}, 65%, 45%), hsl(${hue2}, 75%, 35%))`;

    if (coverUrl) {
        return `
            <div class="book-cover-wrapper" style="width:100%;height:100%;position:relative;background:${gradient};overflow:hidden;border-radius:var(--r-sm);">
                <img src="${coverUrl}" alt="${titleEscaped}" 
                     style="width:100%;height:100%;object-fit:cover;display:block;"
                     onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                <div class="fallback-cover" style="display:none;width:100%;height:100%;position:absolute;top:0;left:0;flex-direction:column;justify-content:space-between;padding:12px;color:white;font-weight:bold;text-align:center;box-sizing:border-box;">
                    <div style="font-size:0.68rem;opacity:0.8;text-transform:uppercase;letter-spacing:1px;">${escapeHtml(book.categoryName || 'Library')}</div>
                    <div style="font-size:0.85rem;line-height:1.2;margin:auto 0;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;">${titleEscaped}</div>
                    <div style="font-size:0.68rem;opacity:0.9;font-weight:normal;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${authorEscaped}</div>
                </div>
            </div>
        `;
    } else {
        return `
            <div class="fallback-cover" style="display:flex;width:100%;height:100%;background:${gradient};flex-direction:column;justify-content:space-between;padding:12px;color:white;font-weight:bold;text-align:center;border-radius:var(--r-sm);box-sizing:border-box;">
                <div style="font-size:0.68rem;opacity:0.8;text-transform:uppercase;letter-spacing:1px;">${escapeHtml(book.categoryName || 'Library')}</div>
                <div style="font-size:0.85rem;line-height:1.2;margin:auto 0;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;">${titleEscaped}</div>
                <div style="font-size:0.68rem;opacity:0.9;font-weight:normal;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${authorEscaped}</div>
            </div>
        `;
    }
}

// ==================== RENDERING UI ====================

function renderPopularBooks() {
    const container = document.getElementById("popularBooksContainer");
    if (!container) return;

    if (!state.books || state.books.length === 0) {
        container.innerHTML = `<div class="loading-spinner">No books found in catalog.</div>`;
        return;
    }

    const popular = state.books.slice(0, 6);
    container.innerHTML = popular.map(b => `
        <div class="book-card" onclick="openDetailModal(${b.bookId})">
            <div class="book-cover" style="padding:0;background:none;display:block;height:160px;position:relative;">
                <span class="category-tag">${escapeHtml(b.categoryName || 'General')}</span>
                ${renderBookCoverHTML(b)}
            </div>
            <div class="book-title" style="margin-top:0.6rem;">${escapeHtml(b.title)}</div>
            <div class="book-author">by ${escapeHtml(b.author)}</div>
            <div class="book-meta">
                <span>ISBN: ${escapeHtml(b.isbn || 'N/A')}</span>
                <span class="copies-badge ${b.availableCopies > 0 ? '' : 'out'}">
                    ${b.availableCopies > 0 ? b.availableCopies + ' Available' : 'Out of Stock'}
                </span>
            </div>
            ${isAdmin() && b.availableCopies > 0 ? `
                <button class="btn btn-primary btn-sm" style="width:100%;margin-top:0.8rem;background:var(--purple);border-color:var(--purple);color:white;" onclick="event.stopPropagation(); openIssueModal(${b.bookId})">⚡ Issue Book</button>
            ` : ''}
        </div>
    `).join('');
}

function renderCatalogBooks() {
    const grid = document.getElementById("catalogGridContainer");
    const tbody = document.getElementById("catalogTableBody");
    if (!grid || !tbody) return;

    if (!state.books || state.books.length === 0) {
        grid.innerHTML = `<div class="full-width" style="text-align:center;padding:2rem;color:#6B7280;">No books match your criteria.</div>`;
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No books match your criteria.</td></tr>`;
        return;
    }

    // Grid Render
    grid.innerHTML = state.books.map(b => `
        <div class="book-card">
            <div class="book-cover" onclick="openDetailModal(${b.bookId})" style="padding:0;background:none;display:block;height:160px;position:relative;">
                <span class="category-tag">${escapeHtml(b.categoryName || 'General')}</span>
                ${renderBookCoverHTML(b)}
            </div>
            <div class="book-title" onclick="openDetailModal(${b.bookId})" style="margin-top:0.6rem;cursor:pointer;">${escapeHtml(b.title)}</div>
            <div class="book-author">by ${escapeHtml(b.author)}</div>
            <div class="book-meta">
                <span>Shelf: ${escapeHtml(b.shelfNumber || 'A-1')}</span>
                <span class="copies-badge ${b.availableCopies > 0 ? '' : 'out'}">
                    ${b.availableCopies} / ${b.totalCopies} left
                </span>
            </div>
            <div style="display:flex;gap:0.4rem;margin-top:0.8rem;width:100%;">
                <button class="btn btn-outline btn-sm" style="flex:1;" onclick="openDetailModal(${b.bookId})">Details</button>
                ${isAdmin() && b.availableCopies > 0 ? `
                    <button class="btn btn-primary btn-sm" style="flex:1;background:var(--purple);border-color:var(--purple);color:white;" onclick="openIssueModal(${b.bookId})">⚡ Issue</button>
                ` : ''}
            </div>
            ${isAdmin() ? `
                <div style="display:flex;gap:0.4rem;margin-top:0.4rem;width:100%;">
                    <button class="btn btn-outline btn-sm" style="flex:1;" onclick="editBook(${b.bookId})">✏️ Edit</button>
                    <button class="btn btn-outline btn-sm" style="color:#EF4444;flex:1;" onclick="deleteBook(${b.bookId})">🗑️ Delete</button>
                </div>
            ` : ''}
        </div>
    `).join('');

    // Table Render
    tbody.innerHTML = state.books.map(b => `
        <tr>
            <td>#${b.bookId}</td>
            <td>
                <strong>${escapeHtml(b.title)}</strong><br>
                <small style="color:#6B7280;">by ${escapeHtml(b.author)}</small>
            </td>
            <td><span class="chip">${escapeHtml(b.categoryName || 'General')}</span></td>
            <td>${escapeHtml(b.isbn || 'N/A')}</td>
            <td>${escapeHtml(b.shelfNumber || 'A-1')}</td>
            <td>${b.availableCopies} / ${b.totalCopies}</td>
            <td>
                <span class="copies-badge ${b.availableCopies > 0 ? '' : 'out'}">
                    ${b.availableCopies > 0 ? 'Available' : 'Out of Stock'}
                </span>
            </td>
            <td>
                <button class="btn btn-outline btn-sm" onclick="openDetailModal(${b.bookId})">View</button>
                ${isAdmin() ? `
                    <button class="btn btn-outline btn-sm" onclick="editBook(${b.bookId})">Edit</button>
                    <button class="btn btn-outline btn-sm" style="color:#EF4444;" onclick="deleteBook(${b.bookId})">Delete</button>
                ` : ''}
            </td>
        </tr>
    `).join('');
}

function renderBorrowHistoryTable() {
    const tbody = document.getElementById("borrowHistoryTableBody");
    if (!tbody) return;

    if (!state.borrowHistory || state.borrowHistory.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:2rem;">No borrow transactions recorded yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.borrowHistory.map(bh => {
        const isOverdue = bh.overdueDays && bh.overdueDays > 0;
        return `
        <tr style="${isOverdue ? 'background:rgba(239,68,68,0.05);' : ''}">
            <td>#${bh.borrowId}</td>
            <td>
                <strong>${escapeHtml(bh.studentName || 'Student')}</strong><br>
                <small style="color:#6B7280;">Reg: ${escapeHtml(bh.studentCode || 'N/A')}</small>
            </td>
            <td><strong>${escapeHtml(bh.bookTitle || 'Book')}</strong></td>
            <td>${formatDate(bh.borrowDate)}</td>
            <td>${formatDate(bh.dueDate)}</td>
            <td style="color:${isOverdue ? '#EF4444' : '#10B981'};font-weight:600;">
                ${isOverdue ? `⚠️ ${bh.overdueDays} day${bh.overdueDays !== 1 ? 's' : ''}` : (bh.returnDate ? '—' : '✅ On time')}
            </td>
            <td>${bh.returnDate ? formatDate(bh.returnDate) : '<span style="color:#9CA3AF;">—</span>'}</td>
            <td>
                <span class="status-tag ${bh.status ? bh.status.toLowerCase() : 'borrowed'}">
                    ${escapeHtml(bh.status || 'BORROWED')}
                </span>
            </td>
            <td style="color:${bh.fineAmount > 0 ? '#EF4444' : '#10B981'};font-weight:600;">
                ₹${(bh.fineAmount || 0).toFixed(2)}
            </td>
            <td>
                <div style="display:flex; gap:0.25rem; align-items:center;">
                    <button class="btn btn-outline btn-sm" onclick="showBorrowQrReceipt(${bh.borrowId})" title="View verification receipt">
                        🔍 Verify
                    </button>
                    ${bh.status === 'BORROWED' || bh.status === 'OVERDUE' ? `
                        <button class="btn btn-primary btn-sm" onclick="processReturnBook(${bh.borrowId}, ${bh.studentId}, ${bh.bookId})">Return</button>
                    ` : `<span style="color:#9CA3AF;font-size:0.8rem;">Returned</span>`}
                </div>
            </td>
        </tr>`;
    }).join('');
}

function renderStudentsTable() {
    const tbody = document.getElementById("studentsTableBody");
    if (!tbody) return;

    if (!state.students || state.students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:2rem;">No student accounts registered yet.</td></tr>`;
        return;
    }

    const filterVal = state.studentDeptFilter || 'ALL';
    const searchInput = document.getElementById("studentSearchInput");
    const searchVal = searchInput ? searchInput.value.trim().toLowerCase() : "";

    let filtered = filterVal === 'ALL'
        ? state.students
        : state.students.filter(s => s.department === filterVal);

    if (searchVal) {
        filtered = filtered.filter(s => 
            (s.fullName && s.fullName.toLowerCase().includes(searchVal)) ||
            (s.studentCode && s.studentCode.toLowerCase().includes(searchVal)) ||
            (s.email && s.email.toLowerCase().includes(searchVal))
        );
    }

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:2rem;">No students found matching the criteria.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(s => `
        <tr>
            <td>#${s.id}</td>
            <td><code>${escapeHtml(s.studentCode)}</code></td>
            <td>
                <strong onclick="openStudentDetailModal(${s.id})" style="color: #7C3AED; cursor: pointer;" onmouseover="this.style.textDecoration='underline'" onmouseout="this.style.textDecoration='none'">
                    ${escapeHtml(s.fullName)}
                </strong><br>
                <small style="color:#6B7280;">${escapeHtml(s.email)}</small>
            </td>
            <td>${escapeHtml(s.department)}</td>
            <td>Year ${s.yearOfStudy}</td>
            <td>${s.currentBorrowed} / ${s.maxBorrowLimit}</td>
            <td style="color:${s.totalFinesOwed > 0 ? '#EF4444' : '#10B981'};font-weight:600;">
                ₹${(s.totalFinesOwed || 0).toFixed(2)}
            </td>
            <td>
                <span class="status-tag ${s.status ? s.status.toLowerCase() : 'active'}">
                    ${escapeHtml(s.status || 'ACTIVE')}
                </span>
            </td>
            <td>
                <div style="display:flex; gap:0.25rem;">
                    <button class="btn btn-outline btn-sm" onclick="showStudentQrCard(${s.id})" title="View Digital ID QR">
                        📇 ID Card
                    </button>
                    ${isAdmin() ? `
                        <button class="btn btn-outline btn-sm" onclick="toggleStudentStatus(${s.id}, '${s.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'}')">
                            ${s.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                        </button>
                    ` : `<span style="color:#9CA3AF;font-size:0.8rem;">Read Only</span>`}
                </div>
            </td>
        </tr>
    `).join('');
}

function filterStudentsByDept() {
    const filterEl = document.getElementById("studentDeptFilter");
    if (filterEl) {
        state.studentDeptFilter = filterEl.value;
    }
    renderStudentsTable();
}

// Populate department dropdown for issue modal step 1
function populateIssueDeptSelect() {
    const deptSelect = document.getElementById("issueDeptSelect");
    if (!deptSelect) return;
    const depts = [...new Set(state.students.map(s => s.department).filter(Boolean))].sort();
    deptSelect.innerHTML = `<option value="">-- Choose Department --</option>` +
        depts.map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
}

// Called whenever dept changes – reset student selection
function onIssueDeptChange() {
    state.issueSelectedStudentId = null;
    document.getElementById("issueSelectedStudentBadge").classList.add("hidden");
    document.getElementById("issueStep2NextBtn").disabled = true;
    filterIssueStudents();
}

// Render filtered student list in step 2
function filterIssueStudents() {
    const dept = document.getElementById("issueDeptSelect")?.value || '';
    const search = (document.getElementById("issueStudentSearch")?.value || '').toLowerCase().trim();
    const list = document.getElementById("issueStudentList");
    if (!list) return;

    let filtered = state.students.filter(s => !dept || s.department === dept);
    if (search) {
        filtered = filtered.filter(s =>
            (s.fullName && s.fullName.toLowerCase().includes(search)) ||
            (s.studentCode && s.studentCode.toLowerCase().includes(search))
        );
    }

    if (filtered.length === 0) {
        list.innerHTML = `<div style="text-align:center;color:#64748B;padding:1rem;font-size:0.85rem;">No students found.</div>`;
        return;
    }

    list.innerHTML = filtered.map(s => `
        <div onclick="selectIssueStudent(${s.id})"
             id="issueStudentRow_${s.id}"
             style="padding:0.65rem 0.9rem; cursor:pointer; border-bottom:1px solid #F1F5F9;
                    display:flex; align-items:center; gap:0.6rem;
                    background:${state.issueSelectedStudentId === s.id ? '#EDE9FE' : 'white'};
                    transition:background 0.15s;"
             onmouseover="if(${state.issueSelectedStudentId} !== ${s.id}) this.style.background='#F5F3FF'"
             onmouseout="if(${state.issueSelectedStudentId} !== ${s.id}) this.style.background='white'">
            <div style="width:30px;height:30px;border-radius:50%;background:linear-gradient(135deg,#7C3AED,#6D28D9);
                        color:white;display:flex;align-items:center;justify-content:center;font-size:0.7rem;font-weight:700;flex-shrink:0;">
                ${escapeHtml(s.fullName.charAt(0))}
            </div>
            <div style="flex:1;min-width:0;">
                <div style="font-weight:600;color:#1E293B;font-size:0.85rem;">${escapeHtml(s.fullName)}</div>
                <div style="font-size:0.75rem;color:#64748B;font-family:monospace;">${escapeHtml(s.studentCode)}</div>
            </div>
            ${state.issueSelectedStudentId === s.id ? `<span style="color:#7C3AED;font-size:1rem;">✓</span>` : ''}
        </div>
    `).join('');
}

function selectIssueStudent(studentId) {
    state.issueSelectedStudentId = studentId;
    const student = state.students.find(s => s.id === studentId);
    if (!student) return;
    document.getElementById("issueSelectedStudentLabel").innerText = `${student.fullName} · ${student.studentCode}`;
    document.getElementById("issueSelectedStudentBadge").classList.remove("hidden");
    document.getElementById("issueStep2NextBtn").disabled = false;
    filterIssueStudents(); // re-render to show check mark
}

function clearIssueStudentSelection() {
    state.issueSelectedStudentId = null;
    document.getElementById("issueSelectedStudentBadge").classList.add("hidden");
    document.getElementById("issueStep2NextBtn").disabled = true;
    filterIssueStudents();
}

// Populate book dropdown in step 3
function populateIssueBookSelect() {
    const bookSelect = document.getElementById("issueBookSelect");
    if (!bookSelect) return;
    const availableBooks = state.books.filter(b => b.availableCopies > 0);
    bookSelect.innerHTML = availableBooks.map(b =>
        `<option value="${b.bookId}">${escapeHtml(b.title)} (${b.availableCopies} available)</option>`
    ).join('');
}

// Kept for backward-compat (issueToVerifiedStudent calls this)
function populateIssueSelects() {
    populateIssueDeptSelect();
    populateIssueBookSelect();
}

// ==================== TAB NAVIGATION ====================

function switchTab(tabName) {
    state.activeTab = tabName;

    // Update Nav Pills
    document.querySelectorAll(".nav-pill").forEach(btn => btn.classList.remove("active"));
    const activeBtn = document.getElementById(`tab${tabName.charAt(0).toUpperCase() + tabName.slice(1)}Btn`);
    if (activeBtn) activeBtn.classList.add("active");

    // Update Tab Views
    document.querySelectorAll(".tab-view").forEach(view => view.classList.add("hidden"));
    const activeView = document.getElementById(`view${tabName.charAt(0).toUpperCase() + tabName.slice(1)}`);
    if (activeView) activeView.classList.remove("hidden");

    // Refresh Tab Content
    if (tabName === 'dashboard') { fetchStats(); fetchOverdueData(); }
    if (tabName === 'catalog') fetchCatalogBooks();
    if (tabName === 'circulation') fetchBorrowHistory();
    if (tabName === 'students') fetchStudents();
    if (tabName === 'attendance') fetchAttendanceLogs();
    if (tabName === 'analytics') { renderAnalyticsCanvasChart(); fetchTopVisitors(); }
}

function setCatalogView(view) {
    state.catalogView = view;
    document.getElementById("viewGridBtn").classList.toggle("active", view === 'grid');
    document.getElementById("viewTableBtn").classList.toggle("active", view === 'table');
    document.getElementById("catalogGridContainer").classList.toggle("hidden", view !== 'grid');
    document.getElementById("catalogTableContainer").classList.toggle("hidden", view !== 'table');
}

function handleCatalogSearch() {
    fetchCatalogBooks();
}

// ==================== MODALS & FORM HANDLERS ====================

function openLoginModal() {
    document.getElementById("loginModal").classList.remove("hidden");
}

function closeLoginModal() {
    document.getElementById("loginModal").classList.add("hidden");
}

async function handleLoginSubmit(e) {
    e.preventDefault();
    const role = document.getElementById("loginRole").value;
    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value.trim();
    try {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ role, username, password })
        });
        const data = await res.json();
        if (data.success) {
            state.currentUser = data.user;
            localStorage.setItem("libraai_user", JSON.stringify(data.user));
            closeLoginModal();
            showToast(`Welcome back, ${data.user.fullName}! 🎉`, 'success');
            showDashboard();
        } else {
            showToast(data.message || 'Invalid credentials', 'error');
        }
    } catch (err) {
        showToast('Server error. Make sure backend is running.', 'error');
    }
}

function openRegisterModal() {
    if (!isAdmin()) {
        showToast('Only admin can register students. Please login as admin first.', 'error');
        return;
    }
    document.getElementById("registerModal").classList.remove("hidden");
}

function closeRegisterModal() {
    document.getElementById("registerModal").classList.add("hidden");
}

async function handleRegisterSubmit(e) {
    e.preventDefault();
    const studentCode = document.getElementById("regCode").value.trim();
    // Validate register number starts with 721424 and has 12 digits
    if (!/^721424\d{6}$/.test(studentCode)) {
        showToast('Register number must be a 12-digit number starting with 721424.', 'error');
        return;
    }
    const fullName = document.getElementById("regFullName").value.trim();
    const email = document.getElementById("regEmail").value.trim();
    const phone = document.getElementById("regPhone").value.trim();
    const department = document.getElementById("regDept").value.trim();
    const yearOfStudy = document.getElementById("regYear").value;
    const password = document.getElementById("regPassword").value;

    try {
        const res = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentCode, fullName, email, phone, department, yearOfStudy, password })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`✅ Student ${fullName} (Reg: ${studentCode}) registered successfully!`, 'success');
            closeRegisterModal();
            document.getElementById('registerForm').reset();
            fetchStudents();
            fetchStats();
        } else {
            showToast(data.message || 'Registration failed.', 'error');
        }
    } catch (err) {
        showToast('Registration error.', 'error');
    }
}


function updateUserUI() {
    const loggedInView = document.getElementById("loggedInView");
    const addBookHeaderBtn = document.getElementById("addBookHeaderBtn");
    const tabAttendanceBtn = document.getElementById("tabAttendanceBtn");
    const dashName = document.getElementById('dashWelcomeName');
    if (state.currentUser) {
        if (loggedInView) loggedInView.classList.remove("hidden");
        const name = state.currentUser.fullName || state.currentUser.username || 'Admin';
        const nameEl = document.getElementById("userFullName");
        const roleEl = document.getElementById("userRoleBadge");
        const avatarEl = document.getElementById("userAvatar");
        if (nameEl) nameEl.innerText = name;
        if (roleEl) roleEl.innerText = state.currentUser.role || 'STUDENT';
        if (avatarEl) avatarEl.innerText = name.charAt(0).toUpperCase();
        if (dashName) dashName.innerText = name.split(' ')[0];
        if (addBookHeaderBtn) addBookHeaderBtn.classList.toggle("hidden", !isAdmin());
        if (tabAttendanceBtn) tabAttendanceBtn.classList.toggle("hidden", !isAdmin());
    } else {
        if (loggedInView) loggedInView.classList.add("hidden");
        if (addBookHeaderBtn) addBookHeaderBtn.classList.add("hidden");
        if (tabAttendanceBtn) tabAttendanceBtn.classList.add("hidden");
    }
}

function logoutUser() {
    state.currentUser = null;
    localStorage.removeItem("libraai_user");
    showToast('Logged out. Goodbye! 👋', 'success');
    showLanding();
}

function isAdmin() {
    return state.currentUser && state.currentUser.role === 'ADMIN';
}

// Book Modal
function openAddBookModal() {
    document.getElementById("bookModalTitle").innerText = "➕ Add New Book";
    document.getElementById("bookForm").reset();
    document.getElementById("bookIdInput").value = "";
    document.getElementById("bookModal").classList.remove("hidden");
}

function editBook(bookId) {
    const book = state.books.find(b => b.bookId === bookId);
    if (!book) return;

    document.getElementById("bookModalTitle").innerText = "✏️ Edit Book Details";
    document.getElementById("bookIdInput").value = book.bookId;
    document.getElementById("bookTitleInput").value = book.title;
    document.getElementById("bookAuthorInput").value = book.author;
    document.getElementById("bookIsbnInput").value = book.isbn;
    document.getElementById("bookPublisherInput").value = book.publisher || '';
    document.getElementById("bookCategoryInput").value = book.categoryName || 'General';
    document.getElementById("bookCopiesInput").value = book.totalCopies;
    document.getElementById("bookAvailableInput").value = book.availableCopies;
    document.getElementById("bookShelfInput").value = book.shelfNumber || '';
    document.getElementById("bookLanguageInput").value = book.language || 'English';
    document.getElementById("bookDescInput").value = book.description || '';

    document.getElementById("bookModal").classList.remove("hidden");
}

function closeBookModal() {
    document.getElementById("bookModal").classList.add("hidden");
}

async function handleBookSubmit(e) {
    e.preventDefault();
    const bookId = document.getElementById("bookIdInput").value;
    const title = document.getElementById("bookTitleInput").value.trim();
    const author = document.getElementById("bookAuthorInput").value.trim();
    const isbn = document.getElementById("bookIsbnInput").value.trim();
    const publisher = document.getElementById("bookPublisherInput").value.trim();
    const categoryName = document.getElementById("bookCategoryInput").value.trim();
    const totalCopies = document.getElementById("bookCopiesInput").value;
    const availableCopies = document.getElementById("bookAvailableInput").value;
    const shelfNumber = document.getElementById("bookShelfInput").value.trim();
    const language = document.getElementById("bookLanguageInput").value.trim();
    const description = document.getElementById("bookDescInput").value.trim();

    const payload = { bookId, title, author, isbn, publisher, categoryName, totalCopies, availableCopies, shelfNumber, language, description };

    try {
        const res = await fetch(`${API_BASE}/api/books`, {
            method: bookId ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Book saved successfully!', 'success');
            closeBookModal();
            fetchCatalogBooks();
            fetchStats();
        } else {
            showToast(data.message || 'Error saving book.', 'error');
        }
    } catch (err) {
        showToast('Save book failed.', 'error');
    }
}

async function deleteBook(bookId) {
    if (!confirm("Are you sure you want to delete this book from the catalog?")) return;
    try {
        const res = await fetch(`${API_BASE}/api/books?id=${bookId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast('Book deleted successfully', 'success');
            fetchCatalogBooks();
            fetchStats();
        } else {
            showToast(data.message || 'Failed to delete book', 'error');
        }
    } catch (err) {
        showToast('Error deleting book', 'error');
    }
}

// ==================== ISSUE BOOK MODAL (3-STEP FLOW) ====================

function issueGoStep(step) {
    [1, 2, 3].forEach(n => {
        const el = document.getElementById(`issueStep${n}`);
        if (el) el.classList.toggle('hidden', n !== step);
        const dot = document.getElementById(`issueStep${n}Dot`);
        if (dot) {
            dot.style.background = n === step ? '#7C3AED' : (n < step ? '#10B981' : '#E2E8F0');
            dot.style.color = n <= step ? 'white' : '#64748B';
            dot.innerText = n < step ? '✓' : String(n);
        }
    });
    const line1 = document.getElementById('issueStepLine1');
    const line2 = document.getElementById('issueStepLine2');
    if (line1) line1.style.background = step > 1 ? '#10B981' : '#E2E8F0';
    if (line2) line2.style.background = step > 2 ? '#10B981' : '#E2E8F0';
}

function issueGoStep2() {
    const dept = document.getElementById("issueDeptSelect")?.value;
    if (!dept) { showToast('Please select a department first.', 'error'); return; }
    document.getElementById("issueStudentSearch").value = '';
    filterIssueStudents();
    issueGoStep(2);
}

function issueGoStep3() {
    if (!state.issueSelectedStudentId) { showToast('Please select a student first.', 'error'); return; }
    const student = state.students.find(s => s.id === state.issueSelectedStudentId);
    if (!student) return;
    document.getElementById("issueStep3StudentName").innerText = student.fullName;
    document.getElementById("issueStep3StudentReg").innerText = `Reg: ${student.studentCode} · ${student.department}`;

    const bookInfoCard = document.getElementById("issueStep3BookInfo");
    const book = state.books.find(b => b.bookId == state.issuePendingBookId);

    if (book) {
        // Book was pre-selected from catalog — show read-only card
        if (bookInfoCard) bookInfoCard.style.display = 'flex';
        document.getElementById("issueStep3BookTitle").innerText = book.title;
        document.getElementById("issueStep3BookMeta").innerText = `${book.author || 'Unknown Author'} · ${book.availableCopies} cop${book.availableCopies === 1 ? 'y' : 'ies'} available`;
        // Remove any temporary dropdown if it exists
        const tempDropdown = document.getElementById("issueFallbackBookRow");
        if (tempDropdown) tempDropdown.remove();
    } else {
        // No book pre-selected (e.g. from QR verify modal) — inject a book select dropdown
        if (bookInfoCard) bookInfoCard.style.display = 'none';
        let tempRow = document.getElementById("issueFallbackBookRow");
        if (!tempRow) {
            tempRow = document.createElement('div');
            tempRow.id = "issueFallbackBookRow";
            tempRow.className = "form-group";
            tempRow.style.marginBottom = "1rem";
            tempRow.innerHTML = `<label>Select Book</label><select id="issueFallbackBookSelect" class="form-input"></select>`;
            bookInfoCard.insertAdjacentElement('afterend', tempRow);
        }
        const availableBooks = state.books.filter(b => b.availableCopies > 0);
        document.getElementById("issueFallbackBookSelect").innerHTML = availableBooks.map(b =>
            `<option value="${b.bookId}">${escapeHtml(b.title)} (${b.availableCopies} available)</option>`
        ).join('');
    }
    issueGoStep(3);
}

function openIssueModal(bookId = null) {
    state.issueSelectedStudentId = null;
    state.issuePendingBookId = bookId;
    populateIssueDeptSelect();
    filterIssueStudents();
    // Reset selections
    const badge = document.getElementById("issueSelectedStudentBadge");
    if (badge) badge.classList.add("hidden");
    const nextBtn = document.getElementById("issueStep2NextBtn");
    if (nextBtn) nextBtn.disabled = true;
    issueGoStep(1);
    // Pre-select book if passed
    if (bookId) {
        state.issuePendingBookId = bookId;
    }
    document.getElementById("issueModal").classList.remove("hidden");
}

function closeIssueModal() {
    document.getElementById("issueModal").classList.add("hidden");
    state.issueSelectedStudentId = null;
    state.issuePendingBookId = null;
}

// Called when "Confirm & Issue" is clicked: close the form modal, open QR scanner in ISSUE_VERIFY mode
function handleIssueConfirmAndScan() {
    if (!state.issueSelectedStudentId) { showToast('No student selected.', 'error'); return; }
    // If no book was pre-selected (came from Verify modal), read from fallback dropdown
    if (!state.issuePendingBookId) {
        const fallback = document.getElementById("issueFallbackBookSelect");
        if (!fallback || !fallback.value) { showToast('Please select a book.', 'error'); return; }
        state.issuePendingBookId = fallback.value;
    }
    const loanDays = document.getElementById("issueLoanDays")?.value || 14;
    state.issuePendingLoanDays = loanDays;
    // Close issue modal and open scanner in ISSUE_VERIFY mode
    document.getElementById("issueModal").classList.add("hidden");
    scannerMode = 'ISSUE_VERIFY';
    const header = document.querySelector("#scannerModal h3");
    if (header) header.innerText = "🔒 Scan Student ID Card to Verify & Issue";
    const statusHint = document.getElementById("scannerStatus");
    if (statusHint) statusHint.innerText = "Scan the QR code on the student's LibraAI ID Card...";
    openScanner();
}

async function executeBookIssue(studentId, bookId, loanDays) {
    try {
        const res = await fetch(`${API_BASE}/api/borrow/issue`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId, bookId, loanDays })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || '✅ Book issued successfully!', 'success');
            fetchCatalogBooks();
            fetchStudents();
            fetchStats();
            fetchBorrowHistory().then(() => {
                const history = state.borrowHistory
                    .filter(bh => bh.studentId == studentId)
                    .sort((a, b) => b.borrowId - a.borrowId);
                if (history.length > 0) {
                    showBorrowQrReceipt(history[0].borrowId);
                }
            });
        } else {
            showToast(data.message || '❌ Issue failed.', 'error');
        }
    } catch (err) {
        showToast('❌ Network error. Book could not be issued.', 'error');
    }
}

// Legacy handleIssueSubmit kept for backward compat (not used by new flow)
async function handleIssueSubmit(e) {
    if (e) e.preventDefault();
    await executeBookIssue(
        state.issueSelectedStudentId,
        document.getElementById("issueBookSelect")?.value,
        document.getElementById("issueLoanDays")?.value
    );
    closeIssueModal();
}

async function processReturnBook(borrowId, studentId, bookId) {
    if (!confirm("Confirm returning this book to the library?")) return;
    try {
        const res = await fetch(`${API_BASE}/api/borrow/return`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ borrowId, studentId, bookId })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Book returned successfully!', 'success');
            fetchBorrowHistory();
            fetchCatalogBooks();
            fetchStudents();
            fetchStats();
        } else {
            showToast(data.message || 'Return failed', 'error');
        }
    } catch (err) {
        showToast('Return book error', 'error');
    }
}

async function toggleStudentStatus(studentId, newStatus) {
    try {
        const res = await fetch(`${API_BASE}/api/students/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId, status: newStatus })
        });
        const data = await res.json();
        if (data.success) {
            showToast(`Student status changed to ${newStatus}`, 'success');
            fetchStudents();
        } else {
            showToast(data.message || 'Failed to update status', 'error');
        }
    } catch (err) {
        showToast('Error updating student status', 'error');
    }
}

// Book Detail Modal
function openDetailModal(bookId) {
    const book = state.books.find(b => b.bookId === bookId);
    if (!book) return;

    state.selectedDetailBook = book;
    document.getElementById("detailBookTitle").innerText = book.title;
    document.getElementById("detailTitleText").innerText = book.title;
    document.getElementById("detailAuthorText").innerText = `by ${book.author}`;
    document.getElementById("detailCategoryChip").innerText = book.categoryName || 'General';
    document.getElementById("detailIsbnChip").innerText = `ISBN: ${book.isbn || 'N/A'}`;
    document.getElementById("detailLangChip").innerText = book.language || 'English';
    document.getElementById("detailDescText").innerText = book.description || 'No description available for this title.';
    document.getElementById("detailLocation").innerText = book.shelfNumber || 'Shelf A-1';
    document.getElementById("detailTotalCopies").innerText = book.totalCopies;
    document.getElementById("detailAvailableCopies").innerText = book.availableCopies;
    document.getElementById("detailPublisher").innerText = book.publisher || 'N/A';

    const detailCoverIcon = document.getElementById("detailCoverIcon");
    if (detailCoverIcon) {
        detailCoverIcon.innerHTML = renderBookCoverHTML(book);
        detailCoverIcon.style.padding = '0';
        detailCoverIcon.style.background = 'none';
        detailCoverIcon.style.display = 'block';
    }

    const stockBadge = document.getElementById("detailStockBadge");
    if (book.availableCopies > 0) {
        stockBadge.innerText = `${book.availableCopies} Available`;
        stockBadge.style.background = '#ECFDF5';
        stockBadge.style.color = '#10B981';
        document.getElementById("detailIssueBtn") && (document.getElementById("detailIssueBtn").style.display = 'inline-block');
    } else {
        stockBadge.innerText = 'Out of Stock';
        stockBadge.style.background = '#FEF2F2';
        stockBadge.style.color = '#EF4444';
        document.getElementById("detailIssueBtn") && (document.getElementById("detailIssueBtn").style.display = 'none');
    }

    document.getElementById("detailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("detailModal").classList.add("hidden");
}

function openStudentDetailModal(studentId) {
    const student = state.students.find(s => s.id === studentId);
    if (!student) return;

    document.getElementById("sdFullName").innerText = student.fullName;
    document.getElementById("sdRegCode").innerText = student.studentCode;
    document.getElementById("sdDept").innerText = student.department;
    document.getElementById("sdYear").innerText = `Year ${student.yearOfStudy}`;
    document.getElementById("sdPhone").innerText = student.phone || 'N/A';
    
    const statusEl = document.getElementById("sdStatus");
    if (statusEl) {
        statusEl.innerText = student.status || 'ACTIVE';
        statusEl.className = 'status-tag ' + (student.status || 'ACTIVE').toLowerCase();
    }

    document.getElementById("sdLoginEmail").innerText = student.email;
    document.getElementById("sdLoginCode").innerText = student.studentCode;
    document.getElementById("sdPasswordHash").innerText = student.passwordHash || 'Hashed & Secured';
    document.getElementById("sdBorrowLimits").innerText = `Borrowed: ${student.currentBorrowed}/${student.maxBorrowLimit}`;

    // Filter borrow history for this student
    const history = state.borrowHistory.filter(bh => bh.studentId === studentId);
    const tbody = document.getElementById("sdBorrowHistoryTableBody");
    if (tbody) {
        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#64748B;padding:1.5rem;">No library borrow history found.</td></tr>`;
        } else {
            tbody.innerHTML = history.map(bh => {
                const borrowDate = formatDate(bh.borrowDate).split(' ')[0];
                const dueDate = formatDate(bh.dueDate).split(' ')[0];
                const returnDate = bh.returnDate ? formatDate(bh.returnDate).split(' ')[0] : '<span style="color:#9CA3AF;">Active</span>';
                const statusClass = bh.status ? bh.status.toLowerCase() : 'borrowed';
                
                return `
                    <tr>
                        <td style="font-weight:600;color:#1E293B;">${escapeHtml(bh.bookTitle)}</td>
                        <td>${borrowDate}</td>
                        <td>${dueDate}</td>
                        <td>${returnDate}</td>
                        <td><span class="status-tag ${statusClass}" style="font-size:0.7rem;padding:2px 6px;">${escapeHtml(bh.status || 'ACTIVE')}</span></td>
                        <td style="font-weight:600;color:${bh.fineAmount > 0 ? '#EF4444' : '#10B981'};">₹${(bh.fineAmount || 0).toFixed(2)}</td>
                    </tr>
                `;
            }).join('');
        }
    }

    document.getElementById("studentDetailModal").classList.remove("hidden");
}

function closeStudentDetailModal() {
    document.getElementById("studentDetailModal").classList.add("hidden");
}

function issueCurrentDetailBook() {
    if (!state.selectedDetailBook) return;
    closeDetailModal();
    openIssueModal();
}

// ==================== CANVAS CHART RENDERER ====================

function renderAnalyticsCanvasChart() {
    const canvas = document.getElementById("webAnalyticsCanvas");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const width = canvas.width;
    const height = canvas.height;
    ctx.clearRect(0, 0, width, height);

    // Compute category frequency from loaded books
    const catMap = {};
    state.books.forEach(b => {
        const cat = b.categoryName || "General";
        catMap[cat] = (catMap[cat] || 0) + (b.totalCopies || 1);
    });

    const categories = Object.keys(catMap);
    const data = categories.length > 0 ? categories.map(c => ({
        label: c,
        value: catMap[c]
    })) : [
        { label: "Computer Sci", value: 35 },
        { label: "Data Science", value: 25 },
        { label: "Physics", value: 18 },
        { label: "Mathematics", value: 22 },
        { label: "Literature", value: 12 }
    ];

    const colors = ["#F97316", "#3B82F6", "#10B981", "#6366F1", "#F59E0B", "#EC4899"];
    const startX = 70;
    const startY = height - 50;
    const chartH = height - 100;
    const maxVal = Math.max(...data.map(d => d.value), 10);
    const barW = Math.min(55, Math.floor((width - startX - 40) / data.length) - 20);
    const gap = 25;

    // Draw X Axis Line
    ctx.beginPath();
    ctx.strokeStyle = "rgba(255, 255, 255, 0.15)";
    ctx.lineWidth = 2;
    ctx.moveTo(startX - 15, startY);
    ctx.lineTo(startX + (data.length * (barW + gap)), startY);
    ctx.stroke();

    // Render Bars & Labels
    data.forEach((item, idx) => {
        const x = startX + idx * (barW + gap);
        const barH = (item.value / maxVal) * chartH;
        const y = startY - barH;
        const color = colors[idx % colors.length];

        // Draw Bar
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.roundRect(x, y, barW, barH, [8, 8, 0, 0]);
        ctx.fill();

        // Draw Value Number
        ctx.fillStyle = "#F8FAFC";
        ctx.font = "bold 13px Outfit";
        ctx.textAlign = "center";
        ctx.fillText(item.value, x + barW / 2, y - 8);

        // Draw Category Label
        ctx.fillStyle = "#94A3B8";
        ctx.font = "12px Inter";
        ctx.fillText(item.label.substring(0, 10), x + barW / 2, startY + 22);
    });

}

async function fetchTopVisitors() {
    try {
        const res = await fetch(`${API_BASE}/api/analytics/top-visitors`);
        if (!res.ok) throw new Error("API error");
        const data = await res.json();
        
        const tbody = document.getElementById("topVisitorsTableBody");
        if (!tbody) return;
        
        if (!data || data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#64748B; padding:2rem;">No attendance logs or session times recorded yet.</td></tr>`;
            return;
        }

        const maxSeconds = Math.max(...data.map(v => v.totalSeconds), 1);

        tbody.innerHTML = data.map((v, idx) => {
            const pct = (v.totalSeconds / maxSeconds) * 100;
            
            // Format duration
            const totalSecs = v.totalSeconds;
            const hrs = Math.floor(totalSecs / 3600);
            const mins = Math.floor((totalSecs % 3600) / 60);
            const secs = totalSecs % 60;
            
            let durationStr = "";
            if (hrs > 0) durationStr += `${hrs}h `;
            if (mins > 0 || hrs > 0) durationStr += `${mins}m `;
            durationStr += `${secs}s`;

            // Medal or rank badge
            let rankBadge = `<span style="font-weight:700; color:#4B5563;">#${idx + 1}</span>`;
            if (idx === 0) rankBadge = `🥇 <span style="font-weight:700; color:#D97706;">1st</span>`;
            else if (idx === 1) rankBadge = `🥈 <span style="font-weight:700; color:#4B5563;">2nd</span>`;
            else if (idx === 2) rankBadge = `🥉 <span style="font-weight:700; color:#B45309;">3rd</span>`;

            return `
                <tr>
                    <td style="font-family:'Outfit'; font-weight:600;">${rankBadge}</td>
                    <td>
                        <strong>${escapeHtml(v.fullName)}</strong><br>
                        <small style="color:#6B7280; font-family:monospace;">Reg: ${escapeHtml(v.studentCode)}</small>
                    </td>
                    <td><span class="chip">${escapeHtml(v.department)}</span></td>
                    <td style="font-weight:700; color:#1E293B;">${durationStr}</td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.75rem; width: 100%;">
                            <div style="flex: 1; background: #E2E8F0; border-radius: 9999px; height: 8px; overflow: hidden; position: relative;">
                                <div style="width: ${pct}%; background: linear-gradient(90deg, #7C3AED, #EC4899); height: 100%; border-radius: 9999px; transition: width 0.6s ease;"></div>
                            </div>
                            <span style="font-size: 0.78rem; font-weight: 600; color: #4B5563; min-width: 35px; text-align: right;">${pct.toFixed(0)}%</span>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error("Error fetching top visitors:", err);
        const tbody = document.getElementById("topVisitorsTableBody");
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#EF4444; padding:2rem;">Failed to load visitor leaderboard.</td></tr>`;
        }
    }
}

// ==================== TOAST & UTILS ====================

function showToast(message, type = 'info') {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span>
        <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function formatDate(tsStr) {
    if (!tsStr) return '-';
    try {
        const d = new Date(tsStr);
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) + ' ' + d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    } catch (e) {
        return tsStr;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ==================== QR CODE GENERATOR & VERIFICATION ====================

function generateQRUrl(data) {
    try {
        const qr = qrcode(0, 'M');
        qr.addData(data);
        qr.make();
        return qr.createDataURL(4, 10);
    } catch (e) {
        console.error("Local QR generation failed, falling back to external server:", e);
        return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&color=7C3AED&data=${encodeURIComponent(data)}`;
    }
}

function showStudentQrCard(studentId) {
    const student = state.students.find(s => s.id == studentId);
    if (!student) return;

    document.getElementById("qrModalTitle").innerText = "Digital ID Card";
    
    document.getElementById("qrIdName").innerText = student.fullName;
    document.getElementById("qrIdCode").innerText = `Reg: ${student.studentCode}`;
    document.getElementById("qrIdDept").innerText = `Dept: ${student.department}`;
    document.getElementById("qrIdYear").innerText = `Year: ${student.yearOfStudy}`;
    
    const qrData = `libraai:student:${student.studentCode}:${student.fullName}:${student.department}`;
    document.getElementById("qrIdCardImage").src = generateQRUrl(qrData);

    document.getElementById("qrIdCardView").classList.remove("hidden");
    document.getElementById("qrReceiptView").classList.add("hidden");
    
    document.getElementById("qrModal").classList.remove("hidden");
}

function showBorrowQrReceipt(borrowId) {
    const bh = state.borrowHistory.find(b => b.borrowId == borrowId);
    if (!bh) return;

    document.getElementById("qrModalTitle").innerText = "Security Verification Receipt";
    
    document.getElementById("qrReceiptBookTitle").innerText = bh.bookTitle;
    document.getElementById("qrReceiptStudent").innerText = `Issued to: ${bh.studentName} (${bh.studentCode})`;
    
    const statusEl = document.getElementById("qrReceiptStatus");
    if (statusEl) {
        statusEl.innerText = bh.status || 'ACTIVE';
        statusEl.className = '';
        statusEl.classList.add("status-tag");
        statusEl.classList.add(bh.status ? bh.status.toLowerCase() : 'borrowed');
    }

    document.getElementById("qrReceiptDetails").innerHTML = `
        <div><strong>Transaction ID:</strong> #${bh.borrowId}</div>
        <div><strong>ISBN:</strong> ${bh.isbn || 'N/A'}</div>
        <div><strong>Borrowed On:</strong> ${formatDate(bh.borrowDate)}</div>
        <div><strong>Due On:</strong> ${formatDate(bh.dueDate)}</div>
        ${bh.returnDate ? `<div><strong>Returned On:</strong> ${formatDate(bh.returnDate)}</div>` : ''}
    `;

    const qrData = `libraai:borrow:${bh.borrowId}:${bh.studentCode}:${bh.bookId}:${bh.borrowDate}`;
    document.getElementById("qrReceiptImage").src = generateQRUrl(qrData);

    document.getElementById("qrIdCardView").classList.add("hidden");
    document.getElementById("qrReceiptView").classList.remove("hidden");
    
    document.getElementById("qrModal").classList.remove("hidden");
}

function closeQrModal() {
    document.getElementById("qrModal").classList.add("hidden");
}

function printQrContent() {
    window.print();
}

let scannerStream = null;
let scannerAnimationId = null;
let scannerMode = 'VERIFY'; // 'VERIFY' or 'ATTENDANCE'
let lastScannedCode = "";
let lastScannedTime = 0;

function openScanner() {
    if (scannerMode !== 'ATTENDANCE') {
        scannerMode = 'VERIFY';
        const header = document.querySelector("#scannerModal h3");
        if (header) header.innerText = "📷 ID Verification Scanner";
    }
    const modal = document.getElementById("scannerModal");
    const video = document.getElementById("scannerVideo");
    const status = document.getElementById("scannerStatus");
    if (!modal || !video) return;

    modal.classList.remove("hidden");
    status.innerText = "Requesting camera access...";
    status.style.color = "#64748B";

    navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } })
        .then(stream => {
            scannerStream = stream;
            video.srcObject = stream;
            video.setAttribute("playsinline", true);
            video.play();
            status.innerText = scannerMode === 'ATTENDANCE' ? "🎥 Scanning for Attendance (Entry/Exit)..." : "🎥 Scanning for student QR Code...";
            status.style.color = "#7C3AED";
            scannerAnimationId = requestAnimationFrame(scanTick);
        })
        .catch(err => {
            console.error("Camera access failed:", err);
            status.innerText = "❌ Camera access denied or not available.";
            status.style.color = "#EF4444";
        });
}

function closeScanner() {
    const modal = document.getElementById("scannerModal");
    if (modal) modal.classList.add("hidden");

    if (scannerStream) {
        scannerStream.getTracks().forEach(track => track.stop());
        scannerStream = null;
    }
    if (scannerAnimationId) {
        cancelAnimationFrame(scannerAnimationId);
        scannerAnimationId = null;
    }
}

function scanTick() {
    const video = document.getElementById("scannerVideo");
    const status = document.getElementById("scannerStatus");
    
    if (video && (video.readyState === video.HAVE_ENOUGH_DATA || video.videoWidth > 0)) {
        let canvas = document.getElementById("offscreenScannerCanvas");
        if (!canvas) {
            canvas = document.createElement("canvas");
            canvas.id = "offscreenScannerCanvas";
        }
        const ctx = canvas.getContext("2d");
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        
        try {
            const code = jsQR(imageData.data, imageData.width, imageData.height, {
                inversionAttempts: "dontInvert",
            });
            
            if (code) {
                console.log("Found QR code:", code.data);
                handleScannedQR(code.data);
                return;
            }
        } catch (e) {
            console.error("Error decoding QR:", e);
        }
    }
    
    if (scannerStream) {
        scannerAnimationId = requestAnimationFrame(scanTick);
    }
}

function handleScannedQR(data) {
    if (scannerMode === 'ATTENDANCE') {
        const now = Date.now();
        if (data === lastScannedCode && (now - lastScannedTime) < 3000) {
            // Debounce scanning the same code too quickly
            if (scannerStream) {
                scannerAnimationId = requestAnimationFrame(scanTick);
            }
            return;
        }
        
        lastScannedCode = data;
        lastScannedTime = now;
        
        playScannerBeep(880, 0.1);

        fetch(`${API_BASE}/api/attendance/scan`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ qrData: data })
        })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                playScannerBeep(1000, 0.08);
                setTimeout(() => playScannerBeep(1200, 0.08), 100);

                if (res.action === 'CHECK_IN') {
                    showToast(`✅ Entered: ${res.studentName} (${res.department})`, "success");
                    // Update scanner status to remind about 15-sec rule
                    const statusEl = document.getElementById("scannerStatus");
                    if (statusEl) {
                        statusEl.innerText = `✅ ${res.studentName} checked IN. Must stay ≥ 15 seconds before leaving.`;
                        statusEl.style.color = "#10B981";
                    }
                } else {
                    showToast(`👋 Left: ${res.studentName} (${res.department})`, "info");
                }
                
                fetchAttendanceLogs();
            } else if (res.remainingSeconds) {
                // 15-second minimum stay not met
                playErrorBeep();
                showToast(`⏱️ Too soon! Wait ${res.remainingSeconds} more second${res.remainingSeconds === 1 ? '' : 's'} before leaving.`, "warning");
                // Update scanner status
                const statusEl = document.getElementById("scannerStatus");
                if (statusEl) {
                    statusEl.innerText = `⏱️ Please wait ${res.remainingSeconds}s more before scanning to leave.`;
                    statusEl.style.color = "#F59E0B";
                }
            } else {
                playErrorBeep();
                showToast(`❌ Scan Error: ${res.message || 'Verification failed'}`, "error");
            }
        })
        .catch(err => {
            playErrorBeep();
            showToast("❌ Network error during attendance scanning.", "error");
        });

        // Continuous scanning: resume scan loop after 1.5 seconds delay
        setTimeout(() => {
            if (scannerStream && scannerMode === 'ATTENDANCE') {
                scannerAnimationId = requestAnimationFrame(scanTick);
            }
        }, 1500);

    } else if (scannerMode === 'ISSUE_VERIFY') {
        // ── QR scan to verify student before issuing a book ──
        closeScanner();

        if (!data.startsWith("libraai:student:")) {
            showToast("❌ Invalid QR. Please scan a LibraAI Student ID Card.", "error");
            return;
        }
        const parts = data.split(":");
        if (parts.length < 5) {
            showToast("❌ Malformed QR code data.", "error");
            return;
        }
        const scannedCode = parts[2];
        const scannedName = parts[3];
        const scannedDept = parts[4];

        // Find the student whose QR was scanned
        const scannedStudent = state.students.find(s => s.studentCode === scannedCode);
        if (!scannedStudent) {
            showToast(`❌ Student Reg No ${scannedCode} not found.`, "error");
            return;
        }
        // Validate against the student selected in the form
        if (scannedStudent.id !== state.issueSelectedStudentId) {
            const expected = state.students.find(s => s.id === state.issueSelectedStudentId);
            playErrorBeep();
            showToast(`❌ ID mismatch! Scanned: ${scannedStudent.fullName}, Expected: ${expected ? expected.fullName : '?'}`, "error");
            return;
        }
        // Double-check QR payload integrity
        if (scannedStudent.fullName.toLowerCase().trim() !== scannedName.toLowerCase().trim()) {
            playErrorBeep();
            showToast("❌ QR data integrity check failed. Verification denied.", "error");
            return;
        }

        playScannerBeep(880, 0.12);
        setTimeout(() => playScannerBeep(1100, 0.08), 120);
        showToast(`✅ ID Verified: ${scannedStudent.fullName}. Issuing book...`, "success");
        // Issue the book
        executeBookIssue(
            state.issueSelectedStudentId,
            state.issuePendingBookId,
            state.issuePendingLoanDays || 14
        );
        state.issueSelectedStudentId = null;
        state.issuePendingBookId = null;
        state.issuePendingLoanDays = null;

    } else {
        closeScanner();
        
        if (!data.startsWith("libraai:student:")) {
            showToast("Invalid QR code format. Not a LibraAI ID Card.", "error");
            return;
        }
        
        const parts = data.split(":");
        if (parts.length < 5) {
            showToast("Malformed QR code data.", "error");
            return;
        }
        
        const studentCode = parts[2];
        const fullName = parts[3];
        const department = parts[4];
        
        const student = state.students.find(s => s.studentCode === studentCode);
        if (!student) {
            showToast(`Student with Reg No ${studentCode} not found in database.`, "error");
            return;
        }
        
        if (student.fullName.toLowerCase().trim() !== fullName.toLowerCase().trim() || 
            student.department.toLowerCase().trim() !== department.toLowerCase().trim()) {
            showToast("QR details mismatch with server record! Verification failed.", "error");
            return;
        }
        
        playScannerBeep(880, 0.15);
        
        const activeBorrows = state.borrowHistory.filter(bh => bh.studentCode === studentCode && (bh.status === 'BORROWED' || bh.status === 'OVERDUE'));
        
        document.getElementById("verifyName").innerText = student.fullName;
        document.getElementById("verifyReg").innerText = `Reg: ${student.studentCode}`;
        document.getElementById("verifyDept").innerText = `Dept: ${student.department}`;
        document.getElementById("verifyYear").innerText = `Year: ${student.yearOfStudy}`;
        
        const listContainer = document.getElementById("verifyBorrowList");
        if (activeBorrows.length === 0) {
            listContainer.innerHTML = `<div style="text-align:center; color:#64748B; padding:0.5rem; font-size:0.85rem;">
                No books currently borrowed.
            </div>`;
        } else {
            listContainer.innerHTML = activeBorrows.map(b => `
                <div style="padding:0.5rem 0; border-bottom:1px solid #E2E8F0; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <div style="font-weight:600; font-size:0.85rem; color:#1E293B;">${escapeHtml(b.bookTitle)}</div>
                        <div style="font-size:0.75rem; color:#64748B;">Due: ${formatDate(b.dueDate).split(' ')[0]}</div>
                    </div>
                    <span class="status-tag ${b.status.toLowerCase()}" style="font-size:0.7rem; padding:2px 6px;">
                        ${escapeHtml(b.status)}
                    </span>
                </div>
            `).join('');
        }
        
        document.getElementById("verifyIssueBtn").setAttribute("data-student-id", student.id);
        document.getElementById("verificationResultModal").classList.remove("hidden");
        showToast(`Verification successful for ${student.fullName}!`, "success");
    }
}

function closeVerificationResult() {
    document.getElementById("verificationResultModal").classList.add("hidden");
}

function issueToVerifiedStudent() {
    const studentId = parseInt(document.getElementById("verifyIssueBtn").getAttribute("data-student-id"));
    const student = state.students.find(s => s.id === studentId);
    closeVerificationResult();
    if (!student) { openIssueModal(); return; }
    // Pre-select the already-verified student and go straight to step 3
    state.issueSelectedStudentId = studentId;
    state.issuePendingBookId = null;
    populateIssueDeptSelect();
    // Set the dept select to match
    const deptSelect = document.getElementById("issueDeptSelect");
    if (deptSelect) deptSelect.value = student.department;
    document.getElementById("issueModal").classList.remove("hidden");
    // Show selected student badge
    const badge = document.getElementById("issueSelectedStudentBadge");
    const label = document.getElementById("issueSelectedStudentLabel");
    if (badge && label) {
        label.innerText = `${student.fullName} · ${student.studentCode}`;
        badge.classList.remove("hidden");
    }
    const nextBtn = document.getElementById("issueStep2NextBtn");
    if (nextBtn) nextBtn.disabled = false;
    issueGoStep3();
}

// ==================== ATTENDANCE MANAGEMENT SYSTEM ====================

function openAttendanceScanner() {
    scannerMode = 'ATTENDANCE';
    lastScannedCode = "";
    lastScannedTime = 0;
    const header = document.querySelector("#scannerModal h3");
    if (header) header.innerText = "📷 Entry/Exit Attendance Scanner";
    openScanner();
}

async function fetchAttendanceLogs() {
    try {
        const res = await fetch(`${API_BASE}/api/attendance/logs`);
        if (!res.ok) throw new Error("API error fetching logs");
        const data = await res.json();
        if (data.success) {
            state.attendanceLogs = data.logs || [];
            state.attendanceCurrentlyInside = data.currentlyInside || 0;
            state.attendanceTodayVisits = data.totalVisitsToday || 0;
            
            updateAttendanceStatsAndLogs(state.attendanceCurrentlyInside, state.attendanceTodayVisits);
        }
    } catch (err) {
        console.error("Error fetching attendance logs:", err);
        showToast("Error loading attendance history.", "error");
    }
}

function updateAttendanceStatsAndLogs(inside, visits) {
    const insideEl = document.getElementById("attendanceCurrentlyInsideCount");
    const visitsEl = document.getElementById("attendanceTodayVisitsCount");
    if (insideEl) insideEl.innerText = inside;
    if (visitsEl) visitsEl.innerText = visits;
    
    // Also re-render list of logs
    renderAttendanceTable();
}

function renderAttendanceTable() {
    const tbody = document.getElementById("attendanceTableBody");
    if (!tbody) return;

    const query = (document.getElementById("attendanceSearchInput")?.value || "").toLowerCase().trim();
    const deptFilter = document.getElementById("attendanceDeptFilter")?.value || "ALL";

    const filtered = state.attendanceLogs.filter(log => {
        const matchesQuery = !query || 
            (log.studentName && log.studentName.toLowerCase().includes(query)) ||
            (log.studentCode && log.studentCode.toLowerCase().includes(query));
        const matchesDept = deptFilter === "ALL" || log.department === deptFilter;
        return matchesQuery && matchesDept;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--text-light);padding:2rem;">No logs found. Open the Entry/Exit scanner to log attendance.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map((log, idx) => {
        const checkIn = formatDateWithSeconds(log.checkInTime);
        const checkOut = log.checkOutTime ? formatDateWithSeconds(log.checkOutTime) : '<span style="color:#9CA3AF;">Still Inside</span>';
        const duration = formatDuration(log.checkInTime, log.checkOutTime);
        const statusClass = log.status === 'IN' ? 'status-tag borrowed' : 'status-tag returned'; // Reuse style tag colors
        const statusLabel = log.status === 'IN' ? '🟢 INSIDE' : '👋 LEFT';

        return `
            <tr>
                <td>${idx + 1}</td>
                <td style="font-family:monospace;font-weight:600;">${escapeHtml(log.studentCode)}</td>
                <td><strong>${escapeHtml(log.studentName)}</strong></td>
                <td><span class="chip">${escapeHtml(log.department)}</span></td>
                <td>${checkIn}</td>
                <td>${checkOut}</td>
                <td style="font-weight:600;color:var(--text);">${duration}</td>
                <td><span class="${statusClass}" style="font-size:0.75rem;padding:2px 8px;font-weight:700;">${statusLabel}</span></td>
            </tr>
        `;
    }).join('');
}

function filterAttendanceLogs() {
    renderAttendanceTable();
}

function formatDateWithSeconds(tsStr) {
    if (!tsStr) return '-';
    try {
        const d = new Date(tsStr.replace(" ", "T"));
        if (isNaN(d.getTime())) return tsStr;
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' + d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    } catch (e) {
        return tsStr;
    }
}

function formatDuration(checkInStr, checkOutStr) {
    if (!checkOutStr || checkOutStr.trim() === "") return '<span style="color:#10B981;font-weight:600;display:flex;align-items:center;gap:0.25rem;"><span class="status-dot active"></span>Active Session</span>';
    try {
        const checkIn = new Date(checkInStr.replace(" ", "T"));
        const checkOut = new Date(checkOutStr.replace(" ", "T"));
        const diffMs = checkOut - checkIn;
        if (isNaN(diffMs) || diffMs < 0) return "—";
        
        const diffSecs = Math.floor(diffMs / 1000);
        const hrs = Math.floor(diffSecs / 3600);
        const mins = Math.floor((diffSecs % 3600) / 60);
        const secs = diffSecs % 60;
        
        let result = "";
        if (hrs > 0) result += `${hrs}h `;
        if (mins > 0 || hrs > 0) result += `${mins}m `;
        result += `${secs}s`;
        return result.trim();
    } catch (e) {
        return "—";
    }
}

function playScannerBeep(freq = 880, duration = 0.15) {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.type = "sine";
        oscillator.frequency.setValueAtTime(freq, audioCtx.currentTime);
        gainNode.gain.setValueAtTime(0.08, audioCtx.currentTime);
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + duration);
    } catch (e) {
        console.log("Audio beep failed:", e);
    }
}

function playErrorBeep() {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.type = "sawtooth";
        oscillator.frequency.setValueAtTime(220, audioCtx.currentTime);
        gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.35);
    } catch (e) {
        console.log("Audio error beep failed:", e);
    }
}

