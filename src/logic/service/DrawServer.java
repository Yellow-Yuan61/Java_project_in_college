package logic.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 抽奖Socket服务器（C/S架构），在本地监听端口，处理客户端的抽奖请求。
 * 使用多线程技术处理并发连接，将开奖过程从GUI中解耦。
 *
 * <p>通信协议：</p>
 * <pre>
 * Client → Server: "DRAW_START"    开始抽奖
 * Server → Client: "ROLL:n1,n2,...n7"  滚动中的随机号码（每80ms发送一次）
 * Client → Server: "DRAW_STOP"     停止抽奖
 * Server → Client: "RESULT:n1,n2,...n7"  最终开奖结果
 * Client → Server: "QUIT"          断开连接
 * </pre>
 *
 * @author Yuan
 * @version 1.0
 */
public class DrawServer {

    /** 默认监听端口 */
    public static final int DEFAULT_PORT = 9527;

    /** 滚动动画间隔（毫秒） */
    private static final long ROLL_INTERVAL_MS = 80;

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final ExecutorService threadPool;

    /** 服务器单例 */
    private static DrawServer instance;

    /**
     * 获取服务器单例
     * @return DrawServer实例
     */
    public static synchronized DrawServer getInstance() {
        if (instance == null) {
            instance = new DrawServer(DEFAULT_PORT);
        }
        return instance;
    }

    private DrawServer(int port) {
        this.port = port;
        this.running = false;
        this.threadPool = Executors.newCachedThreadPool();
    }

    /**
     * 启动服务器（在守护线程中运行）
     * @return 启动成功返回true
     */
    public boolean start() {
        if (running) return true;
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            Thread serverThread = new Thread(() -> {
                System.out.println("[DrawServer] 抽奖服务器已启动，监听端口 " + port);
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("[DrawServer] 新客户端连接: " + clientSocket.getInetAddress());
                        threadPool.submit(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("[DrawServer] 接受连接失败: " + e.getMessage());
                        }
                    }
                }
            }, "DrawServer-Main");
            serverThread.setDaemon(true);
            serverThread.start();
            return true;
        } catch (IOException e) {
            System.err.println("[DrawServer] 启动失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        running = false;
        threadPool.shutdownNow();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[DrawServer] 关闭失败: " + e.getMessage());
        }
        System.out.println("[DrawServer] 抽奖服务器已停止");
    }

    /**
     * 检查服务器是否在运行
     * @return 运行中返回true
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 获取服务器端口
     * @return 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 处理单个客户端连接
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)) {

            String command;
            while ((command = reader.readLine()) != null) {
                if ("DRAW_START".equals(command)) {
                    handleDrawInteractive(writer, reader);
                } else if ("QUIT".equals(command)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[DrawServer] 客户端通信异常: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * 处理交互式抽奖：启动滚动线程发送随机号码，
     * 等待客户端发送停止命令后返回最终结果。
     */
    private void handleDrawInteractive(PrintWriter writer, BufferedReader reader) {
        int[] finalNumbers = LotteryService.generateRandomNumbers();
        final boolean[] stopped = {false};

        // 滚动线程：每80ms向客户端发送一组随机号码
        Thread rollThread = new Thread(() -> {
            Random rand = new Random();
            while (!stopped[0] && !Thread.currentThread().isInterrupted()) {
                Set<Integer> numSet = new HashSet<>();
                while (numSet.size() < LotteryService.NUMBERS_PER_TICKET) {
                    numSet.add(rand.nextInt(LotteryService.NUMBER_POOL_SIZE) + 1);
                }
                StringBuilder sb = new StringBuilder("ROLL:");
                int i = 0;
                for (int num : numSet) {
                    if (i > 0) sb.append(",");
                    sb.append(String.format("%02d", num));
                    i++;
                }
                writer.println(sb.toString());
                try {
                    Thread.sleep(ROLL_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "DrawServer-Roll");
        rollThread.setDaemon(true);
        rollThread.start();

        // 等待客户端发送停止命令
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if ("DRAW_STOP".equals(line)) {
                    stopped[0] = true;
                    rollThread.interrupt();
                    break;
                }
            }
        } catch (IOException ignored) {}

        // 等待滚动线程完全停止
        try {
            rollThread.join(500);
        } catch (InterruptedException ignored) {}

        // 发送最终开奖结果
        StringBuilder result = new StringBuilder("RESULT:");
        for (int i = 0; i < finalNumbers.length; i++) {
            if (i > 0) result.append(",");
            result.append(String.format("%02d", finalNumbers[i]));
        }
        writer.println(result.toString());
    }

    /**
     * 独立启动抽奖服务器
     * @param args 命令行参数（可选端口号）
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("端口号格式错误，使用默认端口 " + DEFAULT_PORT);
            }
        }
        DrawServer server = new DrawServer(port);
        server.start();
        System.out.println("抽奖服务器运行中... 输入 'quit' 停止服务器");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (server.isRunning()) {
            String input = scanner.nextLine();
            if ("quit".equalsIgnoreCase(input.trim())) {
                server.stop();
                break;
            }
        }
        scanner.close();
        System.out.println("服务器已退出");
    }
}
