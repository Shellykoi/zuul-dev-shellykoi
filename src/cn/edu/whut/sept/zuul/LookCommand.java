/**
 * Look命令执行器。
 * 查看当前房间的详细信息，包括房间描述、出口和物品。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class LookCommand implements CommandExecutor
{
    /**
     * 执行look命令，显示当前房间的详细信息。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（look命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();
        System.out.println(currentRoom.getLongDescription());
        return false;
    }
}

