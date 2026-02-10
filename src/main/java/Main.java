import ast.Node;
import ast.ReturnException;
import ast.SequenceNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import parser.ParseException;
import parser.Parser;
import typecheck.Type;
import typecheck.TypeChecker;
import typecheck.TypeError;
import zwischencode.CodeGenerator;
import zwischencode.Instruction;
import zwischencode.VM;


/**
 * Kleine Kommandozeilen-Hauptklasse mit zwei Modi:
 * - Führt vordefinierte Tests non-interaktiv in main aus und zeigt AST + Ergebnis.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        
        List<String> examples = new ArrayList<>();
        String inputPath = args.length > 0 ? args[0] : "src/main/java/input.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    examples.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        Map<String, Double> env = new HashMap<>();
        env.put("a", 2.0);
        env.put("b", 3.0);

        for (String ex : examples) {
            String trimmed = ex.trim();
            // Kommentarzeilen mit '#' oder '//' still ignorieren (keine Ausgabe)
            if (trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
            System.out.println("Input: " + ex);
            try {
                Parser p = new Parser(ex);
                Node ast = p.parse();

                if (ast instanceof SequenceNode s && s.stmts.isEmpty()) {
                    System.out.println("AST: Sequence[]");
                    System.out.println("AST Info: No statements to execute (input was empty or only comments).");
                } else {
                    System.out.println("AST: " + ast);

                    TypeChecker checker = new TypeChecker();
                    Map<String, Type> initialTypes = new HashMap<>();
                    for (String k : env.keySet()) initialTypes.put(k, Type.NUMBER);
                    try {
                        checker.check(ast, initialTypes);
                    } catch (TypeError te) {
                        System.out.println("Type Error: " + te.getMessage());
                        System.out.println();
                        continue;
                    }

                    // Zwischencode immer erzeugen und zur Kontrolle anzeigen
                    CodeGenerator generator = new CodeGenerator();
                    List<Instruction> intermediate = generator.generate(ast);
                    System.out.println("Zwischencode:");
                    for (Instruction i : intermediate) {
                        System.out.println("  " + i);
                    }

                    // AST auswerten. Ein ReturnException kann bei `return` auf oberster Ebene auftreten;
                    // dann den Wert ausgeben statt einer Fehlermeldung.
                    try {
                        double result = ast.eval(env);
                        System.out.println("AST Result: " + result);
                    } catch (ReturnException re) {
                        System.out.println("AST Result: " + re.value);
                    }
                    try {
                        VM vm = new VM();
                        double vmExecResult = vm.run(intermediate, env);
                        System.out.println("VM Result: " + vmExecResult);

                    } catch (Exception vmError) {
                        System.out.println("VM Error: " + vmError.getMessage());
                    }

                }
            } catch (ParseException pe) {
                System.out.println("Parse Error: " + pe.getMessage());
            } catch (Exception e) {
                System.out.println("Runtime Error: " + e.getMessage());
            }
            System.out.println();
        }
    }
}
