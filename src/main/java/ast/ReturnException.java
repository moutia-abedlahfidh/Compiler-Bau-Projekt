package ast;

/**
 * Internal exception used to implement `return` semantics while evaluating AST.
 */
public class ReturnException extends RuntimeException {
    public final double value;
    public ReturnException(double value) { this.value = value; }
}
