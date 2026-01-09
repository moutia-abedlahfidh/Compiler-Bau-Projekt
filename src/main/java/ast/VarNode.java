package ast;

import java.util.Map;

/**
 * AST-Knoten für eine Variable (Namen).
 * Beim Auswerten wird der Wert aus der übergebenen Umgebung (`env`) gelesen.
 */
public class VarNode extends Node {
    public final String name;
    public VarNode(String name) { this.name = name; }
    @Override
    public double eval(Map<String, Double> env) {
        Double v = env.get(name);
        if (v == null) throw new RuntimeException("Undefined variable: " + name);
        return v;
    }
    @Override
    public String toString() { return "Var("+name+")"; }
}
