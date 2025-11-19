/**
 * 该类表示游戏中的玩家。
 * 玩家具有姓名、当前位置、物品清单和负重限制等属性。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Player
{
    /**
     * 玩家的数据库用户ID（用于多人游戏）。
     */
    private Integer userId;
    
    /**
     * 玩家的姓名。
     */
    private String name;
    
    /**
     * 玩家当前所在的房间。
     */
    private Room currentRoom;
    
    /**
     * 玩家随身携带的物品清单，键为物品名称，值为物品对象。
     */
    private HashMap<String, Item> inventory;
    
    /**
     * 玩家可以携带的最大重量（单位：千克）。
     */
    private double maxWeight;
    
    /**
     * 玩家已访问过的房间名称集合（用于通关检测）。
     */
    private Set<String> roomsVisited;
    
    /**
     * 玩家已收集过的物品名称集合（用于通关检测）。
     */
    private Set<String> itemsCollected;
    
    /**
     * 玩家是否已吃掉魔法饼干。
     */
    private boolean cookieEaten;
    
    /**
     * 当前游戏记录ID（用于保存游戏记录）。
     */
    private Integer gameRecordId;

    /**
     * 创建一个玩家对象。
     * 
     * @param name 玩家的姓名
     * @param maxWeight 玩家的最大负重（单位：千克）
     */
    public Player(String name, double maxWeight)
    {
        this.name = name;
        this.maxWeight = maxWeight;
        this.inventory = new HashMap<>();
        this.roomsVisited = new HashSet<>();
        this.itemsCollected = new HashSet<>();
        this.cookieEaten = false;
        this.userId = null;
        this.gameRecordId = null;
    }
    
    /**
     * 获取玩家的数据库用户ID。
     * 
     * @return 用户ID，如果未登录则返回null
     */
    public Integer getUserId()
    {
        return userId;
    }
    
    /**
     * 设置玩家的数据库用户ID。
     * 
     * @param userId 用户ID
     */
    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }
    
    /**
     * 获取当前游戏记录ID。
     * 
     * @return 游戏记录ID
     */
    public Integer getGameRecordId()
    {
        return gameRecordId;
    }
    
    /**
     * 设置当前游戏记录ID。
     * 
     * @param gameRecordId 游戏记录ID
     */
    public void setGameRecordId(Integer gameRecordId)
    {
        this.gameRecordId = gameRecordId;
    }

    /**
     * 获取玩家的姓名。
     * 
     * @return 玩家的姓名
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * 设置玩家的姓名。
     * 
     * @param name 玩家的姓名
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 获取玩家当前所在的房间。
     * 
     * @return 玩家当前所在的房间对象
     */
    public Room getCurrentRoom()
    {
        return currentRoom;
    }

    /**
     * 设置玩家当前所在的房间。
     * 同时记录房间访问历史。
     * 
     * @param room 要设置的房间对象
     */
    public void setCurrentRoom(Room room)
    {
        this.currentRoom = room;
        if (room != null) {
            // 记录房间访问（使用房间描述作为唯一标识）
            roomsVisited.add(room.getShortDescription());
        }
    }

    /**
     * 获取玩家的最大负重。
     * 
     * @return 玩家的最大负重（单位：千克）
     */
    public double getMaxWeight()
    {
        return maxWeight;
    }

    /**
     * 设置玩家的最大负重。
     * 
     * @param maxWeight 新的最大负重值（单位：千克）
     */
    public void setMaxWeight(double maxWeight)
    {
        this.maxWeight = maxWeight;
    }

    /**
     * 增加玩家的最大负重。
     * 
     * @param amount 要增加的重量值（单位：千克）
     */
    public void increaseMaxWeight(double amount)
    {
        this.maxWeight += amount;
    }

    /**
     * 计算玩家当前携带物品的总重量。
     * 
     * @return 当前携带物品的总重量（单位：千克）
     */
    public double getTotalWeight()
    {
        double total = 0.0;
        for (Item item : inventory.values()) {
            total += item.getWeight();
        }
        return total;
    }

    /**
     * 检查玩家是否可以携带指定重量的物品。
     * 
     * @param itemWeight 要携带的物品重量（单位：千克）
     * @return 如果可以携带返回true，否则返回false
     */
    public boolean canCarry(double itemWeight)
    {
        return (getTotalWeight() + itemWeight) <= maxWeight;
    }

    /**
     * 检查玩家是否可以携带指定的物品。
     * 
     * @param item 要携带的物品对象
     * @return 如果可以携带返回true，否则返回false
     */
    public boolean canCarry(Item item)
    {
        return canCarry(item.getWeight());
    }

    /**
     * 玩家拾取一个物品。
     * 如果物品重量超过负重限制，则拾取失败。
     * 
     * @param item 要拾取的物品对象
     * @return 如果拾取成功返回true，否则返回false
     */
    public boolean takeItem(Item item)
    {
        if (item == null) {
            return false;
        }
        if (!canCarry(item)) {
            return false;
        }
        inventory.put(item.getName().toLowerCase(), item);
        // 记录物品收集
        itemsCollected.add(item.getName().toLowerCase());
        return true;
    }

    /**
     * 玩家丢弃一个物品。
     * 
     * @param itemName 要丢弃的物品名称
     * @return 被丢弃的物品对象，如果物品不存在则返回null
     */
    public Item dropItem(String itemName)
    {
        return inventory.remove(itemName.toLowerCase());
    }

    /**
     * 玩家丢弃所有物品。
     * 
     * @return 被丢弃的所有物品的集合
     */
    public Collection<Item> dropAllItems()
    {
        Collection<Item> droppedItems = inventory.values();
        inventory.clear();
        return droppedItems;
    }

    /**
     * 根据物品名称获取玩家携带的物品。
     * 
     * @param itemName 物品名称
     * @return 物品对象，如果不存在则返回null
     */
    public Item getItem(String itemName)
    {
        return inventory.get(itemName.toLowerCase());
    }

    /**
     * 获取玩家携带的所有物品的集合。
     * 
     * @return 玩家携带的所有物品的集合
     */
    public Collection<Item> getInventory()
    {
        return inventory.values();
    }

    /**
     * 检查玩家是否携带了指定名称的物品。
     * 
     * @param itemName 物品名称
     * @return 如果携带了该物品返回true，否则返回false
     */
    public boolean hasItem(String itemName)
    {
        return inventory.containsKey(itemName.toLowerCase());
    }

    /**
     * 生成并返回玩家物品清单的字符串表示。
     * 
     * @return 包含所有物品信息的字符串
     */
    public String getInventoryString()
    {
        if (inventory.isEmpty()) {
            return "你没有携带任何物品。";
        }
        String returnString = "你携带的物品:";
        for (Item item : inventory.values()) {
            returnString += "\n  " + item.toString();
        }
        returnString += "\n总重量: " + String.format("%.2f", getTotalWeight()) + 
                       "kg / " + String.format("%.2f", maxWeight) + "kg";
        return returnString;
    }
    
    /**
     * 获取玩家已访问的房间集合。
     * 
     * @return 已访问的房间名称集合
     */
    public Set<String> getRoomsVisited()
    {
        return new HashSet<>(roomsVisited);
    }
    
    /**
     * 设置玩家已访问的房间集合（用于加载游戏状态）。
     * 
     * @param roomsVisited 已访问的房间名称集合
     */
    public void setRoomsVisited(Set<String> roomsVisited)
    {
        this.roomsVisited = new HashSet<>(roomsVisited);
    }
    
    /**
     * 获取玩家已收集的物品集合。
     * 
     * @return 已收集的物品名称集合
     */
    public Set<String> getItemsCollected()
    {
        return new HashSet<>(itemsCollected);
    }
    
    /**
     * 设置玩家已收集的物品集合（用于加载游戏状态）。
     * 
     * @param itemsCollected 已收集的物品名称集合
     */
    public void setItemsCollected(Set<String> itemsCollected)
    {
        this.itemsCollected = new HashSet<>(itemsCollected);
    }
    
    /**
     * 检查玩家是否已吃掉魔法饼干。
     * 
     * @return 如果已吃掉返回true，否则返回false
     */
    public boolean isCookieEaten()
    {
        return cookieEaten;
    }
    
    /**
     * 设置玩家是否已吃掉魔法饼干。
     * 
     * @param cookieEaten 是否已吃掉
     */
    public void setCookieEaten(boolean cookieEaten)
    {
        this.cookieEaten = cookieEaten;
    }
    
    /**
     * 标记玩家已吃掉魔法饼干。
     */
    public void eatCookie()
    {
        this.cookieEaten = true;
        // 从物品清单中移除cookie（如果存在）
        dropItem("cookie");
    }
}

