package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {

    interface Visitor<R> {
        R visitExprStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVariableStmt(Variable stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitBreakStmt(Break stmt);
        R visitContinueStmt(Continue stmt);
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

   static class If extends Stmt {
        final Expr condition;
        final Stmt ifBlock;
        final Stmt elseBlock;

        If(Expr condition, Stmt ifBlock, Stmt elseBlock) {
            this.condition = condition;
            this.ifBlock = ifBlock;
            this.elseBlock = elseBlock;
        }

        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
   }

   static class While extends Stmt {
        final Expr condition;
        final Stmt block;

        While(Expr condition, Stmt block) {
            this.condition = condition;
            this.block = block;
        }

        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
   }

   static class For extends Stmt {
        final Stmt init;
        final Expr condition;
        final Expr increase;
        final Stmt block;

        For(Stmt init, Expr condition, Expr increase, Stmt block) {
            this.init = init;
            this.condition = condition;
            this.increase = increase;
            this.block = block;
        }

        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
   }

   static class Break extends Stmt {
        @Override
       <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }
   }

   static class Continue extends Stmt {
       @Override
       <R> R accept(Visitor<R> visitor) {
           return visitor.visitContinueStmt(this);
       }
   }
}
