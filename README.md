# Analyzer - Java 词法分析器（Swing GUI）

这是一个使用 **Java** 编写的简易词法分析器，带有 Swing 图形界面。

## 支持功能

- 关键字（`if`, `while`, `class` 等）
- 标识符
- 整数 / 浮点数
- 运算符（单字符与双字符）
- 界符（`(){},.;[]` 等）
- 注释（`//` 与 `/* ... */`）
- 字符串字面量
- 错误检测（未知字符、未闭合字符串、未闭合注释、非法浮点形式）

## 输出内容

- Token 表
- 符号表
- 错误表

## 目录结构

```text
src/analyzer/
  Main.java
  LexerGUI.java
  Lexer.java
  LexerResult.java
  Token.java
  TokenType.java
  SymbolEntry.java
  LexError.java
```

## 编译与运行

在项目根目录执行：

```bash
javac -d out src/analyzer/*.java
java -cp out analyzer.Main
```

启动后可在界面输入 Java 代码并点击“开始词法分析”。
