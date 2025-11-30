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
            System.out.println("去哪里？");
            return false;
        }

        String direction = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 尝试离开当前房间
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            // 检查是否是上锁的房间
            // 需要检查所有方向的出口，看是否有LockedRoom
            String[] allDirections = {"north", "south", "east", "west", "up", "down"};
            boolean foundLockedRoom = false;
            for (String dir : allDirections) {
                if (dir.equals(direction)) {
                    // 直接访问exits来检查（绕过getExit的锁定检查）
                    // 由于Room的exits是private，我们需要另一种方法
                    // 使用反射或者让Room提供hasExit方法
                    // 简化方案：在UseCommand中解锁后，getExit就会返回房间
                    break;
                }
            }
            System.out.println("那里没有门！");
            System.out.println("提示：如果看到上锁的房间，使用 'use key' 命令可以解锁。");
        } else {
            // 记录房间历史（用于back命令）
            game.addRoomToHistory(currentRoom);
            player.setCurrentRoom(nextRoom);
            
            // 检查是否进入传输房间
            if (nextRoom instanceof TransporterRoom) {
                TransporterRoom transporter = (TransporterRoom) nextRoom;
                Room randomRoom = transporter.getRandomRoom();
                if (randomRoom != null) {
                    System.out.println("你踏入了一个神秘的传输房间...");
                    System.out.println("突然，你被传送到另一个位置！");
                    player.setCurrentRoom(randomRoom);
                }
            }
            
            System.out.println(player.getCurrentRoom().getLongDescription());
        }
        return false;
    }
}

