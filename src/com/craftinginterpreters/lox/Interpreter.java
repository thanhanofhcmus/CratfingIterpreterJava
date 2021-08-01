package com.craftinginterpreters.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void> {
    private Token throwToken;
    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            ErrorReporter.error(error);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    @Override
    public Void visitExprStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expr);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVariableStmt(Stmt.Variable stmt) {
        Object value = evaluate(stmt.initializer);
        environment.define(stmt.name, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.stmts, new Environment(this.environment));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.expr);
        setThrowToken(expr.operator);

        return switch (expr.operator.type) {
            case MINUS -> -number(right);
            case BANG  -> !isTruthy(right);
            default -> throw error("Unknown Unary Operator");
        };
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        setThrowToken(expr.operator);

        return switch (expr.operator.type) {
            case PLUS  -> evaluatePlus(left, right);
            case STAR  -> evaluateMultiply(left, right);
            case SLASH -> evaluateDivide(left, right);
            case MINUS -> number(left) - number(right);
            case COMMA -> right;
            case GREATER       -> number(left) > number(right);
            case LESS          -> number(left) < number(right);
            case GREATER_EQUAL -> number(left) >= number(right);
            case LESS_EQUAL    -> number(left) <= number(right);
            case BANG_EQUAL    -> !isEqual(left, right);
            case EQUAL_EQUAL   -> isEqual(left, right);
            default -> throw error("Unknown Binary Operator");
        };
    }

    public Object visitTernaryExpr(Expr.Ternary expr) {
        boolean condition = isTruthy(evaluate(expr.condition));
        Object first = evaluate(expr.first);
        Object second = evaluate(expr.second);
        setThrowToken(null);

        return condition ? first : second;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expr);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        if   (null == expr) { return null; }
        else                { return expr.accept(this); }
    }

    private boolean isTruthy(Object obj) {
        if      (null == obj)            { return false; }
        else if (obj instanceof Boolean) { return (boolean)obj; }
        else if (obj instanceof Double)  { return ((double)obj) == 0.0; }
        else                             { return true; }
    }

    private boolean isEqual(Object left, Object right) {
        if      (null == left && null == right) { return true; }
        else if (null == left)                  { return false; }
        else                                    { return left.equals(right); }
    }

    private Object evaluatePlus(Object left, Object right) {
        if      (left instanceof String)  { return left + stringify(right); }
        else if (right instanceof Double) { return (double)left + (double)right; }
        else { throw error("Cannot do plus on lhs number and rhs string"); }
    }

    private Object evaluateMultiply(Object left, Object right) {
        if (right instanceof Double) {
            if (left instanceof Double) {
                return (double)left * (double)right;
            } else {
                String text = (String)left;
                int rep = (int)(double)right;
                return text.repeat(rep);
            }
        } else {
            throw error("Rhs of multiply must be a number");
        }
    }

    private Object evaluateDivide(Object left, Object right) {
        double a = number(left);
        double b = number(right);

        if (b == 0) { throw error("Cannot divide by zero"); }
        return a / b;
    }

    private double number(Object obj) {
        if   (obj instanceof Double) { return (double)obj; }
        else                         { throw error("Operand must be a number"); }
    }

    private String stringify(Object obj) {
        if (obj == null) return "nil";
        else if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return obj.toString();
    }

    private void executeBlock(List<Stmt> stmts, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void setThrowToken(Token throwToken) {
        this.throwToken = throwToken;
    }

    private RuntimeError error(String message) {
        return new RuntimeError(throwToken, "Interpreter", message);
    }
}
