package ast;

import lexer.Token;

import java.util.Map;

/**
 * Unärer Operator-Knoten (z. B. +x oder -x).
 * Enthält das Operator-Token und den zugehörigen Ausdrucks-Knoten.
 */
public class UnaryNode extends Node {
    private final Token op;
    public final Node expr;
    public UnaryNode(Token op, Node expr) { this.op = op; this.expr = expr; }

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
