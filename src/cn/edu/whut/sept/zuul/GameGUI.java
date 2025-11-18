/**
 * World of Zuul 游戏的图形用户界面。
 * 使用Java Swing创建现代化的游戏界面。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GameGUI extends JFrame
{
    private Game game;
    private Player player;
    
    // UI组件
    private JTextArea roomInfoArea;
    private JTextArea itemsArea;
    private JTextArea messageArea;
    private JTextField commandField;
    private JButton northButton, southButton, eastButton, westButton;
    private JButton lookButton, itemsButton, backButton, helpButton;
    private JButton takeButton, dropButton, eatCookieButton;
    
    // 用于捕获System.out输出
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream customOut;
    
    /**
     * 创建游戏图形界面。
     */
    public GameGUI()
    {
        game = new Game();
        player = game.getPlayer();
        
        // 设置输出重定向
        setupOutputCapture();
        
        initializeGUI();
        updateDisplay();
        
        // 显示欢迎信息
        appendMessage("欢迎来到 World of Zuul！");
        appendMessage("这是一个文本冒险游戏。");
        appendMessage("使用方向按钮或输入命令来探索世界。");
        appendMessage("输入 'help' 查看所有可用命令。\n");
    }
    
    /**
     * 设置输出捕获，将System.out重定向到消息区域。
     */
    private void setupOutputCapture()
    {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        customOut = new PrintStream(outputStream, true);
        System.setOut(customOut);
    }
    
    /**
     * 初始化图形界面组件。
     */
    private void initializeGUI()
    {
        setTitle("World of Zuul - 图形界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 左侧面板：房间信息和物品
        JPanel leftPanel = createLeftPanel();
        
        // 中间面板：消息区域
        JPanel centerPanel = createCenterPanel();
        
        // 右侧面板：命令按钮
        JPanel rightPanel = createRightPanel();
        
        // 底部面板：命令输入
        JPanel bottomPanel = createBottomPanel();
        
        // 组装主面板
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建左侧面板（房间信息和物品列表）。
     */
    private JPanel createLeftPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // 房间信息区域
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(new TitledBorder("当前房间"));
        roomInfoArea = new JTextArea(10, 25);
        roomInfoArea.setEditable(false);
        roomInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        roomInfoArea.setBackground(new Color(240, 248, 255));
        JScrollPane roomScroll = new JScrollPane(roomInfoArea);
        roomPanel.add(roomScroll, BorderLayout.CENTER);
        
        // 物品列表区域
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(new TitledBorder("物品信息"));
        itemsArea = new JTextArea(8, 25);
        itemsArea.setEditable(false);
        itemsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        itemsArea.setBackground(new Color(255, 250, 240));
        JScrollPane itemsScroll = new JScrollPane(itemsArea);
        itemsPanel.add(itemsScroll, BorderLayout.CENTER);
        
        panel.add(roomPanel, BorderLayout.NORTH);
        panel.add(itemsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建中间面板（消息输出区域）。
     */
    private JPanel createCenterPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("游戏消息"));
        
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        messageArea.setBackground(new Color(255, 255, 255));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建右侧面板（命令按钮）。
     */
    private JPanel createRightPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("快速命令"));
        panel.setPreferredSize(new Dimension(200, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // 方向按钮
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("方向移动:"), gbc);
        
        gbc.gridy = 1;
        northButton = createButton("北 (North)", e -> executeCommand("go north"));
        panel.add(northButton, gbc);
        
        gbc.gridy = 2;
        southButton = createButton("南 (South)", e -> executeCommand("go south"));
        panel.add(southButton, gbc);
        
        gbc.gridy = 3;
        eastButton = createButton("东 (East)", e -> executeCommand("go east"));
        panel.add(eastButton, gbc);
        
        gbc.gridy = 4;
        westButton = createButton("西 (West)", e -> executeCommand("go west"));
        panel.add(westButton, gbc);
        
        // 分隔线
        gbc.gridy = 5;
        panel.add(new JSeparator(), gbc);
        
        // 其他命令按钮
        gbc.gridy = 6;
        panel.add(new JLabel("其他命令:"), gbc);
        
        gbc.gridy = 7;
        lookButton = createButton("查看 (Look)", e -> executeCommand("look"));
        panel.add(lookButton, gbc);
        
        gbc.gridy = 8;
        itemsButton = createButton("物品 (Items)", e -> executeCommand("items"));
        panel.add(itemsButton, gbc);
        
        gbc.gridy = 9;
        backButton = createButton("返回 (Back)", e -> executeCommand("back"));
        panel.add(backButton, gbc);
        
        gbc.gridy = 10;
        helpButton = createButton("帮助 (Help)", e -> executeCommand("help"));
        panel.add(helpButton, gbc);
        
        // 分隔线
        gbc.gridy = 11;
        panel.add(new JSeparator(), gbc);
        
        // 物品操作按钮
        gbc.gridy = 12;
        panel.add(new JLabel("物品操作:"), gbc);
        
        gbc.gridy = 13;
        takeButton = createButton("拾取 (Take)", e -> showTakeDialog());
        panel.add(takeButton, gbc);
        
        gbc.gridy = 14;
        dropButton = createButton("丢弃 (Drop)", e -> showDropDialog());
        panel.add(dropButton, gbc);
        
        gbc.gridy = 15;
        eatCookieButton = createButton("吃饼干", e -> executeCommand("eat cookie"));
        panel.add(eatCookieButton, gbc);
        
        return panel;
    }
    
    /**
     * 创建底部面板（命令输入）。
     */
    private JPanel createBottomPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("命令输入"));
        
        commandField = new JTextField();
        commandField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        commandField.addActionListener(e -> {
            String command = commandField.getText().trim();
            if (!command.isEmpty()) {
                executeCommand(command);
                commandField.setText("");
            }
        });
        
        JButton submitButton = new JButton("执行");
        submitButton.addActionListener(e -> {
            String command = commandField.getText().trim();
            if (!command.isEmpty()) {
                executeCommand(command);
                commandField.setText("");
            }
        });
        
        panel.add(new JLabel("输入命令: "), BorderLayout.WEST);
        panel.add(commandField, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建按钮的辅助方法。
     */
    private JButton createButton(String text, ActionListener listener)
    {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(150, 30));
        return button;
    }
    
    /**
     * 执行游戏命令。
     */
    private void executeCommand(String commandString)
    {
        if (commandString == null || commandString.trim().isEmpty()) {
            return;
        }
        
        // 显示用户输入的命令
        appendMessage("> " + commandString);
        
        // 处理quit命令
        if (commandString.trim().equalsIgnoreCase("quit")) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "确定要退出游戏吗？",
                "退出确认",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                appendMessage("感谢游玩！再见！");
                System.setOut(originalOut);
                System.exit(0);
            }
            return;
        }
        
        // 解析并执行命令
        Command command = game.getParser().parseCommand(commandString);
        boolean finished = processCommand(command);
        
        // 捕获输出
        captureOutput();
        
        // 更新显示
        updateDisplay();
        
        // 如果游戏结束
        if (finished) {
            appendMessage("感谢游玩！再见！");
            System.setOut(originalOut);
            System.exit(0);
        }
    }
    
    /**
     * 处理命令（从Game类复制并修改）。
     */
    private boolean processCommand(Command command)
    {
        if (command.isUnknown()) {
            appendMessage("我不知道你在说什么...");
            return false;
        }
        
        // 使用反射或直接调用Game的processCommand方法
        // 为了简化，我们直接使用Game的内部逻辑
        String commandWord = command.getCommandWord();
        
        // 手动处理命令（因为我们需要捕获输出）
        Player player = game.getPlayer();
        
        if (commandWord.equals("go")) {
            if (!command.hasSecondWord()) {
                appendMessage("去哪里？");
                return false;
            }
            String direction = command.getSecondWord();
            Room currentRoom = player.getCurrentRoom();
            Room nextRoom = currentRoom.getExit(direction);
            
            if (nextRoom == null) {
                appendMessage("那里没有门！");
            } else {
                game.addRoomToHistory(currentRoom);
                player.setCurrentRoom(nextRoom);
                
                // 检查传输房间
                if (nextRoom instanceof TransporterRoom) {
                    TransporterRoom transporter = (TransporterRoom) nextRoom;
                    Room randomRoom = transporter.getRandomRoom();
                    if (randomRoom != null) {
                        appendMessage("你踏入了一个神秘的传输房间...");
                        appendMessage("突然，你被传送到另一个位置！");
                        player.setCurrentRoom(randomRoom);
                    }
                }
                
                appendMessage(player.getCurrentRoom().getLongDescription());
            }
        } else if (commandWord.equals("look")) {
            appendMessage(player.getCurrentRoom().getLongDescription());
        } else if (commandWord.equals("back")) {
            Room previousRoom = game.getPreviousRoom();
            if (previousRoom == null) {
                appendMessage("你已经回到了起点！");
            } else {
                player.setCurrentRoom(previousRoom);
                appendMessage("你返回到: " + previousRoom.getLongDescription());
            }
        } else if (commandWord.equals("items")) {
            Room currentRoom = player.getCurrentRoom();
            appendMessage("房间内的物品:");
            String roomItems = currentRoom.getItemsString();
            if (roomItems.isEmpty()) {
                appendMessage("  (无)");
            } else {
                appendMessage(roomItems);
            }
            appendMessage("房间总重量: " + String.format("%.2f", currentRoom.getTotalWeight()) + "kg");
            appendMessage("");
            appendMessage(player.getInventoryString());
        } else if (commandWord.equals("take")) {
            if (!command.hasSecondWord()) {
                appendMessage("拾取什么？");
                return false;
            }
            String itemName = command.getSecondWord();
            Room currentRoom = player.getCurrentRoom();
            Item item = currentRoom.getItem(itemName);
            
            if (item == null) {
                appendMessage("这里没有 " + itemName + "！");
            } else if (!player.canCarry(item)) {
                appendMessage("你无法携带 " + item.getName() + "。它重 " + 
                            String.format("%.2f", item.getWeight()) + "kg，但你只能再携带 " + 
                            String.format("%.2f", player.getMaxWeight() - player.getTotalWeight()) + "kg。");
            } else {
                currentRoom.removeItem(itemName);
                player.takeItem(item);
                appendMessage("你拾取了 " + item.getName() + "。");
            }
        } else if (commandWord.equals("drop")) {
            if (!command.hasSecondWord()) {
                appendMessage("丢弃什么？");
                return false;
            }
            String itemName = command.getSecondWord();
            Item item = player.dropItem(itemName);
            
            if (item == null) {
                appendMessage("你没有 " + itemName + "！");
            } else {
                player.getCurrentRoom().addItem(item);
                appendMessage("你丢弃了 " + item.getName() + "。");
            }
        } else if (commandWord.equals("eat")) {
            if (!command.hasSecondWord() || !command.getSecondWord().equals("cookie")) {
                appendMessage("吃什么？");
                return false;
            }
            Item cookie = player.getItem("cookie");
            if (cookie == null) {
                appendMessage("你没有魔法饼干！");
            } else {
                player.dropItem("cookie");
                player.increaseMaxWeight(5.0);
                appendMessage("你吃掉了魔法饼干。你的负重能力增加了5kg！");
                appendMessage("新的最大负重: " + String.format("%.2f", player.getMaxWeight()) + "kg");
            }
        } else if (commandWord.equals("help")) {
            appendMessage("你可以使用以下命令:");
            appendMessage("  go <方向>  - 向指定方向移动 (north, south, east, west)");
            appendMessage("  look       - 查看当前房间的详细信息");
            appendMessage("  back       - 返回上一个房间");
            appendMessage("  take <物品> - 拾取房间内的物品");
            appendMessage("  drop <物品> - 丢弃身上的物品");
            appendMessage("  items      - 查看房间和身上的物品");
            appendMessage("  eat cookie  - 吃掉魔法饼干（增加负重）");
            appendMessage("  help       - 显示此帮助信息");
            appendMessage("  quit       - 退出游戏");
        } else {
            appendMessage("我不知道你在说什么...");
        }
        
        return false;
    }
    
    /**
     * 捕获System.out的输出并显示在消息区域。
     */
    private void captureOutput()
    {
        String output = outputStream.toString();
        if (!output.isEmpty()) {
            appendMessage(output);
            outputStream.reset();
        }
    }
    
    /**
     * 更新显示（房间信息和物品列表）。
     */
    private void updateDisplay()
    {
        // 更新房间信息
        Room currentRoom = player.getCurrentRoom();
        roomInfoArea.setText(currentRoom.getLongDescription());
        
        // 更新物品信息
        StringBuilder itemsText = new StringBuilder();
        itemsText.append("房间内的物品:\n");
        String roomItems = currentRoom.getItemsString();
        if (roomItems.isEmpty()) {
            itemsText.append("  (无)\n");
        } else {
            itemsText.append(roomItems).append("\n");
        }
        itemsText.append("房间总重量: ").append(String.format("%.2f", currentRoom.getTotalWeight())).append("kg\n\n");
        itemsText.append(player.getInventoryString());
        
        itemsArea.setText(itemsText.toString());
        
        // 更新方向按钮状态
        updateDirectionButtons(currentRoom);
    }
    
    /**
     * 更新方向按钮的启用状态。
     */
    private void updateDirectionButtons(Room room)
    {
        northButton.setEnabled(room.getExit("north") != null);
        southButton.setEnabled(room.getExit("south") != null);
        eastButton.setEnabled(room.getExit("east") != null);
        westButton.setEnabled(room.getExit("west") != null);
    }
    
    /**
     * 显示拾取物品对话框。
     */
    private void showTakeDialog()
    {
        Room currentRoom = player.getCurrentRoom();
        List<Item> roomItems = new ArrayList<>(currentRoom.getItems());
        
        if (roomItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前房间没有物品！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] itemNames = new String[roomItems.size()];
        int i = 0;
        for (Item item : roomItems) {
            itemNames[i++] = item.getName();
        }
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "选择要拾取的物品:",
            "拾取物品",
            JOptionPane.QUESTION_MESSAGE,
            null,
            itemNames,
            itemNames[0]
        );
        
        if (selected != null) {
            executeCommand("take " + selected);
        }
    }
    
    /**
     * 显示丢弃物品对话框。
     */
    private void showDropDialog()
    {
        List<Item> inventory = new ArrayList<>(player.getInventory());
        
        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "你没有携带任何物品！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] itemNames = new String[inventory.size()];
        int i = 0;
        for (Item item : inventory) {
            itemNames[i++] = item.getName();
        }
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "选择要丢弃的物品:",
            "丢弃物品",
            JOptionPane.QUESTION_MESSAGE,
            null,
            itemNames,
            itemNames[0]
        );
        
        if (selected != null) {
            executeCommand("drop " + selected);
        }
    }
    
    /**
     * 向消息区域追加消息。
     */
    private void appendMessage(String message)
    {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
    
    /**
     * 主方法，启动图形界面。
     */
    public static void main(String[] args)
    {
        // 设置外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new GameGUI().setVisible(true);
        });
    }
}

