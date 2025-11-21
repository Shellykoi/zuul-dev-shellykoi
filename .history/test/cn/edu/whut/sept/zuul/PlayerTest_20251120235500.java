/**
 * Player类的单元测试用例。
 * 测试玩家的物品管理、负重检查等功能。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * Player类的单元测试。
 */
public class PlayerTest
{
    /**
     * 运行所有Player类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("Player类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 玩家创建和初始化
        if (testPlayerCreation()) {
            System.out.println("✅ 测试1: 玩家创建和初始化 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 玩家创建和初始化 - 失败");
            failed++;
        }
        
        // 测试用例2: 物品拾取（正常情况）
        if (testTakeItemSuccess()) {
            System.out.println("✅ 测试2: 物品拾取（正常情况） - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 物品拾取（正常情况） - 失败");
            failed++;
        }
        
        // 测试用例3: 物品拾取（超过负重）
        if (testTakeItemExceedsWeight()) {
            System.out.println("✅ 测试3: 物品拾取（超过负重） - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 物品拾取（超过负重） - 失败");
            failed++;
        }
        
        // 测试用例4: 物品丢弃
        if (testDropItem()) {
            System.out.println("✅ 测试4: 物品丢弃 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 物品丢弃 - 失败");
            failed++;
        }
        
        // 测试用例5: 魔法饼干功能
        if (testEatCookie()) {
            System.out.println("✅ 测试5: 魔法饼干功能 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试5: 魔法饼干功能 - 失败");
            failed++;
        }
        
        // 测试用例6: 负重计算
        if (testTotalWeight()) {
            System.out.println("✅ 测试6: 负重计算 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试6: 负重计算 - 失败");
            failed++;
        }
        
        // 测试用例7: 房间访问记录
        if (testRoomVisited()) {
            System.out.println("✅ 测试7: 房间访问记录 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试7: 房间访问记录 - 失败");
            failed++;
        }
        
        // 测试用例8: 物品收集记录
        if (testItemsCollected()) {
            System.out.println("✅ 测试8: 物品收集记录 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试8: 物品收集记录 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 玩家创建和初始化。
     */
    private static boolean testPlayerCreation()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            
            if (!player.getName().equals("TestPlayer")) {
                System.out.println("  错误: 玩家名称不匹配");
                return false;
            }
            
            if (Math.abs(player.getMaxWeight() - 10.0) > 0.001) {
                System.out.println("  错误: 最大负重不匹配");
                return false;
            }
            
            if (Math.abs(player.getTotalWeight() - 0.0) > 0.001) {
                System.out.println("  错误: 初始总重量应该为0");
                return false;
            }
            
