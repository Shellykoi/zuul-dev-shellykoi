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
        
        // 检查是否有上锁的房间
        String[] directions = {"north", "south", "east", "west", "up", "down"};
        boolean hasLockedRoom = false;
        for (String dir : directions) {
            if (currentRoom.hasExit(dir)) {
                Room exit = currentRoom.getExitDirectly(dir);
                if (exit instanceof LockedRoom) {
                    LockedRoom lockedRoom = (LockedRoom) exit;
                    if (!lockedRoom.isUnlocked()) {
                        if (!hasLockedRoom) {
                            System.out.println("\n提示：你发现了一个上锁的房间！");
                            hasLockedRoom = true;
                        }
                        String dirCN = translateDirection(dir);
                        System.out.println("  " + dirCN + " 方向有一个上锁的房间，使用 'use key' 可以解锁。");
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 将方向翻译为中文。
     */
    private String translateDirection(String direction) {
        switch (direction.toLowerCase()) {
            case "north": return "北";
            case "south": return "南";
            case "east": return "东";
            case "west": return "西";
            case "up": return "上";
            case "down": return "下";
            default: return direction;
        }
    }
}

