package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallNode extends Node {
    public final String name;
    public final List<Node> args;

    public CallNode(String name, List<Node> args, int line, int col) {
        super(line, col);
        this.name = name;
        this.args = args;
    }

    @Override
    public double eval(Map<String, Double> env) {
        FunctionNode f = FunctionRegistry.lookup(name);
        if (f == null) throw new RuntimeException("Undefined function: " + name);
        if (args.size() != f.params.size()) {
            throw new RuntimeException(
                "Arity mismatch in call to " + name + " (expected " + f.params.size() + ", got " + args.size() + ")"
            );
        }
        // Argumente auswerten
        List<Double> avals = new ArrayList<>();
        for (Node a : args) avals.add(a.eval(env));
        // Parameter in frisches Environment binden
        Map<String, Double> local = new HashMap<>();
        for (int i = 0; i < f.params.size(); i++) {
            String p = f.params.get(i);
            Double arg = i < avals.size() ? avals.get(i) : null;
            double v = arg != null ? arg : 0.0;
            local.put(p, v);
        }
        try {
            return f.body.eval(local);
        } catch (ReturnException r) {
            return r.value;
        }
    }

    @Override
    public String toString() { return "Call(" + name + ", " + args + ")"; }
}
