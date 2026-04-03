package analyzer;

public class LexError {
    private final String message;
    private final int line;
    private final int column;

    public LexError(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
