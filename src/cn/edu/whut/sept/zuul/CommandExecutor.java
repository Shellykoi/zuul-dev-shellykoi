/**
 * 命令执行器接口。
 * 所有游戏命令都需要实现此接口，以统一命令处理方式。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public interface CommandExecutor
{
    /**
     * 执行命令。
     * 
     * @param command 要执行的命令对象
     * @param game 游戏对象，用于访问游戏状态
     * @return 如果执行的是退出命令，返回true；否则返回false
     */
    boolean execute(Command command, Game game);
}

