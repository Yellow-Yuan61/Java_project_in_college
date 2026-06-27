# 福彩36选7 彩票购买抽奖系统

## 项目简介
本项目是一个模拟福利彩票36选7的购买抽奖系统，使用Java语言开发。系统实现了用户注册登录、彩票购买（手动选号/随机选号）、开奖动画、中奖通知等完整功能，并支持10万用户级别的自动化测试。

## 技术栈
| 技术 | 说明 |
|------|------|
| Java Swing | 图形用户界面 |
| Socket编程 | C/S架构抽奖服务器 |
| 多线程 | 号码滚动动画、Socket并发、AutoTest并发 |
| MVC设计模式 | Model-View-Controller架构分离 |
| 单例模式 | DataStore全局数据存储 |
| 观察者模式 | 中奖通知机制 |
| 正则表达式 | 输入校验 |
| Stream API | 数据统计与过滤 |
| 文件存储 | 自定义格式数据持久化 |

## 目录结构
```
├── src/                    # 源代码
│   ├── ui/                 # GUI界面 (View层)
│   │   ├── App.java
│   │   ├── LoginFrame.java
│   │   ├── MainFrame.java
│   │   ├── BuyPanel.java
│   │   ├── DrawPanel.java
│   │   ├── HistoryPanel.java
│   │   └── UserPanel.java
│   ├── logic/              # 业务逻辑
│   │   ├── model/          # Model实体
│   │   │   ├── User.java
│   │   │   ├── Ticket.java
│   │   │   └── DrawResult.java
│   │   ├── service/        # Controller业务逻辑
│   │   │   ├── UserService.java
│   │   │   ├── LotteryService.java
│   │   │   └── DrawServer.java
│   │   ├── storage/        # 数据持久化
│   │   │   └── DataStore.java
│   │   └── util/           # 工具类
│   │       ├── Validator.java
│   │       └── IDGenerator.java
│   └── test/
│       └── AutoTest.java   # 10万用户自动测试
├── classes/                # 编译输出
├── doc/                    # 项目文档
├── bin/                    # 批处理脚本
│   ├── build.bat
│   ├── run.bat
│   ├── run_server.bat
│   └── run_test.bat
├── img/                    # 图片资源
├── data/                   # 运行时数据文件
├── MANIFEST.MF
├── .gitignore
└── README.md
```

## 环境要求
- JDK 8 或以上（推荐 JDK 17）
- Windows / Linux / macOS

## 部署方法

### 1. 编译
双击 `bin/build.bat` 或在项目根目录执行：
```bash
javac -encoding UTF-8 -d classes -sourcepath src src/ui/App.java src/test/AutoTest.java
```

### 2. 运行GUI程序
双击 `bin/run.bat` 或在项目根目录执行：
```bash
java -cp classes ui.App
```

### 3. 独立运行抽奖服务器
双击 `bin/run_server.bat` 或在项目根目录执行：
```bash
java -cp classes logic.service.DrawServer
```

### 4. 运行自动测试（10万用户）
双击 `bin/run_test.bat` 或在项目根目录执行：
```bash
java -cp classes test.AutoTest
```

### 5. 打包为JAR文件
在项目根目录执行：
```bash
cd classes
jar cvfm ../LotterySystem.jar ../MANIFEST.MF .
```
运行JAR：
```bash
java -jar LotterySystem.jar
```

## 使用说明

1. **注册/登录**：启动程序后，点击「切换注册/登录」切换到注册模式，填写用户名（字母开头3-16位）、密码（6-20位）和电话号码进行注册。注册成功后自动切换回登录模式。
2. **购买彩票**：登录后在「购买彩票」标签页，可以手动点击号码球选号（需选7个），或点击「随机选号购买」。设置投注倍数（1-100倍）。
3. **开奖**：切换到「开奖区」标签，点击「开始抽奖」按钮，号码开始滚动。点击「停止」按钮产生开奖结果。系统自动兑奖并通知中奖用户。
4. **查看记录**：在「历史记录」标签页查看个人购票历史和所有开奖结果。
5. **个人中心**：查看/充值账户余额，查看中奖记录。

## 中奖规则
- **特等奖**：7个号码全中 → 奖金 = 5,000,000 × 投注倍数
- **一等奖**：中6个号码 → 奖金 = 50,000 × 投注倍数

## 通信协议（Socket）
```
Client → Server: DRAW_START     # 开始抽奖
Server → Client: ROLL:n1,...,n7 # 滚动号码（每80ms）
Client → Server: DRAW_STOP      # 停止抽奖
Server → Client: RESULT:n1,...,n7 # 最终开奖号码
```
服务器默认监听端口：9527

## 作者
Yuan
