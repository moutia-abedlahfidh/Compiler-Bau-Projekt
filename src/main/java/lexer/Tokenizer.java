package lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Zerlegt Eingabetext in eine Liste von `Token`-Objekten.
 * Unterstützt Identifier, Zahlen (inkl. Dezimal- und Exponentialnotation),
 * Operatoren, Klammern, Semikolons und Kommentare (Zeilen- und Blockkommentare).
 */
public class Tokenizer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    public Tokenizer(String input) {
        this.input = input != null ? input : "";
        this.line = 1;
        this.col = 1;
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private char next() {
        if (pos >= input.length()) return '\0';
        char c = input.charAt(pos++);
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(peek())) next();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            char c = peek();
            if (c == '\0') {
                tokens.add(new Token(TokenType.EOF, "", pos, line, col));
                break;
            }
            if (Character.isLetter(c) || c == '_') {
                int start = pos;
                StringBuilder sb = new StringBuilder();
                while (Character.isLetterOrDigit(peek()) || peek() == '_') sb.append(next());
                String text = sb.toString();
                TokenType tt = switch (text) {
                    case "if" -> TokenType.IF;
                    case "else" -> TokenType.ELSE;
                    case "while" -> TokenType.WHILE;
                    case "for" -> TokenType.FOR;
                    case "function" -> TokenType.FUNCTION;
                    case "return" -> TokenType.RETURN;
                    default -> TokenType.IDENT;
                };
                tokens.add(new Token(tt, text, start, line, col - (sb.length())));
                continue;
            }
            if (Character.isDigit(c) || (c == '.' && (pos + 1) < input.length() && Character.isDigit(input.charAt(pos + 1)))) {
                int start = pos;
                StringBuilder sb = new StringBuilder();
                // fuehrende Ziffern
                if (Character.isDigit(peek())) {
                    while (Character.isDigit(peek())) sb.append(next());
                }
                // Nachkommanteil
                if (peek() == '.') {
                    sb.append(next()); // '.' konsumieren
                    while (Character.isDigit(peek())) sb.append(next());
                }
                // Exponententeil (optional)
                if (peek() == 'e' || peek() == 'E') {
                    sb.append(next());
                    if (peek() == '+' || peek() == '-') sb.append(next());
                    boolean hasExpDigits = false;
                    while (Character.isDigit(peek())) { hasExpDigits = true; sb.append(next()); }
                    if (!hasExpDigits) throw new RuntimeException("Invalid numeric literal (missing exponent digits): " + sb.toString());
                }
                tokens.add(new Token(TokenType.NUMBER, sb.toString(), start, line, col - (sb.length())));
                continue;
            }
            // Kommentare: // Zeilenkommentar oder /* Blockkommentar */
            if (peek() == '/') {
                if ((pos + 1) < input.length() && input.charAt(pos + 1) == '/') {
                    // '//' konsumieren und bis Zeilenende ueberspringen
                    next(); next();
                    while (peek() != '\n' && peek() != '\0') next();
                    // Zeilenumbruch konsumieren, falls vorhanden
                        if (peek() == '\n') next();
                    continue;
                }
                if ((pos + 1) < input.length() && input.charAt(pos + 1) == '*') {
                    // '/*' konsumieren und bis '*/' ueberspringen
                    next(); next();
                    while (true) {
                        if (peek() == '\0') throw new RuntimeException("Unterminated block comment");
                if (peek() == '*' && (pos + 1) < input.length() && input.charAt(pos + 1) == '/') {
                    next(); next(); // '*/' konsumieren
                    break;
                }
                        next();
                    }
                    continue;
                }
            }
            // '#' als Zeilenkommentar behandeln (haeufig in Testdateien)
            if (peek() == '#') {
                // '#' konsumieren und bis Zeilenende ueberspringen
                next();
                while (peek() != '\n' && peek() != '\0') next();
                if (peek() == '\n') next();
                continue;
            }
            int startPos = pos;
            int startLine = line;
            int startCol = col;
            char op = next();
            // Zwei-Zeichen-Operatoren behandeln: ==, !=, <=, >=
                if (op == '=') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.EQ, "==", startPos, startLine, startCol)); }
                    else { tokens.add(new Token(TokenType.ASSIGN, "=", startPos, startLine, startCol)); }
                    continue;
                }
                if (op == '!') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.NEQ, "!=", startPos, startLine, startCol)); continue; }
                    throw new RuntimeException("Unexpected character: ! at " + startPos);
                }
                if (op == '<') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.LE, "<=", startPos, startLine, startCol)); }
                    else { tokens.add(new Token(TokenType.LT, "<", startPos, startLine, startCol)); }
                    continue;
                }
                if (op == '>') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.GE, ">=", startPos, startLine, startCol)); }
                    else { tokens.add(new Token(TokenType.GT, ">", startPos, startLine, startCol)); }
                    continue;
                }
            switch (op) {
                case '+' -> tokens.add(new Token(TokenType.PLUS, "+", startPos, startLine, startCol));
                case '-' -> tokens.add(new Token(TokenType.MINUS, "-", startPos, startLine, startCol));
                case '*' -> tokens.add(new Token(TokenType.STAR, "*", startPos, startLine, startCol));
                case '/' -> tokens.add(new Token(TokenType.SLASH, "/", startPos, startLine, startCol));
                case '(' -> tokens.add(new Token(TokenType.LPAREN, "(", startPos, startLine, startCol));
                case ')' -> tokens.add(new Token(TokenType.RPAREN, ")", startPos, startLine, startCol));
                case '{' -> tokens.add(new Token(TokenType.LBRACE, "{", startPos, startLine, startCol));
                case '}' -> tokens.add(new Token(TokenType.RBRACE, "}", startPos, startLine, startCol));
                case ';' -> tokens.add(new Token(TokenType.SEMI, ";", startPos, startLine, startCol));
                case ',' -> tokens.add(new Token(TokenType.COMMA, ",", startPos, startLine, startCol));
                default -> throw new RuntimeException("Unexpected character: " + op + " at " + startPos);
            }
        }
        return tokens;
    }
}
