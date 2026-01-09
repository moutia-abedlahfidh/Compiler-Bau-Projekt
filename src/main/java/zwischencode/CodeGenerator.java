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
            emit(InstructionType.NEG);
            return;
        }

        if (node instanceof IfNode i) {
            // gen condition
            gen(i.cond);
            // emit jump-if-false with placeholder
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIndex = instructions.size() - 1;

            // then branch
            gen(i.thenBranch);

            if (i.elseBranch != null) {
                // emit unconditional jump to end
                emit(InstructionType.JUMP, null);
                int jEndIndex = instructions.size() - 1;
                // patch jump-if-false to point to start of else
                instructions.get(jifIndex).operand = instructions.size();
                // else branch
                gen(i.elseBranch);
                // patch end jump to point after else
                instructions.get(jEndIndex).operand = instructions.size();
            } else {
                // no else: patch jump-if-false to point after then-branch
                instructions.get(jifIndex).operand = instructions.size();
            }
            return;
        }

        if (node instanceof WhileNode w) {
            int startIndex = instructions.size();
            gen(w.cond);
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIdx = instructions.size() - 1;
            gen(w.body);
            // jump back to condition
            emit(InstructionType.JUMP, startIndex);
            // patch jif to point after loop body
            instructions.get(jifIdx).operand = instructions.size();
            return;
        }

        if (node instanceof ForNode f) {
            // init
            if (f.init != null) gen(f.init);
            int startIndex = instructions.size();
            // cond (if null => true)
            if (f.cond != null) gen(f.cond);
            else emit(InstructionType.LOAD_CONST, 1.0);
            emit(InstructionType.JUMP_IF_FALSE, null);
            int jifIdx = instructions.size() - 1;
            // body
            gen(f.body);
            // post
            if (f.post != null) gen(f.post);
            // jump back to startIndex
            emit(InstructionType.JUMP, startIndex);
            // patch jif
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
            return;
        }

        if (node instanceof FunctionNode fn) {
        // FUNCTION_DEF speichert Namen + Parameternamen (nicht nur arity),
        // damit die VM Argumente korrekt binden kann.
            emit(InstructionType.FUNCTION_DEF, fn.name + ":" + String.join(",", fn.params));
        // Funktionskörper emittieren (VM wird Definitionen im Main-Flow überspringen)
            gen(fn.body);
            emit(InstructionType.FUNCTION_END, fn.name);
            return;
}


        if (node instanceof CallNode call) {
            // evaluate arguments
            for (Node a : call.args) gen(a);
            // emit call with name and arg count
            emit(InstructionType.CALL, call.name + ":" + call.args.size());
            return;
        }

        if (node instanceof ReturnNode ret) {
            if (ret.expr != null) gen(ret.expr);
            emit(InstructionType.RET, null);
            return;
        }

        if (node instanceof SequenceNode s) {
            for (Node stmt : s.stmts) {
                gen(stmt);
            }
            return;
        }

        throw new RuntimeException("Unknown node type: " + node.getClass());
    }
}
