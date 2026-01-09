package ast;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple global registry for named functions. Used by CallNode/FunctionNode.
 */
public class FunctionRegistry {
    private static final Map<String, FunctionNode> functions = new HashMap<>();

    public static void register(String name, FunctionNode f) { functions.put(name, f); }
    public static FunctionNode lookup(String name) { return functions.get(name); }
}
