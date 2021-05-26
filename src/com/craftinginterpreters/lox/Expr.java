package com.craftinginterpreters.lox;

abstract class Expr {

    interface Visitor<R> {
        R visitLiteral(Literal expr);
        R visitUnary(Unary expr);
        R visitBinary(Binary expr);
        R visitTernary(Ternary expr);
        R visitGrouping(Grouping expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr expr;

        Unary(Token operator, Expr expr) {
            this.operator = operator;
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }
    }

    static class Binary extends Expr {
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
            return visitor.visitBinary(this);
        }
    }

    static class Ternary extends Expr {
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
            return visitor.visitTernary(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expr;

        Grouping(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }
    }
}
