package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "";
        }
        try {
            return BCrypt.hashpw(password, BCrypt.gensalt(10));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        if (rawPassword.isEmpty() || storedHash.isEmpty()) {
            return rawPassword.equals(storedHash);
        }
        try {
            // Support legacy SHA-256 hashes if they exist, or just check BCrypt.
            // BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 chars.
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                return BCrypt.checkpw(rawPassword, storedHash);
            }
            // Fallback for legacy SHA-256 plain hex hashes (64 hex characters)
            if (storedHash.length() == 64) {
                // Return verification of SHA-256 for backward compatibility with existing DB entries
                return hashSha256(rawPassword).equalsIgnoreCase(storedHash);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String hashSha256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return "";
        }
    }
}

