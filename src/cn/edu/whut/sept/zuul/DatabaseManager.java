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
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 先连接到MySQL服务器（不指定数据库）
            Connection serverConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                DB_USER, DB_PASSWORD);
            
            // 创建数据库（如果不存在）
            Statement stmt = serverConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS zuul_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.close();
            serverConnection.close();
            
            // 连接到zuul_game数据库
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // 创建表结构
            createTables();
            
            System.out.println("数据库连接成功！");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到！请确保已添加mysql-connector-java依赖。");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
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
     * 验证用户登录
     */
    public Integer loginUser(String username, String password) {
        try {
            String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                
                // 更新最后登录时间
                String updateSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                rs.close();
                pstmt.close();
                return userId;
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
     * 检查用户名是否存在
     */
    public boolean userExists(String username) {
        try {
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

