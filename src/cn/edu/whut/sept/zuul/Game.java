/**
 * 该类是"World-of-Zuul"应用程序的主类。
 * 《World of Zuul》是一款简单的文本冒险游戏。用户可以在一些房间组成的迷宫中探险。
 * 你们可以通过扩展该游戏的功能使它更有趣!
 *
 * 如果想开始执行这个游戏，用户需要创建Game类的一个实例并调用"play"方法。
 *
 * Game类的实例将创建并初始化所有其他类:它创建所有房间，并将它们连接成迷宫；它创建解析器
 * 接收用户输入，并将用户输入转换成命令后开始运行游戏。
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 2.0 (重构为命令模式，添加玩家系统和物品系统)
 */
package cn.edu.whut.sept.zuul;

import java.util.HashMap;
import java.util.Stack;
import java.util.Random;

public class Game
{
    private Parser parser;
    private Player player;
    /**
     * 命令执行器映射表，键为命令词，值为对应的命令执行器。
     */
    private HashMap<String, CommandExecutor> commandExecutors;
    /**
     * 房间历史栈，用于实现back命令的多级回退功能。
     */
    private Stack<Room> roomHistory;

    /**
     * 创建游戏并初始化内部数据和解析器.
     */
    public Game()
    {
        createRooms();
        parser = new Parser();
        roomHistory = new Stack<>();
        initializeCommands();
        
        // 创建玩家，初始最大负重为10kg
        player = new Player("Player", 10.0);
        player.setCurrentRoom(getStartingRoom());
    }

    /**
     * 初始化命令执行器映射表。
     */
    private void initializeCommands()
    {
        commandExecutors = new HashMap<>();
        commandExecutors.put("go", new GoCommand());
        commandExecutors.put("help", new HelpCommand());
        commandExecutors.put("quit", new QuitCommand());
        commandExecutors.put("look", new LookCommand());
        commandExecutors.put("back", new BackCommand());
        commandExecutors.put("take", new TakeCommand());
        commandExecutors.put("drop", new DropCommand());
        commandExecutors.put("items", new ItemsCommand());
        commandExecutors.put("eat", new EatCookieCommand());
    }

    /**
     * 所有房间的映射表，用于传输房间功能。
     */
    private HashMap<String, Room> allRoomsMap;
    
    /**
     * 创建所有房间对象并连接其出口用以构建迷宫.
     * 同时为房间添加物品和魔法饼干，并创建传输房间。
     */
    private void createRooms()
    {
        Room outside, theater, pub, lab, office, transporter;

        // create the rooms
        outside = new Room("outside the main entrance of the university");
        theater = new Room("in a lecture theater");
        pub = new Room("in the campus pub");
        lab = new Room("in a computing lab");
        office = new Room("in the computing admin office");

        // 创建所有房间的映射表（用于传输房间）
        allRoomsMap = new HashMap<>();
        allRoomsMap.put("outside", outside);
        allRoomsMap.put("theater", theater);
        allRoomsMap.put("pub", pub);
        allRoomsMap.put("lab", lab);
        allRoomsMap.put("office", office);

        // 创建传输房间
        transporter = new TransporterRoom("in a mysterious transporter room", allRoomsMap);
        allRoomsMap.put("transporter", transporter);

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", transporter);  // 添加传输房间入口

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);
        
        // 传输房间可以"离开"到任何方向（实际是随机传输）
        transporter.setExit("north", outside);  // 这些出口会被重写为随机传输
        transporter.setExit("south", outside);
        transporter.setExit("east", outside);
        transporter.setExit("west", outside);

        // 添加物品到房间
        outside.addItem(new Item("key", "a rusty old key", 0.1));
        outside.addItem(new Item("map", "a campus map", 0.2));
        
        theater.addItem(new Item("book", "a programming textbook", 1.5));
        
        pub.addItem(new Item("coin", "a golden coin", 0.05));
        pub.addItem(new Item("bottle", "an empty bottle", 0.3));
        
        lab.addItem(new Item("computer", "a laptop computer", 2.5));
        lab.addItem(new Item("cable", "a USB cable", 0.1));
        
        // 在随机房间添加魔法饼干
        Random random = new Random();
        Room[] rooms = {outside, theater, pub, lab, office};
        Room cookieRoom = rooms[random.nextInt(rooms.length)];
        cookieRoom.addItem(new Item("cookie", "a magic cookie that increases carrying capacity", 0.1));
        
        // 保存起始房间（用于back命令的起点判断）
        startingRoom = outside;
    }
    
    /**
     * 起始房间，用于back命令判断是否到达起点。
     */
    private Room startingRoom;
    
    /**
     * 获取起始房间。
     * 
     * @return 起始房间对象
     */
    private Room getStartingRoom()
    {
        return startingRoom;
    }

    /**
     *  游戏主控循环，直到用户输入退出命令后结束整个程序.
     */
    public void play()
    {
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * 向用户输出欢迎信息.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a new, incredibly boring adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        System.out.println(player.getCurrentRoom().getLongDescription());
    }

    /**
     * 执行用户输入的游戏指令。
     * 使用命令模式处理命令，使系统更易扩展。
     * 
     * @param command 待处理的游戏指令，由解析器从用户输入内容生成.
     * @return 如果执行的是游戏结束指令，则返回true，否则返回false.
     */
    private boolean processCommand(Command command)
    {
        if (command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        CommandExecutor executor = commandExecutors.get(commandWord);
        
        if (executor != null) {
            return executor.execute(command, this);
        } else {
            System.out.println("I don't know what you mean...");
            return false;
        }
    }

    /**
     * 获取玩家对象。
     * 
     * @return 玩家对象
     */
    public Player getPlayer()
    {
        return player;
    }

    /**
     * 获取解析器对象。
     * 
     * @return 解析器对象
     */
    public Parser getParser()
    {
        return parser;
    }

    /**
     * 将房间添加到历史记录中（用于back命令）。
     * 
     * @param room 要添加的房间
     */
    public void addRoomToHistory(Room room)
    {
        roomHistory.push(room);
    }

    /**
     * 获取上一个房间（用于back命令）。
     * 
     * @return 上一个房间对象，如果没有历史记录则返回null
     */
    public Room getPreviousRoom()
    {
        if (roomHistory.isEmpty()) {
            return null;
        }
        return roomHistory.pop();
    }
}
