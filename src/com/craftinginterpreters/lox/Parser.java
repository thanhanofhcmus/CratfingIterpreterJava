package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/*
Parse <- declaration
declaration     = varDeclaration | statement
varDeclaration  = IDENTIFIER ("=" expression)? ";"
statement       = printStatement | exprStatement
printStatement  = "print" expression ";"
exprStatement   = expression ";"
expression      = assignment
assignment      = ternary "=" VARIABLE
ternary         = commaList "?" commaList ":" commaList
commaList       = equality "," equality
equality        = comparison ("==" | "!=") comparison
comparison      = term ("<" | ">" | "<=" ">=") term
term            = factor (("+" | "-") factor)*
factor          = unary (("*" | "/") unary)*
unary           = ("!" | "-") unary | primary
primary         = "true" | "false" | "nil" | NUMBER | STRING | IDENTIFIER | group
group           = "(" expression ")"
VARIABLE        = named assignment
 */

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
                statements.add(declaration());
            }

            // make last expression always print in prompt mode
            if (Lox.isRunPrompt()) {
                var lastStmt = statements.get(statements.size() - 1);
                if (lastStmt instanceof Stmt.Expression) {
                    statements.remove(statements.size() - 1);
                    statements.add(new Stmt.Print(((Stmt.Expression) lastStmt).expr));
                }
            }

            return statements;
        } catch (ParseError error) {
            ErrorReporter.error(peek().line,"Parser", error.getMessage());
            return null;
        }
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) { return varDeclaration(); }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect a variable name");

        Expr initializer = null;
        if (match(EQUAL)) { initializer = expression(); }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Variable(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) { return printStatement(); }
        return exprStatement();
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "'print' statement expects ';' after expression");
        return new Stmt.Print(expr);
    }

    private Stmt exprStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expects ';' after expression to make an expression statement");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            // Purposely not throw to not activate panic mode
            //noinspection ThrowableNotThrown
            error(equals, "Invalid assign target");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = commaList();

        if (match(QUESTION)) {
            Expr first = commaList();
            consume(COLON, "Must have colon(':') in ternary expression");
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
            case IDENTIFIER: return new Expr.Variable(previous());
            case NUMBER: case STRING: return new Expr.Literal(token.literal);
            case LEFT_PAREN: {
                Expr expr = expression();
                consume(RIGHT_PAREN, "Expect ')' after expression");
                return expr;
            }
        }

        // quick and dirty fix for going back
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

   private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS: case FUN: case VAR: case FOR:
                case IF: case WHILE: case PRINT: case RETURN:
                    return;
            }
            advance();
        }
   }

   private Token consume(TokenType expected, String errorMessage) {
        if (check(expected)) {
            return advance();
        }
        throw error(peek(), errorMessage);
   }
}
