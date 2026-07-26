package model;

import java.sql.Timestamp;

public class Achievement {
    private int achievementId;
    private int studentId;
    private String title;
    private String badgeType;
    private Timestamp unlockedAt;

    public Achievement() {}

    public Achievement(int achievementId, int studentId, String title, String badgeType, Timestamp unlockedAt) {
        this.achievementId = achievementId;
        this.studentId = studentId;
        this.title = title;
        this.badgeType = badgeType;
        this.unlockedAt = unlockedAt;
    }

    public int getAchievementId() { return achievementId; }
    public void setAchievementId(int achievementId) { this.achievementId = achievementId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public Timestamp getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }
}
