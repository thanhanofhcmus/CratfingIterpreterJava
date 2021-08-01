package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {

    interface Visitor<R> {
        R visitExprStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVariableStmt(Variable stmt);
        R visitBlockStmt(Block stmt);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Expression extends Stmt {
        final Expr expr;

        Expression(Expr expr) {
            this.expr = expr;
        }

       @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStmt(this);
       }
    }

    static class Print extends Stmt {
        final Expr expr;

        Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

   static class Variable extends Stmt {
        final Token name;
        final Expr initializer;

        Variable(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableStmt(this);
        }
   }

   static class Block extends Stmt {
        final List<Stmt> stmts;

        Block(List<Stmt> stmts) {
            this.stmts = stmts;
        }

        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
   }
}
