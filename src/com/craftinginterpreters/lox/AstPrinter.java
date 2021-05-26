package com.craftinginterpreters.lox;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitLiteral(Expr.Literal expr) {
        if (expr.value == null)
            return "Null";
        return expr.value.toString();
    }

    @Override
    public String visitUnary(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.expr);
    }

    @Override
    public String visitBinary(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitTernary(Expr.Ternary expr) {
        return parenthesize("?:", expr.condition, expr.first, expr.second);
    }

    @Override
    public String visitGrouping(Expr.Grouping expr) {
        return parenthesize("Group", expr.expr);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append('(').append(name);
        for (Expr expr : exprs) {
            builder.append(' ');
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
