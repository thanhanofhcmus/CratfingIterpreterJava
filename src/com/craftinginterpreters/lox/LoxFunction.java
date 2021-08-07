package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.params.size(); ++i) {
            environment.define(declaration.params.get(i), args.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Interpreter.ReturnStmt returnStmt) {
            return returnStmt.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn>$" + declaration.name.lexeme;
    }
}
