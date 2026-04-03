package analyzer;

import java.util.List;

public class LexerResult {
    private final List<Token> tokens;
    private final List<SymbolEntry> symbols;
    private final List<LexError> errors;

    public LexerResult(List<Token> tokens, List<SymbolEntry> symbols, List<LexError> errors) {
        this.tokens = tokens;
        this.symbols = symbols;
        this.errors = errors;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<SymbolEntry> getSymbols() {
        return symbols;
    }

    public List<LexError> getErrors() {
        return errors;
    }
}
