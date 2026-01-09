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
import parser.Parser;
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
        /*examples.add("x = 1 + 2 * (3 - 4)");
        examples.add("a + b * 5");
        examples.add("-3 + 4");
        examples.add("(1 + 2) * 3");
        examples.add("x = 10; y = x * 2; y + 5");
        examples.add("x = 7; x = x + 3; x * 2");
        examples.add("42/ / 6 + 1.5");*/
        try (BufferedReader reader = new BufferedReader(new FileReader("C:/Users/MSI/Downloads/CB_02.12.25/SE1/src/main/java/input.txt"))) {
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
            // silently ignore comment lines that start with '#' or '//' (no output)
            if (trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
            System.out.println("Input: " + ex);
            try {
                Parser p = new Parser(ex);
                Node ast = p.parse();

                if (ast instanceof SequenceNode s && s.stmts.isEmpty()) {
                    System.out.println("AST: Sequence[]");
                    System.out.println("No statements to execute (input was empty or only comments).");
                } else {
                    System.out.println("AST: " + ast);

                    // Always generate and show Zwischencode for inspection
                    CodeGenerator generator = new CodeGenerator();
                    List<Instruction> intermediate = generator.generate(ast);
                    System.out.println("Zwischencode:");
                    for (Instruction i : intermediate) {
                        System.out.println("  " + i);
                    }

                    // Evaluate AST. A ReturnException may be thrown when a top-level `return` appears;
                    // catch it and print its value instead of an error message.
                    try {
                        double result = ast.eval(env);
                        System.out.println("Result: " + result);
                    } catch (ReturnException re) {
                        System.out.println("Result: " + re.value);
                    }
                    try {
                        VM vm = new VM();
                        double vmExecResult = vm.run(intermediate, env);
                        System.out.println("VM Result: " + vmExecResult);

                    } catch (Exception vmError) {
                        System.out.println("VM Error: " + vmError.getMessage());
                    }

                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }
}
