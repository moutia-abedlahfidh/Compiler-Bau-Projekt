package ast;

/**
 * Interne Exception zur Umsetzung der `return`-Semantik bei der AST-Auswertung.
 */
public class ReturnException extends RuntimeException {
    public final double value;
    public ReturnException(double value) { this.value = value; }
}
