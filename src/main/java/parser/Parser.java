package parser;

import ast.*;
import java.util.ArrayList;
import java.util.List;
import lexer.Token;
import lexer.TokenType;
import lexer.Tokenizer;

/**
 * Ein einfacher rekursiver-Descent-Parser für Ausdrücke, Zuweisungen und Sequenzen.
 * Produziert einen AST, der über `eval(Map<String,Double>)` ausgewertet werden kann.
 * Unterstützt Vergleiche, arithmetische Operatoren, Unary-Operatoren und Semikolon-getrennte Sequenzen.
 */
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(String input) {
        this.tokens = new Tokenizer(input).tokenize();
    }

    private Token peek() { return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF, "", pos); }
    private Token next() { return pos < tokens.size() ? tokens.get(pos++) : new Token(TokenType.EOF, "", pos); }

    private void eat(TokenType t) {
        if (peek().type != t) throw new ParseException("Expected " + t + " but found " + peek(), peek());
        next();
    }

    public Node parse() {
        List<Node> stmts = new ArrayList<>();
        while (peek().type != TokenType.EOF) {
            if (peek().type == TokenType.SEMI) { next(); continue; }
            stmts.add(parseStatement());
            while (peek().type == TokenType.SEMI) next();
        }
        if (stmts.size() == 0) return new SequenceNode(new ArrayList<>(), -1, -1);
        if (stmts.size() == 1) return stmts.get(0);
        return new SequenceNode(stmts, -1, -1);
    }

    /**
     * Parst ein Statement oder einen Block. Ein Block ist `{ stmt* }` und liefert eine SequenceNode.
     */
    private Node parseStatement() {
        if (peek().type == TokenType.LBRACE) {
            Token lb = next(); // '{' konsumieren
            List<Node> stmts = new ArrayList<>();
            while (peek().type != TokenType.RBRACE && peek().type != TokenType.EOF) {
                if (peek().type == TokenType.SEMI) { next(); continue; }
                stmts.add(parseStatement());
                while (peek().type == TokenType.SEMI) next();
            }
            eat(TokenType.RBRACE);
            if (stmts.size() == 0) return new SequenceNode(new ArrayList<>(), lb.line, lb.col);
            if (stmts.size() == 1) return stmts.get(0);
            return new SequenceNode(stmts, lb.line, lb.col);
        }
        return parseStart();
    }

    private Node parseStart() {
        // Keywords: IF, WHILE, FOR sind eigene Token-Typen
        if (peek().type == TokenType.IF) {
            Token ifTok = next(); // 'if' konsumieren
            eat(TokenType.LPAREN);
            Node cond = parseComparison();
            eat(TokenType.RPAREN);
            Node thenBranch = parseStatement();
            Node elseBranch = null;
            // optionale Semikolons zwischen Dann-Zweig und else ueberspringen
            while (peek().type == TokenType.SEMI) next();
            if (peek().type == TokenType.ELSE) {
                next();
                elseBranch = parseStatement();
            }
            return new IfNode(cond, thenBranch, elseBranch, ifTok.line, ifTok.col);
        }
        if (peek().type == TokenType.WHILE) {
            Token whileTok = next();
            eat(TokenType.LPAREN);
            Node cond = parseComparison();
            eat(TokenType.RPAREN);
            Node body = parseStatement();
            // leeren Rumpf erkennen (oft ein Fehler, kann Endlosschleife verursachen)
            if (body instanceof SequenceNode s && s.stmts.isEmpty()) {
                System.err.println("Warning: Empty while-body detected (may cause infinite loop) at " + whileTok);
            }
            return new WhileNode(cond, body, whileTok.line, whileTok.col);
        }
        if (peek().type == TokenType.FOR) {
            Token forTok = next();
            eat(TokenType.LPAREN);
            Node init = null;
            if (peek().type != TokenType.SEMI) init = parseStart();
            eat(TokenType.SEMI);
            Node cond = null;
            if (peek().type != TokenType.SEMI) cond = parseComparison();
            eat(TokenType.SEMI);
            Node post = null;
            if (peek().type != TokenType.RPAREN) post = parseStart();
            eat(TokenType.RPAREN);
            Node body = parseStatement();
            if (body instanceof SequenceNode s && s.stmts.isEmpty()) {
                System.err.println("Warning: Empty for-body detected (may be unintended) at " + forTok);
            }
            return new ForNode(init, cond, post, body, forTok.line, forTok.col);
        }
        if (peek().type == TokenType.FUNCTION) {
            Token funcTok = next();
            if (peek().type != TokenType.IDENT) throw new ParseException("Expected function name", peek());
            Token name = next();
            eat(TokenType.LPAREN);
            List<String> params = new ArrayList<>();
            if (peek().type != TokenType.RPAREN) {
                if (peek().type != TokenType.IDENT) throw new ParseException("Expected parameter name", peek());
                params.add(next().text);
                while (peek().type == TokenType.COMMA) { next(); if (peek().type != TokenType.IDENT) throw new ParseException("Expected parameter name", peek()); params.add(next().text); }
            }
            eat(TokenType.RPAREN);
            // Rumpf muss ein Block sein
            Node body = parseStatement();
            return new FunctionNode(name.text, params, body, funcTok.line, funcTok.col);
        }

        if (peek().type == TokenType.RETURN) {
            Token retTok = next();
            Node expr = null;
            if (peek().type != TokenType.SEMI && peek().type != TokenType.RBRACE && peek().type != TokenType.EOF) {
                expr = parseComparison();
            }
            return new ReturnNode(expr, retTok.line, retTok.col);
        }

        if (peek().type == TokenType.IDENT) {
            // Zuweisung: IDENT '=' expr
            Token second = (pos + 1) < tokens.size() ? tokens.get(pos + 1) : new Token(TokenType.EOF, "", pos + 1);
            if (second.type == TokenType.ASSIGN) {
                Token ident = next();
                next(); // ASSIGN konsumieren
                Node e = parseComparison();
                return new AssignNode(ident.text, e, ident.line, ident.col);
            }
        }
        return parseComparison();
    }

    private Node parseComparison() {
        Node left = parseExpr();
        while (peek().type == TokenType.EQ || peek().type == TokenType.NEQ ||
               peek().type == TokenType.LT || peek().type == TokenType.GT ||
               peek().type == TokenType.LE || peek().type == TokenType.GE) {
            Token op = next();
            if (peek().type == TokenType.EOF) throw new ParseException("Missing right-hand side for operator", op);
            Node right = parseExpr();
            left = new BinOpNode(left, op, right);
        }
        return left;
    }

    private Node parseExpr() {
        Node node = parseTerm();
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            Token op = next();
            if (peek().type == TokenType.EOF) throw new ParseException("Missing right-hand side for operator", op);
            Node right = parseTerm();
            node = new BinOpNode(node, op, right);
        }
        return node;
    }

    private Node parseExprPrime(Node left) {
        Node node = parseTermPrime(left);
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            Token op = next();
            Node right = parseTerm();
            node = new BinOpNode(node, op, right);
        }
        return node;
    }

    private Node parseTerm() {
        Node node = parseFactor();
        while (peek().type == TokenType.STAR || peek().type == TokenType.SLASH) {
            Token op = next();
            if (peek().type == TokenType.EOF) throw new ParseException("Missing right-hand side for operator", op);
            Node right = parseFactor();
            node = new BinOpNode(node, op, right);
        }
        return node;
    }

    private Node parseTermPrime(Node left) {
        Node node = left;
        while (peek().type == TokenType.STAR || peek().type == TokenType.SLASH) {
            Token op = next();
            Node right = parseFactor();
            node = new BinOpNode(node, op, right);
        }
        return node;
    }

    private Node parseFactor() {
        if (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            Token op = next();
            Node f = parseFactor();
            return new UnaryNode(op, f);
        }
        if (peek().type == TokenType.NUMBER) {
            Token t = next();
            return new NumberNode(Double.parseDouble(t.text), t.line, t.col);
        }
        if (peek().type == TokenType.IDENT) {
            Token t = next();
            // Funktionsaufruf: IDENT '(' Argumente ')'
            if (peek().type == TokenType.LPAREN) {
                next();
                List<Node> args = new ArrayList<>();
                if (peek().type != TokenType.RPAREN) {
                    args.add(parseComparison());
                    while (peek().type == TokenType.COMMA) {
                        next();
                        args.add(parseComparison());
                    }
                }
                eat(TokenType.RPAREN);
                return new CallNode(t.text, args, t.line, t.col);
            }
            return new VarNode(t.text, t.line, t.col);
        }
        if (peek().type == TokenType.LPAREN) {
            eat(TokenType.LPAREN);
            Node e = parseComparison();
            eat(TokenType.RPAREN);
            return e;
        }
        throw new ParseException("Unexpected factor: " + peek(), peek());
    }
}
