/**
 * Room类的单元测试用例。
 * 测试房间的物品管理、出口管理等功能。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * Room类的单元测试。
 */
public class RoomTest
{
    /**
     * 运行所有Room类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("Room类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 房间创建
        if (testRoomCreation()) {
            System.out.println("✅ 测试1: 房间创建 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 房间创建 - 失败");
            failed++;
        }
        
        // 测试用例2: 物品添加和移除
        if (testAddAndRemoveItem()) {
            System.out.println("✅ 测试2: 物品添加和移除 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 物品添加和移除 - 失败");
            failed++;
        }
        
        // 测试用例3: 物品查找（大小写不敏感）
        if (testGetItemCaseInsensitive()) {
            System.out.println("✅ 测试3: 物品查找（大小写不敏感） - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 物品查找（大小写不敏感） - 失败");
            failed++;
        }
        
        // 测试用例4: 房间总重量计算
        if (testTotalWeight()) {
            System.out.println("✅ 测试4: 房间总重量计算 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 房间总重量计算 - 失败");
            failed++;
        }
        
        // 测试用例5: 出口管理
        if (testExits()) {
            System.out.println("✅ 测试5: 出口管理 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试5: 出口管理 - 失败");
            failed++;
        }
        
        // 测试用例6: 房间描述
        if (testRoomDescription()) {
            System.out.println("✅ 测试6: 房间描述 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试6: 房间描述 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 房间创建。
     */
    private static boolean testRoomCreation()
    {
        try {
            Room room = new Room("测试房间");
            
            if (!room.getShortDescription().equals("测试房间")) {
                System.out.println("  错误: 房间描述不匹配");
                return false;
            }
            
            if (room.getItems().size() != 0) {
                System.out.println("  错误: 新房间不应该有物品");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例2: 物品添加和移除。
     */
    private static boolean testAddAndRemoveItem()
    {
        try {
            Room room = new Room("测试房间");
            Item item = new Item("test", "测试物品", 1.0);
            
            // 添加物品
            room.addItem(item);
            if (room.getItems().size() != 1) {
                System.out.println("  错误: 添加物品后数量不正确");
                return false;
            }
            
            // 移除物品
            Item removed = room.removeItem("test");
            if (removed == null) {
                System.out.println("  错误: 移除物品返回null");
                return false;
            }
            
            if (!removed.getName().equals("test")) {
                System.out.println("  错误: 移除的物品名称不匹配");
                return false;
            }
            
            if (room.getItems().size() != 0) {
                System.out.println("  错误: 移除物品后房间还有物品");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 物品查找（大小写不敏感）。
     */
    private static boolean testGetItemCaseInsensitive()
    {
        try {
            Room room = new Room("测试房间");
            Item item = new Item("Key", "钥匙", 0.1);
            room.addItem(item);
            
            // 测试不同大小写的查找
            Item found1 = room.getItem("key");
            Item found2 = room.getItem("KEY");
            Item found3 = room.getItem("Key");
            
            if (found1 == null || found2 == null || found3 == null) {
                System.out.println("  错误: 大小写不敏感查找失败");
                return false;
            }
            
            if (!found1.getName().equals("Key") || 
                !found2.getName().equals("Key") || 
                !found3.getName().equals("Key")) {
                System.out.println("  错误: 找到的物品不正确");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: 房间总重量计算。
     */
    private static boolean testTotalWeight()
    {
        try {
            Room room = new Room("测试房间");
            room.addItem(new Item("item1", "物品1", 1.0));
            room.addItem(new Item("item2", "物品2", 2.0));
            room.addItem(new Item("item3", "物品3", 0.5));
            
            double totalWeight = room.getTotalWeight();
            double expected = 3.5;
            
            if (Math.abs(totalWeight - expected) > 0.001) {
                System.out.println("  错误: 总重量计算不正确。期望: " + expected + 
                                 ", 实际: " + totalWeight);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例5: 出口管理。
     */
    private static boolean testExits()
    {
        try {
            Room room1 = new Room("房间1");
            Room room2 = new Room("房间2");
            
            // 设置出口
            room1.setExit("north", room2);
            Room exit = room1.getExit("north");
            
            if (exit == null) {
                System.out.println("  错误: 无法获取出口");
                return false;
            }
            
            if (!exit.getShortDescription().equals("房间2")) {
                System.out.println("  错误: 出口房间不正确");
                return false;
            }
            
            // 测试不存在的出口
            Room noExit = room1.getExit("south");
            if (noExit != null) {
                System.out.println("  错误: 不存在的出口应该返回null");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例6: 房间描述。
     */
    private static boolean testRoomDescription()
    {
        try {
            Room room = new Room("测试房间");
            room.addItem(new Item("item1", "物品1", 1.0));
            
            String shortDesc = room.getShortDescription();
            String longDesc = room.getLongDescription();
            
            if (shortDesc == null || shortDesc.isEmpty()) {
                System.out.println("  错误: 简短描述为空");
                return false;
            }
            
            if (longDesc == null || longDesc.isEmpty()) {
                System.out.println("  错误: 详细描述为空");
                return false;
            }
            
            if (!longDesc.contains(shortDesc)) {
                System.out.println("  错误: 详细描述应该包含简短描述");
                return false;
            }
            
            // 测试包含物品的描述
            if (!longDesc.contains("物品1") || !longDesc.contains("item1")) {
                System.out.println("  错误: 详细描述应该包含物品信息");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

