package analyzer;

public class SymbolEntry {
    private final String name;
    private final String kind;
    private final int firstLine;

    public SymbolEntry(String name, String kind, int firstLine) {
        this.name = name;
        this.kind = kind;
        this.firstLine = firstLine;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getFirstLine() {
        return firstLine;
    }
}
