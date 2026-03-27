# 编译原理实践：Java 词法分析器设计（Swing GUI）

本项目是一个适用于课程实验与课堂答辩展示的 **手写词法分析器**。
实现语言为 Java，界面使用 Swing，完整展示“输入源程序 -> 词法分析 -> 输出 Token/错误信息”的流程。

## 1. 项目目标

- 支持源代码输入与文件导入
- 对输入执行逐字符扫描的词法分析
- 输出 Token 列表（含类别、行号、列号）
- 输出错误信息（非法字符、未闭合字符串/注释、数字格式错误等）
- 维护并展示基础符号表（标识符）

## 2. 功能说明

### 输入功能
- 文本区域直接输入源代码
- 一键清空输入和输出结果
- 导入本地 `.txt` / `.java` 文件

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

## 3. 项目结构

```text
src/analyzer/
  Main.java           # 程序入口
  LexerGUI.java       # Swing 图形界面
  Lexer.java          # 词法分析核心（手写扫描器）
  TokenType.java      # Token 类型定义
  Token.java          # Token 数据结构
  LexError.java       # 错误信息结构
  SymbolEntry.java    # 符号表项
  LexerResult.java    # 词法分析结果封装
  FileUtil.java       # 文件读取工具

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

1. 在“源程序输入区”粘贴或输入代码（也可点击“导入文件”）。
2. 点击“开始分析”。
3. 在下方标签页查看：
   - `Token 列表`
   - `符号表`
   - `错误信息`
4. 底部状态栏会显示分析统计信息。

## 6. 测试样例建议

- `samples/sample_ok_1.txt`：基础关键字、标识符、运算符、界符
- `samples/sample_ok_2.txt`：浮点、字符常量、字符串、复合运算符、注释
- `samples/sample_error.txt`：多种词法错误场景

## 7. 简单测试说明

- 使用 `javac` 编译所有源码，确保无语法错误
- 使用临时命令行驱动调用 `Lexer.analyze()`，检查 token/错误数量
- 在本地图形环境运行 GUI 并手动验证输入、清空、导入、分析流程
