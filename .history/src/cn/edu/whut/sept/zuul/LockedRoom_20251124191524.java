/**
 * 该类表示一个上锁的房间。
 * 玩家需要使用特定的钥匙才能进入。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

import java.util.HashMap;

public class LockedRoom extends Room
{
    /**
     * 房间是否已解锁。
     */
    private boolean isUnlocked;
    
    /**
     * 解锁房间所需的钥匙类型。
     */
    private String requiredKeyType;
    
    /**
     * 创建一个上锁的房间。
     * 
     * @param description 房间描述
     * @param requiredKeyType 解锁所需的钥匙类型（如"key"）
     */
    public LockedRoom(String description, String requiredKeyType)
    {
        super(description);
        this.isUnlocked = false;
        this.requiredKeyType = requiredKeyType;
    }
    
    /**
     * 尝试使用钥匙解锁房间。
     * 
     * @param keyType 钥匙类型
     * @return 如果解锁成功返回true，否则返回false
     */
    public boolean unlock(String keyType)
    {
        if (keyType != null && keyType.equalsIgnoreCase(requiredKeyType)) {
            isUnlocked = true;
            return true;
        }
        return false;
    }
    
    /**
     * 检查房间是否已解锁。
     * 
     * @return 如果已解锁返回true，否则返回false
     */
    public boolean isUnlocked()
    {
        return isUnlocked;
    }
    
    /**
     * 获取解锁所需的钥匙类型。
     * 
     * @return 钥匙类型
     */
    public String getRequiredKeyType()
    {
        return requiredKeyType;
    }
    
    /**
     * 重写getExit方法，检查房间是否已解锁。
     * 如果房间未解锁，返回null（阻止进入）。
     * 
     * @param direction 方向
     * @return 如果房间已解锁，返回出口房间；否则返回null
     */
    @Override
    public Room getExit(String direction)
    {
        if (!isUnlocked) {
            return null; // 房间未解锁，无法进入
        }
        return super.getExit(direction);
    }
    
    /**
     * 重写getLongDescription方法，显示房间锁定状态。
     * 
     * @return 房间描述
     */
    @Override
    public String getLongDescription()
    {
        if (!isUnlocked) {
            return "你在" + getShortDescription() + "。\n" +
                   "这扇门是锁着的！你需要使用 " + requiredKeyType + " 来解锁。";
        }
        return super.getLongDescription();
    }
}

