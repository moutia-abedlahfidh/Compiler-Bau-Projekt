package ast;

import java.util.Map;

public class WhileNode extends Node {
    public final Node cond;
    public final Node body;

    public WhileNode(Node cond, Node body, int line, int col) {
        super(line, col);
        this.cond = cond;
        this.body = body;
    }

    @Override
    public double eval(Map<String, Double> env) {
        double last = 0;
        // bei leerem Block warnen und Schleife ueberspringen, um Endlosschleifen zu vermeiden
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
