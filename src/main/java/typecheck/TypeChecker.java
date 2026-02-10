package typecheck;

import ast.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lexer.TokenType;

public class TypeChecker {
    private static class FunctionType {
        final List<Type> params;
        Type returnType = Type.UNKNOWN;

        FunctionType(int arity) {
            this.params = new ArrayList<>(Collections.nCopies(arity, Type.UNKNOWN));
        }
    }

    private final Map<String, Type> vars = new HashMap<>();
    private final Map<String, FunctionType> functions = new HashMap<>();
    private FunctionType currentFunction = null;
    private boolean sawReturn = false;

    public void check(Node node, Map<String, Type> initialVars) {
        vars.clear();
        functions.clear();
        currentFunction = null;
        sawReturn = false;
        if (initialVars != null) vars.putAll(initialVars);
        visit(node);
    }

    private Type visit(Node node) {
        if (node instanceof NumberNode) return Type.NUMBER;
        if (node instanceof VarNode v) return visitVar(v);
        if (node instanceof AssignNode a) return visitAssign(a);
        if (node instanceof UnaryNode u) return visitUnary(u);
        if (node instanceof BinOpNode b) return visitBinOp(b);
        if (node instanceof IfNode i) return visitIf(i);
        if (node instanceof WhileNode w) return visitWhile(w);
        if (node instanceof ForNode f) return visitFor(f);
        if (node instanceof FunctionNode fn) return visitFunction(fn);
        if (node instanceof CallNode c) return visitCall(c);
        if (node instanceof ReturnNode r) return visitReturn(r);
        if (node instanceof SequenceNode s) return visitSequence(s);
        throw new TypeError("Unknown node type: " + node.getClass(), node.line, node.col);
    }

    private Type visitVar(VarNode v) {
        Type t = vars.get(v.name);
        if (t == null) throw new TypeError("Undefined variable: " + v.name, v.line, v.col);
        return t;
    }

    private Type visitAssign(AssignNode a) {
        Type exprType = visit(a.expr);
        Type varType = vars.getOrDefault(a.name, Type.UNKNOWN);
        if (varType == Type.UNKNOWN && exprType != Type.UNKNOWN) {
            vars.put(a.name, exprType);
            varType = exprType;
        } else if (exprType == Type.UNKNOWN && varType != Type.UNKNOWN) {
            assignType(a.expr, varType);
        } else if (varType != Type.UNKNOWN && exprType != Type.UNKNOWN && varType != exprType) {
            throw new TypeError("Type mismatch in assignment to " + a.name + " (" + varType + " vs " + exprType + ")", a.line, a.col);
        }
        return varType == Type.UNKNOWN ? exprType : varType;
    }

    private Type visitUnary(UnaryNode u) {
        Type t = visit(u.expr);
        ensureType(u.expr, t, Type.NUMBER);
        return Type.NUMBER;
    }

    private Type visitBinOp(BinOpNode b) {
        Type left = visit(b.left);
        Type right = visit(b.right);
        TokenType t = b.op.type;

        if (t == TokenType.PLUS || t == TokenType.MINUS || t == TokenType.STAR || t == TokenType.SLASH) {
            ensureType(b.left, left, Type.NUMBER);
            ensureType(b.right, right, Type.NUMBER);
            return Type.NUMBER;
        }
        if (t == TokenType.LT || t == TokenType.GT || t == TokenType.LE || t == TokenType.GE || t == TokenType.EQ || t == TokenType.NEQ) {
            ensureType(b.left, left, Type.NUMBER);
            ensureType(b.right, right, Type.NUMBER);
            return Type.BOOL;
        }
        throw new TypeError("Unknown operator: " + t, b.line, b.col);
    }

    private Type visitIf(IfNode i) {
        Type condType = visit(i.cond);
        ensureType(i.cond, condType, Type.BOOL);
        Type thenType = visit(i.thenBranch);
        if (i.elseBranch == null) return Type.NUMBER;
        Type elseType = visit(i.elseBranch);
        return mergeTypes(thenType, elseType, i);
    }

