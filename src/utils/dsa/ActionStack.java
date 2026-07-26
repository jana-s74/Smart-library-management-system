package utils.dsa;

import java.util.Stack;

/**
 * DSA Implementation: Stack (LIFO) for Action Undo Operations (e.g. Issue & Return operations).
 */
public class ActionStack {

    public static class LibraryAction {
        private final String actionType; // "ISSUE" or "RETURN"
        private final int borrowId;
        private final int bookId;
        private final int studentId;
        private final String description;

        public LibraryAction(String actionType, int borrowId, int bookId, int studentId, String description) {
            this.actionType = actionType;
            this.borrowId = borrowId;
            this.bookId = bookId;
            this.studentId = studentId;
            this.description = description;
        }

        public String getActionType() { return actionType; }
        public int getBorrowId() { return borrowId; }
        public int getBookId() { return bookId; }
        public int getStudentId() { return studentId; }
        public String getDescription() { return description; }
    }

    private final Stack<LibraryAction> undoStack;

    public ActionStack() {
        this.undoStack = new Stack<>();
    }

    public void pushAction(LibraryAction action) {
        undoStack.push(action);
    }

    public LibraryAction popAction() {
        if (undoStack.isEmpty()) {
            return null;
        }
        return undoStack.pop();
    }

    public boolean isEmpty() {
        return undoStack.isEmpty();
    }

    public int size() {
        return undoStack.size();
    }
}
