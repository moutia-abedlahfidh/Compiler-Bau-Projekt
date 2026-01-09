package parser;

import lexer.Token;

/**
 * Laufzeit-Ausnahme, die beim Parsen geworfen wird. Behält das fehlerhafte Token
 * bei, damit Aufrufer die Position (line/col) oder den Tokentext ermitteln können.
 */
public class ParseException extends RuntimeException {
    private final Token token;

    public ParseException(String message, Token token) {
        super(message + " at " + (token != null ? ("pos=" + token.pos + " token=" + token) : "<unknown>"));
        this.token = token;
    }

    public Token getToken() { return token; }
}
