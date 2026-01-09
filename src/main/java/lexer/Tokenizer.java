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
                // leading digits
                if (Character.isDigit(peek())) {
                    while (Character.isDigit(peek())) sb.append(next());
                }
                // fractional part
                if (peek() == '.') {
                    sb.append(next()); // consume '.'
                    while (Character.isDigit(peek())) sb.append(next());
                }
                // exponent part (optional)
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
            // comments: // line comment or /* block comment */
            if (peek() == '/') {
                if ((pos + 1) < input.length() && input.charAt(pos + 1) == '/') {
                    // consume '//' and skip to end of line
                    next(); next();
                    while (peek() != '\n' && peek() != '\0') next();
                    // consume newline if present
                        if (peek() == '\n') next();
                    continue;
                }
                if ((pos + 1) < input.length() && input.charAt(pos + 1) == '*') {
                    // consume '/*' and skip until '*/'
                    next(); next();
                    while (true) {
                        if (peek() == '\0') throw new RuntimeException("Unterminated block comment");
                if (peek() == '*' && (pos + 1) < input.length() && input.charAt(pos + 1) == '/') {
                    int endLine = line;
                    int endCol = col;
                    next(); next(); // consume '*/'
                            break;
                        }
                        next();
                    }
                    continue;
                }
            }
            // treat '#' as a line comment (common in test files)
            if (peek() == '#') {
                // consume '#' and skip to end of line
                next();
                while (peek() != '\n' && peek() != '\0') next();
                if (peek() == '\n') next();
                continue;
            }
            char op = next();
            int opPos = pos - 1;
            // handle two-char operators: ==, !=, <=, >=
                if (op == '=') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.EQ, "==", opPos, line, col - 1)); }
                    else { tokens.add(new Token(TokenType.ASSIGN, "=", opPos, line, col - 1)); }
                    continue;
                }
                if (op == '!') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.NEQ, "!=", opPos, line, col - 1)); continue; }
                    throw new RuntimeException("Unexpected character: ! at " + opPos);
                }
                if (op == '<') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.LE, "<=", opPos, line, col - 1)); }
                    else { tokens.add(new Token(TokenType.LT, "<", opPos, line, col - 1)); }
                    continue;
                }
                if (op == '>') {
                    if (peek() == '=') { next(); tokens.add(new Token(TokenType.GE, ">=", opPos, line, col - 1)); }
                    else { tokens.add(new Token(TokenType.GT, ">", opPos, line, col - 1)); }
                    continue;
                }
            switch (op) {
                case '+': tokens.add(new Token(TokenType.PLUS, "+", opPos, line, col - 1)); break;
                case '-': tokens.add(new Token(TokenType.MINUS, "-", opPos, line, col - 1)); break;
                case '*': tokens.add(new Token(TokenType.STAR, "*", opPos, line, col - 1)); break;
                case '/': tokens.add(new Token(TokenType.SLASH, "/", opPos, line, col - 1)); break;
                case '(' : tokens.add(new Token(TokenType.LPAREN, "(", opPos, line, col - 1)); break;
                case ')' : tokens.add(new Token(TokenType.RPAREN, ")", opPos, line, col - 1)); break;
                case '{' : tokens.add(new Token(TokenType.LBRACE, "{", opPos, line, col - 1)); break;
                case '}' : tokens.add(new Token(TokenType.RBRACE, "}", opPos, line, col - 1)); break;
                case ';' : tokens.add(new Token(TokenType.SEMI, ";", opPos, line, col - 1)); break;
                case ',' : tokens.add(new Token(TokenType.COMMA, ",", opPos, line, col - 1)); break;
                default:
                    throw new RuntimeException("Unexpected character: " + op + " at " + opPos);
            }
        }
        return tokens;
    }
}
