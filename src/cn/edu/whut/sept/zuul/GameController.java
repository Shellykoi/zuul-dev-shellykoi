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
    private Game game;
    private Player player;
    private Parser parser;
    
    /**
     * 创建游戏控制器
     */
    public GameController() {
        game = new Game();
        player = game.getPlayer();
        parser = game.getParser();
    }
    
    /**
     * 执行游戏命令
     * 
     * @param commandString 命令字符串
     * @return 包含执行结果的Map
     */
    public Map<String, Object> executeCommand(String commandString) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 处理quit命令
            if (commandString.trim().equalsIgnoreCase("quit")) {
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
            String output = captureCommandOutput(command);
            
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
    private String captureCommandOutput(Command command) {
        StringBuilder output = new StringBuilder();
        
        String commandWord = command.getCommandWord();
        Player player = game.getPlayer();
        
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
     * 获取游戏状态
     * 
     * @return 包含游戏状态的Map
     */
    public Map<String, Object> getGameStatus() {
        Map<String, Object> status = new HashMap<>();
        
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
        
        return status;
    }
    
    /**
     * 获取Game实例（用于命令执行器）
     */
    public Game getGame() {
        return game;
    }
}

