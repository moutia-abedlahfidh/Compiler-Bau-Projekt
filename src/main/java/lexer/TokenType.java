package lexer;

/**
 * Token-Typen, die vom Tokenizer erzeugt werden können.
 */
public enum TokenType {
    IDENT, NUMBER,
    IF, ELSE, WHILE, FOR,
    FUNCTION, RETURN,
    PLUS, MINUS, STAR, SLASH,
    COMMA,
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    ASSIGN, SEMI,
    // Vergleiche
    EQ, NEQ, LT, GT, LE, GE,
    EOF
}
