package ast;

import java.util.List;
import java.util.Map;

/**
 * Represents a function declaration: `function name(params...) body`.
 * Evaluating a FunctionNode registers it in the global registry.
 */
public class FunctionNode extends Node {
    public final String name;
    public final List<String> params;
    public final Node body;

    public FunctionNode(String name, List<String> params, Node body) {
        this.name = name; this.params = params; this.body = body;
    }

    @Override
    public double eval(Map<String, Double> env) {
        FunctionRegistry.register(name, this);
        return 0.0;
    }

    @Override
    public String toString() { return "Function(" + name + ", params=" + params + ", body=" + body + ")"; }
}
