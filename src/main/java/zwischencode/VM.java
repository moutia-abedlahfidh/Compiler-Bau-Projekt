package zwischencode;

import java.util.*;

/**
 * Stack-basierte VM für den von CodeGenerator erzeugten Zwischencode.
 * - führt arithmetische Ops, Variablen, Sprünge, Vergleiche und Funktionen aus
 * - Funktionen haben ein eigenes lokales Environment (wie beim AST.eval)
 */
public class VM {

    private static class Frame {
        final Map<String, Double> locals;
        final int returnIp;
        final Deque<Double> stack;

        Frame(Map<String, Double> locals, int returnIp, Deque<Double> stack) {
            this.locals = locals;
            this.returnIp = returnIp;
            this.stack = stack;
        }
    }

    private static class FuncInfo {
        final String name;
        final List<String> params;
        final int entryIp;  // erste Instruktion NACH FUNCTION_DEF
        final int endIp;    // Index der FUNCTION_END Instruktion

        FuncInfo(String name, List<String> params, int entryIp, int endIp) {
            this.name = name;
            this.params = params;
            this.entryIp = entryIp;
            this.endIp = endIp;
        }
    }

    // Speichert Zwischenergebnisse von Berechnungen 
    private Deque<Double> stack = new ArrayDeque<>();
    // Speichert Springadresse (returnIP) und Lokale Variablen des Aufrufers
    private final Deque<Frame> callStack = new ArrayDeque<>();
    // Enthält alle bekannten Funktionen
    private final Map<String, FuncInfo> functions = new HashMap<>();
    // Speichert lokale Variablen der aktuell laufenden Funktion
    private Map<String, Double> locals = new HashMap<>();

    public double run(List<Instruction> code, Map<String, Double> initialEnv) {
        stack.clear();
        callStack.clear();
        functions.clear();

        // Umgebung der obersten Ebene
        locals = new HashMap<>(initialEnv);

        // 1) Funktionen indexieren
        indexFunctions(code);

        // 2) Code ausführen
        int ip = 0;
        while (ip >= 0 && ip < code.size()) {
            Instruction ins = code.get(ip);

            switch (ins.type) {

                case LOAD_CONST -> {
                    stack.push((Double) ins.operand);
                    ip++;
                }

                case LOAD_VAR -> {
                    String name = (String) ins.operand;
                    Double v = locals.get(name);
                    stack.push(v != null ? v : 0.0);
                    ip++;
                }

                case STORE_VAR -> {
                    String name = (String) ins.operand;
                    locals.put(name, pop());
                    ip++;
                }

                case ADD -> { stack.push(pop() + pop()); ip++; }
                case SUB -> { double r = pop(); double l = pop(); stack.push(l - r); ip++; }
                case MUL -> { stack.push(pop() * pop()); ip++; }
                case DIV -> {
                    double r = pop();
                    double l = pop();
                    if (r == 0.0) throw new ArithmeticException("Division by zero");
                    stack.push(l / r);
                    ip++;
                }

                case NEG -> { stack.push(-pop()); ip++; }

                // Vergleiche: true => 1.0, false => 0.0
                case LT -> { double r = pop(); double l = pop(); stack.push(l < r ? 1.0 : 0.0); ip++; }
                case GT -> { double r = pop(); double l = pop(); stack.push(l > r ? 1.0 : 0.0); ip++; }
                case LE -> { double r = pop(); double l = pop(); stack.push(l <= r ? 1.0 : 0.0); ip++; }
                case GE -> { double r = pop(); double l = pop(); stack.push(l >= r ? 1.0 : 0.0); ip++; }
                case EQ -> { double r = pop(); double l = pop(); stack.push(Double.compare(l, r) == 0 ? 1.0 : 0.0); ip++; }
                case NEQ -> { double r = pop(); double l = pop(); stack.push(Double.compare(l, r) != 0 ? 1.0 : 0.0); ip++; }

                case JUMP_IF_FALSE -> {
                    int target = (Integer) ins.operand;
                    double cond = pop();
                    ip = (cond == 0.0) ? target : ip + 1;
                }

                case JUMP -> {
                    ip = (Integer) ins.operand;
                }

                // Funktionsdefinitionen werden im Main-Flow NICHT ausgeführt:
                // Wir springen über den Funktionsblock.
                case FUNCTION_DEF -> {
                    String sig = (String) ins.operand; // z.B. "add:x,y"
                    FuncInfo fi = functions.get(sig);
                    if (fi == null) throw new RuntimeException("Unknown function signature: " + sig);
                    ip = fi.endIp + 1;
                }

                // Wenn FUNCTION_END während Funktionsausführung erreicht wird:
                // implizites "return" mit letztem Ausdruck (falls vorhanden)
                case FUNCTION_END -> {
                    double rv = pop();
                    ip = doReturn(rv);
                }

                case CALL -> {
                    String callSig = (String) ins.operand; // weiterhin "name:argc"
                    String fname = parseCallName(callSig);
                    int argc = parseCallArity(callSig);

                    // passende Funktionsdefinition anhand des Namens suchen
                    FuncInfo fi = lookupByNameAndArity(fname, argc);
                    if (fi == null) throw new RuntimeException("Undefined function: " + fname + " with " + argc + " args");

                    // Argumente vom Stack holen (umdrehen, weil Stack)
                    List<Double> args = new ArrayList<>(argc);
                    for (int i = 0; i < argc; i++) args.add(pop());
                    Collections.reverse(args);

                    // new locals wie in AST: frisches Environment nur für Parameter
                    Map<String, Double> newLocals = new HashMap<>();
                    for (int i = 0; i < fi.params.size(); i++) {
                        String p = fi.params.get(i);
                        Double arg = i < args.size() ? args.get(i) : null;
                        double v = arg != null ? arg : 0.0;
                        newLocals.put(p, v);
                    }

                    // Frame pushen + in Funktion springen
                    callStack.push(new Frame(locals, ip + 1, stack));
                    locals = newLocals;
                    stack = new ArrayDeque<>();
                    ip = fi.entryIp;
                }

                case RET -> {
                    // Rueckgabewert ist entweder Stack-Top oder 0.0 (wenn return; ohne expr)
                    double rv = pop();
                    ip = doReturn(rv);
                }

                case PRINT -> {
                    System.out.println(pop());
                    ip++;
                }

                case POP -> {
                    if (!stack.isEmpty()) stack.pop();
                    ip++;
                }

                default -> throw new RuntimeException("Unhandled instruction: " + ins.type);
            }
        }

        return pop();
    }

