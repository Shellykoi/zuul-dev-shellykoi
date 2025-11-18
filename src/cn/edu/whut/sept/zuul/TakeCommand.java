/**
 * Take命令执行器。
 * 玩家拾取房间内的指定物品。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class TakeCommand implements CommandExecutor
{
    /**
     * 执行take命令，拾取房间内的物品。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（take命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        if (!command.hasSecondWord()) {
            System.out.println("Take what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 从房间中获取物品
        Item item = currentRoom.getItem(itemName);
        if (item == null) {
            System.out.println("There is no " + itemName + " in this room.");
            return false;
        }

        // 检查是否可以携带
        if (!player.canCarry(item)) {
            System.out.println("The " + itemName + " is too heavy! You cannot carry it.");
            System.out.println("Your current weight: " + String.format("%.2f", player.getTotalWeight()) + 
                           "kg / " + String.format("%.2f", player.getMaxWeight()) + "kg");
            return false;
        }

        // 拾取物品
        currentRoom.removeItem(itemName);
        player.takeItem(item);
        System.out.println("You take the " + itemName + ".");
        return false;
    }
}

