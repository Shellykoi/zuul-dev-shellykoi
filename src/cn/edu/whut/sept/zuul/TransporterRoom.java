/**
 * 传输房间类。
 * 这是一个特殊的房间，每当玩家进入这个房间，就会被随机传输到另一个房间。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

import java.util.Random;
import java.util.HashMap;
import java.util.Set;

public class TransporterRoom extends Room
{
    /**
     * 所有可传输到的房间列表。
     */
    private HashMap<String, Room> allRooms;
    
    /**
     * 随机数生成器。
     */
    private Random random;

    /**
     * 创建一个传输房间对象。
     * 
     * @param description 房间的描述信息
     * @param allRooms 所有可传输到的房间的映射表
     */
    public TransporterRoom(String description, HashMap<String, Room> allRooms)
    {
        super(description);
        this.allRooms = allRooms;
        this.random = new Random();
    }

    /**
     * 重写getExit方法，实现随机传输功能。
     * 当玩家尝试离开传输房间时，会被随机传送到另一个房间。
     * 
     * @param direction 方向（在此类中会被忽略，因为总是随机传输）
     * @return 随机选择的房间对象
     */
    @Override
    public Room getExit(String direction)
    {
        // 获取所有可传输的房间
        Set<String> roomKeys = allRooms.keySet();
        if (roomKeys.isEmpty()) {
            return null;
        }
        
        // 随机选择一个房间（排除当前传输房间本身）
        String[] keys = roomKeys.toArray(new String[0]);
        Room randomRoom;
        do {
            String randomKey = keys[random.nextInt(keys.length)];
            randomRoom = allRooms.get(randomKey);
        } while (randomRoom == this && keys.length > 1);  // 避免传送到自己（除非只有一个房间）
        
        return randomRoom;
    }
    
    /**
     * 获取随机房间（不通过方向）。
     * 这个方法可以直接获取随机房间，用于玩家进入传输房间时自动传输。
     * 
     * @return 随机选择的房间对象
     */
    public Room getRandomRoom()
    {
        Set<String> roomKeys = allRooms.keySet();
        if (roomKeys.isEmpty()) {
            return null;
        }
        
        String[] keys = roomKeys.toArray(new String[0]);
        Room randomRoom;
        do {
            String randomKey = keys[random.nextInt(keys.length)];
            randomRoom = allRooms.get(randomKey);
        } while (randomRoom == this && keys.length > 1);
        
        return randomRoom;
    }
}

