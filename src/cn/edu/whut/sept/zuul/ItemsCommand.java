/**
 * Items命令执行器。
 * 显示当前房间内所有物品和玩家携带的所有物品。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class ItemsCommand implements CommandExecutor
{
    /**
     * 执行items命令，显示物品列表。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（items命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 显示房间内的物品
        System.out.println("房间内的物品:");
        String roomItems = currentRoom.getItemsString();
        if (roomItems.isEmpty() || roomItems.equals("当前房间没有物品！")) {
            System.out.println("  (无)");
        } else {
            System.out.println(roomItems);
        }
        System.out.println("房间总重量: " + 
                          String.format("%.2f", currentRoom.getTotalWeight()) + "kg");

        System.out.println();

        // 显示玩家携带的物品
        System.out.println(player.getInventoryString());
        return false;
    }
}

