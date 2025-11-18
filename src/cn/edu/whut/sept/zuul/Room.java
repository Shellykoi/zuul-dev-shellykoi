/**
 * 该类表示游戏中的一个房间。
 * 每个房间都有一个描述和可以通向其他房间的出口。
 * 出口使用方向（如"north"、"east"等）作为键，对应的房间对象作为值存储在HashMap中。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.Set;
import java.util.HashMap;

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
     * 创建一个房间对象，使用给定的描述信息初始化。
     * 
     * @param description 房间的描述信息
     */
    public Room(String description)
    {
        this.description = description;
        exits = new HashMap<>();
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
     * 返回房间的详细描述，包括房间描述和所有可用的出口信息。
     * 
     * @return 包含房间描述和出口信息的完整字符串
     */
    public String getLongDescription()
    {
        return "You are " + description + ".\n" + getExitString();
    }

    /**
     * 生成并返回包含所有可用出口的字符串。
     * 格式为："Exits: direction1 direction2 ..."
     * 
     * @return 包含所有出口方向的字符串
     */
    private String getExitString()
    {
        String returnString = "Exits:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
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
}


