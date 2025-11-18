/**
 * Go命令执行器。
 * 处理玩家在房间之间的移动。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class GoCommand implements CommandExecutor
{
    /**
     * 执行go命令，向房间的指定方向出口移动。
     * 如果进入传输房间，会自动随机传输到另一个房间。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（go命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        if (!command.hasSecondWord()) {
            System.out.println("Go where?");
            return false;
        }

        String direction = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 尝试离开当前房间
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no door!");
        } else {
            // 记录房间历史（用于back命令）
            game.addRoomToHistory(currentRoom);
            player.setCurrentRoom(nextRoom);
            
            // 检查是否进入传输房间
            if (nextRoom instanceof TransporterRoom) {
                TransporterRoom transporter = (TransporterRoom) nextRoom;
                Room randomRoom = transporter.getRandomRoom();
                if (randomRoom != null) {
                    System.out.println("You step into a mysterious transporter room...");
                    System.out.println("Suddenly, you are teleported to another location!");
                    player.setCurrentRoom(randomRoom);
                }
            }
            
            System.out.println(player.getCurrentRoom().getLongDescription());
        }
        return false;
    }
}

