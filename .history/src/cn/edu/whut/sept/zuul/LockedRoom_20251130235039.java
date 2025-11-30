/**
 * 该类表示一个上锁的房间。
 * 玩家需要使用特定的钥匙才能进入。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

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
     * 重写getExit方法。
     * 注意：这个方法在玩家"离开"当前房间时调用，而不是"进入"时调用。
     * 锁只阻止从外部进入，不阻止从内部离开。
     * 所以这个方法应该总是返回出口（允许玩家离开）。
     * 
     * @param direction 方向
     * @return 该方向的出口房间
     */
    @Override
    public Room getExit(String direction)
    {
        // 锁只阻止进入，不阻止离开，所以总是返回出口
        return super.getExit(direction);
    }
    
    /**
     * 重写getLongDescription方法，显示房间锁定状态。
     * 即使房间未解锁，也显示出口信息（允许玩家离开）。
     * 
     * @return 房间描述，包含出口信息
     */
    @Override
    public String getLongDescription()
    {
        // 构建出口信息字符串
        String exitString = buildExitString();
        
        // 构建物品信息字符串
        String itemsString = getItemsString();
        
        if (!isUnlocked) {
            // 房间未解锁时，显示锁定提示和出口信息
            String result = "你在" + getShortDescription() + "。\n" +
                           "提示：这扇门是锁着的！你需要使用 " + requiredKeyType + " 来解锁。\n" +
                           "提示：输入 'use key' 命令可以解锁上锁的房间，然后才能进入拾取宝藏！";
            
            // 添加出口信息（如果有）
            if (!exitString.isEmpty()) {
                result += "\n" + exitString;
            }
            
            // 添加物品信息（如果有）
            if (!itemsString.isEmpty() && !itemsString.contains("没有物品")) {
                result += "\n" + itemsString;
            }
            
            return result;
        }
        
        // 房间已解锁，使用父类的标准描述
        return super.getLongDescription();
    }
    
    /**
     * 构建出口信息字符串。
     * 通过检查每个方向是否有出口来构建。
     * 
     * @return 出口信息字符串，格式为"出口: 北 南 ..."
     */
    private String buildExitString() {
        String returnString = "出口:";
        String[] directions = {"north", "south", "east", "west"};
        boolean hasAnyExit = false;
        
        for (String dir : directions) {
            if (getExit(dir) != null) {
                returnString += " " + translateDirection(dir);
                hasAnyExit = true;
            }
        }
        
        // 如果没有出口，返回空字符串
        return hasAnyExit ? returnString : "";
    }
    
    /**
     * 将方向英文名称翻译为中文。
     * 
     * <p>此方法用于在房间描述中显示中文方向信息，提升用户体验。
     * 支持的方向：north(北)、south(南)、east(东)、west(西)。
     * 
     * @param direction 方向的英文名称（不区分大小写）
     * @return 对应的中文方向名称，如果方向未知则返回原始字符串
     * @see Room#getLongDescription()
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
}

