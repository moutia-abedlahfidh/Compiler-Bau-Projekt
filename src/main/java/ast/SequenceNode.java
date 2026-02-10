package ast;

import java.util.List;
import java.util.Map;

/**
 * Repräsentiert eine Sequenz von Statements/Ausdrücken; wertet sie nacheinander aus
 * und liefert den Wert des letzten Elements zurück.
 */
public class SequenceNode extends Node {
    public final List<Node> stmts;
    public SequenceNode(List<Node> stmts, int line, int col) {
        super(line, col);
        this.stmts = stmts;
    }
    @Override
    public double eval(Map<String, Double> env) {
        double last = 0;
        for (Node n : stmts) last = n.eval(env);
        return last;
    }
    @Override
    public String toString() { return "Sequence"+stmts; }
}
