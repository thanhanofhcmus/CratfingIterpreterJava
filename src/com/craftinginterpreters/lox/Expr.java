package com.craftinginterpreters.lox;

import java.util.List;

public abstract class Expr {

    interface Visitor<R> {
        R visitLiteralExpr(Literal expr);
        R visitAssignExpr(Assign expr);
        R visitUnaryExpr(Unary expr);
        R visitBinaryExpr(Binary expr);
        R visitTernaryExpr(Ternary expr);
        R visitGroupingExpr(Grouping expr);
        R visitVariableExpr(Variable expr);
        R visitLogicalExpr(Logical expr);
        R visitCallExpr(Call expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    public static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Assign extends Expr {
        final Token name;
        final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Unary extends Expr {
        final Token operator;
        final Expr expr;

        Unary(Token operator, Expr expr) {
            this.operator = operator;
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Ternary extends Expr {
        final Expr condition;
        final Expr first;
        final Expr second;

        Ternary(Expr condition, Expr first, Expr second) {
            this.condition = condition;
            this.first =first;
            this.second = second;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    public static class Grouping extends Expr {
        final Expr expr;

        Grouping(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Call extends Expr {
        final Expr callee;
        final Token rightParen; // for error reporting
        final List<Expr> arguments;

        Call(Expr callee, Token rightParen, List<Expr> arguments) {
            this.callee = callee;
            this.rightParen = rightParen;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }
}
