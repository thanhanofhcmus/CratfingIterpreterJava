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
        if (hadRuntimeError())
            System.out.println(runtimeErrorMessage);
        while (hadError()) {
            System.out.println(errorQueue.remove());
        }
        reset();
    }

    public static void printPrompt() {
        report();
        System.out.print("> ");
    }

    public static void error(int line, String actor, String message) {
        queue(line, actor, "", message);
    }

    public static void error(Token token, String actor, String message) {
        if (token.type == TokenType.EOF) {
            queue(token.line, actor, "at the end", message);
        } else {
            queue(token.line, actor, "'" + token.lexeme + "'", message);
        }
    }

    public static void error(RuntimeError error) {
        runtimeErrorMessage = error.getMessage();
    }

    private static void queue(int line, String actor, String where, String message) {
        errorQueue.add("[Line " + line + "] [" + actor + "] " + where + ": " + message);
    }
}
