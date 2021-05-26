package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(statement());
            }
            return statements;
        } catch (ParseError error) {
            ErrorReporter.error(peek().line,"Parser", error.getMessage());
            return null;
        }
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        return exprStatement();
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Print expects ';' after value");
        return new Stmt.Print(expr);
    }

    private Stmt exprStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expects ';' after expression to make expression statement");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return ternary();
    }

    private Expr ternary() {
        Expr expr = commaList();

        if (match(QUESTION)) {
            Expr first = commaList();
            consume(COLON, "[Parser] Must have colon in ternary operation");
            Expr second = commaList();
            expr = new Expr.Ternary(expr, first, second);
        }

        return expr;
    }

    private Expr commaList() {
        Expr expr = equality();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        } else {
            return primary();
        }
    }

    private Expr primary() {
        Token token = advance();

        switch (token.type) {
            case FALSE: return new Expr.Literal(false);
            case TRUE:  return new Expr.Literal(true);
            case NIL:   return new Expr.Literal(null);
            case NUMBER: case STRING: return new Expr.Literal(token.literal);
            case LEFT_PAREN: {
                Expr expr = expression();
                consume(RIGHT_PAREN, "Expect ')' after expression");
                return expr;
            }
        }

        // quick fix for going back
        current--;
        throw error(token, "Expected an expression");
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

   private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
   }

   private ParseError error(Token token, String message) {
       ErrorReporter.error(token, "Parser", message);
        return new ParseError();
   }

   private Token consume(TokenType expected, String errorMessage) {
        if (check(expected)) {
            return advance();
        }
        throw error(peek(), errorMessage);
   }
}
