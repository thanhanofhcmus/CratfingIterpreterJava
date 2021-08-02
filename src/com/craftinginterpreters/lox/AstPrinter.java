package com.craftinginterpreters.lox;

public class AstPrinter implements Expr.Visitor<String>,
                                   Stmt.Visitor<String> {

    private int indentLevel = 0;
    private final int numOfSpaces = 2;

    String printExpr(Expr expr) {
        return expr.accept(this);
    }

    String printStmt(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (null == expr.value) { return "Null"; }
        else                    { return expr.value.toString(); }
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
    public String visitLogicalExpr(Expr.Logical expr) {
        return "";
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

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();

        builder.append(" ".repeat(indentLevel * numOfSpaces));
        indentLevel += 1;
        builder.append("Block {");
        for (Stmt statement : stmt.stmts) {
            if (null != statement) {
                builder.append('\n');
                builder.append(new AstPrinter().printStmt(statement));
            }
        }
        builder.append("\n}");
        indentLevel -= 1;

        return builder.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("( IfBlock (\n");
        builder.append(parenthesize("Cond", stmt.condition));

        builder.append("\nIfClause ");
        builder.append(new AstPrinter().printStmt(stmt.ifBlock));
        if (null != stmt.elseBlock) {
            builder.append("\nElseClause");
            builder.append(new AstPrinter().printStmt(stmt.elseBlock));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "";
    }

    @Override
    public String visitForStmt(Stmt.For stmt) {
        return "";
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public String visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append(" ".repeat(indentLevel * numOfSpaces));
        builder.append('(').append(name);
        for (Expr expr : exprs) {
            if (null != expr) {
                builder.append(' ');
                builder.append(expr.accept(this));
            }
        }
        builder.append(")");

        return builder.toString();
    }
}
