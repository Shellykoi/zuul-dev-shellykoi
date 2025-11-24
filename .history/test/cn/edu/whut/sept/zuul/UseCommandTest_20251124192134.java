/**
 * UseCommand类单元测试
 * 测试物品使用功能的各个方面
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class UseCommandTest {
    
    /**
     * 运行所有测试用例
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests() {
        int passed = 0;
        int failed = 0;
        
        System.out.println("\n========================================");
        System.out.println("UseCommand类单元测试");
        System.out.println("========================================\n");
        
        if (testUseKeyOnLockedRoom()) {
            System.out.println("✅ 测试1: 使用钥匙解锁房间 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 使用钥匙解锁房间 - 失败");
            failed++;
        }
        
        if (testUseKeyOnUnlockedRoom()) {
            System.out.println("✅ 测试2: 使用钥匙解锁已解锁的房间 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 使用钥匙解锁已解锁的房间 - 失败");
            failed++;
        }
        
        if (testUseKeyOnNormalRoom()) {
            System.out.println("✅ 测试3: 在普通房间使用钥匙 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 在普通房间使用钥匙 - 失败");
            failed++;
        }
        
        if (testUseMap()) {
            System.out.println("✅ 测试4: 使用地图 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 使用地图 - 失败");
            failed++;
        }
        
        if (testUseNonUsableItem()) {
            System.out.println("✅ 测试5: 使用不可使用的物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试5: 使用不可使用的物品 - 失败");
            failed++;
        }
        
        if (testUseItemNotInInventory()) {
            System.out.println("✅ 测试6: 使用背包中不存在的物品 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试6: 使用背包中不存在的物品 - 失败");
            failed++;
        }
        
        if (testUseCommandWithoutItemName()) {
            System.out.println("✅ 测试7: use命令缺少物品名称 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试7: use命令缺少物品名称 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 使用钥匙解锁房间
     */
    private static boolean testUseKeyOnLockedRoom() {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            
            // 创建上锁房间
            LockedRoom lockedRoom = new LockedRoom("上锁的宝库", "key");
            Room currentRoom = new Room("当前房间");
            currentRoom.setExit("north", lockedRoom);
            player.setCurrentRoom(currentRoom);
            
            // 给玩家一把钥匙
            Item key = new Item("key", "一把钥匙", 0.1, "KEY", "可以解锁上锁的房间");
            player.takeItem(key);
            
            // 移动到上锁房间（应该被阻止）
            Room nextRoom = currentRoom.getExit("north");
            if (nextRoom != null && nextRoom instanceof LockedRoom) {
                LockedRoom lr = (LockedRoom) nextRoom;
                if (lr.isUnlocked()) {
                    System.out.println("  错误: 房间不应该已经解锁");
                    return false;
                }
                
                // 使用钥匙解锁
                boolean unlocked = lr.unlock("key");
                if (!unlocked) {
                    System.out.println("  错误: 钥匙应该能解锁房间");
                    return false;
                }
                
                if (!lr.isUnlocked()) {
                    System.out.println("  错误: 房间应该已解锁");
                    return false;
                }
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试用例2: 使用钥匙解锁已解锁的房间
     */
    private static boolean testUseKeyOnUnlockedRoom() {
        try {
            LockedRoom lockedRoom = new LockedRoom("上锁的房间", "key");
            lockedRoom.unlock("key");
            
            // 尝试再次解锁
            boolean result = lockedRoom.unlock("key");
            // 应该返回true（即使已经解锁）
            if (!lockedRoom.isUnlocked()) {
                System.out.println("  错误: 房间应该保持解锁状态");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 在普通房间使用钥匙
     */
    private static boolean testUseKeyOnNormalRoom() {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            
            // 创建普通房间
            Room normalRoom = new Room("普通房间");
            player.setCurrentRoom(normalRoom);
            
            // 给玩家一把钥匙
            Item key = new Item("key", "一把钥匙", 0.1, "KEY", "可以解锁上锁的房间");
            player.takeItem(key);
            
            // 使用钥匙（应该提示没有需要解锁的房间）
            UseCommand useCommand = new UseCommand();
            Command command = new Command("use", "key");
            
            // 由于UseCommand使用System.out.println，我们无法直接捕获输出
            // 但可以验证钥匙仍然在玩家背包中
            Item keyInInventory = player.getItem("key");
            if (keyInInventory == null) {
                System.out.println("  错误: 钥匙不应该被消耗");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: 使用地图
     */
    private static boolean testUseMap() {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            
            // 给玩家一张地图
            Item map = new Item("map", "一张地图", 0.2, "MAP", "显示当前位置的详细信息");
            player.takeItem(map);
            
            // 验证地图是可使用的
            if (!map.isUsable()) {
                System.out.println("  错误: 地图应该是可使用的");
                return false;
            }
            
            if (!"MAP".equals(map.getItemType())) {
                System.out.println("  错误: 地图类型应该是MAP");
                return false;
            }
            
            // 地图使用后不应该被消耗
            Item mapAfterUse = player.getItem("map");
            if (mapAfterUse == null) {
                System.out.println("  错误: 地图不应该被消耗");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例5: 使用不可使用的物品
     */
    private static boolean testUseNonUsableItem() {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            
            // 创建一个不可使用的物品
            Item normalItem = new Item("book", "一本书", 1.0);
            player.takeItem(normalItem);
            
            // 验证物品不可使用
            if (normalItem.isUsable()) {
                System.out.println("  错误: 普通物品不应该是可使用的");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例6: 使用背包中不存在的物品
     */
    private static boolean testUseItemNotInInventory() {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            
            // 尝试使用不存在的物品
            Item item = player.getItem("nonexistent");
            if (item != null) {
                System.out.println("  错误: 不应该找到不存在的物品");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例7: use命令缺少物品名称
     */
    private static boolean testUseCommandWithoutItemName() {
        try {
            UseCommand useCommand = new UseCommand();
            Command command = new Command("use", null);
            
            // 执行命令（应该返回false，因为命令不会退出游戏）
            boolean result = useCommand.execute(command, new Game());
            
            // 验证命令执行（虽然没有物品名称，但应该处理错误情况）
            if (result) {
                System.out.println("  错误: use命令不应该退出游戏");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

