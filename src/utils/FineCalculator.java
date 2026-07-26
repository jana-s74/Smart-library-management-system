package utils;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class FineCalculator {

    public static final double DAILY_FINE_RATE = 0.50; // $0.50 per overdue day

    public static double calculateFine(Timestamp dueDate, Timestamp returnDate) {
        if (dueDate == null) return 0.0;
        
        long actualEnd = (returnDate != null) ? returnDate.getTime() : System.currentTimeMillis();
        long dueTime = dueDate.getTime();

        if (actualEnd <= dueTime) {
            return 0.0; // Returned on time
        }

        long diffInMillies = actualEnd - dueTime;
        long overdueDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        
        if (overdueDays <= 0) {
            overdueDays = 1; // Minimum 1 day if past timestamp
        }

        return overdueDays * DAILY_FINE_RATE;
    }
}
