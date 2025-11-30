/**
 * 该类表示游戏中的一个房间。
 * 每个房间都有一个描述和可以通向其他房间的出口。
 * 出口使用方向（如"north"、"east"等）作为键，对应的房间对象作为值存储在HashMap中。
 * 房间还可以存储任意数量的物品。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 2.0 (扩展物品存储功能)
 */
package cn.edu.whut.sept.zuul;

import java.util.Set;
import java.util.HashMap;
import java.util.Collection;

public class Room
{
    /**
     * 房间的描述信息。
     */
    private String description;
    
    /**
     * 存储房间出口的HashMap，键为方向字符串，值为对应的房间对象。
     */
    private HashMap<String, Room> exits;
    
    /**
     * 存储房间内物品的HashMap，键为物品名称，值为物品对象。
     */
    private HashMap<String, Item> items;

    /**
     * 创建一个房间对象，使用给定的描述信息初始化。
     * 
     * @param description 房间的描述信息
     */
    public Room(String description)
    {
        this.description = description;
        exits = new HashMap<>();
        items = new HashMap<>();
    }

    /**
     * 设置房间在指定方向的出口，连接到另一个房间。
     * 
     * @param direction 出口的方向（如"north"、"east"等）
     * @param neighbor 该方向连接的相邻房间对象
     */
    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    /**
     * 返回房间的简短描述。
     * 
     * @return 房间的描述字符串
     */
    public String getShortDescription()
    {
        return description;
    }

    /**
     * 返回房间的详细描述，包括房间描述、所有可用的出口信息和房间内的物品信息。
     * 
     * @return 包含房间描述、出口信息和物品信息的完整字符串
     */
    public String getLongDescription()
    {
        String returnString = "你在" + description + "。\n" + getExitString();
        String itemsString = getItemsString();
        if (!itemsString.isEmpty()) {
            returnString += "\n" + itemsString;
        }
        return returnString;
    }

    /**
     * 生成并返回包含所有可用出口的字符串。
     * 格式为："Exits: direction1 direction2 ..."
     * 
     * @return 包含所有出口方向的字符串
     */
    private String getExitString()
    {
        String returnString = "出口:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            // 将方向翻译为中文
            String directionCN = translateDirection(exit);
            returnString += " " + directionCN;
        }
        return returnString;
    }
    
    /**
     * 将方向翻译为中文
     */
    private String translateDirection(String direction) {
        switch(direction.toLowerCase()) {
            case "north": return "北";
            case "south": return "南";
            case "east": return "东";
            case "west": return "西";
            default: return direction;
        }
    }

    /**
     * 根据给定的方向获取对应的房间对象。
     * 
     * @param direction 要查询的方向
     * @return 该方向对应的房间对象，如果该方向没有出口则返回null
     */
    public Room getExit(String direction)
    {
        return exits.get(direction);
    }
    
    /**
     * 检查指定方向是否有出口（即使出口是上锁的房间也返回true）。
     * 这个方法用于检查是否有LockedRoom类型的出口。
     * 
     * @param direction 要查询的方向
     * @return 如果该方向有出口返回true，否则返回false
     */
    public boolean hasExit(String direction)
    {
        return exits.containsKey(direction);
    }
    
    /**
     * 获取指定方向的出口房间（不检查是否上锁）。
     * 这个方法用于GoCommand检查LockedRoom。
     * 
     * @param direction 要查询的方向
     * @return 该方向对应的房间对象，如果该方向没有出口则返回null
     */
    public Room getExitDirectly(String direction)
    {
        return exits.get(direction);
    }
    
    /**
     * 向房间中添加一个物品。
     * 
     * @param item 要添加的物品对象
     */
    public void addItem(Item item)
    {
        items.put(item.getName().toLowerCase(), item);
    }
    
    /**
     * 从房间中移除指定名称的物品。
     * 
     * @param itemName 要移除的物品名称
     * @return 被移除的物品对象，如果物品不存在则返回null
     */
    public Item removeItem(String itemName)
    {
        return items.remove(itemName.toLowerCase());
    }
    
    /**
     * 根据物品名称获取房间中的物品。
     * 
     * @param itemName 物品名称
     * @return 物品对象，如果不存在则返回null
     */
    public Item getItem(String itemName)
    {
        return items.get(itemName.toLowerCase());
    }
    
    /**
     * 获取房间中所有物品的集合。
     * 
     * @return 房间中所有物品的集合
     */
    public Collection<Item> getItems()
    {
        return items.values();
    }
    
    /**
     * 生成并返回包含房间内所有物品的字符串。
     * 格式为："Items: item1 item2 ..."
     * 
     * @return 包含所有物品信息的字符串，如果房间没有物品则返回空字符串
     */
    public String getItemsString()
    {
        if (items.isEmpty()) {
            return "当前房间没有物品！";
        }
        String returnString = "物品:";
        for (Item item : items.values()) {
            returnString += "\n  " + item.toString();
        }
        return returnString;
    }
    
    /**
     * 计算房间内所有物品的总重量。
     * 
     * @return 房间内物品的总重量
     */
    public double getTotalWeight()
    {
        double total = 0.0;
        for (Item item : items.values()) {
            total += item.getWeight();
        }
        return total;
    }
}


