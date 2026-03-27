# 编译原理实践：Java 词法分析器设计（Swing GUI）

本项目是一个适用于课程实验与课堂答辩展示的 **手写词法分析器**。
实现语言为 Java，界面使用 Swing，完整展示“输入源程序 -> 词法分析 -> 输出 Token/错误信息”的流程。

## 1. 项目目标

- 支持源代码输入与文件操作（打开/保存/导出）
- 对输入执行逐字符扫描的词法分析
- 输出 Token 序列（词素、类别、行号、列号）
- 输出错误信息（非法字符、未闭合字符串/注释、数字格式错误等）
- 维护并展示基础符号表（标识符）

## 2. 功能说明

### 输入与编辑功能
- 文本区域直接输入源代码
- 新建、清空全部、清空结果
- 打开本地 `.txt` / `.java` 文件
- 保存当前输入到文件
- 常用编辑动作：剪切、复制、粘贴

### 词法分析支持的 Token
- 关键字（KEYWORD）
- 标识符（IDENTIFIER）
- 整数常量（INTEGER）
- 浮点常量（FLOAT）
- 字符串常量（STRING）
- 字符常量（CHAR）
- 运算符（OPERATOR，含最长匹配：`>= <= == != && || ++ -- += -= *= /=` 等）
- 界符（DELIMITER，`;,(){}[] .`）
- 注释（COMMENT，支持 `//` 与 `/*...*/`）
- 错误 Token（ERROR）

### 错误处理
- 非法字符
- 无法识别 token（如 `123abc`）
- 字符串未闭合
- 块注释未闭合
- 数字格式错误（如 `12.`）

### 结果展示
- 右上：Token 输出区（文本表格）
- 右下左：符号表（表格）
- 右下右：错误信息（表格）
- 底部状态栏：统计 Token / 符号 / 错误数量

## 3. 项目结构

```text
src/analyzer/
  Main.java           # 程序入口
  LexerGUI.java       # Swing 图形界面（菜单栏+工具栏+三分栏）
  Lexer.java          # 词法分析核心（手写扫描器）
  TokenType.java      # Token 类型定义
  Token.java          # Token 数据结构
  LexError.java       # 错误信息结构
  SymbolEntry.java    # 符号表项
  LexerResult.java    # 词法分析结果封装
  FileUtil.java       # 文件读写工具

samples/
  sample_ok_1.txt     # 正常样例 1
  sample_ok_2.txt     # 正常样例 2
  sample_error.txt    # 错误样例
```

## 4. 运行方式

在项目根目录执行：

```bash
javac -d out src/analyzer/*.java
java -cp out analyzer.Main
```

> 说明：在无图形界面的环境（如部分 CI）运行 GUI 会报 `HeadlessException`，属于环境限制。

## 5. 界面说明（演示流程）

1. 在左侧“源程序输入区”输入代码（或“打开”文件）。
2. 点击工具栏“分析”或菜单“词法分析 -> 开始分析”。
3. 在右侧查看：
   - Token 输出区
   - 符号表
   - 错误信息
4. 可使用“导出Token”将 Token 输出保存到文本文件。

## 6. 测试样例建议

- `samples/sample_ok_1.txt`：基础关键字、标识符、运算符、界符
- `samples/sample_ok_2.txt`：浮点、字符常量、字符串、复合运算符、注释
- `samples/sample_error.txt`：多种词法错误场景

## 7. 简单测试说明

- 使用 `javac` 编译所有源码，确保无语法错误
- 在本地图形环境中验证：新建/打开/保存/编辑/分析/导出Token功能
- 使用错误样例验证错误表是否包含行列号与错误信息
