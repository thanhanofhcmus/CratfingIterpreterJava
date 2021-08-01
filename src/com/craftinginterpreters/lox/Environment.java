package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    Environment() { this.parent = null; }
    Environment(Environment parent) { this.parent = parent; }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        } else if (null != parent) {
            return parent.get(name);
        } else {
            throw error(name, "Undefined variable: " + name.lexeme);
        }
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (null != parent) {
            parent.assign(name, value);
        } else {
            throw error(name, "Undefined variable: " + name.lexeme);
        }
    }

    void define(Token name, Object value) {
        if (!values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else {
            throw error(name, "Variable already defined: " + name.lexeme);
        }
    }

    private RuntimeError error(Token token, String message) {
        return new RuntimeError(token, "Environment", message);
    }
}
