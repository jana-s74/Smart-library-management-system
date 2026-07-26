package utils.dsa;

import model.Student;

import java.util.LinkedList;

/**
 * DSA Implementation: Queue (FIFO) for Managing Book Waiting Lists.
 */
public class WaitingQueue {

    private final LinkedList<Student> queue;

    public WaitingQueue() {
        this.queue = new LinkedList<>();
    }

    public void enqueue(Student student) {
        if (student != null && !contains(student)) {
            queue.addLast(student);
        }
    }

    public Student dequeue() {
        if (isEmpty()) {
            return null;
        }
        return queue.removeFirst();
    }

    public Student peek() {
        return queue.peekFirst();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public boolean contains(Student student) {
        if (student == null) return false;
        for (Student s : queue) {
            if (s.getId() == student.getId()) return true;
        }
        return false;
    }

    public java.util.List<Student> getAllInQueue() {
        return new java.util.ArrayList<>(queue);
    }
}
