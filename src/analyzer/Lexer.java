package analyzer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 手写词法分析器：将字符流扫描为 Token 序列。
 * 设计目标：结构清晰、便于课程答辩展示。
 */
public class Lexer {
    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"
    );

    private static final Set<Character> DELIMITERS = Set.of('(', ')', '{', '}', '[', ']', ';', ',', '.');
    private static final Set<String> DOUBLE_OPERATORS = Set.of(
            "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=", "%=", "->"
    );
    private static final Set<Character> SINGLE_OPERATORS = Set.of(
            '+', '-', '*', '/', '%', '=', '>', '<', '!', '&', '|', '^', '~', '?', ':'
    );

    public LexerResult analyze(String source) {
        List<Token> tokens = new ArrayList<>();
        List<LexError> errors = new ArrayList<>();
        Map<String, SymbolEntry> symbolMap = new LinkedHashMap<>();

        int index = 0;
        int line = 1;
        int col = 1;

        while (index < source.length()) {
            char ch = source.charAt(index);

            if (Character.isWhitespace(ch)) {
                int[] p = consumeWhitespace(source, index, line, col);
                index = p[0];
                line = p[1];
                col = p[2];
                continue;
            }

            int startLine = line;
            int startCol = col;

            if (isIdentifierStart(ch)) {
                StringBuilder sb = new StringBuilder();
                while (index < source.length() && isIdentifierPart(source.charAt(index))) {
                    sb.append(source.charAt(index));
                    index++;
                    col++;
                }

                String lexeme = sb.toString();
                if (KEYWORDS.contains(lexeme)) {
                    tokens.add(new Token(TokenType.KEYWORD, lexeme, startLine, startCol));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, lexeme, startLine, startCol));
                    symbolMap.putIfAbsent(lexeme, new SymbolEntry(lexeme, "identifier", startLine));
                }
                continue;
            }

            if (Character.isDigit(ch)) {
                ParseResult numberResult = parseNumber(source, index, line, col);
                tokens.add(numberResult.token());
                if (numberResult.error() != null) {
                    errors.add(numberResult.error());
                }
                index = numberResult.nextIndex();
                line = numberResult.nextLine();
                col = numberResult.nextCol();
                continue;
            }

            if (ch == '"') {
                ParseResult stringResult = parseString(source, index, line, col);
                tokens.add(stringResult.token());
                if (stringResult.error() != null) {
                    errors.add(stringResult.error());
                }
                index = stringResult.nextIndex();
                line = stringResult.nextLine();
                col = stringResult.nextCol();
                continue;
            }

            if (ch == '\'') {
                ParseResult charResult = parseCharLiteral(source, index, line, col);
                tokens.add(charResult.token());
                if (charResult.error() != null) {
                    errors.add(charResult.error());
                }
                index = charResult.nextIndex();
                line = charResult.nextLine();
                col = charResult.nextCol();
                continue;
            }

            if (ch == '/') {
                ParseResult slashResult = parseSlashRelated(source, index, line, col);
                tokens.add(slashResult.token());
                if (slashResult.error() != null) {
                    errors.add(slashResult.error());
                }
                index = slashResult.nextIndex();
                line = slashResult.nextLine();
                col = slashResult.nextCol();
                continue;
            }

            String twoChars = index + 1 < source.length() ? source.substring(index, index + 2) : "";
            if (DOUBLE_OPERATORS.contains(twoChars)) {
                tokens.add(new Token(TokenType.OPERATOR, twoChars, startLine, startCol));
                index += 2;
                col += 2;
                continue;
            }

            if (SINGLE_OPERATORS.contains(ch)) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(ch), startLine, startCol));
                index++;
                col++;
                continue;
            }

            if (DELIMITERS.contains(ch)) {
                tokens.add(new Token(TokenType.DELIMITER, String.valueOf(ch), startLine, startCol));
                index++;
                col++;
                continue;
            }

            errors.add(new LexError("非法字符: " + ch, startLine, startCol));
            tokens.add(new Token(TokenType.ERROR, String.valueOf(ch), startLine, startCol));
            index++;
            col++;
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line, col));
        return new LexerResult(tokens, new ArrayList<>(symbolMap.values()), errors);
    }

    private int[] consumeWhitespace(String source, int index, int line, int col) {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            if (source.charAt(index) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            index++;
        }
        return new int[]{index, line, col};
    }

    private ParseResult parseNumber(String source, int index, int line, int col) {
        int startLine = line;
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        while (index < source.length()) {
            char c = source.charAt(index);
            if (Character.isDigit(c)) {
                sb.append(c);
                index++;
                col++;
            } else if (c == '.' && !hasDot) {
                hasDot = true;
                sb.append(c);
                index++;
                col++;
            } else {
                break;
            }
        }

        String lexeme = sb.toString();

        if (lexeme.endsWith(".")) {
            return new ParseResult(
                    new Token(TokenType.ERROR, lexeme, startLine, startCol),
                    new LexError("数字格式错误: " + lexeme, startLine, startCol),
                    index, line, col
            );
        }

        if (index < source.length() && isIdentifierStart(source.charAt(index))) {
            while (index < source.length() && isIdentifierPart(source.charAt(index))) {
                sb.append(source.charAt(index));
                index++;
                col++;
            }
            lexeme = sb.toString();
            return new ParseResult(
                    new Token(TokenType.ERROR, lexeme, startLine, startCol),
                    new LexError("无法识别的数字 token: " + lexeme, startLine, startCol),
                    index, line, col
            );
        }

        TokenType type = hasDot ? TokenType.FLOAT : TokenType.INTEGER;
        return new ParseResult(new Token(type, lexeme, startLine, startCol), null, index, line, col);
    }

    private ParseResult parseString(String source, int index, int line, int col) {
        int startLine = line;
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        index++;
        col++;

        while (index < source.length()) {
            char c = source.charAt(index);
            sb.append(c);
            index++;
            col++;

            if (c == '\\' && index < source.length()) {
                sb.append(source.charAt(index));
                index++;
                col++;
                continue;
            }

            if (c == '"') {
                return new ParseResult(new Token(TokenType.STRING, sb.toString(), startLine, startCol), null, index, line, col);
            }

            if (c == '\n') {
                line++;
                col = 1;
            }
        }

        return new ParseResult(
                new Token(TokenType.ERROR, sb.toString(), startLine, startCol),
                new LexError("字符串未闭合", startLine, startCol),
                index, line, col
        );
    }

    private ParseResult parseCharLiteral(String source, int index, int line, int col) {
        int startLine = line;
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        sb.append('\'');
        index++;
        col++;

        if (index >= source.length() || source.charAt(index) == '\n') {
            return new ParseResult(
                    new Token(TokenType.ERROR, sb.toString(), startLine, startCol),
                    new LexError("字符常量未闭合", startLine, startCol),
                    index, line, col
            );
        }

        char c = source.charAt(index);
        sb.append(c);
        index++;
        col++;

        if (c == '\\' && index < source.length()) {
            sb.append(source.charAt(index));
            index++;
            col++;
        }

        if (index < source.length() && source.charAt(index) == '\'') {
            sb.append('\'');
            index++;
            col++;
            return new ParseResult(new Token(TokenType.CHAR, sb.toString(), startLine, startCol), null, index, line, col);
        }

        while (index < source.length() && source.charAt(index) != '\n' && source.charAt(index) != '\'') {
            sb.append(source.charAt(index));
            index++;
            col++;
        }
        if (index < source.length() && source.charAt(index) == '\'') {
            sb.append('\'');
            index++;
            col++;
        }

        return new ParseResult(
                new Token(TokenType.ERROR, sb.toString(), startLine, startCol),
                new LexError("字符常量格式错误", startLine, startCol),
                index, line, col
        );
    }

    private ParseResult parseSlashRelated(String source, int index, int line, int col) {
        int startLine = line;
        int startCol = col;

        if (index + 1 >= source.length()) {
            return new ParseResult(new Token(TokenType.OPERATOR, "/", startLine, startCol), null, index + 1, line, col + 1);
        }

        char next = source.charAt(index + 1);
        if (next == '/') {
            StringBuilder sb = new StringBuilder("//");
            index += 2;
            col += 2;
            while (index < source.length() && source.charAt(index) != '\n') {
                sb.append(source.charAt(index));
                index++;
                col++;
            }
            return new ParseResult(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol), null, index, line, col);
        }

        if (next == '*') {
            StringBuilder sb = new StringBuilder("/*");
            index += 2;
            col += 2;

            while (index < source.length()) {
                char c = source.charAt(index);
                sb.append(c);
                index++;

                if (c == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }

                if (c == '*' && index < source.length() && source.charAt(index) == '/') {
                    sb.append('/');
                    index++;
                    col++;
                    return new ParseResult(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol), null, index, line, col);
                }
            }

            return new ParseResult(
                    new Token(TokenType.ERROR, sb.toString(), startLine, startCol),
                    new LexError("块注释未闭合", startLine, startCol),
                    index, line, col
            );
        }

        if (next == '=') {
            return new ParseResult(new Token(TokenType.OPERATOR, "/=", startLine, startCol), null, index + 2, line, col + 2);
        }

        return new ParseResult(new Token(TokenType.OPERATOR, "/", startLine, startCol), null, index + 1, line, col + 1);
    }

    private boolean isIdentifierStart(char ch) {
        return ch == '_' || Character.isLetter(ch);
    }

    private boolean isIdentifierPart(char ch) {
        return ch == '_' || Character.isLetterOrDigit(ch);
    }

    private record ParseResult(Token token, LexError error, int nextIndex, int nextLine, int nextCol) {
    }
}
