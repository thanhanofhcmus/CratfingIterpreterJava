package com.craftinginterpreters.lox;

public class RuntimeError extends RuntimeException {
    final Token token;
    final String actor;

    RuntimeError(Token token, String actor, String message) {
        super(message);
        this.token = token;
        this.actor = actor;
    }
}
