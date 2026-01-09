package ast;

import java.util.Map;

/**
 * For-Loop: for (init; cond; post) body
 * Alle Teile können null sein (besonders nützlich: cond==null bedeutet immer true).
 */
public class ForNode extends Node {
    public final Node init;
    public final Node cond;
    public final Node post;
    public final Node body;

    public ForNode(Node init, Node cond, Node post, Node body) {
        this.init = init; this.cond = cond; this.post = post; this.body = body;
    }

    @Override
    public double eval(Map<String, Double> env) {
        double last = 0;
        if (init != null) init.eval(env);
        // if body is empty, warn and skip executing the loop to avoid accidental infinite loops
        if (body instanceof SequenceNode s && s.stmts.isEmpty()) {
            System.err.println("Runtime warning: empty for-body detected — skipping loop to avoid infinite loop");
            return last;
        }
        while (cond == null || cond.eval(env) != 0.0) {
            last = body.eval(env);
            if (post != null) post.eval(env);
        }
        return last;
    }

    @Override
    public String toString() { return "For(init=" + init + ", cond=" + cond + ", post=" + post + ", body=" + body + ")"; }
}