            if (player.getInventory().size() != 0) {
                System.out.println("  错误: 初始物品清单应该为空");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例2: 物品拾取（正常情况）。
     */
    private static boolean testTakeItemSuccess()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            Item item = new Item("test", "测试物品", 5.0);
            
            boolean success = player.takeItem(item);
            if (!success) {
                System.out.println("  错误: 物品拾取应该成功");
                return false;
            }
            
            if (player.getInventory().size() != 1) {
                System.out.println("  错误: 物品清单大小不正确");
                return false;
            }
            
            if (Math.abs(player.getTotalWeight() - 5.0) > 0.001) {
                System.out.println("  错误: 总重量不正确");
                return false;
            }
            
            if (!player.getItemsCollected().contains("test")) {
                System.out.println("  错误: 物品收集记录不正确");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 物品拾取（超过负重）。
     */
    private static boolean testTakeItemExceedsWeight()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            player.takeItem(new Item("item1", "物品1", 6.0));
            
            Item heavyItem = new Item("heavy", "重物", 5.0);
            if (player.canCarry(heavyItem)) {
                System.out.println("  错误: 应该无法携带超重物品");
                return false;
            }
            
            // 尝试拾取应该失败
            int beforeSize = player.getInventory().size();
            boolean success = player.takeItem(heavyItem);
            if (success) {
                System.out.println("  错误: 超重物品不应该被拾取");
                return false;
            }
            
            if (player.getInventory().size() != beforeSize) {
                System.out.println("  错误: 物品清单大小不应该改变");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: 物品丢弃。
     */
    private static boolean testDropItem()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            Item item = new Item("test", "测试物品", 2.0);
            player.takeItem(item);
            
            Item dropped = player.dropItem("test");
            if (dropped == null) {
                System.out.println("  错误: 丢弃物品返回null");
                return false;
            }
            
            if (!dropped.getName().equals("test")) {
                System.out.println("  错误: 丢弃的物品名称不匹配");
                return false;
            }
            
            if (player.getInventory().size() != 0) {
                System.out.println("  错误: 物品清单应该为空");
                return false;
            }
            
            if (Math.abs(player.getTotalWeight() - 0.0) > 0.001) {
                System.out.println("  错误: 总重量应该为0");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例5: 魔法饼干功能。
     */
    private static boolean testEatCookie()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            Item cookie = new Item("cookie", "魔法饼干", 0.1);
            player.takeItem(cookie);
            
            if (Math.abs(player.getMaxWeight() - 10.0) > 0.001) {
                System.out.println("  错误: 初始最大负重不正确");
                return false;
            }
            
            if (player.isCookieEaten()) {
                System.out.println("  错误: 初始状态不应该已吃饼干");
                return false;
            }
            
            // 吃掉饼干并增加负重
            player.eatCookie();
            player.increaseMaxWeight(5.0);
            
            if (!player.isCookieEaten()) {
                System.out.println("  错误: 应该标记为已吃饼干");
                return false;
            }
            
            if (Math.abs(player.getMaxWeight() - 15.0) > 0.001) {
                System.out.println("  错误: 最大负重应该增加5kg");
                return false;
            }
            
            if (player.hasItem("cookie")) {
                System.out.println("  错误: 饼干应该从物品清单中移除");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例6: 负重计算。
     */
    private static boolean testTotalWeight()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            player.takeItem(new Item("item1", "物品1", 2.0));
            player.takeItem(new Item("item2", "物品2", 3.0));
            player.takeItem(new Item("item3", "物品3", 1.5));
            
            double totalWeight = player.getTotalWeight();
            double expected = 6.5;
            
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
     * 测试用例7: 房间访问记录。
     */
    private static boolean testRoomVisited()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            Room room1 = new Room("房间1");
            Room room2 = new Room("房间2");
            
            player.setCurrentRoom(room1);
            if (player.getRoomsVisited().size() != 1) {
                System.out.println("  错误: 房间访问记录不正确");
                return false;
            }
            
            player.setCurrentRoom(room2);
            if (player.getRoomsVisited().size() != 2) {
                System.out.println("  错误: 房间访问记录应该增加");
                return false;
            }
            
            // 再次访问同一个房间不应该重复记录
            player.setCurrentRoom(room1);
            // Set会自动去重，所以大小应该还是2
            if (player.getRoomsVisited().size() != 2) {
                System.out.println("  错误: 重复访问的房间不应该重复记录");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例8: 物品收集记录。
     */
    private static boolean testItemsCollected()
    {
        try {
            Player player = new Player("TestPlayer", 10.0);
            player.takeItem(new Item("item1", "物品1", 1.0));
            player.takeItem(new Item("item2", "物品2", 2.0));
            
            if (player.getItemsCollected().size() != 2) {
                System.out.println("  错误: 物品收集记录不正确");
                return false;
            }
            
            if (!player.getItemsCollected().contains("item1") ||
                !player.getItemsCollected().contains("item2")) {
                System.out.println("  错误: 物品收集记录缺少物品");
                return false;
            }
            
            // 丢弃物品不应该从收集记录中移除
            player.dropItem("item1");
            if (player.getItemsCollected().size() != 2) {
                System.out.println("  错误: 丢弃物品不应该从收集记录中移除");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

