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
    selectedDetailBook: null
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
            <div class="book-cover">
                <span class="category-tag">${escapeHtml(b.categoryName || 'General')}</span>
                📖
            </div>
            <div class="book-title">${escapeHtml(b.title)}</div>
            <div class="book-author">by ${escapeHtml(b.author)}</div>
            <div class="book-meta">
                <span>ISBN: ${escapeHtml(b.isbn || 'N/A')}</span>
                <span class="copies-badge ${b.availableCopies > 0 ? '' : 'out'}">
                    ${b.availableCopies > 0 ? b.availableCopies + ' Available' : 'Out of Stock'}
                </span>
            </div>
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
            <div class="book-cover" onclick="openDetailModal(${b.bookId})">
                <span class="category-tag">${escapeHtml(b.categoryName || 'General')}</span>
                📖
            </div>
            <div class="book-title" onclick="openDetailModal(${b.bookId})">${escapeHtml(b.title)}</div>
            <div class="book-author">by ${escapeHtml(b.author)}</div>
            <div class="book-meta">
                <span>Shelf: ${escapeHtml(b.shelfNumber || 'A-1')}</span>
                <span class="copies-badge ${b.availableCopies > 0 ? '' : 'out'}">
                    ${b.availableCopies} / ${b.totalCopies} left
                </span>
            </div>
            <div style="display:flex;gap:0.4rem;margin-top:0.8rem;">
                <button class="btn btn-outline btn-sm" style="flex:1;" onclick="openDetailModal(${b.bookId})">Details</button>
                ${isAdmin() ? `
                    <button class="btn btn-outline btn-sm" onclick="editBook(${b.bookId})">✏️</button>
                    <button class="btn btn-outline btn-sm" style="color:#EF4444;" onclick="deleteBook(${b.bookId})">🗑️</button>
                ` : ''}
            </div>
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
                ${bh.status === 'BORROWED' || bh.status === 'OVERDUE' ? `
                    <button class="btn btn-primary btn-sm" onclick="processReturnBook(${bh.borrowId}, ${bh.studentId}, ${bh.bookId})">Return</button>
                ` : `<span style="color:#9CA3AF;font-size:0.8rem;">Returned</span>`}
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

    tbody.innerHTML = state.students.map(s => `
        <tr>
            <td>#${s.id}</td>
            <td><code>${escapeHtml(s.studentCode)}</code></td>
            <td>
                <strong>${escapeHtml(s.fullName)}</strong><br>
                <small style="color:#6B7280;">${escapeHtml(s.email)}</small>
            </td>
            <td>${escapeHtml(s.department)}</td>
            <td>Year ${s.yearOfStudy}</td>
            <td>${s.currentBorrowed} / ${s.maxBorrowLimit}</td>
            <td style="color:${s.totalFinesOwed > 0 ? '#EF4444' : '#10B981'};font-weight:600;">
                $${(s.totalFinesOwed || 0).toFixed(2)}
            </td>
            <td>
                <span class="status-tag ${s.status ? s.status.toLowerCase() : 'active'}">
                    ${escapeHtml(s.status || 'ACTIVE')}
                </span>
            </td>
            <td>
                ${isAdmin() ? `
                    <button class="btn btn-outline btn-sm" onclick="toggleStudentStatus(${s.id}, '${s.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'}')">
                        ${s.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                    </button>
                ` : `<span style="color:#9CA3AF;font-size:0.8rem;">Read Only</span>`}
            </td>
        </tr>
    `).join('');
}

function populateIssueSelects() {
    const studentSelect = document.getElementById("issueStudentSelect");
    const bookSelect = document.getElementById("issueBookSelect");
    if (!studentSelect || !bookSelect) return;

    studentSelect.innerHTML = state.students.map(s => `
        <option value="${s.id}">${escapeHtml(s.fullName)} (${escapeHtml(s.studentCode)})</option>
    `).join('');

    const availableBooks = state.books.filter(b => b.availableCopies > 0);
    bookSelect.innerHTML = availableBooks.map(b => `
        <option value="${b.bookId}">${escapeHtml(b.title)} (${b.availableCopies} available)</option>
    `).join('');
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
    if (tabName === 'analytics') renderAnalyticsCanvasChart();
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
    // Validate register number is between 2024100 and 2024150
    const regNum = parseInt(studentCode, 10);
    if (isNaN(regNum) || regNum < 2024100 || regNum > 2024150) {
        showToast('Register number must be between 2024100 and 2024150.', 'error');
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
    } else {
        if (loggedInView) loggedInView.classList.add("hidden");
        if (addBookHeaderBtn) addBookHeaderBtn.classList.add("hidden");
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

// Issue Book Modal
function openIssueModal() {
    populateIssueSelects();
    document.getElementById("issueModal").classList.remove("hidden");
}

function closeIssueModal() {
    document.getElementById("issueModal").classList.add("hidden");
}

async function handleIssueSubmit(e) {
    e.preventDefault();
    const studentId = document.getElementById("issueStudentSelect").value;
    const bookId = document.getElementById("issueBookSelect").value;
    const loanDays = document.getElementById("issueLoanDays").value;

    try {
        const res = await fetch(`${API_BASE}/api/borrow/issue`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId, bookId, loanDays })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message || 'Book issued successfully!', 'success');
            closeIssueModal();
            fetchCatalogBooks();
            fetchBorrowHistory();
            fetchStudents();
            fetchStats();
        } else {
            showToast(data.message || 'Issue failed.', 'error');
        }
    } catch (err) {
        showToast('Request failed.', 'error');
    }
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

    const stockBadge = document.getElementById("detailStockBadge");
    if (book.availableCopies > 0) {
        stockBadge.innerText = `${book.availableCopies} Available`;
        stockBadge.style.background = '#ECFDF5';
        stockBadge.style.color = '#10B981';
    } else {
        stockBadge.innerText = 'Out of Stock';
        stockBadge.style.background = '#FEF2F2';
        stockBadge.style.color = '#EF4444';
    }

    document.getElementById("detailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("detailModal").classList.add("hidden");
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
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
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
