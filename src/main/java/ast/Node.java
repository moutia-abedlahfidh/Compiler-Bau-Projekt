package ast;

import java.util.Map;

/**
 * Basis-Klasse für AST-Knoten. Alle konkreten Knoten implementieren `eval`.
 * Das `env`-Argument ist eine Map für Variablenbindungen (Name -> Wert).
 */
public abstract class Node {
    public abstract double eval(Map<String, Double> env);
}
