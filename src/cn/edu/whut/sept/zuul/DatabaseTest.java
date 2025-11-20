/**
 * 数据库连接测试工具
 * 用于验证数据库连接和检查数据
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.sql.*;

public class DatabaseTest {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/zuul_game?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "shellykoi";
    private static final String DB_PASSWORD = "123456koiii";
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("数据库连接测试工具");
        System.out.println("========================================\n");
        
        // 测试1: 检查MySQL驱动
        System.out.println("测试1: 检查MySQL驱动...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL驱动加载成功\n");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL驱动未找到！");
            System.err.println("请确保已添加mysql-connector-java到classpath\n");
            return;
        }
        
        // 测试2: 连接MySQL服务器
        System.out.println("测试2: 连接MySQL服务器...");
        Connection serverConnection = null;
        try {
            serverConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                DB_USER, DB_PASSWORD);
            System.out.println("✅ MySQL服务器连接成功\n");
        } catch (SQLException e) {
            System.err.println("❌ MySQL服务器连接失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("\n请检查：");
            System.err.println("1. MySQL服务是否正在运行");
            System.err.println("2. 用户名和密码是否正确");
            System.err.println("3. MySQL是否允许本地连接\n");
            return;
        } finally {
            if (serverConnection != null) {
                try {
                    serverConnection.close();
                } catch (SQLException e) {
                    // 忽略
                }
            }
        }
        
        // 测试3: 检查数据库是否存在
        System.out.println("测试3: 检查数据库是否存在...");
        Connection dbConnection = null;
        try {
            dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ 数据库 'zuul_game' 连接成功\n");
        } catch (SQLException e) {
            System.err.println("❌ 数据库 'zuul_game' 连接失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("\n数据库可能不存在，将在首次运行时自动创建\n");
            return;
        }
        
        // 测试4: 检查表是否存在
        System.out.println("测试4: 检查数据库表...");
        try {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            String[] tableTypes = {"TABLE"};
            ResultSet tables = metaData.getTables(null, null, "%", tableTypes);
            
            boolean hasUsers = false;
            boolean hasGameRecords = false;
            boolean hasPlayerStates = false;
            
            System.out.println("已找到的表：");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("  - " + tableName);
                if (tableName.equals("users")) hasUsers = true;
                if (tableName.equals("game_records")) hasGameRecords = true;
                if (tableName.equals("player_states")) hasPlayerStates = true;
            }
            tables.close();
            
            if (hasUsers && hasGameRecords && hasPlayerStates) {
                System.out.println("✅ 所有必需的表都存在\n");
            } else {
                System.out.println("⚠️  缺少某些表，将在首次运行时自动创建\n");
            }
        } catch (SQLException e) {
            System.err.println("❌ 检查表时出错: " + e.getMessage() + "\n");
        }
        
        // 测试5: 检查用户数据
        System.out.println("测试5: 检查用户数据...");
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            rs.next();
            int userCount = rs.getInt("count");
            rs.close();
            
            if (userCount > 0) {
                System.out.println("✅ 找到 " + userCount + " 个用户\n");
                
                // 显示用户列表
                rs = stmt.executeQuery("SELECT user_id, username, created_at, last_login FROM users ORDER BY user_id");
                System.out.println("用户列表：");
                System.out.println("  ID  | 用户名      | 创建时间              | 最后登录");
                System.out.println("  ----|------------|----------------------|----------------------");
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    
                    System.out.printf("  %-4d| %-11s| %-20s| %-20s%n",
                        userId, username,
                        createdAt != null ? createdAt.toString() : "N/A",
                        lastLogin != null ? lastLogin.toString() : "从未登录");
                }
                rs.close();
                System.out.println();
            } else {
                System.out.println("⚠️  数据库中没有用户数据");
                System.out.println("   这是正常的，用户需要通过注册功能创建\n");
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ 检查用户数据时出错: " + e.getMessage());
            System.err.println("   表可能不存在，将在首次运行时自动创建\n");
        }
        
        // 测试6: 检查游戏记录
        System.out.println("测试6: 检查游戏记录...");
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM game_records");
            rs.next();
            int recordCount = rs.getInt("count");
            rs.close();
            stmt.close();
            
            if (recordCount > 0) {
                System.out.println("✅ 找到 " + recordCount + " 条游戏记录\n");
            } else {
                System.out.println("⚠️  数据库中没有游戏记录");
                System.out.println("   这是正常的，游戏记录会在用户开始游戏时创建\n");
            }
        } catch (SQLException e) {
            System.err.println("❌ 检查游戏记录时出错: " + e.getMessage() + "\n");
        }
        
        // 测试7: 检查玩家状态
        System.out.println("测试7: 检查玩家状态...");
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM player_states");
            rs.next();
            int stateCount = rs.getInt("count");
            rs.close();
            stmt.close();
            
            if (stateCount > 0) {
                System.out.println("✅ 找到 " + stateCount + " 条玩家状态记录\n");
            } else {
                System.out.println("⚠️  数据库中没有玩家状态记录");
                System.out.println("   这是正常的，玩家状态会在保存游戏时创建\n");
            }
        } catch (SQLException e) {
            System.err.println("❌ 检查玩家状态时出错: " + e.getMessage() + "\n");
        }
        
        // 关闭连接
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            // 忽略
        }
        
        System.out.println("========================================");
        System.out.println("测试完成！");
        System.out.println("========================================");
        System.out.println("\n总结：");
        System.out.println("- 如果所有测试都通过，数据库连接正常");
        System.out.println("- 如果没有数据，这是正常的，需要通过注册/登录功能创建");
        System.out.println("- 如果连接失败，请检查MySQL服务和配置");
    }
}

