package ast;

import java.util.Map;

/**
 * Repräsentiert einen numerischen Literal-Knoten im AST.
 */
public class NumberNode extends Node {
    public final double value;
    public NumberNode(double value) { this.value = value; }
    @Override
    public double eval(Map<String, Double> env) { return value; }
    @Override
    public String toString() { return "Number("+value+")"; }
}
