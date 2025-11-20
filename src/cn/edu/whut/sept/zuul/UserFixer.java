package cn.edu.whut.sept.zuul;

import java.sql.*;

public class UserFixer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/zuul_game?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "shellykoi";
    private static final String DB_PASSWORD = "123456koiii";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("开始修复用户数据...");
        System.out.println("========================================");

        try {
            // 1. 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. 连接数据库
            System.out.println("连接数据库...");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("✅ 连接成功！");
                
                String targetUser = "koi";
                String targetPass = "123456";
                
                // 3. 检查用户是否存在
                System.out.println("检查用户 '" + targetUser + "'...");
                String checkSql = "SELECT user_id FROM users WHERE username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setString(1, targetUser);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // 用户存在，重置密码
                        int userId = rs.getInt("user_id");
                        System.out.println("用户已存在 (ID: " + userId + ")，正在重置密码...");
                        
                        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, targetPass);
                            updateStmt.setInt(2, userId);
                            int rows = updateStmt.executeUpdate();
                            if (rows > 0) {
                                System.out.println("✅ 密码已重置为: " + targetPass);
                            } else {
                                System.err.println("❌ 密码重置失败！");
                            }
                        }
                    } else {
                        // 用户不存在，创建用户
                        System.out.println("用户不存在，正在创建...");
                        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, targetUser);
                            insertStmt.setString(2, targetPass);
                            int rows = insertStmt.executeUpdate();
                            if (rows > 0) {
                                System.out.println("✅ 用户 '" + targetUser + "' 创建成功，密码: " + targetPass);
                            } else {
                                System.err.println("❌ 用户创建失败！");
                            }
                        }
                    }
                }
                
                // 4. 列出所有用户
                System.out.println("\n当前数据库中的用户:");
                System.out.println("----------------------------------------");
                System.out.println(String.format("%-5s | %-20s | %-20s", "ID", "Username", "Password"));
                System.out.println("----------------------------------------");
                
                String listSql = "SELECT user_id, username, password FROM users";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(listSql)) {
                    while (rs.next()) {
                        System.out.println(String.format("%-5d | %-20s | %-20s", 
                            rs.getInt("user_id"), 
                            rs.getString("username"), 
                            rs.getString("password")));
                    }
                }
                System.out.println("----------------------------------------");
                
            }
        } catch (Exception e) {
            System.err.println("❌ 发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n修复完成。请重启服务器并尝试登录。");
    }
}

