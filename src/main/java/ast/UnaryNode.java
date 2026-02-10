package ast;

import java.util.Map;
import lexer.Token;
import lexer.TokenType;

/**
 * Unärer Operator-Knoten (z. B. +x oder -x).
 * Enthält das Operator-Token und den zugehörigen Ausdrucks-Knoten.
 */
public class UnaryNode extends Node {
    private final Token op;
    public final Node expr;
    public UnaryNode(Token op, Node expr) {
        super(op.line, op.col);
        this.op = op;
        this.expr = expr;
    }

    public boolean isMinus() {
        return op.type == TokenType.MINUS;
    }

    @Override
    public double eval(Map<String, Double> env) {
        double v = expr.eval(env);
        switch (op.type) {
            case PLUS: return +v;
            case MINUS: return -v;
            default: throw new RuntimeException("Unknown unary op: " + op.type);
        }
    }

    @Override
    public String toString() { return "Unary(" + op.text + " " + expr + ")"; }
}
