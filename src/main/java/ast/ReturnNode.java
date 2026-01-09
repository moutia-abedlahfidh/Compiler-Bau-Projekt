package ast;

import java.util.Map;

public class ReturnNode extends Node {
    public final Node expr;
    public ReturnNode(Node expr) { this.expr = expr; }

    @Override
    public double eval(Map<String, Double> env) {
        double v = expr == null ? 0.0 : expr.eval(env);
        throw new ReturnException(v);
    }

    @Override
    public String toString() { return "Return(" + expr + ")"; }
}
