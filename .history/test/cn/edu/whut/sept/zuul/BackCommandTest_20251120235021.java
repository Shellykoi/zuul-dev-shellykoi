/**
 * BackCommand类的单元测试用例。
 * 测试back命令的执行逻辑。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * BackCommand类的单元测试。
 */
public class BackCommandTest
{
    /**
     * 运行所有BackCommand类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("BackCommand类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 单次back命令
        if (testSingleBack()) {
            System.out.println("✅ 测试1: 单次back命令 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 单次back命令 - 失败");
            failed++;
        }
        
        // 测试用例2: 多次back命令（多级回退）
        if (testMultipleBack()) {
            System.out.println("✅ 测试2: 多次back命令（多级回退） - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 多次back命令（多级回退） - 失败");
            failed++;
        }
        
        // 测试用例3: back到起点
        if (testBackToStart()) {
            System.out.println("✅ 测试3: back到起点 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: back到起点 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 单次back命令。
     */
    private static boolean testSingleBack()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 创建测试房间
            Room room1 = new Room("房间1");
            startRoom.setExit("north", room1);
            
            // 移动到房间1
            GoCommand goCommand = new GoCommand();
            goCommand.execute(new Command("go", "north"), game);
            
            if (player.getCurrentRoom() != room1) {
                System.out.println("  错误: 应该移动到房间1");
                return false;
            }
            
            // 执行back命令
            BackCommand backCommand = new BackCommand();
            backCommand.execute(new Command("back", null), game);
            
            // 验证返回到起始房间
            if (player.getCurrentRoom() != startRoom) {
                System.out.println("  错误: 应该返回到起始房间");
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
     * 测试用例2: 多次back命令（多级回退）。
     */
    private static boolean testMultipleBack()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 创建多个测试房间
            Room room1 = new Room("房间1");
            Room room2 = new Room("房间2");
            Room room3 = new Room("房间3");
            
            startRoom.setExit("north", room1);
            room1.setExit("east", room2);
            room2.setExit("south", room3);
            
            // 移动到房间1
            GoCommand goCommand = new GoCommand();
            goCommand.execute(new Command("go", "north"), game);
            
            // 移动到房间2
            goCommand.execute(new Command("go", "east"), game);
            
            // 移动到房间3
            goCommand.execute(new Command("go", "south"), game);
            
            if (!player.getCurrentRoom().getShortDescription().equals("房间3")) {
                System.out.println("  错误: 应该移动到房间3");
                return false;
            }
            
            // 第一次back，应该回到房间2
            BackCommand backCommand = new BackCommand();
            backCommand.execute(new Command("back", null), game);
            
            if (!player.getCurrentRoom().getShortDescription().equals("房间2")) {
                System.out.println("  错误: 第一次back应该回到房间2");
                return false;
            }
            
            // 第二次back，应该回到房间1
            backCommand.execute(new Command("back", null), game);
            
            if (!player.getCurrentRoom().getShortDescription().equals("房间1")) {
                System.out.println("  错误: 第二次back应该回到房间1");
                return false;
            }
            
            // 第三次back，应该回到起始房间
            backCommand.execute(new Command("back", null), game);
            
            if (player.getCurrentRoom() != startRoom) {
                System.out.println("  错误: 第三次back应该回到起始房间");
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
     * 测试用例3: back到起点。
     */
    private static boolean testBackToStart()
    {
        try {
            Game game = new Game();
            Player player = game.getPlayer();
            Room startRoom = player.getCurrentRoom();
            
            // 移动到另一个房间
            Room room1 = new Room("房间1");
            startRoom.setExit("north", room1);
            
            GoCommand goCommand = new GoCommand();
            goCommand.execute(new Command("go", "north"), game);
            
            // 回到起始房间
            BackCommand backCommand = new BackCommand();
            backCommand.execute(new Command("back", null), game);
            
            // 再次执行back，应该已经在起点
            backCommand.execute(new Command("back", null), game);
            
            // 验证仍然在起始房间
            if (player.getCurrentRoom() != startRoom) {
                System.out.println("  错误: 应该仍然在起始房间");
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

