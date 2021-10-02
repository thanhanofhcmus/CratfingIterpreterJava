package com.craftinginterpreters.lox;

import java.util.Queue;
import java.util.ArrayDeque;

public class ErrorReporter {
    private static final Queue<String> errorQueue = new ArrayDeque<>();
    private static String runtimeErrorMessage;

    public static void reset() {
        errorQueue.clear();
        runtimeErrorMessage = null;
    }

    public static boolean hadError() {
        return !errorQueue.isEmpty();
    }

    public static boolean hadRuntimeError() {
        return runtimeErrorMessage != null;
    }

    public static void report() {
        if (hadRuntimeError()) {
            System.out.println(runtimeErrorMessage);
        }
        while (hadError()) {
            System.out.println(errorQueue.remove());
        }
        reset();
    }

    public static void printPrompt() {
        report();
        System.out.print("> ");
    }

    public static void error(int line, int column, String actor, String message) {
        queue(line, column, actor, "", message);
    }

    public static void error(Token token, String actor, String message) {
        queue(token.line, token.column, actor,
                (token.type == TokenType.EOF) ? "at the end" : "'" + token.lexeme + "'", message);
    }

    public static void error(RuntimeError error) {
        runtimeErrorMessage = String.format("[%s:%s] [%s]: %s",
                error.token.line, error.token.column, error.actor, error.getMessage());
    }

    private static void queue(int line, int column, String actor, String where, String message) {
        errorQueue.add(String.format("[%s:%s] [%s] %s: %s", line, column, actor, where, message));
    }
}
