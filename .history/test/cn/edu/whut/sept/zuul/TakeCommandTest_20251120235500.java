/**
 * TakeCommand类的单元测试用例。
 * 测试take命令的执行逻辑。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * TakeCommand类的单元测试。
 */
public class TakeCommandTest
{
    /**
     * 运行所有TakeCommand类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("TakeCommand类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 成功拾取物品
        if (testTakeItemSuccess()) {
            System.out.println("✅ 测试1: 成功拾取物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 成功拾取物品 - 失败");
            failed++;
        }
        
        // 测试用例2: 拾取不存在的物品
        if (testTakeNonExistentItem()) {
            System.out.println("✅ 测试2: 拾取不存在的物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 拾取不存在的物品 - 失败");
            failed++;
        }
        
        // 测试用例3: 拾取超重物品
        if (testTakeOverweightItem()) {
            System.out.println("✅ 测试3: 拾取超重物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 拾取超重物品 - 失败");
            failed++;
        }
        
        // 测试用例4: 缺少物品名称参数
        if (testMissingItemName()) {
            System.out.println("✅ 测试4: 缺少物品名称参数 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 缺少物品名称参数 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 成功拾取物品。
     */
    private static boolean testTakeItemSuccess()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room currentRoom = player.getCurrentRoom();
            
            // 添加物品到房间
            Item item = new Item("key", "一把钥匙", 1.0);
            currentRoom.addItem(item);
            
            // 执行take命令
            TakeCommand takeCommand = new TakeCommand();
            Command command = new Command("take", "key");
            
            takeCommand.execute(command, game);
            
            // 验证物品已从房间移除
            if (currentRoom.getItem("key") != null) {
                System.out.println("  错误: 物品应该从房间中移除");
                return false;
            }
            
            // 验证物品已添加到玩家背包
            if (!player.hasItem("key")) {
                System.out.println("  错误: 物品应该添加到玩家背包");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试用例2: 拾取不存在的物品。
     */
    private static boolean testTakeNonExistentItem()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room currentRoom = player.getCurrentRoom();
            
            int initialInventorySize = player.getInventory().size();
            
            // 尝试拾取不存在的物品
            TakeCommand takeCommand = new TakeCommand();
            Command command = new Command("take", "nonexistent");
            
            takeCommand.execute(command, game);
            
            // 验证玩家背包没有变化
            if (player.getInventory().size() != initialInventorySize) {
                System.out.println("  错误: 背包大小不应该改变");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 拾取超重物品。
     */
    private static boolean testTakeOverweightItem()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room currentRoom = player.getCurrentRoom();
            
            // 玩家初始最大负重为10kg，先拾取9kg的物品
            Item item1 = new Item("heavy1", "重物1", 9.0);
            currentRoom.addItem(item1);
            player.takeItem(item1);
            
            // 尝试拾取另一个超过剩余负重的物品
            Item item2 = new Item("heavy2", "重物2", 2.0);
            currentRoom.addItem(item2);
            
            int initialInventorySize = player.getInventory().size();
            
            TakeCommand takeCommand = new TakeCommand();
            Command command = new Command("take", "heavy2");
            
            takeCommand.execute(command, game);
            
            // 验证物品仍然在房间中
            if (currentRoom.getItem("heavy2") == null) {
                System.out.println("  错误: 超重物品不应该从房间中移除");
                return false;
            }
            
            // 验证物品没有添加到玩家背包
            if (player.getInventory().size() != initialInventorySize) {
                System.out.println("  错误: 超重物品不应该添加到背包");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试用例4: 缺少物品名称参数。
     */
    private static boolean testMissingItemName()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            int initialInventorySize = player.getInventory().size();
            
            // 执行不带参数的take命令
            TakeCommand takeCommand = new TakeCommand();
            Command command = new Command("take", null);
            
            takeCommand.execute(command, game);
            
            // 验证玩家背包没有变化
            if (player.getInventory().size() != initialInventorySize) {
                System.out.println("  错误: 缺少参数不应该改变背包");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

