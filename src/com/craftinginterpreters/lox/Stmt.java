package com.craftinginterpreters.lox;

abstract class Stmt {

    interface Visitor<R> {

    }

    static class Expression extends Stmt {
        final Expr expr;

        Expression(Expr expr) {
            this.expr = expr;
        }
    }

    static class Print extends Stmt {
        final Expr expr;

        Print(Expr expr) {
            this.expr = expr;
        }
    }
}
