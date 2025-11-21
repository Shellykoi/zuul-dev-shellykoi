/**
 * Command类的单元测试用例。
 * 测试命令对象的创建、属性获取等功能。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * Command类的单元测试。
 */
public class CommandTest
{
    /**
     * 运行所有Command类的测试用例。
     * 
     * @return 测试通过返回true，失败返回false
     */
    public static boolean runAllTests()
    {
        System.out.println("========================================");
        System.out.println("Command类单元测试");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // 测试用例1: 命令创建（有效命令）
        if (testValidCommand()) {
            System.out.println("✅ 测试1: 有效命令创建 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试1: 有效命令创建 - 失败");
            failed++;
        }
        
        // 测试用例2: 命令创建（无效命令）
        if (testInvalidCommand()) {
            System.out.println("✅ 测试2: 无效命令创建 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试2: 无效命令创建 - 失败");
            failed++;
        }
        
        // 测试用例3: 命令属性获取
        if (testCommandProperties()) {
            System.out.println("✅ 测试3: 命令属性获取 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试3: 命令属性获取 - 失败");
            failed++;
        }
        
        // 测试用例4: hasSecondWord方法
        if (testHasSecondWord()) {
            System.out.println("✅ 测试4: hasSecondWord方法 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试4: hasSecondWord方法 - 失败");
            failed++;
        }
        
        // 测试用例5: isUnknown方法
        if (testIsUnknown()) {
            System.out.println("✅ 测试5: isUnknown方法 - 通过");
            passed++;
        } else {
            System.out.println("❌ 测试5: isUnknown方法 - 失败");
            failed++;
        }
        
        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================\n");
        
        return failed == 0;
    }
    
    /**
     * 测试用例1: 有效命令创建。
     */
    private static boolean testValidCommand()
    {
        try {
            Command command = new Command("go", "north");
            
            if (!command.getCommandWord().equals("go")) {
                System.out.println("  错误: 命令词不匹配");
                return false;
            }
            
            if (!command.getSecondWord().equals("north")) {
                System.out.println("  错误: 第二个词不匹配");
                return false;
            }
            
            if (command.isUnknown()) {
                System.out.println("  错误: 有效命令不应该被标记为未知");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例2: 无效命令创建。
     */
    private static boolean testInvalidCommand()
    {
        try {
            Command command = new Command(null, "something");
            
            if (command.getCommandWord() != null) {
                System.out.println("  错误: 无效命令的命令词应该为null");
                return false;
            }
            
            if (!command.isUnknown()) {
                System.out.println("  错误: 无效命令应该被标记为未知");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例3: 命令属性获取。
     */
    private static boolean testCommandProperties()
    {
        try {
            Command command = new Command("take", "key");
            
            String commandWord = command.getCommandWord();
            String secondWord = command.getSecondWord();
            
            if (!commandWord.equals("take")) {
                System.out.println("  错误: 命令词不匹配");
                return false;
            }
            
            if (!secondWord.equals("key")) {
                System.out.println("  错误: 第二个词不匹配");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例4: hasSecondWord方法。
     */
    private static boolean testHasSecondWord()
    {
        try {
            // 有第二个词
            Command command1 = new Command("go", "north");
            if (!command1.hasSecondWord()) {
                System.out.println("  错误: 应该有第二个词");
                return false;
            }
            
            // 没有第二个词
            Command command2 = new Command("look", null);
            if (command2.hasSecondWord()) {
                System.out.println("  错误: 不应该有第二个词");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用例5: isUnknown方法。
     */
    private static boolean testIsUnknown()
    {
        try {
            // 有效命令
            Command validCommand = new Command("help", null);
            if (validCommand.isUnknown()) {
                System.out.println("  错误: 有效命令不应该被标记为未知");
                return false;
            }
            
            // 无效命令
            Command invalidCommand = new Command(null, null);
            if (!invalidCommand.isUnknown()) {
                System.out.println("  错误: 无效命令应该被标记为未知");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
            return false;
        }
    }
}