    private double pop() {
        Double v = stack.poll();
        return v != null ? v : 0.0;
    }

    private int doReturn(double rv) {
        if (callStack.isEmpty()) {
            // return auf oberster Ebene: Programmende
            stack.push(rv);
            return Integer.MAX_VALUE;
        }
        Frame prev = callStack.pop();
        // Das ist die Funktion, die diese Funktion aufgerufen hat
        locals = prev.locals; // Die lokalen Variablen der aufrufenden Funktion werden zurückgesetzt 
        // Die Variablen der aktuellen Funktion verschwinden
        stack = prev.stack;
        stack.push(rv);
        return prev.returnIp;
    }

    // Alle Funktionsdefinitionen im Zwischencode finden und registrieren
    private void indexFunctions(List<Instruction> code) {
        for (int i = 0; i < code.size(); i++) {
            Instruction ins = code.get(i);
            if (ins.type == InstructionType.FUNCTION_DEF) {
                String sig = (String) ins.operand; // "name:param1,param2" (nach deiner Änderung)
                ParsedDef def = parseFunctionDef(sig);

                int endIp = findFunctionEnd(code, i + 1);
                FuncInfo fi = new FuncInfo(def.name, def.params, i + 1, endIp);

                functions.put(sig, fi);
            }
        }
    }

    // Ende einer Funktion finden
    private int findFunctionEnd(List<Instruction> code, int start) {
        for (int i = start; i < code.size(); i++) {
            if (code.get(i).type == InstructionType.FUNCTION_END) return i;
        }
        throw new RuntimeException("FUNCTION_END not found");
    }

    // Ergebnis eines geparsten Funktionskopfes speichern 
    private static class ParsedDef {
        final String name;
        final List<String> params;
        ParsedDef(String name, List<String> params) { this.name = name; this.params = params; }
    }


    // Funktionssignatur aus FUNCTION_DEF zerlegen
    /* Die VM muss wissen: Wie die Funktion heißt , Wie viele Parameter sie hat und Wie die Parameter heißen */
    private ParsedDef parseFunctionDef(String sig) {
        int idx = sig.indexOf(':');
        if (idx < 0) throw new RuntimeException("Bad FUNCTION_DEF signature: " + sig);

        String name = sig.substring(0, idx).trim();
        String rest = sig.substring(idx + 1).trim();

        // rest ist z.B. "x,y" oder "" (keine params)
        List<String> params = new ArrayList<>();
        if (!rest.isEmpty()) {
            for (String p : rest.split(",")) {
                String t = p.trim();
                if (!t.isEmpty()) params.add(t);
            }
        }
        return new ParsedDef(name, params);
    }

    // Funktionsname aus einem CALL extrahieren
    private String parseCallName(String callSig) {
        int idx = callSig.lastIndexOf(':');
        if (idx < 0) throw new RuntimeException("Bad CALL signature: " + callSig);
        return callSig.substring(0, idx).trim();
    }

    // Anzahl der Argumente beim Funktionsaufruf ermitteln
    /* Vielleicht gibt es Funktionen mit gleichen Namen, aber sie enthalten nicht die gleiche Anzahl an Parametern */
    private int parseCallArity(String callSig) {
        int idx = callSig.lastIndexOf(':');
        if (idx < 0) throw new RuntimeException("Bad CALL signature: " + callSig);
        return Integer.parseInt(callSig.substring(idx + 1).trim());
    }

    // Passende Funktion für einen CALL finden
    /* Die VM muss genau wissen, wohin sie bei CALL springen soll */
    private FuncInfo lookupByNameAndArity(String name, int argc) {
        // Wir haben FUNCTION_DEF Signaturen als "name:param1,param2"
        // => wir suchen per name und param-count
        for (FuncInfo fi : functions.values()) {
            if (fi.name.equals(name) && fi.params.size() == argc) return fi;
        }
        return null;
    }
}

