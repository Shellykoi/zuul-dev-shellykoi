/**
 * Status命令执行器。
 * 显示游戏进度和通关状态。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class StatusCommand implements CommandExecutor {
    /**
     * 执行status命令，显示游戏进度。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（status命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game) {
        Player player = game.getPlayer();
        GameCompletionChecker.CompletionInfo info = GameCompletionChecker.checkCompletion(player);
        
        System.out.println(info.getProgressReport());
        
        // 如果通关，更新游戏记录
        if (info.isCompleted() && player.getUserId() != null && player.getGameRecordId() != null) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.updateGameRecord(
                player.getGameRecordId(),
                true,
                info.getRoomsExplored(),
                info.getItemsCollected(),
                info.isCookieEaten()
            );
        }
        
        return false;
    }
}

