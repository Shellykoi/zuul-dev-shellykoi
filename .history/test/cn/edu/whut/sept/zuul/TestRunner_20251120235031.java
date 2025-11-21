/**
 * 测试运行器。
 * 运行所有单元测试用例并汇总结果。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

/**
 * 测试运行器类，用于执行所有单元测试。
 */
public class TestRunner
{
    /**
     * 主方法，运行所有测试用例。
     * 
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args)
    {
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║        World of Zuul 游戏项目 - 单元测试套件            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        
        int totalPassed = 0;
        int totalFailed = 0;
        
        // 运行Item类测试
        if (ItemTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行Room类测试
        if (RoomTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行Player类测试
        if (PlayerTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行Command类测试
        if (CommandTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行GoCommand类测试
        if (GoCommandTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行TakeCommand类测试
        if (TakeCommandTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 运行BackCommand类测试
        if (BackCommandTest.runAllTests()) {
            totalPassed++;
        } else {
            totalFailed++;
        }
        
        // 汇总测试结果
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                     测试结果汇总                        ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║  测试套件: %-43d  ║\n", totalPassed + totalFailed);
        System.out.printf("║  通过: %-47d  ║\n", totalPassed);
        System.out.printf("║  失败: %-47d  ║\n", totalFailed);
        
        double passRate = (totalPassed + totalFailed) > 0 ? 
            (double) totalPassed / (totalPassed + totalFailed) * 100 : 0;
        System.out.printf("║  通过率: %-45.2f%%  ║\n", passRate);
        
        if (totalFailed == 0) {
            System.out.println("║                                                          ║");
            System.out.println("║  ✅ 所有测试通过！                                        ║");
        } else {
            System.out.println("║                                                          ║");
            System.out.println("║  ❌ 部分测试失败，请检查上述错误信息                      ║");
        }
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 返回适当的退出码
        System.exit(totalFailed == 0 ? 0 : 1);
    }
}

