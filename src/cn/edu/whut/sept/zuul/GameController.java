/**
 * 游戏Web API控制器
 * 处理HTTP请求，执行游戏命令并返回JSON响应
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {
    // 多玩家会话管理：使用sessionId作为键
    private Map<String, GameSession> sessions;
    private DatabaseManager dbManager;
    
    /**
     * 创建游戏控制器
     */
    public GameController() {
        sessions = new HashMap<>();
        dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * 游戏会话类
     */
    private static class GameSession {
        Game game;
        Player player;
        Parser parser;
        String username;
        Integer userId;
        
        GameSession(String username, Integer userId) {
            this.username = username;
            this.userId = userId;
            this.game = new Game();
            this.player = game.getPlayer();
            this.player.setUserId(userId);
            this.player.setName(username);
            this.parser = game.getParser();
            
            // 创建游戏记录
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Integer recordId = dbManager.createGameRecord(userId);
            this.player.setGameRecordId(recordId);
        }
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * 注册新用户
     */
    public Map<String, Object> register(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名不能为空");
            return response;
        }
        
        if (password == null || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "密码不能为空");
            return response;
        }
        
        if (dbManager.userExists(username)) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return response;
        }
        
        boolean success = dbManager.registerUser(username, password);
        if (success) {
            // 注册成功后自动登录
            Integer userId = dbManager.loginUser(username, password);
            if (userId != null) {
                String sessionId = generateSessionId();
                sessions.put(sessionId, new GameSession(username, userId));
                response.put("success", true);
                response.put("message", "注册成功！");
                response.put("sessionId", sessionId);
                response.put("username", username);
            } else {
                response.put("success", false);
                response.put("message", "注册成功，但登录失败");
            }
        } else {
            response.put("success", false);
            response.put("message", "注册失败");
        }
        
        return response;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        
        Integer userId = dbManager.loginUser(username, password);
        if (userId != null) {
            // 如果用户已有会话，先移除旧会话（确保每次登录都是新游戏）
            String oldSessionId = null;
            for (Map.Entry<String, GameSession> entry : sessions.entrySet()) {
                if (entry.getValue().username.equals(username) && 
                    entry.getValue().userId.equals(userId)) {
                    oldSessionId = entry.getKey();
                    break;
                }
            }
            if (oldSessionId != null) {
                sessions.remove(oldSessionId);
            }
            
            // 创建新会话（新游戏）
            String sessionId = generateSessionId();
            sessions.put(sessionId, new GameSession(username, userId));
            response.put("success", true);
            response.put("message", "登录成功！");
            response.put("sessionId", sessionId);
            response.put("username", username);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return response;
    }
    
    /**
     * 获取游戏会话
     */
    private GameSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 执行游戏命令（向后兼容，使用默认会话）
     * 
     * @param commandString 命令字符串
     * @return 包含执行结果的Map
     */
    public Map<String, Object> executeCommand(String commandString) {
        // 如果没有会话，创建一个临时会话（游客模式）
        if (sessions.isEmpty()) {
            // 创建匿名会话（每次都是新游戏）
            String sessionId = generateSessionId();
            sessions.put(sessionId, new GameSession("Guest", null));
            return executeCommand(commandString, sessionId);
        }
        // 使用第一个会话
        return executeCommand(commandString, sessions.keySet().iterator().next());
    }
    
    /**
     * 执行游戏命令
     * 
     * @param commandString 命令字符串
     * @param sessionId 会话ID
     * @return 包含执行结果的Map
     */
    public Map<String, Object> executeCommand(String commandString, String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        GameSession session = getSession(sessionId);
        if (session == null) {
            response.put("success", false);
            response.put("message", "会话无效，请重新登录");
            return response;
        }
        
        Game game = session.game;
        Player player = session.player;
        Parser parser = session.parser;
        
        try {
            // 处理quit命令
            if (commandString.trim().equalsIgnoreCase("quit")) {
                // 更新游戏记录
                if (player.getGameRecordId() != null) {
                    GameCompletionChecker.CompletionInfo info = 
                        GameCompletionChecker.checkCompletion(player);
                    dbManager.updateGameRecord(
                        player.getGameRecordId(),
                        info.isCompleted(),
                        info.getRoomsExplored(),
                        info.getItemsCollected(),
                        info.isCookieEaten()
                    );
                }
                // 移除会话
                sessions.remove(sessionId);
                response.put("success", true);
                response.put("message", "感谢游玩！再见！");
                response.put("quit", true);
                return response;
            }
            
            // 解析命令
            Command command = parser.parseCommand(commandString);
            
            if (command.isUnknown()) {
                response.put("success", false);
                response.put("message", "我不知道你在说什么...");
                return response;
            }
            
            // 执行命令并捕获输出
            String output = captureCommandOutput(command, game, player);
            
            // 检查通关状态
            GameCompletionChecker.CompletionInfo info = 
                GameCompletionChecker.checkCompletion(player);
            response.put("completed", info.isCompleted());
            response.put("progress", info);
            
            response.put("success", true);
            response.put("message", output);
            response.put("quit", false);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "执行命令时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * 捕获命令执行的输出
     */
    private String captureCommandOutput(Command command, Game game, Player player) {
        StringBuilder output = new StringBuilder();
        
        String commandWord = command.getCommandWord();
        
        if (commandWord.equals("go")) {
            if (!command.hasSecondWord()) {
                return "去哪里？";
            }
            String direction = command.getSecondWord();
            Room currentRoom = player.getCurrentRoom();
            Room nextRoom = currentRoom.getExit(direction);
            
            if (nextRoom == null) {
                return "那里没有门！";
            } else {
                game.addRoomToHistory(currentRoom);
                player.setCurrentRoom(nextRoom);
                
                // 检查传输房间
                if (nextRoom instanceof TransporterRoom) {
                    TransporterRoom transporter = (TransporterRoom) nextRoom;
                    Room randomRoom = transporter.getRandomRoom();
                    if (randomRoom != null) {
                        output.append("你踏入了一个神秘的传输房间...\n");
                        output.append("突然，你被传送到另一个位置！\n");
                        player.setCurrentRoom(randomRoom);
                    }
                }
                
                output.append(player.getCurrentRoom().getLongDescription());
            }
        } else if (commandWord.equals("look")) {
            output.append(player.getCurrentRoom().getLongDescription());
        } else if (commandWord.equals("back")) {
            Room previousRoom = game.getPreviousRoom();
            if (previousRoom == null) {
                output.append("你已经回到了起点！");
            } else {
                player.setCurrentRoom(previousRoom);
                output.append("你返回到: ").append(previousRoom.getLongDescription());
            }
        } else if (commandWord.equals("items")) {
            Room currentRoom = player.getCurrentRoom();
            output.append("房间内的物品:\n");
            String roomItems = currentRoom.getItemsString();
            if (roomItems.isEmpty()) {
                output.append("  (无)\n");
            } else {
                output.append(roomItems).append("\n");
            }
            output.append("房间总重量: ").append(String.format("%.2f", currentRoom.getTotalWeight())).append("kg\n\n");
            output.append(player.getInventoryString());
        } else if (commandWord.equals("take")) {
            if (!command.hasSecondWord()) {
                return "拾取什么？";
            }
            String itemName = command.getSecondWord();
            Room currentRoom = player.getCurrentRoom();
            Item item = currentRoom.getItem(itemName);
            
            if (item == null) {
                output.append("这里没有 ").append(itemName).append("！");
            } else if (!player.canCarry(item)) {
                output.append("你无法携带 ").append(item.getName())
                      .append("。它重 ").append(String.format("%.2f", item.getWeight()))
                      .append("kg，但你只能再携带 ")
                      .append(String.format("%.2f", player.getMaxWeight() - player.getTotalWeight()))
                      .append("kg。");
            } else {
                currentRoom.removeItem(itemName);
                player.takeItem(item);
                output.append("你拾取了 ").append(item.getName()).append("。");
            }
        } else if (commandWord.equals("drop")) {
            if (!command.hasSecondWord()) {
                return "丢弃什么？";
            }
            String itemName = command.getSecondWord();
            Item item = player.dropItem(itemName);
            
            if (item == null) {
                output.append("你没有 ").append(itemName).append("！");
            } else {
                player.getCurrentRoom().addItem(item);
                output.append("你丢弃了 ").append(item.getName()).append("。");
            }
        } else if (commandWord.equals("eat")) {
            if (!command.hasSecondWord() || !command.getSecondWord().equals("cookie")) {
                return "吃什么？";
            }
            Item cookie = player.getItem("cookie");
            if (cookie == null) {
                output.append("你没有魔法饼干！");
            } else {
                player.dropItem("cookie");
                player.increaseMaxWeight(5.0);
                output.append("你吃掉了魔法饼干。你的负重能力增加了5kg！\n");
                output.append("新的最大负重: ").append(String.format("%.2f", player.getMaxWeight())).append("kg");
            }
        } else if (commandWord.equals("help")) {
            output.append("你可以使用以下命令:\n");
            output.append("  go <方向>  - 向指定方向移动 (north, south, east, west)\n");
            output.append("  look       - 查看当前房间的详细信息\n");
            output.append("  back       - 返回上一个房间\n");
            output.append("  take <物品> - 拾取房间内的物品\n");
            output.append("  drop <物品> - 丢弃身上的物品\n");
            output.append("  items      - 查看房间和身上的物品\n");
            output.append("  eat cookie  - 吃掉魔法饼干（增加负重）\n");
            output.append("  help       - 显示此帮助信息\n");
            output.append("  quit       - 退出游戏");
        } else {
            // 其他命令通过Game的processCommand处理
            output.append("我不知道你在说什么...");
        }
        
        return output.toString();
    }
    
    /**
     * 保存游戏状态
     */
    public Map<String, Object> saveGame(String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        GameSession session = getSession(sessionId);
        if (session == null) {
            response.put("success", false);
            response.put("message", "会话无效，请重新登录");
            return response;
        }
        
        GameStateManager stateManager = new GameStateManager(session.game);
        boolean success = stateManager.saveGameState();
        
        if (success) {
            response.put("success", true);
            response.put("message", "游戏状态已保存！");
        } else {
            response.put("success", false);
            response.put("message", "保存失败");
        }
        
        return response;
    }
    
    /**
     * 加载游戏状态
     */
    public Map<String, Object> loadGame(String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        GameSession session = getSession(sessionId);
        if (session == null) {
            response.put("success", false);
            response.put("message", "会话无效，请重新登录");
            return response;
        }
        
        GameStateManager stateManager = new GameStateManager(session.game);
        boolean success = stateManager.loadGameState();
        
        if (success) {
            response.put("success", true);
            response.put("message", "游戏状态已加载！");
            response.put("gameStatus", getGameStatus(sessionId));
        } else {
            response.put("success", false);
            response.put("message", "没有找到保存的游戏状态");
        }
        
        return response;
    }
    
    /**
     * 获取游戏状态（向后兼容，使用默认会话）
     * 
     * @return 包含游戏状态的Map
     */
    public Map<String, Object> getGameStatus() {
        if (sessions.isEmpty()) {
            Map<String, Object> status = new HashMap<>();
            status.put("error", "没有活动会话");
            return status;
        }
        return getGameStatus(sessions.keySet().iterator().next());
    }
    
    /**
     * 获取游戏状态
     * 
     * @param sessionId 会话ID
     * @return 包含游戏状态的Map
     */
    public Map<String, Object> getGameStatus(String sessionId) {
        Map<String, Object> status = new HashMap<>();
        
        GameSession session = getSession(sessionId);
        if (session == null) {
            status.put("error", "会话无效");
            return status;
        }
        
        Player player = session.player;
        
        // 当前房间信息
        Room currentRoom = player.getCurrentRoom();
        Map<String, Object> roomInfo = new HashMap<>();
        roomInfo.put("shortDescription", currentRoom.getShortDescription());
        roomInfo.put("longDescription", currentRoom.getLongDescription());
        
        // 房间出口
        Map<String, Boolean> exits = new HashMap<>();
        exits.put("north", currentRoom.getExit("north") != null);
        exits.put("south", currentRoom.getExit("south") != null);
        exits.put("east", currentRoom.getExit("east") != null);
        exits.put("west", currentRoom.getExit("west") != null);
        roomInfo.put("exits", exits);
        
        // 房间物品
        List<Map<String, Object>> roomItems = new ArrayList<>();
        for (Item item : currentRoom.getItems()) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("name", item.getName());
            itemInfo.put("description", item.getDescription());
            itemInfo.put("weight", item.getWeight());
            roomItems.add(itemInfo);
        }
        roomInfo.put("items", roomItems);
        
        status.put("currentRoom", roomInfo);
        
        // 玩家信息
        Map<String, Object> playerInfo = new HashMap<>();
        playerInfo.put("name", player.getName());
        playerInfo.put("maxWeight", player.getMaxWeight());
        playerInfo.put("totalWeight", player.getTotalWeight());
        
        // 玩家物品
        List<Map<String, Object>> inventory = new ArrayList<>();
        for (Item item : player.getInventory()) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("name", item.getName());
            itemInfo.put("description", item.getDescription());
            itemInfo.put("weight", item.getWeight());
            inventory.add(itemInfo);
        }
        playerInfo.put("inventory", inventory);
        
        status.put("player", playerInfo);
        
        // 通关信息
        GameCompletionChecker.CompletionInfo completionInfo = 
            GameCompletionChecker.checkCompletion(player);
        Map<String, Object> completion = new HashMap<>();
        completion.put("completed", completionInfo.isCompleted());
        completion.put("roomsExplored", completionInfo.getRoomsExplored());
        completion.put("totalRooms", completionInfo.getTotalRooms());
        completion.put("itemsCollected", completionInfo.getItemsCollected());
        completion.put("totalItems", completionInfo.getTotalItems());
        completion.put("cookieEaten", completionInfo.isCookieEaten());
        completion.put("atStartRoom", completionInfo.isAtStartRoom());
        status.put("completion", completion);
        
        return status;
    }
    
    /**
     * 获取Game实例（用于命令执行器，兼容旧代码）
     */
    public Game getGame() {
        // 返回第一个会话的游戏（用于向后兼容）
        if (!sessions.isEmpty()) {
            return sessions.values().iterator().next().game;
        }
        return null;
    }
}

