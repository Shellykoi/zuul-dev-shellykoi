/**
 * Quit命令执行器。
 * 处理玩家退出游戏。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class QuitCommand implements CommandExecutor
{
    /**
     * 执行quit命令，退出游戏。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 如果确认退出返回true，否则返回false
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        if (command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        } else {
            return true;  // 信号表示要退出
        }
    }
}

