package ast;

import java.util.Map;

public class WhileNode extends Node {
    public final Node cond;
    public final Node body;

    public WhileNode(Node cond, Node body) { this.cond = cond; this.body = body; }

    @Override
    public double eval(Map<String, Double> env) {
        double last = 0;
        // if body is an empty block, avoid spinning forever — print runtime warning and skip loop
        if (body instanceof SequenceNode s && s.stmts.isEmpty()) {
            System.err.println("Runtime warning: empty while-body detected — skipping loop to avoid infinite loop");
            return last;
        }
        while (cond.eval(env) != 0.0) {
            last = body.eval(env);
        }
        return last;
    }

    @Override
    public String toString() { return "While(" + cond + ", " + body + ")"; }
}
