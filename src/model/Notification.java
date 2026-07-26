package model;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private String userType;
    private int userId;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(int notificationId, String userType, int userId, String title, String message, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.userType = userType;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
