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

public class Player
{
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
     * 
     * @param room 要设置的房间对象
     */
    public void setCurrentRoom(Room room)
    {
        this.currentRoom = room;
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
}

