/**
 * 游戏状态管理类
 * 负责保存和加载游戏状态到数据库
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameStateManager {
    private DatabaseManager dbManager;
    private Game game;
    
    /**
     * 创建游戏状态管理器
     */
    public GameStateManager(Game game) {
        this.game = game;
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * 保存当前游戏状态到数据库
     */
    public boolean saveGameState() {
        Player player = game.getPlayer();
        
        if (player.getUserId() == null) {
            return false; // 未登录用户无法保存
        }
        
        // 获取当前房间名称
        Room currentRoom = player.getCurrentRoom();
        String currentRoomName = currentRoom != null ? currentRoom.getShortDescription() : "大学主入口外";
        
        // 获取物品清单
        List<String> inventory = new ArrayList<>();
        for (Item item : player.getInventory()) {
            inventory.add(item.getName());
        }
        
        // 获取已访问的房间
        List<String> roomsVisited = new ArrayList<>(player.getRoomsVisited());
        
        // 获取已收集的物品
        List<String> itemsCollected = new ArrayList<>(player.getItemsCollected());
        
        // 保存到数据库
        return dbManager.savePlayerState(
            player.getUserId(),
            currentRoomName,
            player.getMaxWeight(),
            inventory,
            roomsVisited,
            itemsCollected,
            player.isCookieEaten()
        );
    }
    
    /**
     * 从数据库加载游戏状态
     */
    public boolean loadGameState() {
        Player player = game.getPlayer();
        
        if (player.getUserId() == null) {
            return false; // 未登录用户无法加载
        }
        
        // 从数据库加载状态
        Map<String, Object> state = dbManager.loadPlayerState(player.getUserId());
        
        if (state == null) {
            return false; // 没有保存的状态
        }
        
        // 恢复房间位置
        String currentRoomName = (String) state.get("currentRoom");
        Room targetRoom = game.getRoomByName(currentRoomName);
        if (targetRoom != null) {
            player.setCurrentRoom(targetRoom);
        }
        
        // 恢复最大负重
        player.setMaxWeight((Double) state.get("maxWeight"));
        
        // 恢复已访问的房间
        @SuppressWarnings("unchecked")
        List<String> roomsVisited = (List<String>) state.get("roomsVisited");
        if (roomsVisited != null) {
            player.setRoomsVisited(Set.copyOf(roomsVisited));
        }
        
        // 恢复已收集的物品
        @SuppressWarnings("unchecked")
        List<String> itemsCollected = (List<String>) state.get("itemsCollected");
        if (itemsCollected != null) {
            player.setItemsCollected(Set.copyOf(itemsCollected));
        }
        
        // 恢复饼干状态
        player.setCookieEaten((Boolean) state.get("cookieEaten"));
        
        // 恢复物品清单（需要从房间中重新拾取，因为物品在房间中）
        @SuppressWarnings("unchecked")
        List<String> inventory = (List<String>) state.get("inventory");
        if (inventory != null) {
            // 注意：物品需要从当前房间或初始房间中重新拾取
            // 这里只记录已收集的物品，实际物品需要玩家重新拾取
            // 或者可以从所有房间中查找并拾取
            restoreInventory(inventory);
        }
        
        return true;
    }
    
    /**
     * 恢复物品清单（从所有房间中查找并拾取）
     */
    private void restoreInventory(List<String> itemNames) {
        Player player = game.getPlayer();
        
        // 遍历所有房间，查找物品并拾取
        for (String itemName : itemNames) {
            Item item = game.findItemInAllRooms(itemName);
            if (item != null && player.canCarry(item)) {
                // 从房间移除并添加到玩家背包
                Room itemRoom = game.findItemRoom(itemName);
                if (itemRoom != null) {
                    itemRoom.removeItem(itemName);
                    player.takeItem(item);
                }
            }
        }
    }
}

