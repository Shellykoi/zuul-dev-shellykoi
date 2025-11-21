/**
 * Item类的单元测试用例。
 * 测试物品的创建、属性获取、toString方法等功能。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * Item类的单元测试。
 * 由于项目没有使用Maven/Gradle，这里使用简单的测试框架。
 */
public class ItemTest
{
    /**
     * 运行所有Item类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("Item类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 物品创建
        if (testItemCreation()) {
            System.out.println("✅ 测试1: 物品创建 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 物品创建 - 失败");
            failed++;
        }
        
        // 测试用例2: 属性获取
        if (testItemProperties()) {
            System.out.println("✅ 测试2: 属性获取 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 属性获取 - 失败");
            failed++;
        }
        
        // 测试用例3: toString方法
        if (testItemToString()) {
            System.out.println("✅ 测试3: toString方法 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: toString方法 - 失败");
            failed++;
        }
        
        // 测试用例4: 边界情况 - 零重量
        if (testZeroWeight()) {
            System.out.println("✅ 测试4: 零重量物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 零重量物品 - 失败");
            failed++;
        }
        
        // 测试用例5: 边界情况 - 大重量
        if (testLargeWeight()) {
            System.out.println("✅ 测试5: 大重量物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试5: 大重量物品 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 物品创建。
     * 验证物品创建后属性是否正确设置。
     */
    private static boolean testItemCreation()
    {
        try {
            Item item = new Item("key", "一把钥匙", 0.1);
            
            if (!item.getName().equals("key")) {
                System.out.println("  错误: 物品名称不匹配");
                return false;
            }
            
            if (!item.getDescription().equals("一把钥匙")) {
                System.out.println("  错误: 物品描述不匹配");
                return false;
            }
            
            if (Math.abs(item.getWeight() - 0.1) > 0.001) {
                System.out.println("  错误: 物品重量不匹配");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例2: 属性获取。
     * 验证物品的所有getter方法。
     */
    private static boolean testItemProperties()
    {
        try {
            Item item = new Item("map", "一张地图", 0.2);
            
            String name = item.getName();
            String description = item.getDescription();
            double weight = item.getWeight();
            
            if (name == null || name.isEmpty()) {
                System.out.println("  错误: 物品名称为空");
                return false;
            }
            
            if (description == null || description.isEmpty()) {
                System.out.println("  错误: 物品描述为空");
                return false;
            }
            
            if (weight < 0) {
                System.out.println("  错误: 物品重量为负数");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: toString方法。
     * 验证toString方法返回正确的字符串格式。
     */
    private static boolean testItemToString()
    {
        try {
            Item item = new Item("sword", "一把剑", 2.5);
            String str = item.toString();
            
            if (str == null || str.isEmpty()) {
                System.out.println("  错误: toString返回空字符串");
                return false;
            }
            
            if (!str.contains("sword")) {
                System.out.println("  错误: toString不包含物品名称");
                return false;
            }
            
            if (!str.contains("一把剑")) {
                System.out.println("  错误: toString不包含物品描述");
                return false;
            }
            
            if (!str.contains("2.5")) {
                System.out.println("  错误: toString不包含物品重量");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: 边界情况 - 零重量物品。
     */
    private static boolean testZeroWeight()
    {
        try {
            Item item = new Item("feather", "一根羽毛", 0.0);
            
            if (Math.abs(item.getWeight() - 0.0) > 0.001) {
                System.out.println("  错误: 零重量物品测试失败");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例5: 边界情况 - 大重量物品。
     */
    private static boolean testLargeWeight()
    {
        try {
            Item item = new Item("rock", "一块巨石", 1000.0);
            
            if (Math.abs(item.getWeight() - 1000.0) > 0.001) {
                System.out.println("  错误: 大重量物品测试失败");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

