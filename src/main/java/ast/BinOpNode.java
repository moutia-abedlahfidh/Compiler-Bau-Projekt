package ast;

import java.util.Map;
import lexer.Token;
import lexer.TokenType;

/**
 * Binärer Operator-Knoten (z. B. +, -, *, /, sowie Vergleichsoperatoren).
 * Beim Auswerten werden beide Operanden ausgewertet und die Operation
 * entsprechend dem TokenType ausgeführt.
 */
public class BinOpNode extends Node {
    public final Node left;
    public final Token op;
    public final Node right;
    public BinOpNode(Node left, Token op, Node right) {
        super(op.line, op.col);
        this.left = left;
        this.op = op;
        this.right = right;
    }
    @Override
    public double eval(Map<String, Double> env) {
        double l = left.eval(env);
        double r = right.eval(env);
        TokenType t = op.type;
        if (t == TokenType.PLUS) return l + r;
        if (t == TokenType.MINUS) return l - r;
        if (t == TokenType.STAR) return l * r;
        if (t == TokenType.SLASH) {
            if (r == 0.0) throw new ArithmeticException("Division by zero");
            return l / r;
        }
        // Vergleiche: 1.0 fuer true, 0.0 fuer false
        if (t == TokenType.LT) return l < r ? 1.0 : 0.0;
        if (t == TokenType.GT) return l > r ? 1.0 : 0.0;
        if (t == TokenType.LE) return l <= r ? 1.0 : 0.0;
        if (t == TokenType.GE) return l >= r ? 1.0 : 0.0;
        if (t == TokenType.EQ) return l == r ? 1.0 : 0.0;
        if (t == TokenType.NEQ) return l != r ? 1.0 : 0.0;
        throw new RuntimeException("Unknown binary op: " + t);
    }
    @Override
    public String toString() { return "BinOp("+left+" "+op.text+" "+right+")"; }
}
