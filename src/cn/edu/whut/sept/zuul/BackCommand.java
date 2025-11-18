/**
 * Back命令执行器。
 * 将玩家带回上一个房间，支持多级回退。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class BackCommand implements CommandExecutor
{
    /**
     * 执行back命令，返回上一个房间。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（back命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        Room previousRoom = game.getPreviousRoom();
        if (previousRoom == null) {
            System.out.println("You are at the starting point. Cannot go back further.");
        } else {
            game.getPlayer().setCurrentRoom(previousRoom);
            System.out.println("You go back to the previous room.");
            System.out.println(previousRoom.getLongDescription());
        }
        return false;
    }
}

