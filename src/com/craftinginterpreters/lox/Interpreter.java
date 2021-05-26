package com.craftinginterpreters.lox;

public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void> {
    private Token throwToken;

    public void interpret(Expr expr) {
        if (expr == null) {
            return;
        }
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            ErrorReporter.error(error);
        }
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object right = evaluate(expr.expr);
        setThrowToken(expr.operator);

        return switch (expr.operator.type) {
            case MINUS -> -number(right);
            case BANG  -> !isTruthy(right);
            default    -> /* Unreachable */ null;
        };
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
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
            default    -> /* Unreachable */ null;
        };
    }

    public Object visitTernary(Expr.Ternary expr) {
        boolean condition = isTruthy(evaluate(expr.condition));
        Object first = evaluate(expr.first);
        Object second = evaluate(expr.second);
        setThrowToken(null);

        return condition ? first : second;
    }

    @Override
    public  Object visitGrouping(Expr.Grouping expr) {
        return evaluate(expr.expr);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean)obj;
        if (obj instanceof Double) return ((double)obj) == 0.0;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        else if (left == null) return false;
        return left.equals(right);
    }

    private Object evaluatePlus(Object left, Object right) {
        if (left instanceof String) {
            return left + stringify(right);
        } else if (right instanceof Double) {
            return (double)left + (double)right;
        } else {
            throw new RuntimeError(throwToken, "[Interpreter] Cannot do plus on lhs number and rhs string");
        }
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
            throw new RuntimeError(throwToken, "[Interpreter] Rhs of multiply must be a number");
        }
    }

    private Object evaluateDivide(Object left, Object right) {
        double a = number(left);
        double b = number(right);

        if (b == 0) {
            throw new RuntimeError(throwToken, "[Interpreter] Cannot divide by zero");
        }
        return a / b;
    }

    private double number(Object obj) {
        if (obj instanceof Double) return (double)obj;
        throw new RuntimeError(throwToken, "[Interpreter] Operand must be a number");
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

    public void setThrowToken(Token throwToken) {
        this.throwToken = throwToken;
    }
}
