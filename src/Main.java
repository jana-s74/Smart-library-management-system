import database.SchemaInitializer;
import web.server.LibraWebServer;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Set System Look and Feel for modern OS native rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Enable Anti-aliasing text for smooth Segoe UI typography
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        System.out.println("==================================================");
        System.out.println("📚 Launching LibraAI – Smart Library Management System");
        System.out.println("==================================================");

        // 1. Initialize Database Schema & Seed Data automatically
        SchemaInitializer.initializeDatabase();

        // 2. Start JDK HTTP Web Backend Server
        new Thread(() -> {
            LibraWebServer webServer = new LibraWebServer();
            webServer.start();
        }, "LibraWebServer-Thread").start();

        System.out.println(" Desktop GUI disabled. Web Server mode is active.");
    }
}

