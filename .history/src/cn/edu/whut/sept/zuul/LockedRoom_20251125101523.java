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
        // 获取基础描述（包括出口信息）
        String baseDescription = super.getLongDescription();
        
        if (!isUnlocked) {
            // 房间未解锁时，添加锁定提示
            // 从基础描述中提取出口信息（从"出口:"开始的部分）
            String exitInfo = "";
            if (baseDescription.contains("出口:")) {
                int exitIndex = baseDescription.indexOf("出口:");
                exitInfo = baseDescription.substring(exitIndex);
            } else {
                // 如果没有找到"出口:"，尝试构建出口信息
                // 获取所有出口方向
                java.util.Set<String> exitDirections = getExitsSet();
                if (!exitDirections.isEmpty()) {
                    exitInfo = "出口:";
                    for (String dir : exitDirections) {
                        exitInfo += " " + translateDirection(dir);
                    }
                }
            }
            
            return "你在" + getShortDescription() + "。\n" +
                   "提示：这扇门是锁着的！你需要使用 " + requiredKeyType + " 来解锁。" +
                   "\n提示：输入 'use key' 命令可以解锁上锁的房间，然后才能进入拾取宝藏！\n" +
                   (exitInfo.isEmpty() ? "" : exitInfo);
        }
        return baseDescription;
    }
    
    /**
     * 获取所有出口方向的集合（用于构建出口信息）
     * 这个方法访问父类的私有字段，需要通过反射或公开方法来实现
     * 作为临时方案，我们可以通过尝试所有方向来获取出口
     */
    private java.util.Set<String> getExitsSet() {
        java.util.Set<String> exits = new java.util.HashSet<>();
        String[] directions = {"north", "south", "east", "west"};
        for (String dir : directions) {
            if (getExit(dir) != null) {
                exits.add(dir);
            }
        }
        return exits;
    }
    
    /**
     * 将方向翻译为中文（与Room类中的方法保持一致）
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

