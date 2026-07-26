package service;

import dao.BorrowDAO;
import model.BorrowHistory;
import utils.dsa.ActionStack;
import utils.dsa.ActionStack.LibraryAction;

import java.util.List;

public class BorrowService {

    private final BorrowDAO borrowDAO;
    private final ActionStack actionStack;

    public BorrowService() {
        this.borrowDAO = new BorrowDAO();
        this.actionStack = new ActionStack();
    }

    public boolean issueBook(int studentId, int bookId, int loanDays) {
        boolean success = borrowDAO.issueBook(studentId, bookId, loanDays);
        if (success) {
            actionStack.pushAction(new LibraryAction("ISSUE", 0, bookId, studentId, "Issued Book ID " + bookId + " to Student ID " + studentId));
        }
        return success;
    }

    public boolean returnBook(int borrowId, int studentId, int bookId, java.sql.Timestamp dueDate) {
        boolean success = borrowDAO.returnBook(borrowId, studentId, bookId, dueDate);
        if (success) {
            actionStack.pushAction(new LibraryAction("RETURN", borrowId, bookId, studentId, "Returned Book ID " + bookId + " from Student ID " + studentId));
        }
        return success;
    }

    public List<BorrowHistory> getAllBorrowHistory() {
        return borrowDAO.getAllBorrowHistory();
    }

    public List<BorrowHistory> getBorrowHistoryByStudent(int studentId) {
        return borrowDAO.getBorrowHistoryByStudent(studentId);
    }

    public ActionStack getActionStack() {
        return actionStack;
    }
}
