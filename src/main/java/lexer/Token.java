package lexer;

/**
 * Repräsentiert ein Token, erzeugt vom Tokenizer.
 * Enthält den Token-Typ, den originalen Text, sowie Position (Offset, Zeile, Spalte).
 */
public class Token {
    public final TokenType type;
    public final String text;
    public final int pos; // offset in input (0-based)
    public final int line; // 1-based line number
    public final int col; // 1-based column number

    public Token(TokenType type, String text, int pos) {
        this(type, text, pos, 1, pos + 1);
    }

    public Token(TokenType type, String text, int pos, int line, int col) {
        this.type = type;
        this.text = text;
        this.pos = pos;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return type + (text != null && !text.isEmpty() ? ("(" + text + ")") : "") + "@" + pos + "[" + line + "," + col + "]";
    }
}
