package typecheck;

public class TypeError extends RuntimeException {
    public final int line;
    public final int col;

    public TypeError(String message, int line, int col) {
        super(message + " at " + ((line > 0 && col > 0) ? (line + ":" + col) : "?:?"));
        this.line = line;
        this.col = col;
    }
}
