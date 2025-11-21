/**
 * GoCommand类的单元测试用例。
 * 测试go命令的执行逻辑。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * GoCommand类的单元测试。
 */
public class GoCommandTest
{
    /**
     * 运行所有GoCommand类的测试用例。
     * 注意：这个测试需要Game类的实例，所以需要创建简化的游戏环境。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("GoCommand类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 有效方向移动
        if (testValidDirection()) {
            System.out.println("✅ 测试1: 有效方向移动 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 有效方向移动 - 失败");
            failed++;
        }
        
        // 测试用例2: 无效方向移动
        if (testInvalidDirection()) {
            System.out.println("✅ 测试2: 无效方向移动 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 无效方向移动 - 失败");
            failed++;
        }
        
        // 测试用例3: 缺少方向参数
        if (testMissingDirection()) {
            System.out.println("✅ 测试3: 缺少方向参数 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 缺少方向参数 - 失败");
            failed++;
        }
        
        // 测试用例4: 房间历史记录
        if (testRoomHistory()) {
            System.out.println("✅ 测试4: 房间历史记录 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: 房间历史记录 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 有效方向移动。
     */
    private static boolean testValidDirection()
    {
        try {
            // 创建测试环境
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 创建测试房间
            Room testRoom = new Room("测试房间");
            startRoom.setExit("north", testRoom);
            
            // 执行go命令
            GoCommand goCommand = new GoCommand();
            Command command = new Command("go", "north");
            
            // 注意：这里不能直接捕获System.out，所以只检查房间是否改变
            goCommand.execute(command, game);
            
            // 验证玩家已移动到新房间
            if (player.getCurrentRoom() == startRoom) {
                System.out.println("  错误: 玩家应该移动到新房间");
                return false;
            }
            
            if (!player.getCurrentRoom().getShortDescription().equals("测试房间")) {
                System.out.println("  错误: 玩家所在的房间不正确");
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
     * 测试用例2: 无效方向移动。
     */
    private static boolean testInvalidDirection()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 尝试移动到不存在的方向
            GoCommand goCommand = new GoCommand();
            Command command = new Command("go", "invalid");
            
            goCommand.execute(command, game);
            
            // 验证玩家仍然在原房间
            if (player.getCurrentRoom() != startRoom) {
                System.out.println("  错误: 无效方向不应该移动玩家");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 缺少方向参数。
     */
    private static boolean testMissingDirection()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 执行不带参数的go命令
            GoCommand goCommand = new GoCommand();
            Command command = new Command("go", null);
            
            goCommand.execute(command, game);
            
            // 验证玩家仍然在原房间
            if (player.getCurrentRoom() != startRoom) {
                System.out.println("  错误: 缺少参数不应该移动玩家");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: 房间历史记录。
     */
    private static boolean testRoomHistory()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 创建测试房间
            Room room1 = new Room("房间1");
            Room room2 = new Room("房间2");
            startRoom.setExit("north", room1);
            room1.setExit("east", room2);
            
            // 移动到房间1
            GoCommand goCommand = new GoCommand();
            goCommand.execute(new Command("go", "north"), game);
            
            Room currentRoom1 = player.getCurrentRoom();
            if (currentRoom1 != room1) {
                System.out.println("  错误: 应该移动到房间1");
                return false;
            }
            
            // 移动到房间2
            goCommand.execute(new Command("go", "east"), game);
            
            Room currentRoom2 = player.getCurrentRoom();
            if (currentRoom2 != room2) {
                System.out.println("  错误: 应该移动到房间2");
                return false;
            }
            
            // 测试back命令（通过获取上一个房间）
            Room previousRoom = game.getPreviousRoom();
            if (previousRoom != room1) {
                System.out.println("  错误: 上一个房间应该是房间1");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

