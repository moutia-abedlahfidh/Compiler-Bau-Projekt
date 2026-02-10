package zwischencode;

import ast.*;
import java.util.*;

public class CodeGenerator {

    private final List<Instruction> instructions = new ArrayList<>();

    public List<Instruction> generate(Node node) {
        instructions.clear();
        gen(node);
        return instructions;
    }

    private void emit(InstructionType type) {
        instructions.add(new Instruction(type, null));
    }

    private void emit(InstructionType type, Object operand) {
        instructions.add(new Instruction(type, operand));
    }

    private void gen(Node node) {
        if (node == null) throw new RuntimeException("Unknown node type: null");

        if (node instanceof NumberNode n) {
            emit(InstructionType.LOAD_CONST, n.value);
            return;
        }

        if (node instanceof VarNode v) {
            emit(InstructionType.LOAD_VAR, v.name);
            return;
        }

        if (node instanceof UnaryNode u) {
            gen(u.expr);
            if (u.isMinus()) emit(InstructionType.NEG);
            return;
        }

        if (node instanceof IfNode i) {
            // Bedingung erzeugen
            gen(i.cond);
            // JUMP_IF_FALSE mit Platzhalter emittieren
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIndex = instructions.size() - 1;

            // Dann-Zweig
            gen(i.thenBranch);

            if (i.elseBranch != null) {
                // bedingungslosen Sprung zum Ende emittieren
                emit(InstructionType.JUMP, null);
                int jEndIndex = instructions.size() - 1;
                // JUMP_IF_FALSE auf Beginn des else-Zweigs patchen
                instructions.get(jifIndex).operand = instructions.size();
                // else-Zweig
                gen(i.elseBranch);
                // Endsprung auf Position nach else-Zweig patchen
                instructions.get(jEndIndex).operand = instructions.size();
            } else {
                // kein else: JUMP_IF_FALSE auf Position nach Dann-Zweig patchen
                instructions.get(jifIndex).operand = instructions.size();
            }
            return;
        }

        if (node instanceof WhileNode w) {
            if (isEmptySequence(w.body)) {
                System.err.println("CodeGen warning: empty while-body detected — skipping loop to avoid infinite loop");
                return;
            }
            int startIndex = instructions.size();
            gen(w.cond);
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIdx = instructions.size() - 1;
            gen(w.body);
            // Rumpf-Wert pro Iteration verwerfen (Statement-Kontext)
            emit(InstructionType.POP);
            // zur Bedingung zurueckspringen
            emit(InstructionType.JUMP, startIndex);
            // JIF auf Position nach dem Schleifenrumpf patchen
            instructions.get(jifIdx).operand = instructions.size();
            return;
        }

        if (node instanceof ForNode f) {
            // Initialisierung
            if (f.init != null) {
                gen(f.init);
                emit(InstructionType.POP);
            }
            if (isEmptySequence(f.body)) {
                System.err.println("CodeGen warning: empty for-body detected — skipping loop to avoid infinite loop");
                return;
            }
            int startIndex = instructions.size();
            // Bedingung (null => true)
            if (f.cond != null) gen(f.cond);
            else emit(InstructionType.LOAD_CONST, 1.0);
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIdx = instructions.size() - 1;
            // Rumpf
            gen(f.body);
            // Rumpf-Wert pro Iteration verwerfen (Statement-Kontext)
            emit(InstructionType.POP);
            // Post-Ausdruck
            if (f.post != null) {
                gen(f.post);
                emit(InstructionType.POP);
            }
            // zum Startindex zurueckspringen
            emit(InstructionType.JUMP, startIndex);
            // JIF patchen
            instructions.get(jifIdx).operand = instructions.size();
            return;
        }

        if (node instanceof BinOpNode b) {
            gen(b.left);
            gen(b.right);

            switch (b.op.type) {
                case PLUS -> emit(InstructionType.ADD);
                case MINUS -> emit(InstructionType.SUB);
                case  STAR -> emit(InstructionType.MUL);
                case SLASH -> emit(InstructionType.DIV);
                case LT -> emit(InstructionType.LT);
                case GT -> emit(InstructionType.GT);
                case LE -> emit(InstructionType.LE);
                case GE -> emit(InstructionType.GE);
                case EQ -> emit(InstructionType.EQ);
                case NEQ -> emit(InstructionType.NEQ);
                default -> throw new RuntimeException("Unknown operator: " + b.op.type);
            }
            return;
        }

        if (node instanceof AssignNode a) {
            gen(a.expr);
            emit(InstructionType.STORE_VAR, a.name);
            // Zuweisung liefert den zugewiesenen Wert (AST-Semantik)
            emit(InstructionType.LOAD_VAR, a.name);
            return;
        }

        if (node instanceof FunctionNode fn) {
        // FUNCTION_DEF speichert Namen + Parameternamen (nicht nur Parameteranzahl),
        // damit die VM Argumente korrekt binden kann.
            emit(InstructionType.FUNCTION_DEF, fn.name + ":" + String.join(",", fn.params));
        // Funktionskörper emittieren (VM wird Definitionen im Main-Flow überspringen)
            gen(fn.body);
            emit(InstructionType.FUNCTION_END, fn.name);
            return;
}


        if (node instanceof CallNode call) {
            // Argumente auswerten
            for (Node a : call.args) gen(a);
            // CALL mit Name und Argumentanzahl emittieren
            emit(InstructionType.CALL, call.name + ":" + call.args.size());
            return;
        }

        if (node instanceof ReturnNode ret) {
            if (ret.expr != null) gen(ret.expr);
            emit(InstructionType.RET, null);
            return;
        }

        if (node instanceof SequenceNode s) {
            for (int i = 0; i < s.stmts.size(); i++) {
                Node stmt = s.stmts.get(i);
                gen(stmt);
                if (i < s.stmts.size() - 1) emit(InstructionType.POP);
            }
            return;
        }

        throw new RuntimeException("Unknown node type: " + node.getClass());
    }

    private boolean isEmptySequence(Node node) {
        return (node instanceof SequenceNode s) && s.stmts.isEmpty();
    }
}
