package com.craftinginterpreters.lox;

public class AstPrinter implements Expr.Visitor<String>,
                                   Stmt.Visitor<String> {
    String printExpr(Expr expr) {
        return expr.accept(this);
    }

    String printStmt(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "Null";
        return expr.value.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("= " + expr.name.lexeme, expr.value);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.expr);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize("?:", expr.condition, expr.first, expr.second);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("Group", expr.expr);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return parenthesize(expr.name.lexeme);
    }

    @Override
    public String visitExprStmt(Stmt.Expression stmt) {
        return parenthesize("ExprStmt", stmt.expr);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("Print", stmt.expr);
    }

    @Override
    public String visitVariableStmt(Stmt.Variable stmt) {
        return parenthesize("DecVar " + stmt.name.lexeme, stmt.initializer);
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
