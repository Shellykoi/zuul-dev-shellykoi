/**
 * 该类表示游戏中的一个物品。
 * 每个物品都有一个名称、描述和重量值。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class Item
{
    /**
     * 物品的名称。
     */
    private String name;
    
    /**
     * 物品的描述信息。
     */
    private String description;
    
    /**
     * 物品的重量值（单位：千克）。
     */
    private double weight;
    
    /**
     * 物品类型（如：KEY, MAP, FOOD, TOOL等）。
     */
    private String itemType;
    
    /**
     * 物品是否可以使用。
     */
    private boolean usable;
    
    /**
     * 物品使用效果描述。
     */
    private String useEffect;

    /**
     * 创建一个物品对象。
     * 
     * @param name 物品的名称
     * @param description 物品的描述信息
     * @param weight 物品的重量值
     */
    public Item(String name, String description, double weight)
    {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.itemType = "NORMAL";
        this.usable = false;
        this.useEffect = "";
    }
    
    /**
     * 创建一个可使用的物品对象。
     * 
     * @param name 物品的名称
     * @param description 物品的描述信息
     * @param weight 物品的重量值
     * @param itemType 物品类型
     * @param useEffect 使用效果描述
     */
    public Item(String name, String description, double weight, String itemType, String useEffect)
    {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.itemType = itemType;
        this.usable = true;
        this.useEffect = useEffect;
    }

    /**
     * 获取物品的名称。
     * 
     * @return 物品的名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 获取物品的描述信息。
     * 
     * @return 物品的描述信息
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * 获取物品的重量值。
     * 
     * @return 物品的重量值
     */
    public double getWeight()
    {
        return weight;
    }

    /**
     * 返回物品的字符串表示。
     * 格式为："名称 (描述) - 重量kg"
     * 
     * @return 物品的字符串描述
     */
    @Override
    public String toString()
    {
        return name + " (" + description + ") - " + weight + "kg";
    }
}

