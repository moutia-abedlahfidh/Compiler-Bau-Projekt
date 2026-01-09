package ast;

import java.util.Map;

/**
 * If-Then-Else Knoten. Die elseBranch kann null sein.
 */
public class IfNode extends Node {
    public final Node cond;
    public final Node thenBranch;
    public final Node elseBranch; // optional

    public IfNode(Node cond, Node thenBranch, Node elseBranch) {
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public double eval(Map<String, Double> env) {
        double c = cond.eval(env);
        if (c != 0.0) {
            return thenBranch.eval(env);
        } else if (elseBranch != null) {
            return elseBranch.eval(env);
        }
        return 0.0;
    }

    @Override
    public String toString() { return "If(" + cond + ", then=" + thenBranch + (elseBranch != null ? ", else=" + elseBranch : "") + ")"; }
}
