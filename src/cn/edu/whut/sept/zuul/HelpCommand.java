/**
 * Help命令执行器。
 * 显示游戏帮助信息。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class HelpCommand implements CommandExecutor
{
    /**
     * 执行help命令，显示游戏帮助信息。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（help命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        System.out.println("你迷路了。你独自一人。");
        System.out.println("你在大学里四处游荡。");
        System.out.println();
        System.out.println("你可以使用的命令:");
        game.getParser().showCommands();
        return false;
    }
}

