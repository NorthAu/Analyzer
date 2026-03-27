package analyzer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                if (ch == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
                index++;
                continue;
            }

            int startLine = line;
            int startCol = col;

            if (Character.isJavaIdentifierStart(ch)) {
                StringBuilder sb = new StringBuilder();
                while (index < source.length() && Character.isJavaIdentifierPart(source.charAt(index))) {
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
                    errors.add(new LexError("Malformed float literal: " + lexeme, startLine, startCol));
                    tokens.add(new Token(TokenType.ERROR, lexeme, startLine, startCol));
                } else {
                    tokens.add(new Token(hasDot ? TokenType.FLOAT : TokenType.INTEGER, lexeme, startLine, startCol));
                }
                continue;
            }

            if (ch == '"') {
                StringBuilder sb = new StringBuilder();
                sb.append(ch);
                index++;
                col++;
                boolean closed = false;

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
                        closed = true;
                        break;
                    }
                    if (c == '\n') {
                        line++;
                        col = 1;
                    }
                }

                if (closed) {
                    tokens.add(new Token(TokenType.STRING, sb.toString(), startLine, startCol));
                } else {
                    errors.add(new LexError("Unterminated string literal", startLine, startCol));
                    tokens.add(new Token(TokenType.ERROR, sb.toString(), startLine, startCol));
                }
                continue;
            }

            if (ch == '/') {
                if (index + 1 < source.length()) {
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
                        tokens.add(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol));
                        continue;
                    }
                    if (next == '*') {
                        StringBuilder sb = new StringBuilder("/*");
                        index += 2;
                        col += 2;
                        boolean closed = false;
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
                                closed = true;
                                break;
                            }
                        }
                        if (closed) {
                            tokens.add(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol));
                        } else {
                            errors.add(new LexError("Unterminated block comment", startLine, startCol));
                            tokens.add(new Token(TokenType.ERROR, sb.toString(), startLine, startCol));
                        }
                        continue;
                    }
                }
                tokens.add(new Token(TokenType.OPERATOR, "/", startLine, startCol));
                index++;
                col++;
                continue;
            }

            String two = index + 1 < source.length() ? source.substring(index, index + 2) : "";
            if (Set.of("==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=").contains(two)) {
                tokens.add(new Token(TokenType.OPERATOR, two, startLine, startCol));
                index += 2;
                col += 2;
                continue;
            }

            if (Set.of('+', '-', '*', '%', '=', '>', '<', '!', '&', '|', '^', '~', '?', ':').contains(ch)) {
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

            errors.add(new LexError("Unknown character: " + ch, startLine, startCol));
            tokens.add(new Token(TokenType.ERROR, String.valueOf(ch), startLine, startCol));
            index++;
            col++;
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line, col));
        return new LexerResult(tokens, new ArrayList<>(symbolMap.values()), errors);
    }
}