    private Type visitWhile(WhileNode w) {
        Type condType = visit(w.cond);
        ensureType(w.cond, condType, Type.BOOL);
        visit(w.body);
        return Type.NUMBER;
    }

    private Type visitFor(ForNode f) {
        if (f.init != null) visit(f.init);
        if (f.cond != null) {
            Type condType = visit(f.cond);
            ensureType(f.cond, condType, Type.BOOL);
        }
        if (f.post != null) visit(f.post);
        visit(f.body);
        return Type.NUMBER;
    }

    private Type visitFunction(FunctionNode fn) {
        if (functions.containsKey(fn.name)) {
            throw new TypeError("Function already defined: " + fn.name, fn.line, fn.col);
        }
        FunctionType ft = new FunctionType(fn.params.size());
        functions.put(fn.name, ft);

        Map<String, Type> oldVars = new HashMap<>(vars);
        FunctionType oldFn = currentFunction;
        boolean oldSawReturn = sawReturn;

        vars.clear();
        for (String p : fn.params) vars.put(p, Type.UNKNOWN);
        currentFunction = ft;
        sawReturn = false;

        Type bodyType = visit(fn.body);
        if (!sawReturn && ft.returnType == Type.UNKNOWN) ft.returnType = bodyType;

        vars.clear();
        vars.putAll(oldVars);
        currentFunction = oldFn;
        sawReturn = oldSawReturn;

        return Type.NUMBER;
    }

    private Type visitCall(CallNode c) {
        FunctionType ft = functions.get(c.name);
        if (ft == null) throw new TypeError("Undefined function: " + c.name, c.line, c.col);
        if (c.args.size() != ft.params.size()) {
            throw new TypeError("Arity mismatch in call to " + c.name + " (expected " + ft.params.size() + ", got " + c.args.size() + ")", c.line, c.col);
        }
        for (int i = 0; i < c.args.size(); i++) {
            Node arg = c.args.get(i);
            Type argType = visit(arg);
            Type paramType = ft.params.get(i);
            if (paramType == Type.UNKNOWN && argType != Type.UNKNOWN) {
                ft.params.set(i, argType);
            } else if (argType == Type.UNKNOWN && paramType != Type.UNKNOWN) {
                assignType(arg, paramType);
            } else if (paramType != Type.UNKNOWN && argType != Type.UNKNOWN && paramType != argType) {
                throw new TypeError("Type mismatch for parameter " + (i + 1) + " in call to " + c.name + " (" + paramType + " vs " + argType + ")", c.line, c.col);
            }
        }
        return ft.returnType;
    }

    private Type visitReturn(ReturnNode r) {
        Type t = r.expr == null ? Type.NUMBER : visit(r.expr);
        if (currentFunction != null) {
            if (currentFunction.returnType == Type.UNKNOWN) {
                currentFunction.returnType = t;
            } else if (currentFunction.returnType != t && t != Type.UNKNOWN) {
                throw new TypeError("Inconsistent return type (" + currentFunction.returnType + " vs " + t + ")", r.line, r.col);
            }
            sawReturn = true;
        }
        return t;
    }

    private Type visitSequence(SequenceNode s) {
        if (s.stmts.isEmpty()) return Type.NUMBER;
        Type last = Type.NUMBER;
        for (Node n : s.stmts) last = visit(n);
        return last;
    }

    private void ensureType(Node node, Type actual, Type expected) {
        if (actual == expected) return;
        if (actual == Type.UNKNOWN) {
            assignType(node, expected);
            return;
        }
        throw new TypeError("Expected " + expected + " but found " + actual, node.line, node.col);
    }

    private Type mergeTypes(Type a, Type b, Node node) {
        if (a == b) return a;
        if (a == Type.UNKNOWN) return b;
        if (b == Type.UNKNOWN) return a;
        throw new TypeError("Type mismatch (" + a + " vs " + b + ")", node.line, node.col);
    }

    private void assignType(Node node, Type expected) {
        if (node instanceof VarNode v) {
            vars.put(v.name, expected);
        } else if (node instanceof CallNode c) {
            FunctionType ft = functions.get(c.name);
            if (ft != null && ft.returnType == Type.UNKNOWN) ft.returnType = expected;
        }
    }
}
