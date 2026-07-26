package model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role; // ADMIN or STUDENT
    private String profilePicPath;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String username, String fullName, String email, String phone, String role, String profilePicPath, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profilePicPath = profilePicPath;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
