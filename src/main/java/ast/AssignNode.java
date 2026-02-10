package ast;

import java.util.Map;

/**
 * Zuweisungs-Knoten: `name = expr`.
 * Wert von `expr` wird berechnet und in der Umgebung (`env`) unter `name` gespeichert.
 */
public class AssignNode extends Node {
    public final String name;
    public final Node expr;
    public AssignNode(String name, Node expr, int line, int col) {
        super(line, col);
        this.name = name;
        this.expr = expr;
    }
    @Override
    public double eval(Map<String, Double> env) {
        double v = expr.eval(env);
        env.put(name, v);
        return v;
    }
    @Override
    public String toString() { return "Assign("+name+", "+expr+")"; }
}
