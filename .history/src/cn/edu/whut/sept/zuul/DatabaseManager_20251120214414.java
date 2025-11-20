/**
 * 数据库管理类
 * 负责MySQL数据库的连接、初始化和基本操作
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/zuul_game?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "shellykoi";
    private static final String DB_PASSWORD = "123456koiii";
    
    private static DatabaseManager instance;
    private Connection connection;
    
    /**
     * 获取数据库管理器单例
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数
     */
    private DatabaseManager() {
        initializeDatabase();
    }
    
    /**
     * 初始化数据库连接和表结构
     */
    private void initializeDatabase() {
        System.out.println("\n========================================");
        System.out.println("正在初始化数据库连接...");
        System.out.println("========================================");
        
        try {
            // 加载MySQL驱动
            System.out.println("步骤1: 加载MySQL驱动...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL驱动加载成功");
            
            // 先连接到MySQL服务器（不指定数据库）
            System.out.println("步骤2: 连接MySQL服务器...");
            Connection serverConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                DB_USER, DB_PASSWORD);
            System.out.println("✅ MySQL服务器连接成功");
            
            // 创建数据库（如果不存在）
            System.out.println("步骤3: 检查/创建数据库 'zuul_game'...");
            Statement stmt = serverConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS zuul_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.close();
            serverConnection.close();
            System.out.println("✅ 数据库 'zuul_game' 已就绪");
            
            // 连接到zuul_game数据库
            System.out.println("步骤4: 连接到数据库 'zuul_game'...");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ 数据库连接成功");
            
            // 创建表结构
            System.out.println("步骤5: 检查/创建数据库表...");
            createTables();
            System.out.println("✅ 数据库表已就绪");
            
            // 检查现有数据
            System.out.println("步骤6: 检查数据库数据...");
            checkDatabaseData();
            
            System.out.println("========================================");
            System.out.println("数据库初始化完成！");
            System.out.println("========================================");
        } catch (ClassNotFoundException e) {
            System.err.println("\n❌ MySQL驱动未找到！");
            System.err.println("请确保已添加mysql-connector-java依赖到classpath");
            System.err.println("例如: java -cp \"bin;lib\\mysql-connector-j-9.5.0.jar\" ...");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\n❌ 数据库连接失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("\n请检查：");
            System.err.println("1. MySQL服务是否正在运行");
            System.err.println("2. 用户名和密码是否正确（当前: " + DB_USER + "）");
            System.err.println("3. MySQL是否允许本地连接");
            e.printStackTrace();
        }
    }
    
    /**
     * 检查数据库中的数据并输出统计信息
     */
    private void checkDatabaseData() {
        try {
            // 检查用户数量
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            rs.next();
            int userCount = rs.getInt("count");
            rs.close();
            
            if (userCount > 0) {
                System.out.println("  - 用户数量: " + userCount);
            } else {
                System.out.println("  - 用户数量: 0 (需要通过注册功能创建用户)");
            }
            
            // 检查游戏记录数量
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM game_records");
            rs.next();
            int recordCount = rs.getInt("count");
            rs.close();
            
            if (recordCount > 0) {
                System.out.println("  - 游戏记录: " + recordCount);
            } else {
                System.out.println("  - 游戏记录: 0 (游戏记录会在用户开始游戏时创建)");
            }
            
            // 检查玩家状态数量
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM player_states");
            rs.next();
            int stateCount = rs.getInt("count");
            rs.close();
            stmt.close();
            
            if (stateCount > 0) {
                System.out.println("  - 玩家状态: " + stateCount);
            } else {
                System.out.println("  - 玩家状态: 0 (玩家状态会在保存游戏时创建)");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️  无法检查数据统计: " + e.getMessage());
        }
    }
    
    /**
     * 创建数据库表
     */
    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // 用户表
        String createUsersTable = 
            "CREATE TABLE IF NOT EXISTS users (" +
            "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  username VARCHAR(50) UNIQUE NOT NULL," +
            "  password VARCHAR(255) NOT NULL," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  last_login TIMESTAMP NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        stmt.executeUpdate(createUsersTable);
        
        // 游戏记录表
        String createGameRecordsTable = 
            "CREATE TABLE IF NOT EXISTS game_records (" +
            "  record_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  user_id INT NOT NULL," +
            "  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  end_time TIMESTAMP NULL," +
            "  is_completed BOOLEAN DEFAULT FALSE," +
            "  rooms_explored INT DEFAULT 0," +
            "  items_collected INT DEFAULT 0," +
            "  cookie_eaten BOOLEAN DEFAULT FALSE," +
            "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        stmt.executeUpdate(createGameRecordsTable);
        
        // 玩家状态表（保存游戏进度）
        String createPlayerStatesTable = 
            "CREATE TABLE IF NOT EXISTS player_states (" +
            "  state_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  user_id INT NOT NULL," +
            "  current_room VARCHAR(50) NOT NULL," +
            "  max_weight DOUBLE DEFAULT 10.0," +
            "  inventory TEXT," +
            "  rooms_visited TEXT," +
            "  items_collected_list TEXT," +
            "  cookie_eaten BOOLEAN DEFAULT FALSE," +
            "  saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE," +
            "  UNIQUE KEY unique_user_state (user_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        stmt.executeUpdate(createPlayerStatesTable);
        
        stmt.close();
    }
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }
    
    /**
     * 注册新用户
     */
    public boolean registerUser(String username, String password) {
        try {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 实际应用中应该加密
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // 重复键错误
                return false; // 用户名已存在
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册用户并返回失败原因（null 表示成功）
     */
    public String registerUserWithMessage(String username, String password) {
        try {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 实际应用中应该加密
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0 ? null : "注册失败";
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return "用户名已存在";
            }
            e.printStackTrace();
            return "数据库错误：" + e.getMessage();
        }
    }
    
    /**
     * 验证用户登录
     */
    public Integer loginUser(String username, String password) {
        try {
            // 检查连接是否有效
            if (connection == null || connection.isClosed()) {
                System.err.println("数据库连接不可用，无法验证登录");
                return null;
            }
            
            // 去除用户名和密码的前后空格
            if (username != null) {
                username = username.trim();
            }
            if (password != null) {
                password = password.trim();
            }
            
            System.out.println("=== 登录调试信息 ===");
            System.out.println("输入用户名: [" + username + "] (长度: " + (username != null ? username.length() : 0) + ")");
            System.out.println("输入密码: [" + password + "] (长度: " + (password != null ? password.length() : 0) + ")");
            
            // 先检查用户是否存在
            String checkUserSql = "SELECT user_id, password FROM users WHERE username = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkUserSql);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next()) {
                String dbPassword = checkRs.getString("password");
                int userId = checkRs.getInt("user_id");
                
                System.out.println("找到用户 ID: " + userId);
                System.out.println("数据库密码: [" + dbPassword + "] (长度: " + (dbPassword != null ? dbPassword.length() : 0) + ")");
                
                // 去除数据库密码的前后空格（防止存储时有多余空格）
                if (dbPassword != null) {
                    dbPassword = dbPassword.trim();
                }
                
                System.out.println("去除空格后数据库密码: [" + dbPassword + "] (长度: " + (dbPassword != null ? dbPassword.length() : 0) + ")");
                
                // 详细的密码比较调试
                System.out.println("密码比较详情:");
                try {
                    System.out.println("  输入密码字节: " + (password != null ? java.util.Arrays.toString(password.getBytes("UTF-8")) : "null"));
                    System.out.println("  数据库密码字节: " + (dbPassword != null ? java.util.Arrays.toString(dbPassword.getBytes("UTF-8")) : "null"));
                } catch (java.io.UnsupportedEncodingException e) {
                    System.out.println("  无法获取字节数组: " + e.getMessage());
                }
                System.out.println("  使用equals比较: " + (password != null && password.equals(dbPassword)));
                System.out.println("  使用equalsIgnoreCase比较: " + (password != null && password.equalsIgnoreCase(dbPassword)));
                
                // 比较密码（使用equals确保精确匹配）
                if (password != null && password.equals(dbPassword)) {
                    System.out.println("✅ 密码验证成功！");
                    // 更新最后登录时间
                    String updateSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    checkRs.close();
                    checkStmt.close();
                    return userId;
                } else {
                    System.out.println("❌ 密码验证失败！");
                    System.out.println("  输入密码: [" + password + "]");
                    System.out.println("  数据库密码: [" + dbPassword + "]");
                    System.out.println("  是否相等: " + (password != null && password.equals(dbPassword)));
                    checkRs.close();
                    checkStmt.close();
                    return null;
                }
            } else {
                System.out.println("❌ 用户不存在: " + username);
                checkRs.close();
                checkStmt.close();
                return null;
            }
        } catch (SQLException e) {
            System.err.println("登录过程发生SQL异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean userExists(String username) {
        try {
            // 检查连接是否有效
            if (connection == null || connection.isClosed()) {
                System.err.println("数据库连接不可用，无法检查用户是否存在");
                return false;
            }
            
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            pstmt.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 保存玩家游戏状态
     */
    public boolean savePlayerState(int userId, String currentRoom, double maxWeight, 
                                   List<String> inventory, List<String> roomsVisited, 
                                   List<String> itemsCollected, boolean cookieEaten) {
        try {
            String sql = "INSERT INTO player_states " +
                        "(user_id, current_room, max_weight, inventory, rooms_visited, items_collected_list, cookie_eaten) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "current_room = VALUES(current_room), " +
                        "max_weight = VALUES(max_weight), " +
                        "inventory = VALUES(inventory), " +
                        "rooms_visited = VALUES(rooms_visited), " +
                        "items_collected_list = VALUES(items_collected_list), " +
                        "cookie_eaten = VALUES(cookie_eaten)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, currentRoom);
            pstmt.setDouble(3, maxWeight);
            pstmt.setString(4, String.join(",", inventory));
            pstmt.setString(5, String.join(",", roomsVisited));
            pstmt.setString(6, String.join(",", itemsCollected));
            pstmt.setBoolean(7, cookieEaten);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 加载玩家游戏状态
     */
    public Map<String, Object> loadPlayerState(int userId) {
        try {
            String sql = "SELECT * FROM player_states WHERE user_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> state = new HashMap<>();
                state.put("currentRoom", rs.getString("current_room"));
                state.put("maxWeight", rs.getDouble("max_weight"));
                
                String inventoryStr = rs.getString("inventory");
                state.put("inventory", inventoryStr != null && !inventoryStr.isEmpty() 
                          ? List.of(inventoryStr.split(",")) : new ArrayList<>());
                
                String roomsVisitedStr = rs.getString("rooms_visited");
                state.put("roomsVisited", roomsVisitedStr != null && !roomsVisitedStr.isEmpty() 
                         ? List.of(roomsVisitedStr.split(",")) : new ArrayList<>());
                
                String itemsCollectedStr = rs.getString("items_collected_list");
                state.put("itemsCollected", itemsCollectedStr != null && !itemsCollectedStr.isEmpty() 
                         ? List.of(itemsCollectedStr.split(",")) : new ArrayList<>());
                
                state.put("cookieEaten", rs.getBoolean("cookie_eaten"));
                
                rs.close();
                pstmt.close();
                return state;
            }
            
            rs.close();
            pstmt.close();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建新的游戏记录
     */
    public int createGameRecord(int userId) {
        try {
            // 检查连接是否有效
            if (connection == null || connection.isClosed()) {
                System.err.println("数据库连接不可用，无法创建游戏记录");
                return -1;
            }
            
            String sql = "INSERT INTO game_records (user_id) VALUES (?)";
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            int recordId = -1;
            if (rs.next()) {
                recordId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            return recordId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 更新游戏记录（游戏结束时）
     */
    public boolean updateGameRecord(int recordId, boolean isCompleted, 
                                    int roomsExplored, int itemsCollected, boolean cookieEaten) {
        try {
            String sql = "UPDATE game_records SET " +
                        "end_time = CURRENT_TIMESTAMP, " +
                        "is_completed = ?, " +
                        "rooms_explored = ?, " +
                        "items_collected = ?, " +
                        "cookie_eaten = ? " +
                        "WHERE record_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setBoolean(1, isCompleted);
            pstmt.setInt(2, roomsExplored);
            pstmt.setInt(3, itemsCollected);
            pstmt.setBoolean(4, cookieEaten);
            pstmt.setInt(5, recordId);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

