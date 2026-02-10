package ast;

import java.util.Map;

/**
 * Basis-Klasse für AST-Knoten. Alle konkreten Knoten implementieren `eval`.
 * Das `env`-Argument ist eine Map für Variablenbindungen (Name -> Wert).
 */
public abstract class Node {
    public final int line;
    public final int col;

    protected Node(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public String location() {
        return (line > 0 && col > 0) ? (line + ":" + col) : "?:?";
    }

    public abstract double eval(Map<String, Double> env);
}
